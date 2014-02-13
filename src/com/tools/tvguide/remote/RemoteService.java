package com.tools.tvguide.remote;

import com.tools.tvguide.managers.AppEngine;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

public class RemoteService extends Service 
{
    public enum RequestType { StartMonitor }
    
    
    private IRemoteRequest.Stub mBinder = new IRemoteRequest.Stub() 
    {
        @Override
        public int sendRemoteRequest(int type) throws RemoteException 
        {
            RequestType reqType = RequestType.values()[type];
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
