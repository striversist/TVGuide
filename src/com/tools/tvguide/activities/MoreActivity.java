package com.tools.tvguide.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tools.tvguide.R;
import com.tools.tvguide.components.PackageInstaller;
import com.tools.tvguide.managers.AdManager;
import com.tools.tvguide.managers.AppEngine;
import com.tools.tvguide.managers.StatManager.ClickModule;
import com.tools.tvguide.managers.UrlManager;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;

public class MoreActivity extends BaseActivity implements UmengUpdateListener 
{
    private Dialog mCheckingDialog;
    private TextView mCheckTextView;
    private ImageView mUpdateNewIcon;
    private ViewGroup mSupportUsLayout;
    private ImageView mSupportRedDot;
    private enum TextStatus {State_Check, State_Upgrade};
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more);
        createDialogs();
        
        mCheckTextView = (TextView)findViewById(R.id.more_version_check);
        String versionFormat = mCheckTextView.getText().toString();
        String finalVersion = String.format(versionFormat, AppEngine.getInstance().getUpdateManager().getCurrentVersionName());
        mCheckTextView.setText(finalVersion);
        mCheckTextView.setTag(TextStatus.State_Check);
        
        mUpdateNewIcon = (ImageView) findViewById(R.id.more_update_new_icon);
        mSupportUsLayout = (RelativeLayout) findViewById(R.id.more_support_us);
        mSupportRedDot = (ImageView) findViewById(R.id.more_support_us_red_dot);
        UmengUpdateAgent.setUpdateListener(this);
    }
    
    @Override
    protected void onResume()
    {
        super.onResume();
        if (AdManager.isTimeToShowAd()) {
            // 显示“支持我们”
            mSupportUsLayout.setVisibility(View.VISIBLE);
            
            // 显示“支持我们”小红点
            if (AppEngine.getInstance().getStatManager().getClickTimes(ClickModule.TabMoreSupportUs) == 0) { // 从未点击过
                mSupportRedDot.setVisibility(View.VISIBLE);
            } else if (mSupportRedDot.getVisibility() == View.VISIBLE) {
                mSupportRedDot.setVisibility(View.INVISIBLE);
            }
        } else {
            mSupportUsLayout.setVisibility(View.GONE);
        }
    }
    
    private void createDialogs()
    {
        mCheckingDialog = new AlertDialog.Builder(MoreActivity.this)
            .setTitle(getResources().getString(R.string.check_update))
            .setMessage("正在进行，请稍等...")
            .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which) 
                {
                    
                }
            })
            .create();
    }

    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.more_alarms:
                Intent alarmIntent = new Intent(MoreActivity.this, AlarmSettingActivity.class);
                startActivity(alarmIntent);
                break;
            case R.id.more_check_new_ver:
                TextStatus state = (TextStatus) mCheckTextView.getTag();
                if (state == TextStatus.State_Check)
                    checkNewVersion();
                else if (state == TextStatus.State_Upgrade)
                    upgrade();
                break;
            case R.id.more_feedback:
//                Intent feedbackIntent = new Intent(MoreActivity.this, FeedbackActivity.class);
//                startActivity(feedbackIntent);
                AppEngine.getInstance().getAdManager().showFeedback(this);
                break;
            case R.id.more_about:
                showAbout();
                break;
            case R.id.more_support_us:
                AppEngine.getInstance().getStatManager().clickModule(ClickModule.TabMoreSupportUs);
                Intent supportIntent = new Intent(MoreActivity.this, SupportActivity.class);
                startActivity(supportIntent);
                break;
            case R.id.more_clear_cache:
                clearCache();
                break;
            case R.id.more_quit:
                quit();
                break;
        }
    }
    
    private void checkNewVersion()
    {
        mCheckingDialog.show();
        UmengUpdateAgent.update(this);
    }
    
    private void showAbout()
    {
        AlertDialog dialog = new AlertDialog.Builder(MoreActivity.this)
            .setIcon(R.drawable.application)
            .setTitle(getResources().getString(R.string.app_name))
            .setMessage("作者：大眼牛工作室\n邮箱：bigeyecow@qq.com\n版权：©2015-2016")
            .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() 
            {
                @Override
                public void onClick(DialogInterface dialog, int which) 
                {
                    dialog.dismiss();
                }
            })
            .create();
        dialog.show();
    }
    
    private void upgrade()
    {
        String newUrl = UrlManager.tryToReplaceHostNameWithIP(AppEngine.getInstance().getUpdateManager().getUrl());
        new PackageInstaller(MoreActivity.this).installRemotePackage(newUrl);
        Toast.makeText(MoreActivity.this, "正在下载...", Toast.LENGTH_SHORT).show();
    }
    
    private void clearCache()
    {
        AppEngine.getInstance().getCacheManager().clear();
        AppEngine.getInstance().getDiskCacheManager().clearAll();
        Toast.makeText(MoreActivity.this, "缓存已清除", Toast.LENGTH_SHORT).show();
    }
    
    private void quit()
    {
        AppEngine.getInstance().prepareBeforeExit();
        finish();
    }

    @Override
    public void onUpdateReturned(int updateStatus, UpdateResponse updateInfo) {
        if (mCheckingDialog.isShowing())
            mCheckingDialog.dismiss();
        
        switch (updateStatus) {
        case UpdateStatus.Yes: // has update
            UmengUpdateAgent.showUpdateDialog(this, updateInfo);
            break;
        case UpdateStatus.No: // has no update
            Toast.makeText(this, "没有更新", Toast.LENGTH_SHORT).show();
            break;
        case UpdateStatus.NoneWifi: // none wifi
            Toast.makeText(this, "没有wifi连接， 只在wifi下更新", Toast.LENGTH_SHORT).show();
            break;
        case UpdateStatus.Timeout: // time out
            Toast.makeText(this, "超时", Toast.LENGTH_SHORT).show();
            break;
        }
    }
}
