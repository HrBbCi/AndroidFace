package com.ax.detectionv2.tflite;

import android.graphics.Bitmap;
import android.graphics.RectF;

import java.util.List;

public interface Classifier {
    List<Recognition> recognizeImage(Bitmap bitmap);

    void enableStatLogging(final boolean debug);

    String getStatString();

    void close();

    void setNumThreads(int num_threads);

    void setUseNNAPI(boolean isChecked);

    public class Recognition {
        private int id;
        private String title;
        private float confidence;
        private RectF location;
        private long time;

        public Recognition(
                int id, String title, float confidence, RectF location, long time) {
            this.id = id;
            this.title = title;
            this.confidence = confidence;
            this.location = location;
            time = System.currentTimeMillis();
        }
        public Recognition(
                int id, String title, float confidence, RectF location) {
            this.id = id;
            this.title = title;
            this.confidence = confidence;
            this.location = location;
        }
        public Recognition() {
            id = 0;
            title = "";
            confidence = 0.4f;
            location = new RectF(0.0f, 0.0f, 0.0f, 0.0f);
            time = System.currentTimeMillis();
        }

        public void setFace(int id, String title, float confidence, RectF location, long time) {
            set(id, title, confidence,  location, time);
        }

        public synchronized void set(int id, String title, float confidence, RectF location, long time) {
            this.id = id;
            this.title = title;
            this.confidence = confidence;
            this.location = location;
            this.time = time;
        }

        public void clear() {
            set(0, "", 0.4f,  new RectF(0.0f, 0.0f, 0.0f, 0.0f)
                    , System.currentTimeMillis());
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public int getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public float getConfidence() {
            return confidence;
        }

        public RectF getLocation() {
            return new RectF(location);
        }

        public void setLocation(RectF location) {
            this.location = location;
        }

        @Override
        public String toString() {
            return "[" + confidence + ";;;" + location + "]";
        }
    }
}
