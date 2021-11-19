package com.mega.carmaintenance.callback;

import com.mega.carmaintenance.model.ShopListDateModel;

public interface PositionItemCallback {
    void onRepairClick();

    void onMaintainClick();

    void onTimeClick(String timeString);

    void updateShopId(ShopListDateModel shopId);
}
