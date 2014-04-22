package com.tools.tvguide.views;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.tools.tvguide.data.ChannelDate;
import com.tools.tvguide.data.Program;
import com.tools.tvguide.managers.AppEngine;
import com.tools.tvguide.managers.ChannelHtmlManager.ChannelDetailCallback;
import com.tools.tvguide.managers.UrlManager;
import com.tools.tvguide.utils.ProgramUtil;
import com.tools.tvguide.utils.Utility;

import android.content.Context;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.TextView;

public class OnPlayingProgramTextView extends TextView implements Callback {

    private static final String TAG = "OnPlayingProgramTextView";
    private String mTvmaoId;
    private int mDay;
    private List<Program> mProgramList = new ArrayList<Program>();
    private HandlerThread mHandlerThread;
    private Handler mWorkerHandler;
    private Handler mUiHandler;
    private enum SelfMessage { Update_OnPlaying_Program }
    
    public OnPlayingProgramTextView(Context context) {
        super(context);
        initHandler();
    }
    
    public OnPlayingProgramTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initHandler();
    }
    
    public OnPlayingProgramTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initHandler();
    }
    
    private void initHandler() {
        if (mUiHandler == null) {
            mUiHandler = new Handler(this);
        }
        if (mWorkerHandler == null || mHandlerThread == null) {
            mHandlerThread = new HandlerThread(TAG);
            mHandlerThread.start();
            mWorkerHandler = new Handler(mHandlerThread.getLooper());
        }
    }
    
    /**
     * 在设置了tvmaoId和day的情况下，直接update
     * @return
     */
    public boolean update() {
        if (mTvmaoId == null || mDay == 0)
            return false;
        return update(mTvmaoId, mDay);
    }
    
    /**
     * 设置对应的频道id，更新当天的正在播放的节目
     * @param tvmaoId
     * @return
     */
    public boolean update(String tvmaoId) {
        if (tvmaoId == null)
            return false;
        return update(tvmaoId, Utility.getProxyDay(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)));
    }

    /**
     * 设置对应的频道id和天数
     * @param tvmaoId
     * @param day
     * @return
     */
    public boolean update(String tvmaoId, int day) {
        if (tvmaoId == null || day < 0)
            return false;
        
        mTvmaoId = tvmaoId;
        mDay = day;
        String channelUrl = UrlManager.getWebChannelUrl(tvmaoId, day);
        AppEngine.getInstance().getChannelHtmlManager().getChannelDetailFromSimpleWebAsync(0, channelUrl, 
                new ChannelDetailCallback() {
            
            @Override
            public void onProgramsLoaded(int requestId, List<Program> programList) {
                if (programList != null) {
                    mProgramList.clear();
                    mProgramList.addAll(programList);
                    
                    Program onPlayingProgram = ProgramUtil.getOnplayingProgramByTime(mProgramList, 
                            System.currentTimeMillis());
                    if (onPlayingProgram != null) {
                        mUiHandler.obtainMessage(SelfMessage.Update_OnPlaying_Program.ordinal(), onPlayingProgram)
                                .sendToTarget();
                    }
                }
            }
            
            @Override
            public void onDateLoaded(int requestId, List<ChannelDate> channelDateList) {
            }
        }, mWorkerHandler);
        
        return true;
    }

    @Override
    public boolean handleMessage(Message msg) {
        SelfMessage selfMsg = SelfMessage.values()[msg.what];
        switch (selfMsg) {
            case Update_OnPlaying_Program:
                Program onplayingProgram = (Program) msg.obj;
                setText(onplayingProgram.time + ": " + onplayingProgram.title);
                break;
        }
        return false;
    }
}
