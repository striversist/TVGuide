package com.tools.tvguide.managers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
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
    private boolean mSettingChanged = false;
    private String FILE_ALARM_HELPER = "alarm_settings.txt";
    
    public AlarmHelper(Context context)
    {
        mContext = context;
        loadAlarmSettings();
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
        mSettingChanged = true;
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
        mSettingChanged = true;
    }
    
    public long getAlarmTimeAtMillis(String channelName, String program)
    {
        if (isAlarmSet(channelName, program))
            return Long.valueOf(mRecords.get(makeKey(channelName, program)).get("time"));
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
    
    public void shutDown()
    {
        if (mSettingChanged)
        {
            saveAlarmSettings();
        }
    }
    
    private void saveAlarmSettings()
    {
        try
        {
            FileOutputStream fos = mContext.openFileOutput(FILE_ALARM_HELPER, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(mRecords);
            oos.flush();
            oos.close();
            fos.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    @SuppressWarnings("unchecked")
    private void loadAlarmSettings()
    {
        boolean loadSuccess = true;
        try
        {
            FileInputStream fis = mContext.openFileInput(FILE_ALARM_HELPER);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Object obj = ois.readObject();
            if (obj instanceof HashMap<?, ?>)
            {
                mRecords =  (HashMap<String, HashMap<String, String>>) obj;
            }
            else 
            {
                loadSuccess = false;
            }
            ois.close();
            fis.close();
        }
        catch (StreamCorruptedException e)
        {
            loadSuccess = false;
            e.printStackTrace();
        }
        catch (FileNotFoundException e)
        {
            loadSuccess = false;
            e.printStackTrace();
        }
        catch (IOException e)
        {
            loadSuccess = false;
            e.printStackTrace();
        }
        catch (ClassNotFoundException e)
        {
            loadSuccess = false;
            e.printStackTrace();
        }
        
        if (loadSuccess == false)
        {
            mRecords = new HashMap<String, HashMap<String, String>>();
        }
    }
}
