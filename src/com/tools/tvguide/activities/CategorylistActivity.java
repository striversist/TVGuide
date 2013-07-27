package com.tools.tvguide.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.tools.tvguide.managers.AppEngine;
import com.tools.tvguide.managers.ContentManager;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class CategorylistActivity extends Activity 
{
    private static final String TAG = "CategorylistActivity";
    private String mCategoryId;
    private String mCategoryName;
    private ListView mCategoryListView;
    private SimpleAdapter mListViewAdapter;
    private ArrayList<HashMap<String, Object>> mItemList;
    private TextView mTitleTextView;
    private List<HashMap<String, String>> mCategoryList;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categorylist);
        mCategoryListView = (ListView)findViewById(R.id.categorylist_listview);
        mTitleTextView = (TextView)findViewById(R.id.categorylist_text_title);
        mCategoryList = new ArrayList<HashMap<String,String>>();
        mItemList = new ArrayList<HashMap<String, Object>>();
        mListViewAdapter = new SimpleAdapter(CategorylistActivity.this, mItemList, R.layout.home_list_item,
                new String[]{"name"}, new int[]{R.id.home_item_text});
        mCategoryListView.setAdapter(mListViewAdapter);
        
        mCategoryId = getIntent().getStringExtra("categoryId");
        mCategoryName = getIntent().getStringExtra("categoryName");
        mTitleTextView.setText(mCategoryName);
        if (mCategoryId != null)
        {
            update();
        }
        
        mCategoryListView.setOnItemClickListener(new OnItemClickListener() 
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
            {
                if (mCategoryList != null)
                {
                    String categoryId = mCategoryList.get(position).get("id");
                    String categoryName = mCategoryList.get(position).get("name");
                    Intent intent = new Intent(CategorylistActivity.this, ChannellistActivity.class);
                    intent.putExtra("categoryId", categoryId);
                    intent.putExtra("categoryName", categoryName);
                    startActivity(intent);
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
        
    private void update()
    {
        mCategoryList.clear();
        boolean isSyncLoad = false;
        isSyncLoad = AppEngine.getInstance().getContentManager().loadCategoriesByType(mCategoryId, mCategoryList, new ContentManager.LoadListener() 
        {    
            @Override
            public void onLoadFinish(int status) 
            {
                uiHandler.sendEmptyMessage(0);
            }
        });
        if (isSyncLoad == true)
        {
            uiHandler.sendEmptyMessage(0);
        }
    }
    
    private Handler uiHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            if (mCategoryList != null)
            {
                mItemList.clear();
                for (int i=0; i<mCategoryList.size(); ++i)
                {
                    HashMap<String, Object> item = new HashMap<String, Object>();
                    item.put("name", mCategoryList.get(i).get("name"));
                    mItemList.add(item);
                }
                mListViewAdapter.notifyDataSetChanged();
            }
        }
    };
}
