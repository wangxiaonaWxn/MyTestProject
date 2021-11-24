package com.mega.scenemode;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.mega.scenemode.menu.LooperLayoutManagerVertical;
import com.mega.scenemode.menu.MenuAdapter;
import com.mega.scenemode.menu.MenuItem;
import com.mega.scenemode.menu.MenuItemClickListener;
import com.mega.scenemode.menu.MenuType;

import java.util.ArrayList;
import java.util.List;

import static androidx.viewpager.widget.ViewPager.SCROLL_STATE_IDLE;

public class MainActivity extends AppCompatActivity implements MenuItemClickListener {
    private RecyclerView mRecyclerView;
    private MenuAdapter mAdapter;
    private LooperLayoutManagerVertical mManager;
    private DecelerateInterpolator mInterpolator = new DecelerateInterpolator();
    private View mLastSelectedView = null;
    private int []mRecyclerViewLocation = new int[2];
    private int []mFirstItemLocation = new int[2];
    private int [] mMiddleItemLocation = new int[2];
    private static final int CENTER_ITEM_Y = 380;
    private List<MenuItem> mMenuItemList;
    private MenuType[] mTitles = new MenuType[]{
            MenuType.SMOKING,
            MenuType.DISTURB,
            MenuType.NAP,
            MenuType.TEMPERATURE,
            MenuType.CAMPING};
    private int[] mSrcIds = new int[]{
            R.drawable.smoking_selector,
            R.drawable.disturb_selector,
            R.drawable.nap_selector,
            R.drawable.temperature_selector,
            R.drawable.camping_selector};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = findViewById(R.id.menu_list);
        initData();
        mManager = new LooperLayoutManagerVertical(getApplicationContext(),
                LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mManager);
        mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                findView();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                mRecyclerView.getLocationInWindow(mRecyclerViewLocation);;
            }
        });
    }

    private void initData() {
        if (mMenuItemList == null) mMenuItemList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            MenuItem item = new MenuItem();
            item.srcId = mSrcIds[i];
            item.type = mTitles[i];
            mMenuItemList.add(item);
        }
    }

    private void findView() {
        mAdapter = new MenuAdapter(R.layout.item_arc, this, mMenuItemList, mRecyclerView);
        mAdapter.setMenuItemClickListener(this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateSelectedPage();
            }
        }, 100L);

        mRecyclerView.addOnScrollListener(mOnScrollListener);
    }

    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (mLastSelectedView != null) {
                mLastSelectedView.setSelected(false);
            }
            if (newState == SCROLL_STATE_IDLE) {
                updateSelectedPage();
            }
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            for (int i = 0; i < recyclerView.getChildCount(); i++) {
                recyclerView.getChildAt(i).invalidate();
            }
        }
    };

    private void updateSelectedPage() {
        if (mLastSelectedView != null) {
            mLastSelectedView.setSelected(false);
        }
        int first = mManager.findFirstCompletelyVisibleItemPosition();
        View view = mManager.findViewByPosition(first);
        view.getLocationInWindow(mFirstItemLocation);;
        int distance = mFirstItemLocation[1] - mRecyclerViewLocation[1];
        mRecyclerView.scrollBy(0, distance);
        mLastSelectedView = mRecyclerView.findChildViewUnder(0, CENTER_ITEM_Y);
        mLastSelectedView.setSelected(true);
        updatePage((MenuType) mLastSelectedView.getTag());
    }

    private void updatePage(MenuType type) {
        switch (type) {
            case CAMPING:
                Toast.makeText(getApplicationContext(), "CAMPING", Toast.LENGTH_LONG).show();
                break;
            case NAP:
                Toast.makeText(getApplicationContext(), "NAP", Toast.LENGTH_LONG).show();
                break;
            case TEMPERATURE:
                Toast.makeText(getApplicationContext(), "TEMPERATURE", Toast.LENGTH_LONG).show();
                break;
            case DISTURB:
                Toast.makeText(getApplicationContext(), "DISTURB", Toast.LENGTH_LONG).show();
                break;
            case SMOKING:
                Toast.makeText(getApplicationContext(), "SMOKING", Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }
    }

    @Override
    public void onMenuItemClick(View view) {
        mLastSelectedView.setSelected(false);
        mLastSelectedView.getLocationInWindow(mMiddleItemLocation);
        mLastSelectedView = view;
        mLastSelectedView.setSelected(true);
        mLastSelectedView.getLocationInWindow(mFirstItemLocation);
        int distance = mFirstItemLocation[1] - mMiddleItemLocation[1];
        mRecyclerView.smoothScrollBy(0, distance, mInterpolator);
    }
}
