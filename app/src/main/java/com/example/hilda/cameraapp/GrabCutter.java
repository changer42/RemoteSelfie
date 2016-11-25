package com.example.hilda.cameraapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.example.hilda.cameraapp.MainActivity;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;

/**
 * Created by Johnny on 11/23/2016.
 */

public class GrabCutter {

    Mat inputImage;
    Rect rect;

    Mat mask = new Mat();
    Mat outputImage = new Mat();

    Mat fgdModel = new Mat();
    Mat bgdModel = new Mat();


    Point p1, p2;

    public GrabCutter() {

    }

    public GrabCutter(Mat inputImage, Rect
            rect) {
        this.inputImage = inputImage.clone();
        this.rect = rect.clone();

        p1 = rect.tl();
        p2 = rect.br();

    }


    public GrabCutter(Mat inputImage) {
        this.inputImage = inputImage.clone();
        int r = inputImage.rows();
        int c = inputImage.cols();

        p1 = new Point(c / 10, 0);
        p2 = new Point(c - c / 10, r - 1);

        this.rect = new Rect(p1, p2);

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

//        Core.convertScaleAbs(mask, mask, 100, 0);
//        Imgproc.cvtColor(mask, mask, Imgproc.COLOR_GRAY2RGBA);

        return mask;
    }

    public Mat getMaskImage() {
        getMask();

        Mat source = new Mat(1, 1, CvType.CV_8U, new Scalar(3.0));

        Mat newMask = new Mat();
        Core.compare(mask, source, newMask, Core.CMP_EQ);
        Mat foreground = new Mat(inputImage.size(), CvType.CV_8UC3, new Scalar(255, 255, 255));
        inputImage.copyTo(foreground, newMask);
        Imgproc.rectangle(inputImage, p1, p2, new Scalar(255, 0, 0, 255));

//        Mat background = new Mat();
//        try {
//            background = Utils.loadResource(getContext(),
//                    R.drawable.wall2);
//        } catch (IOException e) {
//
//            e.printStackTrace();
//        }
//        Mat tmp = new Mat();
//        Imgproc.resize(background, tmp, inputImage.size());
//        background = tmp;

        Mat background = new Mat(inputImage.size(), inputImage.type());
        background.setTo(new Scalar(5, 5, 5));
        Mat tempMask = new Mat(foreground.size(), CvType.CV_8UC1, new Scalar(255, 255, 255));
        Imgproc.cvtColor(foreground, tempMask, 6/* COLOR_BGR2GRAY */);
        //Imgproc.threshold(tempMask, tempMask, 254, 255, 1 /* THRESH_BINARY_INV */);

        Mat vals = new Mat(1, 1, CvType.CV_8UC3, new Scalar(0.0));
        Mat dst = new Mat();
        background.setTo(vals, tempMask);
        Imgproc.resize(foreground, foreground, mask.size());
        Core.add(background, foreground, dst, tempMask);

        //convert to Bitmap
        Log.d(MainActivity.LOG_TAG, "Convert to Bitmap");

        //release MAT part
        //mask.release();
        //fgdModel.release();
        //bgdModel.release();

        return dst;
    }
}