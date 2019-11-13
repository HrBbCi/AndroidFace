package com.ax.detectionv2.tflite;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ax.detectionv2.R;
import com.ax.detectionv2.utils.ImagePreviewAdapter;
import com.ax.detectionv2.utils.ImageUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class TfPickActivity extends AppCompatActivity {

    private static final String TAG = TfPickActivity.class.getSimpleName();

    private static final int RC_HANDLE_WRITE_EXTERNAL_STORAGE_PERM = 3;
    private static int PICK_IMAGE_REQUEST = 5;
    private RecyclerView recyclerView;
    private ImagePreviewAdapter imagePreviewAdapter;
    private ArrayList<Bitmap> facesBitmap;
    private FaceView faceView;

    //Image Classifier
    private Classifier detector;
    // Configuration values for the prepackaged SSD model.
    private static final int TF_OD_API_INPUT_SIZE = 300;
    private static final float THRESHOLD = 0.5f;
    private static final boolean TF_OD_API_IS_QUANTIZED = true;
    private static final String TF_OD_API_MODEL_FILE = "detect.tflite";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/labelmap.txt";
    TextView result_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tf_pick);

        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Face Detect Image");

        faceView = findViewById(R.id.faceView);
        recyclerView = findViewById(R.id.recycler_view);
        result_text = findViewById(R.id.result_text);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        ((LinearLayoutManager) mLayoutManager).setOrientation(RecyclerView.HORIZONTAL);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());


        initDataTensor();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_photo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
            case R.id.gallery:
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        resetData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                if (bitmap != null) {
                    detectFace(bitmap);
//                    detectImageView(bitmap);
                } else
                    Toast.makeText(this, "Cann't open this image.", Toast.LENGTH_LONG).show();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != RC_HANDLE_WRITE_EXTERNAL_STORAGE_PERM) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }
        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            return;
        }
    }

    private void initDataTensor() {
        try {
            detector =
                    TFLiteObjectDetectionAPIModel.create(
                            getAssets(),
                            TF_OD_API_MODEL_FILE,
                            TF_OD_API_LABELS_FILE,
                            TF_OD_API_INPUT_SIZE,
                            TF_OD_API_IS_QUANTIZED);
        } catch (final IOException e) {
            e.printStackTrace();
            Toast toast =
                    Toast.makeText(
                            getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }

    }

    @SuppressLint("SetTextI18n")
    private void detectFace(Bitmap bitmap) {
        resetData();
        Bitmap reBitmap = ImageUtils.scaleImage(bitmap, TF_OD_API_INPUT_SIZE);
        List<Classifier.Recognition> faces;
        int countRotate = 0;
        int rotate = 0;
        long end, start;
        long time;
        do {
            start = System.currentTimeMillis();
            faces = detector.recognizeImage(reBitmap);
            if (count(faces) <= 0) {
                rotate += 90;
                reBitmap = ImageUtils.rotate(reBitmap, rotate);
                countRotate++;
            } else {
                break;
            }
            if (countRotate == 4) {
                break;
            }
        } while (count(faces) == 0);
        end = System.currentTimeMillis();
        time = end - start;
        result_text.setText("Kết quả: " + time + "ms; Số lượng khuôn mặt phát hiện:" + faces.size());

        faceView.setRotate(rotate);
        if (count(faces) == 0) {
            Toast.makeText(this, "Không tìm thấy!!!", Toast.LENGTH_SHORT).show();
        } else {
            for (final Classifier.Recognition faceResult : faces) {
                final RectF location = faceResult.getLocation();
                if (location != null && faceResult.getConfidence() >= THRESHOLD) {
                    Bitmap cropedFace = ImageUtils.cropFace(location, reBitmap, 0);
                    if (cropedFace != null) {
                        imagePreviewAdapter.add(cropedFace);
                    }
                }
            }
            FaceView overlay = (FaceView) findViewById(R.id.faceView);
            overlay.setContent(reBitmap, faces);

        }
    }

    private int count(List<Classifier.Recognition> results) {
        int count = 0;
        for (final Classifier.Recognition result : results) {
            final RectF location = result.getLocation();
            if (location != null && result.getConfidence() >= THRESHOLD) {
                count++;
            }
        }
        return count;
    }

    private void resetData() {
        if (imagePreviewAdapter == null) {
            facesBitmap = new ArrayList<>();
            imagePreviewAdapter = new ImagePreviewAdapter(TfPickActivity.this, facesBitmap, new ImagePreviewAdapter.ViewHolder.OnItemClickListener() {
                @Override
                public void onClick(View v, int position) {
                    imagePreviewAdapter.setCheck(position);
                    imagePreviewAdapter.notifyDataSetChanged();
                }
            });
            recyclerView.setAdapter(imagePreviewAdapter);
        } else {
            imagePreviewAdapter.clearAll();
        }
        faceView.reset();
    }
}
