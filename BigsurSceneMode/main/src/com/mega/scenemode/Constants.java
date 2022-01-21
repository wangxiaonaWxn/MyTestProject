package com.mega.scenemode;

public class Constants {
    public static final int MODE_ITEM_TOTAL = 5;
    public static final int RECYCLER_VIEW_HEIGHT = 900;
    //recyclerView 的中心点
    public static final int CENTER_RECYCLER_VIEW_Y = 450;
    //MenuItem 的高度
    public static final int MENU_ITEM_HEIGHT = 180;
    //一次滑动耗时
    public static final int SCROLL_DURATION = 500;

    public static final String KEY_NAP_AUDIO = "nap_audio";
    public static final String KEY_NAP_TOTAL_TIME = "nap_total_time";
    public static final String KEY_DISTURB_MODE = "phone_disturb";
    public static final int KEY_DISTURB_CLOSE = 0;
    public static final int KEY_DISTURB_OPEN = 1;
    public static final int NAP_AUDIO_FIRST = 0;
    public static final int NAP_AUDIO_SECOND = 1;
    public static final int NAP_AUDIO_THIRD = 2;
    public static final int NAP_SETTING_TOTAL_TIME = 120;
    public static final int NAP_SETTING_DEFAULT_TIME = 20;

    public static final int VEHICLE_STATE_SOC_POWER_25 = 25;
    public static final int VEHICLE_STATE_SOC_POWER_20 = 20;
    public static final int VEHICLE_STATE_SOC_POWER_NAP_MIN = 18;
    public static final int VEHICLE_STATE_SOC_POWER_TEMPERATURE_MIN = 10;

    public static final int VEHICLE_STATE_THERMOSTATIC_OFF = 0;
    public static final int VEHICLE_STATE_THERMOSTATIC_NORMAL = 1;
    public static final int VEHICLE_STATE_THERMOSTATIC_LOW_POWER = 2;

    public enum MenuType {
        NAP,
        SMOKING,
        TEMPERATURE,
        DISTURB,
        CAMPING
    }
}
