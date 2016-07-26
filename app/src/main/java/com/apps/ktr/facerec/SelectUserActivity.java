package com.apps.ktr.facerec;

import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

public class SelectUserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_user);
        Resources r = getResources();
        ListView listView = (ListView) findViewById(R.id.listView);
        String[] arrayColumns = new String[]{FaceRecContract.UserEntry._ID, FaceRecContract.UserEntry.COLUMN_NAME_USERNAME, FaceRecContract.UserEntry.COLUMN_NAME_USERSURNAME};
        int[] arrayIds = new int[]{R.id.listID, R.id.listName, R.id.listSurname};
        FaceRecDbHelper mDbHelper = new FaceRecDbHelper(getApplicationContext());
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String[] projection = {FaceRecContract.UserEntry._ID, FaceRecContract.UserEntry.COLUMN_NAME_USERNAME, FaceRecContract.UserEntry.COLUMN_NAME_USERSURNAME};
        Cursor c = db.query(
                FaceRecContract.UserEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.activity_select_user_eachitem, c, arrayColumns, arrayIds, 0);
        if(listView!=null){
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    TextView tV = (TextView) view.findViewById(R.id.listID);
                    String userId = tV.getText().toString();
                    Intent intent=new Intent();
                    intent.putExtra("USERID",userId);
                    setResult(1,intent);
                    finish();
                }
            });
        }
    }
}
