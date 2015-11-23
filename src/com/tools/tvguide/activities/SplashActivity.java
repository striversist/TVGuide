package com.tools.tvguide.activities;

import com.tools.tvguide.components.SplashImageView;
import com.tools.tvguide.managers.AppEngine;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;

public class SplashActivity extends BaseActivity {

    public static final String TAG = SplashActivity.class.getSimpleName();
    private static final int SPLASH_DURATION = 1500;    // ms
    private SplashImageView mSplashImage;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,    
                WindowManager.LayoutParams.FLAG_FULLSCREEN); 
        
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();
        
        mSplashImage = new SplashImageView(this) {
            @Override
            public void draw(Canvas canvas) {
                super.draw(canvas);
                onSplashShown();
            }
        };
        setContentView(mSplashImage, new LayoutParams(width, height));
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSplashImage != null) {
            mSplashImage.recycle();
        }
    }

    public void onSplashShown() {
        AppEngine.getInstance().getBootManager().onSplashStarted();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                onSplashTimesUp();
            }
        }, SPLASH_DURATION);
    }
    
    public void onSplashTimesUp() {
        finish();
        AppEngine.getInstance().getBootManager().onSplashFinished();
    }
    
    /**
     * 返回键的拦截
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Back key disabled
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
