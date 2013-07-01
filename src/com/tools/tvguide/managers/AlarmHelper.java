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
        String key = makeKey(channelName, program);
        if (isAlarmSet(channelName, program))
            removeAlarm(channelName, program);
        
        HashMap<String, String> info = new HashMap<String, String>();
        info.put("channel", channelName);
        info.put("program", program);
        info.put("time", Long.toString(triggerAtMillis));
        mRecords.put(key, info);
        
        // 指定闹钟设置的时间到时，要运行的CallAlarm.class  
        Intent intent = new Intent(mContext, CallAlarmReceiver.class);
        intent.putExtra("channel", channelName);
        intent.putExtra("program", program);
        PendingIntent sender = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, sender);
    }
    
    public void removeAlarm(String channelName, String program)
    {
        String key = makeKey(channelName, program);
        mRecords.remove(key);
        
        Intent intent = new Intent(mContext, CallAlarmReceiver.class);
        intent.putExtra("channel", channelName);
        intent.putExtra("program", program);
        PendingIntent sender = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        am.cancel(sender);
    }
    
    public long getAlarmTimeAtMillis(String channelName, String program)
    {
        if (isAlarmSet(channelName, program))
        {
            String key = makeKey(channelName, program);
            String time = mRecords.get(key).get("time");
            long timeMillis = Long.valueOf(time);
            return timeMillis;
        }
        return -1;
    }
    
    public boolean isAlarmSet(String channelName, String program)
    {
        return mRecords.containsKey(makeKey(channelName, program));
    }
    
    private String makeKey(String channelName, String program)
    {
        return channelName + SEPERATOR + program;
    }
}
