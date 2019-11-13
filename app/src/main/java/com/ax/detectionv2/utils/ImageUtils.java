package com.ax.detectionv2.utils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ImageUtils {

    public static Bitmap scaleImage(Bitmap bitmap, int maxSize) {
        int outWidth;
        int outHeight;
        int inWidth = bitmap.getWidth();
        int inHeight = bitmap.getHeight();
        if (inWidth > inHeight) {
            outWidth = maxSize;
            outHeight = (inHeight * maxSize) / inWidth;
        } else {
            outHeight = maxSize;
            outWidth = (inWidth * maxSize) / inHeight;
        }
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, outWidth, outHeight, false);
        return resizedBitmap;
    }

    //Rotate Bitmap
    public final static Bitmap rotate(Bitmap b, float degrees) {
        if (degrees != 0 && b != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) b.getWidth() / 2,
                    (float) b.getHeight() / 2);
            Bitmap b2 = Bitmap.createBitmap(b, 0, 0, b.getWidth(),
                    b.getHeight(), m, true);
            if (b != b2) {
                b.recycle();
                b = b2;
            }
        }
        return b;
    }

    //PB File
    public static Bitmap cropFace(Rect location, Bitmap bitmap, int rotate) {
        Bitmap bmp;

        Bitmap.Config config = Bitmap.Config.RGB_565;
        if (bitmap.getConfig() != null) config = bitmap.getConfig();
        bmp = bitmap.copy(config, true);

        switch (rotate) {
            case 90:
                bmp = ImageUtils.rotate(bmp, 270);
                break;
            case 180:
                bmp = ImageUtils.rotate(bmp, 360);
                break;
            case 270:
                bmp = ImageUtils.rotate(bmp, 0);
                break;
        }

        bmp = ImageUtils.cropBitmap(bmp, location);
        return bmp;
    }

    //Tflite
    public static Bitmap cropFace(RectF location, Bitmap bitmap, int rotate) {
        Bitmap bmp;

        Bitmap.Config config = Bitmap.Config.RGB_565;
        if (bitmap.getConfig() != null) config = bitmap.getConfig();
        bmp = bitmap.copy(config, true);

        switch (rotate) {
            case 90:
                bmp = ImageUtils.rotate(bmp, 270);
                break;
            case 180:
                bmp = ImageUtils.rotate(bmp, 360);
                break;
            case 270:
                bmp = ImageUtils.rotate(bmp, 0);
                break;
        }

        bmp = ImageUtils.cropBitmap(bmp, location);
        return bmp;
    }
    public static Bitmap cropBitmap(Bitmap bitmap, RectF rect) {
        int w = (int) (rect.right - rect.left);
        int h = (int) (rect.bottom - rect.top);
        Bitmap ret = Bitmap.createBitmap(w, h, bitmap.getConfig());
        Canvas canvas = new Canvas(ret);
        canvas.drawBitmap(bitmap, -rect.left, -rect.top, null);
        bitmap.recycle();
        return ret;
    }

    //PB File
    public static Bitmap cropBitmap(Bitmap bitmap, Rect rect) {
        int w = (int) (rect.right - rect.left);
        int h = (int) (rect.bottom - rect.top);
        Bitmap ret = Bitmap.createBitmap(w, h, bitmap.getConfig());
        Canvas canvas = new Canvas(ret);
        canvas.drawBitmap(bitmap, -rect.left, -rect.top, null);
        bitmap.recycle();
        return ret;
    }

    //Save Bitmap
    public static void saveTempBitmap(Activity activity, Bitmap bitmap) {
        if (isExternalStorageWritable()) {
            saveImage(activity, bitmap);
        } else {
        }
    }

    public static void saveImage(Activity activity, Bitmap finalBitmap) {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/Download");
        myDir.mkdirs();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fname = "cut_" + timeStamp + ".jpg";
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                final Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                final Uri contentUri = Uri.fromFile(file);
                scanIntent.setData(contentUri);
                activity.sendBroadcast(scanIntent);
            } else {
                final Intent intent = new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory()));
                activity.sendBroadcast(intent);
            }
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public static void appendLog(String text) {
        File logFile = new File("sdcard/log.txt");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Chuyển đổi mảng một chiều thành mảng hai chiều
    public static float[][] TwoArry(float[] inputfloat) {
        int n = inputfloat.length;
        int num = inputfloat.length / 6;
        float[][] outputfloat = new float[num][6];
        int k = 0;
        for (int i = 0; i < num; i++) {
            int j = 0;

            while (j < 6) {
                outputfloat[i][j] = inputfloat[k];
                k++;
                j++;
            }

        }

        return outputfloat;
    }
}
