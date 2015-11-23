package com.tools.tvguide.activities;

import java.math.BigDecimal;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tools.tvguide.R;
import com.tools.tvguide.components.MyProgressDialog;
import com.tools.tvguide.managers.AdManager.GetPointsCallback;
import com.tools.tvguide.managers.AdManager.SpendPointsCallback;
import com.tools.tvguide.managers.AppEngine;

public class SupportActivity extends BaseActivity implements Callback 
{
    private static final int REMOVE_AD_POINTS           = 300;
    private static final int REMOVE_ONEDAY_AD_POINTS    = 10;
    private static final int ONEDAY_MILLIS              = 24 * 3600 * 1000;
    
    private TextView mPointsTextView;
    private TextView mRemoveOnedayAdTextView;
    private Button mRemoveOnedayAdButton;
    private Button mRemoveAdButton;
    
    private String mPointsFormatString;
    private String mLeftHoursFormatString;
    private int mCurrentPoints = 0;
    
    private Handler mUiHandler;
    private enum SelfMessage { UpdatePoints, UpdatePointsFail };
    
    private interface UpdatePointsCallback
    {
        void onUpdatePoints(boolean success);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);
        
        mPointsTextView = (TextView) findViewById(R.id.current_point_tv);
        mRemoveOnedayAdTextView = (TextView) findViewById(R.id.remove_oneday_ad_tv);
        mRemoveAdButton = (Button) findViewById(R.id.remove_ad_btn);
        mRemoveOnedayAdButton = (Button) findViewById(R.id.remove_oneday_ad_btn);
        mUiHandler = new Handler(this);
        
        mPointsFormatString = mPointsTextView.getText().toString();
        mPointsTextView.setText(String.format(mPointsFormatString, "..."));
        
        mLeftHoursFormatString = mRemoveOnedayAdTextView.getText().toString();
        mRemoveOnedayAdButton.setText(String.valueOf(REMOVE_ONEDAY_AD_POINTS) + "金币");
        mRemoveAdButton.setText(String.valueOf(REMOVE_AD_POINTS) + "金币");
        
        updatePoints();
        updateAdLeftTime();
    }
    
    @Override
    protected void onResume()
    {
        super.onResume();
        updatePoints();
        updateAdLeftTime();
    }

    public void back(View view)
    {
        if (view instanceof Button)
        {
            finish();
        }
    }

    public void onClick(View v) 
    {
        switch (v.getId())
        {
            case R.id.get_point_btn:
                showOffers();
                break;
            case R.id.remove_ad_btn:
                checkRemoveAd(REMOVE_AD_POINTS);
                break;
            case R.id.restore_points_btn:
                restorePoints();
                break;
            case R.id.remove_oneday_ad_btn:
                checkRemoveAd(REMOVE_ONEDAY_AD_POINTS);
                break;
        }
    }
    
    private void updateAdLeftTime()
    {
        long leftTimeMs = AppEngine.getInstance().getAdManager().getDisableAdTillTime() - System.currentTimeMillis();
        if (leftTimeMs <= 0)
        {
            mRemoveOnedayAdTextView.setText(String.format(mLeftHoursFormatString, ""));
            return;
        }
        
        int leftHours = new BigDecimal((double)leftTimeMs / 1000 / 3600).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();     // 四舍五入取整
        mRemoveOnedayAdTextView.setText(String.format(mLeftHoursFormatString, "(剩" + String.valueOf(leftHours) + "小时)"));
    }
    
    private void updatePoints()
    {
        updatePoints(null);
    }
    
    private void updatePoints(final UpdatePointsCallback callback)
    {
        AppEngine.getInstance().getAdManager().getPointsAsync(this, new GetPointsCallback() 
        {
            @Override
            public void onUpdatePointsFailed(String error) {
                mUiHandler.obtainMessage(SelfMessage.UpdatePointsFail.ordinal(), error).sendToTarget();
                if (callback != null) {
                    callback.onUpdatePoints(false);
                }
            }
            
            @Override
            public void onUpdatePoints(String currencyName, int points) {
                mCurrentPoints = points;
                mUiHandler.obtainMessage(SelfMessage.UpdatePoints.ordinal(), points, 0).sendToTarget();
                if (callback != null) {
                    callback.onUpdatePoints(true);
                }
            }
        });
    }
    
    private void checkRemoveAd(final int amount)
    {
        if (mCurrentPoints < amount)
        {
            new AlertDialog.Builder(SupportActivity.this)
            .setTitle("提示")
            .setMessage("您当前的金币不足，是否去赚一些呢？")
            .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which) 
                {
                }
            })
            .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which) 
                {
                    showOffers();
                }
            })
            .create().show();
        } 
        else 
        {
            final MyProgressDialog dialog = new MyProgressDialog(this);
            dialog.setMessage("广告移除中，请稍等...");
            dialog.show();
            
            AppEngine.getInstance().getAdManager().spendPoints(this, amount, new SpendPointsCallback() {
                @Override
                public void onUpdatePointsFailed(String error) {
                    dialog.dismiss();
                    mUiHandler.obtainMessage(SelfMessage.UpdatePointsFail.ordinal(), error).sendToTarget();
                }
                
                @Override
                public void onUpdatePoints(String currencyName, int points) {
                    dialog.dismiss();
                    mUiHandler.obtainMessage(SelfMessage.UpdatePoints.ordinal(), points, 0).sendToTarget();
                    mPointsTextView.post(new Runnable() {
                        @Override
                        public void run() {
                            if (amount == REMOVE_AD_POINTS) {
                                AppEngine.getInstance().getAdManager().removeAd();
                                Toast.makeText(SupportActivity.this, "所有广告已永久移除！请重启程序！", Toast.LENGTH_LONG).show();
                            } else if (amount == REMOVE_ONEDAY_AD_POINTS) {
                                AppEngine.getInstance().getAdManager().addDisableAddDuration(ONEDAY_MILLIS);
                                updateAdLeftTime();
                                Toast.makeText(SupportActivity.this, "成功关闭广告一天，请重启程序！", Toast.LENGTH_LONG).show();
                            }                            
                        }
                    });
                }
            });   
        }
    }
    
    private void showOffers()
    {
        AppEngine.getInstance().getAdManager().showOffers(this);
    }
    
    private void restorePoints()
    {
        final MyProgressDialog dialog = new MyProgressDialog(this);
        dialog.setMessage("恢复金币中，请稍等...");
        dialog.show();
        updatePoints(new UpdatePointsCallback() {
            @Override
            public void onUpdatePoints(final boolean success) {
                dialog.dismiss();
                mPointsTextView.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SupportActivity.this, "恢复金币" + (success ? "成功" : "失败") + " ！", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public boolean handleMessage(Message msg) 
    {
        SelfMessage selfMsg = SelfMessage.values()[msg.what];
        switch (selfMsg)
        {
            case UpdatePoints:
                mPointsTextView.setText(String.format(mPointsFormatString, String.valueOf(msg.arg1)));
                break;
            case UpdatePointsFail:
                Toast.makeText(SupportActivity.this, "出错啦：" + (String) msg.obj, Toast.LENGTH_LONG).show();
                break;
        }

        return true;
    }

}
