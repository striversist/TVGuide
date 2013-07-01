package com.tools.tvguide.activities;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;

public class MoreActivity extends Activity 
{
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more);
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
}
