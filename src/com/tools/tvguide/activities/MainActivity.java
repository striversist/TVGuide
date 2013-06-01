package com.tools.tvguide.activities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.tools.tvguide.managers.AppEngine;

import android.os.Bundle;
import android.app.TabActivity;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TabHost.OnTabChangeListener;

public class MainActivity extends TabActivity implements OnTabChangeListener, OnClickListener
{
    private static final String TAG = "MainActivity";
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
    
    private String          mStringHome;
    private String          mStringCollect;
    private String          mStringSearch;
    private String          mStringAbout;
    private String          mStringMore;
    
    private long mExitTime;
    
    private class TabContainer
    {
        public TabContainer(String id, LinearLayout tab, ImageView image, TextView text)
        {
            mTabId = id;
            mTab = tab;
            mImage = image;
            mText = text;
            mOrignTextColor = mText.getCurrentTextColor();
        }
        
        public String id()
        {
            return mTabId;
        }
        
        public void focus(boolean focus)
        {
            if (focus == true) {
                if (mTabId == mStringHome) {
                    mImage.setImageResource(R.drawable.icon_1_c);
                }
                else if (mTabId == mStringCollect) {
                    mImage.setImageResource(R.drawable.icon_2_c);
                }
                else if (mTabId == mStringSearch) {
                    mImage.setImageResource(R.drawable.icon_3_c);
                }
                else if (mTabId == mStringAbout) {
                    mImage.setImageResource(R.drawable.icon_4_c);
                }
                else if (mTabId == mStringMore) {
                    mImage.setImageResource(R.drawable.icon_5_c);
                }
                mText.setTextColor(Color.WHITE);
            } else {
                if (mTabId == mStringHome) {
                    mImage.setImageResource(R.drawable.icon_1_n);
                }
                else if (mTabId == mStringCollect) {
                    mImage.setImageResource(R.drawable.icon_2_n);
                }
                else if (mTabId == mStringSearch) {
                    mImage.setImageResource(R.drawable.icon_3_n);
                }
                else if (mTabId == mStringAbout) {
                    mImage.setImageResource(R.drawable.icon_4_n);
                }
                else if (mTabId == mStringMore) {
                    mImage.setImageResource(R.drawable.icon_5_n);
                }
                mText.setTextColor(mOrignTextColor);
            }
        }
        private String mTabId;
        private LinearLayout mTab;
        private ImageView mImage;
        private TextView mText;
        private int mOrignTextColor;
    }
    
    private List<TabContainer>  mTabList;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        Log.e(TAG, "onCreate this = " + this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTabHost = getTabHost();
        mTabList = new ArrayList<TabContainer>();
        
        mStringHome     = getResources().getString(R.string.category_home);
        mStringCollect  = getResources().getString(R.string.category_collect);
        mStringSearch   = getResources().getString(R.string.category_search);
        mStringAbout    = getResources().getString(R.string.category_about);
        mStringMore     = getResources().getString(R.string.category_more);
        
        mTabHome        = (LinearLayout)findViewById(R.id.tab_home);
        mTabCollect     = (LinearLayout)findViewById(R.id.tab_collect);
        mTabSearch      = (LinearLayout)findViewById(R.id.tab_search);
        mTabAbout       = (LinearLayout)findViewById(R.id.tab_about);
        mTabMore        = (LinearLayout)findViewById(R.id.tab_more);
        
        mImageHome      = (ImageView)findViewById(R.id.image_home);
        mImageCollect   = (ImageView)findViewById(R.id.image_collect);
        mImageSearch    = (ImageView)findViewById(R.id.image_search);
        mImageAbout     = (ImageView)findViewById(R.id.image_about);
        mImageMore      = (ImageView)findViewById(R.id.image_more);
        
        mTextHome       = (TextView)findViewById(R.id.text_home);
        mTextCollect    = (TextView)findViewById(R.id.text_collect);
        mTextSearch     = (TextView)findViewById(R.id.text_search);
        mTextAbout      = (TextView)findViewById(R.id.text_about);
        mTextMore       = (TextView)findViewById(R.id.text_more);
        
        mTabList.add(new TabContainer(mStringHome, mTabHome, mImageHome, mTextHome));
        mTabList.add(new TabContainer(mStringCollect, mTabCollect, mImageCollect, mTextCollect));
        mTabList.add(new TabContainer(mStringSearch, mTabSearch, mImageSearch, mTextSearch));
        mTabList.add(new TabContainer(mStringAbout, mTabAbout, mImageAbout, mTextAbout));
        mTabList.add(new TabContainer(mStringMore, mTabMore, mImageMore, mTextMore));
       
        mTabHost.addTab(mTabHost.newTabSpec(mStringHome)
                .setIndicator(getResources().getString(R.string.category_home))
                .setContent(new Intent(this, HomeActivity.class)));
               
        mTabHost.addTab(mTabHost.newTabSpec(mStringCollect)
                .setIndicator(getResources().getString(R.string.category_collect))
                .setContent(new Intent(this, CollectActivity.class)));
        
        mTabHost.addTab(mTabHost.newTabSpec(mStringSearch)
                .setIndicator(getResources().getString(R.string.category_search))
                .setContent(new Intent(this, SearchActivity.class)));
        
        mTabHost.addTab(mTabHost.newTabSpec(mStringAbout)
                .setIndicator(getResources().getString(R.string.category_about))
                .setContent(new Intent(this, AboutActivity.class)));
        
        mTabHost.addTab(mTabHost.newTabSpec(mStringMore)
                .setIndicator(getResources().getString(R.string.category_more))
                .setContent(new Intent(this, MoreActivity.class)));

        mTabHost.setOnTabChangedListener(this);
        
        Iterator<TabContainer> iterator = mTabList.iterator();
        while(iterator.hasNext())
        {
            TabContainer container = iterator.next();
            container.mTab.setOnClickListener(this);
            if (container.id() == mStringHome)
            {
                container.focus(true);
            }
        }
        
        AppEngine.getInstance().setContext(this);
        AppEngine.getInstance().setApplicationContext(getApplicationContext());
        AppEngine.getInstance().getLoginManager().startKeepAliveProcess();
    }

    @Override
    protected void onDestroy() 
    {
        AppEngine.getInstance().prepareBeforeExit();
    };
    
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
        Iterator<TabContainer> iterator = mTabList.iterator();
        while (iterator.hasNext())
        {
            TabContainer container = iterator.next();
            if (container.id() == tabId)
            {
                container.focus(true);
            }
            else 
            {
                container.focus(false);
            }
        }
    }
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) 
    {
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK)
        {
            if ((System.currentTimeMillis() - mExitTime) > 2000)
            {
                mExitTime = System.currentTimeMillis();
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            }
            else
            {
                finish();
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onClick(View view) 
    {
        switch (view.getId()) 
        {
            case R.id.tab_home:
                mTabHost.setCurrentTabByTag(mStringHome);
                break;
            case R.id.tab_collect:
                mTabHost.setCurrentTabByTag(mStringCollect);
                break;
            case R.id.tab_search:
                mTabHost.setCurrentTabByTag(mStringSearch);
                break;
            case R.id.tab_about:
                mTabHost.setCurrentTabByTag(mStringAbout);
                break;
            case R.id.tab_more:
                mTabHost.setCurrentTabByTag(mStringMore);
                break;
            default:
                break;
        }
    }
    
}
