package com.tools.tvguide.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.tools.tvguide.R;
import com.tools.tvguide.adapters.ResultPageAdapter;
import com.tools.tvguide.managers.AdManager.AdSize;
import com.tools.tvguide.managers.AppEngine;
import com.tools.tvguide.managers.HotHtmlManager.ProgramDetailCallback;
import com.tools.tvguide.utils.Utility;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class HotProgramActivity extends Activity 
{
    private static int smRequestId = 0;
    private String mName;
    private String mLink;
    private String mProfile;
    private String mSummary;
    private String mActors;
    private Bitmap mPicture;
    private HashMap<String, List<String>> mPlayTimes;
    private List<HashMap<String, String>> mEpisodes;
    
    private TextView mProgramNameTextView;
    private TextView mProgramProfileTextView;
    private ImageView mProgramImageView;
    private ImageView mPlotsImageView;
    private ViewPager mViewPager;
    private ResultPageAdapter mProgramPageAdapter;
    private LinearLayout mActorsLayout;
    private LinearLayout mSummaryLayout;
    private LinearLayout mPlayTimesLayout;
    private LinearLayout.LayoutParams mCenterLayoutParams;
    private LayoutInflater mInflater;
    private RadioGroup mTabsGroup;
    
    private final int MSG_PROFILE_LOADED = 1;
    private final int MSG_SUMMARY_LOADED = 2;
    private final int MSG_PICTURE_LOADED = 3;
    private final int MSG_ACTORS_LOADED = 4;
    private final int MSG_PLAYTIMES_LOADED = 5;
    private final int MSG_HAS_DETAIL_PLOTS = 6;
    
    private final int TAB_INDEX_ACTORS = 0;
    private final int TAB_INDEX_SUMMARY = 1;
    private final int TAB_INDEX_PLAYTIMES = 2;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hot_program);
        
        mInflater = LayoutInflater.from(this);
        mPlayTimes = new HashMap<String, List<String>>();
        mProgramNameTextView = (TextView) findViewById(R.id.program_name);
        mProgramProfileTextView = (TextView) findViewById(R.id.program_profile);
        mProgramImageView = (ImageView) findViewById(R.id.program_image);
        mPlotsImageView = (ImageView) findViewById(R.id.episode_iv);
        mViewPager = (ViewPager) findViewById(R.id.program_view_pager);
        mTabsGroup = (RadioGroup) findViewById(R.id.program_tabs);
        
        mProgramPageAdapter = new ResultPageAdapter();
        mActorsLayout = (LinearLayout) mInflater.inflate(R.layout.program_tab_simpletext, null);
        mSummaryLayout = (LinearLayout) mInflater.inflate(R.layout.program_tab_summary, null);
        mPlayTimesLayout = (LinearLayout) mInflater.inflate(R.layout.program_tab_playtimes, null);
        mCenterLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        
        for (int i=0; i<mTabsGroup.getChildCount(); ++i)
        {
            LinearLayout loadingLayout = (LinearLayout)mInflater.inflate(R.layout.center_text_tips, null);
            ((TextView) loadingLayout.findViewById(R.id.center_tips_text_view)).setText(getResources().getString(R.string.loading_string));
            mProgramPageAdapter.addView(loadingLayout);
        }
        mViewPager.setAdapter(mProgramPageAdapter);
        
        mViewPager.setOnPageChangeListener(new OnPageChangeListener() 
        {
            @Override
            public void onPageSelected(int position) 
            {
                if (position == TAB_INDEX_ACTORS)
                    mTabsGroup.check(R.id.program_tab_actors);
                else if (position == TAB_INDEX_SUMMARY)
                    mTabsGroup.check(R.id.program_tab_summary);
                else if (position == TAB_INDEX_PLAYTIMES)
                    mTabsGroup.check(R.id.program_tab_playtimes);
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
        
        mName = getIntent().getStringExtra("name");
        mLink = getIntent().getStringExtra("link");
        
        if (mLink == null)
            return;
        
        mProgramNameTextView.setText(mName);
        mPlotsImageView.setVisibility(View.INVISIBLE);
        update();
        
        new Handler().postDelayed(new Runnable() 
        {
            @Override
            public void run() 
            {
                AppEngine.getInstance().getAdManager().addAdView(HotProgramActivity.this, R.id.adLayout, AdSize.NORMAL_SIZE);
            }
        }, 500);
    }
 
    @Override
    public void onBackPressed() 
    {
        finish();
    }
    
    public void onClickTabs(View view)
    {
        switch(view.getId())
        {
            case R.id.program_tab_actors:
                mViewPager.setCurrentItem(TAB_INDEX_ACTORS);
                break;
            case R.id.program_tab_summary:
                mViewPager.setCurrentItem(TAB_INDEX_SUMMARY);
                break;
            case R.id.program_tab_playtimes:
                mViewPager.setCurrentItem(TAB_INDEX_PLAYTIMES);
                break;
        }
    }
    
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.episode_iv:
                startEpisodeActivity();
            break;
        }
    }
    
    private void startEpisodeActivity()
    {
        if (mEpisodes != null && mEpisodes.size() > 0)
        {
            Intent intent = new Intent(HotProgramActivity.this, EpisodeActivity.class);
            ArrayList<HashMap<String, String>> episodes = new ArrayList<HashMap<String,String>>();
            episodes.addAll(mEpisodes);
            intent.putExtra("source", "tvsou");
            intent.putExtra("episode_entry_list", episodes);
            intent.putExtra("program_name", mName);
            startActivity(intent);
        }
    }
    
    private void update()
    {
        smRequestId++;
        AppEngine.getInstance().getHotHtmlManager().getProgramDetailAsync(smRequestId, mLink, new ProgramDetailCallback()
        {
            @Override
            public void onSummaryLoaded(int requestId, List<String> paragraphs) 
            {
                mSummary = "";
                for (int i=0; i<paragraphs.size(); ++i)
                    mSummary += "　　" + paragraphs.get(i) + "\n";
                uiHandler.sendEmptyMessage(MSG_SUMMARY_LOADED);
            }
            
            @Override
            public void onProfileLoaded(int requestId, String profile) 
            {
                mProfile = profile;
                uiHandler.sendEmptyMessage(MSG_PROFILE_LOADED);
            }
            
            @Override
            public void onPlayTimesLoaded(int requestId, HashMap<String, List<String>> playTimes) 
            {
                mPlayTimes = playTimes;
                uiHandler.sendEmptyMessage(MSG_PLAYTIMES_LOADED);
            }
            
            @Override
            public void onPicureLinkParsed(int requestId, final String picLink) 
            {
                new Thread(new Runnable() 
                {
                    @Override
                    public void run() 
                    {
                        mPicture = Utility.getNetworkImage(picLink);
                        if (mPicture != null)
                            uiHandler.sendEmptyMessage(MSG_PICTURE_LOADED);
                    }
                }).start();
            }
            
            @Override
            public void onActorsLoaded(int requestId, String actors) 
            {
                mActors = actors;
                uiHandler.sendEmptyMessage(MSG_ACTORS_LOADED);
            }

            @Override
            public void onEpisodesLoaded(int requestId, List<HashMap<String, String>> episodes) 
            {
                mEpisodes = episodes;
                if (mEpisodes != null && mEpisodes.size() > 0)
                    uiHandler.sendEmptyMessage(MSG_HAS_DETAIL_PLOTS);
            }
        });
    }
    
    private Handler uiHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            switch (msg.what)
            {
                case MSG_PROFILE_LOADED:
                    mProgramProfileTextView.setText(mProfile);
                    if (mProfile.length() > 0)
                    {
                        ((ScrollView) findViewById(R.id.program_profile_scroll_view)).setVisibility(View.VISIBLE);
                        ((LinearLayout) findViewById(R.id.program_profile_loading_ll)).setVisibility(View.GONE);
                    }
                    else 
                    {
                        ((TextView) findViewById(R.id.program_profile_loading_tv)).setText(getResources().getString(R.string.no_data));
                    }
                    break;
                case MSG_PICTURE_LOADED:
                    mProgramImageView.setImageBitmap(mPicture);
                    break;
                case MSG_SUMMARY_LOADED:
                    ((TextView) mSummaryLayout.findViewById(R.id.program_tab_simpletext)).setText(mSummary);
                    ((LinearLayout) mProgramPageAdapter.getView(TAB_INDEX_SUMMARY)).removeAllViews();
                    ((LinearLayout) mProgramPageAdapter.getView(TAB_INDEX_SUMMARY)).addView(mSummaryLayout, mCenterLayoutParams);
                    mViewPager.setCurrentItem(TAB_INDEX_SUMMARY);
                    break;
                case MSG_ACTORS_LOADED:
                    ((TextView) mActorsLayout.findViewById(R.id.program_tab_simpletext)).setText(mActors);
                    ((LinearLayout) mProgramPageAdapter.getView(TAB_INDEX_ACTORS)).removeAllViews();
                    ((LinearLayout) mProgramPageAdapter.getView(TAB_INDEX_ACTORS)).addView(mActorsLayout, mCenterLayoutParams);
                    break;
                case MSG_PLAYTIMES_LOADED:                    
                    Iterator<Entry<String, List<String>>> iter = mPlayTimes.entrySet().iterator();
                    List<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
                    while (iter.hasNext())
                    {
                        Entry<String, List<String>> entry = iter.next();
                        String channelName = entry.getKey();
                        List<String> playTimes = entry.getValue();
                        
                        // Channel
                        HashMap<String, String> itemChannel = new HashMap<String, String>();
                        itemChannel.put("name", channelName);
                        data.add(itemChannel);
                        
                        // Program play times
                        for (int i=0; i<playTimes.size(); ++i)
                        {
                            HashMap<String, String> itemProgram = new HashMap<String, String>();
                            itemProgram.put("name", ("　" + playTimes.get(i)));
                            data.add(itemProgram);
                        }
                        
                        // Space line
                        HashMap<String, String> space = new HashMap<String, String>();
                        space.put("name", " ");
                        data.add(space);
                    }
                    ((ListView) mPlayTimesLayout.findViewById(R.id.program_tab_playtimes_listview)).setAdapter(new SimpleAdapter(HotProgramActivity.this, 
                            data, R.layout.program_tab_playtimes_item, new String[]{"name"}, new int[]{R.id.playtimes_item_text}));
                    ((LinearLayout) mProgramPageAdapter.getView(TAB_INDEX_PLAYTIMES)).removeAllViews();
                    ((LinearLayout) mProgramPageAdapter.getView(TAB_INDEX_PLAYTIMES)).addView(mPlayTimesLayout, mCenterLayoutParams);
                    break;
                case MSG_HAS_DETAIL_PLOTS:
                    mPlotsImageView.setVisibility(View.VISIBLE);
                    ((Button) mSummaryLayout.findViewById(R.id.more_plot_btn)).setVisibility(View.VISIBLE);
                    ((Button) mSummaryLayout.findViewById(R.id.more_plot_btn)).setOnClickListener(new View.OnClickListener() 
                    {                        
                        @Override
                        public void onClick(View view) 
                        {
                            startEpisodeActivity();
                        }
                    });
                    break;
            }
        }
    };
}
