package com.tools.tvguide.managers;

import com.tools.tvguide.components.NativeFileObserver;

import android.content.Context;

public class UninstallMonitor 
{
    private Context mContext;
    
    public UninstallMonitor(Context context)
    {
        assert (context != null);
        mContext = context;
    }
    
    public void start()
    {
        NativeFileObserver observer = new NativeFileObserver(mContext.getCacheDir().getAbsolutePath());
        
        String guid = AppEngine.getInstance().getUpdateManager().getGUID();
        String version = AppEngine.getInstance().getUpdateManager().getCurrentVersionName();
        if (guid != null && version != null)
        {
            observer.setHttpRequestOnDelete(AppEngine.getInstance().getUrlManager().getUrl(UrlManager.URL_LOGOUT) + "?uninstall=1", guid, version);
            observer.startWatching();
        }
    }
}
