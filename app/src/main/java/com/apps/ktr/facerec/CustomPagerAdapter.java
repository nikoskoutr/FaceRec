package com.apps.ktr.facerec;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

/**
 * Created by nikos on 7/4/16.
 * Project name: FaceRec.
 * File name: ${FILE_NAME}.
 * Developed with: Android Studio.
 */
public class CustomPagerAdapter extends PagerAdapter {
    private final Context mContext;

    public CustomPagerAdapter(Context context){
        mContext = context;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        PagerHelperEnum pagerEnum = PagerHelperEnum.values()[position];
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewGroup layout = (ViewGroup) inflater.inflate(pagerEnum.getLayoutResId(), container, false);
        setStatistics(position, layout);
        container.addView(layout);
        return layout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return PagerHelperEnum.values().length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        PagerHelperEnum pagerEnum = PagerHelperEnum.values()[position];
        return mContext.getString(pagerEnum.getTitleResId());
    }

    private void setStatistics(int position, ViewGroup layout){
        PagerHelperEnum pagerEnum = PagerHelperEnum.values()[position];
        String[] projection = null;
        String sortOder = null;
        int graphResId = 0;
        String graphTitle = null;
        switch (pagerEnum) {
            case TIME:
                graphResId = R.id.graphViewTime;
                projection = new String[]{
                        FaceRecContract.StatisticsEntry.COLUMN_NAME_ALGORITHM,
                        FaceRecContract.StatisticsEntry.COLUMN_NAME_NUMBERIMAGESTOTAL,
                        FaceRecContract.StatisticsEntry.COLUMN_NAME_TIMETRAIN};
                sortOder = FaceRecContract.StatisticsEntry.COLUMN_NAME_NUMBERIMAGESTOTAL + " ASC";
                graphTitle = "Train Time - Total Images";
                break;
            case TIMEPREDICT:
                graphResId = R.id.time_predict_graph;
                projection = new String[]{
                        FaceRecContract.StatisticsEntry.COLUMN_NAME_ALGORITHM,
                        FaceRecContract.StatisticsEntry.COLUMN_NAME_TIMEPREDICT,
                        FaceRecContract.StatisticsEntry.COLUMN_NAME_NUMBERIMAGESTOTAL};
                sortOder = FaceRecContract.StatisticsEntry.COLUMN_NAME_NUMBERIMAGESTOTAL + " ASC";
                graphTitle = "Predict Time - Total Images";
                break;
            case SUCCESSNUMBER:
                graphResId = R.id.graphViewSuccessNumber;
                projection = new String[] {
                        FaceRecContract.StatisticsEntry.COLUMN_NAME_ALGORITHM,
                        FaceRecContract.StatisticsEntry.COLUMN_NAME_NUMBERIMAGESID,
                        FaceRecContract.StatisticsEntry.COLUMN_NAME_SUCCESS,
                        FaceRecContract.StatisticsEntry.COLUMN_NAME_TOTAL_HITS};
                sortOder = FaceRecContract.StatisticsEntry.COLUMN_NAME_NUMBERIMAGESID + " ASC";
                graphTitle = "User Images Number - Success %";
                break;
        }
        SQLiteDatabase db = new FaceRecDbHelper(mContext).getWritableDatabase();
        Cursor c = db.query(
                FaceRecContract.StatisticsEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOder
        );
        if (c.moveToFirst()) {
            GraphView graph = (GraphView) layout.findViewById(graphResId);
            graph.setTitle(graphTitle);
            while (!c.isAfterLast()) {
                c.moveToNext();
            }
            LineGraphSeries<DataPoint> dataEigen = new LineGraphSeries<>();
            LineGraphSeries<DataPoint> dataFisher = new LineGraphSeries<>();
            LineGraphSeries<DataPoint> dataLBPH = new LineGraphSeries<>();
            c.moveToFirst();
            String algorithm = "";
            float success;
            DataPoint insert = null;
            while (!c.isAfterLast()) {
                switch (pagerEnum) {
                    case TIME:
                        float imagesNumber = c.getFloat(c.getColumnIndex(FaceRecContract.StatisticsEntry.COLUMN_NAME_NUMBERIMAGESTOTAL));
                        float trainTime = c.getFloat(c.getColumnIndex(FaceRecContract.StatisticsEntry.COLUMN_NAME_TIMETRAIN));
                        algorithm = c.getString(c.getColumnIndex(FaceRecContract.StatisticsEntry.COLUMN_NAME_ALGORITHM));
                        insert = new DataPoint(imagesNumber, trainTime);
                        break;
                    case TIMEPREDICT:
                        float imagesNumberTotal = c.getFloat(c.getColumnIndex(FaceRecContract.StatisticsEntry.COLUMN_NAME_NUMBERIMAGESTOTAL));
                        float predictTime = c.getFloat(c.getColumnIndex(FaceRecContract.StatisticsEntry.COLUMN_NAME_TIMEPREDICT));
                        algorithm = c.getString(c.getColumnIndex(FaceRecContract.StatisticsEntry.COLUMN_NAME_ALGORITHM));
                        insert = new DataPoint(imagesNumberTotal, predictTime);
                        break;
                    case SUCCESSNUMBER:
                        success = c.getFloat(c.getColumnIndex(FaceRecContract.StatisticsEntry.COLUMN_NAME_SUCCESS));
                        float hits = c.getFloat(c.getColumnIndex(FaceRecContract.StatisticsEntry.COLUMN_NAME_TOTAL_HITS));
                        float userImagesNum = c.getFloat(c.getColumnIndex(FaceRecContract.StatisticsEntry.COLUMN_NAME_NUMBERIMAGESID));
                        algorithm = c.getString(c.getColumnIndex(FaceRecContract.StatisticsEntry.COLUMN_NAME_ALGORITHM));
                        insert = new DataPoint(userImagesNum, success/hits);
                        break;
                }
                switch (algorithm){
                    case "eigenfaces":
                        dataEigen.appendData(insert, true, 60);
                        break;
                    case "fisherfaces":
                        dataFisher.appendData(insert, true, 60);
                        break;
                    case "LBPH":
                        dataLBPH.appendData(insert, true, 60);
                        break;
                    default:
                        insert = null;
                        break;
                }
                c.moveToNext();
            }

            if(!dataEigen.isEmpty()){
                graph.addSeries(dataEigen);
                dataEigen.setTitle("Eigenfaces");
                dataEigen.setColor(Color.RED);
                graph.getLegendRenderer().setVisible(true);
                graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
            }
            if(!dataFisher.isEmpty()){
                graph.addSeries(dataFisher);
                dataFisher.setTitle("Fisherfaces");
                dataFisher.setColor(Color.GREEN);
                graph.getLegendRenderer().setVisible(true);
                graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
            }
            if(!dataLBPH.isEmpty()){
                graph.addSeries(dataLBPH);
                dataLBPH.setTitle("LBPH");
                dataLBPH.setColor(Color.BLUE);
                graph.getLegendRenderer().setVisible(true);
                graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
            }
        }
        c.close();
        db.close();
    }
}
