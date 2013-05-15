package com.tools.tvguide.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.tools.tvguide.managers.AppEngine;
import com.tools.tvguide.utils.Utility;
import com.tools.tvguide.utils.XmlParser;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;

public class CollectActivity extends Activity 
{
    private ListView mChannelListView;
    private SimpleAdapter mListViewAdapter;
    private ArrayList<HashMap<String, Object>> mItemList;
    private HashMap<String, HashMap<String, Object>> mCollectChannels;
    private HashMap<String, HashMap<String, Object>> mXmlChannelInfo;
    private final String XML_ELEMENT_LOGO = "logo";
    
    private class MySimpleAdatper extends SimpleAdapter
    {
        public MySimpleAdatper(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) 
        {
            super(context, data, resource, from, to);
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) 
        {
            View view = super.getView(position, convertView, parent);
            Button rmBtn = (Button)view.findViewById(R.id.collect_item_del_btn);
            if (rmBtn != null)
            {
                rmBtn.setTag(position);
                rmBtn.setOnClickListener(new OnClickListener() 
                {
                    @Override
                    public void onClick(View v) 
                    {
                        
                    }
                });
            }
            return view;
        }
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect);
        
        mChannelListView = (ListView)findViewById(R.id.collect_channel_list_view);
        mCollectChannels = AppEngine.getInstance().getUserSettingManager().getCollectChannels();
        mXmlChannelInfo = XmlParser.parseChannelInfo(this);
        
        createAndSetListViewAdapter();
    }
    
    @Override
    protected void onResume() 
    {
        super.onResume();
        updateChannelList();
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_collect, menu);
        return true;
    }

    private void createAndSetListViewAdapter()
    {
        mItemList = new ArrayList<HashMap<String, Object>>();
        mListViewAdapter = new MySimpleAdatper(CollectActivity.this, mItemList, R.layout.collect_list_item,
                new String[]{"image", "name", "program", "button"}, 
                new int[]{R.id.collect_item_logo, R.id.collect_item_channel, R.id.collect_item_program, R.id.collect_item_del_btn});
        mListViewAdapter.setViewBinder(new MyViewBinder());
        mChannelListView.setAdapter(mListViewAdapter);
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
    
    private void updateChannelList()
    {
        Iterator<Entry<String, HashMap<String, Object>>> iter = mCollectChannels.entrySet().iterator();
        mItemList.clear();
        while (iter.hasNext())
        {
            Map.Entry<String, HashMap<String, Object>> entry = (Map.Entry<String, HashMap<String,Object>>)iter.next(); 
            String id = entry.getKey();
            String name = (String) entry.getValue().get("name");
            HashMap<String, Object> item = new HashMap<String, Object>();
            if (mXmlChannelInfo.get(id) != null)
            {
                item.put("image", Utility.getImage(CollectActivity.this, (String) mXmlChannelInfo.get(id).get(XML_ELEMENT_LOGO)));                        
            }
            item.put("name", name);
            mItemList.add(item);
        }
        mListViewAdapter.notifyDataSetChanged();
    }
    
}
