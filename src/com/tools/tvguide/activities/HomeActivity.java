package com.tools.tvguide.activities;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.tools.tvguide.utils.NetDataGetter;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Pair;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class HomeActivity extends Activity 
{
    private ListView mCategoryListView;
    private HandlerThread mSearchThread;
    private Handler mSearchHandler;
    private List<Pair<String, String>> mCategoryList;
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mCategoryListView = (ListView)findViewById(R.id.category_list);
        mCategoryList = new ArrayList<Pair<String,String>>();
        
        createSearchThreadAndHandler();
        searchAsync();
    }
    
    private void createSearchThreadAndHandler()
    {
        mSearchThread = new HandlerThread("SearchThread");
        mSearchThread.start();
        mSearchHandler = new Handler(mSearchThread.getLooper());
    }
    
    private void searchAsync()
    {
        mSearchHandler.post(new Runnable()
        {
            public void run()
            {
                String url = "http://192.168.1.103/projects/TV/json/categories.php";
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
                                Pair<String, String> pair = new Pair<String, String>(categoryArray.getJSONObject(i).getString("id"), 
                                        categoryArray.getJSONObject(i).getString("name"));
                                mCategoryList.add(pair);
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
                    categories[i] = mCategoryList.get(i).second;
                }
                mCategoryListView.setAdapter(new ArrayAdapter<String>(HomeActivity.this, android.R.layout.simple_list_item_1, categories));
            }
        }
    };
}
