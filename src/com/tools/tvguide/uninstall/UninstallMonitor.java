package com.tools.tvguide.uninstall;

import android.content.Context;

public class UninstallMonitor
{
    private final String TAG = "UninstallMonitor";
    private Context mContext;
    private NativeFileObserver mFileObserver;
    private boolean mStart = false;
    
    public interface InitCallback {
    	public void onInitComplete(int result);
    }
    
    public UninstallMonitor(Context context)
    {
        assert (context != null);
        mContext = context;
    }
    
    public void setHttpRequestOnUninstall(String url)
    {
    	if (!mStart) {
    		if (!start()) {
    			return;
    		}
    	}
    	
    	if (url != null) {
    		mFileObserver.setHttpRequestOnDelete(url);
    	}
    }
    
    private boolean start()
    {
    	if (mFileObserver == null)
    		mFileObserver = new NativeFileObserver("/data/data/" + mContext.getPackageName());
    	
    	if (mStart)
    		return true;
    	
    	mFileObserver.startWatching();
    	mStart = true;
    	return true;
    }
}
