package com.tools.tvguide.managers;

import com.tools.tvguide.remote.IRemoteRequest;
import com.tools.tvguide.remote.RemoteService;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

public class ServiceManager 
{
    private Context             mContext;
    private boolean             mHasInit = false;
    private IRemoteRequest      mRemoteRequest;
    private boolean             mStartMonitorRequest = false; 
    
    public ServiceManager(Context context)
    {
        assert (context != null);
        mContext = context;
    }
    
    public void init()
    {
        if (mHasInit)
            return;
        
        bindService();
    }
    
    public void startMonitor()
    {
        if (mRemoteRequest != null)
            startMonitorInternal();
        else 
            mStartMonitorRequest = true;
    }
    
    private void startMonitorInternal()
    {
        try 
        {
            mRemoteRequest.sendRemoteRequest(RemoteService.RequestType.StartMonitor.ordinal());
            mStartMonitorRequest = false;
        } 
        catch (RemoteException e) 
        {
            e.printStackTrace();
        }
    }
    
    private void bindService()
    {
        if (mContext == null)
            return;
        
        Intent serviceIntent = new Intent(mContext, RemoteService.class);
        mContext.startService(serviceIntent);
        mContext.bindService(serviceIntent, new ServiceConnection() 
        {
            @Override
            public void onServiceDisconnected(ComponentName name) 
            {
                mRemoteRequest = null;
            }
            
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) 
            {
                mRemoteRequest = IRemoteRequest.Stub.asInterface(service);
                if (mStartMonitorRequest)
                    startMonitorInternal();
            }
        }, Context.BIND_AUTO_CREATE);
    }
}
