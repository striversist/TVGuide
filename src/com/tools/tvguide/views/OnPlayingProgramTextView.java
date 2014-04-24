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
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

public class OnPlayingProgramTextView extends TextView implements Callback {

    private static final String TAG = "OnPlayingProgramTextView";
    private String mTvmaoId;
    private int mDay;
    private int mRequestId = 0;
    private List<Program> mProgramList = new ArrayList<Program>();
    private HandlerThread mHandlerThread;
    private Handler mWorkerHandler;
    private Handler mUiHandler;
    private enum SelfMessage { Update_OnPlaying_Program }
    
    public OnPlayingProgramTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
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
        
        initHandler();
        if (TextUtils.equals(mTvmaoId, tvmaoId) && mDay == day) {
            if (!mProgramList.isEmpty()) {
                updateOnPlayingProgram();
            }
        }
        
        mTvmaoId = tvmaoId;
        mDay = day;
        mRequestId++;
        String channelUrl = UrlManager.getSimpleWebChannelUrl(tvmaoId, day);
        AppEngine.getInstance().getChannelHtmlManager().getChannelDetailFromSimpleWebAsync(mRequestId, channelUrl, 
                new ChannelDetailCallback() {
            
            @Override
            public void onProgramsLoaded(int requestId, List<Program> programList) {
                if (requestId != mRequestId)    // 在回调之前已经被改变，则Cancel之前的操作
                    return;
                if (programList != null) {
                    mProgramList.clear();
                    mProgramList.addAll(programList);
                    updateOnPlayingProgram();
                }
            }
            
            @Override
            public void onDateLoaded(int requestId, List<ChannelDate> channelDateList) {
                if (requestId != mRequestId)    // 在回调之前已经被改变，则Cancel之前的操作
                    return;
            }
        }, mWorkerHandler);
        
        return true;
    }
    
    private void updateOnPlayingProgram() {
        Program onPlayingProgram = ProgramUtil.getOnplayingProgramByTime(mProgramList, 
                System.currentTimeMillis());
        if (onPlayingProgram != null) {
            mUiHandler.obtainMessage(SelfMessage.Update_OnPlaying_Program.ordinal(), onPlayingProgram)
                    .sendToTarget();
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        SelfMessage selfMsg = SelfMessage.values()[msg.what];
        switch (selfMsg) {
            case Update_OnPlaying_Program:
                Program onplayingProgram = (Program) msg.obj;
                setText("正在播出：" + onplayingProgram.time + ": " + onplayingProgram.title);
                break;
        }
        return false;
    }
}
