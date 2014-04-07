package com.tools.tvguide.data;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.tools.tvguide.utils.Utility;

public class AlarmData implements Serializable 
{
    private static final long serialVersionUID = 1L;
    public enum AlarmMode { Once, Daily, Weekly }
    
    private Program     mRelatedProgram;
    private Channel     mRelatedChannel;
    private AlarmMode   mMode;
    
    private String        mAlarmStartTime;  // Date + Time: yyyy-MM-dd HH:mm
    private List<Boolean> mWeekList                 = new ArrayList<Boolean>();        // Mode: Weekly
    private List<Integer> mAdvanceMinuteSortedList  = new ArrayList<Integer>();         // 提前几分钟(如5, 15, 30)，从小到大排序
    
    public AlarmData(Program relatedProgram, Channel relatedChannel, AlarmMode mode)
    {
        assert (relatedProgram != null);
        assert (relatedChannel != null);
        assert (mode != null);
        
        mRelatedProgram = relatedProgram;
        mRelatedChannel = relatedChannel;
        mMode = mode;
    }
    
    public static int makeAlarmId(Program relatedProgram, Channel relatedChannel)
    {
        assert (relatedProgram != null);
        assert (relatedChannel != null);
        String tvmaoId = relatedChannel.tvmaoId;
        String time = relatedProgram.time;
        String title = relatedProgram.title;
        if (tvmaoId != null && time != null && title != null) {
            return tvmaoId.hashCode() + time.hashCode() + title.hashCode();
        }
        
        return -1;
    }
    
    public int getAlarmId()
    {
        return makeAlarmId(mRelatedProgram, mRelatedChannel);
    }
    
    public AlarmMode getMode()
    {
        return mMode;
    }
    
    public void setMode(AlarmMode mode)
    {
        assert (mode != null);
        mMode = mode;
    }
    
    public Program getRelatedProgram()
    {
        return mRelatedProgram;
    }
    
    public Channel getRelatedChannel()
    {
        return mRelatedChannel;
    }
    
    public String getStartTime()
    {
        return mAlarmStartTime;
    }
    
