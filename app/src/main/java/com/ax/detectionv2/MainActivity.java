package com.ax.detectionv2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.ax.detectionv2.ncnn.NcnnCamActivity;
import com.ax.detectionv2.ncnn.NcnnPiclActivity;
import com.ax.detectionv2.paddlelite.PaddleCamActivity;
import com.ax.detectionv2.paddlelite.PaddlePickActivity;
import com.ax.detectionv2.pb.PbCamActivity;
import com.ax.detectionv2.pb.PbPickActivity;
import com.ax.detectionv2.tflite.TfCamActivity;
import com.ax.detectionv2.tflite.TfPickActivity;

public class MainActivity extends AppCompatActivity {

    Button btnTfliteCam, btnTflitePick, btnNCNNCam, btnNcnnPick,
            btnPB, btnPBPick, btnPaddllLite, btnPaddllLitePick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnTfliteCam = findViewById(R.id.btnTfliteCam);
        btnTflitePick = findViewById(R.id.btnTflitePick);
        btnNCNNCam = findViewById(R.id.btnNCNNCam);
        btnNcnnPick = findViewById(R.id.btnNcnnPick);
        btnPB = findViewById(R.id.btnPB);
        btnPBPick = findViewById(R.id.btnPBPick);
        btnPaddllLite = findViewById(R.id.btnPaddllLite);
        btnPaddllLitePick = findViewById(R.id.btnPaddllLitePick);

        btnTfliteCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, TfCamActivity.class));
            }
        });
        btnTflitePick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, TfPickActivity.class));
            }
        });
        btnNCNNCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, NcnnCamActivity.class));
            }
        });
        btnNcnnPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, NcnnPiclActivity.class));
            }
        });
        btnPB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, PbCamActivity.class));
            }
        });
        btnPBPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, PbPickActivity.class));
            }
        });
        btnPaddllLite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, PaddleCamActivity.class));
            }
        });
        btnPaddllLitePick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, PaddlePickActivity.class));
            }
        });
    }
}
