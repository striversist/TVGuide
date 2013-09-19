package com.tools.tvguide.activities;

import java.net.MalformedURLException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.tools.tvguide.R;
import com.tools.tvguide.components.DefaultNetDataGetter;
import com.tools.tvguide.components.MyProgressDialog;
import com.tools.tvguide.managers.AppEngine;
import com.tools.tvguide.managers.UrlManager;
import com.tools.tvguide.utils.NetDataGetter;
import com.tools.tvguide.utils.NetworkManager;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class HotActivity extends Activity 
{
    private ListView mListView;
    private BaseAdapter mListViewAdapter;
    private ArrayList<IListItem> mItemList;
    private ArrayList<IListItem> mItemDataList;
    private LayoutInflater mInflater;
    private Handler mUpdateHandler;
    private MyProgressDialog mProgressDialog;
    
    class PartAdapter extends BaseAdapter 
    {
        @Override
        public int getCount() 
        {
            return mItemList.size();
        }

        @Override
        public Object getItem(int position) 
        {
            return mItemList.get(position);
        }

        @Override
        public long getItemId(int position) 
        {
            return position;
        }
        
        @Override
        public boolean isEnabled(int position) 
        {
            return mItemList.get(position).isClickable();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) 
        {
            return mItemList.get(position).getView(HotActivity.this, convertView, mInflater);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hot);
        
        mListView = (ListView)findViewById(R.id.hot_list_view);
        mItemList = new ArrayList<IListItem>();
        mItemDataList = new ArrayList<IListItem>();
        mListViewAdapter = new PartAdapter();
        mListView.setAdapter(mListViewAdapter);
        mInflater = LayoutInflater.from(this);
        mProgressDialog = new MyProgressDialog(this);
        
        createUpdateThreadAndHandler();
        updateResult();
    }
    
    @Override
    protected void onResume()
    {
        super.onResume();
        // 发现之前获取失败，则重新获取
        if (mItemList.size() == 0)
            updateResult();
    }

    private void createUpdateThreadAndHandler()
    {
        mUpdateHandler = new Handler(NetworkManager.getInstance().getNetworkThreadLooper());
    }
    
    private void updateResult()
    {
        mProgressDialog.show();
        mUpdateHandler.post(new Runnable()
        {
            public void run()
            {
                String url = AppEngine.getInstance().getUrlManager().getUrl(UrlManager.URL_HOT);
                NetDataGetter getter;
                try 
                {
                    getter = new DefaultNetDataGetter(url);
                    JSONObject jsonRoot = getter.getJSONsObject();
                    mItemDataList.clear();
                    if (jsonRoot != null)
                    {
                        JSONArray resultArray = jsonRoot.getJSONArray("hot");
                        if (resultArray != null)
                        {
                            
                            for (int i=0; i<resultArray.length(); ++i)
                            {
                                String channelName = resultArray.getJSONObject(i).getString("name");
                                JSONArray programsArray = resultArray.getJSONObject(i).getJSONArray("programs");
                                
                                mItemDataList.add(new LabelItem(channelName));
                                if (programsArray != null)
                                {
                                    for (int j=0; j<programsArray.length(); ++j)
                                    {
                                        String title = programsArray.get(j).toString();
                                        Item item = new Item();
                                        item.title = title;
                                        mItemDataList.add(new ContentItem(item));
                                    }
                                }
                            }
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
    
    private Handler uiHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            mProgressDialog.dismiss();
            mItemList.clear();
            for (int i=0; i<mItemDataList.size(); ++i)
            {
                mItemList.add(mItemDataList.get(i));
            }
            mListViewAdapter.notifyDataSetChanged();
        }
    };
    
    interface IListItem
    {
        public int getLayout();
        public boolean isClickable();
        public View getView(Context context, View convertView, LayoutInflater inflater);
    }

    class LabelItem implements IListItem 
    {
        private String mLabel;
        public LabelItem(String label)
        {
            mLabel = label;
        }
        
        @Override
        public int getLayout() 
        {
            return R.layout.search_list_label_item;
        }

        @Override
        public boolean isClickable() 
        {
            return false;
        }

        @Override
        public View getView(Context context, View convertView, LayoutInflater inflater) 
        {
            convertView = inflater.inflate(getLayout(), null);
            TextView title = (TextView) convertView.findViewById(R.id.search_item_label_text_view);
            title.setText(mLabel);
            return convertView;
        }
    }

    class Item
    {
        String title;
    }

    class ContentItem implements IListItem 
    {
        private Item mItem;
        public ContentItem(Item item)
        {
            mItem = item;
        }
        
        @Override
        public int getLayout() 
        {
            return R.layout.search_list_content_item;
        }

        @Override
        public boolean isClickable() 
        {
            return true;
        }

        @Override
        public View getView(Context context, View convertView, LayoutInflater inflater) 
        {
            convertView = inflater.inflate(getLayout(), null);
            TextView tv = (TextView) convertView.findViewById(R.id.search_item_content_text_view);
            SpannableString ss = new SpannableString(mItem.title);
            tv.setText(ss);
            return convertView;
        }
    }
}
