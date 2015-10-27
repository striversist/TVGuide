package com.tools.tvguide.activities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.tools.tvguide.R;
import com.tools.tvguide.components.MyProgressDialog;
import com.tools.tvguide.data.Category;
import com.tools.tvguide.managers.AppEngine;
import com.tools.tvguide.managers.BootManager.OnSplashFinishedCallback;
import com.tools.tvguide.managers.OnPlayingHtmlManager.CategoryEntriesCallback;

public class HomeActivity extends Activity implements Callback 
{
    private static final String TAG = "HomeActivity";
    private ListView mCategoryListView;
    private SimpleAdapter mListViewAdapter;
    private List<HashMap<String, Object>> mItemList;
    private List<Category> mCategoryList;
    private MyProgressDialog mProgressDialog;
    private boolean mHasUpdated = false;
    private Handler mUiHandler;
    private enum SelfMessage { SHOW_CATEGORY };
    
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
        mCategoryList = new ArrayList<Category>();
        mProgressDialog = new MyProgressDialog(this);
        mUiHandler = new Handler(this);
        
        mCategoryListView.setAdapter(mListViewAdapter);
        mCategoryListView.setOnItemClickListener(new OnItemClickListener() 
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
            {
                if (mCategoryList != null)
                {
                    Intent intent = null;
                    if (mCategoryList.get(position).next == Category.Next.CategoryList)
                    {
                        intent = new Intent(HomeActivity.this, CategorylistActivity.class);
                    }
                    else
                    {
                        intent = new Intent(HomeActivity.this, ChannellistActivity.class);
                    }
                    
                    if (intent != null)
                    {
                        intent.putExtra("category", (Serializable) mCategoryList.get(position));
                        startActivity(intent);
                    }
                }
            }
        });
        
        update();
    }
    
    @Override
    protected void onResume()
    {
        super.onResume();
        // 发现之前获取失败，则重新获取
        if (mHasUpdated && (mCategoryList == null || mCategoryList.size() == 0))
            update();
    }
    
    @Override
    protected void onDestroy() {
        if (mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        super.onDestroy();
    }

    private void update()
    {
        AppEngine.getInstance().getOnPlayingHtmlManager().getCategoryEntries(0, new CategoryEntriesCallback() 
        {
            @Override
            public void onCategoryEntriesLoaded(int requestId, List<Category> categories) 
            {
                if (categories != null)
                {
                    mCategoryList.clear();
                    mCategoryList.addAll(categories);
                    mUiHandler.obtainMessage(SelfMessage.SHOW_CATEGORY.ordinal()).sendToTarget();
                }
            }
        });
        
        if (AppEngine.getInstance().getBootManager().isSplashShowing())
        {
            AppEngine.getInstance().getBootManager().addOnSplashFinishedCallback(new OnSplashFinishedCallback() 
            {
                @Override
                public void OnSplashFinished() 
                {
                    if (mCategoryList.isEmpty())
                        mProgressDialog.show();
                }
            });
        }
        else if (mCategoryList.isEmpty())
        {
            mProgressDialog.show();
        }
    }

    @Override
    public boolean handleMessage(Message msg) 
    {
        SelfMessage selfMsg = SelfMessage.values()[msg.what];
        switch (selfMsg)
        {
            case SHOW_CATEGORY:
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
                for (int i=0; i<mCategoryList.size(); ++i)
                {
                    HashMap<String, Object> item = new HashMap<String, Object>();
                    item.put("name", mCategoryList.get(i).name);
                    mItemList.add(item);
                }
                mListViewAdapter.notifyDataSetChanged();
                mHasUpdated = true;
                break;
        }
        
        return true;
    }
}
