package com.tools.tvguide.remote;

import com.tools.tvguide.managers.AppEngine;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class RemoteService extends Service 
{
    public static final String TAG = "RemoteService";
    public enum RequestType { StartMonitor }    
    
    private IRemoteRequest.Stub mBinder = new IRemoteRequest.Stub() 
    {
        @Override
        public int sendRemoteRequest(int type) throws RemoteException 
        {
            RequestType reqType = RequestType.values()[type];
            Log.d(TAG, "sendRemoteRequest type=" + reqType);
            switch (reqType)
            {
                case StartMonitor:
                    startMonitor();
                    break;
                default:
                    break;
            }
            
            return 0;
        }
    };
    
    @Override
    public IBinder onBind(Intent intent) 
    {
        return mBinder;
    }

    private void startMonitor()
    {
        AppEngine.getInstance().getUninstallMonitor().start();
    }
}
