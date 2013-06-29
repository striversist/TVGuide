package com.tools.tvguide.managers;

import java.util.HashMap;

import com.tools.tvguide.utils.CallAlarmReceiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class AlarmHelper
{
    private Context mContext;
    private HashMap<String, HashMap<String, String>> mRecords;
    private final String SEPERATOR = "#";
    
    public AlarmHelper(Context context)
    {
        mContext = context;
        mRecords = new HashMap<String, HashMap<String,String>>();
    }
    
    public void addAlarm(String channelName, String program, long triggerAtMillis)
    {
        HashMap<String, String> info = new HashMap<String, String>();
        info.put("channel", channelName);
        info.put("program", program);
        String key = makeKey(channelName, program, triggerAtMillis);
        mRecords.put(key, info);
        
        // 指定闹钟设置的时间到时，要运行的CallAlarm.class  
        Intent intent = new Intent(mContext, CallAlarmReceiver.class);
        intent.putExtra("channel", channelName);
        intent.putExtra("program", program);
        PendingIntent sender = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, sender);
    }
    
    public void isAlarmSet(String channelName, String program)
    {
    }
    
    private String makeKey(String channelName, String program, long triggerAtMillis)
    {
        return channelName + SEPERATOR + program + SEPERATOR + triggerAtMillis;
    }
}
