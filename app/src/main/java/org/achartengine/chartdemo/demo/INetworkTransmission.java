package org.achartengine.chartdemo.demo;

/**
 * Created by water.zhou on 10/28/2014.
 */
public interface INetworkTransmission {

    public void setParameters(String ip, int port);
    public boolean open();
    public void close();
    public boolean send(String text);
    public void onReceive(byte[] buffer, int length);
}
