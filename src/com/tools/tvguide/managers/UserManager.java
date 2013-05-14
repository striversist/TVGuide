package com.tools.tvguide.managers;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

public class UserManager
{
    public static final String TAG = "UserManager";
    
    private Context mContext;
    private boolean mSettingChanged = false;
    private List<String> mCollectChannels;
    
    public UserManager(Context context)
    {
        mContext = context;
        mCollectChannels = new ArrayList<String>();
    }
    
    public void addCollectChannel(String channelId)
    {
        mCollectChannels.add(channelId);
        mSettingChanged = true;
    }
    
    public void removeCollectChannel(String channelId)
    {
        for (int i=0; i<mCollectChannels.size(); ++i)
        {
            if (mCollectChannels.get(i).equals(channelId))
            {
                mCollectChannels.remove(i);
                break;
            }
        }
    }
    
    public List<String> getCollectChannels()
    {
        return mCollectChannels;
    }
}
