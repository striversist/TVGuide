package com.tools.tvguide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import com.tools.tvguide.data.AlarmData;
import com.tools.tvguide.data.AlarmData.AlarmMode;
import com.tools.tvguide.data.Channel;
import com.tools.tvguide.data.GlobalData;
import com.tools.tvguide.data.Program;
import com.tools.tvguide.managers.AppEngine;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

public class AdvanceAlarmActivity extends Activity 
{
    public static final int Result_Code_Success = 100;
    public static final int Result_Code_Cancelled = 0;
    private static final String Split_Token = " ";
    
    // 标题栏
    private TextView    mProgramTitleTextView;
    private TextView    mProgramTimeTextView;
    private TextView    mChannelNameTextView;
    
    // 频率设置
    private CheckBox    mAlarmModeOnceCheckBox;
    private CheckBox    mAlarmModeDailyCheckBox;
    private CheckBox    mAlarmModeWeeklyCheckBox;
    private List<CheckBox> mAlarmModeCheckBoxList = new ArrayList<CheckBox>();
    
    // 每周几设置
    private LinearLayout mAlarmWeekLayout;
    private CheckBox    mAlarmWeek1CheckBox;
    private CheckBox    mAlarmWeek2CheckBox;
    private CheckBox    mAlarmWeek3CheckBox;
    private CheckBox    mAlarmWeek4CheckBox;
    private CheckBox    mAlarmWeek5CheckBox;
    private CheckBox    mAlarmWeek6CheckBox;
    private CheckBox    mAlarmWeek7CheckBox;
    private List<CheckBox> mAlarmWeekCheckBoxList = new ArrayList<CheckBox>();
    
    // 闹钟开始时间设置
    private LinearLayout mAlarmDateLayout;
    private LinearLayout mAlarmTimeLayout;
    private TextView    mAlarmDateSettingTextView;
    private TextView    mAlarmTimeSettingTextView;
    
    // 闹钟提前几分钟设置
    private LinearLayout mAlarmAdvanceLayout;
    private CheckBox    mAdvance5CheckBox;
    private CheckBox    mAdvance10CheckBox;
    private CheckBox    mAdvance30CheckBox;
    
    private Program     mProgram;
    private Channel     mChannel;
    private String      mAlarmDateString;
    private String      mAlarmTimeString; 
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        mProgram = (Program) getIntent().getSerializableExtra("program");
        if (mProgram == null) {
            Toast.makeText(this, "出错啦：节目为空，闹钟设置失败！", Toast.LENGTH_SHORT).show();
            return;
        }
        
        mChannel = (Channel) getIntent().getSerializableExtra("channel");
        if (mChannel == null) {
            Toast.makeText(this, "出错啦：频道为空，闹钟设置失败！", Toast.LENGTH_SHORT).show();
            return;
        }
        
        setContentView(R.layout.activity_advance_alarm);
        
        mProgramTitleTextView = (TextView) findViewById(R.id.alarm_program_title_tv);
        mProgramTimeTextView = (TextView) findViewById(R.id.alarm_program_time_tv);
        mChannelNameTextView = (TextView) findViewById(R.id.alarm_channel_name_tv);
        
        mAlarmModeOnceCheckBox = (CheckBox) findViewById(R.id.alarm_mode_once_cb);
        mAlarmModeDailyCheckBox = (CheckBox) findViewById(R.id.alarm_mode_daily_cb);
        mAlarmModeWeeklyCheckBox = (CheckBox) findViewById(R.id.alarm_mode_weekly_cb);
        mAlarmModeCheckBoxList.add(mAlarmModeOnceCheckBox);
        mAlarmModeCheckBoxList.add(mAlarmModeDailyCheckBox);
        mAlarmModeCheckBoxList.add(mAlarmModeWeeklyCheckBox);
        
