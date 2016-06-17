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
        public static final String COLUMN_NAME_USERID = "userid";
        public static final String COLUMN_NAME_USERNAME = "name";
        public static final String COLUMN_NAME_USERSURNAME = "surname";
        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN_NAME_USERID + INTEGER_TYPE + PRIMARY_KEY + COMMA_SEP +
                        COLUMN_NAME_USERNAME + TEXT_TYPE + COMMA_SEP +
                        COLUMN_NAME_USERSURNAME + TEXT_TYPE + COMMA_SEP +
                        "UNIQUE(" + COLUMN_NAME_USERNAME + COMMA_SEP + COLUMN_NAME_USERSURNAME + ")" +
                " )";

        public static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    // Contract for the table containing the location of face images with their corresponding users.
//    public static abstract class FaceImageEntry implements BaseColumns {
//        public static final String TABLE_NAME = "faces";
//        public static final String COLUMN_NAME_LOCATION = "location";
//        public static final String COLUMN_NAME_USERID = "userid";
//        public static final String SQL_CREATE_ENTRIES =
//                "CREATE TABLE " + TABLE_NAME + " (" +
//                        COLUMN_NAME_LOCATION + TEXT_TYPE + PRIMARY_KEY + COMMA_SEP +
//                        COLUMN_NAME_USERID + INTEGER_TYPE + COMMA_SEP +
//                        "FOREIGN KEY ( " + COLUMN_NAME_USERID + " ) references " +
//                        UserEntry.TABLE_NAME + "(" + UserEntry.COLUMN_NAME_USERID + ")" +
//                        " )";
//
//        public static final String SQL_DELETE_ENTRIES =
//                "DROP TABLE IF EXISTS " + TABLE_NAME;
//    }
}
