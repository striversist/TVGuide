package com.tools.tvguide.managers;

import net.youmi.android.banner.AdView;
import net.youmi.android.diy.banner.DiyAdSize;
import net.youmi.android.diy.banner.DiyBanner;

import android.app.Activity;
import android.content.Context;
import android.widget.RelativeLayout;

public class AdManager 
{
	public static final String TAG = "AdManager";
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
		net.youmi.android.AdManager.getInstance(mContext).init("18d3cb43223cb656", "ca026a5093fb2378", false);
		net.youmi.android.AdManager.getInstance(mContext).setEnableDebugLog(false);
	}
	
	public void addAdView(Activity activity, int id, AdSize size)
	{
		if (activity == null)
			return;
		
		RelativeLayout layout = (RelativeLayout) activity.findViewById(id);
		if (layout == null)
			return;
		
		switch (size) 
		{
			case MINI_SIZE:
				layout.addView(new DiyBanner(activity, DiyAdSize.SIZE_MATCH_SCREENx32));
				break;
			case NORMAL_SIZE:
				layout.addView(new AdView(activity, net.youmi.android.banner.AdSize.FIT_SCREEN));
				break;
		}
	}
}
