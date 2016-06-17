package com.apps.ktr.facerec;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.PointF;
import android.graphics.Rect;
import android.media.FaceDetector;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Created by nikos on 4/14/16.
 */
public class DetectFaceHelperTask extends AsyncTask<String, Void, Bitmap>{

    private static final String TAG = "FACEREC";
    private final WeakReference<ImageView> imageViewReference;
    private final WeakReference<ProgressDialog> pd;
    public AsyncResponse delegate = null;

    public interface AsyncResponse {
        void processFinish(Bitmap output);
    }

    public DetectFaceHelperTask(ImageView imageView, ProgressDialog pD, AsyncResponse del) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        imageViewReference = new WeakReference<>(imageView);
        pd = new WeakReference<>(pD);
        delegate = del;
    }

    @Override
    protected Bitmap doInBackground(String... str) {
        BitmapFactory.Options bitmapFactoryOptions = new BitmapFactory.Options();
        bitmapFactoryOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap img = null;
        while (img == null) {
            img = BitmapFactory.decodeFile(str[0], bitmapFactoryOptions);
            try {
                Log.e(TAG, "Image not found, retrying in one second");
                Thread.sleep(1000, 0);
            } catch (InterruptedException e) {
                Log.e(TAG, "Interrupted exception while sleeping detect face thread" + e.toString());
            }
        }
        FaceDetector faceDetector = new FaceDetector(img.getWidth(), img.getHeight(), 1);
        FaceDetector.Face[] faceArray = new FaceDetector.Face[1];
        int c = faceDetector.findFaces(img, faceArray);
        if (c > 0) {
            int eyeD = (int) faceArray[0].eyesDistance();
            int width =  eyeD * 9 / 4;
            int height = eyeD * 10 / 3;
            PointF pointF = new PointF();
            faceArray[0].getMidPoint(pointF);
            int x = (int) pointF.x;
            int y = (int) pointF.y;
            Rect r = new Rect(x - width/2, y - 4 * height/11, x + width/2, y + 6 * height/11);
            try {
                BitmapRegionDecoder bRD = BitmapRegionDecoder.newInstance(str[0], false);
                BitmapFactory.Options ops = new BitmapFactory.Options();
                ops.inPreferredConfig = Bitmap.Config.ALPHA_8;
                return bRD.decodeRegion(r, ops);
            } catch (IOException e){
                Log.e(TAG, "Error at bitmap region decoder");
            }
        } else {
            img = null;
        }

        return img;
    }

    @Override
    protected void onPostExecute(Bitmap res) {
        if (res != null) {
            final ImageView imageView = imageViewReference.get();
            imageView.setImageBitmap(res);
        }
        final ProgressDialog progressDialog = pd.get();
        progressDialog.dismiss();
        delegate.processFinish(res);
    }
}
