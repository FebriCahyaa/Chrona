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

package com.febricahyaa.chrona.timer;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.febricahyaa.chrona.DeskClock;
import com.febricahyaa.chrona.R;
import com.febricahyaa.chrona.data.DataModel;
import com.febricahyaa.chrona.data.Timer;
import com.febricahyaa.chrona.widget.MockFabContainer;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Locale;

import static android.text.format.DateUtils.HOUR_IN_MILLIS;
import static android.text.format.DateUtils.MINUTE_IN_MILLIS;
import static android.text.format.DateUtils.SECOND_IN_MILLIS;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Exercise the user interface that collects new timer lengths.
 */
@RunWith(AndroidJUnit4ClassRunner.class)
public class TimerSetupViewTest {

    private MockFabContainer fabContainer;
    private TimerSetupView timerSetupView;

    private View timeView;
    private TextView hoursView;
    private TextView minutesView;
    private TextView secondsView;
    private View startView;
    private View deleteView;

    private Locale defaultLocale;

    @Rule
    public ActivityTestRule<DeskClock> rule = new ActivityTestRule<>(DeskClock.class, true);

    @Before
    public void setUp() {
        defaultLocale = Locale.getDefault();
        Locale.setDefault(new Locale("en", "US"));
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        final TimerFragment fragment = new TimerFragment();
        rule.getActivity().getSupportFragmentManager()
                .beginTransaction().add(fragment, null).commit();
        Runnable selectTabRunnable = () -> {
            fragment.selectTab();
            fabContainer = new MockFabContainer(fragment, context);
            fragment.setFabContainer(fabContainer);

            // Fetch the child views the tests will manipulate.
            final View view = fragment.getView();
            assertNotNull(view);

            timerSetupView = view.findViewById(R.id.timer_setup);
            assertNotNull(timerSetupView);
            assertEquals(VISIBLE, timerSetupView.getVisibility());

            timeView = timerSetupView.findViewById(R.id.timer_setup_time);
            timeView.setActivated(true);
            hoursView = timerSetupView.findViewById(R.id.timer_setup_hours);
            minutesView = timerSetupView.findViewById(R.id.timer_setup_minutes);
            secondsView = timerSetupView.findViewById(R.id.timer_setup_seconds);
            startView = timerSetupView.findViewById(R.id.timer_setup_start);
            deleteView = timerSetupView.findViewById(R.id.timer_setup_delete);
        };
        InstrumentationRegistry.getInstrumentation().runOnMainSync(selectTabRunnable);
    }

    @After
    public void tearDown() {
        fabContainer = null;
        timerSetupView = null;

        timeView = null;
        deleteView = null;

        Locale.setDefault(defaultLocale);
    }

    private void validateDefaultState() {
        assertIsReset();
    }

    @Test
    public void validateDefaultState_TimersExist() {
        Runnable addTimerRunnable = () -> {
            Timer timer = DataModel.getDataModel().addTimer(5000L, "", false);
            validateDefaultState();
            DataModel.getDataModel().removeTimer(timer);
        };
        InstrumentationRegistry.getInstrumentation().runOnMainSync(addTimerRunnable);
    }

    @Test
    public void validateDefaultState_NoTimersExist() {
        Runnable runnable = () -> {
            validateDefaultState();
        };
        InstrumentationRegistry.getInstrumentation().runOnMainSync(runnable);
    }

    private void type0InDefaultState() {
        performClick(R.id.timer_setup_digit_0);
        assertIsReset();
    }

    @Test
    public void type0InDefaultState_TimersExist() {
        Runnable addTimerRunnable = () -> {
            Timer timer = DataModel.getDataModel().addTimer(5000L, "", false);
            type0InDefaultState();
            DataModel.getDataModel().removeTimer(timer);
        };
        InstrumentationRegistry.getInstrumentation().runOnMainSync(addTimerRunnable);
    }

    @Test
    public void type0InDefaultState_NoTimersExist() {
        Runnable runnable = () -> {
            type0InDefaultState();
        };
        InstrumentationRegistry.getInstrumentation().runOnMainSync(runnable);
    }

    private void fillDisplayThenDeleteAll() {
        assertIsReset();
        // Fill the display.
        performClick(R.id.timer_setup_digit_1);
        assertHasValue(0, 0, 1);
        performClick(R.id.timer_setup_digit_2);
        assertHasValue(0, 0, 12);
        performClick(R.id.timer_setup_digit_3);
        assertHasValue(0, 1, 23);
        performClick(R.id.timer_setup_digit_4);
        assertHasValue(0, 12, 34);
        performClick(R.id.timer_setup_digit_5);
        assertHasValue(1, 23, 45);
        performClick(R.id.timer_setup_digit_6);
        assertHasValue(12, 34, 56);

        // Typing another character is ignored.
        performClick(R.id.timer_setup_digit_7);
        assertHasValue(12, 34, 56);
        performClick(R.id.timer_setup_digit_8);
        assertHasValue(12, 34, 56);

        // Delete everything in the display.
        performClick(R.id.timer_setup_delete);
        assertHasValue(1, 23, 45);
        performClick(R.id.timer_setup_delete);
        assertHasValue(0, 12, 34);
        performClick(R.id.timer_setup_delete);
        assertHasValue(0, 1, 23);
        performClick(R.id.timer_setup_delete);
        assertHasValue(0, 0, 12);
        performClick(R.id.timer_setup_delete);
        assertHasValue(0, 0, 1);
        performClick(R.id.timer_setup_delete);
        assertIsReset();
    }

