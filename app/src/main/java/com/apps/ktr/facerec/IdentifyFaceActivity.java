package com.apps.ktr.facerec;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;


public class IdentifyFaceActivity extends AppCompatActivity {
    private static final String TAG = "FACEREC";
    private static int CAMERA_REQUEST_CODE = 1;
    private String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identify_face);
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallBack);

        Intent cameraIntent = new Intent(this, CameraActivity.class);
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);

        createButtonListeners(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        mCurrentPhotoPath = data.getStringExtra(MainActivity.EXTRA_MESSAGE);
    }

    private void createButtonListeners(Context c){
        final Context context = c;
        Button btn = (Button) findViewById(R.id.eigenFaceSearch);
        if (btn != null) {
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new RecognizeFaceHelperTask(mCurrentPhotoPath, RecognizeFaceHelperTask.EIGENFACES, context, (ImageView) findViewById(R.id.faceToDetect)).execute();
                }
            });
        }
    }

    private BaseLoaderCallback mLoaderCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
}