        mAlarmWeekLayout = (LinearLayout) findViewById(R.id.alarm_week_ll);
        mAlarmWeek1CheckBox = (CheckBox) findViewById(R.id.alarm_week1_cb);
        mAlarmWeek2CheckBox = (CheckBox) findViewById(R.id.alarm_week2_cb);
        mAlarmWeek3CheckBox = (CheckBox) findViewById(R.id.alarm_week3_cb);
        mAlarmWeek4CheckBox = (CheckBox) findViewById(R.id.alarm_week4_cb);
        mAlarmWeek5CheckBox = (CheckBox) findViewById(R.id.alarm_week5_cb);
        mAlarmWeek6CheckBox = (CheckBox) findViewById(R.id.alarm_week6_cb);
        mAlarmWeek7CheckBox = (CheckBox) findViewById(R.id.alarm_week7_cb);
        mAlarmWeekCheckBoxList.add(mAlarmWeek1CheckBox);
        mAlarmWeekCheckBoxList.add(mAlarmWeek2CheckBox);
        mAlarmWeekCheckBoxList.add(mAlarmWeek3CheckBox);
        mAlarmWeekCheckBoxList.add(mAlarmWeek4CheckBox);
        mAlarmWeekCheckBoxList.add(mAlarmWeek5CheckBox);
        mAlarmWeekCheckBoxList.add(mAlarmWeek6CheckBox);
        mAlarmWeekCheckBoxList.add(mAlarmWeek7CheckBox);
        
        mAlarmDateLayout = (LinearLayout) findViewById(R.id.alarm_date_ll);
        mAlarmTimeLayout = (LinearLayout) findViewById(R.id.alarm_time_ll);
        mAlarmDateSettingTextView = (TextView) findViewById(R.id.alarm_date_setting_tv);
        mAlarmTimeSettingTextView = (TextView) findViewById(R.id.alarm_time_setting_tv);
        
        mAlarmAdvanceLayout = (LinearLayout) findViewById(R.id.alarm_advance_ll);
        mAdvance5CheckBox = (CheckBox) findViewById(R.id.alarm_advance5_cb);
        mAdvance10CheckBox = (CheckBox) findViewById(R.id.alarm_advance10_cb);
        mAdvance30CheckBox = (CheckBox) findViewById(R.id.alarm_advance30_cb);
        
