package com.tools.tvguide.managers;

import com.tools.tvguide.components.NativeFileObserver;
import com.tools.tvguide.components.Shutter;

import android.content.Context;
import android.util.Log;

public class UninstallMonitor implements Shutter 
{
    private final String TAG = "UninstallMonitor";
    private Context mContext;
    private boolean mLoadSuccess = false;
    
    public UninstallMonitor(Context context)
    {
        assert (context != null);
        mContext = context;
        
        if (mLoadSuccess == false)
        {
            try
            {
                System.loadLibrary("monitor");
                mLoadSuccess = true;
            }
            catch (UnsatisfiedLinkError e)
            {
                Log.e(TAG, e.toString());
                mLoadSuccess = false;
            }
        }
    }
    
    public void start()
    {
        if (!EnvironmentManager.enableUninstallReport)
        {
            Log.d(TAG, "disable uninstall report");
            return;
        }
        
        if (mLoadSuccess == false)
        {
            Log.d(TAG, "load library failed");
            return;
        }
        
        final NativeFileObserver observer = new NativeFileObserver(mContext.getCacheDir().getAbsolutePath());
        
        String guid = AppEngine.getInstance().getUpdateManager().getGUID();
        String version = AppEngine.getInstance().getUpdateManager().getCurrentVersionName();
        Log.d(TAG, "guid=" + guid + ", version=" + version);
        if (guid != null && version != null)
        {
            observer.setHttpRequestOnDelete(AppEngine.getInstance().getUrlManager().getUrl(UrlManager.ProxyUrl.Logout) + "?uninstall=1", guid, version);
            observer.startWatching();
        }
    }

    @Override
    public void onShutDown() 
    {
        mLoadSuccess = false;
    }
}
