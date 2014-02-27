package com.tools.tvguide.managers;

import com.tools.tvguide.components.Shutter;

import android.content.Context;
import android.content.SharedPreferences;

public class EnvironmentManager implements Shutter
{
    public static final boolean isDevelopMode          = false;
    public static final boolean enableACRA             = true;
    public static final boolean enableUninstallReport  = true;
    public static final int defaultChannelVersion      = 4;
    
    private Context mContext;
    private boolean mIsChannelDetailFromWeb;
    private boolean mIsAdEnable;
    private String  mHotSource;

    private SharedPreferences   mPreference;
    private static final String SHARE_PREFERENCES_NAME                 = "evnironment_settings";
    private static final String CHANNEL_DETAIL_FROM_WEB_FLAG           = "key_channel_detail_from_web_flag";
    private static final String AD_ENABLE_FLAG                         = "key_ad_enable_flag";
    private static final String HOT_SOURCE_FLAG                        = "key_hot_source_flag";
    
    public EnvironmentManager(Context context)
    {
        assert (context != null);
        mContext = context;
    }
    
	public void init()
	{
	    mPreference = mContext.getSharedPreferences(SHARE_PREFERENCES_NAME, Context.MODE_PRIVATE);
	    mIsChannelDetailFromWeb = mPreference.getBoolean(CHANNEL_DETAIL_FROM_WEB_FLAG, true);
	    mIsAdEnable = mPreference.getBoolean(AD_ENABLE_FLAG, true);
	    mHotSource = mPreference.getString(HOT_SOURCE_FLAG, "tvmao");
	}
	
	public boolean isChannelDetailFromWeb()
	{
	    return mIsChannelDetailFromWeb;
	}
	
	public boolean isAdEnable()
	{
	    return mIsAdEnable;
	}
	
	public boolean isHotFromTvmao()
	{
	    return mHotSource.equals("tvmao");
	}
	
	public void setChannelDetailFromWeb(boolean fromWeb)
	{
	    mIsChannelDetailFromWeb = fromWeb;
	}
	
	public void setAdEnable(boolean enable)
	{
	    mIsAdEnable = enable;
	}
	
	public void setHotSource(String source)
	{
	    mHotSource = source;
	}

    @Override
    public void onShutDown() 
    {
        mPreference.edit().putBoolean(CHANNEL_DETAIL_FROM_WEB_FLAG, mIsChannelDetailFromWeb).commit();
        mPreference.edit().putBoolean(AD_ENABLE_FLAG, mIsAdEnable).commit();
        mPreference.edit().putString(HOT_SOURCE_FLAG, mHotSource).commit();
    }
}
