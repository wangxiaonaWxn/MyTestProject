package com.mega.carmaintenance.utils;

import android.content.res.Resources;
import android.util.Log;

import com.mega.carmaintenance.R;
import com.mega.carmaintenance.model.DateModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DateUtil {
    private static final String DATE_PATTER = "yyyy-MM-dd";

    public static DateModel getDateModel(Calendar calendar, Resources res) {
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        DateModel model = new DateModel();
        model.dateInfo = month + "." + day;
        model.weekInfo = getWeekName(calendar.get(Calendar.DAY_OF_WEEK), res);
        model.dateString =  getDateWithYMD(calendar, DATE_PATTER);
        return model;
    }

    private static String getWeekName(int week, Resources res) {
        switch (week) {
            case 1:
                return res.getString(R.string.sunday);
            case 2:
                return res.getString(R.string.monday);
            case 3:
                return res.getString(R.string.tuesday);
            case 4:
                return res.getString(R.string.wednesday);
            case 5:
                return res.getString(R.string.thursday);
            case 6:
                return res.getString(R.string.friday);
            case 7:
                return res.getString(R.string.saturday);
            default:
                return null;
        }
    }

    public static String getDateWithYMD(Calendar calendar, String pattern) {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern, Locale.CHINA);
        return formatter.format(calendar.getTime());
    }
}
