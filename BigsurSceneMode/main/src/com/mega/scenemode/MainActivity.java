package com.mega.scenemode;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mega.nexus.os.MegaPowerManager;
import com.mega.nexus.os.MegaUserHandle;
import com.mega.scenemode.Constants.MenuType;
import com.mega.scenemode.fragment.IFragmentPresenter;
import com.mega.scenemode.fragment.NapConfirmDialog;
import com.mega.scenemode.fragment.NapFragment;
import com.mega.scenemode.fragment.NapSettingFragment;
import com.mega.scenemode.fragment.TemperatureSettingFragment;
import com.mega.scenemode.manager.CarPropManager;
import com.mega.scenemode.manager.SceneModeManager;
import com.mega.scenemode.menu.LooperLayoutManagerVertical;
import com.mega.scenemode.menu.MenuAdapter;
import com.mega.scenemode.menu.MenuItem;
import com.mega.scenemode.menu.MenuItemClickListener;
import com.mega.scenemode.view.ConfirmDialog;

import java.util.ArrayList;
import java.util.List;

import mega.car.config.ParamsCommon;
import mega.log.MLog;

import static androidx.viewpager.widget.ViewPager.SCROLL_STATE_IDLE;
import static com.mega.nexus.os.MegaPowerManager.BACK_LIGHT_STATE_ON;
import static com.mega.nexus.provider.MegaSettings.MAIN_CONTROL_BRIGHTNESS;
import static com.mega.scenemode.Constants.CENTER_RECYCLER_VIEW_Y;
import static com.mega.scenemode.Constants.KEY_DISTURB_CLOSE;
import static com.mega.scenemode.Constants.KEY_DISTURB_MODE;
import static com.mega.scenemode.Constants.KEY_DISTURB_OPEN;
import static com.mega.scenemode.Constants.MENU_ITEM_HEIGHT;
import static com.mega.scenemode.Constants.MODE_ITEM_TOTAL;
import static com.mega.scenemode.Constants.SCROLL_DURATION;
import static com.mega.scenemode.Constants.VEHICLE_STATE_SOC_POWER_20;
import static com.mega.scenemode.Constants.VEHICLE_STATE_THERMOSTATIC_LOW_POWER;
import static com.mega.scenemode.Constants.VEHICLE_STATE_THERMOSTATIC_NORMAL;
import static com.mega.scenemode.Constants.VEHICLE_STATE_THERMOSTATIC_OFF;

