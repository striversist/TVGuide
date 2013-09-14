package com.tools.tvguide.components;

import com.tools.tvguide.managers.AppEngine;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Canvas;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;

public class SplashDialog extends Dialog implements OnDismissListener 
{
    private final static String     TAG                        = "SplashDialog";
    private Context                 mContext;
    private SplashImageView         mSplashImage               = null;
    private boolean                 mIsNotified                = false;

    private long                    iTimeUp_ms                 = 2000;
    private long                    mStartTime                 = 0;
    private static final int        MSG_SPLASH_START           = 0;
    private static final int        MSG_SPLASH_TO_REMOVE       = 1;
    private static final int        MSG_SPLASH_REMOVED         = 2;
    
    public SplashDialog(Context context) 
    {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
        setCancelable(false);
        
        mContext = context;
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();
        
        mSplashImage = new SplashImageView(context)
        {
            @Override
            public void draw(Canvas canvas)
            {
                super.draw(canvas);
                notifyOnSplashStart();
            }
        };
        setContentView(mSplashImage, new LayoutParams(width, height));
    }

    public void showSplash()
    {
        super.show();
        setOnDismissListener(this);
    }
    
    private void notifyOnSplashStart()
    {
        if (mIsNotified)
            return;

        mHandler.removeMessages(MSG_SPLASH_START);
        mHandler.sendEmptyMessage(MSG_SPLASH_START);
    }

    public void checkTimeToRemove()
    {
        long plusTime = System.currentTimeMillis() - mStartTime;
        plusTime = plusTime < iTimeUp_ms ? iTimeUp_ms - plusTime : 0;
        mHandler.sendEmptyMessageDelayed(MSG_SPLASH_TO_REMOVE, plusTime);
    }

    public void forceRemoveSplash()
    {
        mHandler.sendEmptyMessage(MSG_SPLASH_TO_REMOVE);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
        return true;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event)
    {
        return true;
    }
    
    @Override
    public void onDismiss(DialogInterface dialog) 
    {
        AppEngine.getInstance().getBootManager().onSplashFinished();
        destroy();
    }
    
    private void destroy()
    {
        if (mSplashImage != null)
            mSplashImage.recycle();
    };
    
    private Handler mHandler = new Handler()
    {
        public void handleMessage(android.os.Message msg)
        {
            switch (msg.what)
            {
                case MSG_SPLASH_START:
                    mIsNotified = true;
                    mStartTime = System.currentTimeMillis();
                    AppEngine.getInstance().getBootManager().onSplashStarted();
                    break;

                case MSG_SPLASH_TO_REMOVE:
                    dismiss();
                    break;
            }
        };
    };
}