    public boolean setStartTime(String fullTime)
    {
        assert (fullTime != null);
        SimpleDateFormat sdf = new SimpleDateFormat(GlobalData.DATE_FORMAT, Locale.ENGLISH);
        try {
            sdf.parse(fullTime);
            mAlarmStartTime = fullTime;
            return true;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean setWeekList(List<Boolean> weekList)
    {
        if (weekList != null) {
            mWeekList.clear();
            mWeekList.addAll(weekList);
            return true;
        }
        return false;
    }
    
    public List<Boolean> getWeekList()
    {
        return mWeekList;
    }
    
    public boolean setAdvanceMinuteList(List<Integer> advanceList)
    {
        if (advanceList != null) {
            mAdvanceMinuteSortedList.clear();
            mAdvanceMinuteSortedList.addAll(advanceList);
            Collections.sort(mAdvanceMinuteSortedList);
            return true;
        }
        return false;
    }
    
    public List<Integer> getAdvanceMinuteList()
    {
        return mAdvanceMinuteSortedList;
    }
    
    /**
     * 获取时间点列表（提前时间从小到大）
     * @param orignTriggerTime
     * @return 返回提前时间后的时间点List；若无提前时间，则列表中只有一个元素（即传入的时间点）
     */
    private List<Long> getAdvanceTriggerList(long orignTriggerTime)
    {
        List<Long> result = new ArrayList<Long>();
        result.add(Long.valueOf(orignTriggerTime));
        if (mAdvanceMinuteSortedList == null) {
            return result;
        }
        
        for (int i=0; i<mAdvanceMinuteSortedList.size(); ++i) {
            result.add(Long.valueOf(orignTriggerTime - mAdvanceMinuteSortedList.get(i) * GlobalData.ONE_MINUTE_MS));
        }
        
        return result;
    }
    
    /**
     * 获取最合适的闹钟时间，若闹钟都过期，则返回-1
     * @param originTriggerTime
     * @return
     */
    private long findMostProperAlarmTime(long originTriggerTime)
    {
        List<Long> advanceTriggerList = getAdvanceTriggerList(originTriggerTime);
        for (int i=advanceTriggerList.size()-1; i>=0; --i) {
            if (advanceTriggerList.get(i) > System.currentTimeMillis()) {   // 还未闹
                return advanceTriggerList.get(i);
            }
        }
        return -1;
    }
    
    private boolean hasWeekdaySet()
    {
        boolean hasSet = false;
        for (Boolean isSet : mWeekList) {
            if (isSet) {
                hasSet = true;
                break;
            }
        }
        
        return hasSet;
    }
    
    /**
     * 从传入weekday开始算起，找到设置过的最接近的日期
     * @param weekday (1-7 表示周一至周日)
     * @return -1(未找到)
     */
    private int findMostCloseWeekDay(int weekday)
    {
        if (weekday <=0 || weekday > 7)
            return -1;
        
        if (mWeekList == null)
            return -1;
        
        if (!hasWeekdaySet())
            return -1;
        
        // 本周开始
        for (int i=weekday-1; i<mWeekList.size(); ++i) {
            if (mWeekList.get(i).booleanValue()) {
                return i + 1;
            }
        }
        
        // 从下周开始
        for (int i=0; i<mWeekList.size(); ++i) {
            if (mWeekList.get(i).booleanValue()) {
                return i + 1;
            }
        }
        
        return -1;
    }
    
    /**
     * 获取明天的星期数（1-7: 周一至周日）
     * @param weekday
     * @return
     */
    private int getNextWeekday(int weekday)
    {
        if (weekday <=0 || weekday > 7)
            return -1;
        
        if (weekday == 7)
            return 1;
        
        return weekday + 1;
    }
    
    /**
     * 获取下一次闹钟绝对时间(ms)
     * @return：若没有下一次时间，则返回-1。此时调用者负责将对象移除
     */
    public long getNextAlarmTriggerTime()
    {
        long nextTriggerTime = -1L;
        if (mAlarmStartTime == null)
            return -1;
        
        Calendar startCalendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(GlobalData.FULL_TIME_FORMAT, Locale.ENGLISH);
        try {
            startCalendar.setTime(sdf.parse(mAlarmStartTime));
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
        
        switch (mMode) {
            case Once:
                nextTriggerTime = findMostProperAlarmTime(startCalendar.getTimeInMillis());
                break;
            case Daily:
                Calendar dailyCalendar = Calendar.getInstance();
                dailyCalendar.set(Calendar.HOUR_OF_DAY, startCalendar.get(Calendar.HOUR_OF_DAY));
                dailyCalendar.set(Calendar.MINUTE, startCalendar.get(Calendar.MINUTE));
                nextTriggerTime = findMostProperAlarmTime(dailyCalendar.getTimeInMillis());
                if (nextTriggerTime < 0) {  // 未找到
                    nextTriggerTime = findMostProperAlarmTime(dailyCalendar.getTimeInMillis() + GlobalData.ONE_DAY_MS);
                }
                break;
            case Weekly:
                if (!hasWeekdaySet())
                    break;
                
                Calendar weekCalendar = Calendar.getInstance();
                int weekToday = Utility.getProxyDay(weekCalendar.get(Calendar.DAY_OF_WEEK));
                int nextWeekday = findMostCloseWeekDay(weekToday);
                if (nextWeekday < 0)  // 未找到
                    break;
                
                long originWeekMillis = weekCalendar.getTimeInMillis();
                weekCalendar.set(Calendar.DAY_OF_WEEK, Utility.getCalendarDayOfWeek(nextWeekday));
                if (weekCalendar.getTimeInMillis() < originWeekMillis) {    // 时间往前了，需要加一周
                    weekCalendar.setTimeInMillis(weekCalendar.getTimeInMillis() + GlobalData.ONE_WEEK_MS);
                }
                nextTriggerTime = findMostProperAlarmTime(weekCalendar.getTimeInMillis());
                
                if (nextTriggerTime < 0) {  // 闹钟时间已过
                    nextWeekday = findMostCloseWeekDay(getNextWeekday(weekToday));
                    if (nextWeekday < 0)
                        break;
                    
                    originWeekMillis = weekCalendar.getTimeInMillis();
                    weekCalendar.set(Calendar.DAY_OF_WEEK, Utility.getCalendarDayOfWeek(nextWeekday));
                    if (weekCalendar.getTimeInMillis() <= originWeekMillis) {    // 时间往前了，需要加一周（注意：也不能相等）
                        weekCalendar.setTimeInMillis(weekCalendar.getTimeInMillis() + GlobalData.ONE_WEEK_MS);
                    }
                    nextTriggerTime = findMostProperAlarmTime(weekCalendar.getTimeInMillis());
                }
                break;
        }
        
        return nextTriggerTime;
    }
}
