package com.tools.tvguide.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import com.tools.tvguide.managers.AppEngine;
import com.tools.tvguide.managers.ContentManager;
import com.tools.tvguide.utils.Utility;
import com.tools.tvguide.utils.XmlParser;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Pair;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleAdapter.ViewBinder;

public class ChannellistActivity extends Activity 
{
    private String mCategoryId;
    private String mCategoryName;
    private ListView mChannelListView;
    private TextView mTitltTextView;
    private List<Pair<String, String>> mChannelList;                    // List of "id"-"name" pair
    private List<Pair<String, String>> mOnPlayingProgramList;           // List of "id"-"title" pair
    private HashMap<String, HashMap<String, Object>> mXmlChannelInfo;
    private SimpleAdapter mListViewAdapter;
    private ArrayList<HashMap<String, Object>> mItemList;
    private final String XML_ELEMENT_LOGO = "logo";
    private final int MSG_REFRESH_CHANNEL_LIST              = 0;
    private final int MSG_REFRESH_ON_PLAYING_PROGRAM_LIST   = 1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channellist);
        mChannelListView = (ListView)findViewById(R.id.channel_list);
        mTitltTextView = (TextView)findViewById(R.id.channellist_text_title);
        mChannelList = new ArrayList<Pair<String, String>>();
        mOnPlayingProgramList = new ArrayList<Pair<String,String>>();
        mXmlChannelInfo = XmlParser.parseChannelInfo(this);
        mItemList = new ArrayList<HashMap<String, Object>>();
        createAndSetListViewAdapter();
        
        mCategoryId = getIntent().getStringExtra("categoryId");
        mCategoryName = getIntent().getStringExtra("categoryName");
        mTitltTextView.setText(mCategoryName);
        if (mCategoryId != null)
        {
            updateChannelList();
        }
        mChannelListView.setOnItemClickListener(new OnItemClickListener() 
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
            {
                String channelId = mChannelList.get(position).first;
                String channelName = mChannelList.get(position).second;
                Intent intent = new Intent(ChannellistActivity.this, ChannelDetailActivity.class);
                intent.putExtra("id", channelId);
                intent.putExtra("name", channelName);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_channellist, menu);
        return true;
    }

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
        mListViewAdapter = new SimpleAdapter(ChannellistActivity.this, mItemList, R.layout.channellist_item,
                new String[]{"image", "name", "program"}, 
                new int[]{R.id.itemImage, R.id.itemChannel, R.id.itemProgram});
        mListViewAdapter.setViewBinder(new MyViewBinder());
        mChannelListView.setAdapter(mListViewAdapter);
    }
    
    public class MyViewBinder implements ViewBinder
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
            return false;
        }
    }
       
    private void updateChannelList()
    {
        mChannelList.clear();
        AppEngine.getInstance().getContentManager().loadChannelsByCategory(mCategoryId, mChannelList, new ContentManager.LoadListener() 
        {    
            @Override
            public void onLoadFinish(int status) 
            {
                uiHandler.sendEmptyMessage(MSG_REFRESH_CHANNEL_LIST);
            }
        });
    }
    
    private void updateOnPlayingProgramList()
    {
        mOnPlayingProgramList.clear();
//        List<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
//        //String test = "{\"channels\":[\"cctv1\", \"cctv3\"]}";
//        String idArray = "[";
//        for (int i=0; i<mChannelList.size(); ++i)
//        {
//            idArray += "\"" + mChannelList.get(i).first + "\"";
//            if (i < (mChannelList.size() - 1))
//            {
//                idArray += ",";
//            }
//        }
//        idArray += "]";
//        pairs.add(new BasicNameValuePair("channels", "{\"channels\":" + idArray + "}"));
        List<String> idList = new ArrayList<String>();
        for (int i=0; i<mChannelList.size(); ++i)
        {
            idList.add(mChannelList.get(i).first);
        }
        AppEngine.getInstance().getContentManager().loadOnPlayingPrograms(idList, mOnPlayingProgramList, new ContentManager.LoadListener() 
        {    
            @Override
            public void onLoadFinish(int status) 
            {
                uiHandler.sendEmptyMessage(MSG_REFRESH_ON_PLAYING_PROGRAM_LIST);
            }
        });
    }
       
    private Handler uiHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            switch (msg.what) 
            {
                case MSG_REFRESH_CHANNEL_LIST:
                    if (mChannelList != null)
                    {
                        mItemList.clear();
                        for(int i=0; i<mChannelList.size(); ++i)
                        {
                            HashMap<String, Object> item = new HashMap<String, Object>();
                            item.put("id", mChannelList.get(i).first);
                            if (mXmlChannelInfo.get(mChannelList.get(i).first) != null)
                            {
                                item.put("image", Utility.getImage(ChannellistActivity.this, (String) mXmlChannelInfo.get(mChannelList.get(i).first).get(XML_ELEMENT_LOGO)));                        
                            }
                            item.put("name", mChannelList.get(i).second);
                            mItemList.add(item);
                        }
                        mListViewAdapter.notifyDataSetChanged();
                        updateOnPlayingProgramList();
                    }
                    break;
                case MSG_REFRESH_ON_PLAYING_PROGRAM_LIST:
                    if (mOnPlayingProgramList != null)
                    {
                        for (int i=0; i<mItemList.size(); ++i)
                        {
                            for (int j=0; j<mOnPlayingProgramList.size(); ++j)
                            {
                                if (mItemList.get(i).get("id").equals(mOnPlayingProgramList.get(j).first))
                                {
                                    mItemList.get(i).put("program", "ÕýÔÚ²¥·Å£º  " + mOnPlayingProgramList.get(j).second);
                                }
                            }
                        }
                        mListViewAdapter.notifyDataSetChanged();
                    }
                    break;
                default:
                    break;
            }
        }
    };
}
