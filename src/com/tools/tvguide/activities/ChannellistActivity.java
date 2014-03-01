package com.tools.tvguide.activities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.tools.tvguide.R;
import com.tools.tvguide.adapters.ChannellistAdapter;
import com.tools.tvguide.components.MyProgressDialog;
import com.tools.tvguide.data.Channel;
import com.tools.tvguide.managers.AppEngine;
import com.tools.tvguide.managers.ContentManager;
import com.tools.tvguide.managers.AdManager.AdSize;
import com.tools.tvguide.utils.Utility;
import com.tools.tvguide.utils.XmlParser;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ChannellistActivity extends Activity 
{
    private String mCategoryId;
    private String mCategoryName;
    private ListView mChannelListView;
    private ChannellistAdapter mListAdapter;
    private TextView mTitltTextView;
    private List<Channel> mChannelList;
    private List<HashMap<String, String>> mOnPlayingProgramList;            // Key: id, title
    private HashMap<String, HashMap<String, Object>> mXmlChannelInfo;
    private ArrayList<HashMap<String, Object>> mItemList;
    private MyProgressDialog mProgressDialog;
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
        mChannelList = new ArrayList<Channel>();
        mOnPlayingProgramList = new ArrayList<HashMap<String,String>>();
        mXmlChannelInfo = XmlParser.parseChannelInfo(this);
        mItemList = new ArrayList<HashMap<String, Object>>();
        mListAdapter = new ChannellistAdapter(this, mItemList);
        mChannelListView.setAdapter(mListAdapter);
        mProgressDialog = new MyProgressDialog(this);
        
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
                String channelId = mChannelList.get(position).id;
                String channelName = mChannelList.get(position).name;
                Intent intent = new Intent(ChannellistActivity.this, ChannelDetailActivity.class);
                intent.putExtra("id", channelId);
                intent.putExtra("name", channelName);
                
                ArrayList<Channel> channelList = new ArrayList<Channel>();
                for (int i=0; i<mChannelList.size(); ++i)
                {
                    Channel channel = new Channel();
                    channel.id = mChannelList.get(i).id;
                    channel.name = mChannelList.get(i).name;
                    channelList.add(channel);
                }
                intent.putExtra("channel_list", (Serializable) channelList);
                startActivity(intent);
            }
        });
        
        new Handler().postDelayed(new Runnable() 
        {
            @Override
            public void run() 
            {
                AppEngine.getInstance().getAdManager().addAdView(ChannellistActivity.this, R.id.adLayout, AdSize.NORMAL_SIZE);
            }
        }, 500);
    }

    public void back(View view)
    {
        if (view instanceof Button)
        {
            // The same effect with press back key
            finish();
        }
    }
       
    private void updateChannelList()
    {
        mChannelList.clear();
        boolean isSyncLoad = AppEngine.getInstance().getContentManager().loadChannelsByCategory(mCategoryId, mChannelList, new ContentManager.LoadListener() 
        {    
            @Override
            public void onLoadFinish(int status) 
            {
                uiHandler.sendEmptyMessage(MSG_REFRESH_CHANNEL_LIST);
            }
        });
        if (isSyncLoad == true)
            uiHandler.sendEmptyMessage(MSG_REFRESH_CHANNEL_LIST);
        else
            mProgressDialog.show();
    }
    
    private void updateOnPlayingProgramList()
    {
        mOnPlayingProgramList.clear();
        List<String> idList = new ArrayList<String>();
        for (int i=0; i<mChannelList.size(); ++i)
        {
            idList.add(mChannelList.get(i).id);
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
                        mProgressDialog.dismiss();
                        mItemList.clear();
                        for(int i=0; i<mChannelList.size(); ++i)
                        {
                            HashMap<String, Object> item = new HashMap<String, Object>();
                            item.put("id", mChannelList.get(i).id);
                            if (mXmlChannelInfo.get(mChannelList.get(i).id) != null)
                            {
                                item.put("image", Utility.getImage(ChannellistActivity.this, (String) mXmlChannelInfo.get(mChannelList.get(i).id).get(XML_ELEMENT_LOGO)));                        
                            }
                            item.put("name", mChannelList.get(i).name);
                            mItemList.add(item);
                        }
                        mListAdapter.notifyDataSetChanged();
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
                                if (mItemList.get(i).get("id").equals(mOnPlayingProgramList.get(j).get("id")))
                                {
                                    mItemList.get(i).put("program", "正在播出：" + mOnPlayingProgramList.get(j).get("title"));
                                }
                            }
                        }
                        mListAdapter.notifyDataSetChanged();
                    }
                    break;
                default:
                    break;
            }
        }
    };
}
