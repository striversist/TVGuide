package com.tools.tvguide.activities;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SearchActivity extends Activity 
{
    private AutoCompleteTextView mSearchTextView;
    private ListView mListView;
    private ArrayList<ListItems> mItemList;
    private LayoutInflater mInflater;
    
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
        
        // For test
        LabelItem label = new LabelItem("CCTV-1");
        mItemList.add(label);
        Item item = new Item();
        item.id = "cctv1";
        item.name = "CCTV-1";
        item.time = "00:26";
        item.title = "е§Дѓзлве";
        ContentItem contentItem = new ContentItem(item);
        mItemList.add(contentItem);
        
        mSearchTextView = (AutoCompleteTextView)findViewById(R.id.search_auto_text_view);
        mListView = (ListView)findViewById(R.id.search_list_view);
        mItemList = new ArrayList<ListItems>();
        mListView.setAdapter(new PartAdapter());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_search, menu);
        return true;
    }
    
    public void search(View view)
    {
    }

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
        return android.R.layout.simple_list_item_1;
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
        TextView title = (TextView) convertView;
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
        return android.R.layout.simple_list_item_2;
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
