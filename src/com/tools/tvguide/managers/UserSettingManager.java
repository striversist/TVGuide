package com.tools.tvguide.managers;

import java.util.HashMap;

import android.content.Context;

public class UserSettingManager
{
    public static final String TAG = "UserSettingManager";
    
    private Context mContext;
    private boolean mSettingChanged = false;
    private HashMap<String, HashMap<String, Object>> mCollectChannels;
    
    public UserSettingManager(Context context)
    {
        mContext = context;
        mCollectChannels = new HashMap<String, HashMap<String,Object>>();
    }
    
    public void addCollectChannel(String id, HashMap<String, Object> info)
    {
        mCollectChannels.put(id, info);
        mSettingChanged = true;
    }
    
    public void removeCollectChannel(String id)
    {
        for (int i=0; i<mCollectChannels.size(); ++i)
        {
            if (mCollectChannels.get(i).equals(id))
            {
                mCollectChannels.remove(i);
                break;
            }
        }
    }
    
    public HashMap<String, HashMap<String, Object>> getCollectChannels()
    {
        return mCollectChannels;
    }
}
