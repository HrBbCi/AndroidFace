package com.ax.detectionv2.ncnn;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


public class FaceOverlayObjecView extends View {

    private Paint mPaint;
    private Paint mTextPaint;
    private int mDisplayOrientation;
    private int mOrientation;
    private int previewWidth;
    private int previewHeight;
    private float[] mFaces;
    private double fps;
    private boolean isFront = false;
    private Bitmap mBitmap;
    private float scaleX;
    private float scaleY;
    private List<String> resultLabel = new ArrayList<>();

    public List<String> getResultLabel() {
        return resultLabel;
    }

    public void setResultLabel(List<String> resultLabel) {
        this.resultLabel = resultLabel;
    }

    public FaceOverlayObjecView(Context context) {
        super(context);
        initialize();
    }

    @Override
    public float getScaleX() {
        return scaleX;
    }

    public void setScaleX(float scaleX) {
        this.scaleX = scaleX;
    }

    @Override
    public float getScaleY() {
        return scaleY;
    }

    public void setScaleY(float scaleY) {
        this.scaleY = scaleY;
    }

    public void setmBitmap(Bitmap mBitmap) {
        this.mBitmap = mBitmap;
    }

    private void initialize() {
        // We want a green box around the face:
        DisplayMetrics metrics = getResources().getDisplayMetrics();

        int stroke = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, metrics);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.GREEN);
        mPaint.setStrokeWidth(stroke);
        mPaint.setStyle(Paint.Style.STROKE);

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setDither(true);
        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, metrics);
        mTextPaint.setTextSize(size);
        mTextPaint.setColor(Color.GREEN);
        mTextPaint.setStyle(Paint.Style.FILL);
    }

    public void setFPS(double fps) {
        this.fps = fps;
    }

    public void setFaces(float[] faces) {
        mFaces = faces;
        invalidate();
    }

    public void setOrientation(int orientation) {
        mOrientation = orientation;
    }

    public void setDisplayOrientation(int displayOrientation) {
        mDisplayOrientation = displayOrientation;
        invalidate();
    }

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

    private void drawFaceBox(Canvas canvas) {
        canvas.save();
        int count = 0;

        float get_finalresult[][] = TwoArry(mFaces);
        int object_num = 0;
        int num = mFaces.length / 6;// number of object
        //continue to draw rect
        for (object_num = 0; object_num < num; object_num++) {
            canvas.drawRect(get_finalresult[object_num][2] * scaleX,
                    get_finalresult[object_num][3] * scaleY,
                    get_finalresult[object_num][4] * scaleX,
                    get_finalresult[object_num][5] * scaleY ,mPaint);
            canvas.drawText(resultLabel.get((int) get_finalresult[object_num][0]) +
                            "\n" + get_finalresult[object_num][1],
                    get_finalresult[object_num][2] * scaleX,
                    get_finalresult[object_num][3] * scaleY, mTextPaint);
        }
        canvas.restore();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mFaces != null ) {
            drawFaceBox(canvas);
        }
        DecimalFormat df2 = new DecimalFormat(".##");
        canvas.drawText("Detected_Frame/s: " + df2.format(fps) + " @ "
                + previewWidth + "x" + previewHeight, mTextPaint.getTextSize(),
                mTextPaint.getTextSize(), mTextPaint);
    }

    public void setPreviewWidth(int previewWidth) {
        this.previewWidth = previewWidth;
    }

    public void setPreviewHeight(int previewHeight) {
        this.previewHeight = previewHeight;
    }

    public void setFront(boolean front) {
        isFront = front;
    }
}