    @Test
    public void fillDisplayThenDeleteAll_TimersExist() {
        Runnable addTimerRunnable = () -> {
            Timer timer = DataModel.getDataModel().addTimer(5000L, "", false);
            fillDisplayThenDeleteAll();
            DataModel.getDataModel().removeTimer(timer);
        };
        InstrumentationRegistry.getInstrumentation().runOnMainSync(addTimerRunnable);
    }

    @Test
    public void fillDisplayThenDeleteAll_NoTimersExist() {
        Runnable runnable = () -> {
            fillDisplayThenDeleteAll();
        };
        InstrumentationRegistry.getInstrumentation().runOnMainSync(runnable);
    }

    private void fillDisplayWith9s() {
        performClick(R.id.timer_setup_digit_9);
        performClick(R.id.timer_setup_digit_9);
        performClick(R.id.timer_setup_digit_9);
        performClick(R.id.timer_setup_digit_9);
        performClick(R.id.timer_setup_digit_9);
        performClick(R.id.timer_setup_digit_9);
        assertHasValue(99, 99, 99);
    }

    @Test
    public void fillDisplayWith9s_TimersExist() {
        Runnable addTimerRunnable = () -> {
            Timer timer = DataModel.getDataModel().addTimer(5000L, "", false);
            fillDisplayWith9s();
            DataModel.getDataModel().removeTimer(timer);
        };
        InstrumentationRegistry.getInstrumentation().runOnMainSync(addTimerRunnable);
    }

    @Test
    public void fillDisplayWith9s_NoTimersExist() {
        Runnable runnable = () -> {
            fillDisplayWith9s();
        };
        InstrumentationRegistry.getInstrumentation().runOnMainSync(runnable);
    }

    private void assertIsReset() {
        assertStateEquals(0, 0, 0);
        assertFalse(timerSetupView.hasValidInput());
        assertEquals(0, timerSetupView.getTimeInMillis());

        assertValues(0, 0, 0);
        assertFalse(startView.isEnabled());
        assertTrue(TextUtils.equals("0 hours, 0 minutes, 0 seconds",
                timeView.getContentDescription()));

        assertFalse(deleteView.isEnabled());
        assertTrue(TextUtils.equals("Delete", deleteView.getContentDescription()));

        final View fab = fabContainer.getFab();
        final TextView leftButton = fabContainer.getLeftButton();
        final TextView rightButton = fabContainer.getRightButton();

        if (DataModel.getDataModel().getTimers().isEmpty()) {
            assertEquals(INVISIBLE, leftButton.getVisibility());
        } else {
            assertEquals(VISIBLE, leftButton.getVisibility());
            assertTrue(TextUtils.equals("Cancel", leftButton.getText()));
        }

        assertNull(fab.getContentDescription());
        assertEquals(INVISIBLE, fab.getVisibility());
        assertEquals(INVISIBLE, rightButton.getVisibility());
    }

    private void assertHasValue(int hours, int minutes, int seconds) {
        final long time =
                hours * HOUR_IN_MILLIS + minutes * MINUTE_IN_MILLIS + seconds * SECOND_IN_MILLIS;
        assertStateEquals(hours, minutes, seconds);
        assertTrue(timerSetupView.hasValidInput());
        assertEquals(time, timerSetupView.getTimeInMillis());

        assertValues(hours, minutes, seconds);
        assertTrue(startView.isEnabled());

        assertTrue(deleteView.isEnabled());
        assertTrue(TextUtils.equals("Delete " + seconds % 10, deleteView.getContentDescription()));

        final View fab = fabContainer.getFab();
        final TextView leftButton = fabContainer.getLeftButton();
        final TextView rightButton = fabContainer.getRightButton();

        if (DataModel.getDataModel().getTimers().isEmpty()) {
            assertEquals(INVISIBLE, leftButton.getVisibility());
        } else {
            assertEquals(VISIBLE, leftButton.getVisibility());
            assertTrue(TextUtils.equals("Cancel", leftButton.getText()));
        }

        // The setup view's own start button replaces the shared fab.
        assertEquals(INVISIBLE, fab.getVisibility());
        assertEquals(INVISIBLE, rightButton.getVisibility());
    }

    private void assertValues(int hours, int minutes, int seconds) {
        assertTrue(TextUtils.equals(String.format(Locale.US, "%02d", hours), hoursView.getText()));
        assertTrue(TextUtils.equals(String.format(Locale.US, "%02d", minutes),
                minutesView.getText()));
        assertTrue(TextUtils.equals(String.format(Locale.US, "%02d", seconds),
                secondsView.getText()));
    }

    private void assertStateEquals(int hours, int minutes, int seconds) {
        final int[] expected = {
                seconds % 10, seconds / 10, minutes % 10, minutes / 10, hours % 10, hours / 10
        };
        final int[] actual = (int[]) timerSetupView.getState();
        assertArrayEquals(expected, actual);
    }

    private void performClick(@IdRes int id) {
        final View view = timerSetupView.findViewById(id);
        assertNotNull(view);
        assertTrue(view.performClick());
    }
}
