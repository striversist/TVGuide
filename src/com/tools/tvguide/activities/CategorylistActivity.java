package com.tools.tvguide.activities;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.tools.tvguide.managers.UrlManager;
import com.tools.tvguide.utils.NetDataGetter;
import com.tools.tvguide.utils.NetworkManager;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class CategorylistActivity extends Activity 
{
    private static final String TAG = "CategorylistActivity";
    private String mCategoryId;
    private String mCategoryName;
    private ListView mCategoryListView;
    private TextView mTitleTextView;
    private Handler mUpdateHandler;
    private List<HashMap<String, String>> mCategoryList;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categorylist);
        mCategoryListView = (ListView)findViewById(R.id.categorylist_listview);
        mTitleTextView = (TextView)findViewById(R.id.categorylist_text_title);
        mCategoryList = new ArrayList<HashMap<String,String>>();
        createUpdateThreadAndHandler();
        
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_categorylist, menu);
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
    
    private void createUpdateThreadAndHandler()
    {
        mUpdateHandler = new Handler(NetworkManager.getInstance().getNetworkThreadLooper());
    }
    
    private void update()
    {
        mUpdateHandler.post(new Runnable()
        {
            public void run()
            {
                String url = UrlManager.URL_CATEGORIES + "?type=" + mCategoryId;
                NetDataGetter getter;
                try 
                {
                    getter = new NetDataGetter(url);
                    JSONObject jsonRoot = getter.getJSONsObject();
                    mCategoryList.clear();
                    if (jsonRoot != null)
                    {
                        JSONArray categoryArray = jsonRoot.getJSONArray("categories");
                        if (categoryArray != null)
                        {
                            for (int i=0; i<categoryArray.length(); ++i)
                            {
                                HashMap<String, String> category = new HashMap<String, String>();
                                category.put("id", categoryArray.getJSONObject(i).getString("id"));
                                category.put("name", categoryArray.getJSONObject(i).getString("name"));
                                category.put("has_sub_category", categoryArray.getJSONObject(i).getString("has_sub_category"));
                                mCategoryList.add(category);
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
            if (mCategoryList != null)
            {
                String categories[] = new String[mCategoryList.size()];
                for (int i=0; i<mCategoryList.size(); ++i)
                {
                    categories[i] = mCategoryList.get(i).get("name");
                }
                mCategoryListView.setAdapter(new ArrayAdapter<String>(CategorylistActivity.this, android.R.layout.simple_list_item_1, categories));
            }
        }
    };
}
