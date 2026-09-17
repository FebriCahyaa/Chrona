/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;

import com.febricahyaa.clockapp.R;

/** Battery-conscious home-screen widget. TextClock updates its displayed time itself. */
public final class ChronaClockWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_chrona_clock);
        for (int id : appWidgetIds) {
            appWidgetManager.updateAppWidget(id, views);
        }
    }
}
