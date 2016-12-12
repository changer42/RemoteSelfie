package com.example.hilda.cameraapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import static org.opencv.core.CvType.CV_8U;

public class ScribbleActivity extends AppCompatActivity {

    Mat inputImage=new Mat();
    Mat outputImage=new Mat();

    public static Mat fgModel=new Mat();
    public static Mat bgModel=new Mat();
    public static Rect rect=new Rect();


    private DrawableView dv;
    public static Mat grabCutMask=new Mat();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //In child activity
        //long addr = getIntent().getLongExtra("myImg", 0);
        //inputImage=new Mat(addr);
        //Mat img = tempImg.clone();

        inputImage=MainActivity.capturedOutFrame.clone();

        dv=new DrawableView(this);
        dv.setInputImage(inputImage);
        setContentView(dv);

        //grabCut();

        //fillHoles();
        //display(outputImage);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_scribble,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id=item.getItemId();

        switch (id) {
            case R.id.menu_action_bg:
                dv.setForeground(false);
                setContentView(dv);
                break;
            case R.id.menu_action_fg:
                dv.setForeground(true);
                setContentView(dv);
                break;
            case R.id.menu_action_process:

//                Mat temp1= Bitmap2Mat(dv.getBlankFgBitmap());
//                Mat temp2= Bitmap2Mat(dv.getBlankBgBitmap());
//
//                Mat temp3=new Mat();
//                Mat temp4=new Mat();
//                Imgproc.cvtColor(temp1,temp3,Imgproc.COLOR_RGB2GRAY);
//                Imgproc.cvtColor(temp2,temp4,Imgproc.COLOR_RGB2GRAY);
//
//                Imgproc.resize(temp3,fgModel,MainActivity.capturedInFrame.size());
//                Imgproc.resize(temp4,bgModel,MainActivity.capturedInFrame.size());

                Mat temp=Bitmap2Mat(dv.getCurBlankBitmap());
                Imgproc.cvtColor(temp,temp,Imgproc.COLOR_RGB2GRAY);
                Imgproc.resize(temp, temp,MainActivity.capturedInFrame.size());

                Core.MinMaxLocResult minMaxLocResult=Core.minMaxLoc(temp);
                double min=minMaxLocResult.minVal;
                double max=minMaxLocResult.maxVal;

                grabCutMask=temp.clone();

                Intent intent=new Intent(this,DisplayActivity.class);
                startActivity(intent);

                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public Mat Bitmap2Mat(Bitmap bitmap)
    {
        Mat mat=new Mat();
        Utils.bitmapToMat(bitmap,mat);
        return mat;
    }

    private void fillHoles()
    {
        BgdSubtractor bgdSubtractor=new BgdSubtractor();
        outputImage=bgdSubtractor.fillHoles(inputImage);
    }
}
