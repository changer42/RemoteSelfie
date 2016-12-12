package com.example.hilda.cameraapp;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

/**
 * Created by Johnny on 11/29/2016.
 */

public class DrawableView extends View {

    private Mat inputImage;
    private boolean foreground;

    public int width;
    public int height;
    private Bitmap fgBitmap,bgBitmap, blankBgBitmap, blankFgBitmap, curBitmap,curBlankBitmap;
    private Canvas fgCanvas,bgCanvas, blankBgCanvas, blankFgCanvas,curCanvas, curBlankCanvas;
    private Path mPath;
    private Paint mBitmapPaint;
    private Context context;
    private Paint circlePaint;
    private Path circlePath;

    private Paint mPaint;

    public DrawableView(Context context) {
        super(context);
        mPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        circlePaint = new Paint();
        circlePath = new Path();
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(Color.BLUE);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeJoin(Paint.Join.MITER);
        circlePaint.setStrokeWidth(4f);

        initmPaint();
    }

    public Bitmap getCurBlankBitmap() {
        return curBlankBitmap;
    }

    public void initForeground()
    {
        curCanvas=fgCanvas;
        curBlankCanvas=blankFgCanvas;
        curBitmap=fgBitmap;
        curBlankBitmap=blankFgBitmap;
    }

    public void setForeground(boolean foreground) {
        this.foreground = foreground;

        if(foreground)
        {
//            curCanvas=fgCanvas;
//            curBlankCanvas=blankFgCanvas;
//            curBitmap=fgBitmap;
//            curBlankBitmap=blankFgBitmap;
            mPaint.setColor(Color.WHITE);
        }
        else
        {
//            curCanvas=bgCanvas;
//            curBlankCanvas=blankBgCanvas;
//            curBitmap=bgBitmap;
//            curBlankBitmap=blankBgBitmap;
            mPaint.setColor(Color.BLACK);
        }
    }

    public boolean isForeground() {
        return this.foreground;
    }

    public void setInputImage(Mat inputImage) {
        this.inputImage = inputImage;
    }

    public void initmPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(12);
    }

    public Bitmap mat2Bitmap(Mat mat)
    {
        Bitmap bitmap= Bitmap.createBitmap(mat.width(),mat.height(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat,bitmap);

        return bitmap;
    }



    public Bitmap getBlankBgBitmap() {
        return blankBgBitmap;
    }
    public Bitmap getBlankFgBitmap() {
        return blankFgBitmap;
    }

    public DrawableView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap(curBitmap, 0, 0, mBitmapPaint);

        canvas.drawPath(mPath, mPaint);
        canvas.drawPath(circlePath, circlePaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        fgBitmap = initBitmap(inputImage,w,h);
        bgBitmap = initBitmap(inputImage,w,h);

        blankFgBitmap = initBlankBitmap(w,h);
        blankBgBitmap = initBlankBitmap(w,h);

        fgCanvas = initCanvas(fgBitmap,w,h);
        bgCanvas = initCanvas(bgBitmap,w,h);

        blankFgCanvas = initBlankCanvas(blankFgBitmap,w,h);
        blankBgCanvas = initBlankCanvas(blankBgBitmap,w,h);

        initForeground();
    }

    private Bitmap initBitmap(Mat inputImage, int w, int h) {

        Bitmap bitmap = mat2Bitmap(inputImage);
        return Bitmap.createScaledBitmap(bitmap, w,h,true);
    }


    private Bitmap initBlankBitmap(int w, int h) {
        return Bitmap.createBitmap(w,h, Bitmap.Config.ARGB_8888);

    }

    private Canvas initCanvas(Bitmap bitmap,int w,int h)
    {
        //fgBitmap = BitmapFactory.decodeResource(getResources(), );
        //fgBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        return new Canvas(bitmap);
    }

    private Canvas initBlankCanvas(Bitmap bitmap, int w,int h)
    {

        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.GRAY);
        return canvas;
    }

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    private void touch_start(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;

            circlePath.reset();
            circlePath.addCircle(mX, mY, 30, Path.Direction.CW);
        }
    }

    private void touch_up() {
        mPath.lineTo(mX, mY);
        circlePath.reset();
        // commit the path to our offscreen

        curCanvas.drawPath(mPath, mPaint);

        curBlankCanvas.drawPath(mPath,mPaint);
        // kill this so we don't double draw
        mPath.reset();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //super.onTouchEvent(event);

        float x = event.getX();
        float y = event.getY();

        Log.d(MainActivity.LOG_TAG,x+","+y);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
        }
        return true;
    }
}
