package com.tools.tvguide.activities;

import java.util.HashMap;
import java.util.List;

import com.tools.tvguide.R;
import com.tools.tvguide.managers.AppEngine;
import com.tools.tvguide.managers.HotHtmlManager.EpisodeDetailCallback;
import com.tools.tvguide.views.SlidingMenuView;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import android.app.Activity;
import android.content.Intent;

public class EpisodeActivity extends Activity 
{
    private static int smRequestId = 0;
    private List<HashMap<String, String>> mEpisodes;
    private SlidingMenuView mSlidingMenuView;
    private TextView mProgramNameTV;
    
    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episode);
        mSlidingMenuView = (SlidingMenuView) findViewById(R.id.episode_slidingmenu);
        mProgramNameTV = (TextView) findViewById(R.id.episode_program_name_tv);
        
        Intent intent = getIntent();
        mEpisodes = (List<HashMap<String, String>>) intent.getSerializableExtra("episodes");
        if (mEpisodes == null)
            return;
        String programName = intent.getStringExtra("program_name");
        mProgramNameTV.setText(programName + getResources().getString(R.string.plot_detail));
        
        update();
    }

    @Override
    public void onBackPressed() 
    {
        finish();
    }
    
    private void update()
    {
        for (int i=0; i<mEpisodes.size(); ++i)
        {
            String name = mEpisodes.get(i).get("name");
            String link = mEpisodes.get(i).get("link");
            mSlidingMenuView.addMenu(name, link);
        }
        
        smRequestId++;
        String firstLink = mEpisodes.get(0).get("link");
        AppEngine.getInstance().getHotHtmlManager().getEpisodesAsync(smRequestId, firstLink, new EpisodeDetailCallback() 
        {
            @Override
            public void onEpisodeLoaded(int requestId, List<HashMap<String, String>> episodes) 
            {
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
            }
        }
    };
}
