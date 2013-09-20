package com.tools.tvguide.managers;

import com.tools.tvguide.components.ShortcutInstaller;
import com.tools.tvguide.components.SplashDialog;
import com.tools.tvguide.utils.Utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

public class BootManager 
{
    private Context             mContext;
    private SplashDialog        mSplashDialog;
    private boolean             mShowSplash                                 = true;
    private SharedPreferences   mPreference;
    private static final String SHARE_PREFERENCES_NAME                      = "boot_settings";
    private static final String KEY_FIRST_START_FLAG                        = "key_first_start_flag";
    private String              mUA;
    
    public BootManager(Context context)
    {
        mContext = context;
        mPreference = context.getSharedPreferences(SHARE_PREFERENCES_NAME, Context.MODE_PRIVATE);
        mUA = getUserAgentInternal();
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
                AppEngine.getInstance().getUpdateManager().checkUpdate();
                AppEngine.getInstance().getLoginManager().login();
            }
        });
        AppEngine.getInstance().getLoginManager().startKeepAliveProcess();
        if (isFirstStart())
            new ShortcutInstaller(mContext).createShortCut();
    }
    
    public String getUserAgent()
    {
        return mUA;
    }
    
    // This API should be called in UI main thread
    private String getUserAgentInternal()
    {
        WebView webView = new WebView(mContext);
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
        mSplashDialog = new SplashDialog(mContext);
        mSplashDialog.showSplash();
    }
    
    public void removeSplash()
    {
        if (mSplashDialog != null)
            mSplashDialog.checkTimeToRemove();
    }
    
    public void onSplashStarted()
    {
        removeSplash();
    }
    
    public void onSplashFinished()
    {
        if (isFirstStart())
            mPreference.edit().putBoolean(KEY_FIRST_START_FLAG, false).commit();
        mSplashDialog = null;
    }
    
    private void checkNetwork()
    {
        if (!Utility.isNetworkAvailable())
            Toast.makeText(mContext, "注意：当前网络不可用！", Toast.LENGTH_LONG).show();
    }
}
