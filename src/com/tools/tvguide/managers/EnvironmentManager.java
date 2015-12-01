package com.tools.tvguide.managers;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;

import com.tools.tvguide.components.Shutter;
import com.tools.tvguide.utils.Utility;

public class EnvironmentManager implements Shutter
{
    public static final boolean isDevelopMode          = false;
    public static final boolean enableACRA             = false;
    public static final boolean enableACRALog          = false;
    public static final boolean enableUninstallReport  = false;
    public static final int AD_OPEN_AFTER_STARTUP_TIMES = 3;
    public static boolean useLocalTime = false;
    
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
	    loadNetworkTime();
	    
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
	    mPreference.edit().putBoolean(AD_ENABLE_PERMANENT_FLAG, mIsAdEnablePermanent).commit();
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
	
	private void loadNetworkTime() {
	    // 使用网络时间
	    final StringBuffer buffer = new StringBuffer();
        AppEngine.getInstance().getContentManager().loadNowTimeFromNetwork(buffer, new ContentManager.LoadListener() {    
            @Override
            public void onLoadFinish(int status) {
                try {
                    Date date = new Date(Long.valueOf(buffer.toString()));
                    
                    // 优化：判断下次是否使用本地时间替代网络时间，以加快“正在播出”节目的显示
                    long proxyHour = date.getHours();
                    long proxyMinute = date.getMinutes();
                    int localHour = Calendar.getInstance().getTime().getHours();
                    int localMinute = Calendar.getInstance().getTime().getMinutes();
                    if (Math.abs((proxyHour * 60 + proxyMinute) - (localHour * 60 + localMinute)) < 10) // 相差在10分钟以内
                        useLocalTime = true;
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }
            }
        });
	}

    @Override
    public void onShutDown() 
    {
        mPreference.edit().putBoolean(CHANNEL_DETAIL_FROM_WEB_FLAG, mIsChannelDetailFromWeb).commit();
        mPreference.edit().putBoolean(AD_ENABLE_FLAG, mIsAdEnable).commit();
        mPreference.edit().putBoolean(AD_ENABLE_PERMANENT_FLAG, mIsAdEnablePermanent).commit();
        mPreference.edit().putString(HOT_SOURCE_FLAG, mHotSource).commit();
        saveExternalPrefernce();
    }
}
