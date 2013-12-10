package com.tools.tvguide.activities;

import java.util.HashMap;
import java.util.List;

import net.youmi.android.banner.AdSize;
import net.youmi.android.banner.AdView;

import com.tools.tvguide.R;
import com.tools.tvguide.adapters.ResultPageAdapter;
import com.tools.tvguide.managers.AppEngine;
import com.tools.tvguide.managers.HotHtmlManager.EpisodeDetailCallback;
import com.tools.tvguide.views.SlidingMenuView;
import com.tools.tvguide.views.SlidingMenuView.OnSlidingMenuSelectListener;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Html;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.app.Activity;
import android.content.Intent;

public class EpisodeActivity extends Activity implements OnSlidingMenuSelectListener
{
    private List<HashMap<String, String>> mEpisodes;
    private SlidingMenuView mSlidingMenuView;
    private TextView mProgramNameTV;
    private LayoutInflater mInflater;
    private ViewPager mViewPager;
    private ResultPageAdapter mPageAdapter;
    private enum SelfMessage {MSG_SHOW_PLOTS}
    
    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episode);
        mInflater = LayoutInflater.from(this);
        mSlidingMenuView = (SlidingMenuView) findViewById(R.id.episode_slidingmenu);
        mViewPager = (ViewPager) findViewById(R.id.episode_viewpager);
        mProgramNameTV = (TextView) findViewById(R.id.episode_program_name_tv);
        
        Intent intent = getIntent();
        mEpisodes = (List<HashMap<String, String>>) intent.getSerializableExtra("episodes");
        if (mEpisodes == null)
            return;
        String programName = intent.getStringExtra("program_name");
        mProgramNameTV.setText(programName + getResources().getString(R.string.plot_detail));
        
        mPageAdapter = new ResultPageAdapter();
        for (int i=0; i<mEpisodes.size(); ++i)
        {
            LinearLayout loadingLayout = (LinearLayout)mInflater.inflate(R.layout.center_text_tips, null);
            ((TextView) loadingLayout.findViewById(R.id.center_tips_text_view)).setText(getResources().getString(R.string.loading_string));
            mPageAdapter.addView(loadingLayout);
        }
        mViewPager.setAdapter(mPageAdapter);
        
        mViewPager.setOnPageChangeListener(new OnPageChangeListener() 
        {
            @Override
            public void onPageSelected(int position) 
            {
                if (position >= mSlidingMenuView.getMenuCount())
                    return;
                if (position != mSlidingMenuView.getSelectIndex())
                    mSlidingMenuView.setSelectIndex(position);
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
        
        mSlidingMenuView.setOnSlidingMenuSelectListener(this);
        
        initSlidingMenu();
        update(0);
        
        // 将广告条adView添加到需要展示的layout控件中
        RelativeLayout adLayout = (RelativeLayout) findViewById(R.id.adLayout);
        AdView adView = new AdView(EpisodeActivity.this, AdSize.FIT_SCREEN);
        adLayout.addView(adView);
    }

    @Override
    public void onBackPressed() 
    {
        finish();
    }
    
    @Override
    public void onItemSelect(int index, Object paramObject) 
    {
        String url = (String)paramObject;
        mViewPager.setCurrentItem(index);
        update(index);
    }
    
    private void initSlidingMenu()
    {
        for (int i=0; i<mEpisodes.size(); ++i)
        {
            String name = mEpisodes.get(i).get("name");
            String link = mEpisodes.get(i).get("link");
            mSlidingMenuView.addMenu(name, link);
        }
    }
    
    private void update(int index)
    {
        if (index < 0 || index >= mEpisodes.size())
            return;
        
        String link = mEpisodes.get(index).get("link");
        AppEngine.getInstance().getHotHtmlManager().getEpisodesAsync(index, link, new EpisodeDetailCallback() 
        {
            @Override
            public void onEpisodeLoaded(int requestId, List<HashMap<String, String>> episodes) 
            {
                Message msg = Message.obtain(uiHandler, SelfMessage.MSG_SHOW_PLOTS.ordinal(), requestId, 0, episodes);
                uiHandler.sendMessage(msg);
            }
        });
    }
    
    private Handler uiHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            SelfMessage selfMsg = SelfMessage.values()[msg.what];
            switch (selfMsg)
            {
                case MSG_SHOW_PLOTS:
                    int index = msg.arg1;
                    List<HashMap<String, String>> episodes = (List<HashMap<String, String>>) msg.obj;
                    LinearLayout layout = (LinearLayout) mInflater.inflate(R.layout.program_tab_simpletext, null);
                    
                    String plots = "";
                    for (int i=0; i<episodes.size(); ++i)
                    {
                        plots += "<strong><big>" + episodes.get(i).get("title") + "</big></strong><br/>";
                        plots += "　　" + episodes.get(i).get("plot") + "<br/><br/>";
                    }
                    ((TextView) layout.findViewById(R.id.program_tab_simpletext)).setText(Html.fromHtml(plots));
                    ((LinearLayout) mPageAdapter.getView(index)).removeAllViews();
                    ((LinearLayout) mPageAdapter.getView(index)).addView(layout);
                    break;
            }
        }
    };
}
