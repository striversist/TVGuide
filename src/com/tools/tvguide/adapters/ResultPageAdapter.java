package com.tools.tvguide.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

public class ResultPageAdapter extends PagerAdapter 
{
    private List<View> mViewLists = new ArrayList<View>();
    
    public void addView(View view)
    {
        mViewLists.add(view);
    }
        
    @Override
    public void destroyItem(View arg0, int arg1, Object arg2) 
    {
        Log.d("ResultPageAdapter", "destroyItem");
        ((ViewPager) arg0).removeView(mViewLists.get(arg1));
    }

    @Override
    public void finishUpdate(View arg0) 
    {
        Log.d("ResultPageAdapter", "finishUpdate");
    }

    @Override
    public int getCount() 
    {
        Log.d("ResultPageAdapter", "getCount");
        return mViewLists.size();
    }

    @Override
    public Object instantiateItem(View arg0, int arg1) 
    {
        Log.d("ResultPageAdapter", "instantiateItem");
        ((ViewPager) arg0).addView(mViewLists.get(arg1), 0);
        return mViewLists.get(arg1);
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) 
    {
        Log.d("ResultPageAdapter", "isViewFromObject");
        return arg0 == (arg1);
    }

    @Override
    public void restoreState(Parcelable arg0, ClassLoader arg1) 
    {
        Log.d("ResultPageAdapter", "restoreState");
    }

    @Override
    public Parcelable saveState() 
    {
        Log.d("ResultPageAdapter", "saveState");
        return null;
    }

    @Override
    public void startUpdate(View arg0) 
    {
        Log.d("ResultPageAdapter", "startUpdate");
    }

}
