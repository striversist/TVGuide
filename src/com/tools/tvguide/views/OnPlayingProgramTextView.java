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
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

public class OnPlayingProgramTextView extends TextView {

    private static final String TAG = "OnPlayingProgramTextView";
    private static HandlerThread sHandlerThread;
    private static Handler sWorkerHandler;
    private String mTvmaoId;
    private int mDay;
    private int mRequestId = 0;
    private List<Program> mProgramList = new ArrayList<Program>();
    
    public interface UpdateCallback {
        public void onUpdate(TextView textView, String text);
    }
    
    public OnPlayingProgramTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    private void initHandler() {
        if (sWorkerHandler == null || sHandlerThread == null) {
            sHandlerThread = new HandlerThread(TAG);
            sHandlerThread.start();
            sWorkerHandler = new Handler(sHandlerThread.getLooper());
        }
    }
    
    /**
     * 在设置了tvmaoId和day的情况下，直接update
     * @return
     */
    public boolean update() {
        if (mTvmaoId == null || mDay == 0)
            return false;
        return update(mTvmaoId, mDay, null);
    }
    
    /**
     * 设置对应的频道id，更新当天的正在播放的节目
     * @param tvmaoId
     * @return
     */
    public boolean update(String tvmaoId, UpdateCallback callback) {
        if (tvmaoId == null)
            return false;
        return update(tvmaoId, Utility.getProxyDay(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)), callback);
    }

    /**
     * 设置对应的频道id和天数
     * @param tvmaoId
     * @param day
     * @return
     */
    public boolean update(String tvmaoId, int day, final UpdateCallback callback) {
        if (tvmaoId == null || day < 0)
            return false;
        
        initHandler();
        if (TextUtils.equals(mTvmaoId, tvmaoId) && mDay == day) {
            if (!mProgramList.isEmpty()) {
                updateOnPlayingProgram(callback);
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
                    updateOnPlayingProgram(callback);
                }
            }
            
            @Override
            public void onDateLoaded(int requestId, List<ChannelDate> channelDateList) {
                if (requestId != mRequestId)    // 在回调之前已经被改变，则Cancel之前的操作
                    return;
            }

            @Override
            public void onError(int requestId, String errorMsg) {
                
            }
        }, sWorkerHandler);
        
        return true;
    }
    
    private void updateOnPlayingProgram(final UpdateCallback callback) {
        // 由于getOnplayingProgramByTime操作比较耗时，故不放在UI线程中
        sWorkerHandler.post(new Runnable() {
            @Override
            public void run() {
                final Program onPlayingProgram = ProgramUtil.getOnplayingProgramByTime(mProgramList, 
                        System.currentTimeMillis());
                if (onPlayingProgram != null) {
                    String text = "正在播出：" + onPlayingProgram.time + ": " + onPlayingProgram.title;
                    setTextOnUI(text);
                    if (callback != null) {
                        callback.onUpdate(OnPlayingProgramTextView.this, text);
                    }
                }
            }
        });
    }
    
    private void setTextOnUI(final String text) {
        post(new Runnable() { 
            @Override
            public void run() {
                setText(text);
            }
        });
    }
}
