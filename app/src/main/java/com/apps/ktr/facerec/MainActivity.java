package com.apps.ktr.facerec;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    static {
        System.loadLibrary("native");
    }
    public final static String EXTRA_MESSAGE = "com.apps.ktr.facerec.MESSAGE";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String TAG = "FACEREC";
    private String mCurrentPhotoPath;
    private Context mContext;
    private Handler handler;
    private ProgressDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        Button btn = (Button) findViewById(R.id.cameraButton);
        if (btn != null) {
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent takePictureIntent = new Intent(mContext, CameraActivity.class);
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            });
        }

        btn = (Button) findViewById(R.id.identifyButton);
        if(btn != null) {
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent recognizeFaceIntent = new Intent(mContext, IdentifyFaceActivity.class);
                    startActivity(recognizeFaceIntent);
                }
            });
        }

        btn = (Button) findViewById(R.id.createCsvButton);
        if(btn != null) {
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog = new ProgressDialog(mContext);
                    dialog.setMessage("Please Wait!!");
                    dialog.setCancelable(false);
                    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    dialog.show();

                    new Thread() {
                        public void run() {
                            reloadCsv();
                            handler.sendEmptyMessage(0);
                        }
                    }.start();
                    handler = new Handler() {
                        public void handleMessage(android.os.Message msg) {
                            dialog.dismiss();
                        }
                    };
                }
            });
        }


        btn = (Button) findViewById(R.id.dropDbButton);
        if (btn != null) {
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e(TAG,"Droped db");
                    FaceRecDbHelper mDbHelper = new FaceRecDbHelper(getApplicationContext());
                    SQLiteDatabase db = mDbHelper.getWritableDatabase();
                    mDbHelper.onUpgrade(db,0,0);
                    db.close();
                }
            });
        }

        btn = (Button) findViewById(R.id.statisticsButton);
        if (btn != null) {
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent statisticsIntent = new Intent(mContext, StatisticsActivity.class);
                    startActivity(statisticsIntent);
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        mCurrentPhotoPath = data.getStringExtra(MainActivity.EXTRA_MESSAGE);
        galleryAddPic();
        Intent confirmFaceIntent = new Intent(this, ConfirmFaceActivity.class);
        confirmFaceIntent.putExtra(EXTRA_MESSAGE, mCurrentPhotoPath);
        startActivity(confirmFaceIntent);
    }


    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = null;
        while (f == null) {
            f = new File(mCurrentPhotoPath);
            try {
                Thread.sleep(1000, 0);
            } catch (InterruptedException e) {
                Log.e(TAG, "Sleep interrupted at media scanner sleep" + e.toString());
            }
        }
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void reloadCsv(){
            Hashtable<String,String[]> csvData = new Hashtable<>();
            File rootFolder = new File(Environment.getExternalStorageDirectory() + "/FaceRec");
            if (!(rootFolder.exists())) {
                rootFolder.mkdir();
            }

            File[] contents = rootFolder.listFiles();
            for(File f : contents) {
                if (f.isDirectory()) {
                    String id = f.getName();
                    File[] files = f.listFiles();
                    String[] images = new String[files.length];
                    int i = 0;
                    for(File image : files){
                        images[i] = image.getAbsolutePath();
                        i++;
                    }
                    csvData.put(id, images);
                }
            }

            File csv = new File(rootFolder.getAbsolutePath() + "/images.csv");
            if(csv.exists()){
                csv.delete();
            }
            try{
                csv.createNewFile();
                BufferedWriter w = new BufferedWriter(new FileWriter(csv, true));
                Set<Map.Entry<String, String[]>> dataSet = csvData.entrySet();
                for (Map.Entry<String, String[]> entry : dataSet) {
                    String[] images = entry.getValue();
                    String location = entry.getKey();
                    for (String image : images) {
                        w.write(image + ";" + location);
                        w.newLine();
                    }
                }
                w.close();
            } catch (IOException e) {
                Log.e(TAG, "Error recreating csv file");
            }
        }

}

