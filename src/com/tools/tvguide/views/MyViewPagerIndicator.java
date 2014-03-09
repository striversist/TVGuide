package com.tools.tvguide.views;

import java.util.ArrayList;
import java.util.List;

import com.tools.tvguide.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

public class MyViewPagerIndicator extends RelativeLayout implements View.OnClickListener 
{
    private LinearLayout mTabsLinearLayout;
    private List<RadioButton> mTabList;
    private int mCurIndex = -1;
    private OnTabClickListener mListener;
    
    public interface OnTabClickListener
    {
        public void onTabClick(int index, Object tag);
    }

    public MyViewPagerIndicator(Context context, AttributeSet attrs) 
    {
        super(context, attrs);
        
        LayoutInflater.from(context).inflate(R.layout.viewpager_indicator_layout, this);
        
        mTabsLinearLayout = (LinearLayout) findViewById(R.id.tabs_ll);
        mTabList = new ArrayList<RadioButton>();
    }
    
    public void addTab(String text, Object tag)
    {
        if (mTabsLinearLayout.getChildCount() > 0)
            mTabsLinearLayout.addView(createDivider());
        
        RadioButton tab = createTab(text);
        tab.setTag(tag);
        tab.setOnClickListener(this);
        
        mTabsLinearLayout.addView(tab);
        mTabList.add(tab);
    }
    
    public int getTabsCount()
    {
        return mTabList.size();
    }
    
    public void reset()
    {
        mTabsLinearLayout.removeAllViews();
        mTabList.clear();
        mCurIndex = -1;
    }
    
    public void setCurrentTab(int index)
    {
        if (index < 0 || index > mTabList.size() - 1)
            return;
        
        mCurIndex = index;
        refresh();
    }
    
    public void setOnTabClickListener(OnTabClickListener listener)
    {
        assert (listener != null);
        mListener = listener;
    }
    
    @Override
    public void onClick(View view) 
    {
        for (int i=0; i<mTabList.size(); ++i)
        {
            if (mTabList.get(i).equals(view))
            {
                mCurIndex = i;
                refresh();
                if (mListener != null)
                    mListener.onTabClick(i, mTabList.get(i).getTag());
                    
                break;
            }
        }   
    }
    
    private RadioButton createTab(String text)
    {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
        RadioButton button = (RadioButton) LayoutInflater.from(getContext()).inflate(R.layout.viewpager_indicator_button, null).getRootView();
        button.setLayoutParams(params);
        button.setText(text);
        
        return button;
    }
    
    private ImageView createDivider()
    {
        ImageView divider = (ImageView) LayoutInflater.from(getContext()).inflate(R.layout.viewpager_indicator_divider, null).getRootView();
        divider.setLayoutParams(new LayoutParams(1, LayoutParams.MATCH_PARENT));
        
        return divider;
    }
    
    private void refresh()
    {
        for (int i=0; i<mTabList.size(); ++i)
        {
            if (i == mCurIndex)
                mTabList.get(i).setChecked(true);
            else
                mTabList.get(i).setChecked(false);
        }
    }
}
