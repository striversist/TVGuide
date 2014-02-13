package com.tools.tvguide.managers;

import java.util.ArrayList;
import java.util.List;

import com.tools.tvguide.components.Shutter;
import com.tools.tvguide.utils.MyApplication;

import android.content.Context;

public class AppEngine 
{
    private static final String                     TAG                         = "AppEngine";
    private static AppEngine                        mInstance;
    private Context                                 mContext;
    private Context                                 mApplicationContext;
    private List<Shutter>							mShutterList				= new ArrayList<Shutter>();
    private CollectManager                          mUserSettingManager;
    private LoginManager                            mLoginManager;
    private ContentManager                          mContentManager;
    private CacheManager                            mCacheManager;
    private AlarmHelper                             mAlarmHelper;
    private UrlManager                              mUrlManager;
    private DnsManager                              mDnsManager;
    private UpdateManager                           mUpdateManager;
    private BootManager                             mBootManager;
    private AdManager								mAdManager;
    private HotHtmlManager                          mHotHtmlManager;
    private ChannelHtmlManager                      mChannelHtmlManager;
    private ProgramHtmlManager                      mProgramHtmlManager;
    private UninstallMonitor                        mUninstallMonitor;
    private ServiceManager                          mServiceManager;
    
    /********************************* Manager定义区，所有受AppEngine管理的Manger统一定义 **********************************/
    
    public static AppEngine getInstance()
    {
        if (mInstance == null)
            mInstance = new AppEngine();
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
    
    private void checkInitialized()
    {
        if (mApplicationContext == null)
            mApplicationContext = MyApplication.getInstance().getApplicationContext();
    }
    
    public CollectManager getCollectManager()
    {
        checkInitialized();
        if (mUserSettingManager == null)
        {
            mUserSettingManager = new CollectManager(mApplicationContext);
            mShutterList.add(mUserSettingManager);
        }
        return mUserSettingManager;
    }
    
    public LoginManager getLoginManager()
    {
        checkInitialized();
        if (mLoginManager == null)
            mLoginManager = new LoginManager(mApplicationContext);
        return mLoginManager;
    }
    
    public ContentManager getContentManager()
    {
        checkInitialized();
        if (mContentManager == null)
        {
            mContentManager = new ContentManager(mApplicationContext);
            mShutterList.add(mContentManager);
        }
        return mContentManager;
    }
    
    public CacheManager getCacheManager()
    {
        checkInitialized();
        if (mCacheManager == null)
            mCacheManager = new CacheManager(mApplicationContext);
        return mCacheManager;
    }
    
    public AlarmHelper getAlarmHelper()
    {
        checkInitialized();
        if (mAlarmHelper == null)
        {
            mAlarmHelper = new AlarmHelper(mApplicationContext);
            mShutterList.add(mAlarmHelper);
        }
        return mAlarmHelper;
    }
    
    public UrlManager getUrlManager()
    {
        checkInitialized();
        if (mUrlManager == null)
            mUrlManager = new UrlManager(mApplicationContext);
        return mUrlManager;
    }
    
    public DnsManager getDnsManager()
    {
        checkInitialized();
        if (mDnsManager == null)
            mDnsManager = new DnsManager(mApplicationContext);
        return mDnsManager;
    }
    
    public UpdateManager getUpdateManager()
    {
        checkInitialized();
        if (mUpdateManager == null)
            mUpdateManager = new UpdateManager(mApplicationContext);
        return mUpdateManager;
    }
    
    public BootManager getBootManager()
    {
        checkInitialized();
        if (mBootManager == null)
        {
            mBootManager = new BootManager(mApplicationContext);
            mShutterList.add(mBootManager);
        }
        return mBootManager;
    }
    
    public AdManager getAdManager()
    {
    	checkInitialized();
    	if (mAdManager == null)
    		mAdManager = new AdManager(mApplicationContext);
    	return mAdManager;
    }
    
    public HotHtmlManager getHotHtmlManager()
    {
        checkInitialized();
        if (mHotHtmlManager == null)
            mHotHtmlManager = new HotHtmlManager(mApplicationContext);
        return mHotHtmlManager;
    }
    
    public ChannelHtmlManager getChannelHtmlManager()
    {
        checkInitialized();
        if (mChannelHtmlManager == null)
            mChannelHtmlManager = new ChannelHtmlManager(mApplicationContext);
        return mChannelHtmlManager;
    }
    
    public ProgramHtmlManager getProgramHtmlManager()
    {
        checkInitialized();
        if (mProgramHtmlManager == null)
            mProgramHtmlManager = new ProgramHtmlManager(mApplicationContext);
        return mProgramHtmlManager;
    }
    
    public UninstallMonitor getUninstallMonitor()
    {
        checkInitialized();
        if (mUninstallMonitor == null)
        {
            mUninstallMonitor = new UninstallMonitor(mApplicationContext);
            mShutterList.add(mUninstallMonitor);
        }
        return mUninstallMonitor;
    }
    
    public ServiceManager getServiceManager()
    {
        checkInitialized();
        if (mServiceManager == null)
            mServiceManager = new ServiceManager(mApplicationContext);
        return mServiceManager;
    }
    
    public void prepareBeforeExit()
    {        
    	for (Shutter shutter : mShutterList)
    	{
    		if (shutter == null)
    			continue;
    		shutter.onShutDown();
    	}
    	
        exit();
    }
    
    public void exit()
    {
        mInstance = null;
    }
}