public class MainActivity extends AppCompatActivity implements MenuItemClickListener,
        View.OnClickListener, IFragmentPresenter, SceneModeManager.OnModeChangedListener {
    private RecyclerView mRecyclerView;
    private MenuAdapter mAdapter;
    private LooperLayoutManagerVertical mManager;
    private View mLastSelectedView = null;
    private Handler mHandler;

    private List<MenuItem> mMenuItemList;
    private boolean mIsTouch;
    private MenuType mCurrentType;
    private int mTemperatureMode;

    private ImageView mMainBg;
    private TextView mTitle;
    private TextView mModeIntroduce;
    private Button mButtonLeft;
    private Button mButtonRight;
    private ImageView mSettingsImg;
    private ImageView mDisableView;

    private NapConfirmDialog mNapCountDownConfirmDialog;
    private int mNapCountDownTime;

    private CarPropManager mCarPropManager;
    private SceneModeManager mSceneModeManager;

    private final MenuType[] mModeTitles = new MenuType[]{
            MenuType.DISTURB,
            MenuType.TEMPERATURE,
            MenuType.NAP,
            MenuType.SMOKING,
            MenuType.CAMPING};
    private final int[] mModeSrcIds = new int[]{
            R.drawable.disturb_selector,
            R.drawable.temperature_selector,
            R.drawable.nap_selector,
            R.drawable.smoking_selector,
            R.drawable.camping_selector};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        initView();
        mHandler = new Handler();
        mCarPropManager = CarPropManager.getInstance();
        mSceneModeManager = SceneModeManager.getInstance();
        mSceneModeManager.setModeChangedListener(this);
    }

    //初始化图标列表
    private void initData() {
        if (mMenuItemList == null) mMenuItemList = new ArrayList<>();
        for (int i = 0; i < MODE_ITEM_TOTAL; i++) {
            MenuItem item = new MenuItem();
            item.srcId = mModeSrcIds[i];
            item.type = mModeTitles[i];
            mMenuItemList.add(item);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public void initView() {
        //全屏显示，沉浸式状态栏
        findViewById(R.id.main_layout)
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        mMainBg = findViewById(R.id.main_bg);
        mTitle = findViewById(R.id.mode_title);
        mModeIntroduce = findViewById(R.id.mode_introduce_text);
        mButtonLeft = findViewById(R.id.mode_button_left);
        mButtonRight = findViewById(R.id.mode_button_right);
        mButtonLeft.setOnClickListener(this);
        mButtonRight.setOnClickListener(this);
        mDisableView = findViewById(R.id.mode_disable);
        mDisableView.setVisibility(View.GONE);
        mDisableView.setOnClickListener(this);

        mRecyclerView = findViewById(R.id.menu_list);
        mManager = new LooperLayoutManagerVertical(getApplicationContext(),
                LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mManager);
        mAdapter = new MenuAdapter(R.layout.item_arc, this, mMenuItemList, mRecyclerView);
        mAdapter.setMenuItemClickListener(this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(mOnScrollListener);
        mRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mIsTouch = true;
                if (mLastSelectedView != null) {
                    mLastSelectedView.setSelected(false);
                }
                return false;
            }
        });
        //RecyclerView 加载完成后，选中中间一条
        mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        updateSelectedPage(null);
                        updateChildAlpha(mRecyclerView);
                    }
                });

        mSettingsImg = findViewById(R.id.mode_setting);
        mSettingsImg.setOnClickListener(this);
    }

    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

            MLog.d("onScrollStateChanged === " + newState);
            if (!mIsTouch && newState == SCROLL_STATE_IDLE) {
                updateChildAlpha(recyclerView);
            }

            if (!mIsTouch) {
                return;
            }
            if (newState == SCROLL_STATE_IDLE) {
                updateSelectedPage(null);
                mIsTouch = false;
            }
        }

        //当RecyclerView滑动时重新调整矩阵偏移量
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            updateChildAlpha(recyclerView);
            if (mIsTouch) {
                View view = recyclerView.findChildViewUnder(0, CENTER_RECYCLER_VIEW_Y);
                updatePage((MenuType) view.getTag());
            }
        }
    };

    private void updateChildAlpha(@NonNull RecyclerView recyclerView) {
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            View child = recyclerView.getChildAt(i);
            child.setAlpha(getAlphaValue(child.getTop()));
            child.invalidate();
        }
    }

    private float getAlphaValue(int top) {
        float distance = Math.abs(CENTER_RECYCLER_VIEW_Y - top - (MENU_ITEM_HEIGHT / 2));
        float alpha = distance / CENTER_RECYCLER_VIEW_Y;
        return (1 - alpha);
    }

    //初始和滑动停止后，自动选中
    private void updateSelectedPage(View view) {
        MLog.d("updateSelectedPage");
        if (mLastSelectedView != null) {
            mLastSelectedView.setSelected(false);
        }

        // 如果不设置 View，则取当前居中的 View
        if (view == null) {
            mLastSelectedView = mRecyclerView.findChildViewUnder(0, CENTER_RECYCLER_VIEW_Y);
        } else {
            mLastSelectedView = view;
        }
        //当前居中控件的一半高度
        int childHalfHeight = MENU_ITEM_HEIGHT / 2;
        //当前居中控件距离顶部距离
        int childViewTop = mLastSelectedView.getTop();
        // 需要滑动的距离
        int smoothDistance = childViewTop - CENTER_RECYCLER_VIEW_Y + childHalfHeight;
        mRecyclerView.smoothScrollBy(0, smoothDistance, null, SCROLL_DURATION);
        mLastSelectedView.setSelected(true);
        updatePage((MenuType) mLastSelectedView.getTag());
        ensureModeInformation((MenuType) mLastSelectedView.getTag());
    }

    private void updatePage(MenuType type) {
        int titleId;
        int introduceId;
        switch (type) {
            case CAMPING:
                titleId = R.string.mode_camping;
                introduceId = R.string.mode_camping_introduce;
                break;
            case NAP:
                titleId = R.string.mode_nap;
                introduceId = R.string.mode_nap_introduce;
                break;
            case TEMPERATURE:
                titleId = R.string.mode_temperature;
                introduceId = R.string.mode_temperature_introduce;
                break;
            case DISTURB:
                titleId = R.string.mode_disturb;
                introduceId = R.string.mode_disturb_introduce;
                break;
            case SMOKING:
                titleId = R.string.mode_smoking;
                introduceId = R.string.mode_smoking_introduce;
                break;
            default:
                titleId = R.string.mode_nap;
                introduceId = R.string.mode_nap_introduce;
                break;
        }

        mTitle.setText(titleId);
        mModeIntroduce.setText(introduceId);
        mButtonLeft.setText(R.string.mode_open);
        mButtonRight.setVisibility(View.GONE);
        mCurrentType = type;
    }

    private void ensureModeInformation(MenuType type) {
        int bgResource;
        int disableVisible = View.GONE;
        mButtonRight.setVisibility(View.GONE);
        if (type == MenuType.TEMPERATURE) {
            mButtonLeft.setText(R.string.mode_normal);
            mSettingsImg.setVisibility(View.VISIBLE);
        } else {
            mButtonLeft.setText(R.string.mode_open);
            mButtonLeft.setBackgroundResource(R.drawable.mode_button_bg_selector);
            if (type == MenuType.NAP) {
                mSettingsImg.setVisibility(View.VISIBLE);
            } else {
                mSettingsImg.setVisibility(View.GONE);
            }
        }

        switch (type) {
            case CAMPING:
                bgResource = R.mipmap.bg_mode_camping;
                if (mCarPropManager.getCampStatus()) {
                    //已开启
                    disableVisible = View.VISIBLE;
                    mDisableView.setImageResource(R.mipmap.ic_mode_already_open);
                    mButtonLeft.setText(getString(R.string.mode_close));
                } else if (!mCarPropManager.checkCamping()) {
                    //不可用
                    mDisableView.setImageResource(R.mipmap.ic_mode_disable);
                    disableVisible = View.VISIBLE;
                    mButtonLeft.setVisibility(View.INVISIBLE);
                } else {
                    mButtonLeft.setVisibility(View.VISIBLE);
                }
                break;
            case TEMPERATURE:
                bgResource = R.mipmap.bg_mode_temperature;
                if (mCarPropManager.getTemperatureStatus() != ParamsCommon.OnOff.OFF) {
                    //已开启
                    disableVisible = View.VISIBLE;
                    mDisableView.setImageResource(R.mipmap.ic_mode_already_open);
                    mButtonLeft.setText(getString(R.string.mode_close));
                    mButtonRight.setVisibility(View.VISIBLE);
                } else if (!mCarPropManager.checkTemperature()) {
                    //不可用
                    mDisableView.setImageResource(R.mipmap.ic_mode_disable);
                    disableVisible = View.VISIBLE;
                    mButtonLeft.setVisibility(View.INVISIBLE);
                    mButtonRight.setVisibility(View.GONE);
                } else {
                    mButtonLeft.setVisibility(View.VISIBLE);
                    mButtonRight.setVisibility(View.VISIBLE);
                }
                onTemperatureModeChanged(mCarPropManager.getTemperatureStatus());
                break;
            case DISTURB:
                bgResource = R.mipmap.bg_mode_disturb;
                int disturbStatus = Settings.Global
                        .getInt(getContentResolver(), KEY_DISTURB_MODE, KEY_DISTURB_CLOSE);
                if (disturbStatus == 1) {
                    disableVisible = View.VISIBLE;
                    mDisableView.setImageResource(R.mipmap.ic_mode_already_open);
                    mButtonLeft.setText(getString(R.string.mode_close));
                } else {
                    mButtonLeft.setVisibility(View.VISIBLE);
                }
                break;
            case SMOKING:
                bgResource = R.mipmap.bg_mode_smoking;
                if (mCarPropManager.getSmokeStatus()) {
                    //已开启
                    disableVisible = View.VISIBLE;
                    mDisableView.setImageResource(R.mipmap.ic_mode_already_open);
                    mButtonLeft.setText(getString(R.string.mode_close));
                } else if (!mCarPropManager.checkPower()) {
                    //不可用
                    mDisableView.setImageResource(R.mipmap.ic_mode_disable);
                    disableVisible = View.VISIBLE;
                    mButtonLeft.setVisibility(View.INVISIBLE);
                } else {
                    mButtonLeft.setVisibility(View.VISIBLE);
                }
                break;
            case NAP:
            default:
                bgResource = R.mipmap.bg_mode_nap;
                if (!mCarPropManager.checkNap()) {
                    mDisableView.setImageResource(R.mipmap.ic_mode_disable);
                    disableVisible = View.VISIBLE;
                    mButtonLeft.setVisibility(View.INVISIBLE);
                } else {
                    mButtonLeft.setVisibility(View.VISIBLE);
                }
                break;
        }
        mMainBg.setBackgroundResource(bgResource);
        mDisableView.setVisibility(disableVisible);
    }

    //点击一个 Item 后，自动跳转到中心，并且选中
    @Override
    public void onMenuItemClick(View view) {
        mIsTouch = false;
        updateSelectedPage(view);
    }

    @Override
    public void onClick(@NonNull View view) {
        MLog.d("click ------------------------ ");
        switch (view.getId()) {
            case R.id.mode_setting:
                if (mCurrentType == MenuType.NAP) {
                    NapSettingFragment fragment = new NapSettingFragment();
                    fragment.setPresenter(this);
                    showFragment(fragment, "nap_setting");
                } else if (mCurrentType == MenuType.TEMPERATURE) {
                    TemperatureSettingFragment fragment = new TemperatureSettingFragment();
                    fragment.setPresenter(this);
                    showFragment(fragment, "temperature_setting");
                }
                break;

            case R.id.mode_button_left:
                if (mCurrentType == MenuType.NAP) {
                    showNapCountDownDialog();
                } else if (mCurrentType == MenuType.CAMPING) {
                    SceneModeManager.getInstance().switchCampMode(!mCarPropManager.getCampStatus());
                } else if (mCurrentType == MenuType.SMOKING) {
                    SceneModeManager.getInstance()
                            .switchSmokeMode(!mCarPropManager.getSmokeStatus());
                } else if (mCurrentType == MenuType.DISTURB) {
                    //没有信号交互，主动刷新页面
                    int disturbStatus = Settings.Global
                            .getInt(getContentResolver(), KEY_DISTURB_MODE, KEY_DISTURB_CLOSE);
                    Settings.Global.putInt(getContentResolver(), KEY_DISTURB_MODE,
                            disturbStatus == 0 ? KEY_DISTURB_OPEN : KEY_DISTURB_CLOSE);
                    ensureModeInformation(mCurrentType);
                } else if (mCurrentType == MenuType.TEMPERATURE) {
                    SceneModeManager.getInstance().switchTemperatureMode(
                            mTemperatureMode == VEHICLE_STATE_THERMOSTATIC_NORMAL
                                    ? VEHICLE_STATE_THERMOSTATIC_OFF
                                    : VEHICLE_STATE_THERMOSTATIC_NORMAL);
                }
                break;

            case R.id.mode_button_right:
                if (mCurrentType == MenuType.TEMPERATURE) {
                    SceneModeManager.getInstance().switchTemperatureMode(
                            mTemperatureMode == VEHICLE_STATE_THERMOSTATIC_LOW_POWER
                                    ? VEHICLE_STATE_THERMOSTATIC_OFF
                                    : VEHICLE_STATE_THERMOSTATIC_LOW_POWER);
                }
                break;

            case R.id.mode_disable:
                if (mCurrentType == MenuType.NAP) {
                    showDisableDialog(getString(R.string.nap_disable));
                } else if (mCurrentType == MenuType.CAMPING) {
                    showDisableDialog(getString(R.string.camp_disable));
                } else if (mCurrentType == MenuType.TEMPERATURE) {
                    showDisableDialog(getString(R.string.temperature_disable));
                }
                break;
            default:
                break;
        }
    }

    private void showFragment(Fragment fragment, String tag) {
        FragmentTransaction transaction
                = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.main_layout, fragment);
        transaction.addToBackStack(tag);
        transaction.commit();
    }

    @Override
    public void backToActivity() {
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void startNapMode() {
        MLog.d("startNapMode");
        NapFragment fragment = new NapFragment();
        fragment.setPresenter(this);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_layout, fragment);
        transaction.addToBackStack("nap");
        transaction.commit();
    }

    private final Runnable mCountDownRunnable = new Runnable() {
        @Override
        public void run() {
            mNapCountDownConfirmDialog.setHint(
                    String.format(getString(R.string.nap_count_down_tip), mNapCountDownTime--));
            if (mNapCountDownTime == -1) {
                mNapCountDownConfirmDialog.dismiss();
                //开启小憩模式
                startNapMode();
            } else {
                mHandler.postDelayed(mCountDownRunnable, 1000);
            }
        }
    };

    @Override
    public void onModeChanged(@NonNull MenuType type, boolean isOn) {
        if (type == mCurrentType) {
            ensureModeInformation(mCurrentType);
        }

        if (type == MenuType.CAMPING && !isOn) {
            Toast.makeText(this, R.string.camp_exit, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onTemperatureModeChanged(int status) {
        if (mCurrentType == MenuType.TEMPERATURE) {
            switch (status) {
                case VEHICLE_STATE_THERMOSTATIC_OFF:
                    mButtonLeft.setBackgroundResource(R.mipmap.mode_button_bg_gray);
                    mButtonRight.setBackgroundResource(R.mipmap.mode_button_bg_gray);
                    break;

                case VEHICLE_STATE_THERMOSTATIC_NORMAL:
                    mButtonLeft.setBackgroundResource(R.mipmap.mode_button_bg_light);
                    mButtonRight.setBackgroundResource(R.mipmap.mode_button_bg_gray);
                    break;

                case VEHICLE_STATE_THERMOSTATIC_LOW_POWER:
                    mButtonLeft.setBackgroundResource(R.mipmap.mode_button_bg_gray);
                    mButtonRight.setBackgroundResource(R.mipmap.mode_button_bg_light);
                    break;

                default:
                    break;
            }
        }
    }

    @Override
    public void lowEnergy(int soc) {
        //露营模式低电量提醒
        if (mCurrentType == MenuType.CAMPING && soc < VEHICLE_STATE_SOC_POWER_20) {
            //弹出警示弹窗
            showWarningDialog(getString(R.string.camp_warning),
                    new ConfirmDialog.OnClickListener() {
                @Override
                public void onPositiveClick() {
                    SceneModeManager.getInstance().stopCampBell();
                }

                @Override
                public void onNegativeClick() {
                }
            });
            //主屏幕亮屏
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            MegaPowerManager.updateBackLightState(powerManager, MAIN_CONTROL_BRIGHTNESS,
                    BACK_LIGHT_STATE_ON, MegaUserHandle.USER_CURRENT_OR_SYSTEM);
            //铃声提醒
            SceneModeManager.getInstance().playCampBell();
        }
    }

    private void showNapCountDownDialog() {
        mNapCountDownTime = 5;
        mNapCountDownConfirmDialog = new NapConfirmDialog();
        mNapCountDownConfirmDialog.setHint(
                String.format(getString(R.string.nap_count_down_tip), mNapCountDownTime--));
        mNapCountDownConfirmDialog.setPositiveVisible(View.GONE);
        mNapCountDownConfirmDialog.setOnClickListener(new ConfirmDialog.OnClickListener() {
            @Override
            public void onPositiveClick() {
            }

            @Override
            public void onNegativeClick() {
                mHandler.removeCallbacksAndMessages(null);
            }
        });
        mNapCountDownConfirmDialog.show(getSupportFragmentManager(), "nap_confirm");

        mHandler.postDelayed(mCountDownRunnable, 1000);
    }

    private void showDisableDialog(String hint) {
        ConfirmDialog disableDialog = new ConfirmDialog();
        disableDialog.setNegativeVisible(View.GONE);
        disableDialog.setTitle(getString(R.string.disable_title));
        disableDialog.setHint(hint);
        disableDialog.setPositiveText(getString(R.string.confirm));
        disableDialog.show(getSupportFragmentManager(), "disable");
    }

    private void showWarningDialog(String hint, ConfirmDialog.OnClickListener listener) {
        ConfirmDialog disableDialog = new ConfirmDialog();
        disableDialog.setNegativeVisible(View.GONE);
        disableDialog.setTitleVisible(View.GONE);
        disableDialog.setHint(hint);
        disableDialog.setPositiveText(getString(R.string.confirm));
        disableDialog.setOnClickListener(listener);
        disableDialog.show(getSupportFragmentManager(), "warning");
    }
}