package com.apps.ktr.facerec;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    public final static String EXTRA_MESSAGE = "com.apps.ktr.facerec.MESSAGE";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String TAG = "FACEREC";
    private String mCurrentPhotoPath;
    private static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "test: " + Environment.DIRECTORY_PICTURES);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        Button btn = (Button) findViewById(R.id.cameraButton);
        if (btn != null) {
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent takePictureIntent = new Intent(mContext, CameraActivity.class);
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        mCurrentPhotoPath = data.getStringExtra(MainActivity.EXTRA_MESSAGE);
        galleryAddPic();
        Intent confirmFaceIntent = new Intent(this, ConfirmFaceActivity.class);
        confirmFaceIntent.putExtra(EXTRA_MESSAGE, mCurrentPhotoPath);
        startActivity(confirmFaceIntent);
    }


    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = null;
        while (f ==null) {
            f = new File(mCurrentPhotoPath);
            try {
                Thread.sleep(1000, 0);
            } catch (InterruptedException e) {
                Log.e(TAG, "Sleep interrupted at media scanner sleep" + e.toString());
            }
        }
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

}

