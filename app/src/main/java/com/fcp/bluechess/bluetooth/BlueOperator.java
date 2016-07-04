package com.fcp.bluechess.bluetooth;

/**
 * 蓝牙操作接口
 * Created by fcp on 2015/9/7.
 */
public interface BlueOperator {

    /**
     * 生成
     */
    void create(OnCreateListener onCreateListener);

    /**
     * 发送数据
     */
    void send(String sendData);

    /**
     * 接收
     */
    void receive();

    /**
     * 关闭资源
     */
    void close();

}
