package com.ax.detectionv2.tflite;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import java.text.DecimalFormat;
import java.util.List;


public class FaceOverlayObjecView extends View {

    private Paint mPaint;
    private Paint mTextPaint;
    private int mDisplayOrientation;
    private int mOrientation;
    private int previewWidth;
    private int previewHeight;
    private List<Classifier.Recognition> mFaces;
    private double fps;
    private boolean isFront = false;
    private static final float THRESHOLD = 0.8f;
    private Bitmap mBitmap;

    public FaceOverlayObjecView(Context context) {
        super(context);
        initialize();
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

    public void setFaces(List<Classifier.Recognition> faces) {
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

    private double scaleY(Canvas canvas) {
        double viewHeight = canvas.getHeight();
        double imageHeight = mBitmap.getHeight();
        double scaleY = viewHeight / imageHeight;
        return scaleY;
    }

    private double scaleX(Canvas canvas) {
        double viewWidth = canvas.getWidth();
        double imageWidth = mBitmap.getWidth();
        double scaleY = viewWidth / imageWidth;
        return scaleY;
    }

    private void drawFaceBox(Canvas canvas, double scaleX, double scaleY) {
        canvas.save();
        for (Classifier.Recognition face : mFaces) {
            RectF location = face.getLocation();
            if (location != null && face.getConfidence() >= THRESHOLD) {
                RectF rectF = new RectF();
                rectF.set(new RectF(location.left * (float) scaleX, location.top * (float) scaleY
                        , location.right * (float) scaleX, location.bottom * (float) scaleY));
                RectF rectF1 = new RectF();

                if (isFront) {
                    rectF.set(new RectF(location.left * (float) scaleX, location.top * (float) scaleY
                            , location.right * (float) scaleX, location.bottom * (float) scaleY));
                    float left = rectF.left;
                    float right = rectF.right;
                    rectF.left = (getWidth() - right);
                    rectF.right = (getWidth() - left);
                    rectF1.set(rectF.left, rectF.top, rectF.right, rectF.bottom);
                } else {
                    rectF1.set(rectF.left, rectF.top, rectF.right, rectF.bottom);
                }
                canvas.drawRect(rectF1, mPaint);
                canvas.drawText("ID " + face.getId(), rectF1.left, rectF1.bottom + mTextPaint.getTextSize(), mTextPaint);
                canvas.drawText("" + face.getConfidence(), rectF1.left, rectF1.bottom + mTextPaint.getTextSize() * 2, mTextPaint);
            }
        }
        canvas.restore();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mFaces != null && mFaces.size() > 0) {
            drawFaceBox(canvas, scaleX(canvas), scaleY(canvas));
        }
        DecimalFormat df2 = new DecimalFormat(".##");
        canvas.drawText("Detected_Frame/s: " + df2.format(fps) + " @ " + previewWidth + "x" + previewHeight, mTextPaint.getTextSize(), mTextPaint.getTextSize(), mTextPaint);
//        canvas.drawText(previewWidth + "x" + previewHeight, mTextPaint.getTextSize(), mTextPaint.getTextSize(), mTextPaint);
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