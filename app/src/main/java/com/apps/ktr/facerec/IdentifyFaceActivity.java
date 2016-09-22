package com.apps.ktr.facerec;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class IdentifyFaceActivity extends AppCompatActivity implements DetectFaceHelperTask.AsyncResponse, RecognizeFaceHelperTask.AsyncResponseRec{
    private boolean useTrainedData = false;
    private static final String TAG = "FACEREC";
    private Bitmap mFaceBitmap;
    private int algorithm = 0;
    private final Context mContext = this;
    private final RecognizeFaceHelperTask.AsyncResponseRec mAsyncResponseRec = this;
    private static final String ROOT = "FaceRec";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identify_face);

        //Get picture that will be tested against the database.
        Intent cameraIntent = new Intent(this, CameraActivity.class);
        int CAMERA_REQUEST_CODE = 1;
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);

        createButtonListeners();
        checkFiles();
    }

    private void checkFiles() {
        File storage = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/FaceRec/");
        File eigen = new File(storage + "/eigenfaces.yml");
        Log.e(TAG, "-------------------" + eigen.getAbsolutePath());
        File fisher = new File(storage + "/fisherfaces.yml");
        File lbph = new File(storage + "/LBPH.yml");
        if(eigen.exists() && fisher.exists() && lbph.exists()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            String message = getResources().getString(R.string.traineFileFound);
            builder.setMessage(message).setTitle(R.string.traineFileFoundTitle);
            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    useTrainedData = true;
                }
            });
            builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        //Location of current photo
        String mCurrentPhotoPath = data.getStringExtra(MainActivity.EXTRA_MESSAGE);

        //Progress dialog, while cropping face
        ProgressDialog progress = new ProgressDialog(mContext);
        progress.setTitle("Loading");
        progress.setMessage("Searching for face...");
        progress.show();

        //Start async task for detecting and cropping face
        new DetectFaceHelperTask((ImageView) findViewById(R.id.faceToDetect), progress, this).execute(mCurrentPhotoPath);
    }

    //Creates the button listeners
    private void createButtonListeners(){
        Button btn = (Button) findViewById(R.id.eigenFaceSearch);
        if (btn != null) {
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Search with eigenfaces
                    algorithm = 1;
                    new RecognizeFaceHelperTask(algorithm, mAsyncResponseRec, useTrainedData).execute();
                }
            });
        }

        btn = (Button) findViewById(R.id.fisherFaceSearch);
        if (btn != null) {
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Search with fisherfaces
                    algorithm = 2;
                    new RecognizeFaceHelperTask(algorithm, mAsyncResponseRec, useTrainedData).execute();
                }
            });
        }

        btn = (Button) findViewById(R.id.LBPHFaceSearch);
        if (btn != null) {
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Search with LBPH
                    algorithm = 3;
                    new RecognizeFaceHelperTask(algorithm, mAsyncResponseRec, useTrainedData).execute();
                }
            });
        }
    }

    //Implements face detection function, in order to get bitmap back from the task
    @Override
    public void processFinish(Bitmap output) {
        if (output != null) {
            mFaceBitmap = output;
            ImageView iV = (ImageView) findViewById(R.id.faceToDetect);
            if (iV != null) {
                iV.setImageBitmap(output);
            }
            saveBitmap();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            String message = getResources().getString(R.string.faceNotFound);
            builder.setMessage(message).setTitle(R.string.faceNotFoundTitle);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    finish();
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    //Implements face recognition function, in order to get label back from the task
    @Override
    public void processFinishRec(String output) {
        Intent recognitionResultIntent = new Intent(getApplicationContext(), RecognitionResultActivity.class);
        recognitionResultIntent.putExtra("UserId", output);
        startActivity(recognitionResultIntent);
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
            normalizeImageSize();
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

    private void normalizeImageSize() {
        File dir = new File(Environment.getExternalStorageDirectory() + "/" + ROOT + "/");
        File[] contents = dir.listFiles();
        if(contents.length > 0) {
            for(File f : contents) {
                if(f.isDirectory() && f.list().length>0) {
                    File template = f.listFiles()[0];
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(template.getAbsolutePath(), options);
                    int width = options.outWidth;
                    int height = options.outHeight;
                    mFaceBitmap = Bitmap.createScaledBitmap(mFaceBitmap, width, height, false);
                    break;
                }
            }
        }
    }
}
