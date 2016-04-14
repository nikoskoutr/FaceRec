package com.apps.ktr.facerec;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.media.FaceDetector;

/**
 * Created by nikos on 4/14/16.
 */
public class DetectFaceHelper {

    public static Bitmap findFace(String imgPath) {
        int c = 0;
        Bitmap img = BitmapFactory.decodeFile(imgPath);
        FaceDetector faceDetector = new FaceDetector(img.getWidth(), img.getHeight(), 1);
        FaceDetector.Face[] faceArray = new FaceDetector.Face[1];
        c = faceDetector.findFaces(img, faceArray);
        if (c>0) {
            double width = faceArray[0].eyesDistance()*1.5;
            double height = faceArray[0].eyesDistance()*1.5;
            PointF pointF = new PointF();
            faceArray[0].getMidPoint(pointF);
            return Bitmap.createBitmap(img, (int) pointF.x - (int) faceArray[0].eyesDistance(), (int) pointF.y - (int) faceArray[0].eyesDistance(), (int) width, (int) height);
        }

        return null;
    }
}
