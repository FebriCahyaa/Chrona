/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.deskclock

import android.annotation.SuppressLint
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import androidx.core.os.BundleCompat
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat


import kotlin.math.pow

/**
 * This class controls playback of ringtones. Uses [MediaPlayer] in a dedicated thread so that
 * this class can be called from the main thread. Consequently, problems controlling the
 * ringtone do not cause ANRs in the main thread of the application.
 *
 * Playback happens in this app's own [MediaPlayer] with alarm audio attributes, so the sound
 * is attributed to this app and follows the alarm volume. Custom ringtones are read through
 * the persistable URI permission granted when the user picked them.
 *
 * If the requested audio fails to play, the system default alarm and then an
 * [in-app fallback][.getFallbackRingtoneUri] are tried, because playing **some**
 * sort of noise is always preferable to remaining silent.
 */
class AsyncRingtonePlayer(private val mContext: Context) {
    /** Handler running on the ringtone thread.  */
    private var mHandler: Handler? = null

    private var mPlaybackDelegate: PlaybackDelegate? = null

    /** Plays the ringtone.  */
    fun play(ringtoneUri: Uri?, crescendoDuration: Long) {
        LOGGER.d("Posting play.")
        postMessage(EVENT_PLAY, ringtoneUri, crescendoDuration, 0)
    }

    /** Stops playing the ringtone.  */
    fun stop() {
        LOGGER.d("Posting stop.")
        postMessage(EVENT_STOP, null, 0, 0)
    }

    /** Schedules an adjustment of the playback volume 50ms in the future.  */
    private fun scheduleVolumeAdjustment() {
        LOGGER.v("Adjusting volume.")

        // Ensure we never have more than one volume adjustment queued.
        mHandler!!.removeMessages(EVENT_VOLUME)

        // Queue the next volume adjustment.
        postMessage(EVENT_VOLUME, null, 0, 50)
    }

    /**
     * Posts a message to the ringtone-thread handler.
     *
     * @param messageCode the message to post
     * @param ringtoneUri the ringtone in question, if any
     * @param crescendoDuration the length of time, in ms, over which to crescendo the ringtone
     * @param delayMillis the amount of time to delay sending the message, if any
     */
    private fun postMessage(
        messageCode: Int,
        ringtoneUri: Uri?,
        crescendoDuration: Long,
        delayMillis: Long
    ) {
        synchronized(this) {
            if (mHandler == null) {
                mHandler = getNewHandler()
            }

            val message = mHandler!!.obtainMessage(messageCode)
            if (ringtoneUri != null) {
                val bundle = Bundle()
                bundle.putParcelable(RINGTONE_URI_KEY, ringtoneUri)
                bundle.putLong(CRESCENDO_DURATION_KEY, crescendoDuration)
                message.data = bundle
            }

            mHandler!!.sendMessageDelayed(message, delayMillis)
        }
    }

    /**
     * Creates a new ringtone Handler running in its own thread.
     */
    @SuppressLint("HandlerLeak")
    private fun getNewHandler(): Handler {
            val thread = HandlerThread("ringtone-player")
            thread.start()

            return object : Handler(thread.looper) {
                override fun handleMessage(msg: Message) {
                    when (msg.what) {
                        EVENT_PLAY -> {
                            val data = msg.data
                            val ringtoneUri = BundleCompat.getParcelable(
                                    data, RINGTONE_URI_KEY, Uri::class.java)
                            val crescendoDuration = data.getLong(CRESCENDO_DURATION_KEY)
                            if (playbackDelegate.play(mContext, ringtoneUri, crescendoDuration)) {
                                scheduleVolumeAdjustment()
                            }
                        }
                        EVENT_STOP -> playbackDelegate.stop(mContext)
                        EVENT_VOLUME -> if (playbackDelegate.adjustVolume(mContext)) {
                            scheduleVolumeAdjustment()
                        }
                    }
                }
            }
        }

    /**
     * Check if the executing thread is the one dedicated to controlling the ringtone playback.
     */
    private fun checkAsyncRingtonePlayerThread() {
        if (Looper.myLooper() != mHandler!!.looper) {
            LOGGER.e("Must be on the AsyncRingtonePlayer thread!",
                    IllegalStateException())
        }
    }

    /**
     * @return the platform-specific playback delegate to use to play the ringtone
     */
    private val playbackDelegate: PlaybackDelegate
        get() {
            checkAsyncRingtonePlayerThread()
            if (mPlaybackDelegate == null) {
                mPlaybackDelegate = MediaPlayerPlaybackDelegate()
            }
            return mPlaybackDelegate!!
        }

