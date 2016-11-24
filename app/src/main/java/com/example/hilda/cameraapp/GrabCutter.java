package com.example.hilda.cameraapp;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

/**
 * Created by Johnny on 11/23/2016.
 */

public class GrabCutter {

    Mat inputImage;
    Rect rect;

    Mat mask=new Mat();
    Mat outputImage=new Mat();

    Mat fgdModel=new Mat();
    Mat bgdModel=new Mat();


    public GrabCutter()
    {

    }

    public GrabCutter(Mat inputImage, Rect
            rect)
    {
        this.inputImage=inputImage.clone();
        this.rect=rect.clone();
    }


    public GrabCutter(Mat inputImage)
    {
        this.inputImage=inputImage.clone();
        int r = inputImage.rows();
        int c = inputImage.cols();

        Point p1 = new Point(c/5, 0);
        Point p2 = new Point(c-c/5, r-1);

        this.rect = new Rect(p1,p2);

    }

    public void setInputImage(Mat inputImage) {
        this.inputImage = inputImage.clone();
    }

    public void setRect(Rect rect) {
        this.rect = rect.clone();
    }

    public Mat getMask() {
        //Rect rect = new Rect(50,30, 100,200);

        mask.setTo(new Scalar(125));
        fgdModel.setTo(new Scalar(255, 255, 255));
        bgdModel.setTo(new Scalar(255, 255, 255));

        Mat imgC3 = new Mat();
        Imgproc.cvtColor(inputImage, imgC3, Imgproc.COLOR_RGBA2RGB);
        Log.d(MainActivity.LOG_TAG, "imgC3: " + imgC3);

        Log.d(MainActivity.LOG_TAG, "Grabcut begins");
        Imgproc.grabCut(imgC3, mask, rect, bgdModel, fgdModel, 5, Imgproc.GC_INIT_WITH_RECT);

        Core.convertScaleAbs(mask, mask, 100, 0);
        Imgproc.cvtColor(mask, mask, Imgproc.COLOR_GRAY2RGBA);

        return mask;
    }


}
