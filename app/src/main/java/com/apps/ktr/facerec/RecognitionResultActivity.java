package com.apps.ktr.facerec;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ButtonBarLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class RecognitionResultActivity extends AppCompatActivity {
    private final String TAG = "FACEREC";
    private String algorithm, name, surname, mLabelPredicted, actualUser;
    private long trainTime, predictTime;
    private int componentsUsed;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognition_result);
        mContext = this;
        Bundle extras = getIntent().getExtras();
        mLabelPredicted = extras.getString("UserId");
        if (mLabelPredicted.equals("0") && mLabelPredicted != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            String message = getResources().getString(R.string.notRecognized);
            builder.setMessage(message).setTitle(R.string.notRecognizedTitle);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    findUser();
                    finish();
                }
            });
        } else {

            try {
                readFile();
            } catch (IOException e) {
                Log.e(TAG, "Error finding or getting line from times file: " + e.getMessage());
            }

            if(algorithm.equals("LBPH")) {
                TextView tV = (TextView) findViewById(R.id.removable);
                if (tV != null) {
                    tV.setVisibility(View.GONE);
                }
            }
            getDataFromDB();
            setBitmaps();
            appendData();
            setButtonListeners();

        }
    }

    private void readFile() throws IOException {
        File timesFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/FaceRec/times.txt");
        BufferedReader reader = new BufferedReader(new FileReader(timesFile));
        String line = reader.readLine();
        while(line != null) {
            String attribute;
            String value;
            attribute = line.substring(0, line.indexOf("="));
            value = line.substring(line.indexOf("=")+1);
            switch (attribute) {
                case "algorithm":
                    algorithm = value;
                    break;
                case "train":
                    trainTime = Long.parseLong(value);
                    break;
                case "predict":
                    predictTime = Long.parseLong(value);
                    break;
            }
            line = reader.readLine();
        }
    }

    private void getDataFromDB() {
        FaceRecDbHelper mDbHelper = new FaceRecDbHelper(getApplicationContext());
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] projection = {FaceRecContract.UserEntry.COLUMN_NAME_USERNAME, FaceRecContract.UserEntry.COLUMN_NAME_USERSURNAME};
        String selection = FaceRecContract.UserEntry._ID + " = ?";
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
        name = " " + c.getString(c.getColumnIndex(FaceRecContract.UserEntry.COLUMN_NAME_USERNAME));
        surname = " " + c.getString(c.getColumnIndex(FaceRecContract.UserEntry.COLUMN_NAME_USERSURNAME));
        c.close();
        db.close();
    }

    private void appendData() {
        TextView tV = (TextView) findViewById(R.id.resultName);
        if (tV!=null) {
            tV.append(name);
        }

        tV = (TextView) findViewById(R.id.resultSurname);
        if (tV!=null) {
            tV.append(surname);
        }

        tV = (TextView) findViewById(R.id.componentsUsed);
        if (tV!=null) {
            tV.append(" " + String.valueOf(componentsUsed));
        }
    }

    private void setBitmaps() {
        File appDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/FaceRec");
        File[] contents = appDir.listFiles();
        for(File f : contents){
            if (f.getName().startsWith("test")){
                ImageView iV = (ImageView) findViewById(R.id.testFace);
                if(iV!=null){
                    iV.setImageBitmap(BitmapFactory.decodeFile(f.getAbsolutePath()));
                }
            }
            if (f.getName().startsWith("reconstruction_a")){
                String str = f.getName();
                str = str.replaceAll("\\D+","");
                componentsUsed = Integer.parseInt(str);
                ImageView iV = (ImageView) findViewById(R.id.reconstructedFace);
                if(iV!=null){
                    iV.setImageBitmap(BitmapFactory.decodeFile(f.getAbsolutePath()));
                }
            }
        }
    }

    private int countTotalImages() {
        int count = 0;
        File rootDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/FaceRec");
        File[] contents = rootDir.listFiles();
        for(File f : contents){
            if(f.isDirectory()) {
                count = f.list().length;
            }
        }
        return count;
    }

    private int countUserImages(String user) {
        int count = 0;
        File rootDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/FaceRec");
        File[] contents = rootDir.listFiles();
        for(File f : contents){
            if(f.getName().equals(user) && f.isDirectory()) {
                count = f.list().length;
                return count;
            }
        }
        return count;
    }

    private void setButtonListeners() {
        Button btn = (Button) findViewById(R.id.confirmRecognitionButton);
        if(btn!=null){
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e(TAG, "Button clicked ");
                    actualUser = mLabelPredicted;
                    setStatistics(1);
                }
            });
        } else {
            Log.e(TAG, "Error button not found");
        }

        btn = (Button) findViewById(R.id.declineRecognitionButton);
        if(btn!=null){
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    findUser();
                }
            });
        }
    }

    private void setStatistics(int success) {
        FaceRecDbHelper mDbHelper = new FaceRecDbHelper(getApplicationContext());
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String selection = FaceRecContract.StatisticsEntry.COLUMN_NAME_ALGORITHM + "=? AND " +
                FaceRecContract.StatisticsEntry.COLUMN_NAME_NUMBERIMAGESID + "=?";
        String[] selectionArgs = {algorithm, Integer.toString(countUserImages(actualUser))};
        Cursor c = db.query(
                FaceRecContract.StatisticsEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        if (!(c.moveToFirst()) || c.getCount() == 0) {
            ContentValues values = new ContentValues();
            values.put(FaceRecContract.StatisticsEntry.COLUMN_NAME_ALGORITHM, algorithm);
            values.put(FaceRecContract.StatisticsEntry.COLUMN_NAME_SUCCESS, success);
            values.put(FaceRecContract.StatisticsEntry.COLUMN_NAME_TIMEPREDICT, predictTime);
            Log.e(TAG, "Train Time -----------: " + predictTime);
            values.put(FaceRecContract.StatisticsEntry.COLUMN_NAME_TIMETRAIN, trainTime);
            values.put(FaceRecContract.StatisticsEntry.COLUMN_NAME_NUMBERIMAGESID, countUserImages(actualUser));
            values.put(FaceRecContract.StatisticsEntry.COLUMN_NAME_NUMBERIMAGESTOTAL, countTotalImages());
            values.put(FaceRecContract.StatisticsEntry.COLUMN_NAME_USERID, Integer.parseInt(actualUser));
            values.put(FaceRecContract.StatisticsEntry.COLUMN_NAME_USERID_PREDICTED, Integer.parseInt(mLabelPredicted));
            values.put(FaceRecContract.StatisticsEntry.COLUMN_NAME_TOTAL_HITS, 1);
            long i = db.insert(
                    FaceRecContract.StatisticsEntry.TABLE_NAME,
                    null,
                    values);
            Log.e(TAG, "Database insertion result:  " + i);
            c.close();
            db.close();
        } else {
            Log.e(TAG, "!!!!!!! GOT INTO SPECIAL DB INSERTION !!!!!!!");
            c.moveToFirst();
            while (!c.isAfterLast()){
                int totalHits = c.getInt(c.getColumnIndex(FaceRecContract.StatisticsEntry.COLUMN_NAME_TOTAL_HITS));
                totalHits++;
                ContentValues values = new ContentValues();
                values.put(FaceRecContract.StatisticsEntry.COLUMN_NAME_TOTAL_HITS, totalHits);
                if (success > 0) {
                    int successNumber = c.getInt(c.getColumnIndex(FaceRecContract.StatisticsEntry.COLUMN_NAME_SUCCESS));
                    successNumber++;
                    values.put(FaceRecContract.StatisticsEntry.COLUMN_NAME_SUCCESS, successNumber);
                }
                long columnId = c.getLong(c.getColumnIndex(FaceRecContract.StatisticsEntry._ID));
                long i = db.update(
                        FaceRecContract.StatisticsEntry.TABLE_NAME,
                        values,
                        FaceRecContract.StatisticsEntry._ID + "=" + columnId,
                        null
                );
                Log.e(TAG, "Database insertion result:  " + i);
                c.moveToNext();
            }
            c.close();
            db.close();
        }
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1){
            actualUser=data.getStringExtra("USERID");
            setStatistics(0);
        }
    }

    private void findUser(){
        Intent selectUserIntent = new Intent(RecognitionResultActivity.this, SelectUserActivity.class);
        startActivityForResult(selectUserIntent, 1);
    }
}
