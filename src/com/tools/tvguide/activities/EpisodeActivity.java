package com.tools.tvguide.activities;

import java.util.HashMap;
import java.util.List;

import com.tools.tvguide.R;
import com.tools.tvguide.managers.AppEngine;
import com.tools.tvguide.managers.HotHtmlManager.EpisodeDetailCallback;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;

public class EpisodeActivity extends Activity 
{
    private static int smRequestId = 0;
    private List<HashMap<String, String>> mEpisodeLinks;
    
    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episode);
        
        Intent intent = getIntent();
        mEpisodeLinks = (List<HashMap<String, String>>) intent.getSerializableExtra("episodes");
        if (mEpisodeLinks == null)
            return;
        
        update();
    }

    @Override
    public void onBackPressed() 
    {
        finish();
    }
    
    private void update()
    {
        smRequestId++;
        String firstLink = mEpisodeLinks.get(0).get("link");
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
