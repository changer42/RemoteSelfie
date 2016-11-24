package com.example.hilda.cameraapp;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import static org.opencv.imgproc.Imgproc.COLOR_BGRA2BGR;
import static org.opencv.photo.Photo.NORMAL_CLONE;
import static org.opencv.photo.Photo.seamlessClone;

/**
 * Created by Johnny on 11/23/2016.
 */

public class ImageBlender {

    Mat src,dst;

    public ImageBlender(Mat src, Mat dst)
    {
        Imgproc.cvtColor(src,src,COLOR_BGRA2BGR);
        Imgproc.cvtColor(dst,dst,COLOR_BGRA2BGR);

        this.src=src;
        this.dst=dst;
    }

    public Mat blend()
    {
        Mat src_mask= Mat.ones(src.rows(), src.cols(), src.depth());
        Core.multiply(src_mask,new Scalar(255),src_mask);

// The location of the center of the src in the dst
        Point center=new Point(dst.cols()/2,dst.rows()/2);

// Seamlessly clone src into dst and put the results in output
        Mat normal_clone=new Mat();
        Mat mixed_clone=new Mat();

        seamlessClone(src, dst, src_mask, center, normal_clone, NORMAL_CLONE);
        //seamlessClone(src, dst, src_mask, center, mixed_clone, MIXED_CLONE);

// Save results

        return normal_clone;
    }
}
