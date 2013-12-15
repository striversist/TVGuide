package com.tools.tvguide.activities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.tools.tvguide.R;
import com.tools.tvguide.adapters.ResultProgramAdapter;
import com.tools.tvguide.components.MyProgressDialog;
import com.tools.tvguide.managers.AppEngine;
import com.tools.tvguide.managers.ContentManager;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.R.array;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class ChannelDetailActivity extends Activity 
{
    private static final String TAG = "ChannelDetailActivity";
    private String mChannelName;
    private String mChannelId;
    private TextView mChannelNameTV;
    private TextView mDateTV;
    private ListView mProgramLV;
    private ListView mDateChosenLV;
    
    private List<HashMap<String, String>> mProgramList;             // Key: time, title
    private List<HashMap<String, String>> mOnPlayingProgram;        // Key: time, title
    private MyProgressDialog mProgressDialog;
    private int mCurrentSelectedDay;
    private List<ResultProgramAdapter.IListItem> mItemDataList;
    
    private enum SelfMessage {MSG_UPDATE_PROGRAMS, MSG_UPDATE_ONPLAYING_PROGRAM};

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_detail);
        
        // configure the SlidingMenu
        SlidingMenu menu = new SlidingMenu(this);
        menu.setMode(SlidingMenu.LEFT_RIGHT);
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        menu.setShadowWidthRes(R.dimen.shadow_width);
        menu.setShadowDrawable(R.drawable.shadow);
        menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        menu.setFadeDegree(0.35f);
        menu.setMenu(R.layout.channel_detail_left);
        menu.setSecondaryMenu(R.layout.channel_detail_right);
        menu.setSecondaryShadowDrawable(R.drawable.shadowright);
        menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        
        mChannelId = getIntent().getStringExtra("id");
        mChannelName = getIntent().getStringExtra("name");
        mProgramList = new ArrayList<HashMap<String,String>>();
        mOnPlayingProgram = new ArrayList<HashMap<String,String>>();
        mProgressDialog = new MyProgressDialog(this);
        mCurrentSelectedDay = 1;
        mItemDataList = new ArrayList<ResultProgramAdapter.IListItem>();
     
        initViews();
        
        updateProgramList();
    }
    
    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }
    
    @Override
    public void onNewIntent (Intent intent)
    {
        setIntent(intent);
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        
        // 检测屏幕的方向：纵向或横向  
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            // 当前为横屏， 在此处添加额外的处理代码 
        }
        else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
        {
            // 当前为竖屏， 在此处添加额外的处理代码  
        }
    }
    
    public void initViews()
    {
        mChannelNameTV = (TextView) findViewById(R.id.channeldetail_channel_name_tv);
        mDateTV = (TextView) findViewById(R.id.channeldetail_date_tv);
        mProgramLV = (ListView) findViewById(R.id.channeldetail_program_listview);
        mDateChosenLV = (ListView) findViewById(R.id.channeldetail_date_chosen_listview);
        
        mChannelNameTV.setText(mChannelName);
    }

    private void updateProgramList()
    {
        boolean isSyncLoad = AppEngine.getInstance().getContentManager().loadProgramsByChannel2(mChannelId, getHostDay(mCurrentSelectedDay), mProgramList, 
                                mOnPlayingProgram, new ContentManager.LoadListener() 
        {
            @Override
            public void onLoadFinish(int status) 
            {
                uiHandler.sendEmptyMessage(SelfMessage.MSG_UPDATE_PROGRAMS.ordinal());
            }
        });
        if (isSyncLoad == false)
            mProgressDialog.show();
    }
    
    private void updateOnplayingProgram()
    {
        AppEngine.getInstance().getContentManager().loadOnPlayingProgramByChannel(mChannelId, mOnPlayingProgram, new ContentManager.LoadListener() 
        {    
            @Override
            public void onLoadFinish(int status) 
            {
                uiHandler.sendEmptyMessage(SelfMessage.MSG_UPDATE_ONPLAYING_PROGRAM.ordinal());
            }
        });
    }

    @Override
    public void onBackPressed() 
    {
        finish();
    };
    
    public void back(View view)
    {
        if (view instanceof Button)
        {
            // The same effect with press back key
            finish();
        }
    }
    
    public void collectChannel(View view)
    {

    }
    
    public void selectDay(View view)
    {

    }
    
    /*
     * Trasfer the day to the server host day: Monday~Sunday -> 1~7
     */
    private int getHostDay(int day)
    {
        assert(day >=1 && day <=7);
        int hostDay = 0;
        switch (day)
        {
            case Calendar.MONDAY:
                hostDay = 1;
                break;
            case Calendar.TUESDAY:
                hostDay = 2;
                break;
            case Calendar.WEDNESDAY:
                hostDay = 3;
                break;
            case Calendar.THURSDAY:
                hostDay = 4;
                break;
            case Calendar.FRIDAY:
                hostDay = 5;
                break;
            case Calendar.SATURDAY:
                hostDay = 6;
                break;
            case Calendar.SUNDAY:
                hostDay = 7;
                break;
        }
        return hostDay;
    }
    
    private Handler uiHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            SelfMessage selfMsg = SelfMessage.values()[msg.what];
            switch (selfMsg) 
            {
                case MSG_UPDATE_PROGRAMS:
                    mItemDataList.clear();
                    mProgressDialog.dismiss();
                    for (int i=0; i<mProgramList.size(); ++i)
                    {
                        String time = mProgramList.get(i).get("time");
                        String title = mProgramList.get(i).get("title");
                        
                        ResultProgramAdapter.Item item = new ResultProgramAdapter.Item();
                        item.time = time;
                        item.title = title;
                        ResultProgramAdapter.ContentItem contentItem = new ResultProgramAdapter.ContentItem(item, R.layout.hot_program_item, R.id.hot_program_name_tv);
                        contentItem.setClickable(true);
                        mItemDataList.add(contentItem);
                    }
                    addTimeLable();
                    mProgramLV.setAdapter(new ResultProgramAdapter(ChannelDetailActivity.this, mItemDataList));
                    break;
                case MSG_UPDATE_ONPLAYING_PROGRAM:
                    break;
                default:
                    break;
            }
            
        }
    };
    
    private void addTimeLable()
    {
        if (mProgramList.size() == 0)
            return;
        
        if (mProgramList.size() == 1)
        {
            mItemDataList.add(0, new ResultProgramAdapter.LabelItem(getResources().getString(R.string.full_day), R.layout.hot_channel_item, R.id.hot_channel_name_tv));
            return;
        }
        
        final String morning = "00:00";
        final String midday = "12:00";
        final String evening = "18:00";
        int hasMorning = 0;
        int hasMidday = 0;
        int hasEvening = 0;
        
        for (int i=0; i<mProgramList.size()-1; ++i)
        {
            String time1 = mProgramList.get(i).get("time");
            String time2 = mProgramList.get(i+1).get("time");
            
            if (i == 0 && compareTime(time1, midday) < 0)
            {
                mItemDataList.add(i, new ResultProgramAdapter.LabelItem(getResources().getString(R.string.morning), R.layout.hot_channel_item, R.id.hot_channel_name_tv));
                hasMorning = 1;
            }
            if (compareTime(time1, midday) < 0 && compareTime(time2, midday) >= 0 && compareTime(time2, evening) < 0)
            {
                mItemDataList.add(i + 1 + hasMorning, new ResultProgramAdapter.LabelItem(getResources().getString(R.string.midday), R.layout.hot_channel_item, R.id.hot_channel_name_tv));
                hasMidday = 1;
            }
            else if (compareTime(time1, evening) < 0 && compareTime(time2, evening) >= 0)
            {
                mItemDataList.add(i + 1 + hasMorning + hasMidday, new ResultProgramAdapter.LabelItem(getResources().getString(R.string.evening), R.layout.hot_channel_item, R.id.hot_channel_name_tv));
                hasEvening = 1;
            }
        }
    }
    
    private int compareTime(String time1, String time2)
    {
        assert(time1 != null && time2 != null);
        
        try 
        {
            SimpleDateFormat df = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
            Date date1 = df.parse(time1);
            Date date2 = df.parse(time2);
            
            if (date1.getTime() > date2.getTime())
                return 1;
            else if (date1.getTime() < date2.getTime())
                return -1;
            else
                return 0;
        } 
        catch (ParseException e) 
        {
            e.printStackTrace();
        }
        
        return 0;
    }
}
