package com.tools.tvguide.activities;

import com.tools.tvguide.R;
import com.tools.tvguide.adapters.ResultPageAdapter;

import android.os.Bundle;
import android.app.Activity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

public class HotActivity2 extends Activity 
{
    private LayoutInflater mInflater;
    private ViewPager mViewPager;
    private RadioGroup mTabsGroup;
    private ResultPageAdapter mPageAdapter;
    
    private final int TAB_INDEX_DRAMA = 0;
    private final int TAB_INDEX_TVCOLUMN = 1;
    private final int TAB_INDEX_MOVIE = 2;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hot2);
        
        mInflater = LayoutInflater.from(this);
        mViewPager = (ViewPager) findViewById(R.id.hot_view_pager);
        mTabsGroup = (RadioGroup) findViewById(R.id.hot_tabs_group);
        
        mPageAdapter = new ResultPageAdapter();
        for (int i=0; i<mTabsGroup.getChildCount(); ++i)
        {
            LinearLayout loadingLayout = (LinearLayout)mInflater.inflate(R.layout.center_text_tips, null);
            ((TextView) loadingLayout.findViewById(R.id.center_tips_text_view)).setText(getResources().getString(R.string.loading_string));
            mPageAdapter.addView(loadingLayout);
        }
        mViewPager.setAdapter(mPageAdapter);
        
        mViewPager.setOnPageChangeListener(new OnPageChangeListener() 
        {
            @Override
            public void onPageSelected(int position) 
            {
                if (position == TAB_INDEX_DRAMA)
                    mTabsGroup.check(R.id.hot_tab_drama);
                else if (position == TAB_INDEX_TVCOLUMN)
                    mTabsGroup.check(R.id.hot_tab_tvcolumn);
                else if (position == TAB_INDEX_MOVIE)
                    mTabsGroup.check(R.id.hot_tab_movie);
            }
            
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) 
            {
            }
            
            @Override
            public void onPageScrollStateChanged(int state) 
            {
            }
        });
    }

    public void onClickTabs(View view)
    {
        switch (view.getId())
        {
            case R.id.hot_tab_drama:
                mViewPager.setCurrentItem(TAB_INDEX_DRAMA);
                break;
            case R.id.hot_tab_tvcolumn:
                mViewPager.setCurrentItem(TAB_INDEX_TVCOLUMN);
                break;
            case R.id.hot_tab_movie:
                mViewPager.setCurrentItem(TAB_INDEX_MOVIE);
                break;
        }
    }
}
