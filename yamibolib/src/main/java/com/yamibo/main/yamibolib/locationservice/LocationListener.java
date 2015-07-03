package com.yamibo.main.yamibolib.locationservice;


/**
 * created by 牛奶
 */
public interface LocationListener {
    /**
     * 当位置或定位状态发生改变时调用
     */
    void onLocationChanged(LocationService sender);
}
