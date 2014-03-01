package com.tools.tvguide.managers;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;

public class AdManager 
{
	public static final String TAG = "AdManager";
	private static final String ADMOB_ID = "a15306e4c0310af";
	public enum AdSize {MINI_SIZE, NORMAL_SIZE};
	
	private Context mContext;
	
	public AdManager(Context context)
	{
		assert (context != null);
		mContext = context;
	}
	
	public void init()
	{
		// 初始化应用的发布ID和密钥，以及设置测试模式
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
}
