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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class HomeActivity extends Activity 
{
    private static final String TAG = "HomeActivity";
    private ListView mCategoryListView;
    private Handler mUpdateHandler;
    private List<HashMap<String, String>> mCategoryList;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        Log.e(TAG, "onCreate this = " + this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mCategoryListView = (ListView)findViewById(R.id.category_list);
        mCategoryList = new ArrayList<HashMap<String,String>>();
        
        mCategoryListView.setOnItemClickListener(new OnItemClickListener() 
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
            {
                if (mCategoryList != null)
                {
                    String categoryId = mCategoryList.get(position).get("id");
                    Intent intent;
                    if (mCategoryList.get(position).get("has_sub_category").equals("1"))
                    {
                        intent = new Intent(HomeActivity.this, CategorylistActivity.class);
                    }
                    else
                    {
                        intent = new Intent(HomeActivity.this, ChannellistActivity.class);
                    }
                    intent.putExtra("category", categoryId);
                    startActivity(intent);
                }
            }
        });
        
        createUpdateThreadAndHandler();
        update();
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
                String url = UrlManager.URL_CATEGORIES;
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
                mCategoryListView.setAdapter(new ArrayAdapter<String>(HomeActivity.this, android.R.layout.simple_list_item_1, categories));
            }
        }
    };
}
