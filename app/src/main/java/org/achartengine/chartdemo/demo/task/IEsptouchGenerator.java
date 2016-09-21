package org.achartengine.chartdemo.demo.task;

/**
 * Created by A41569 on 9/21/2016.
 */
public interface IEsptouchGenerator {
    /**
     * Get guide code by the format of byte[][]
     *
     * @return guide code by the format of byte[][]
     */
    byte[][] getGCBytes2();

    /**
     * Get data code by the format of byte[][]
     *
     * @return data code by the format of byte[][]
     */
    byte[][] getDCBytes2();
}
