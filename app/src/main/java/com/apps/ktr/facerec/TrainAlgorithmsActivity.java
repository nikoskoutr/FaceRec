package com.apps.ktr.facerec;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

public class TrainAlgorithmsActivity extends AppCompatActivity {
    private Handler handler;
    private ProgressDialog dialog;
    private final int TRAIN_EIGENFACES = 1;
    private final int TRAIN_FISHERFACES = 2;
    private final int TRAIN_LBPH = 3;
    private final int SAVE_EIGENFACES = 4;
    private final int SAVE_FISHERFACES = 5;
    private final int SAVE_LBPH = 6;
    private long eigenFacesTime;
    private long fisherFacesTime;
    private long lbphTime;
    final Context mContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train_algorithms);

        dialog = new ProgressDialog(mContext);
        dialog.setMessage("Please Wait!!");
        dialog.setCancelable(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();

        new Thread() {
            public void run() {
                long start = System.nanoTime();
                NativeClass.nativeFunction(TRAIN_EIGENFACES);
                eigenFacesTime =  TimeUnit.SECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS);
                start = System.nanoTime();
                NativeClass.nativeFunction(TRAIN_FISHERFACES);
                fisherFacesTime = TimeUnit.SECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS);
                start = System.nanoTime();
                NativeClass.nativeFunction(TRAIN_LBPH);
                lbphTime = TimeUnit.SECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS);
                handler.sendEmptyMessage(0);
            }
        }.start();
        handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                TextView tv = (TextView) findViewById(R.id.eigenfacetime);
                if (tv != null) {
                    tv.append(" " + Long.toString(eigenFacesTime, 10));
                }
                tv = (TextView) findViewById(R.id.fisherfacetime);
                if (tv != null) {
                    tv.append(" " + Long.toString(fisherFacesTime, 10));
                }
                tv = (TextView) findViewById(R.id.lbphtime);
                if (tv != null) {
                    tv.append(" " + Long.toString(lbphTime, 10));
                }
                dialog.dismiss();
            }
        };

        Button btn = (Button) findViewById(R.id.okTrainAlgorithmsButton);
        if (btn != null) {
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        btn = (Button) findViewById(R.id.saveTrainedFile);
        if (btn != null) {
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    String message = getResources().getString(R.string.alertSaveFile);
                    builder.setMessage(message).setTitle(R.string.alertSaveFileTitle);
                    builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            saveFiles();
                        }
                    });
                    builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            });
        }
    }

    private void saveFiles() {
        dialog.show();

        new Thread() {
            public void run() {
                NativeClass.nativeFunction(SAVE_FISHERFACES);
                NativeClass.nativeFunction(SAVE_EIGENFACES);
                NativeClass.nativeFunction(SAVE_LBPH);
                handler.sendEmptyMessage(0);
            }
        }.start();

        handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                dialog.dismiss();
            }
        };
    }
}
