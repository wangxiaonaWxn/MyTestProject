package com.mega.scenemode.view;

import android.content.Context;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;

import com.mega.scenemode.R;

import mega.log.MLog;
import mega.widget.FastWidget;

/**
 * Implementation of App Widget functionality.
 */
public class BigsurSceneAppWidget extends FastWidget {
    public BigsurSceneAppWidget() {
        MLog.d("BigsurSceneAppWidget");
    }

    @Override
    public void onEnabled(@NonNull Context context) {
        MLog.d("onEnabled");
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(@NonNull Context context) {
        MLog.d("onDisabled context: " + context);
        super.onDisabled(context);
    }

    public @NonNull RemoteViews getDisplayViews(@NonNull Context context) {
        MLog.d("getDisplayViews");
        RemoteViews result = new RemoteViews(context.getPackageName(), R.layout.scene_app_widget);
        return result;
    }
}