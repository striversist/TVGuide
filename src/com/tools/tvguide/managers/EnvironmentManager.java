package com.tools.tvguide.managers;

import java.io.File;

import com.tools.tvguide.components.Shutter;
import com.tools.tvguide.utils.Utility;

import android.content.Context;
import android.content.SharedPreferences;

public class EnvironmentManager implements Shutter
{
    public static final boolean isDevelopMode          = false;
    public static final boolean enableACRA             = true;
    public static final boolean enableACRALog          = false;
    public static final boolean enableUninstallReport  = true;
    
    private Context mContext;
    private boolean mIsChannelDetailFromWeb;
    private boolean mIsAdEnable;
    private boolean mIsAdEnablePermanent;
    private String  mHotSource;

    private SharedPreferences   mPreference;
    private static final String SHARE_PREFERENCES_NAME                 = "evnironment_settings";
    private static final String CHANNEL_DETAIL_FROM_WEB_FLAG           = "key_channel_detail_from_web_flag";
    private static final String AD_ENABLE_FLAG                         = "key_ad_enable_flag";
    private static final String AD_ENABLE_PERMANENT_FLAG               = "key_ad_enable_permanent_flag";
    private static final String HOT_SOURCE_FLAG                        = "key_hot_source_flag";
    
    public EnvironmentManager(Context context)
    {
        assert (context != null);
        mContext = context;
    }
    
	public void init()
	{
	    loadExternalPreference();
	    
	    mPreference = mContext.getSharedPreferences(SHARE_PREFERENCES_NAME, Context.MODE_PRIVATE);
	    mIsChannelDetailFromWeb = mPreference.getBoolean(CHANNEL_DETAIL_FROM_WEB_FLAG, true);
	    mIsAdEnable = mPreference.getBoolean(AD_ENABLE_FLAG, true);
	    mIsAdEnablePermanent = mPreference.getBoolean(AD_ENABLE_PERMANENT_FLAG, true);
	    mHotSource = mPreference.getString(HOT_SOURCE_FLAG, "tvmao");
	}
	
	public boolean isChannelDetailFromWeb()
	{
	    return mIsChannelDetailFromWeb;
	}
	
	public boolean isAdEnable()
	{
	    return mIsAdEnable && mIsAdEnablePermanent;
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
	
	/**
	 * 用于用户攒积金币永久关闭广告
	 */
	public void setAdEnablePermanent(boolean enable)
	{
	    mIsAdEnablePermanent = enable;
	}
	
	public void setHotSource(String source)
	{
	    mHotSource = source;
	}
	
	private void saveExternalPrefernce()
	{
	    File file = new File(mContext.getExternalFilesDir(null), SHARE_PREFERENCES_NAME);
	    Utility.saveSharedPreferencesToFile(SHARE_PREFERENCES_NAME, file);
	}
	
	private void loadExternalPreference()
	{
	    File file = new File(mContext.getExternalFilesDir(null), SHARE_PREFERENCES_NAME);
	    Utility.loadSharedPreferencesFromFile(SHARE_PREFERENCES_NAME, file);
	}

    @Override
    public void onShutDown() 
    {
        mPreference.edit().putBoolean(CHANNEL_DETAIL_FROM_WEB_FLAG, mIsChannelDetailFromWeb).commit();
        mPreference.edit().putBoolean(AD_ENABLE_FLAG, mIsAdEnable).commit();
        mPreference.edit().putBoolean(AD_ENABLE_PERMANENT_FLAG, mIsAdEnable).commit();
        mPreference.edit().putString(HOT_SOURCE_FLAG, mHotSource).commit();
        saveExternalPrefernce();
    }
}
