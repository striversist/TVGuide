package com.tools.tvguide.activities;

import com.tools.tvguide.managers.AppEngine;

import android.os.Bundle;
import android.app.TabActivity;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TabHost;
import android.widget.Toast;

public class MainActivity extends TabActivity
{
    private static final String TAG = "MainActivity";
    private TabHost         mTabHost;
    
    private RadioGroup     mTabGroup;
    
    private String          mStringHome;
    private String          mStringCollect;
    private String          mStringSearch;
    private String          mStringAbout;
    private String          mStringMore;
    
    private long mExitTime;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        Log.e(TAG, "onCreate this = " + this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTabHost = getTabHost();
        mTabGroup = (RadioGroup)findViewById(R.id.tab_group);
        
        mStringHome     = getResources().getString(R.string.category_home);
        mStringCollect  = getResources().getString(R.string.category_collect);
        mStringSearch   = getResources().getString(R.string.category_search);
        mStringAbout    = getResources().getString(R.string.category_about);
        mStringMore     = getResources().getString(R.string.category_more);
       
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

        mTabGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() 
        {        
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) 
            {
                switch (checkedId) 
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
        });
        
        
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
}
