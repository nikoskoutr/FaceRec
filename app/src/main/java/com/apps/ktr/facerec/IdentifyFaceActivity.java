package com.apps.ktr.facerec;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

//import org.opencv.android.BaseLoaderCallback;
//import org.opencv.android.LoaderCallbackInterface;
//import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class IdentifyFaceActivity extends AppCompatActivity implements DetectFaceHelperTask.AsyncResponse, RecognizeFaceHelperTask.AsyncResponseRec{
    private static final String TAG = "FACEREC";
    private static int CAMERA_REQUEST_CODE = 1;
    private String mCurrentPhotoPath;
    private Bitmap mFaceBitmap;
    private String mLabelPredicted;
    private Context mContext = this;
    private RecognizeFaceHelperTask.AsyncResponseRec mAsyncResponseRec = this;
    private static final String ROOT = "FaceRec";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identify_face);
        //OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallBack);

        //Get picture that will be tested against the database.
        Intent cameraIntent = new Intent(this, CameraActivity.class);
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);

        createButtonListeners(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        //Location of current photo
        mCurrentPhotoPath = data.getStringExtra(MainActivity.EXTRA_MESSAGE);

        //Progress dialog, while cropping face
        ProgressDialog progress = new ProgressDialog(mContext);
        progress.setTitle("Loading");
        progress.setMessage("Searching for face...");
        progress.show();

        //Start async task for detecting and cropping face
        new DetectFaceHelperTask((ImageView) findViewById(R.id.faceToDetect), progress, this).execute(mCurrentPhotoPath);
    }

    //Creates the button listeners
    private void createButtonListeners(Context c){
        final Context context = c;
        Button btn = (Button) findViewById(R.id.eigenFaceSearch);
        if (btn != null) {
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Search with eigenfaces
                    new RecognizeFaceHelperTask(Environment.getExternalStorageDirectory() + "/" + ROOT + "/test.png", RecognizeFaceHelperTask.EIGENFACES, mAsyncResponseRec).execute();
                }
            });
        }
    }

//    private BaseLoaderCallback mLoaderCallBack = new BaseLoaderCallback(this) {
//        //Callback function for opencv load
//        @Override
//        public void onManagerConnected(int status) {
//            switch (status) {
//                case LoaderCallbackInterface.SUCCESS:
//                {
//                    Log.i(TAG, "OpenCV loaded successfully");
//                } break;
//                default:
//                {
//                    super.onManagerConnected(status);
//                } break;
//            }
//        }
//    };

    //Implements face detection function, in order to get bitmap back from the task
    @Override
    public void processFinish(Bitmap output) {
        mFaceBitmap = output;
        ImageView iV = (ImageView) findViewById(R.id.faceToDetect);
        if (iV != null) {
            iV.setImageBitmap(output);
        }
        saveBitmap();
    }

    //Implements face recognition function, in order to get label back from the task
    @Override
    public void processFinishRec(String output) {
        mLabelPredicted = output;

    }

    private void saveBitmap() {
        File test = new File(Environment.getExternalStorageDirectory() + "/" + ROOT + "/test.png");
        if(!test.exists()) {
            try {
                test.createNewFile();
            } catch (IOException e) {
                Log.e(TAG, "Error creating new test file: " + e.getMessage());
            }
        }

        FileOutputStream out = null;
        try {
            String name = test.getName();
            out = new FileOutputStream(test.getAbsolutePath());
            mFaceBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

        } catch (IOException e) {
            Log.e(TAG, "Error compressing bmp");
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error closing output stream: " + e.toString());
            }
        }


    }
}