        init();
    }
    
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.alarm_mode_once_cb:
            case R.id.alarm_mode_daily_cb:
            case R.id.alarm_mode_weekly_cb:
                updateFrequencyMode(view.getId(), ((CheckBox) view).isChecked());
                break;
            case R.id.alarm_week1_cb:
            case R.id.alarm_week2_cb:
            case R.id.alarm_week3_cb:
            case R.id.alarm_week4_cb:
            case R.id.alarm_week5_cb:
            case R.id.alarm_week6_cb:
            case R.id.alarm_week7_cb:
                updateWeekPick(view.getId());
                break;
            case R.id.alarm_date_setting_tv:
                updateDateSetting();
                break;
            case R.id.alarm_time_setting_tv:
                updateTimeSetting();
                break;
            case R.id.alarm_advance5_cb:
            case R.id.alarm_advance10_cb:
            case R.id.alarm_advance30_cb:
                updateAdvanceMinutes(view.getId());
                break;
            case R.id.save_alarm_btn:
                saveAlarm();
                break;
        }
    }
    
    private void init()
    {
        updateTitle();
        if (AppEngine.getInstance().getAlarmHelper().isAlarmSet(mChannel, mProgram)) {
            AlarmData alarmData = AppEngine.getInstance().getAlarmHelper().getAlarmData(AlarmData.makeAlarmId(mProgram, mChannel));
            String alarmStartTime = alarmData.getStartTime();
            mAlarmDateString = alarmStartTime.split(Split_Token)[0];
            mAlarmTimeString = alarmStartTime.split(Split_Token)[1];
            switch (alarmData.getMode()) {
                case Once:
                    mAlarmModeOnceCheckBox.performClick();
                    break;
                case Daily:
                    mAlarmModeDailyCheckBox.performClick();
                    break;
                case Weekly:
                    mAlarmModeWeeklyCheckBox.performClick();
                    List<Boolean> weekList = alarmData.getWeekList();
                    for (int i=0; i<weekList.size(); ++i) {
                        mAlarmWeekCheckBoxList.get(i).setChecked(weekList.get(i).booleanValue());
                    }
                    break;
            }
            List<Integer> advanceMinuteList = alarmData.getAdvanceMinuteList();
            for (Integer advanceMinute : advanceMinuteList) {
                if (advanceMinute == 5) {
                    mAdvance5CheckBox.setChecked(true);
                } else if (advanceMinute == 10) {
                    mAdvance10CheckBox.setChecked(true);
                } else if (advanceMinute == 30) {
                    mAdvance30CheckBox.setChecked(true);
                }
            }
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat(GlobalData.DATE_FORMAT, Locale.ENGLISH);
            mAlarmDateString = sdf.format(Calendar.getInstance().getTime());
            mAlarmTimeString = mProgram.time;
            mAlarmModeOnceCheckBox.performClick();            
        }
        
        mAlarmDateSettingTextView.setText(mAlarmDateString);
        mAlarmTimeSettingTextView.setText(mAlarmTimeString);
    }
    
    private void updateTitle()
    {
        mProgramTitleTextView.setText(mProgram.title);
        mProgramTimeTextView.setText(mProgram.time);
        mChannelNameTextView.setText(mChannel.name);
    }
    
    private void updateFrequencyMode(int id, boolean checked)
    {
        for (CheckBox checkBox : mAlarmModeCheckBoxList) {
            if (checkBox.getId() != id) {
                checkBox.setChecked(false);
            }
        }
        
        if (checked) {
            switch (id) {
                case R.id.alarm_mode_once_cb:
                    mAlarmWeekLayout.setVisibility(View.GONE);
                    mAlarmDateLayout.setVisibility(View.VISIBLE);
                    mAlarmTimeLayout.setVisibility(View.VISIBLE);
                    mAlarmAdvanceLayout.setVisibility(View.VISIBLE);
                    break;
                case R.id.alarm_mode_daily_cb:
                    mAlarmWeekLayout.setVisibility(View.GONE);
                    mAlarmDateLayout.setVisibility(View.GONE);
                    mAlarmTimeLayout.setVisibility(View.VISIBLE);
                    mAlarmAdvanceLayout.setVisibility(View.VISIBLE);
                    break;
                case R.id.alarm_mode_weekly_cb:
                    mAlarmWeekLayout.setVisibility(View.VISIBLE);
                    mAlarmDateLayout.setVisibility(View.GONE);
                    mAlarmTimeLayout.setVisibility(View.VISIBLE);
                    mAlarmAdvanceLayout.setVisibility(View.VISIBLE);
                    break;
            }
        } else {
            if (isAlarmCanceled()) {
                mAlarmWeekLayout.setVisibility(View.GONE);
                mAlarmDateLayout.setVisibility(View.GONE);
                mAlarmTimeLayout.setVisibility(View.GONE);
                mAlarmAdvanceLayout.setVisibility(View.GONE);
                Toast.makeText(this, "闹钟已取消", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private boolean isAlarmCanceled()
    {
        boolean allUnChecked = true;
        for (CheckBox checkBox : mAlarmModeCheckBoxList) {
            if (checkBox.isChecked()) {
                allUnChecked = false;
                break;
            }
        }
        return allUnChecked;
    }
    
    private void updateWeekPick(int id)
    {
        
    }
    
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setWeekPickEnable(boolean enable)
    {
        for (CheckBox checkBox : mAlarmWeekCheckBoxList) {
            if (enable) {
                if (Build.VERSION.SDK_INT >= 11) {
                    checkBox.setAlpha((float) 1.0);
                }
                checkBox.setClickable(true);
            } else {
                if (Build.VERSION.SDK_INT >= 11) {
                    checkBox.setAlpha((float) 0.3);
                }
                checkBox.setClickable(false);
            }
        }
    }
    
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void updateDateSetting()
    {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        if (mAlarmDateString != null) {
            try {
                calendar.setTime(new SimpleDateFormat(GlobalData.DATE_FORMAT, Locale.ENGLISH).parse(mAlarmDateString));
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH); 
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        
        if (Build.VERSION.SDK_INT >= 11) {
            DatePickerDialog dialog = new DatePickerDialog(this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, new OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(year, monthOfYear, dayOfMonth);
                    mAlarmDateString = new SimpleDateFormat(GlobalData.DATE_FORMAT, Locale.ENGLISH).format(calendar.getTime());
                    mAlarmDateSettingTextView.setText(mAlarmDateString);
                }
            }, year, month, dayOfMonth);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
        } else {
            new DatePickerDialog(this, new OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(year, monthOfYear, dayOfMonth);
                    mAlarmDateString = new SimpleDateFormat(GlobalData.DATE_FORMAT, Locale.ENGLISH).format(calendar.getTime());
                    mAlarmDateSettingTextView.setText(mAlarmDateString);
                }
            }, year, month, dayOfMonth).show();
        }
    }
    
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void updateTimeSetting()
    {
        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        if (mAlarmTimeString != null) {
            try {
                calendar.setTime(new SimpleDateFormat(GlobalData.TIME_FORMAT, Locale.ENGLISH).parse(mAlarmTimeString));
                hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
                minute = calendar.get(Calendar.MINUTE);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        
        if (Build.VERSION.SDK_INT >= 11) {
            TimePickerDialog dialog = new TimePickerDialog(this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, new OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    mAlarmTimeString = new SimpleDateFormat(GlobalData.TIME_FORMAT, Locale.ENGLISH).format(calendar.getTime());
                    mAlarmTimeSettingTextView.setText(mAlarmTimeString);
                }
            }, hourOfDay, minute, true);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
        } else {
            new TimePickerDialog(this, new OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    mAlarmTimeString = new SimpleDateFormat(GlobalData.TIME_FORMAT, Locale.ENGLISH).format(calendar.getTime());
                    mAlarmTimeSettingTextView.setText(mAlarmTimeString);
                }
            }, hourOfDay, minute, true).show();
        }
    }
    
    private void updateAdvanceMinutes(int id)
    {
        
    }
    
    private boolean checkCorrect()
    {
        if (isAlarmCanceled())
            return true;
        
        if (mAlarmModeWeeklyCheckBox.isChecked()) {
            boolean hasWeekPick = false;
            for (CheckBox checkBox : mAlarmWeekCheckBoxList) {
                if (checkBox.isChecked()) {
                    hasWeekPick = true;
                    break;
                }
            }
            if (!hasWeekPick) {
                Toast.makeText(this, "请选择每周几提醒！", Toast.LENGTH_LONG).show();
                return false;
            }
        } else if (mAlarmModeOnceCheckBox.isChecked()) {
            if (mAlarmDateString == null || mAlarmTimeString == null) {
                Toast.makeText(this, "没有选择日期和时间！", Toast.LENGTH_LONG).show();
                return false;
            } else {
                try {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(new SimpleDateFormat(GlobalData.FULL_TIME_FORMAT, Locale.ENGLISH).parse(mAlarmDateString + Split_Token + mAlarmTimeString));
                    if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                        Toast.makeText(this, "闹钟时间已过期，请重新设置！", Toast.LENGTH_LONG).show();
                        return false;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "程序错误：闹钟日期格式不对！", Toast.LENGTH_LONG).show();
                    return false;
                }
            }
        }
        
        return true;
    }
    
    private void saveAlarm()
    {
        Intent data = new Intent();
        data.putExtra("program", mProgram);
        
        if (isAlarmCanceled()) {
            AppEngine.getInstance().getAlarmHelper().removeAlarmData(AlarmData.makeAlarmId(mProgram, mChannel));
            setResult(Result_Code_Cancelled, data);
            finish();
            return;
        }
        
        if (!checkCorrect())
            return;
        
        List<Integer> advanceMinuteList = new ArrayList<Integer>();
        if (mAdvance5CheckBox.isChecked()) {
            advanceMinuteList.add(Integer.valueOf(5));
        }
        if (mAdvance10CheckBox.isChecked()) {
            advanceMinuteList.add(Integer.valueOf(10));
        }
        if (mAdvance30CheckBox.isChecked()) {
            advanceMinuteList.add(Integer.valueOf(30));
        }
        
        AlarmData alarmData = null;
        if (mAlarmModeOnceCheckBox.isChecked()) {
            alarmData = new AlarmData(mProgram, mChannel, AlarmMode.Once);
        } else if (mAlarmModeDailyCheckBox.isChecked()) {
            alarmData = new AlarmData(mProgram, mChannel, AlarmMode.Daily);
        } else if (mAlarmModeWeeklyCheckBox.isChecked()) {
            alarmData = new AlarmData(mProgram, mChannel, AlarmMode.Weekly);
            List<Boolean> weekList = new ArrayList<Boolean>(7);
            for (int i=0; i<mAlarmWeekCheckBoxList.size(); ++i) {
                weekList.add(mAlarmWeekCheckBoxList.get(i).isChecked());
            }
            alarmData.setWeekList(weekList);
        }
        
        alarmData.setStartTime(mAlarmDateString + Split_Token + mAlarmTimeString);
        alarmData.setAdvanceMinuteList(advanceMinuteList);
        
        // TODO: for test, remove in the future
        long nextAlarmTime = alarmData.getNextAlarmTriggerTime();
        Log.d("", "" + nextAlarmTime);
        
        AppEngine.getInstance().getAlarmHelper().addAlarmData(alarmData);
        
        data.putExtra("alarm_data", alarmData);
        setResult(Result_Code_Success, data);
        
        finish();
    }
}

