package com.example.hilda.cameraapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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

    public Mat userMask = new Mat();
    Point p1, p2;

    private int method = Imgproc.GC_INIT_WITH_RECT;
    private Mat blendMask=new Mat();

    public GrabCutter() {

        initParameter();

    }

    public GrabCutter(Mat inputImage) {
        this.inputImage = inputImage.clone();
        initParameter();
    }

    private void initParameter() {
        int r = inputImage.rows();
        int c = inputImage.cols();

        p1 = new Point(0, 0);
        p2 = new Point(c - 1, r - 1);

        this.rect = new Rect(p1, p2);

        mask=new Mat();
        //mask.setTo(new Scalar(125));
        fgdModel.setTo(new Scalar(255, 255, 255));
        bgdModel.setTo(new Scalar(255, 255, 255));
    }

    public void setRect(Rect rect) {
        this.rect = rect.clone();
        p1 = rect.tl();
        p2 = rect.br();
    }

    public void setMethod(int method) {
        this.method = method;
    }

    public void setUserMask(Mat userMask) {
        this.userMask = userMask;
        //convertToOpencvValues();
    }

    public void setInputImage(Mat inputImage) {
        this.inputImage = inputImage.clone();
    }

    public Mat getMask() {
        //Rect rect = new Rect(50,30, 100,200);

        Mat imgC3 = new Mat();
        imgC3 = inputImage.clone();
        //Imgproc.cvtColor(inputImage, imgC3, Imgproc.COLOR_RGBA2RGB);
        Log.d(MainActivity.LOG_TAG, "imgC3: " + imgC3);

        Log.d(MainActivity.LOG_TAG, "Grabcut begins");
        Imgproc.grabCut(imgC3, mask, rect, bgdModel, fgdModel, 5, Imgproc.GC_INIT_WITH_RECT);

//        Core.convertScaleAbs(mask, mask, 100, 0);
//        Imgproc.cvtColor(mask, mask, Imgproc.COLOR_GRAY2RGBA);

        //convertToHumanValues();

//        mask=convertToOpencvValues(mask);
//        mask=convertToHumanValues(mask);
//        mask=convertToOpencvValues(mask);
        mask=convertToHumanValues(mask);
//
//        userMask=convertToOpencvValues(userMask);
//        userMask=convertToHumanValues(userMask);

        return mask;
    }

    public Mat getMaskWithUserInput()
    {
        //mask = getMask();
        //incorporateUserMask();

        mask=new Mat(userMask.size(),userMask.type(),new Scalar(2.0));
        userMask=convertToOpencvValues(userMask);
        mask=addMask(userMask,mask);
        //userMask=convertToOpencvValues(userMask);
        //mask=userMask.clone();
//
//        mask=convertToHumanValues(mask);
//        return mask;

        Mat imgC3 = inputImage.clone();
        //Imgproc.cvtColor(inputImage, imgC3, Imgproc.COLOR_RGBA2RGB);
        Log.d(MainActivity.LOG_TAG, "imgC3: " + imgC3);

        Log.d(MainActivity.LOG_TAG, "Grabcut begins");
        Imgproc.grabCut(imgC3, mask, rect, bgdModel, fgdModel, 5, Imgproc.GC_INIT_WITH_MASK);

        mask=convertToHumanValues(mask);
        mask=thresholdMask(mask,165.0,255.0);

        blendMask=mask.clone();

        return mask;
    }

    public Mat getBlendMask() {
        return blendMask;
    }

    public Mat thresholdMask(Mat mask, double threshold, double maxVal)
    {
        Imgproc.threshold(mask,mask,threshold,maxVal,Imgproc.THRESH_BINARY);
        return mask;
    }

    public void incorporateUserMask() {
        userMask = convertToOpencvValues(userMask);
        mask = convertToOpencvValues(mask);

        mask = addMask(userMask, mask);
    }

    public Mat addMask(Mat userMask, Mat mask) {
        double[] buffer1 = new double[1];
        byte[] buffer2 = new byte[1];
        byte[] buffer3 = new byte[1];

        //Log.d("sssss","ssssssssss!!!!!!!!!");

        Mat temp=new Mat();
        userMask.convertTo(temp,CvType.CV_64FC3);

        for (int x = 0; x < mask.rows(); x++) {
            for (int y = 0; y < mask.cols(); y++) {

                mask.get(x,y,buffer2);
                userMask.get(x, y, buffer3);
                int value = buffer3[0];
                //Log.d("sssss",value+"");

                if (value == Imgproc.GC_BGD) {
                    buffer2[0] = Imgproc.GC_BGD; // for sure background
                } else if(value == Imgproc.GC_FGD){
                    buffer2[0] = Imgproc.GC_FGD; // for sure foreground

                }
                mask.put(x, y, buffer2);
            }
        }

        return mask;
    }

    public Mat getMaskImage() {
        getMaskWithUserInput();

        mask=convertToOpencvValues(mask);
        mask=convertToMaskValues(mask);

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

    private Mat convertToMaskValues(Mat mask)
    {
        byte[] buffer = new byte[1];
        for (int x = 0; x < mask.rows(); x++) {
            for (int y = 0; y < mask.cols(); y++) {
                mask.get(x, y, buffer);
                int value = buffer[0];
                if (value == Imgproc.GC_BGD) {
                    buffer[0] = Imgproc.GC_PR_BGD;
                } else buffer[0] = Imgproc.GC_PR_FGD;
                mask.put(x, y, buffer);
            }
        }

        return mask;
    }

    private Mat convertToHumanValues(Mat mask) {
        byte[] buffer = new byte[3];
        for (int x = 0; x < mask.rows(); x++) {
            for (int y = 0; y < mask.cols(); y++) {
                mask.get(x, y, buffer);
                int value = buffer[0];
                if (value == Imgproc.GC_BGD) {
                    buffer[0] = (byte) 0; // for sure background
                } else if (value == Imgproc.GC_PR_BGD) {
                    buffer[0] = (byte) 85; // probably background
                } else if (value == Imgproc.GC_PR_FGD) {
                    buffer[0] = (byte) 170; // probably foreground
                } else {
                    buffer[0] = (byte) 255; // for sure foreground

                }
                mask.put(x, y, buffer);
            }
        }

        return mask;
    }

    /**
     * Converts level of grayscale into OpenCV values. White - foreground, Black
     * - background.
     */
    private Mat convertToOpencvValues(Mat mask) {

        Core.MinMaxLocResult minMaxLocResult= Core.minMaxLoc(mask);
        Log.d("SSS",minMaxLocResult.maxVal+","+minMaxLocResult.minVal);
//        Log.d("SSS",mask.rows()+","+mask.cols());

//        Mat tempMat=new Mat();
//        Imgproc.cvtColor(mask,tempMat,Imgproc.COLOR_GRAY2BGR);
//        Bitmap tempBitmap=mat2Bitmap(mask);
//
//        Log.d("SssSS",tempBitmap.getHeight()+","+tempBitmap.getWidth());
//
//        int R,G,B;
//        int color;

        Mat temp=new Mat();
        mask.convertTo(temp, CvType.CV_64FC3);

//        color=tempBitmap.getP;

        double[] buffer_ = new double[1];
        byte[] buffer = new byte[1];

        for (int x = 0; x < mask.rows(); x++) {
            for (int y = 0; y < mask.cols(); y++) {
                temp.get(x, y, buffer_);

                int value=(int)buffer_[0];

//                R = Color.red(color);     //bitwise shifting
//                G = Color.green(color);
//                B = Color.blue(color);
//
//                if(x>620)
//                Log.d("SsSS",x+","+y+":"+(int)temp);

                if (value >= 0 && value < 64) {
                    buffer[0] = Imgproc.GC_BGD; // for sure background
                } else if (value >= 64 && value < 128) {
                    buffer[0] = Imgproc.GC_PR_BGD; // probably background
                } else if (value >= 128 && value < 192) {
                    buffer[0] = Imgproc.GC_PR_FGD; // probably foreground
                } else {
                    buffer[0] = Imgproc.GC_FGD; // for sure foreground

                }
                mask.put(x, y, buffer);
            }
        }

        return mask;
    }

    private Bitmap mat2Bitmap(Mat mat)
    {
        Bitmap bitmap=Bitmap.createBitmap(mat.width(),mat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat,bitmap);
        return bitmap;
    }
}