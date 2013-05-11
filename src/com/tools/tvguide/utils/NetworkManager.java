package com.tools.tvguide.utils;

import android.os.HandlerThread;
import android.os.Looper;

public class NetworkManager 
{
    private static NetworkManager mInstance;
    private HandlerThread mNetworkThread;
    
    public static NetworkManager getInstance()
    {
        if (mInstance == null)
        {
            mInstance = new NetworkManager();
        }
        return mInstance;
    }
    
    private NetworkManager()
    {
        mNetworkThread = new HandlerThread("networkThread");
        mNetworkThread.start();
    }
    
    public Looper getNetworkThreadLooper()
    {
        return mNetworkThread.getLooper();
    }
}
