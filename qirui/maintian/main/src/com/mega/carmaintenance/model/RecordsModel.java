package com.mega.carmaintenance.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class RecordsModel implements Serializable {
    public String storeName;
    public String storePhoneNum;
    public String recordTime;
    public String storeAddress;
    @SerializedName("type")
    public String recordType; // 预约项目类型 1.保养，2.维修
    public String recordTypeName;
}
