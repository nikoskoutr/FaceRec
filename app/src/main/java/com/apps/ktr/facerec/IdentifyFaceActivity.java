package com.apps.ktr.facerec;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class IdentifyFaceActivity extends AppCompatActivity implements DetectFaceHelperTask.AsyncResponse, RecognizeFaceHelperTask.AsyncResponseRec{
    private static final String TAG = "FACEREC";
    private static int CAMERA_REQUEST_CODE = 1;
    private String mCurrentPhotoPath;
    private Bitmap mFaceBitmap;
    private String mLabelPredicted;
    int algorithm = 0;
    private Context mContext = this;
    private RecognizeFaceHelperTask.AsyncResponseRec mAsyncResponseRec = this;
    private static final String ROOT = "FaceRec";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identify_face);

        //Get picture that will be tested against the database.
        Intent cameraIntent = new Intent(this, CameraActivity.class);
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);

        createButtonListeners();
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
    private void createButtonListeners(){
        Button btn = (Button) findViewById(R.id.eigenFaceSearch);
        if (btn != null) {
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Search with eigenfaces
                    algorithm = 1;
                    new RecognizeFaceHelperTask(algorithm, mAsyncResponseRec).execute();
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
                    new RecognizeFaceHelperTask(algorithm, mAsyncResponseRec).execute();
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
                    new RecognizeFaceHelperTask(algorithm, mAsyncResponseRec).execute();
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
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    //Implements face recognition function, in order to get label back from the task
    @Override
    public void processFinishRec(String output) {
        mLabelPredicted = output;
        FaceRecDbHelper mDbHelper = new FaceRecDbHelper(getApplicationContext());
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] projection = {FaceRecContract.UserEntry.COLUMN_NAME_USERNAME, FaceRecContract.UserEntry.COLUMN_NAME_USERSURNAME};
        String selection = FaceRecContract.UserEntry.COLUMN_NAME_USERID + " = ?";
        String[] selectionArgs = {mLabelPredicted};
        Cursor c = db.query(
                FaceRecContract.UserEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null);
        c.moveToFirst();
        String message = c.getString(c.getColumnIndex(FaceRecContract.UserEntry.COLUMN_NAME_USERNAME));
        message += " " + c.getString(c.getColumnIndex(FaceRecContract.UserEntry.COLUMN_NAME_USERSURNAME));
        c.close();
        db.close();
        Context context = getApplicationContext();
        CharSequence text = message;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
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

    private void normalizeImageSize() {
        File dir = new File(Environment.getExternalStorageDirectory() + "/" + ROOT + "/");
        File[] contents = dir.listFiles();
        if(!(contents.length == 0)) {
            File[] images = contents[0].listFiles();
            if(!(images == null)) {
                if(!(images.length == 0)) {
                    File template = images[0];
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(template.getAbsolutePath(), options);
                    int width = options.outWidth;
                    int height = options.outHeight;
                    mFaceBitmap = Bitmap.createScaledBitmap(mFaceBitmap, width, height, false);
                }
            }
        }
    }
}
