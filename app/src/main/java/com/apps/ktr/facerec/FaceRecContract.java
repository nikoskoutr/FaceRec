package com.apps.ktr.facerec;

import android.provider.BaseColumns;

/**
 * Created by nikos on 6/14/16.
 * Project name: FaceRec.
 * File name: ${FILE_NAME}.
 * Developed with: Android Studio.
 */
public final class FaceRecContract {
    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String PRIMARY_KEY = " PRIMARY KEY";
    private static final String COMMA_SEP = ",";
    // Prevent instantiation of contract, empty constructor provided.
    public  FaceRecContract() {}

    // Contract for the table containing user info.
    public static abstract class UserEntry implements BaseColumns {

        public static final String TABLE_NAME = "users";
        public static final String COLUMN_NAME_USERNAME = "name";
        public static final String COLUMN_NAME_USERSURNAME = "surname";
        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + INTEGER_TYPE + PRIMARY_KEY + COMMA_SEP +
                        COLUMN_NAME_USERNAME + TEXT_TYPE + COMMA_SEP +
                        COLUMN_NAME_USERSURNAME + TEXT_TYPE + COMMA_SEP +
                        "UNIQUE(" + COLUMN_NAME_USERNAME + COMMA_SEP + COLUMN_NAME_USERSURNAME + ")" +
                " )";

        public static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    //Contract for the table containing data about the prediction times and stressfulness of the algorithms.
    public static abstract class StatisticsEntry implements BaseColumns {
        public static final String TABLE_NAME = "statistics";
        public static final String COLUMN_NAME_ALGORITHM = "algorithm";
        public static final String COLUMN_NAME_TIMETRAIN = "ttime";
        public static final String COLUMN_NAME_TIMEPREDICT = "ptime";
        public static final String COLUMN_NAME_NUMBERIMAGESID = "nimagesid";
        public static final String COLUMN_NAME_NUMBERIMAGESTOTAL = "nimagestotal";
        public static final String COLUMN_NAME_SUCCESS = "success";
        public static final String COLUMN_NAME_USERID = "userid";
        public static final String COLUMN_NAME_USERID_PREDICTED = "useridp";
        public static final String COLUMN_NAME_TOTAL_HITS = "totalhits";
        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + INTEGER_TYPE + PRIMARY_KEY + COMMA_SEP +
                        COLUMN_NAME_ALGORITHM + TEXT_TYPE + COMMA_SEP +
                        COLUMN_NAME_TIMETRAIN + INTEGER_TYPE + COMMA_SEP +
                        COLUMN_NAME_TIMEPREDICT + INTEGER_TYPE + COMMA_SEP +
                        COLUMN_NAME_NUMBERIMAGESID + INTEGER_TYPE + COMMA_SEP +
                        COLUMN_NAME_NUMBERIMAGESTOTAL + INTEGER_TYPE + COMMA_SEP +
                        COLUMN_NAME_SUCCESS + INTEGER_TYPE + COMMA_SEP +
                        COLUMN_NAME_USERID + INTEGER_TYPE + COMMA_SEP +
                        COLUMN_NAME_USERID_PREDICTED + INTEGER_TYPE + COMMA_SEP +
                        COLUMN_NAME_TOTAL_HITS + INTEGER_TYPE +
                        " )";

        public static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }
}
