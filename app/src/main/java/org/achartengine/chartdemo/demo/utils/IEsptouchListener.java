package org.achartengine.chartdemo.demo.utils;

/**
 * Created by A41569 on 9/21/2016.
 */
public interface IEsptouchListener {
    /**
     * when new esptouch result is added, the listener will call
     * onEsptouchResultAdded callback
     *
     * @param result
     *            the Esptouch result
     */
    void onEsptouchResultAdded(IEsptouchResult result);
}
