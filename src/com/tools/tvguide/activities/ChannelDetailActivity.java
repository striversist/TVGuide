package com.tools.tvguide.activities;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.tools.tvguide.activities.ChannellistActivity.MyViewBinder;
import com.tools.tvguide.utils.NetDataGetter;
import com.tools.tvguide.utils.NetworkManager;

import android.R.integer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.util.Pair;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class ChannelDetailActivity extends Activity 
{
    private List<Pair<Integer, Button>> mDaysBtnList = new ArrayList<Pair<Integer,Button>>();
    private int mCurrentIndex;
    private ListView mDetailListView;
    private SimpleAdapter mListViewAdapter;
    private ArrayList<HashMap<String, Object>> mItemList;
    private List<String> mProgramList;
    private Handler mUpdateHandler;
    private String mChannelId;
    private String mChannelName;
    private String mOnplayingProgram;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_detail);
        
        mChannelId = getIntent().getStringExtra("id");
        mChannelName = getIntent().getStringExtra("name");
        mOnplayingProgram = getIntent().getStringExtra("program");
        ((TextView)findViewById(R.id.title)).setText(mChannelName);
        mProgramList = new ArrayList<String>();
        mItemList = new ArrayList<HashMap<String, Object>>();
        
        initViews();
        createAndSetListViewAdapter();
        createUpdateThreadAndHandler();
        update();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_channel_detail, menu);
        return true;
    }
    
    public void initViews()
    {
        mDaysBtnList.add(new Pair<Integer, Button>(Calendar.MONDAY, (Button)findViewById(R.id.Mon)));
        mDaysBtnList.add(new Pair<Integer, Button>(Calendar.TUESDAY, (Button)findViewById(R.id.Tue)));
        mDaysBtnList.add(new Pair<Integer, Button>(Calendar.WEDNESDAY, (Button)findViewById(R.id.Wed)));
        mDaysBtnList.add(new Pair<Integer, Button>(Calendar.THURSDAY, (Button)findViewById(R.id.Thu)));
        mDaysBtnList.add(new Pair<Integer, Button>(Calendar.FRIDAY, (Button)findViewById(R.id.Fri)));
        mDaysBtnList.add(new Pair<Integer, Button>(Calendar.SATURDAY, (Button)findViewById(R.id.Sat)));
        mDaysBtnList.add(new Pair<Integer, Button>(Calendar.SUNDAY, (Button)findViewById(R.id.Sun)));
        
        for (int i=0; i<mDaysBtnList.size(); ++i)
        {
            if (mDaysBtnList.get(i).first.intValue() == Calendar.getInstance().get(Calendar.DAY_OF_WEEK))
            {
                mDaysBtnList.get(i).second.setBackgroundResource(R.drawable.text_bg2);
                mCurrentIndex = i;
            }
        }
        
        mDetailListView = (ListView)findViewById(R.id.detail_listview);
    }
    
    private void createAndSetListViewAdapter()
    {
        mListViewAdapter = new SimpleAdapter(ChannelDetailActivity.this, mItemList, R.layout.channeldetail_item,
                new String[]{"program"}, 
                new int[]{R.id.detail_item_program});
        mDetailListView.setAdapter(mListViewAdapter);
    }
    
    private void createUpdateThreadAndHandler()
    {
        mUpdateHandler = new Handler(NetworkManager.getInstance().getNetworkThreadLooper());
    }
    
    private void update()
    {
        mUpdateHandler.post(new Runnable()
        {
            public void run()
            {

                String url = "http://192.168.1.103/projects/TV/json/choose.php?" + "channel=" + mChannelId + "&day=" + getHostDay(mCurrentIndex);
                NetDataGetter getter;
                try 
                {
                    getter = new NetDataGetter(url);
                    JSONObject jsonRoot = getter.getJSONsObject();
                    mProgramList.clear();
                    if (jsonRoot != null)
                    {
                        JSONArray resultArray = jsonRoot.getJSONArray("result");
                        if (resultArray != null)
                        {

                        }
                    }
                    uiHandler.sendEmptyMessage(0);
                }
                catch (MalformedURLException e) 
                {
                    e.printStackTrace();
                }
                catch (JSONException e) 
                {
                    e.printStackTrace();
                }
            }
        });
    }

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
        if (view instanceof Button)
        {
            
        }
    }
    
    public void selectDay(View view)
    {
        for (int i=0; i<mDaysBtnList.size(); ++i)
        {
            if (mDaysBtnList.get(i).second.getId() == view.getId())
            {
                mDaysBtnList.get(i).second.setBackgroundResource(R.drawable.text_bg2);
                mCurrentIndex = i;
            }
            else
            {
                mDaysBtnList.get(i).second.setBackgroundResource(R.drawable.text_bg);
            }
        }
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
        }
    };
}
