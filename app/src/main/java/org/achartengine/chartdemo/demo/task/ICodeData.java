package org.achartengine.chartdemo.demo.task;

/**
 * Created by A41569 on 9/21/2016.
 */
public interface ICodeData {
    /**
     * Get the byte[] to be transformed.
     *
     *
     * @return the byte[] to be transfromed
     */
    byte[] getBytes();

    /**
     * Get the char[](u8[]) to be transfromed.
     *
     * @return the char[](u8) to be transformed
     */
    char[] getU8s();
}
