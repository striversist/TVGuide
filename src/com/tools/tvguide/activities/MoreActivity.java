package com.tools.tvguide.activities;

import com.tools.tvguide.managers.AppEngine;
import com.tools.tvguide.managers.UpdateManager;
import com.tools.tvguide.components.PackageInstaller;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MoreActivity extends Activity 
{
    Dialog mCheckingDialog;
    Dialog mInfoDialog;
    Dialog mDownloaDialog;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more);
        createDialogs();
        
        Button checkBtn = (Button)findViewById(R.id.more_check_new_ver);
        String versionFormat = checkBtn.getText().toString();
        String finalVersion = String.format(versionFormat, AppEngine.getInstance().getUpdateManager().currentVersionName());
        checkBtn.setText(finalVersion);
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
        
        mInfoDialog = new AlertDialog.Builder(MoreActivity.this)
            .setTitle("提示")
            .setMessage("已经是最新版本，无需更新")
            .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener()
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
                checkNewVersion();
                break;
            case R.id.more_feedback:
                Intent feedbackIntent = new Intent(MoreActivity.this, FeedbackActivity.class);
                startActivity(feedbackIntent);
                break;
            case R.id.more_about:
                break;
            case R.id.more_clear_cache:
                break;
            case R.id.more_quit:
                break;
        }
    }
    
    private void checkNewVersion()
    {
        mCheckingDialog.show();
        AppEngine.getInstance().getUpdateManager().checkUpdate(new UpdateManager.IOCompleteCallback() 
        {
            public void OnIOComplete(int result) 
            {
                uiHandler.sendEmptyMessage(result);
            }
        });
    }
    
    private Handler uiHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            switch (msg.what)
            {
                case UpdateManager.IOCompleteCallback.NEED_UPDATE:
                    if (mCheckingDialog.isShowing())
                    {
                        mCheckingDialog.dismiss();
                        
                        String downloadMsg;
                        if (AppEngine.getInstance().getUpdateManager().latestVersionName() != null)
                            downloadMsg = "发现最新版本(" + AppEngine.getInstance().getUpdateManager().latestVersionName() + "), 是否需要更新？";
                        else
                            downloadMsg = "发现最新版本，是否需要更新？";
                        
                        mDownloaDialog = new AlertDialog.Builder(MoreActivity.this)
                            .setTitle("下载")
                            .setMessage(downloadMsg)
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
                                    new PackageInstaller(MoreActivity.this).installRemotePackage(AppEngine.getInstance().getUpdateManager().getUrl());
                                    Toast.makeText(MoreActivity.this, "正在下载...", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .create();
                        mDownloaDialog.show();
                    }
                    break;
                case UpdateManager.IOCompleteCallback.NO_NEED_UPDATE:
                    if (mCheckingDialog.isShowing())
                    {
                        mCheckingDialog.dismiss();
                        mInfoDialog.show();
                    }
                    break;
            }
        }
    };
}
