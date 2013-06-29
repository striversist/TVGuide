package com.tools.tvguide.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;

public class AlarmAlertActivity extends Activity 
{
    public void onCreate(Bundle SavedInstanceState) 
    {
        super.onCreate(SavedInstanceState);
        final MediaPlayer localMediaPlayer = MediaPlayer.create(this, getDefaultRingtoneUri(4));
        localMediaPlayer.setLooping(true);
        localMediaPlayer.start();
        String channel = getIntent().getStringExtra("channel");
        String program = getIntent().getStringExtra("program");
        AlertDialog dialog = new AlertDialog.Builder(AlarmAlertActivity.this).setIcon(R.drawable.clock)
                .setTitle(channel)
                .setMessage(program)
                .setPositiveButton("È·¶¨", new DialogInterface.OnClickListener() 
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) 
                    {
                        AlarmAlertActivity.this.finish();
                        localMediaPlayer.stop();
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
    
    public Uri getDefaultRingtoneUri(int paramInt)
    {
        return RingtoneManager.getActualDefaultRingtoneUri(this, paramInt);
    }
}
