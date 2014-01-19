package com.tools.tvguide;

import com.tools.tvguide.data.Program;
import com.tools.tvguide.managers.AppEngine;
import com.tools.tvguide.managers.ProgramHtmlManager;
import com.tools.tvguide.utils.Utility;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class ProgramActivity extends Activity 
{
    private static int sRequestId;
    private String mProfile;
    private Bitmap mPicture;
    
    private Program mProgram;
    private TextView mProgramNameTextView;
    private TextView mProgramProfileTextView;
    private ImageView mProgramImageView;
    
    enum SelfMessage {MSG_SUMMARY_LOADED, MSG_PROFILE_LOADED, MSG_PICTURE_LOADED, MSG_HAS_DETAIL_PLOTS};
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_program);
        
        mProgram = (Program) getIntent().getSerializableExtra("program");
        if (mProgram == null)
            return;
        
        mProgramNameTextView = (TextView) findViewById(R.id.program_name);
        mProgramProfileTextView = (TextView) findViewById(R.id.program_profile);
        mProgramImageView = (ImageView) findViewById(R.id.program_image);
        
        mProgramNameTextView.setText(mProgram.title);
        
        update();
    }

    private void update()
    {
        sRequestId++;
        AppEngine.getInstance().getProgramHtmlManager().getProgramDetailAsync(sRequestId, mProgram.link, new ProgramHtmlManager.ProgramDetailCallback() 
        {
            @Override
            public void onSummaryLoaded(int requestId, String summary) 
            {
                uiHandler.sendEmptyMessage(SelfMessage.MSG_SUMMARY_LOADED.ordinal());
            }
            
            @Override
            public void onProfileLoaded(int requestId, String profile) 
            {
                mProfile = profile;
                uiHandler.sendEmptyMessage(SelfMessage.MSG_PROFILE_LOADED.ordinal());
            }
            
            @Override
            public void onPictureLinkParsed(int requestId, final String link) 
            {
                new Thread(new Runnable() 
                {
                    @Override
                    public void run() 
                    {
                        mPicture = Utility.getNetworkImage(link);
                        if (mPicture != null)
                            uiHandler.sendEmptyMessage(SelfMessage.MSG_PICTURE_LOADED.ordinal());
                    }
                }).start();
            }
            
            @Override
            public void onEpisodeLinkParsed(int requestId, String link) 
            {
                uiHandler.sendEmptyMessage(SelfMessage.MSG_HAS_DETAIL_PLOTS.ordinal());
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
                case MSG_SUMMARY_LOADED:
                    break;
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
                case MSG_HAS_DETAIL_PLOTS:
                    break;
            }
        }
    };
}
