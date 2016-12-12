package com.example.hilda.cameraapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import static android.R.id.input;
import static android.R.id.mask;

public class DisplayActivity extends AppCompatActivity {

    Mat inputImage=new Mat();
    Mat outputImage=new Mat();
    Mat fgModel=new Mat();
    Mat bgModel=new Mat();
    Rect rect=new Rect();

    Mat grabcutMask=new Mat();
    Mat blendMask=new Mat();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        inputImage=MainActivity.capturedInFrame.clone();
        outputImage=new Mat();

        fgModel=ScribbleActivity.fgModel;
        bgModel=ScribbleActivity.bgModel;
        grabcutMask=ScribbleActivity.grabCutMask.clone();

        Log.d(MainActivity.LOG_TAG,inputImage.size()+"");
        Log.d(MainActivity.LOG_TAG,fgModel.size()+"");


        //Mat temp=new Mat();
        //Imgproc.cvtColor(inputImage,temp,Imgproc.COLOR_BGR2RGB);

        //display(ScribbleActivity.grabCutMask);
        grabCut(inputImage);
        //grabCut(R.drawable.graves);
        //display(outputImage);
        //display(add(inputImage,fgModel));
        blend(outputImage,MainActivity.capturedBackground.clone(),blendMask);
        display(outputImage);
    }

    private void grabCut(int imgId) {

        Bitmap bmpIn= BitmapFactory.decodeResource(getResources(), imgId);
        Mat mat=new Mat();
        Utils.bitmapToMat(bmpIn, mat);
        //Imgproc.cvtColor(mat,mat,Imgproc.COLOR_RGB2BGR);

        GrabCutter grabCutter=new GrabCutter(mat);
        outputImage = grabCutter.getMaskImage();
    }

    private void grabCut(Mat inputImage) {

        GrabCutter grabCutter=new GrabCutter(inputImage);
        //grabCutter.setMethod(Imgproc.GC_INIT_WITH_MASK);
        grabCutter.setUserMask(grabcutMask);

        outputImage = grabCutter.getMaskImage().clone();
        blendMask = grabCutter.getBlendMask().clone();
//
        //Core.convertScaleAbs(blendMask, blendMask, 100, 0);
        //Imgproc.cvtColor(blendMask, outputImage, Imgproc.COLOR_GRAY2RGBA);
    }

    private void blend(Mat src,Mat dst,Mat mask)
    {
        ImageBlender blender=new ImageBlender(src,dst);
        blender.setMask(mask);
        outputImage=blender.simpleBlend();
    }

    private void display(Mat mat) {
        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
        Bitmap bmpOut = Bitmap.createBitmap(mat.width(), mat.height(), conf);

        Utils.matToBitmap(mat, bmpOut);

        ImageView iv= (ImageView) findViewById(R.id.cutImage);
        iv.setImageBitmap(bmpOut);
    }

    private Mat add(Mat A,Mat B)
    {
        Mat C=new Mat();
        Core.add(A,B,C);
        return C;
    }
}
