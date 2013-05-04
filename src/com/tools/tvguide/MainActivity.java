package com.tools.tvguide;

import android.os.Bundle;
import android.R.integer;
import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.ViewFlipper;
import android.widget.TabHost.OnTabChangeListener;

public class MainActivity extends TabActivity implements OnTabChangeListener, OnClickListener
{
    private TabHost         mTabHost;
    
    private LinearLayout    mTabHome;
    private LinearLayout    mTabCollect;
    private LinearLayout    mTabSearch;
    private LinearLayout    mTabAbout;
    private LinearLayout    mTabMore;
    
    private ImageView       mImageHome;
    private ImageView       mImageCollect;
    private ImageView       mImageSearch;
    private ImageView       mImageAbout;
    private ImageView       mImageMore;
    
    private TextView        mTextHome;
    private TextView        mTextCollect;
    private TextView        mTextSearch;
    private TextView        mTextAbout;
    private TextView        mTextMore;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTabHost = getTabHost();
        
        mTabHome = (LinearLayout)findViewById(R.id.tab_home);
        mTabCollect = (LinearLayout)findViewById(R.id.tab_collect);
        mTabSearch = (LinearLayout)findViewById(R.id.tab_search);
        mTabAbout = (LinearLayout)findViewById(R.id.tab_about);
        mTabMore = (LinearLayout)findViewById(R.id.tab_more);
        
        mImageHome = (ImageView)findViewById(R.id.image_home);
        mImageCollect = (ImageView)findViewById(R.id.image_collect);
        mImageSearch = (ImageView)findViewById(R.id.image_search);
        mImageAbout = (ImageView)findViewById(R.id.image_about);
        mImageMore = (ImageView)findViewById(R.id.image_more);
        
        mTextHome = (TextView)findViewById(R.id.text_home);
        mTextCollect = (TextView)findViewById(R.id.text_collect);
        mTextSearch = (TextView)findViewById(R.id.text_search);
        mTextAbout = (TextView)findViewById(R.id.text_about);
        mTextMore = (TextView)findViewById(R.id.text_more);

        mTabHome.setOnClickListener(this);
        mTabCollect.setOnClickListener(this);
        mTabSearch.setOnClickListener(this);
        mTabAbout.setOnClickListener(this);
        mTabMore.setOnClickListener(this);
        
        mTabHost.addTab(mTabHost.newTabSpec(getResources().getString(R.string.category_home))
                .setIndicator(getResources().getString(R.string.category_home))
                .setContent(new Intent(this, HomeActivity.class)));
               
        mTabHost.addTab(mTabHost.newTabSpec(getResources().getString(R.string.category_collect))
                .setIndicator(getResources().getString(R.string.category_collect))
                .setContent(new Intent(this, CollectActivity.class)));
        
        mTabHost.addTab(mTabHost.newTabSpec(getResources().getString(R.string.category_search))
                .setIndicator(getResources().getString(R.string.category_search))
                .setContent(new Intent(this, SearchActivity.class)));
        
        mTabHost.addTab(mTabHost.newTabSpec(getResources().getString(R.string.category_about))
                .setIndicator(getResources().getString(R.string.category_about))
                .setContent(new Intent(this, AboutActivity.class)));
        
        mTabHost.addTab(mTabHost.newTabSpec(getResources().getString(R.string.category_more))
                .setIndicator(getResources().getString(R.string.category_more))
                .setContent(new Intent(this, MoreActivity.class)));
        
        mTabHost.setOnTabChangedListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public void onTabChanged(String tabId) 
    {
        mImageHome.setImageResource(R.drawable.icon_1_n);
        mImageCollect.setImageResource(R.drawable.icon_2_n);
        mImageSearch.setImageResource(R.drawable.icon_3_n);
        mImageAbout.setImageResource(R.drawable.icon_4_n);
        mImageMore.setImageResource(R.drawable.icon_5_n);
        if (tabId == getResources().getString(R.string.category_home))
        {
            mImageHome.setImageResource(R.drawable.icon_1_c);
        }
        else if (tabId == getResources().getString(R.string.category_collect))
        {
            mImageCollect.setImageResource(R.drawable.icon_2_c);
        }
        else if (tabId == getResources().getString(R.string.category_search))
        {
            mImageSearch.setImageResource(R.drawable.icon_3_c);
        }
        else if (tabId == getResources().getString(R.string.category_about))
        {
            mImageAbout.setImageResource(R.drawable.icon_4_c);
        }
        else if (tabId == getResources().getString(R.string.category_more))
        {
            mImageMore.setImageResource(R.drawable.icon_5_c);
        }
    }

    @Override
    public void onClick(View view) 
    {
        switch (view.getId()) 
        {
            case R.id.tab_home:
                mTabHost.setCurrentTabByTag(getResources().getString(R.string.category_home));
                break;
            case R.id.tab_collect:
                mTabHost.setCurrentTabByTag(getResources().getString(R.string.category_collect));
                break;
            case R.id.tab_search:
                mTabHost.setCurrentTabByTag(getResources().getString(R.string.category_search));
                break;
            case R.id.tab_about:
                mTabHost.setCurrentTabByTag(getResources().getString(R.string.category_about));
                break;
            case R.id.tab_more:
                mTabHost.setCurrentTabByTag(getResources().getString(R.string.category_more));
                break;
            default:
                break;
        }
    }
    
}
