package com.mega.carmaintenance.model;

import java.util.ArrayList;
import java.util.List;

public class ShopListDateModel {
    public String positionName;
    public String positionAddress;
    public String shopId;
    public String city;
    public String province;
    public double distance;
    //需要自己换算经纬度为地址????? how to compute distance?
    // current location and this location???
    public long lon;
    public long lan;
    public List<BookTimeModel> timeList = new ArrayList<>();

    public ShopListDateModel() {
        StringBuilder builder = new StringBuilder();
        for (int i = 9; i < 18; i++) {
            BookTimeModel model = new BookTimeModel();
            model.timeHour = i;
            builder.setLength(0);
            builder.append(i);
            builder.append(":00");
            model.showTimeString = builder.toString();
            builder.setLength(0);
            builder.append(i);
            builder.append(":00-");
            builder.append(i + 1);
            builder.append(":00");
            model.timeString = builder.toString();
            timeList.add(model);
        }
    }
}
