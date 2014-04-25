package com.tools.tvguide.managers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

import com.tools.tvguide.components.CallAlarmReceiver;
import com.tools.tvguide.components.Shutter;
import com.tools.tvguide.data.AlarmData;
import com.tools.tvguide.data.Channel;
import com.tools.tvguide.data.Program;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class AlarmHelper implements Shutter
{
    public static final String TAG = "AlarmHelper";
    private Context mContext;
    private boolean mSettingChanged = false;
    private List<AlarmListener> mListeners;
    private String FILE_ALARM_HELPER = "advance_alarm_settings.txt";
    
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
    
    public synchronized void resetAllAlarms()
    {
        for (AlarmData alarmData : mAlarmDataList) {
            removeAlarmData(alarmData);
            addAlarmData(alarmData);
        }
    }
    
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
    
    public AlarmData getAlarmData(int alarmId)
    {
        for (AlarmData alarmData : mAlarmDataList) {
            if (alarmData.getAlarmId() == alarmId) {
                return alarmData;
            }
        }
        return null;
    }
    
    public AlarmData removeAlarmData(int alarmId)
    {
        AlarmData alarmData = getAlarmData(alarmId);
        if (alarmData == null)
            return null;
        
        return removeAlarmData(alarmData);
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
            if (new File(FILE_ALARM_HELPER).exists()) {
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
