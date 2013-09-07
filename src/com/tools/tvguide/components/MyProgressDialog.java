package com.tools.tvguide.components;

import android.app.ProgressDialog;
import android.content.Context;


public class MyProgressDialog extends ProgressDialog 
{
    public MyProgressDialog(Context context) 
    {
        super(context);
        setProgressStyle(0);
        setMessage("数据载入中，请稍后...");
    }
}
