package com.apps.ktr.facerec;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_face;
import org.bytedeco.javacpp.opencv_face.FaceRecognizer;
import org.bytedeco.javacpp.opencv_imgcodecs;
import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.nio.IntBuffer;
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
//        Bitmap bmp32 = input.copy(Bitmap.Config.ARGB_8888, true);
//        Mat mat = new Mat();
//        mat = norm(bmp32);
//        Log.e(TAG, mat.channels() + "");
//        bmp32 = matToBitmap(mat);

        String csvLocation = Environment.getExternalStorageDirectory() + "/" + ROOT + "/images.csv";
        int lines = countLines(csvLocation);
        opencv_core.MatVector images = new opencv_core.MatVector(lines);
        Mat labels = new Mat(lines, 1, opencv_core.CV_32SC1);

        readCsv(csvLocation, images, labels);
        int height = images.get(0).rows();
//
        FaceRecognizer model = opencv_face.createEigenFaceRecognizer();
        model.
        model.train(images, new Mat(1, labels.length, labels));
    }

    @Override
    protected void onPostExecute(Bitmap b) {
        super.onPostExecute(b);
        final ImageView iV = wR.get();
        iV.setImageBitmap(b);

    }

    private Mat norm (Bitmap b) {
        Mat ret = bitmapToMat(b);
        Mat returningMat = new Mat();

        switch (ret.channels()) {
            case 1: {
                opencv_core.normalize(ret, returningMat, 0.0, 255.0, opencv_core.NORM_MINMAX, opencv_core.CV_8UC1, null);
                return returningMat;
            }
            case 2: {
                opencv_core.normalize(ret, returningMat, 0.0, 255.0, opencv_core.NORM_MINMAX, opencv_core.CV_8UC2, null);
                return returningMat;
            }
            case 3: {
                opencv_core.normalize(ret, returningMat, 0.0, 255.0, opencv_core.NORM_MINMAX, opencv_core.CV_8UC3, null);
                return returningMat;
            }
            case 4: {
                opencv_core.normalize(ret, returningMat, 0.0, 255.0, opencv_core.NORM_MINMAX, opencv_core.CV_8UC4, null);
                return returningMat;
            }
            default: {
                return ret;
            }
        }
    }

    private void readCsv(String location, opencv_core.MatVector images, Mat labels) {
        File csv = new File(location);
        try {
            BufferedReader bR = new BufferedReader(new FileReader(csv));
            while(!bR.ready()){
                try {
                    wait(1000);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Error waiting thread for reader: " + e.getMessage());
                }
            }
            String line = bR.readLine();
            IntBuffer labelsBuf = labels.getIntBuffer();
            int counter = 0;
            while (line != null) {
                int index = line.indexOf(";");
                if (index != -1) {
                    Mat image = opencv_imgcodecs.imread(line.substring(0, index));
                    Integer label = Integer.parseInt(line.substring(index + 1));
                    images.put(counter, image);
                    labelsBuf.put(counter, label);
                    counter++;
                }
                line = bR.readLine();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error reading csv file: " + e.getMessage());
        }
    }

    private Mat bitmapToMat(Bitmap bmp) {
        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
        AndroidFrameConverter toFrame = new AndroidFrameConverter();
        Frame frame = toFrame.convert(bmp);
        Mat mat = converter.convertToMat(frame);
        return mat;
    }

    private Bitmap matToBitmap (Mat mat) {
        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
        AndroidFrameConverter toBitmap = new AndroidFrameConverter();
        Frame frame = converter.convert(mat);
        Bitmap bmp = toBitmap.convert(frame);
        return bmp;
    }

    public int countLines(String filename) {
        try {
            InputStream is = new BufferedInputStream(new FileInputStream(filename));
            byte[] c = new byte[1024];
            int count = 0;
            int readChars = 0;
            boolean empty = true;
            while ((readChars = is.read(c)) != -1) {
                empty = false;
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n') {
                        ++count;
                    }
                }
            }
            return (count == 0 && !empty) ? 1 : count;
        } catch (IOException e) {
            Log.e(TAG, "Error counting lines");
        }
        return 0;
    }


}
