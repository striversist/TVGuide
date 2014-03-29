package com.tools.tvguide.activities;

import com.tools.tvguide.R;
import com.tools.tvguide.managers.AppEngine;
import com.tools.tvguide.managers.BootManager.OnStartedCallback;

import android.os.Bundle;
import android.os.Handler;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.Toast;

public class MainActivity extends TabActivity implements OnStartedCallback
{
    private TabHost         mTabHost;
    
    private RadioGroup      mTabGroup;
    
    private String          mStringHome;
    private String          mStringCollect;
    private String          mStringSearch;
    private String          mStringAbout;
    private String          mStringMore;
    private ImageView        mNewMsg;
    
    private long mExitTime;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
//        Log.d(TAG, "onCreate this = " + this);
        super.onCreate(savedInstanceState);
        
        // Must be set first
        AppEngine.getInstance().setContext(this);
        AppEngine.getInstance().getBootManager().start(this);
        
        // 延缓MainActivity组件的初始化（显示），否则在闪屏之前背景会闪出一下，影响体验
        int delayTime = 0;
        if (AppEngine.getInstance().getBootManager().isShowSplash())
            delayTime = 500;
        else
            delayTime = 0;
        
        new Handler().postDelayed(new Runnable() 
        {
            @Override
            public void run() 
            {
                config();
            }
        }, delayTime);
        
        AppEngine.getInstance().getAdManager().init(this);
    }
    
    private void config()
    {
        setContentView(R.layout.activity_main);
        mTabHost = getTabHost();
        mTabGroup = (RadioGroup)findViewById(R.id.tab_group);
        
        mStringHome     = getResources().getString(R.string.category_home);
        mStringCollect  = getResources().getString(R.string.category_collect);
        mStringSearch   = getResources().getString(R.string.category_search);
        mStringAbout    = getResources().getString(R.string.category_hot);
        mStringMore     = getResources().getString(R.string.category_more);
        mNewMsg         = (ImageView) findViewById(R.id.new_msg_tv);
       
        mTabHost.addTab(mTabHost.newTabSpec(mStringHome)
                .setIndicator(getResources().getString(R.string.category_home))
                .setContent(new Intent(this, HomeActivity.class)));
               
        mTabHost.addTab(mTabHost.newTabSpec(mStringCollect)
                .setIndicator(getResources().getString(R.string.category_collect))
                .setContent(new Intent(this, CollectActivity.class)));
        
        mTabHost.addTab(mTabHost.newTabSpec(mStringSearch)
                .setIndicator(getResources().getString(R.string.category_search))
                .setContent(new Intent(this, SearchActivity.class)));
        
        if (AppEngine.getInstance().getEnvironmentManager().isHotFromTvmao())
        {
            mTabHost.addTab(mTabHost.newTabSpec(mStringAbout)
                    .setIndicator(getResources().getString(R.string.category_hot))
                    .setContent(new Intent(this, HotActivity.class)));
        }
        else
        {
            mTabHost.addTab(mTabHost.newTabSpec(mStringAbout)
                    .setIndicator(getResources().getString(R.string.category_hot))
                    .setContent(new Intent(this, HotActivityTvsou.class)));
        }
        
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
                    case R.id.tab_hot:
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
    }
    
    @Override
    protected void onDestroy() 
    {
        super.onDestroy();
        AppEngine.getInstance().prepareBeforeExit();
        doOnDestroy();
    };

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
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        
        // 检测屏幕的方向：纵向或横向  
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            // 当前为横屏， 在此处添加额外的处理代码 
        }
        else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
        {
            // 当前为竖屏， 在此处添加额外的处理代码  
        }
    }
    
    public void onClick(View view)
    {
        switch (view.getId()) 
        {
            case R.id.tab_more:
                mTabGroup.check(R.id.tab_more);
                if (mNewMsg.getVisibility() == View.VISIBLE)
                    mNewMsg.setVisibility(View.INVISIBLE);
                break;
        }
    }
    
    public void doOnDestroy()
    {
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    public void OnUpdateInfo(boolean needUpdate) 
    {
        if (needUpdate)
        {
            if (!TextUtils.equals(mTabHost.getCurrentTabTag(), mStringMore))    // 当前Tab没有被选中时
                mNewMsg.setVisibility(View.VISIBLE);
        }
    }
}
