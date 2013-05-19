package com.tools.tvguide.activities;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.tools.tvguide.utils.NetDataGetter;
import com.tools.tvguide.utils.NetworkManager;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SearchActivity extends Activity 
{
    private AutoCompleteTextView mSearchTextView;
    private ListView mListView;
    private BaseAdapter mListViewAdapter;
    private ArrayList<ListItems> mItemList;
    private LayoutInflater mInflater;
    private Handler mUpdateHandler;
    
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
            return mItemList.get(position).getView(SearchActivity.this, convertView, mInflater);
        }
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        
        mSearchTextView = (AutoCompleteTextView)findViewById(R.id.search_auto_text_view);
        mListView = (ListView)findViewById(R.id.search_list_view);
        mItemList = new ArrayList<ListItems>();
        mListViewAdapter = new PartAdapter();
        mListView.setAdapter(mListViewAdapter);
        mInflater = LayoutInflater.from(this);
        createUpdateThreadAndHandler();
        
        // For test
//        LabelItem label = new LabelItem("CCTV-1");
//        mItemList.add(label);
//        Item item = new Item();
//        item.id = "cctv1";
//        item.name = "CCTV-1";
//        item.time = "00:26";
//        item.title = "正大综艺";
//        ContentItem contentItem = new ContentItem(item);
//        mItemList.add(contentItem);
//        mListViewAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_search, menu);
        return true;
    }

    private void createUpdateThreadAndHandler()
    {
        mUpdateHandler = new Handler(NetworkManager.getInstance().getNetworkThreadLooper());
    }
    
    public void search(View view)
    {
        if (mSearchTextView.getText().toString().trim().equals(""))
        {
            Toast.makeText(this, "请输入要搜索的节目关键字!", Toast.LENGTH_SHORT).show();
            return;
        }
        updateResult();
    }
    
    private void updateResult()
    {
        mUpdateHandler.post(new Runnable()
        {
            public void run()
            {
                String url = "http://192.168.1.103/projects/TV/json/search.php?" + "keyword=" + mSearchTextView.getText().toString();
                NetDataGetter getter;
                try 
                {
                    getter = new NetDataGetter(url);
                    JSONObject jsonRoot = getter.getJSONsObject();
                    mItemList.clear();
                    if (jsonRoot != null)
                    {
                        JSONArray resultArray = jsonRoot.getJSONArray("result");
                        if (resultArray != null)
                        {
                            
                            for (int i=0; i<resultArray.length(); ++i)
                            {
                                String id = resultArray.getJSONObject(i).getString("id");
                                String name = resultArray.getJSONObject(i).getString("name");
                                JSONArray programsArray = resultArray.getJSONObject(i).getJSONArray("programs");
                                
                                mItemList.add(new LabelItem(name));
                                if (programsArray != null)
                                {
                                    for (int j=0; j<programsArray.length(); ++j)
                                    {
                                        String time = programsArray.getJSONObject(j).getString("time");
                                        String title = programsArray.getJSONObject(j).getString("title");                               
                                        Item item = new Item();
                                        item.id = id;
                                        item.name = name;
                                        item.time = time;
                                        item.title = title;
                                        mItemList.add(new ContentItem(item));
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
    
    private void hideInputKeyboard()
    {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mSearchTextView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }
    
    private Handler uiHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            hideInputKeyboard();
            mListViewAdapter.notifyDataSetChanged();
        }
    };
}

interface ListItems
{
    public int getLayout();
    public boolean isClickable();
    public View getView(Context context, View convertView, LayoutInflater inflater);
}

class LabelItem implements ListItems 
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
    String id;
    String name;
    String time;
    String title;
}

class ContentItem implements ListItems 
{
    private String SEPERATOR = ": ";
    private Item mItem;
    public ContentItem(Item item)
    {
        mItem = item;
    }
    
    @Override
    public int getLayout() 
    {
        return android.R.layout.simple_list_item_1;
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
        TextView title = (TextView) convertView;
        title.setText(mItem.time + SEPERATOR + mItem.title);
        return convertView;
    }
}
