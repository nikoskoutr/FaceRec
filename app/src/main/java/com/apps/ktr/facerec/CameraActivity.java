package com.apps.ktr.facerec;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CameraActivity extends AppCompatActivity {
    private Camera mCamera;
    private CameraPreview mPreview;
    private static final String TAG = "FACEREC";
    private Overlay mFaceView;
    private String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // Create an instance of Camera
        if (checkCameraHardware(this)) {
            mCamera = openFrontFacingCamera();
        }

        mFaceView = new Overlay(this, mCamera);

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera, mFaceView);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        if (preview != null) {
            preview.addView(mPreview);
            preview.addView(mFaceView);
        }

        // Set button listener to capture image.
        Button captureBtn = (Button) findViewById(R.id.button_capture);
        if (captureBtn != null) {
            captureBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Set take picture function with listener.
                    mCamera.takePicture(null, null, new Camera.PictureCallback() {
                        @Override
                        public void onPictureTaken(byte[] data, Camera camera) {
                            File pictureFile = null;
                            try {
                                //Create image file to write image into.
                                pictureFile = createImageFIle();
                            } catch (IOException e) {
                                Log.e(TAG, "Error creating image file: " + e.getMessage());
                            }
                            if (pictureFile == null) {
                                Log.d(TAG, "Error creating media file, check storage permissions: ");
                                return;
                            }

                            try {
                                // Write image into file.
                                FileOutputStream fos = new FileOutputStream(pictureFile);
                                fos.write(data);
                                fos.close();
                            } catch (FileNotFoundException e) {
                                Log.d(TAG, "File not found: " + e.getMessage());
                            } catch (IOException e) {
                                Log.d(TAG, "Error accessing file: " + e.getMessage());
                            }

                            // Set result intent and return.
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra(MainActivity.EXTRA_MESSAGE, mCurrentPhotoPath);
                            setResult(Activity.RESULT_OK, resultIntent);
                            finish();
                        }
                    });
                }
            });
        }
    }

    public File createImageFIle() throws IOException {
        // Timestamp to identify file.
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = this.getCacheDir();
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        // Set member variable for current image path.
        mCurrentPhotoPath = image.getAbsolutePath();

        // Returns image file ready to be written on.
        return image;
    }

    private boolean checkCameraHardware(Context c) {

        // Returns boolean for camera availability.
        return c.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);

    }

    private Camera openFrontFacingCamera() {

        // Function to open front facing camera.
        Camera c = null;
        int cameraCount = 0;
        // CameraInfo object to get type of camera.
        Camera.CameraInfo cI = new Camera.CameraInfo();
        // Get number of available cameras.
        cameraCount = Camera.getNumberOfCameras();
        // Iterate through available cameras.
        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, cI);
            // If camera selected is front facing, open it.
            if (cI.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    c = Camera.open(i);
                } catch (RuntimeException e) {
                    Log.e(TAG, "Error opening camera.");
                }
            }
        }
        // Returns either null or open front facing camera.
        return c;
    }
}


class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Camera.Size mPreviewSize;
    private static final String TAG = "FACEREC";
    private static List<Camera.Size> mSupportedPreviewSizes;
    private Context mContext;
    private Overlay mFaceView;

    public CameraPreview(Context c, Camera ca, Overlay o) {
        super(c);
        // Constructor that sets member variables for context, camera and supported preview sizes.
        mContext = c;
        mCamera = ca;
        mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
        mFaceView = o;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // Deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
            // Provides live face detection listener;
            mCamera.setFaceDetectionListener(new Camera.FaceDetectionListener() {
                @Override
                public void onFaceDetection(Camera.Face[] faces, Camera camera) {
                    mFaceView.setFaces(faces);
                }
            });
            // Starts face detection, it should be stopped on activity destroy.
            mCamera.startFaceDetection();
        } catch (IOException e) {
            Log.e(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        // Stop face detection.
        mCamera.stopFaceDetection();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // Set optimal preview size to avoid wrong image representation.
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
        mCamera.setParameters(parameters);

        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // Extends superclass to set member variables for optimal preview size.
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);

        if (mSupportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
        }
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        // Iterates through available supported preview sizes
        // to get the largest supported preview.
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - h) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - h);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - h) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - h);
                }
            }
        }

        return optimalSize;
    }
}

class Overlay extends View {
    private Paint mPaint;
    private Paint mTextPaint;
    private Camera.Face[] mFaces;
    private Camera mCamera;

    public Overlay(Context c, Camera cam) {
        super(c);
        mCamera = cam;
        initialize();
    }

    private void initialize() {
        // We want a green box around the face:
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.GREEN);
        mPaint.setAlpha(128);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(5);

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setDither(true);
        mTextPaint.setTextSize(40);
        mTextPaint.setColor(Color.GREEN);
        mTextPaint.setStyle(Paint.Style.FILL);
    }

    public void setFaces(Camera.Face[] faces) {
        mFaces = faces;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mFaces != null && mFaces.length > 0) {
            RectF rectF = new RectF();
            Matrix matrix = new Matrix();
            matrix.setScale(-1, 1);
            matrix.postScale(getWidth() / 2000f, getHeight() / 2000f);
            matrix.postTranslate(getWidth() / 2f, getHeight() / 2f);
            for (Camera.Face face : mFaces) {
                rectF.set(face.rect);
                matrix.mapRect(rectF);
                canvas.drawRect(rectF, mPaint);
                canvas.drawText("Score " + face.score, rectF.right, rectF.top, mTextPaint);
            }
            if (Build.VERSION.SDK_INT != Build.VERSION_CODES.M) canvas.restore();
        }
    }
}


