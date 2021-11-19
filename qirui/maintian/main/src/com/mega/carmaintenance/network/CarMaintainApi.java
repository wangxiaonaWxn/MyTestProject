package com.mega.carmaintenance.network;

import java.util.Map;
import mega.http.DefaultRequest;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

public interface CarMaintainApi {
    @POST("/api/1/profile/contacts/upload")
    DefaultRequest<RecordResponse> getMaintainRecordList(@QueryMap Map<String, String> map,
                                                       @Body RequestBody contacts);

    @POST("/api/1/profile/contacts/upload")
    DefaultRequest<ShopListResponse> get4SShopList(@QueryMap Map<String, String> map,
                                                         @Body RequestBody contacts);

    @POST("/api/1/profile/contacts/upload")
    DefaultRequest<BookResultResponse> bookMaintain(@QueryMap Map<String, String> map,
                                                         @Body RequestBody contacts);
}
