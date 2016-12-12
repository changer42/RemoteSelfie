package com.example.hilda.cameraapp;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements CvCameraViewListener2, OnTouchListener {

    private static final String TAG = "MainActivity";
    public static final String LOG_TAG = "MainActivity";

    static {
        System.loadLibrary("opencv_java3");
    }
    private Activity mActivity = this;
    private Tutorial3View mOpenCvCameraView;
    private List<Size> mResolutionList;
    private MenuItem[] mEffectMenuItems;
    private SubMenu mColorEffectsMenu;
    private MenuItem[] mResolutionMenuItems;
    private SubMenu mResolutionMenu;
    private Button mCaptureButton;

    private BgdSubtractor bgdSubtractor;

    public static Mat capturedOutFrame =new Mat();
    public static Mat capturedInFrame=new Mat();
    public static Mat capturedBackground=new Mat();


    private Mat inFrame =new Mat();
    private Mat outFrame=new Mat();

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    private int frameCount = 0;

    public MainActivity(){
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        mOpenCvCameraView = (Tutorial3View) findViewById(R.id.tutorial3_activity_java_surface_view);
        mOpenCvCameraView.setCameraIndex(1);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mCaptureButton = (Button) findViewById(R.id.captureButton);
        mCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG,"onTouch event");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                String currentDateandTime = sdf.format(new Date());
                String fileName = Environment.getExternalStorageDirectory().getPath() +
                        "/sample_picture_" + currentDateandTime + ".jpg";
                mOpenCvCameraView.takePicture(fileName);
                Toast.makeText(mActivity, fileName + " saved", Toast.LENGTH_SHORT).show();
//                return false;

                capturedOutFrame =getRotatedMat(outFrame.clone(),270);
                capturedInFrame = getRotatedMat(inFrame.clone(),270);
                capturedBackground = getRotatedMat(bgdSubtractor.getBackground().clone(),270);

                direct2DisplayActivity();
            }
        });

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);

        initBgd(R.drawable.background);
    }

    private void initBgd(int imgId)
    {
        bgdSubtractor=new BgdSubtractor("Morph");
        //bgdSubtractor.initMoG2(500,16,true);
        bgdSubtractor.setBackground(imgId2Mat(imgId));
    }

    private void initBgd(int imgId,int degree)
    {
        bgdSubtractor=new BgdSubtractor("Morph");
        //bgdSubtractor.initMoG2(500,16,true);

        Mat temp=imgId2Mat(imgId);
        bgdSubtractor.setBackground(getRotatedMat(temp,degree));
    }


    public void direct2DisplayActivity()
    {
        // In parent activity

        //Mat inFrameCopy=inFrame.clone();

        Intent i = new Intent();
        //Bundle b = new Bundle();
        //b.putParcelable("myImg", (Parcelable) inFrameCopy);
        //i.putExtras(b);
        i.setClass(this, ScribbleActivity.class);
        startActivity(i);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public Mat getRotatedMat(Mat src, int mCameraOrientation)
    {
        Mat dst=new Mat();
        if (mCameraOrientation == 270) {
            // Rotate clockwise 270 degrees
            Core.flip(src.t(), dst, 0);
        } else if (mCameraOrientation == 180) {
            // Rotate clockwise 180 degrees
            Core.flip(src, dst, -1);
        } else if (mCameraOrientation == 90) {
            // Rotate clockwise 90 degrees
            Core.flip(src.t(), dst, 1);
        } else if (mCameraOrientation == 0) {
            // No rotation
            dst = src;
        }
        return dst;
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        //Rotation from horizontal to vertical

        //checkFrameCount();

        if(bgdSubtractor.getBackground().empty() ||
                inputFrame.rgba().empty()) return inputFrame.rgba();


        Core.flip(inputFrame.rgba(), inFrame,0);

        bgdSubtractor.setInput(inFrame);
        outFrame = bgdSubtractor.getBlendImage();

        //Mat outFrame = bgdSubtractor.getBackground();
        System.gc();

        return outFrame;

    }

    public void savePicture(Mat outputFrame){
        Log.i(TAG,"onClick event");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDateandTime = sdf.format(new Date());
        String fileName = Environment.getExternalStorageDirectory().getPath() +
                "/remote_selfies_" + currentDateandTime + ".jpg";
        mOpenCvCameraView.takePicture(fileName);


        Bitmap bmp = null;
        try {
            bmp = Bitmap.createBitmap(outputFrame.cols(), outputFrame.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(outputFrame, bmp);
        } catch (CvException e) {
            Log.d(TAG, e.getMessage());
        }

        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();

        } catch (java.io.IOException e) {
            Log.e("SavePicture", "Exception in photoCallback", e);
        }

        Toast.makeText(mActivity, fileName + " saved", Toast.LENGTH_SHORT).show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        List<String> effects = mOpenCvCameraView.getEffectList();

        if (effects == null) {
            Log.e(TAG, "Color effects are not supported by device!");
            return true;
        }

        mColorEffectsMenu = menu.addSubMenu("Color Effect");
        mEffectMenuItems = new MenuItem[effects.size()];

        int idx = 0;
        ListIterator<String> effectItr = effects.listIterator();
        while(effectItr.hasNext()) {
            String element = effectItr.next();
            mEffectMenuItems[idx] = mColorEffectsMenu.add(1, idx, Menu.NONE, element);
            idx++;
        }

        mResolutionMenu = menu.addSubMenu("Resolution");
        mResolutionList = mOpenCvCameraView.getResolutionList();
        mResolutionMenuItems = new MenuItem[mResolutionList.size()];

        ListIterator<Size> resolutionItr = mResolutionList.listIterator();
        idx = 0;
        while(resolutionItr.hasNext()) {
            Size element = resolutionItr.next();
            mResolutionMenuItems[idx] = mResolutionMenu.add(2, idx, Menu.NONE,
                    Integer.valueOf(element.width).toString() + "x" + Integer.valueOf(element.height).toString());
            idx++;
        }

        getMenuInflater().inflate(R.menu.activity_main,menu);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item.getGroupId() == 1)
        {
            mOpenCvCameraView.setEffect((String) item.getTitle());
            Toast.makeText(this, mOpenCvCameraView.getEffect(), Toast.LENGTH_SHORT).show();
        }
        else if (item.getGroupId() == 2)
        {
            int id = item.getItemId();
            Size resolution = mResolutionList.get(id);
            mOpenCvCameraView.setResolution(resolution);
            resolution = mOpenCvCameraView.getResolution();
            String caption = Integer.valueOf(resolution.width).toString() + "x" + Integer.valueOf(resolution.height).toString();
            Toast.makeText(this, caption, Toast.LENGTH_SHORT).show();
        }

        int id=item.getItemId();
        switch (id)
        {
            case R.id.menu_action_effiel:
                initBgd(R.drawable.effiel,90);
                break;
            case R.id.menu_action_pyramid:
                initBgd(R.drawable.pyramid);
                break;
            case R.id.menu_action_liberty:
                initBgd(R.drawable.liberty,90);
                break;
            case R.id.menu_action_hogwartz:
                initBgd(R.drawable.hogwartz2);
                break;
            case R.id.menu_action_empire:
                initBgd(R.drawable.empire);
                break;
            case R.id.menu_action_hp:
                initBgd(R.drawable.hp);
                break;
            case R.id.menu_action_london:
                initBgd(R.drawable.london,90);
                break;
            default:
                break;
        }

        return true;
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.i(TAG,"onTouch event");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDateandTime = sdf.format(new Date());
        String fileName = Environment.getExternalStorageDirectory().getPath() +
                "/sample_picture_" + currentDateandTime + ".jpg";
        mOpenCvCameraView.takePicture(fileName);
        Toast.makeText(this, fileName + " saved", Toast.LENGTH_SHORT).show();
        return false;
    }


    public Mat imgId2Mat(int imgId)
    {

        Bitmap bmpIn= BitmapFactory.decodeResource(getResources(), imgId);
        Mat mat=new Mat();
        Utils.bitmapToMat(bmpIn, mat);

        return mat;
    }

    public void checkFrameCount()
    {
        if(frameCount == 300) {
            frameCount =0;
            bgdSubtractor= new BgdSubtractor();
        }
        else
            frameCount++;

    }
}

//public class MainActivity extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//    }
//}
