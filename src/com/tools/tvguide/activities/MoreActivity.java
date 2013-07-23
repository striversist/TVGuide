package com.tools.tvguide.activities;

import com.tools.tvguide.managers.AppEngine;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class MoreActivity extends Activity 
{
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more);
        
        Button checkBtn = (Button)findViewById(R.id.more_check_new_ver);
        String versionFormat = checkBtn.getText().toString();
        String finalVersion = String.format(versionFormat, AppEngine.getInstance().getUpdateManager().currentVersionName());
        checkBtn.setText(finalVersion);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_more, menu);
        return true;
    }

    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.more_alarms:
                Intent intent = new Intent(MoreActivity.this, AlarmSettingActivity.class);
                startActivity(intent);
                break;
            case R.id.more_check_new_ver:
                checkNewVersion();
                break;
            case R.id.more_issue_report:
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
        Dialog alertDialog = new AlertDialog.Builder(MoreActivity.this)
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
        alertDialog.show();
    }
}
