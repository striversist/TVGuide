package com.tools.tvguide;

import java.util.HashMap;
import java.util.List;

import com.tools.tvguide.managers.AppEngine;
import com.tools.tvguide.managers.HotHtmlManager.ProgramDetailCallback;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class ProgramActivity extends Activity 
{
    private static int smRequestId = 0;
    private String mName;
    private String mLink;
    private String mProfile;
    private String mSummary;
    private String mActors;
    private HashMap<String, List<String>> mPlayTimes;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_program);
        
        mName = getIntent().getStringExtra("name");
        mLink = getIntent().getStringExtra("link");
        
        if (mLink == null)
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
        AppEngine.getInstance().getHotHtmlManager().getProgramDetailAsync(smRequestId, mLink, new ProgramDetailCallback()
        {
            @Override
            public void onSummaryLoaded(int requestId, String summary) 
            {
                mSummary = summary;
            }
            
            @Override
            public void onProfileLoaded(int requestId, String profile) 
            {
                mProfile = profile;
            }
            
            @Override
            public void onPlayTimesLoaded(int requestId, HashMap<String, List<String>> playTimes) 
            {
                mPlayTimes = playTimes;
            }
            
            @Override
            public void onPicureLinkParsed(int requestId, String picLink) 
            {
                
            }
            
            @Override
            public void onActorsLoaded(int requestId, String actors) 
            {
                mActors = actors;
            }
        });
    }
}
