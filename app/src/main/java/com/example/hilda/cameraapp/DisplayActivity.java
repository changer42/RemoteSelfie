package com.example.hilda.cameraapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

public class DisplayActivity extends AppCompatActivity {

    Mat inputImage=new Mat();
    Mat outputImage=new Mat();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        //In child activity
        //long addr = getIntent().getLongExtra("myImg", 0);
        //inputImage=new Mat(addr);
        //Mat img = tempImg.clone();

        inputImage=MainActivity.capturedFrame.clone();

        grabCut();
        display(outputImage);
    }

    private void grabCut() {

        GrabCutter grabCutter=new GrabCutter(inputImage);
        outputImage = grabCutter.getMaskImage();
    }

    private void display(Mat mat) {
        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
        Bitmap bmpOut = Bitmap.createBitmap(mat.width(), mat.height(), conf);

        Utils.matToBitmap(mat, bmpOut);

        ImageView iv= (ImageView) findViewById(R.id.cutImage);
        iv.setImageBitmap(bmpOut);
    }
}
