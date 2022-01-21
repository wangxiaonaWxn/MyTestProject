package com.mega.scenemode.menu;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.mega.scenemode.Constants.RECYCLER_VIEW_HEIGHT;

/*
 * 矩阵偏移的形式实现轮盘，RecyclerView的Item布局的根布局使用这个Layout
 */
public class MatrixTranslateLayout extends LinearLayout {
    private int mTopOffset = 0;

    public MatrixTranslateLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        canvas.save();
        if (mTopOffset == 0) {
            mTopOffset = getHeight() / 2;
        }

        int top = getTop() + mTopOffset;

//        MLog.d("getTop : " + getTop() + ", offset : " + mTopOffset + ", top :" + top);
        float translate = calculateTranslate(top, RECYCLER_VIEW_HEIGHT);
        Matrix matrix = new Matrix();
        matrix.setTranslate(translate, 0);
        canvas.concat(matrix);
        super.dispatchDraw(canvas);
        canvas.restore();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    //计算偏移量，通过根式，使结果更偏向弧形
    private float calculateTranslate(int top, int height) {
//        MLog.d("calculateTranslate : " + top + ", height : " + height);
        int mediumHeight = height / 2;
        if (top - mediumHeight == 0) {
            return 0;
        }

        float abs = Math.abs(top - mediumHeight);
        float result = abs * -1;
        if (mediumHeight == abs) {
            return result;
        }
        float temp = (float) Math.sqrt((mediumHeight - abs) / 10);
//        MLog.d("calculateTranslate result: " + result / temp);
        return (float) (result / temp);
    }
}
