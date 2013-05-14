package com.tools.tvguide.activities;

import java.util.ArrayList;
import java.util.HashMap;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.Bitmap;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;

public class CollectActivity extends Activity 
{
    private ListView mChannelListView;
    private SimpleAdapter mListViewAdapter;
    private ArrayList<HashMap<String, Object>> mItemList;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect);
        
        mChannelListView = (ListView)findViewById(R.id.collect_channel_list_view);
        createAndSetListViewAdapter();
    }

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
        mListViewAdapter = new SimpleAdapter(CollectActivity.this, mItemList, R.layout.collect_list_item,
                new String[]{"image", "name", "program"}, 
                new int[]{R.id.collect_item_logo, R.id.collect_item_channel, R.id.collect_item_program});
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

    }
    
    
    public void remove(View view)
    {
    }
}
