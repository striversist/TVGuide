package com.tools.tvguide.activities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.tools.tvguide.R;
import com.tools.tvguide.adapters.HotProgramListAdapter;
import com.tools.tvguide.adapters.ResultPageAdapter;
import com.tools.tvguide.data.Program;
import com.tools.tvguide.data.ProgramType;
import com.tools.tvguide.managers.AppEngine;
import com.tools.tvguide.managers.ProgramHtmlManager.HotProgramsCallback;
import com.tools.tvguide.managers.UrlManager;
import com.tools.tvguide.views.MyViewPagerIndicator;

public class HotActivity extends BaseActivity implements Callback 
{
    private LayoutInflater mInflater;
    private ViewPager mViewPager;
    private MyViewPagerIndicator mIndicator;
    private ResultPageAdapter mPageAdapter;
    private HashMap<TabIndex, List<HashMap<String, String>>> mProgramInfoListMap;
    private HashMap<TabIndex, LinearLayout> mClassifyLayoutMap;
    private Handler mUiHandler;
    
    enum TabIndex {Drama, Tvcolumn, Movie}
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hot);
        
        mInflater = LayoutInflater.from(this);
        mViewPager = (ViewPager) findViewById(R.id.hot_view_pager);
        mIndicator = (MyViewPagerIndicator) findViewById(R.id.indicator);
        mProgramInfoListMap = new HashMap<TabIndex, List<HashMap<String,String>>>();
        mClassifyLayoutMap = new HashMap<TabIndex, LinearLayout>();
        mUiHandler = new Handler(this);
        
        mIndicator.addTab(getResources().getString(R.string.tv_drama), null);
        mIndicator.addTab(getResources().getString(R.string.tv_column), null);
        mIndicator.addTab(getResources().getString(R.string.movie), null);
        mIndicator.setCurrentTab(0);
        mIndicator.setOnTabClickListener(new MyViewPagerIndicator.OnTabClickListener() 
        {
            @Override
            public void onTabClick(int index, Object tag) 
            {
                mViewPager.setCurrentItem(index);
                update();
            }
        });
        
        mPageAdapter = new ResultPageAdapter();
        for (int i=0; i<mIndicator.getTabsCount(); ++i)
        {
            LinearLayout loadingLayout = (LinearLayout)mInflater.inflate(R.layout.center_text_tips_layout, null);
            ((TextView) loadingLayout.findViewById(R.id.center_tips_text_view)).setText(getResources().getString(R.string.loading_string));
            mPageAdapter.addView(loadingLayout);
            mClassifyLayoutMap.put(TabIndex.values()[mPageAdapter.getCount() - 1], (LinearLayout) mInflater.inflate(R.layout.hot_program_layout, null));
        }
        mViewPager.setAdapter(mPageAdapter);
        
        mViewPager.setOnPageChangeListener(new OnPageChangeListener() 
        {
            @Override
            public void onPageSelected(int position) 
            {
                mIndicator.setCurrentTab(position);
                update();
            }
            
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) 
            {
            }
            
            @Override
            public void onPageScrollStateChanged(int state) 
            {
            }
        });
        
        update();
    }
    
    private void update()
    {
        final TabIndex curIndex = TabIndex.values()[mViewPager.getCurrentItem()];
        String hotUrl = UrlManager.URL_WWW_HOME;
        ProgramType type = ProgramType.Drama;
        switch (curIndex)
        {
            case Drama:
                type = ProgramType.Drama;
                break;
            case Tvcolumn:
                type = ProgramType.Tvcolumn;
                break;
            case Movie:
                type = ProgramType.Movie;
                break;
        }

        if (mProgramInfoListMap.get(curIndex) != null)
        {
            mUiHandler.sendEmptyMessage(0);
            return;
        }
        
        AppEngine.getInstance().getProgramHtmlManager().getHotProgramsAsync(0, hotUrl, type, new HotProgramsCallback() 
        {
            @Override
            public void onProgramsLoaded(int requestId, List<HashMap<String, String>> programInfoList) 
            {
                if (!programInfoList.isEmpty())
                {
                    List<HashMap<String, String>> tmpProgramInfoList = new ArrayList<HashMap<String,String>>();
                    tmpProgramInfoList.addAll(programInfoList);
                    mProgramInfoListMap.put(curIndex, tmpProgramInfoList);
                    mUiHandler.sendEmptyMessage(0);
                }
            }
        });
    }

    @Override
    public boolean handleMessage(Message msg) 
    {
        switch (msg.what) 
        {
            case 0:
                TabIndex curIndex = TabIndex.values()[mViewPager.getCurrentItem()];
                if (mProgramInfoListMap.get(curIndex) == null)
                    break;
                
                LinearLayout layout = mClassifyLayoutMap.get(curIndex);
                ListView hotProgramListView = (ListView) layout.findViewById(R.id.hot_program_listview);
                HotProgramListAdapter adapter = (HotProgramListAdapter) hotProgramListView.getAdapter();
                if (adapter == null)
                {
                    adapter = new HotProgramListAdapter(HotActivity.this, mProgramInfoListMap.get(curIndex));
                    hotProgramListView.setAdapter(adapter);
                }
                else 
                {
                    adapter.updateItems(mProgramInfoListMap.get(curIndex));
                }
                
                hotProgramListView.setOnItemClickListener(new OnItemClickListener() 
                {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
                    {
                        @SuppressWarnings("unchecked")
                        HashMap<String, String> programInfo = (HashMap<String, String>) parent.getItemAtPosition(position);
                        if (programInfo == null)
                            return;
                        
                        String name = programInfo.get("name");
                        String link = programInfo.get("link");
                        
                        TabIndex curIndex = TabIndex.values()[mViewPager.getCurrentItem()];
                        if (curIndex == TabIndex.Tvcolumn) {
                            link = link.replace("www.tvmao.com", "m.tvmao.com");
                        } else if (curIndex == TabIndex.Drama
                                || curIndex == TabIndex.Movie) {
                            link = link.replace("m.tvmao.com", "www.tvmao.com");
                        }
                        
                        Intent intent = new Intent(HotActivity.this, ProgramActivity.class);
                        Program program = new Program();
                        program.title = name;
                        program.link = link;
                        intent.putExtra("program", (Serializable) program);
                        startActivity(intent);
                    }
                });
                
                ((LinearLayout) mPageAdapter.getView(mViewPager.getCurrentItem())).removeAllViews();
                ((LinearLayout) mPageAdapter.getView(mViewPager.getCurrentItem())).addView(layout);
                break;
        }
        return true;
    }
}
