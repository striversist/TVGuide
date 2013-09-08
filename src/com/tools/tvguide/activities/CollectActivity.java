package com.tools.tvguide.activities;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.tools.tvguide.R;
import com.tools.tvguide.managers.AppEngine;
import com.tools.tvguide.managers.UrlManager;
import com.tools.tvguide.utils.NetDataGetter;
import com.tools.tvguide.utils.NetworkManager;
import com.tools.tvguide.utils.Utility;
import com.tools.tvguide.utils.XmlParser;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Pair;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleAdapter.ViewBinder;

public class CollectActivity extends Activity 
{
    private ListView mChannelListView;
    private SimpleAdapter mListViewAdapter;
    private ArrayList<HashMap<String, Object>> mItemList;
    private List<Channel> mChannelList;
    private HashMap<String, HashMap<String, Object>> mXmlChannelInfo;
    private final String XML_ELEMENT_LOGO = "logo";
    private Handler mUpdateHandler;
    private List<Pair<String, String>> mOnPlayingProgramList;           // List of "id"-"title" pair
    private final int MSG_REFRESH_ON_PLAYING_PROGRAM_LIST   = 1;
    
    private class Channel
    {
        String id;
        String name;
        int position;
    }
    
    private class MySimpleAdapter extends SimpleAdapter
    {
        public MySimpleAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) 
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
                        int position = Integer.parseInt(v.getTag().toString());
                        
                        for (int i=0; i<mChannelList.size(); ++i)
                        {
                            if (mChannelList.get(i).position == position)
                            {
                                AppEngine.getInstance().getCollectManager().removeCollectChannel(mChannelList.get(i).id);
                            }
                        }
                        mChannelList.remove(position);
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
        setContentView(R.layout.activity_collect);
        
        mChannelListView = (ListView)findViewById(R.id.collect_channel_list_view);
        mXmlChannelInfo = XmlParser.parseChannelInfo(this);
        mChannelList = new ArrayList<CollectActivity.Channel>();
        mOnPlayingProgramList = new ArrayList<Pair<String,String>>();
        
        mChannelListView.setOnItemClickListener(new OnItemClickListener() 
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
            {
                String channelId = mChannelList.get(position).id;
                String channelName = mChannelList.get(position).name;
                Intent intent = new Intent(CollectActivity.this, ChannelDetailActivity.class);
                intent.putExtra("id", channelId);
                intent.putExtra("name", channelName);
                startActivity(intent);
            }
        });
        
        createAndSetListViewAdapter();
        createUpdateThreadAndHandler();
    }
    
    @Override
    protected void onResume() 
    {
        super.onResume();
        updateChannelListView();
        updateOnPlayingProgramList();
    };

    private void createAndSetListViewAdapter()
    {
        mItemList = new ArrayList<HashMap<String, Object>>();
        mListViewAdapter = new MySimpleAdapter(CollectActivity.this, mItemList, R.layout.collect_list_item,
                new String[]{"image", "name", "program", "button"}, 
                new int[]{R.id.collect_item_logo, R.id.collect_item_channel, R.id.collect_item_program, R.id.collect_item_del_btn});
        mListViewAdapter.setViewBinder(new MyViewBinder());
        mChannelListView.setAdapter(mListViewAdapter);
    }
    
    private void initChannelList()
    {
        mChannelList.clear();
        Iterator<Entry<String, HashMap<String, Object>>> iter = AppEngine.getInstance().getCollectManager().getCollectChannels().entrySet().iterator();
        while (iter.hasNext())
        {
            Map.Entry<String, HashMap<String, Object>> entry = (Map.Entry<String, HashMap<String,Object>>)iter.next();
            Channel channel = new Channel();
            channel.id = entry.getKey();
            channel.name = (String) entry.getValue().get("name");
            mChannelList.add(channel);
        }
    }
    
    private void updateChannelListView()
    {
        initChannelList();
        mItemList.clear();
        for (int i=0; i<mChannelList.size(); ++i)
        { 
            String id = mChannelList.get(i).id;
            String name = mChannelList.get(i).name;
            HashMap<String, Object> item = new HashMap<String, Object>();
            if (mXmlChannelInfo.get(id) != null)
            {
                item.put("image", Utility.getImage(CollectActivity.this, (String) mXmlChannelInfo.get(id).get(XML_ELEMENT_LOGO)));                        
            }
            item.put("name", name);
            mItemList.add(item);
            mChannelList.get(i).position = i;
        }
        mListViewAdapter.notifyDataSetChanged();
    }
    
    private void createUpdateThreadAndHandler()
    {
        mUpdateHandler = new Handler(NetworkManager.getInstance().getNetworkThreadLooper());
    }
    
    private void updateOnPlayingProgramList()
    {
        mUpdateHandler.post(new Runnable()
        {
            public void run()
            {
                assert(mChannelList != null);
                String url = AppEngine.getInstance().getUrlManager().getUrl(UrlManager.URL_ON_PLAYING_PROGRAMS);
                try 
                {
                    NetDataGetter getter;
                    getter = new NetDataGetter(url);
                    getter.setHeader("GUID", AppEngine.getInstance().getUpdateManager().getGUID());
                    List<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
                    //String test = "{\"channels\":[\"cctv1\", \"cctv3\"]}";
                    String idArray = "[";
                    for (int i=0; i<mChannelList.size(); ++i)
                    {
                        idArray += "\"" + mChannelList.get(i).id + "\"";
                        if (i < (mChannelList.size() - 1))
                        {
                            idArray += ",";
                        }
                    }
                    idArray += "]";
                    
                    pairs.add(new BasicNameValuePair("channels", "{\"channels\":" + idArray + "}"));
                    JSONObject jsonRoot = getter.getJSONsObject(pairs);
                    mOnPlayingProgramList.clear();
                    if (jsonRoot != null)
                    {
                        JSONArray resultArray = jsonRoot.getJSONArray("result");
                        if (resultArray != null)
                        {
                            for (int i=0; i<resultArray.length(); ++i)
                            {
                                Pair<String, String> pair = new Pair<String, String>(resultArray.getJSONObject(i).getString("id"), 
                                        resultArray.getJSONObject(i).getString("title"));
                                mOnPlayingProgramList.add(pair);
                            }
                        }
                    }
                    uiHandler.sendEmptyMessage(MSG_REFRESH_ON_PLAYING_PROGRAM_LIST);
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
    
    private Handler uiHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            switch (msg.what) 
            {
                case MSG_REFRESH_ON_PLAYING_PROGRAM_LIST:
                    if (mOnPlayingProgramList != null)
                    {
                        for (int i=0; i<mOnPlayingProgramList.size(); ++i)
                        {
                            mItemList.get(i).put("program", "正在播放： " +  mOnPlayingProgramList.get(i).second);
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
