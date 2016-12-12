package com.example.hilda.cameraapp;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import static org.opencv.core.Core.findNonZero;
import static org.opencv.imgproc.Imgproc.COLOR_BGRA2BGR;
import static org.opencv.photo.Photo.MIXED_CLONE;
import static org.opencv.photo.Photo.NORMAL_CLONE;
import static org.opencv.photo.Photo.seamlessClone;

/**
 * Created by Johnny on 11/23/2016.
 */

public class ImageBlender {

    Mat src,dst;
    Mat src_mask=new Mat();

    public ImageBlender()
    {

    }


    public void setImage(Mat src,Mat dst)
    {
        this.src=src;
        this.dst=dst;
    }

    public ImageBlender(Mat src, Mat dst)
    {
//        Imgproc.cvtColor(src,src,COLOR_BGRA2BGR);
//        Imgproc.cvtColor(dst,dst,COLOR_BGRA2BGR);
        this.src=src;
        this.dst=dst;
    }

    public void initMask()
    {
        src_mask= Mat.ones(src.rows(), src.cols(), src.depth());
        Core.multiply(src_mask,new Scalar(255),src_mask);
    }

    public void setMask(Mat mask)
    {
        this.src_mask=mask.clone();
    }

    public Mat getMorphImage(Mat mask)
    {
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(4, 4));
        Imgproc.morphologyEx(mask, mask, Imgproc.MORPH_OPEN, kernel);
        return mask;
    }

    public Mat blend()
    {
        //src_mask=getMorphImage(src_mask);
// The location of the center of the src in the dst
        //Point center=getCenterOfRect(getBoundingRect(src_mask));
        Point center=new Point(src.cols()/2,src.rows()/2);
        //Point center=getCenterOfMask(src_mask);
// Seamlessly clone src into dst and put the results in output
        Mat normal_clone=new Mat();
        //Mat mixed_clone=new Mat();

        seamlessClone(src, dst, src_mask, center, normal_clone, NORMAL_CLONE);
        //seamlessClone(src, dst, src_mask, center, mixed_clone, MIXED_CLONE);

// Save results

        return normal_clone;
    }

    public Mat simpleBlend()
    {
        src.copyTo(dst,src_mask);
        return dst;
    }

    public Point getCenterOfMask(Mat mask)
    {
        Moments m=Imgproc.moments(mask,false);
        return new Point(m.m10/m.m00, m.m01/m.m00);
    }

    public Rect getBoundingRect(Mat mask)
    {
        Mat Points=new Mat();
        findNonZero(mask,Points);
        MatOfPoint matOfPoint = new MatOfPoint(Points);
        return Imgproc.boundingRect(matOfPoint);
    }

    public Point getCenterOfRect(Rect rect)
    {
        return new Point((rect.x+rect.width)/2,(rect.y+rect.height)/2);
    }

    public Mat alphaBlend()
    {
        Point center=new Point(src.cols()/2,src.rows()/2);

    }
}
