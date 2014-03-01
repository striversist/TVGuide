package com.tools.tvguide.managers;

import android.app.Activity;
import android.content.Context;
import android.view.View;
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
				break;
			case NORMAL_SIZE:
				break;
		}
		layout.setVisibility(View.VISIBLE);
		return true;
	}
}
