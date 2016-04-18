package com.apps.ktr.facerec;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ConfirmFaceActivity extends AppCompatActivity implements DetectFaceHelperTask.AsyncResponse {

    private static final String TAG = "FACEREC";
    private static final String ROOT = "FaceRec";
    private Bitmap faceImg = null;
    private String mCurrentPhotoPath;

    @Override
    public void processFinish(Bitmap output){
        faceImg = output;
        Log.e(TAG, "Image return!");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_face);
        ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Searching for face...");
        progress.show();

        ImageView imgV = (ImageView) findViewById(R.id.faceFoundView);
        if (imgV == null) {
            Log.e(TAG, "View not found.");
        }
        Intent intent = getIntent();
        mCurrentPhotoPath = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        new DetectFaceHelperTask(imgV, progress, this).execute(mCurrentPhotoPath);

        Button btn = (Button) findViewById(R.id.confirmFaceButton);
        if (btn != null) {
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    saveFace();
                    finish();
                }
            });
        }

        Log.e(TAG, "Error:" + Environment.getExternalStorageDirectory() + "/" + ROOT);
    }

    private void saveFace() {
        Log.e(TAG, Environment.getExternalStorageDirectory() + "/" + ROOT);
        File rootFolder = new File(Environment.getExternalStorageDirectory() + "/" + ROOT);
        if (!(rootFolder.exists())){
            rootFolder.mkdir();
        }
        EditText editText = (EditText) findViewById(R.id.userIdInput);
        String userId = null;
        if (editText != null) {
            userId = editText.getText().toString();
        }
        File userFolder = new File(rootFolder.getAbsolutePath() + "/" + userId);
        if (!(userFolder.exists())) {
            userFolder.mkdir();
        }

        FileOutputStream out = null;
        try {
            File img = new File(mCurrentPhotoPath);
            String name = img.getName();
            out = new FileOutputStream(userFolder.getAbsoluteFile() + "/" + name);
            faceImg.compress(Bitmap.CompressFormat.PNG, 100, out);

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
