package com.ax.detectionv2.ncnn;

import android.graphics.Bitmap;

/**
 *  Giao diện java của MobileNetssd, lặp lại mã c ++ riêng có nguồn gốc cục bộ Tệp này lặp lại MobileNetssd.cpp
 */
public class MobileNetssd {

    public native boolean Init(byte[] param, byte[] bin); // Chức năng khởi tạo
    public native float[] Detect(Bitmap bitmap); // Chức năng phát hiện
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("MobileNetssd");
    }
}

