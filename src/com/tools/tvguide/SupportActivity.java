package com.tools.tvguide;

import com.tools.tvguide.components.MyProgressDialog;
import com.tools.tvguide.managers.AdManager.GetPointsCallback;
import com.tools.tvguide.managers.AppEngine;

import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SupportActivity extends Activity implements Callback 
{
    private static final int REMOVE_AD_POINTS = 100;
    private TextView mPointsTextView;
    private Button mRemoveAdButton;
    
    private String mPointsFormatString;
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
        mRemoveAdButton = (Button) findViewById(R.id.remove_ad_btn);
        mUiHandler = new Handler(this);
        
        mPointsFormatString = mPointsTextView.getText().toString();
        mPointsTextView.setText(String.format(mPointsFormatString, "加载中..."));
        mRemoveAdButton.setText(String.valueOf(REMOVE_AD_POINTS) + "金币");
        
        updatePoints();
    }
    
    @Override
    protected void onResume()
    {
        super.onResume();
        updatePoints();
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
                checkRemoveAd();
                break;
            case R.id.restore_points_btn:
                restorePoints();
                break;
        }
    }
    
    private void updatePoints()
    {
        updatePoints(null);
    }
    
    private void updatePoints(final UpdatePointsCallback callback)
    {
        AppEngine.getInstance().getAdManager().getPointsAsync(new GetPointsCallback() 
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
    
    private void checkRemoveAd()
    {
        if (mCurrentPoints < REMOVE_AD_POINTS)
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
            AppEngine.getInstance().getAdManager().removeAd();
            Toast.makeText(SupportActivity.this, "恭喜您！所有广告已移除！请重启程序！", Toast.LENGTH_LONG).show();
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
                        Toast.makeText(SupportActivity.this, "恢复金币" + (success ? "成功" : "失败") + "!", Toast.LENGTH_SHORT).show();
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
