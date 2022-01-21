package com.mega.scenemode.manager;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.mega.scenemode.SceneModeApplication;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import mega.car.CarPropertyManager;
import mega.car.MegaCarPropHelper;
import mega.car.config.Driving;
import mega.car.config.ElecPower;
import mega.car.config.ParamsCommon;
import mega.car.hardware.CarPropertyValue;
import mega.log.MLog;

import static com.mega.scenemode.Constants.VEHICLE_STATE_SOC_POWER_20;
import static com.mega.scenemode.Constants.VEHICLE_STATE_SOC_POWER_25;
import static mega.car.config.Cabin.ID_THERMOSTATIC_COCKPIT;
import static mega.car.config.Dms.ID_SMOKE_MODE_SWITCH;
import static mega.car.config.Driving.ID_BRAKE_PEDAL_STATUS;
import static mega.car.config.Driving.ID_DRIVER_INFO_STS_VEH_POWER;
import static mega.car.config.Driving.ID_DRV_INFO_GEAR_POSITION;
import static mega.car.config.ElecPower.ID_EP_HV_BATT_CHG_STATUS;
import static mega.car.config.ElecPower.ID_HV_PERCENT;
import static mega.car.config.EntryLocks.ID_DOOR;
import static mega.car.config.Infotainment.ID_CAMPING_MODE;
import static mega.car.config.Warnings.ID_DRIVER_INFO_STS_DOOR_AJAR_WARN;

/**
 * 与 CarService 通信类
 */
public class CarPropManager implements CarPropertyManager.CarPropChangeCallback {
    private static final String TAG = "CarPropManager";

    private MegaCarPropHelper mCarPropHelper;
    private EventHandler mHandler;

    private static CarPropManager sInstance;
    private SceneModeManager mModeManager;

    public static @NonNull
    CarPropManager getInstance() {
        if (sInstance == null) {
            synchronized (CarPropManager.class) {
                if (sInstance == null) {
                    sInstance = new CarPropManager();
                }
            }
        }
        return sInstance;
    }

    public CarPropManager() {
        MLog.d(TAG, "initManager()");
        HandlerThread thread = new HandlerThread(this.getClass().getSimpleName());
        thread.start();
        mHandler = new EventHandler(thread.getLooper());
        mModeManager = SceneModeManager.getInstance();

        mCarPropHelper = MegaCarPropHelper
                .getInstance(SceneModeApplication.getInstance(), mHandler);
        mCarPropHelper.registerCallback(this, mIds);
    }

