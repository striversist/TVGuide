package com.tools.tvguide.managers;

import cn.waps.AppConnect;
import cn.waps.AppListener;
import cn.waps.UpdatePointsNotifier;
import cn.waps.ac;

import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.tools.tvguide.components.Shutter;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;

public class AdManager implements Shutter 
{
	public static final String TAG = "AdManager";
	private static final String ADMOB_ID = "a15306e4c0310af";
	public enum AdSize {MINI_SIZE, NORMAL_SIZE};
	
	private Context mContext;
	private Activity mMainActivity;
	
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
	}
	
	public void init(Activity activity)
	{
	    assert (activity != null);
	    mMainActivity = activity;
		// 初始化应用的发布ID和密钥，以及设置测试模式
	    AppConnect.getInstance("09f277ca386ee99cb4c910e09f562112", "default", mMainActivity);
	    AppConnect.getInstance(mMainActivity).setCrashReport(false);
	}
	
	/**
	 * @return: true on Success; false on Failed
	 */
	public boolean addAdView(Activity activity, int id, AdSize size)
	{
	    if (!AppEngine.getInstance().getEnvironmentManager().isAdEnable())
	        return false;
	    
		if (activity == null)
			return false;
		
		RelativeLayout layout = (RelativeLayout) activity.findViewById(id);
		if (layout == null)
			return false;
		
		switch (size) 
		{
			case MINI_SIZE:
			    AdView adViewMini = new AdView(activity, com.google.ads.AdSize.SMART_BANNER, ADMOB_ID);
			    layout.addView(adViewMini);
			    adViewMini.loadAd(new AdRequest());
				break;
			case NORMAL_SIZE:
			    AdView adView = new AdView(activity, com.google.ads.AdSize.BANNER, ADMOB_ID);
                layout.addView(adView);
                adView.loadAd(new AdRequest());
				break;
		}
		layout.setVisibility(View.VISIBLE);
		return true;
	}
	
	/**
	 * 显示积分墙
	 */
	public void showOffers(Activity activity)
	{
	    if (activity == null)
	        return;
	    
	    AppConnect.getInstance(mMainActivity).showOffers(mMainActivity);
	    AppConnect.getInstance(mMainActivity).setOffersCloseListener(new AppListener()
	    {
	        @Override
	        public void onOffersClose()
	        {
	        }
	    });
	}
	
	public void getPointsAsync(final GetPointsCallback callback)
	{
	    if (callback == null)
	        return;
	    
	    AppConnect.getInstance(mMainActivity).getPoints(new UpdatePointsNotifier() 
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
	
	public void spendPoints(int amount, final SpendPointsCallback callback )
	{
	    if (amount <= 0 || callback == null)
	        return;
	    
	    AppConnect.getInstance(mMainActivity).spendPoints(amount, new UpdatePointsNotifier() {
            
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

    @Override
    public void onShutDown() 
    {
        AppConnect.getInstance(mMainActivity).close();
    }
}
