package com.fcp.bluechess.bluetooth;

/**
 * 生成监视器
 * Created by fcp on 2015/9/7.
 */
public interface OnCreateListener {

    /**
     * 生成成功
     */
    void onCreateSuccess();

    /**
     * 生成失败
     */
    void onCreateFail(String cause);

}
