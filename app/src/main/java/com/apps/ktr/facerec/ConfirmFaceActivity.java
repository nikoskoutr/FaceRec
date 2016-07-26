package com.apps.ktr.facerec;

import android.app.ProgressDialog;
import android.content.ContentValues;
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
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
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
    private Context mContext;
    private Bitmap faceImg = null;
    private String mCurrentPhotoPath;
    private String mUserId;
    private boolean mIdSetFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
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

        setButtonListeners();
        setTextListeners();

    }

    // Gets the resulting face
    @Override
    public void processFinish(Bitmap output) {
        if (output != null){
            faceImg = output;

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
        Log.e(TAG, "Image return!");
    }

    // Finds and saves the face with all the needed supporting functions
    private void saveFace() throws Exception {
        // Get the root folder that the app will use
        File rootFolder = new File(Environment.getExternalStorageDirectory() + "/" + ROOT);
        if (!(rootFolder.exists())) {
            rootFolder.mkdir();
        }

        // Variables to be used for sending data to the database
        String userName = null;
        String userSurname = null;

        // Get user name from textbox
        EditText editText = (EditText) findViewById(R.id.userNameInput);
        if (editText != null) {
            userName = editText.getText().toString();
        }

        //Get user surname from textbox
        editText = (EditText) findViewById(R.id.userSurnameInput);
        if (editText != null) {
            userSurname = editText.getText().toString();
        }

        EditText userIdEditText = (EditText) findViewById(R.id.userIdInput);
        long userId = 0;
        if (userIdEditText != null && userIdEditText.getText().toString().matches("")) {
            // Prepare data to be inserted to the db.
            ContentValues values = new ContentValues();
            values.put(FaceRecContract.UserEntry.COLUMN_NAME_USERNAME, userName);
            values.put(FaceRecContract.UserEntry.COLUMN_NAME_USERSURNAME, userSurname);

            // Get the appropriate variables for db manipulation.
            FaceRecDbHelper mDbHelper = new FaceRecDbHelper(getApplicationContext());
            SQLiteDatabase db = mDbHelper.getWritableDatabase();

            // Insert data and get back the row/user id.
            userId = db.insertWithOnConflict(
                    FaceRecContract.UserEntry.TABLE_NAME,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE); // On conflict the id of the existing row will be returned.
            if (userId == -1) {
                String[] projection = {FaceRecContract.UserEntry._ID};
                String selection = FaceRecContract.UserEntry.COLUMN_NAME_USERNAME + " = ?" + " AND " +
                        FaceRecContract.UserEntry.COLUMN_NAME_USERSURNAME + "= ?";
                String[] selectionArgs = {userName, userSurname};
                Cursor c = db.query(
                        FaceRecContract.UserEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        null);
                c.moveToFirst();
                userId = c.getLong(c.getColumnIndex(FaceRecContract.UserEntry._ID));
                c.close();
            }

            db.close();
            mUserId = Long.toString(userId, 10);
        } else {
            if (userIdEditText != null) {
                mUserId = userIdEditText.getText().toString();
                userId = Long.parseLong(mUserId);
                FaceRecDbHelper mDbHelper = new FaceRecDbHelper(getApplicationContext());
                SQLiteDatabase db = mDbHelper.getReadableDatabase();
                String[] projection = {FaceRecContract.UserEntry.COLUMN_NAME_USERNAME, FaceRecContract.UserEntry.COLUMN_NAME_USERSURNAME};
                String selection = FaceRecContract.UserEntry._ID + " = ?";
                String[] selectionArgs = {mUserId};
                Cursor c = db.query(
                        FaceRecContract.UserEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        null);
                c.moveToFirst();
                if (c.getCount() != 0) {
                    db.close();
                    mIdSetFlag = true;
                } else {
                    throw new Exception();
                }
            }
        }

        // Create folder for storing user face images. The folder name is the user id.
        File userFolder = new File(rootFolder.getAbsolutePath() + "/" + userId);
        if (!(userFolder.exists())) {
            userFolder.mkdir();
        }

        // All images should have the same pixel dimensions in order for the algorithms to work.
        // The follGraphView) layout.findViewById(graphResId);owing method does that.
        normalizeImageSize();

        // Initialize the string that will contain the image location.
        String imageLocation = "";

        // Write the face image on the directory of the user with an auto-generated name.
        FileOutputStream out = null;
        try {
            File img = new File(mCurrentPhotoPath);
            String name = img.getName();
            imageLocation = userFolder.getAbsoluteFile() + "/" + name;
            out = new FileOutputStream(imageLocation);
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

        File csv = new File(rootFolder.getAbsolutePath() + "/images.csv");
        if (!(csv.exists())) {
            try {
                csv.createNewFile();
                BufferedWriter w = new BufferedWriter(new FileWriter(csv, false));
                w.write(imageLocation + ";" + userId);
                w.close();
            } catch (IOException e) {
                Log.e(TAG, "Error creating csv file");
            }
        } else {
            try {
                BufferedWriter w = new BufferedWriter(new FileWriter(csv, true));
                w.newLine();
                w.write(imageLocation + ";" + userId);
                w.close();
            } catch (IOException e) {
                Log.e(TAG, "Error creating csv." + e.getMessage());
            }
        }
    }

    // Saves every face image on the same size, so that facial recognition can work
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
                    faceImg = Bitmap.createScaledBitmap(faceImg, width, height, false);
                    break;
                }
            }
        }
    }

    // Set button listeners
    private void setButtonListeners() {
        Button btn = (Button) findViewById(R.id.confirmFaceButton);

        if (btn != null) {
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        saveFace();
                        if (mIdSetFlag) {
                            finish();
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                            String message = getResources().getString(R.string.dialogUserId) + " " + mUserId;
                            builder.setMessage(message).setTitle(R.string.dialogUserIdTitle);
                            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    finish();
                                }
                            });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                    } catch (Exception e) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        String message = getResources().getString(R.string.noIdFound);
                        builder.setMessage(message).setTitle(R.string.noIdFoundTitle);
                        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }

                }
            });
        }

        btn = (Button) findViewById(R.id.cancelFaceButton);
        if (btn != null) {
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
        }
    }

    // Set text change listeners for disabling input that should not be used together
    private void setTextListeners() {
        TextWatcher userInfoWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                EditText toChange = (EditText) findViewById(R.id.userIdInput);
                EditText toChangeName = (EditText) findViewById(R.id.userNameInput);
                EditText toChangeSurname = (EditText) findViewById(R.id.userSurnameInput);
                if (toChange != null && toChangeName != null && toChangeSurname != null) {
                    if (s.toString().length() > 0) {
                        toChange.setInputType(InputType.TYPE_NULL);
                        toChange.setCursorVisible(false);
                        toChange.setEnabled(false);
                    } else if (toChangeName.getText().toString().equals("") && toChangeSurname.getText().toString().equals("")){
                        toChange.setInputType(InputType.TYPE_CLASS_NUMBER);
                        toChange.setEnabled(true);
                        toChange.setCursorVisible(true);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
        EditText et = (EditText) findViewById(R.id.userNameInput);
        if (et != null) {
            et.addTextChangedListener(userInfoWatcher);
        }
        et = (EditText) findViewById(R.id.userSurnameInput);
        if (et != null) {
            et.addTextChangedListener(userInfoWatcher);
        }
        et = (EditText) findViewById(R.id.userIdInput);
        if (et != null) {
            et.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    EditText toChangeName = (EditText) findViewById(R.id.userNameInput);
                    EditText toChangeSurname = (EditText) findViewById(R.id.userSurnameInput);
                    if (toChangeName != null && toChangeSurname != null) {
                        if (s.toString().length() > 0) {
                            toChangeName.setInputType(InputType.TYPE_NULL);
                            toChangeName.setEnabled(false);
                            toChangeName.setCursorVisible(false);
                            toChangeSurname.setInputType(InputType.TYPE_NULL);
                            toChangeSurname.setEnabled(false);
                            toChangeSurname.setCursorVisible(false);
                        } else {
                            toChangeName.setInputType(InputType.TYPE_CLASS_TEXT);
                            toChangeName.setEnabled(true);
                            toChangeName.setCursorVisible(true);
                            toChangeSurname.setInputType(InputType.TYPE_CLASS_TEXT);
                            toChangeSurname.setEnabled(true);
                            toChangeSurname.setCursorVisible(true);
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }
    }
}
