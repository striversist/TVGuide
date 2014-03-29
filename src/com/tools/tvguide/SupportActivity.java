package com.tools.tvguide;

import cn.waps.AppConnect;

import com.tools.tvguide.managers.AdManager.GetPointsCallback;
import com.tools.tvguide.managers.AdManager.SpendPointsCallback;
import com.tools.tvguide.managers.AppEngine;

import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SupportActivity extends Activity implements OnClickListener, Callback 
{
    private static final int REMOVE_AD_POINTS = 100;
    private TextView mPointsTextView;
    private Button mGetPointsButton;
    private Button mRemoveAdButton;
    
    private String mPointsFormatString;
    private int mCurrentPoints = 0;
    
    private Handler mUiHandler;
    private enum SelfMessage { UpdatePoints };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);
        
        mPointsTextView = (TextView) findViewById(R.id.current_point_tv);
        mGetPointsButton = (Button) findViewById(R.id.get_point_btn);
        mRemoveAdButton = (Button) findViewById(R.id.remove_ad_btn);
        mUiHandler = new Handler(this);
        
        mPointsFormatString = mPointsTextView.getText().toString();
        mPointsTextView.setText(String.format(mPointsFormatString, "加载中..."));
        
        mGetPointsButton.setOnClickListener(this);
        mRemoveAdButton.setOnClickListener(this);
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
    
    private void updatePoints()
    {
        AppEngine.getInstance().getAdManager().getPointsAsync(new GetPointsCallback() 
        {
            @Override
            public void onUpdatePointsFailed(String error) {
                Toast.makeText(SupportActivity.this, "出错啦：" + error, Toast.LENGTH_LONG).show();
            }
            
            @Override
            public void onUpdatePoints(String currencyName, int points) {
                mCurrentPoints = points;
                mUiHandler.obtainMessage(SelfMessage.UpdatePoints.ordinal(), points, 0).sendToTarget();
            }
        });
    }

    @Override
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
        }
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
            AppEngine.getInstance().getAdManager().spendPoints(REMOVE_AD_POINTS, new SpendPointsCallback() {
                @Override
                public void onUpdatePointsFailed(String error) {
                    Toast.makeText(SupportActivity.this, "去除广告出错：" + error, Toast.LENGTH_LONG).show();
                }
                
                @Override
                public void onUpdatePoints(String currencyName, int points) {
                    updatePoints();
                    Toast.makeText(SupportActivity.this, "恭喜您！所有广告已移除！请重启程序！", Toast.LENGTH_LONG).show();
                }
            });
        }
    }
    
    private void showOffers()
    {
        AppEngine.getInstance().getAdManager().showOffers(this);
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
        }

        return true;
    }

}
