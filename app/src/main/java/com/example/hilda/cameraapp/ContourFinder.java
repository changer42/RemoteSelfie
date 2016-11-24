package com.example.hilda.cameraapp;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Johnny on 11/23/2016.
 */

public class ContourFinder {

    private Mat input;
    private Mat output;

    public ContourFinder(Mat input)
    {
        this.input=input.clone();
        output=input.clone();
    }

    public Mat getLargestContourImage()
    {
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(input, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        double maxArea = -1;
        int maxAreaIdx = -1;
        for (int idx = 0; idx < contours.size(); idx++) {
            Mat contour = contours.get(idx);
            double contourarea = Imgproc.contourArea(contour);
            if (contourarea > maxArea) {
                maxArea = contourarea;
                maxAreaIdx = idx;
            }
        }

        if(contours.size()>0)
            Imgproc.drawContours(output,contours,maxAreaIdx,new Scalar(1.0,1.0,1.0));

        return output;

    }
}
