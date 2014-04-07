package com.tools.tvguide.managers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import com.tools.tvguide.components.CallAlarmReceiver;
import com.tools.tvguide.components.Shutter;
import com.tools.tvguide.data.AlarmData;
import com.tools.tvguide.data.Channel;
import com.tools.tvguide.data.Program;
import com.tools.tvguide.utils.Utility;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class AlarmHelper implements Shutter
{
    public static final String TAG = "AlarmHelper";
    private Context mContext;
    private LinkedHashMap<String, HashMap<String, String>> mRecords;
    private final String SEPERATOR = "#";
    private boolean mSettingChanged = false;
    private List<AlarmListener> mListeners;
    private String FILE_ALARM_HELPER = "advance_alarm_settings.txt";
    private final int MIN_DAY = 1;  // 本周一
    private final int MAX_DAY = 14; // 下周日
    
    private List<AlarmData> mAlarmDataList = new ArrayList<AlarmData>();
    
    public interface AlarmListener
    {
        public void onAlarmed(AlarmData alarmData);
    }
    
    public AlarmHelper(Context context)
    {
        assert(context != null);
        mContext = context;
        mListeners = new ArrayList<AlarmHelper.AlarmListener>();
        loadAlarmSettings();
    }
    
//    private void checkInitialized() 
//    {
//        if (mRecords == null)
//            throw new IllegalStateException("mRecords is null");
//    }
    
//    public void addAlarm(String channelId, String channelName, String program, int day, long triggerAtMillis)
//    {
//        assert(day >= MIN_DAY && day <= MAX_DAY);
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeInMillis(triggerAtMillis);
////        Log.d(TAG, "addAlarm: channel=" + channelName + ", program=" + program + ", triggerTime=" + calendar.getTime().toString());
//        String key = makeKey(channelId, channelName, program, day);
//        if (isAlarmSet(channelId, channelName, program, day))
//            removeAlarm(channelId, channelName, program, day);
//        
//        HashMap<String, String> info = new HashMap<String, String>();
//        info.put("channel_id", channelId);
//        info.put("channel_name", channelName);
//        info.put("program", program);
//        info.put("day", String.valueOf(day));
//        if (hasConflictWithOthers(triggerAtMillis))
//        {
//            long thresholdAtMillis = 5000;
//            long tuning = key.hashCode() % 20000;
//            if (Math.abs(tuning) < thresholdAtMillis)
//            {
//                if (tuning >= 0)
//                    tuning += thresholdAtMillis;
//                else
//                    tuning += thresholdAtMillis * -1;
//            }
//            triggerAtMillis += tuning;
//            calendar.setTimeInMillis(triggerAtMillis);
////            Log.d(TAG, "addAlarm: has conflict, adjust=" + tuning + ", to" + calendar.getTime().toString());
//        }
//        info.put("time", Long.toString(triggerAtMillis));
//        mRecords.put(key, info);
//        
//        // 指定闹钟设置的时间到时，要运行的CallAlarm.class  
//        Intent intent = new Intent(mContext, CallAlarmReceiver.class);
//        intent.putExtra("channel_id", channelId);
//        intent.putExtra("channel_name", channelName);
//        intent.putExtra("program", program);
//        intent.putExtra("day", String.valueOf(day));
//        
////        Log.d(TAG, "addAlarm: alarm id = " + key.hashCode());
//        PendingIntent sender = PendingIntent.getBroadcast(mContext, key.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
//        am.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, sender);
//        mSettingChanged = true;
//    }
//    
//    public void removeAlarm(String channelId, String channelName, String program, int day)
//    {
//        String key = makeKey(channelId, channelName, program, day);
//        mRecords.remove(key);
//        
//        Intent intent = new Intent(mContext, CallAlarmReceiver.class);
//        intent.putExtra("channel_id", channelId);
//        intent.putExtra("channel_name", channelName);
//        intent.putExtra("program", program);
//        intent.putExtra("day", String.valueOf(day));
//        
//        PendingIntent sender = PendingIntent.getBroadcast(mContext, key.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
//        am.cancel(sender);
//        mSettingChanged = true;
//        saveAlarmSettings();        // removeAlarm 可能会在主程序未启动时调用到，所以不能依靠shutDown()中来存储
//    }
    
    public void notifyAlarmListeners(AlarmData alarmData)
    {
        for (int i=0; i<mListeners.size(); ++i)
            mListeners.get(i).onAlarmed(alarmData);
    }
    
    public void addAlarmListener(AlarmListener listener)
    {
        assert (listener != null);
        mListeners.add(listener);
    }
    
    public void removeAlarmListener(AlarmListener listener)
    {
        assert (listener != null);
        mListeners.remove(listener);
    }
    
//    public HashMap<String, HashMap<String, String>> getAllRecords()
//    {
//        return mRecords;
//    }
    
//    public long getAlarmTimeAtMillis(String channelId, String channelName, String program, int day)
//    {
//        if (isAlarmSet(channelId, channelName, program, day))
//            return Long.valueOf(mRecords.get(makeKey(channelId, channelName, program, day)).get("time"));
//        return -1;
//    }
    
//    public boolean isAlarmSet(String channelId, String channelName, String program, int day)
//    {
//        assert (day>= MIN_DAY && day<= MAX_DAY);
//        return mRecords.containsKey(makeKey(channelId, channelName, program, day));
//    }
    
    public void resetAllAlarms()
    {
        for (AlarmData alarmData : mAlarmDataList) {
            removeAlarmData(alarmData);
            addAlarmData(alarmData);
        }
    }
    
//    private int tryToGetWeekdayByMillis(long triggerAtMillis)
//    {
//        long durationMs = triggerAtMillis - Utility.getMillisSinceWeekBegin();
//        if (durationMs < 0) // Error
//            return MIN_DAY;
//        
//        int value = new BigDecimal((double)durationMs / 1000 / 3600 / 24).setScale(0, BigDecimal.ROUND_UP).intValue();
//        return value;
//    }
//    
//    private String makeKey(String channelId, String channelName, String program, int day)
//    {
//        if (day == 0)   // 为兼容之前的版本
//            return channelId + SEPERATOR + channelName + SEPERATOR + program;
//        return channelId + SEPERATOR + channelName + SEPERATOR + program + SEPERATOR + String.valueOf(day);
//    }
//    
//    private boolean hasConflictWithOthers(long triggerAtMillis)
//    {
//        Iterator<Entry<String, HashMap<String, String>>> iter = mRecords.entrySet().iterator();
//        while (iter.hasNext())
//        {
//            Entry<String, HashMap<String, String>> entry = iter.next();
//            HashMap<String, String> info = entry.getValue();
//            if (info.get("time") != null)
//            {
//                long triggerTime = Long.valueOf(info.get("time")).longValue();
//                if (triggerTime == triggerAtMillis)
//                    return true;
//            }
//        }
//        return false;
//    }
    
    public List<AlarmData> getAllRecords()
    {
        return mAlarmDataList;
    }
    
    public boolean isAlarmSet(Channel channel, Program program)
    {
        if (channel == null || program == null)
            return false;
        
        for (int i=0; i<mAlarmDataList.size(); ++i) {
            if (mAlarmDataList.get(i).getAlarmId() == AlarmData.makeAlarmId(program, channel)) {
                return true;
            }
        }
        
        return false;
    }
    
    public boolean addAlarmData(AlarmData alarmData)
    {
        if (alarmData == null)
            return false;
        
        boolean addNew = true;
        for (int i=0; i<mAlarmDataList.size(); ++i) {
            if (mAlarmDataList.get(i).getAlarmId() == alarmData.getAlarmId()) {
                mAlarmDataList.set(i, alarmData);
                addNew = false;
            }
        }
        if (addNew) {
            mAlarmDataList.add(alarmData);
        }
        
        // 指定闹钟设置的时间到时，要运行的CallAlarmReceiver.class
        Intent intent = new Intent(mContext, CallAlarmReceiver.class);
        intent.putExtra("alarm_data", alarmData);
        PendingIntent sender = PendingIntent.getBroadcast(mContext, alarmData.getAlarmId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, alarmData.getNextAlarmTriggerTime(), sender);
        mSettingChanged = true;
        
        return true;
    }
    
    public AlarmData removeAlarmData(AlarmData alarmData)
    {
        if (alarmData == null)
            return null;
        
        AlarmData result = null;
        int removeIndex = -1;
        for (int i=0; i<mAlarmDataList.size(); ++i) {
            if (mAlarmDataList.get(i).getAlarmId() == alarmData.getAlarmId()) {
                removeIndex = i;
                break;
            }
        }
        
        if (removeIndex >= 0) {
            result = mAlarmDataList.remove(removeIndex);
            Intent intent = new Intent(mContext, CallAlarmReceiver.class);
            intent.putExtra("alarm_data", result);
            PendingIntent sender = PendingIntent.getBroadcast(mContext, result.getAlarmId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
            am.cancel(sender);
            mSettingChanged = true;
            saveAlarmSettings();        // removeAlarm 可能会在主程序未启动时调用到，所以不能依靠shutDown()中来存储
        }
        
        return result;
    }
    
    @Override
    public void onShutDown()
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
            oos.writeObject(mAlarmDataList);
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
        try
        {
            FileInputStream fis = mContext.openFileInput(FILE_ALARM_HELPER);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Object obj = ois.readObject();
            if (obj instanceof List<?>)
            {
                mAlarmDataList =  (ArrayList<AlarmData>) obj;
            }
            ois.close();
            fis.close();
        }
        catch (StreamCorruptedException e)
        {
            e.printStackTrace();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }
}
