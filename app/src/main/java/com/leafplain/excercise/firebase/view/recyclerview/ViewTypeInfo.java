package com.leafplain.excercise.firebase.view.recyclerview;

/**
 * Created by kennethyeh on 2016/12/28.
 */

public class ViewTypeInfo {
    public int viewType;
    public Object dataObject;

    public ViewTypeInfo() {
    }

    public ViewTypeInfo(int viewType, Object dataObject) {
        this.viewType = viewType;
        this.dataObject = dataObject;
    }

}
