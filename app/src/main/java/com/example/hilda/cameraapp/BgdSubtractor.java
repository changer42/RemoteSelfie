package com.example.hilda.cameraapp;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractorMOG2;
import org.opencv.video.Video;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.opencv.core.Core.merge;
import static org.opencv.core.Core.split;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.COLOR_BGRA2BGR;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;

/**
 * Created by Johnny on 11/22/2016.
 */

public class BgdSubtractor {

    private BackgroundSubtractorMOG2 bgs;
    private Mat fgMask =new Mat();
    private Mat output=new Mat();
    private Mat input=new Mat();

    private Mat background =new Mat();
    private Mat backgroundSeg=new Mat();

    Mat b=new Mat(),g=new Mat(),r=new Mat();
    List<Mat> bgr=new ArrayList<>();
    Mat mask=new Mat();
    Mat colorMask=new Mat();
    Mat res=new Mat();

    public BgdSubtractor()
    {
        bgs= Video.createBackgroundSubtractorMOG2();
    }

    public Mat getBlendImage()
    {
        mask = getThresholdMask();
        colorMask = getColorMask();

        //Mat newMask=new Mat();
        Imgproc.cvtColor(mask,mask,COLOR_BGR2GRAY);

        //Mat newMask2=new Mat();
        Imgproc.threshold(mask,mask,35,255,THRESH_BINARY);

        //Mat normalizedMask=new Mat();
        Core.divide(mask,new Scalar(255),mask);

        backgroundSeg = getMaskImage(mask,background);

        //output=getAddImage(colorMask,backgroundSeg);
        Core.add(backgroundSeg,colorMask,output);

//        normalizedMask.release();
//        newMask.release();
//        newMask2.release();
//        mask.release();
//        colorMask.release();
//        backgroundSeg.release();

        return output;
    }

    public Mat getThresholdMask()
    {
        bgs.apply(input, fgMask);

        //Mat mask=new Mat();
        Imgproc.threshold(fgMask, mask, 35, 255, Imgproc.THRESH_BINARY_INV);

        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Imgproc.morphologyEx(mask, mask, Imgproc.MORPH_OPEN, kernel);

        Imgproc.cvtColor(mask, mask, Imgproc.COLOR_GRAY2BGR);

        return mask;
    }

    public Mat getColorMask()
    {
        //Mat mask = getThresholdMask();

        //Mat output=new Mat();
        Core.subtract(input,mask,colorMask);

        return colorMask;
    }

    public void setBackground(Mat background) {

        Imgproc.cvtColor(background,background,COLOR_BGRA2BGR);
        this.background = background;
    }

    public void setInput(Mat input) {

        Imgproc.cvtColor(input,input,COLOR_BGRA2BGR);

        this.input = input;
        resize(input.size(),background,background);
    }

    public void resize(Size size, Mat input, Mat output)
    {
        Imgproc.resize(input,output,size);
    }

    public Mat getBackground() {
        return background;
    }

    public Mat getMaskImage(Mat mask, Mat input)
    {
        split(input,bgr);

        Core.multiply(bgr.get(0),mask,b);
        Core.multiply(bgr.get(1),mask,g);
        Core.multiply(bgr.get(2),mask,r);

        //Mat output=new Mat();
        bgr.clear();
        bgr.add(b);
        bgr.add(g);
        bgr.add(r);

        merge(bgr,res);

        b.release();
        g.release();
        r.release();
        bgr.clear();

        System.gc();

        return res;
    }

    public Mat getAddImage(Mat A, Mat B)
    {
        List<Mat> bgr1=new ArrayList<>();
        List<Mat> bgr2=new ArrayList<>();

        split(A,bgr1);split(B,bgr2);

        Mat b=new Mat(),g=new Mat(),r=new Mat();

        Core.add(bgr1.get(0),bgr2.get(0),b);
        Core.add(bgr1.get(1),bgr2.get(1),g);
        Core.add(bgr1.get(2),bgr2.get(2),r);

        Mat output=new Mat();
        merge(Arrays.asList(new Mat[]{b,g,r}),output);

        b.release();
        g.release();
        r.release();

        return output;
    }

    public Mat getSubtractImage(Mat A, Mat B)
    {
        List<Mat> bgr1=new ArrayList<>();
        List<Mat> bgr2=new ArrayList<>();

        split(A,bgr1);split(B,bgr2);

        Mat b=new Mat(),g=new Mat(),r=new Mat();

        Core.subtract(bgr1.get(0),bgr2.get(0),b);
        Core.subtract(bgr1.get(1),bgr2.get(1),g);
        Core.subtract(bgr1.get(2),bgr2.get(2),r);

        Mat output=new Mat();
        merge(Arrays.asList(new Mat[]{b,g,r}),output);

        b.release();
        g.release();
        r.release();

        return output;
    }
}
