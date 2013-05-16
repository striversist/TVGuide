package com.tools.tvguide.managers;

import android.content.Context;

public class AppEngine 
{
    private static final String                     TAG                         = "AppEngine";
    private static AppEngine                        mInstance                   = new AppEngine();
    private Context                                 mContext;
    private Context                                 mApplicationContext;
    private UserSettingManager                      mUserSettingManager;
    
    /********************************* Manager定义区，所有受AppEngine管理的Manger统一定义 **********************************/
    
    public static AppEngine getInstance()
    {
        return mInstance;
    }
    
    public void setContext(Context context)
    {
        mContext = context;
    }

    public void setApplicationContext(Context context)
    {
        mApplicationContext = context;
    }

    public Context getApplicationContext()
    {
        return mApplicationContext;
    }

    public Context getContext()
    {
        return mContext;
    }
    
    public UserSettingManager getUserSettingManager()
    {
        if (mUserSettingManager == null)
            mUserSettingManager = new UserSettingManager(mContext);
        return mUserSettingManager;
    }
    
    public void prepareBeforeExit()
    {
        mUserSettingManager.shutDown();
    }
    
}
