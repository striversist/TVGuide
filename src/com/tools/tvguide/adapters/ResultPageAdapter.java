package com.tools.tvguide.adapters;

import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;

public class ResultPageAdapter extends PagerAdapter 
{
    @Override
    public void destroyItem(View arg0, int arg1, Object arg2) 
    {
        Log.d("k", "destroyItem");
    }

    @Override
    public void finishUpdate(View arg0) 
    {
        Log.d("k", "finishUpdate");
    }

    @Override
    public int getCount() 
    {
        Log.d("k", "getCount");
        return 0;
    }

    @Override
    public Object instantiateItem(View arg0, int arg1) 
    {
        Log.d("k", "instantiateItem");
        return null;
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) 
    {
        Log.d("k", "isViewFromObject");
        return arg0 == (arg1);
    }

    @Override
    public void restoreState(Parcelable arg0, ClassLoader arg1) 
    {
        Log.d("k", "restoreState");
    }

    @Override
    public Parcelable saveState() 
    {
        Log.d("k", "saveState");
        return null;
    }

    @Override
    public void startUpdate(View arg0) 
    {
        Log.d("k", "startUpdate");
    }

}
