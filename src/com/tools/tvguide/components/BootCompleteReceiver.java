package com.tools.tvguide.components;

import com.tools.tvguide.managers.AppEngine;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompleteReceiver extends BroadcastReceiver
{
    private static final String TAG = "BootCompleteReceiver";
    private Context mContext;
    
	@Override
	public void onReceive(Context context, Intent intent) 
	{
	    mContext = context;
	    
		final String action = intent.getAction();
		Log.d("BootCompleteReceiver::onReceive", action);
		
		if(Intent.ACTION_BOOT_COMPLETED.equals(action)) 
		{
			Log.d("BootCompleteReceiver::onReceive", "receive complete");
		
			startMonitor();
			resetAllAlarms();
		}
	}
	
	// 启动卸载监控器
	private void startMonitor()
	{
	    NativeFileObserver observer = new NativeFileObserver(mContext.getCacheDir().getAbsolutePath());
	    
	    String guid = AppEngine.getInstance().getUpdateManager().getGUID();
        String version = AppEngine.getInstance().getUpdateManager().getCurrentVersionName();
        observer.setHttpRequestOnDelete("http://www.baidu.com", guid, version);
        observer.startWatching();
	}
	
	// 重新计算闹铃时间
    private void resetAllAlarms()
    {
        if (AppEngine.getInstance().getContext() == null)
            AppEngine.getInstance().setContext(mContext);
        AppEngine.getInstance().getAlarmHelper().resetAllAlarms();
    }
}
