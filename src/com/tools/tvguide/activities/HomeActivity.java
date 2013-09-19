package com.tools.tvguide.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.tools.tvguide.R;
import com.tools.tvguide.components.MyProgressDialog;
import com.tools.tvguide.managers.AppEngine;
import com.tools.tvguide.managers.ContentManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class HomeActivity extends Activity 
{
    private static final String TAG = "HomeActivity";
    private ListView mCategoryListView;
    private SimpleAdapter mListViewAdapter;
    private List<HashMap<String, Object>> mItemList;
    private List<HashMap<String, String>> mCategoryList;
    private MyProgressDialog mProgressDialog;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        Log.e(TAG, "onCreate this = " + this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mCategoryListView = (ListView)findViewById(R.id.category_list);
        mItemList = new ArrayList<HashMap<String, Object>>();
        mListViewAdapter = new SimpleAdapter(HomeActivity.this, mItemList, R.layout.home_list_item,
                new String[]{"name"}, new int[]{R.id.home_item_text});
        mCategoryList = new ArrayList<HashMap<String,String>>();
        mProgressDialog = new MyProgressDialog(this);
        
        mCategoryListView.setAdapter(mListViewAdapter);
        mCategoryListView.setOnItemClickListener(new OnItemClickListener() 
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
            {
                if (mCategoryList != null)
                {
                    String categoryId = mCategoryList.get(position).get("id");
                    String categoryName = mCategoryList.get(position).get("name");
                    Intent intent;
                    if (mCategoryList.get(position).get("has_sub_category").equals("1"))
                    {
                        intent = new Intent(HomeActivity.this, CategorylistActivity.class);
                    }
                    else
                    {
                        intent = new Intent(HomeActivity.this, ChannellistActivity.class);
                    }
                    intent.putExtra("categoryId", categoryId);
                    intent.putExtra("categoryName", categoryName);
                    startActivity(intent);
                }
            }
        });
        
        update();
    }

    private void update()
    {
        mCategoryList.clear();
        boolean isSyncLoad = false;
        isSyncLoad = AppEngine.getInstance().getContentManager().loadCategoriesByType("root", mCategoryList, new ContentManager.LoadListener() 
        {    
            @Override
            public void onLoadFinish(int status) 
            {
                uiHandler.sendEmptyMessage(0);
            }
        });
        if (isSyncLoad == true)
            uiHandler.sendEmptyMessage(0);
        else 
            mProgressDialog.show();
    }
    
    private Handler uiHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            if (mProgressDialog.isShowing())
                mProgressDialog.dismiss();
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
