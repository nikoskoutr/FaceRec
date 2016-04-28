package com.apps.ktr.facerec;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

public class ConfirmFaceActivity extends AppCompatActivity implements DetectFaceHelperTask.AsyncResponse {

    private static final String TAG = "FACEREC";
    private static final String ROOT = "FaceRec";
    private Bitmap faceImg = null;
    private String mCurrentPhotoPath;

    @Override
    public void processFinish(Bitmap output) {
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

        // Clearing Preferences, for development only.
        btn = (Button) findViewById(R.id.releasePrefs);
        if (btn != null) {
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPreferences p = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                    SharedPreferences.Editor e = p.edit();
                    e.clear();
                    e.apply();
                }
            });
        }
    }

    private void saveFace() {
        File rootFolder = new File(Environment.getExternalStorageDirectory() + "/" + ROOT);
        if (!(rootFolder.exists())) {
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

        SharedPreferences prefs = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        int prefsCount = prefs.getAll().size();
        if (!(prefs.contains(userId))) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(userId, prefsCount);
            editor.apply();
        }

        File csv = new File(rootFolder.getAbsolutePath() + "/images.csv");
        if (!(csv.exists())) {
            try {
                boolean b = csv.createNewFile();
                Log.e(TAG, "" + b);
                BufferedWriter w = new BufferedWriter(new FileWriter(csv, false));
                w.write(mCurrentPhotoPath + ";" + prefs.getInt(userId, 0));
                w.close();
            } catch (IOException e) {
                Log.e(TAG, "Error creating csv file");
            }
        } else {
            try {
                BufferedWriter w = new BufferedWriter(new FileWriter(csv, true));
                w.newLine();
                w.write(mCurrentPhotoPath + ";" + prefs.getInt(userId, 0));
                w.close();
            } catch (IOException e) {
                Log.e(TAG, "Error creating csv." + e.getMessage());
            }
        }
    }
}
