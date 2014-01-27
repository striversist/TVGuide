package com.tools.tvguide.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.tools.tvguide.R;
import com.tools.tvguide.managers.AdManager.AdSize;
import com.tools.tvguide.managers.AppEngine;
import com.tools.tvguide.utils.Utility;
import com.tools.tvguide.utils.XmlParser;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;

public class AlarmSettingActivity extends Activity 
{
    private ListView mListView;
    private MySimpleAdapter mListViewAdapter;
    private ArrayList<HashMap<String, Object>> mItemList;
    private HashMap<String, HashMap<String, Object>> mXmlChannelInfo;
    private final String XML_ELEMENT_LOGO = "logo";
    
    private class MySimpleAdapter extends SimpleAdapter
    {
        public MySimpleAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) 
        {
            super(context, data, resource, from, to);
        }
        
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) 
        {
            View view = super.getView(position, convertView, parent);
            Button rmBtn = (Button)view.findViewById(R.id.collect_item_del_btn);
            if (rmBtn != null)
            {
                rmBtn.setText("删除");
                rmBtn.setTag(position);
                rmBtn.setOnClickListener(new OnClickListener() 
                {
                    @Override
                    public void onClick(View v) 
                    {
                        String channelId = (String) mItemList.get(position).get("id");
                        String channelName = (String) mItemList.get(position).get("name");
                        String program = (String) mItemList.get(position).get("program");
                        String day = (String) mItemList.get(position).get("day");
                        
                        AppEngine.getInstance().getAlarmHelper().removeAlarm(channelId, channelName, program, Integer.valueOf(day));
                        mItemList.remove(position);
                        mListViewAdapter.notifyDataSetChanged();
                    }
                });
            }
            return view;
        }
    }
    
    private class MyViewBinder implements ViewBinder
    {
        public boolean setViewValue(View view, Object data,
                String textRepresentation)
        {
            if((view instanceof ImageView) && (data instanceof Bitmap))
            {
                ImageView iv = (ImageView)view;
                Bitmap bm = (Bitmap)data;
                iv.setImageBitmap(bm);
                return true;
            }
            else if (view instanceof Button)
            {
                Button btn = (Button)view;
                btn.setText(getResources().getString(R.string.delete));
                return true;
            }
            
            return false;
        }
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_setting);
        
        mListView = (ListView) findViewById(R.id.alarm_setting_list_view);
        mXmlChannelInfo = XmlParser.parseChannelInfo(this);
        createAndSetListViewAdapter();
        initAlarmList();
        
        AppEngine.getInstance().getAdManager().addAdView(this, R.id.adLayout, AdSize.NORMAL_SIZE);
    }
    
    @Override
    protected void onResume() 
    {
        super.onResume();
        initAlarmList();
    };
    
    public void back(View view)
    {
        if (view instanceof Button)
        {
            // The same effect with press back key
            finish();
        }
    }

    private void createAndSetListViewAdapter()
    {
        mItemList = new ArrayList<HashMap<String, Object>>();
        mListViewAdapter = new MySimpleAdapter(AlarmSettingActivity.this, mItemList, R.layout.collect_list_item,
                new String[]{"image", "name", "program", "button"}, 
                new int[]{R.id.collect_item_logo, R.id.collect_item_channel, R.id.collect_item_program, R.id.collect_item_del_btn});
        mListViewAdapter.setViewBinder(new MyViewBinder());
        mListView.setAdapter(mListViewAdapter);
    }
    
    private void initAlarmList()
    {
        mItemList.clear();
        Iterator<Entry<String, HashMap<String, String>>> iter = AppEngine.getInstance().getAlarmHelper().getAllRecords().entrySet().iterator();
        while (iter.hasNext())
        {
            Map.Entry<String, HashMap<String, String>> entry = (Map.Entry<String, HashMap<String, String>>) iter.next();
            String channelId = entry.getValue().get("channel_id");
            String channelName = entry.getValue().get("channel_name");
            String program = entry.getValue().get("program");
            String day = entry.getValue().get("day");
            
            HashMap<String, Object> item = new HashMap<String, Object>();
            if (mXmlChannelInfo.get(channelId) != null)
            {
                item.put("image", Utility.getImage(AlarmSettingActivity.this, (String) mXmlChannelInfo.get(channelId).get(XML_ELEMENT_LOGO)));                        
            }
            item.put("id", channelId);
            item.put("name", channelName);
            item.put("program", program);
            item.put("day", day);
            mItemList.add(item);
        }
        mListViewAdapter.notifyDataSetChanged();
    }
}
