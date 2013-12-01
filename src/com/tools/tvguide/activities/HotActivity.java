package com.tools.tvguide.activities;

import java.net.MalformedURLException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.tools.tvguide.R;
import com.tools.tvguide.adapters.ResultProgramAdapter;
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
import android.widget.ListView;
import android.widget.Toast;

public class HotActivity extends Activity 
{
    private ListView mListView;
    private ArrayList<ResultProgramAdapter.IListItem> mItemList;
    private ArrayList<ResultProgramAdapter.IListItem> mItemDataList;
    private Handler mUpdateHandler;
    private MyProgressDialog mProgressDialog;
    private boolean mHasUpdated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hot);
        
        mListView = (ListView)findViewById(R.id.hot_list_view);
        mItemList = new ArrayList<ResultProgramAdapter.IListItem>();
        mItemDataList = new ArrayList<ResultProgramAdapter.IListItem>();
        mProgressDialog = new MyProgressDialog(this);
        
        createUpdateThreadAndHandler();
        updateResult();
    }
    
    @Override
    protected void onResume()
    {
        super.onResume();
        // 发现之前获取失败，则重新获取
        if (mHasUpdated && mItemList.size() == 0)
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
                String url = AppEngine.getInstance().getUrlManager().tryToGetDnsedUrl(UrlManager.URL_HOT);
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
                                
                                mItemDataList.add(new ResultProgramAdapter.LabelItem(channelName));
                                if (programsArray != null)
                                {
                                    for (int j=0; j<programsArray.length(); ++j)
                                    {
                                        String title = programsArray.get(j).toString();
                                        ResultProgramAdapter.Item item = new ResultProgramAdapter.Item();
                                        item.title = title;
                                        mItemDataList.add(new ResultProgramAdapter.ContentItem(item));
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
            if (mItemDataList.size() == 0)
                Toast.makeText(HotActivity.this, getResources().getString(R.string.server_is_busy), Toast.LENGTH_LONG).show();
            for (int i=0; i<mItemDataList.size(); ++i)
            {
                mItemList.add(mItemDataList.get(i));
            }
            
            mListView.setAdapter(new ResultProgramAdapter(HotActivity.this, mItemList));
            mHasUpdated = true;
        }
    };
}
