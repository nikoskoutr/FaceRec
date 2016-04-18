package com.apps.ktr.facerec;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class ConfirmFaceActivity extends AppCompatActivity {

    private static final String TAG = "FACEREC";
    private String mCurrentPhotoPath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_face);
        ProgressDialog progress = new ProgressDialog(this);
//        progress.setTitle("Loading");
//        progress.setMessage("Searching for face...");
//        progress.show();

        ImageView imgV = (ImageView) findViewById(R.id.faceFoundView);
        if (imgV == null) {
            Log.e(TAG, "View not found.");
        }
        Intent intent = getIntent();
        mCurrentPhotoPath = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        new DetectFaceHelperTask(imgV, progress);

//        Button btn = (Button) findViewById(R.id.confirmFaceButton);
//        if (btn != null) {
//            btn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//
//                }
//            });
//        }
    }
}
