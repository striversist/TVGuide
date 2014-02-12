package com.tools.tvguide.remote;

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
            return 200;
        }
    };
    
    @Override
    public IBinder onBind(Intent intent) 
    {
        return mBinder;
    }

}
