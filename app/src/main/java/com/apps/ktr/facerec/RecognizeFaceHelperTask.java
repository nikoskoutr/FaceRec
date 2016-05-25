package com.apps.ktr.facerec;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

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
import java.nio.IntBuffer;


/**
 * Created by nikos on 4/24/16.
 * Project name: FaceRec.
 * File name: ${FILE_NAME}.
 * Developed with: Android Studio.
 */
public class RecognizeFaceHelperTask extends AsyncTask<String, Void, Integer> {
    private static final String TAG = "FACEREC";
    private static final String ROOT = "FaceRec";

    public final static int EIGENFACES = 1;
    public final static int FISHERFACES = 2;

    private String mCurrentPhotoPath;
    private Bitmap mFaceImg = null;
    private int mAlgorithm;

    public AsyncResponseRec delegate = null;
    public interface AsyncResponseRec {
        void processFinishRec(String output);
    }

    public RecognizeFaceHelperTask(String photoPath, int algorithmNumber, AsyncResponseRec del){
        mCurrentPhotoPath = photoPath;
        mAlgorithm = algorithmNumber;
        delegate = del;
    }

    @Override
    protected Integer doInBackground(String... strings) {
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
        String csvLocation = Environment.getExternalStorageDirectory() + "/" + ROOT + "/images.csv";
        int lines = countLines(csvLocation);
        opencv_core.MatVector images = new opencv_core.MatVector(lines);
        opencv_core.Mat asd = new Mat();
        opencv_core.Mat labels = new Mat(lines, 1);

        readCsv(csvLocation, images, labels);

        FaceRecognizer model = opencv_face.createFisherFaceRecognizer();
        model.train(images, labels);

        opencv_core.Mat test = bitmapToMat(mFaceImg);
        int predictLabel = model.predict(test);
        return predictLabel;
    }

    @Override
    protected void onPostExecute(Integer i) {
        super.onPostExecute(i);
        delegate.processFinishRec(i.toString());
    }

    private opencv_core.Mat norm (Bitmap b) {
        opencv_core.Mat ret = bitmapToMat(b);
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
                    Mat image = opencv_imgcodecs.imread(line.substring(0, index), opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
                    int label = Integer.parseInt(line.substring(index + 1));
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

    private int countLines(String filename) {
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
