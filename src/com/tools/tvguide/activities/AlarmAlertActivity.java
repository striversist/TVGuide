package com.tools.tvguide.activities;

import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.KeyEvent;

import com.tools.tvguide.R;
import com.tools.tvguide.data.AlarmData;
import com.tools.tvguide.data.AlarmData.AlarmMode;
import com.tools.tvguide.data.Channel;
import com.tools.tvguide.data.Program;
import com.tools.tvguide.managers.AppEngine;

public class AlarmAlertActivity extends BaseActivity 
{
	private MediaPlayer mMediaPlayer;
	private Vibrator	mVibrator;
	
    public void onCreate(Bundle SavedInstanceState) 
    {
        super.onCreate(SavedInstanceState);
        Uri alarmUri = getDefaultRingtoneUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri == null)
            alarmUri = getDefaultRingtoneUri(RingtoneManager.TYPE_RINGTONE);
        if (alarmUri == null)
            alarmUri = getDefaultRingtoneUri(RingtoneManager.TYPE_NOTIFICATION);
        if (alarmUri != null)
            mMediaPlayer = MediaPlayer.create(this, alarmUri);
        else
            mMediaPlayer = null;
        
        mVibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
        
        final AlarmData alarmData = (AlarmData) getIntent().getSerializableExtra("alarm_data");
        if (alarmData == null)
            return;

        startMakingNoisy();
        final Channel channel = alarmData.getRelatedChannel();
        final Program program = alarmData.getRelatedProgram();
        AlertDialog dialog = new AlertDialog.Builder(AlarmAlertActivity.this).setIcon(R.drawable.clock)
                .setTitle(channel.name)
                .setMessage(program.getFullName())
                .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() 
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) 
                    {
                        stopMakingNoisy();
                        if (AppEngine.getInstance().getContext() == null)
                            AppEngine.getInstance().setContext(AlarmAlertActivity.this);    // 需要设置，否则会有空指针的异常
                        
                        AppEngine.getInstance().getAlarmHelper().notifyAlarmListeners(alarmData);
                        if (alarmData.getMode() == AlarmMode.Once) {
                            AppEngine.getInstance().getAlarmHelper().removeAlarmData(alarmData);
                        }
                        AlarmAlertActivity.this.finish();
                    }
                }).create();
        dialog.setCancelable(false);
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() 
        {       
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) 
            {
                if (keyCode == KeyEvent.KEYCODE_SEARCH)
                    return true;
                else
                    return false;
            }
        });
        dialog.show();
    }
    
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        resetAllAlarms();
    }
    
    public Uri getDefaultRingtoneUri(int type)
    {
        return RingtoneManager.getActualDefaultRingtoneUri(this, type);
    }
    
    private void startMakingNoisy()
    {
        if (mMediaPlayer != null)
        {
            mMediaPlayer.setLooping(true);
            mMediaPlayer.start();
        }
        if (mVibrator != null)
        {
            mVibrator.vibrate(new long[]{600, 1000, 600, 1000, 600, 1000}, 0);
        }
    }
    
    private void stopMakingNoisy()
    {
    	if (mMediaPlayer != null)
    	    mMediaPlayer.stop();
    	if (mVibrator != null)
    	    mVibrator.cancel();
    }
    
    // 重新计算闹铃时间
    private void resetAllAlarms()
    {
        if (AppEngine.getInstance().getContext() == null)
            AppEngine.getInstance().setContext(AlarmAlertActivity.this);
        AppEngine.getInstance().getAlarmHelper().resetAllAlarms();
    }
}