    /** Playback of the ringtone on the ringtone thread. */
    private interface PlaybackDelegate {
        /**
         * @return `true` iff a [volume adjustment][.adjustVolume] should be scheduled
         */
        fun play(context: Context, ringtoneUri: Uri?, crescendoDuration: Long): Boolean

        /**
         * Stop any ongoing ringtone playback.
         */
        fun stop(context: Context?)

        /**
         * @return `true` iff another volume adjustment should be scheduled
         */
        fun adjustVolume(context: Context?): Boolean
    }

    /**
     * Plays the alarm with a [MediaPlayer] owned by this app, attributed as an alarm
     * ([AudioAttributes.USAGE_ALARM]). android.media.Ringtone could hand playback to the
     * system's remote ringtone player, which made the sound appear to come from Android System
     * rather than this app.
     */
    private inner class MediaPlayerPlaybackDelegate : PlaybackDelegate {
        /** The audio focus manager. Only used by the ringtone thread.  */
        private var mAudioManager: AudioManager? = null
        private var mAudioFocusRequest: AudioFocusRequest? = null

        /** The current player. Only used by the ringtone thread.  */
        private var mMediaPlayer: MediaPlayer? = null

        /** The duration over which to increase the volume.  */
        private var mCrescendoDuration: Long = 0

        /** The time at which the crescendo shall cease; 0 if no crescendo is present.  */
        private var mCrescendoStopTime: Long = 0

        /**
         * Starts the actual playback of the ringtone. Executes on ringtone-thread.
         */
        override fun play(context: Context, ringtoneUri: Uri?, crescendoDuration: Long): Boolean {
            checkAsyncRingtonePlayerThread()
            mCrescendoDuration = crescendoDuration

            LOGGER.i("Play ringtone via android.media.MediaPlayer.")

            if (mAudioManager == null) {
                mAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            }

            val inTelephoneCall = isInTelephoneCall(context)
            val candidates = if (inTelephoneCall) {
                listOf(getInCallRingtoneUri(context))
            } else {
                listOfNotNull(ringtoneUri,
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                        getFallbackRingtoneUri(context))
            }

            for (uri in candidates) {
                try {
                    return startPlayback(context, uri, inTelephoneCall)
                } catch (e: Exception) {
                    LOGGER.e("Unable to play $uri, trying the next fallback", e)
                    releasePlayer()
                }
            }

            LOGGER.e("Failed to play any alarm ringtone")
            return false
        }

        /**
         * Prepare the player for playback, then start the playback.
         *
         * @param inTelephoneCall `true` if there is currently an active telephone call
         * @return `true` if a crescendo has started and future volume adjustments are
         * required to advance the crescendo effect
         */
        private fun startPlayback(context: Context, uri: Uri, inTelephoneCall: Boolean): Boolean {
            val player = MediaPlayer()
            mMediaPlayer = player
            player.setAudioAttributes(ALARM_AUDIO_ATTRIBUTES)
            player.setOnErrorListener { _, what, extra ->
                LOGGER.e("Error occurred while playing audio: what=$what extra=$extra")
                stop(context)
                true
            }
            player.setDataSource(context, uri)
            player.isLooping = true
            player.prepare()

            // Attempt to adjust the ringtone volume if the user is in a telephone call.
            var scheduleVolumeAdjustment = false
            if (inTelephoneCall) {
                LOGGER.v("Using the in-call alarm")
                setPlayerVolume(IN_CALL_VOLUME)
            } else if (mCrescendoDuration > 0) {
                setPlayerVolume(0f)

                // Compute the time at which the crescendo will stop.
                mCrescendoStopTime = Utils.now() + mCrescendoDuration
                scheduleVolumeAdjustment = true
            }

            requestAudioFocus()
            player.start()

            return scheduleVolumeAdjustment
        }

        private fun requestAudioFocus() {
            if (mAudioFocusRequest == null) {
                mAudioFocusRequest = AudioFocusRequest.Builder(
                        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                        .setAudioAttributes(ALARM_AUDIO_ATTRIBUTES)
                        .build()
            }
            mAudioManager!!.requestAudioFocus(mAudioFocusRequest!!)
        }

        /**
         * Sets the volume of the player.
         *
         * @param volume a raw scalar in range 0.0 to 1.0, where 0.0 mutes this player, and 1.0
         * corresponds to no attenuation being applied.
         */
        private fun setPlayerVolume(volume: Float) {
            mMediaPlayer?.setVolume(volume, volume)
        }

        private fun releasePlayer() {
            mMediaPlayer?.let { player ->
                try {
                    if (player.isPlaying) {
                        player.stop()
                    }
                } catch (e: IllegalStateException) {
                    LOGGER.w("MediaPlayer was not in a stoppable state: $e")
                }
                player.release()
            }
            mMediaPlayer = null
        }

        /**
         * Stops the playback of the ringtone. Executes on the ringtone-thread.
         */
        override fun stop(context: Context?) {
            checkAsyncRingtonePlayerThread()

            LOGGER.i("Stop ringtone via android.media.MediaPlayer.")

            mCrescendoDuration = 0
            mCrescendoStopTime = 0

            releasePlayer()

            mAudioFocusRequest?.let { request ->
                mAudioManager?.abandonAudioFocusRequest(request)
                mAudioFocusRequest = null
            }
        }

        /**
         * Adjusts the volume of the ringtone being played to create a crescendo effect.
         */
        override fun adjustVolume(context: Context?): Boolean {
            checkAsyncRingtonePlayerThread()

            // If the player is absent or not playing, ignore volume adjustment.
            val player = mMediaPlayer
            if (player == null || !player.isPlaying) {
                mCrescendoDuration = 0
                mCrescendoStopTime = 0
                return false
            }

            // If the crescendo is complete set the volume to the maximum; we're done.
            val currentTime = Utils.now()
            if (currentTime > mCrescendoStopTime) {
                mCrescendoDuration = 0
                mCrescendoStopTime = 0
                setPlayerVolume(1f)
                return false
            }

            val volume = computeVolume(currentTime, mCrescendoStopTime, mCrescendoDuration)
            setPlayerVolume(volume)

            // Schedule the next volume bump in the crescendo.
            return true
        }
    }

    companion object {
        private val LOGGER = LogUtils.Logger("AsyncRingtonePlayer")
        private val ALARM_AUDIO_ATTRIBUTES = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

        // Volume suggested by media team for in-call alarms.
        private const val IN_CALL_VOLUME = 0.125f

        // Message codes used with the ringtone thread.
        private const val EVENT_PLAY = 1
        private const val EVENT_STOP = 2
        private const val EVENT_VOLUME = 3
        private const val RINGTONE_URI_KEY = "RINGTONE_URI_KEY"
        private const val CRESCENDO_DURATION_KEY = "CRESCENDO_DURATION_KEY"

        /**
         * @return `true` iff the device is currently in a telephone call
         */
        @Suppress("DEPRECATION")
        private fun isInTelephoneCall(context: Context): Boolean {
            if (ContextCompat.checkSelfPermission(
                            context, Manifest.permission.READ_PHONE_STATE) !=
                    PackageManager.PERMISSION_GRANTED) {
                return false
            }
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            return tm.callState != TelephonyManager.CALL_STATE_IDLE
        }

        /**
         * @return Uri of the ringtone to play when the user is in a telephone call
         */
        private fun getInCallRingtoneUri(context: Context): Uri {
            return Utils.getResourceUri(context, R.raw.alarm_expire)
        }

        /**
         * @return Uri of the ringtone to play when the chosen ringtone fails to play
         */
        private fun getFallbackRingtoneUri(context: Context): Uri {
            return Utils.getResourceUri(context, R.raw.alarm_expire)
        }

        /**
         * @param currentTime current time of the device
         * @param stopTime time at which the crescendo finishes
         * @param duration length of time over which the crescendo occurs
         * @return the scalar volume value that produces a linear increase in volume (in decibels)
         */
        private fun computeVolume(currentTime: Long, stopTime: Long, duration: Long): Float {
            // Compute the percentage of the crescendo that has completed.
            val elapsedCrescendoTime = stopTime - currentTime.toFloat()
            val fractionComplete = 1 - elapsedCrescendoTime / duration

            // Use the fraction to compute a target decibel between
            // -40dB (near silent) and 0dB (max).
            val gain = fractionComplete * 40 - 40

            // Convert the target gain (in decibels) into the corresponding volume scalar.
            val volume = 10.0.pow(gain / 20f.toDouble()).toFloat()

            LOGGER.v("Ringtone crescendo %,.2f%% complete (scalar: %f, volume: %f dB)",
                    fractionComplete * 100, volume, gain)

            return volume
        }
    }
}