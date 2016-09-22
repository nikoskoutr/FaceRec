package com.apps.ktr.facerec;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by nikos on 6/14/16.
 * Project name: FaceRec.
 * File name: ${FILE_NAME}.
 * Developed with: Android Studio.
 */
public class FaceRecDbHelper extends SQLiteOpenHelper{
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "FaceRec.db";

    public FaceRecDbHelper(Context c) {
        super(c, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(FaceRecContract.UserEntry.SQL_CREATE_ENTRIES);
        db.execSQL(FaceRecContract.StatisticsEntry.SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(FaceRecContract.UserEntry.SQL_DELETE_ENTRIES);
        db.execSQL(FaceRecContract.StatisticsEntry.SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
