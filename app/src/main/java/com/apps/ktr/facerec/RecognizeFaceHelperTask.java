package com.apps.ktr.facerec;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;


/**
 * Created by nikos on 4/24/16.
 * Project name: FaceRec.
 * File name: ${FILE_NAME}.
 * Developed with: Android Studio.
 */
public class RecognizeFaceHelperTask extends AsyncTask<String, Void, Integer> {
    private static final String TAG = "FACEREC";
    public AsyncResponseRec delegate = null;
    private int algorithm;
    private boolean useTrainedData;

    static {
        System.loadLibrary("native");
    }

    public interface AsyncResponseRec {
        void processFinishRec(String output);
    }

    public RecognizeFaceHelperTask(int alg, AsyncResponseRec del, boolean b){
        delegate = del;
        algorithm = alg;
        useTrainedData = b;
    }

    @Override
    protected Integer doInBackground(String... strings) {
        Bitmap temp = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/FaceRec/test.png");
        if (temp == null) {
            try {
                Log.e(TAG, "Image not found, retrying in one second");
                Thread.sleep(500, 0);
            } catch (InterruptedException e) {
                Log.e(TAG, "Interrupted exception while sleeping detect face thread" + e.toString());
            }
        }
        String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();
        File appDir = new File(sdcard + "/FaceRec");
        if (appDir.exists()) {
            File[] contents = appDir.listFiles();
            for (File f : contents) {
                if(f.getName().startsWith("reconstruction") || f.getName().startsWith("eigenface")){
                    f.delete();
                }
            }
        }

        String test = NativeClass.nativeFunction(algorithm, useTrainedData, sdcard);
        return  Integer.parseInt(test);
//        switch (mAlgorithm) {
//            case EIGENFACES: break;
//            case FISHERFACES: break;
//        }
//        BitmapFactory.Options bitmapFactoryOptions = new BitmapFactory.Options();
//        bitmapFactoryOptions.inPreferredConfig = Bitmap.Config.RGB_565;
//        Bitmap input = null;
//        while (input == null) {
//            input = BitmapFactory.decodeFile(mCurrentPhotoPath, bitmapFactoryOptions);
//            try {
//                Log.e(TAG, "Image not found, retrying in one second");
//                Thread.sleep(1000, 0);
//            } catch (InterruptedException e) {
//                Log.e(TAG, "Interrupted exception while sleeping detect face thread" + e.toString());
//            }
//        }
//        String csvLocation = Environment.getExternalStorageDirectory() + "/" + ROOT + "/images.csv";
//        int lines = countLines(csvLocation);
//        List<Mat> images = new ArrayList<>();
//        Mat labels = new Mat(lines, 1, CvType.CV_32SC1);
//        Log.e(TAG, "-------Number of labels: " + lines);
//        readCsv(csvLocation, images, labels);
////
//        FaceRecognizer model = Face.createEigenFaceRecognizer();
//        model.train(images, labels);
////
//        Mat test = Imgcodecs.imread(mCurrentPhotoPath);
//        int predictLabel = model.predict_label(test);
//        return predictLabel;
    }

    @Override
    protected void onPostExecute(Integer i) {
        super.onPostExecute(i);
        delegate.processFinishRec(i.toString());
    }

//    private opencv_core.Mat norm (Bitmap b) {
//        opencv_core.Mat ret = bitmapToMat(b);
//        Mat returningMat = new Mat();
//
//        switch (ret.channels()) {
//            case 1: {
//                opencv_core.normalize(ret, returningMat, 0.0, 255.0, opencv_core.NORM_MINMAX, opencv_core.CV_8UC1, null);
//                return returningMat;
//            }
//            case 2: {
//                opencv_core.normalize(ret, returningMat, 0.0, 255.0, opencv_core.NORM_MINMAX, opencv_core.CV_8UC2, null);
//                return returningMat;
//            }
//            case 3: {
//                opencv_core.normalize(ret, returningMat, 0.0, 255.0, opencv_core.NORM_MINMAX, opencv_core.CV_8UC3, null);
//                return returningMat;
//            }
//            case 4: {
//                opencv_core.normalize(ret, returningMat, 0.0, 255.0, opencv_core.NORM_MINMAX, opencv_core.CV_8UC4, null);
//                return returningMat;
//            }
//            default: {
//                return ret;
//            }
//        }
//    }

//    private void readCsv(String location, List<Mat> images, Mat labels) {
//        File csv = new File(location);
//        try {
//            BufferedReader bR = new BufferedReader(new FileReader(csv));
//            while(!bR.ready()){
//                try {
//                    wait(1000);
//                } catch (InterruptedException e) {
//                    Log.e(TAG, "Error waiting thread for reader: " + e.getMessage());
//                }
//            }
//            String line = bR.readLine();
//            int counter = 0;
//            while (line != null) {
//                int index = line.indexOf(";");
//                if (index != -1) {
//                    Mat image = Imgcodecs.imread(line.substring(0, index), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
//                    int label = Integer.parseInt(line.substring(index + 1));
//                    images.add(counter, image);
//                    labels.put(counter, 0, new int[]{label});
//                    counter++;
//                }
//                line = bR.readLine();
//            }
//        } catch (IOException e) {
//            Log.e(TAG, "Error reading csv file: " + e.getMessage());
//        }
//    }
//
//    private Mat bitmapToMat(Bitmap bmp) {
//        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
//        AndroidFrameConverter toFrame = new AndroidFrameConverter();
//        Frame frame = toFrame.convert(bmp);
//        Mat mat = converter.convertToMat(frame);
//        return mat;
//    }
//
//    private Bitmap matToBitmap (Mat mat) {
//        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
//        AndroidFrameConverter toBitmap = new AndroidFrameConverter();
//        Frame frame = converter.convert(mat);
//        Bitmap bmp = toBitmap.convert(frame);
//        return bmp;
//    }
//
//    private int countLines(String filename) {
//        try {
//            InputStream is = new BufferedInputStream(new FileInputStream(filename));
//            byte[] c = new byte[1024];
//            int count = 0;
//            int readChars = 0;
//            boolean empty = true;
//            while ((readChars = is.read(c)) != -1) {
//                empty = false;
//                for (int i = 0; i < readChars; ++i) {
//                    if (c[i] == '\n') {
//                        ++count;
//                    }
//                }
//            }
//            return (count == 0 && !empty) ? 1 : count;
//        } catch (IOException e) {
//            Log.e(TAG, "Error counting lines");
//        }
//        return 0;
//    }
}
