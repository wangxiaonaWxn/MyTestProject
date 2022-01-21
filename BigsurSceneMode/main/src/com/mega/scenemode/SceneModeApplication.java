package com.mega.scenemode;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;

public class SceneModeApplication extends Application {
    private static SceneModeApplication sInstance;

    public static synchronized @NonNull Application getInstance() {
        return sInstance;
    }

    @Override
    protected void attachBaseContext(@NonNull Context base) {
        super.attachBaseContext(base);
        sInstance = this;
    }
}