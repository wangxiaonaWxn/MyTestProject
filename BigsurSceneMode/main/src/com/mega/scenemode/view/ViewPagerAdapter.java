package com.mega.scenemode.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.mega.scenemode.R;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends PagerAdapter {
    private static final String TAG = "ViewPagerAdapter";

    private Context mContext;
    private List<Integer> mImage;

    public ViewPagerAdapter(@NonNull Context context, @NonNull ArrayList<Integer> image) {
        this.mContext = context;
        this.mImage = image;
    }

    /*
     * 根据 mData 的数量，创建 ViewPager 中的页面数量
     */
    @Override
    public int getCount() {
        return mImage.size();
    }

    /*
     * 判断是否重复，如果已经加载了那就不去重复加载
     */
    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public @NonNull Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = View.inflate(mContext, R.layout.view_pager_item, null);
        ImageView imageView = view.findViewById(R.id.view_pager_icon);
        imageView.setBackgroundResource(mImage.get(position));
        container.addView(view); // 将实例化的 pager.xml 加入到容器中去, container 就是容器.
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}