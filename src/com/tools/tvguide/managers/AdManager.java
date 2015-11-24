package com.tools.tvguide.managers;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.LinearLayout;

import com.tools.tvguide.ad.AppConnect;
import com.tools.tvguide.ad.AppListener;
import com.tools.tvguide.ad.UpdatePointsListener;
import com.tools.tvguide.components.Shutter;

public class AdManager implements Shutter 
{
	public static final String TAG = "AdManager";
	
    private static final String SHARE_PREFERENCES_NAME              = "admanager_settings";
    private static final String DISABLE_AD_TILL_TIME_FLAG           = "key_disable_ad_till_time_flag";
	
    private SharedPreferences   mPreference;
	public enum AdSize {MINI_SIZE, NORMAL_SIZE};
	
	private Context mContext;
	
	public interface GetPointsCallback
	{
	    public void onUpdatePoints(String currencyName, int points);
	    public void onUpdatePointsFailed(String error);
	}
	
	public interface SpendPointsCallback
	{
	    public void onUpdatePoints(String currencyName, int points);
        public void onUpdatePointsFailed(String error); 
	}
	
	public AdManager(Context context)
	{
		assert (context != null);
		mContext = context;
		mPreference = mContext.getSharedPreferences(SHARE_PREFERENCES_NAME, Context.MODE_PRIVATE);
	}
	
	public static boolean isTimeToShowAd() {
	    return AppEngine.getInstance().getBootManager().getStartupTimes() 
	            > EnvironmentManager.AD_OPEN_AFTER_STARTUP_TIMES;
	}
	
	public void init(Activity activity)
	{
	    assert (activity != null);
	    
		// 初始化应用的发布ID和密钥，以及设置测试模式
	    // 方式1：通过代码设置APP_ID, APP_PID
//	    AppConnect.getInstance("1fc85ef0cd8d2b0206b2cb481cd25d69", "default", activity);
	    // 方式2：从AndroidManifest.xml中读取
	    AppConnect.getInstance(activity);
	    
	    AppConnect.getInstance(mContext).setCrashReport(false);
	    AppConnect.getInstance(mContext).initPopAd(mContext);
	    AppConnect.getInstance(mContext).setPopAdBack(true);
	    AppConnect.getInstance(mContext).initUninstallAd(mContext);
	}
	
	/**
	 * @return: true on Success; false on Failed
	 */
	public boolean addAdView(Activity activity, int id, AdSize size)
	{
	    if (!AppEngine.getInstance().getEnvironmentManager().isAdEnable())
	        return false;
	    
        if (isTimeToShowAd()) 
            return false;
	    
	    if (System.currentTimeMillis() < getDisableAdTillTime())
	        return false;
	    
		if (activity == null)
			return false;
		
		LinearLayout layout = (LinearLayout) activity.findViewById(id);
		if (layout == null)
			return false;
		
		switch (size) 
		{
			case MINI_SIZE:
			    AppConnect.getInstance(activity).showMiniAd(activity, layout, 10);   // 10秒刷新一次
				break;
			case NORMAL_SIZE:
			    AppConnect.getInstance(activity).showBannerAd(activity, layout);
				break;
		}
		layout.setVisibility(View.VISIBLE);
		return true;
	}
	
	public void showPopAd(Context context) {
	    AppConnect.getInstance(mContext).showPopAd(context);
	}
	
	public void dissmissPopAd(Context context) {
	    Dialog popDialog = AppConnect.getInstance(mContext).getPopAdDialog();
	    if (popDialog != null && popDialog.isShowing()) {
	        popDialog.dismiss();
	    }
	}
	
	public void removeAd()
	{
	    AppEngine.getInstance().getEnvironmentManager().setAdEnablePermanent(false);
	}
	
	/**
	 * 获取广告重新打开的时间（单位：毫秒）
	 */
	public long getDisableAdTillTime()
	{
	    return mPreference.getLong(DISABLE_AD_TILL_TIME_FLAG, 0L);
	}
	
	/**
	 * 增加屏蔽广告的时间（单位：毫秒）
	 */
	public void addDisableAddDuration(long duration)
	{
	    if (duration < 0)
	        return;
	    
	    long time = getDisableAdTillTime();
	    if (time == 0) { // 第一次
	        time = System.currentTimeMillis();
	    }
	    mPreference.edit().putLong(DISABLE_AD_TILL_TIME_FLAG, time + duration).commit();
	}
	
	/**
	 * 显示积分墙
	 */
	public void showOffers(Activity activity)
	{
	    if (activity == null)
	        return;
	    
	    AppConnect.getInstance(mContext).showOffers(activity);
	    AppConnect.getInstance(mContext).setOffersCloseListener(new AppListener()
	    {
	        @Override
	        public void onOffersClose()
	        {
	        }
	    });
	}
	
	public void getPointsAsync(Activity activity, final GetPointsCallback callback)
	{
	    if (activity == null || callback == null)
	        return;
	    
	    AppConnect.getInstance(activity).getPoints(new UpdatePointsListener() 
	    {
            @Override
            public void getUpdatePointsFailed(String error) {
                callback.onUpdatePointsFailed(error);
            }
            
            @Override
            public void getUpdatePoints(String currencyName, int points) {
                callback.onUpdatePoints(currencyName, points);
            }
        });
	}
	
	public void spendPoints(Activity activity, int amount, final SpendPointsCallback callback)
	{
	    if (activity == null || amount <= 0 || callback == null)
	        return;
	    
	    AppConnect.getInstance(activity).spendPoints(amount, new UpdatePointsListener() {
            
            @Override
            public void getUpdatePointsFailed(String error) {
                callback.onUpdatePointsFailed(error);
            }
            
            @Override
            public void getUpdatePoints(String currencyName, int points) {
                callback.onUpdatePoints(currencyName, points);
            }
        });
	}
	
	public void showFeedback(Activity activity) {
	    AppConnect.getInstance(mContext).showFeedback(activity);
	}

    @Override
    public void onShutDown() 
    {
        AppConnect.getInstance(mContext).close();
    }
}
