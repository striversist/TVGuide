package com.tools.tvguide;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import com.tools.tvguide.managers.AppEngine;
import com.tools.tvguide.managers.HotHtmlManager.ProgramDetailCallback;
import com.tools.tvguide.utils.NetDataGetter;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import android.widget.TextView;

public class ProgramActivity extends Activity 
{
    private static int smRequestId = 0;
    private String mName;
    private String mLink;
    private String mProfile;
    private String mSummary;
    private String mActors;
    private Bitmap mPicture;
    private HashMap<String, List<String>> mPlayTimes;
    
    private TextView mProgramNameTextView;
    private TextView mProgramProfileTextView;
    private ImageView mProgramImageView;
    
    private final int MSG_PROFILE_LOADED = 1;
    private final int MSG_SUMMARY_LOADED = 2;
    private final int MSG_PICTURE_LOADED = 3;
    private final int MSG_ACTORS_LOADED = 4;
    private final int MSG_PLAYTIMES_LOADED = 5;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_program);
        mProgramNameTextView = (TextView) findViewById(R.id.program_name);
        mProgramProfileTextView = (TextView) findViewById(R.id.program_profile);
        mProgramImageView = (ImageView) findViewById(R.id.program_image);
        
        mName = getIntent().getStringExtra("name");
        mLink = getIntent().getStringExtra("link");
        
        if (mLink == null)
            return;
        
        mProgramNameTextView.setText(mName);
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
                        mPicture = getImage(picLink);
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
        });
    }
    
    private Bitmap getImage(String url)
    {
        Bitmap bitmap = null;
        try
        {
            bitmap = BitmapFactory.decodeStream(new NetDataGetter(url).getInputStream());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
        return bitmap;
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
                    break;
                case MSG_PICTURE_LOADED:
                    mProgramImageView.setImageBitmap(mPicture);
                    break;
                case MSG_SUMMARY_LOADED:
                    break;
                case MSG_ACTORS_LOADED:
                    break;
                case MSG_PLAYTIMES_LOADED:
                    break;
            }
        }
    };
}
