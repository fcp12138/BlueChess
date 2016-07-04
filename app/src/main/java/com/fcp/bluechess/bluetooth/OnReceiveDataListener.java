package com.fcp.bluechess.bluetooth;

/**
 * 接收数据接口
 * Created by fcp on 2015/9/7.
 */
public interface OnReceiveDataListener {
    /**
     * 成功获取到数据
     * @param data String
     */
    void onReceiverSuccess(String data);
}
