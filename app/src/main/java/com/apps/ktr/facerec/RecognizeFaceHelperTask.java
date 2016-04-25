package com.apps.ktr.facerec;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.CvType;
import org.opencv.android.Utils;
import org.bytedeco.javacpp.openc

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Vector;


/**
 * Created by nikos on 4/24/16.
 * Project name: FaceRec.
 * File name: ${FILE_NAME}.
 * Developed with: Android Studio.
 */
public class RecognizeFaceHelperTask extends AsyncTask<String, Void, Bitmap> {
    private static final String TAG = "FACEREC";
    private static final String ROOT = "FaceRec";

    public final static int EIGENFACES = 1;
    public final static int FISHERFACES = 2;

    private String mCurrentPhotoPath;
    private int mAlgorithm;
    private Context mContext;
    private String mCsvLocation = Environment.getExternalStorageDirectory() + "/" + ROOT + "/images.csv";
    private WeakReference<ImageView> wR;

    public RecognizeFaceHelperTask(String photoPath, int algorithmNumber, Context c, ImageView iV){
        mCurrentPhotoPath = photoPath;
        mAlgorithm = algorithmNumber;
        mContext = c;
        wR = new WeakReference<>(iV);
    }

    @Override
    protected Bitmap doInBackground(String... strings) {
//        switch (mAlgorithm) {
//            case EIGENFACES: break;
//            case FISHERFACES: break;
//        }
        BitmapFactory.Options bitmapFactoryOptions = new BitmapFactory.Options();
        bitmapFactoryOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap input = null;
        while (input == null) {
            input = BitmapFactory.decodeFile(mCurrentPhotoPath, bitmapFactoryOptions);
            try {
                Log.e(TAG, "Image not found, retrying in one second");
                Thread.sleep(1000, 0);
            } catch (InterruptedException e) {
                Log.e(TAG, "Interrupted exception while sleeping detect face thread" + e.toString());
            }
        }
        Bitmap bmp32 = input.copy(Bitmap.Config.ARGB_8888, true);
        Mat mat;
        mat = norm(bmp32);
        Log.e(TAG, mat.channels() + "");
        bmp32 = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bmp32);
        //return bmp32;

        Vector<Mat> images = new Vector<>();
        Vector<Integer> labels = new Vector<>();
        readCsv(mCsvLocation, images, labels);
    }

    @Override
    protected void onPostExecute(Bitmap b) {
        super.onPostExecute(b);
        final ImageView iV = wR.get();
        iV.setImageBitmap(b);

    }

    private Mat norm (Bitmap b) {
        Mat ret = new Mat();
        Utils.bitmapToMat(b, ret);
        Mat returningMat = new Mat();

        switch (ret.channels()) {
            case 1:
                Core.normalize(ret, returningMat, 0.0, 255.0, Core.NORM_MINMAX, CvType.CV_8UC1);
                return returningMat;
            case 2:
                Core.normalize(ret, returningMat, 0.0, 255.0, Core.NORM_MINMAX, CvType.CV_8UC2);
                return returningMat;
            case 3:
                Core.normalize(ret, returningMat, 0.0, 255.0, Core.NORM_MINMAX, CvType.CV_8UC3);
                return returningMat;
            case 4:
                Core.normalize(ret, returningMat, 0.0, 255.0, Core.NORM_MINMAX, CvType.CV_8UC4);
                return returningMat;
            default:
                return ret;
        }
    }

    private void readCsv(String location, Vector<Mat> images, Vector<Integer> labels) {
        File rootFolder = new File(location);
    }


}
