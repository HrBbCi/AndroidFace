package com.ax.detectionv2.ncnn;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ax.detectionv2.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

public class NcnnPiclActivity extends AppCompatActivity {

    private static final String TAG = NcnnPiclActivity.class.getName();
    private static final int USE_PHOTO = 1001;
    private String camera_image_path;
    private ImageView show_image;
    private TextView result_text;
    private boolean load_result = false;
    private int[] ddims = {1, 3, 300, 300}; // Giá trị của kích thước ở đây phải tương ứng với đầu vào của mô hình
    private int model_index = 1;
    private List<String> resultLabel = new ArrayList<>();
    private MobileNetssd mobileNetssd = new MobileNetssd(); //Khởi tạo giao diện //
    // java Sau đây sử dụng trực tiếp chức năng java để gọi hàm NDK c + +
    Button btn_select;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ncnn_picl);
        try {
            initMobileNetSSD();
        } catch (IOException e) {
            Log.e("MainActivity", "initMobileNetSSD error");
        }
        init_view();
        readCacheLabelFromLocalFile();
    }

    /**
     * Khởi tạo MobileNetssd, để tải tệp mô hình
     */
    private void initMobileNetSSD() throws IOException {
        byte[] param = null;
        byte[] bin = null;
        {
            //Đọc tệp nhị phân với luồng io và cuối cùng lưu trữ nó trong mảng byte [].
            InputStream assetsInputStream = getAssets().open("MobileNetSSD_deploy.param.bin");// param：  网络结构文件
            int available = assetsInputStream.available();
            param = new byte[available];
            int byteCode = assetsInputStream.read(param);
            assetsInputStream.close();
        }
        {
            //Đọc tệp nhị phân với luồng io và cuối cùng lưu nó thành byte, chuyển đổi thành kiểu int
            InputStream assetsInputStream = getAssets().open("MobileNetSSD_deploy.bin");//bin：   model文件
            int available = assetsInputStream.available();
            bin = new byte[available];
            int byteCode = assetsInputStream.read(bin);
            assetsInputStream.close();
        }

        load_result = mobileNetssd.Init(param, bin);// 再将文件传入java的NDK接口(c++ 代码中的init接口 )
        Log.d("load model", "MobileNetSSD_load_model_result:" + load_result);
    }


    // initialize view
    private void init_view() {
        request_permissions();
        show_image = (ImageView) findViewById(R.id.show_image);
        result_text = (TextView) findViewById(R.id.result_text);
        btn_select = (Button) findViewById(R.id.btn_select);
        // use photo click
        btn_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!load_result) {
                    Toast.makeText(NcnnPiclActivity.this, "never load model", Toast.LENGTH_SHORT).show();
                    return;
                }
                PhotoUtil.use_photo(NcnnPiclActivity.this, USE_PHOTO);
            }
        });
    }

    // load label's name
    private void readCacheLabelFromLocalFile() {
        try {
            AssetManager assetManager = getApplicationContext().getAssets();
            BufferedReader reader = new BufferedReader(new InputStreamReader(assetManager.open("words.txt")));//这里是label的文件
            String readLine = null;
            while ((readLine = reader.readLine()) != null) {
                resultLabel.add(readLine);
            }
            reader.close();
        } catch (Exception e) {
            Log.e("labelCache", "error " + e);
        }
    }


    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String image_path;
        RequestOptions options = new RequestOptions().skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case USE_PHOTO:
                    if (data == null) {
                        Log.w(TAG, "user photo data is null");
                        return;
                    }
                    Uri image_uri = data.getData();

                    //Glide.with(MainActivity.this).load(image_uri).apply(options).into(show_image);

                    // get image path from uri
                    image_path = PhotoUtil.get_path_from_URI(NcnnPiclActivity.this, image_uri);
                    // predict image
                    predict_image(image_path);
                    break;
            }
        }
    }

    private void predict_image(String image_path) {
        // picture to float array
        Bitmap bmp = PhotoUtil.getScaleBitmap(image_path);
        Bitmap rgba = bmp.copy(Bitmap.Config.ARGB_8888, true);
        // resize
        Bitmap input_bmp = Bitmap.createScaledBitmap(rgba, ddims[2], ddims[3], false);
        try {
            // Data format conversion takes too long
            // Log.d("inputData", Arrays.toString(inputData));
            long start = System.currentTimeMillis();
            // get predict result
            float[] result = mobileNetssd.Detect(input_bmp);
            // time end
            long end = System.currentTimeMillis();
            Log.d(TAG, "origin predict result:" + Arrays.toString(result));
            long time = end - start;
            Log.d("result length", "length of result: " + String.valueOf(result.length));
            // show predict result and time
            // float[] r = get_max_result(result);

//            String show_text = "result：" + Arrays.toString(result) + "\nname：" + resultLabel.get((int) result[0]) + "\nprobability：" + result[1] + "\ntime：" + time + "ms";


            // Cấu hình
            Canvas canvas = new Canvas(rgba);
            //Vẽ một hình chữ nhật trên hình ảnh
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5);
            int count = 0;

            float get_finalresult[][] = TwoArry(result);
            int object_num = 0;
            int num = result.length / 6;// number of object
            //continue to draw rect
            for (object_num = 0; object_num < num; object_num++) {
                // 画框
                paint.setColor(Color.RED);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(5);
                Log.d("CONV",resultLabel.get((int) result[object_num]));
                if(resultLabel.get((int) get_finalresult[object_num][0]).equals("person")){
                    canvas.drawRect(get_finalresult[object_num][2] * rgba.getWidth(), get_finalresult[object_num][3] * rgba.getHeight(),
                            get_finalresult[object_num][4] * rgba.getWidth(), get_finalresult[object_num][5] * rgba.getHeight(), paint);
                    canvas.drawText(resultLabel.get((int) get_finalresult[object_num][0]) +
                                    "\n" + get_finalresult[object_num][1],
                        get_finalresult[object_num][2] * rgba.getWidth(), get_finalresult[object_num][3] * rgba.getHeight(), paint);
                    count ++;
                }

            }
            String show_text = "result："  + count  + "\ntime：" + time + "ms";
            result_text.setText(show_text);
            show_image.setImageBitmap(rgba);


        } catch (Exception e) {
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

    /*
    // get max probability label
    private float[] get_max_result(float[] result) {
        int num_rs = result.length / 6;
        float maxProp = result[1];
        int maxI = 0;
        for(int i = 1; i<num_rs;i++){
            if(maxProp<result[i*6+1]){
                maxProp = result[i*6+1];
                maxI = i;
            }
        }
        float[] ret = {0,0,0,0,0,0};
        for(int j=0;j<6;j++){
            ret[j] = result[maxI*6 + j];
        }
        return ret;
    }
    */
    // request permissions(add)
    private void request_permissions() {
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.CAMERA);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        // if list is not empty will request permissions
        if (!permissionList.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        int grantResult = grantResults[i];
                        if (grantResult == PackageManager.PERMISSION_DENIED) {
                            String s = permissions[i];
                            Toast.makeText(this, s + "permission was denied", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
        }
    }
}
