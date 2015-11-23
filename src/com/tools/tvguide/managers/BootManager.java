package com.tools.tvguide.managers;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.tools.tvguide.activities.SplashActivity;
import com.tools.tvguide.components.ShortcutInstaller;
import com.tools.tvguide.components.Shutter;
import com.tools.tvguide.data.GlobalData;
import com.tools.tvguide.uninstall.UninstallObserver;
import com.tools.tvguide.utils.Utility;
import com.umeng.onlineconfig.OnlineConfigAgent;

public class BootManager implements Shutter
{
    private Context             mContext;
    private boolean             mIsSplashEnabled                            = !EnvironmentManager.isDevelopMode;
    private SharedPreferences   mPreference;
    private static final String SHARE_PREFERENCES_NAME                      = "boot_settings";
    private static final String KEY_STARTUP_TIMES                           = "key_startup_times";
    private static final String KEY_LAST_STARTUP_TIME                       = "key_last_startup_time_flag";
    private List<OnSplashFinishedCallback> mOnSplashFinishedCallbackList;
    private OnStartedCallback   mOnStartedCallback;
    private boolean             mIsSplashShowing                            = false;
    private int                 mStartupTimes;
    private enum SelfMessage {Msg_Need_Update};
    
    public interface OnSplashFinishedCallback
    {
        void OnSplashFinished();
    }
    
    public interface OnStartedCallback
    {
        void OnUpdateInfo(boolean needUpdate);
    }
    
    public BootManager(Context context)
    {
        assert (context != null);
        mContext = context;
        mPreference = context.getSharedPreferences(SHARE_PREFERENCES_NAME, Context.MODE_PRIVATE);
        GlobalData.UserAgent = getUserAgentInternal();
        mOnSplashFinishedCallbackList = new ArrayList<BootManager.OnSplashFinishedCallback>();
        mStartupTimes = mPreference.getInt(KEY_STARTUP_TIMES, 0);
        mPreference.edit().putInt(KEY_STARTUP_TIMES, ++mStartupTimes).commit();
        mPreference.edit().putLong(KEY_LAST_STARTUP_TIME, System.currentTimeMillis()).commit();
    }
    
    public void start(OnStartedCallback callback)
    {
        assert (callback != null);
        mOnStartedCallback = callback;
        start();
    }
    
    public void start()
    {
        if (mIsSplashEnabled)
            showSplash();
        
        checkNetwork();
        OnlineConfigAgent.getInstance().setDebugMode(false);
        OnlineConfigAgent.getInstance().updateOnlineConfig(mContext);
        AppEngine.getInstance().getEnvironmentManager().init();
        AppEngine.getInstance().getUrlManager().init(new UrlManager.OnInitCompleteCallback() 
        {
            @Override
            public void OnInitComplete(int result)
            {
                AppEngine.getInstance().getUpdateManager().checkUpdate(new UpdateManager.IOCompleteCallback() 
                {
                    public void OnIOComplete(CheckResult result) 
                    {
                        if (result == CheckResult.Need_Update)
                            uiHandler.obtainMessage(SelfMessage.Msg_Need_Update.ordinal()).sendToTarget();
                    }
                });
                AppEngine.getInstance().getLoginManager().login();
            }
        });
        if (isFirstStart())
            new ShortcutInstaller(AppEngine.getInstance().getContext()).createShortCut();
        
        UninstallObserver.autoSetHttpRequestOnUninstall(mContext.getApplicationContext());
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
        return mStartupTimes == 1;
    }
    
    public int getStartupTimes() {
        return mStartupTimes;
    }
    
    public long getLastStartTime()
    {
        return mPreference.getLong(KEY_LAST_STARTUP_TIME, 0);
    }
    
    public boolean isSplashEnabled()
    {
        return mIsSplashEnabled;
    }
    
    public void showSplash()
    {
        Intent intent = new Intent(mContext, SplashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);;
    }
    
    public boolean isSplashShowing()
    {
        return mIsSplashShowing;
    }
    
    public void onSplashStarted()
    {
        mIsSplashShowing = true;
    }
    
    public void onSplashFinished()
    {        
        for (int i=0; i<mOnSplashFinishedCallbackList.size(); ++i)
            mOnSplashFinishedCallbackList.get(i).OnSplashFinished();
        
        mOnSplashFinishedCallbackList.clear();
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
    public void onShutDown() {
    }
    
    private void checkNetwork()
    {
        if (!Utility.isNetworkAvailable())
            Toast.makeText(AppEngine.getInstance().getApplicationContext(), "注意：当前网络不可用！", Toast.LENGTH_LONG).show();
    }
    
    private Handler uiHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            SelfMessage selfMsg = SelfMessage.values()[msg.what];
            switch(selfMsg)
            {
                case Msg_Need_Update:
                    Toast.makeText(AppEngine.getInstance().getApplicationContext(), "有新版本啦，请检查更新", Toast.LENGTH_LONG).show();
                    if (mOnStartedCallback != null)
                        mOnStartedCallback.OnUpdateInfo(true);
                    break;
            }
        }
    };
}
