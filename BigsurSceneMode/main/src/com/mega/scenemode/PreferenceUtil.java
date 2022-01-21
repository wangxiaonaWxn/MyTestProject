package com.mega.scenemode;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import mega.log.MLog;

/**
 * PreferencesUtils, easy to get or put data
 */
public class PreferenceUtil {
    private static final String PF_NAME = "scene_mode";

    /**
     * save date to prefers
     */
    public static void put(@NonNull String key, @NonNull Object obj) {
        SharedPreferences sp = SceneModeApplication.getInstance()
                .getSharedPreferences(PF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        if (obj instanceof Boolean) {
            editor.putBoolean(key, (Boolean) obj);
        } else if (obj instanceof Float) {
            editor.putFloat(key, (Float) obj);
        } else if (obj instanceof Integer) {
            editor.putInt(key, (Integer) obj);
        } else if (obj instanceof Long) {
            editor.putLong(key, (Long) obj);
        } else {
            editor.putString(key, (String) obj);
        }
        editor.apply();
    }

    /**
     * get the specific data
     */
    public static @NonNull Object get(@NonNull String key, @NonNull Object defaultObj) {
        SharedPreferences sp = SceneModeApplication.getInstance()
                .getSharedPreferences(PF_NAME, Context.MODE_PRIVATE);
        if (defaultObj instanceof Boolean) {
            return sp.getBoolean(key, (Boolean) defaultObj);
        } else if (defaultObj instanceof Float) {
            return sp.getFloat(key, (Float) defaultObj);
        } else if (defaultObj instanceof Integer) {
            return sp.getInt(key, (Integer) defaultObj);
        } else if (defaultObj instanceof Long) {
            return sp.getLong(key, (Long) defaultObj);
        } else if (defaultObj instanceof String) {
            return sp.getString(key, (String) defaultObj);
        }
        return defaultObj;
    }

    /**
     * remove the specific date
     */
    public static void remove(@NonNull String key) {
        SharedPreferences sp = SceneModeApplication.getInstance()
                .getSharedPreferences(PF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(key);
        editor.apply();
    }

    /**
     * remove all date in prefers
     */
    public static void clear() {
        SharedPreferences sp = SceneModeApplication.getInstance()
                .getSharedPreferences(PF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.apply();
    }

    /**
     * check whether the key corresponding to the data exists
     */
    public static boolean contains(@NonNull String key) {
        SharedPreferences sp = SceneModeApplication.getInstance()
                .getSharedPreferences(PF_NAME, Context.MODE_PRIVATE);
        MLog.d("key set : " + sp.getAll().keySet());
        return sp.contains(key);
    }
}
