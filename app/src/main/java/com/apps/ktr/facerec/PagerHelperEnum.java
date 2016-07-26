package com.apps.ktr.facerec;

/**
 * Created by nikos on 7/4/16.
 * Project name: FaceRec.
 * File name: ${FILE_NAME}.
 * Developed with: Android Studio.
 */
public enum PagerHelperEnum {
    TIME(R.string.timePageTitle, R.layout.train_time_graph),
    TIMEPREDICT(R.string.timePredictPageTitle, R.layout.time_predict_graph),
    SUCCESSNUMBER(R.string.successNumberPageTitle, R.layout.success_number_graph);

    private int mTitleResId;
    private int mLayoutResId;

    PagerHelperEnum(int titleResId, int layoutResId){
        mTitleResId = titleResId;
        mLayoutResId = layoutResId;
    }

    public int getTitleResId(){
        return mTitleResId;
    }

    public int getLayoutResId(){
        return mLayoutResId;
    }

}