    public void exit() {
        MLog.d(TAG, "exit()");
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    private final Set<Integer> mIds = new HashSet<>(
            Arrays.asList(
                    //电源
                    ID_DRIVER_INFO_STS_VEH_POWER,
                    //电量
                    ID_HV_PERCENT,
                    //系统档位
                    ID_DRV_INFO_GEAR_POSITION,
                    //制动踏板
                    ID_BRAKE_PEDAL_STATUS,

                    ID_DRIVER_INFO_STS_DOOR_AJAR_WARN,
                    //恒温座舱
                    ID_THERMOSTATIC_COCKPIT,
                    //抽烟模式
                    ID_CAMPING_MODE
                    ));

    @Override
    public void onChanged(int id, @NonNull Object status, boolean isSetFailFallback) {
        boolean isNeedExit;
        switch (id) {
            case ID_DRIVER_INFO_STS_VEH_POWER:
                boolean isPower = ((int) status == Driving.ParamsVehPowerStatusSts.IGN_ON
                        || (int) status == Driving.ParamsVehPowerStatusSts.ACC_ON);
                mModeManager.onCampModeChanged(!isPower);
                break;
            case ID_HV_PERCENT:
                if ((int) status < VEHICLE_STATE_SOC_POWER_20) {
                    mModeManager.onLowEnergy((int) status);
                }
                break;
            case ID_DRV_INFO_GEAR_POSITION:
                boolean park = (int) status == Driving.ParamsDrvInfoGearPosition.PARKING;
                mModeManager.onCampModeChanged(!park);
                break;
            case ID_BRAKE_PEDAL_STATUS:
                boolean pedal = (int) status == Driving.BrakePedalSts.NOT_PRESSED;
                mModeManager.onCampModeChanged(!pedal);
                break;
            case ID_DOOR:
                isNeedExit = (int) status != ParamsCommon.OnOff.OFF;
                break;
            case ID_CAMPING_MODE:
                //获取开关状态，刷新页面
                mModeManager.onCampModeChanged((int) status != ParamsCommon.OnOff.OFF);
                break;
            case ID_THERMOSTATIC_COCKPIT:
                mModeManager.onTemperatureModeChanged((int) status);
                break;
            default:
                break;
        }
    }

    private class EventHandler extends Handler {
        public EventHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                default:
                    break;
            }
        }
    }

    public void publish(int topic, @NonNull Object content) {
        if (mCarPropHelper == null) {
            return;
        }
        mCarPropHelper.setRawProp(new CarPropertyValue<>(topic, content));
    }

    public boolean checkPower() {
        int ignState = mCarPropHelper.getIntProp(ID_DRIVER_INFO_STS_VEH_POWER);
        MLog.d(TAG, "checkPower (1 2 pass): " + ignState);
        boolean ignPass;
        ignPass = ignState == Driving.ParamsVehPowerStatusSts.IGN_ON
                || ignState == Driving.ParamsVehPowerStatusSts.ACC_ON;
        return ignPass;
    }

    //车辆检查 --- 小憩模式
    public boolean checkNap() {
        MLog.d(TAG, "checkNap");
        //电源 ON
        boolean ignPass = checkPower();

        //ID_HV_PERCENT --  SOC
        int soc = mCarPropHelper.getIntProp(ID_HV_PERCENT);
        boolean socPass = (soc > VEHICLE_STATE_SOC_POWER_20);
        MLog.d(TAG, "checkNap socPass(20 pass): " + socPass);

        //ID_EP_HV_BATT_CHG_STATUS --外接充电
        int chargeStatus = mCarPropHelper.getIntProp(ID_EP_HV_BATT_CHG_STATUS);
        boolean chargeStatusPass = (chargeStatus == ElecPower.ParamsBattChargeStatus.NO_CHARGING);
        MLog.d(TAG, "checkNap chargeStatusPass(0 pass): " + chargeStatusPass);

        //系统档位 P- - ID_DRV_INFO_GEAR_POSITION
        int vehState = mCarPropHelper.getIntProp(ID_DRV_INFO_GEAR_POSITION);
        MLog.d(TAG, "checkNap vehState(0 pass): " + vehState);
        boolean parkPass;
        parkPass = vehState == Driving.ParamsDrvInfoGearPosition.PARKING;

        int door = mCarPropHelper.getIntProp(ID_DOOR);
        MLog.d(TAG, "checkNap door(0 pass): " + door);
        boolean doorPass = door == ParamsCommon.OnOff.OFF;

        //制动踏板 --- ID_BRAKE_PEDAL_STATUS
        int pedal = mCarPropHelper.getIntProp(ID_BRAKE_PEDAL_STATUS);
        MLog.d(TAG, "checkNap pedal(0 pass): " + door);
        boolean pedalPass = pedal == Driving.BrakePedalSts.NOT_PRESSED;

        MLog.d(TAG, "checkNap : " + parkPass + doorPass + ignPass);
//        return parkPass && socPass && chargeStatusPass && doorPass && ignPass && pedalPass;
        return true;
    }

    //车辆检查 --- 露营模式
    public boolean checkCamping() {
        MLog.d(TAG, "checkCamping");
        //电源 ON
        boolean ignPass = checkPower();

        //ID_HV_PERCENT --  SOC
        int soc = mCarPropHelper.getIntProp(ID_HV_PERCENT);
        boolean socPass = (soc > VEHICLE_STATE_SOC_POWER_25);
        MLog.d(TAG, "checkCamping socPass(25 pass): " + socPass);
        //系统档位 P- - ID_DRV_INFO_GEAR_POSITION
        int vehState = mCarPropHelper.getIntProp(ID_DRV_INFO_GEAR_POSITION);
        MLog.d(TAG, "checkCamping vehState(3 pass): " + vehState);
        boolean parkPass = vehState == Driving.ParamsDrvInfoGearPosition.PARKING;

        //制动踏板为未踩下 -- ID_BRAKE_PEDAL_STATUS
        int pedalState = mCarPropHelper.getIntProp(ID_BRAKE_PEDAL_STATUS);
        MLog.d(TAG, "checkCamping pedalState(3 pass): " + vehState);
        boolean pedalPass = pedalState == Driving.BrakePedalSts.NOT_PRESSED;
        return ignPass && socPass && parkPass && pedalPass;
//        return true;
    }

    //车辆检查 --- 恒温座舱
    public boolean checkTemperature() {
        MLog.d(TAG, "checkTemperature");
        //电源 ON
        boolean ignPass = checkPower();

        //ID_HV_PERCENT --  SOC
        int soc = mCarPropHelper.getIntProp(ID_HV_PERCENT);
        boolean socPass = (soc > VEHICLE_STATE_SOC_POWER_20);

//        return ignPass && socPass;
        return true;
    }

    public boolean getCampStatus() {
        int campStatus = mCarPropHelper.getIntProp(ID_CAMPING_MODE);
        return campStatus == ParamsCommon.OnOff.ON;
    }

    public boolean getSmokeStatus() {
        int smokeStatus = mCarPropHelper.getIntProp(ID_SMOKE_MODE_SWITCH);
        return smokeStatus == ParamsCommon.OnOff.ON;
    }

    public int getTemperatureStatus() {
        int smokeStatus = mCarPropHelper.getIntProp(ID_THERMOSTATIC_COCKPIT);
        return smokeStatus;
    }
}