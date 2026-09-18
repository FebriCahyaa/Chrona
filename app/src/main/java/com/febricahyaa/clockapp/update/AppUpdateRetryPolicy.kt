/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.update

import com.febricahyaa.clockapp.data.update.GitHubReleaseException
import java.io.IOException

object AppUpdateRetryPolicy {
    fun shouldRetry(error: Throwable): Boolean = when (error) {
        is GitHubReleaseException -> error.httpCode == 429 || error.httpCode in 500..599
        is IOException -> true
        else -> false
    }
}
