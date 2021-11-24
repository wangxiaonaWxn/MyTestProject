package com.mega.scenemode.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class MatrixTranslateLayout extends LinearLayout {
    private int mParentHeight = 0;
    private int mTopOffset = 0;
    private static final int CENTER_ITEM_TOP = 304;
    private static final int CENTER_TRAN_OFFSET = 22;

    public MatrixTranslateLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setParentHeight(int height) {
        mParentHeight = height;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.save();
        if (mTopOffset == 0) {
            mTopOffset = getHeight() / 2;
        }
        int top = getTop() + mTopOffset;

       float tran = calculateTranslate(top, mParentHeight);
       // if the item is the center one , modify the left to make it in the center
       if (getY() == CENTER_ITEM_TOP) {
           tran = tran - CENTER_TRAN_OFFSET;
       }
        Matrix m = canvas.getMatrix();
        m.setTranslate(tran, 0);
        canvas.concat(m);
        super.dispatchDraw(canvas);
        canvas.restore();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private float calculateTranslate(int top, int h) {
        float result;
        int hh = h / 2;
        result = Math.abs(top - hh) * -1;
        return (result / 2);
    }
}
