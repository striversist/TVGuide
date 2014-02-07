package com.tools.tvguide.components;

import com.tools.tvguide.activities.AlarmAlertActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CallAlarmReceiver extends BroadcastReceiver 
{
    private static final String TAG = "CallAlarmReceiver";
    private Context mContext;
    
    @Override
    public void onReceive(Context context, Intent intent) 
    {
        mContext = context;
        
        intent.setClass(context, AlarmAlertActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
