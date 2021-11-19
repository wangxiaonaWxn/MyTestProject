package com.mega.carmaintenance.network;

import com.google.gson.annotations.SerializedName;
import com.mega.carmaintenance.model.RecordsModel;

public class RecordResponse {
    @SerializedName("data")
    public RecordsModel shopList;
}
