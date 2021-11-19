package com.mega.carmaintenance.adapter;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import com.mega.carmaintenance.R;
import com.mega.carmaintenance.callback.DateItemClickCallback;
import com.mega.carmaintenance.model.DateModel;

import java.util.List;

public class DateListAdapter extends CommonAdapter<DateModel> {
    private View mLastSelectedView;
    private DateItemClickCallback mCallback;
    private Context mContext;

    public DateListAdapter(int layoutId, Context context, List<DateModel> data) {
        super(layoutId, context, data);
        mContext = context;
    }

    public DateListAdapter(int layoutId, Context context, List<DateModel> data,
                           DateItemClickCallback callback) {
        this(layoutId, context, data);
        mCallback = callback;
    }

    @Override
    void bingData(final DateModel data, CommonViewHolder holder) {
        final TextView item = holder.getViewById(R.id.date_item);
        item.setText(getSpanString(data.weekInfo, data.dateInfo));
        item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item.setSelected(true);
                if (mLastSelectedView != null) {
                    mLastSelectedView.setSelected(false);
                }
                mLastSelectedView = item;
                if (mCallback != null) {
                    mCallback.onDateClick(data.dateString);
                }
            }
        });
    }

    private SpannableStringBuilder getSpanString(String week, String date) {
        SpannableStringBuilder spannable = new SpannableStringBuilder();
        spannable.append(week);
        spannable.append("\n");
        spannable.append(date);
        AbsoluteSizeSpan sizeSpan = new AbsoluteSizeSpan(24);
        CharacterStyle colorSpan = new ForegroundColorSpan(mContext.getResources()
                .getColor(R.color.week_text_color));
        spannable.setSpan(colorSpan, 0, week.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(sizeSpan, 0, week.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }
}
