package com.tools.tvguide.activities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.DragSortListener;
import com.tools.tvguide.R;
import com.tools.tvguide.adapters.CollectListAdapter;
import com.tools.tvguide.adapters.CollectListAdapter.RemoveItemCallback;
import com.tools.tvguide.data.Channel;
import com.tools.tvguide.managers.AppEngine;
import com.tools.tvguide.managers.CollectManager;

public class CollectActivity extends Activity implements DragSortListener
{
    private final int TIMER_SCHEDULE_PERIOD = 1 * 60 * 1000;        // 1 minute
    private Timer mTimer;
    private static boolean sHasShownTips = false;
    private DragSortListView mChannelListView;
    private DragSortController mController;
    private CollectListAdapter mListViewAdapter;
    private List<Channel> mChannelList;
    private LayoutInflater mInflater;
    private LinearLayout mContentLayout;
    private LinearLayout mNoCollectLayout;
    private LinearLayout.LayoutParams mCenterLayoutParams;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect);
        
        mChannelListView = (DragSortListView)findViewById(R.id.collect_channel_list_view);
        mChannelList = new ArrayList<Channel>();
        mInflater = LayoutInflater.from(this);
        mContentLayout = (LinearLayout)findViewById(R.id.collect_content_layout);
        mNoCollectLayout = (LinearLayout)mInflater.inflate(R.layout.center_text_tips_layout, null);
        mCenterLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        ((TextView) mNoCollectLayout.findViewById(R.id.center_tips_text_view)).setText(getResources().getString(R.string.promote_collect_tips));
        
        mController = buildController(mChannelListView);
        mChannelListView.setFloatViewManager(mController);
        mChannelListView.setOnTouchListener(mController);
        mChannelListView.setDragSortListener(this);
        mChannelListView.setRemoveListener(this);
        mChannelListView.setDragEnabled(true);
        
        mChannelListView.setOnItemClickListener(new OnItemClickListener() 
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
            {
                Intent intent = new Intent(CollectActivity.this, ChannelDetailActivity.class);
                intent.putExtra("tvmao_id", mChannelList.get(position).tvmaoId);
                intent.putExtra("name", mChannelList.get(position).name);
                intent.putExtra("channel_list", (Serializable) mChannelList);
                startActivity(intent);
            }
        });
        
        createAndSetListViewAdapter();
        
        mTimer = new Timer(true);
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mChannelListView != null && mChannelListView.isShown()) {
                    mChannelListView.post(new Runnable() {  
                        @Override
                        public void run() {
                            mListViewAdapter.updateOnPlayingProgram();
                        }
                    });
                }
            }
        }, TIMER_SCHEDULE_PERIOD, TIMER_SCHEDULE_PERIOD);
    }
    
    @Override
    protected void onResume() 
    {
        super.onResume();
        updateChannelListView();
        
        if (mChannelList.isEmpty())
        {
            mContentLayout.removeAllViews();
            mContentLayout.addView(mNoCollectLayout, mCenterLayoutParams);
        }
        else
        {
            mContentLayout.removeAllViews();
            mContentLayout.addView(mChannelListView);
            // Show tips: long press to sort
            if (mChannelList.size() > 1 && sHasShownTips == false)
            {
                Toast.makeText(this, getResources().getString(R.string.long_press_sort_tips), Toast.LENGTH_LONG).show();
                sHasShownTips = true;
            }
        }
    };
    
    @Override
    protected void onDestroy() {
        mTimer.cancel();
        super.onDestroy();
    }

    private void createAndSetListViewAdapter()
    {
        initChannelList();
        List<HashMap<String, Object>> itemList = new ArrayList<HashMap<String, Object>>();
        for (Channel channel : mChannelList) {
            HashMap<String, Object> item = new HashMap<String, Object>();
            item.put("tvmao_id", channel.tvmaoId);
            item.put("name", channel.name);
            itemList.add(item);
        }
        mListViewAdapter = new CollectListAdapter(this, itemList);
        mListViewAdapter.setOnRemoveListener(new RemoveItemCallback() {
            @Override
            public void onRemove(int position, HashMap<String, Object> item) {
                mChannelList.remove(position);
                String tvmaoId = (String)item.get("tvmao_id");
                if (tvmaoId != null) {
                    AppEngine.getInstance().getCollectManager().removeCollectChannel(tvmaoId);
                }
            }
        });
        
        mChannelListView.setAdapter(mListViewAdapter);
    }
    
    private void initChannelList()
    {
        mChannelList.clear();
        Iterator<Entry<String, HashMap<String, Object>>> iter = AppEngine.getInstance().getCollectManager().getCollectChannels().entrySet().iterator();
        while (iter.hasNext())
        {
            Map.Entry<String, HashMap<String, Object>> entry = (Map.Entry<String, HashMap<String,Object>>)iter.next();
            Channel channel = new Channel();
            channel.tvmaoId = entry.getKey();
            channel.name = (String) entry.getValue().get("name");
            mChannelList.add(channel);
        }
    }
    
    private void updateChannelListView()
    {
        initChannelList();
        List<HashMap<String, Object>> itemList = new ArrayList<HashMap<String,Object>>();
        for (int i=0; i<mChannelList.size(); ++i)
        { 
            String id = mChannelList.get(i).tvmaoId;
            String name = mChannelList.get(i).name;
            
            HashMap<String, Object> item = new HashMap<String, Object>();            
            item.put("tvmao_id", id);
            item.put("name", name);
            itemList.add(item);
        }
        mListViewAdapter.update(itemList);
    }
    
    private DragSortController buildController(DragSortListView dslv) 
	{
        DragSortController controller = new DragSortController(dslv);
        controller.setRemoveEnabled(false);
        controller.setSortEnabled(true);
        controller.setDragInitMode(DragSortController.ON_LONG_PRESS);
//        controller.setRemoveMode(DragSortController.FLING_REMOVE);
        return controller;
    }

	@SuppressWarnings("unchecked")
    @Override
	public void drop(int from, int to) 
	{
		CollectManager manager = AppEngine.getInstance().getCollectManager();
		String dragChannelId = mChannelList.get(from).tvmaoId;
		if (dragChannelId != null)
		{
			HashMap<String, Object> dragChannelInfo = manager.removeCollectChannel(dragChannelId);
			manager.addCollectChannel(to, dragChannelId, dragChannelInfo);
		}
		
		Object dragObject = mListViewAdapter.remove(from);
		if (dragObject != null) {
		    Channel channel = mChannelList.remove(from);
            mChannelList.add(to, channel);
		    
		    HashMap<String, Object> item = (HashMap<String, Object>) dragObject;
            mListViewAdapter.add(to, item);
		}
	}

	@Override
	public void drag(int from, int to) 
	{
	}

	@Override
	public void remove(int which) 
	{
	}
}
