package com.tools.tvguide.managers;

import java.util.ArrayList;
import java.util.List;

import com.tools.tvguide.components.NativeFileObserver;
import com.tools.tvguide.components.ShortcutInstaller;
import com.tools.tvguide.components.Shutter;
import com.tools.tvguide.components.SplashDialog;
import com.tools.tvguide.data.GlobalData;
import com.tools.tvguide.managers.UpdateManager.IOCompleteCallback;
import com.tools.tvguide.remote.IRemoteRequest;
import com.tools.tvguide.remote.RemoteService;
import com.tools.tvguide.utils.Utility;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

public class BootManager implements Shutter
{
    private Context             mContext;
    private SplashDialog        mSplashDialog;
    private boolean             mShowSplash                                 = !EnvironmentManager.isDevelopMode;
    private SharedPreferences   mPreference;
    private NativeFileObserver  mNativeFileObserver;
    private static final String SHARE_PREFERENCES_NAME                      = "boot_settings";
    private static final String KEY_FIRST_START_FLAG                        = "key_first_start_flag";
    private List<OnSplashFinishedCallback> mOnSplashFinishedCallbackList;
    private boolean             mIsSplashShowing                            = false;
    private IRemoteRequest      mRemoteRequest;
    
    public interface OnSplashFinishedCallback
    {
        void OnSplashFinished();
    }
    
    public BootManager(Context context)
    {
        assert (context != null);
        mContext = context;
        mPreference = context.getSharedPreferences(SHARE_PREFERENCES_NAME, Context.MODE_PRIVATE);
        GlobalData.UserAgent = getUserAgentInternal();
        mOnSplashFinishedCallbackList = new ArrayList<BootManager.OnSplashFinishedCallback>();
        mNativeFileObserver = new NativeFileObserver(mContext.getCacheDir().getAbsolutePath());
    }
    
    public void start()
    {
        if (mShowSplash)
            showSplash();
        
        checkNetwork();
        
        AppEngine.getInstance().getUrlManager().init(new UrlManager.OnInitCompleteCallback() 
        {
            @Override
            public void OnInitComplete(int result)
            {
                AppEngine.getInstance().getUpdateManager().checkUpdate(new UpdateManager.IOCompleteCallback() 
                {
                    public void OnIOComplete(int result) 
                    {
                        uiHandler.sendEmptyMessage(result);
                    }
                });
                AppEngine.getInstance().getLoginManager().login();
            }
        });
        if (isFirstStart())
            new ShortcutInstaller(AppEngine.getInstance().getContext()).createShortCut();
        
        AppEngine.getInstance().getUninstallMonitor().start();
        
        bindService();
    }
    
    public void addOnSplashFinishedCallback(final OnSplashFinishedCallback callback)
    {
        mOnSplashFinishedCallbackList.add(callback);
    }
    
    // This API should be called in UI main thread
    private String getUserAgentInternal()
    {
        WebView webView = new WebView(AppEngine.getInstance().getContext());
        webView.layout(0, 0, 0, 0);
        WebSettings settings = webView.getSettings();
        return settings.getUserAgentString();
    }
    
    public boolean isFirstStart()
    {
        return mPreference.getBoolean(KEY_FIRST_START_FLAG, true);
    }
    
    public boolean isShowSplash()
    {
        return mShowSplash;
    }
    
    public void showSplash()
    {
        mSplashDialog = new SplashDialog(AppEngine.getInstance().getContext());
        mSplashDialog.showSplash();
    }
    
    public boolean isSplashShowing()
    {
        return mIsSplashShowing;
    }
    
    public void removeSplash()
    {
        if (mSplashDialog != null)
            mSplashDialog.checkTimeToRemove();
    }
    
    public void onSplashStarted()
    {
        mIsSplashShowing = true;
        removeSplash();
    }
    
    public void onSplashFinished()
    {        
        for (int i=0; i<mOnSplashFinishedCallbackList.size(); ++i)
            mOnSplashFinishedCallbackList.get(i).OnSplashFinished();
        
        mOnSplashFinishedCallbackList = null;
        mSplashDialog = null;
        mIsSplashShowing = false;
        
        new Handler().postDelayed(new Runnable() 
        {
            @Override
            public void run() 
            {
                AppEngine.getInstance().getAlarmHelper().resetAllAlarms();
            }
        }, 1000);
    }
    
    @Override
    public void onShutDown()
    {
        if (isFirstStart())
            mPreference.edit().putBoolean(KEY_FIRST_START_FLAG, false).commit();
    }
    
    private void checkNetwork()
    {
        if (!Utility.isNetworkAvailable())
            Toast.makeText(AppEngine.getInstance().getApplicationContext(), "注意：当前网络不可用！", Toast.LENGTH_LONG).show();
    }
    
    private void bindService()
    {
        Context context = AppEngine.getInstance().getApplicationContext();
        if (context == null)
            return;
        
        Intent serviceIntent = new Intent(context, RemoteService.class);
        context.startService(serviceIntent);
        context.bindService(serviceIntent, new ServiceConnection() 
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
                try 
                {
                    mRemoteRequest.sendRemoteRequest(RemoteService.RequestType.StartMonitor.ordinal());
                } 
                catch (RemoteException e) 
                {
                    e.printStackTrace();
                }
            }
        }, Context.BIND_AUTO_CREATE);
    }
    
    private Handler uiHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            switch(msg.what)
            {
                case IOCompleteCallback.NEED_UPDATE:
                    Toast.makeText(AppEngine.getInstance().getApplicationContext(), "有新版本啦，请检查更新", Toast.LENGTH_LONG).show();
                    break;
                case IOCompleteCallback.NO_NEED_UPDATE:
                    break;
            }
        }
    };
}
