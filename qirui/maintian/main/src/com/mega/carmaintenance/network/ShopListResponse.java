package com.mega.carmaintenance.network;

import com.google.gson.annotations.SerializedName;
import com.mega.carmaintenance.model.ShopListDateModel;

public class ShopListResponse {
    @SerializedName("data")
    public ShopListDateModel shopList;
}
