package com.tools.tvguide.managers;

import com.tools.tvguide.components.SplashDialog;
import android.content.Context;

public class BootManager 
{
    private Context         mContext;
    private SplashDialog    mSplashDialog;
    private boolean         mShowSplash     = true;
    
    public BootManager(Context context)
    {
        mContext = context;
    }
    
    public void start()
    {
        if (mShowSplash)
            showSplash();
        
        AppEngine.getInstance().getUrlManager().init(new UrlManager.OnInitCompleteCallback() 
        {
            @Override
            public void OnInitComplete(int result)
            {
                AppEngine.getInstance().getUpdateManager().checkUpdate();
                AppEngine.getInstance().getLoginManager().login();
            }
        });
        AppEngine.getInstance().getLoginManager().startKeepAliveProcess();
    }
    
    public void showSplash()
    {
        mSplashDialog = new SplashDialog(mContext);
        mSplashDialog.showSplash();
    }
    
    public void removeSplash()
    {
        if (mSplashDialog != null)
            mSplashDialog.checkTimeToRemove();
    }
    
    public void onSplashStarted()
    {
        removeSplash();
    }
    
    public void onSplashFinished()
    {
        mSplashDialog = null;
    }
}
