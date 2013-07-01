package com.tools.tvguide.managers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.HashMap;

import android.content.Context;

public class CollectManager
{
    public static final String TAG = "UserSettingManager";
    
    private Context mContext;
    private boolean mSettingChanged = false;
    private HashMap<String, HashMap<String, Object>> mCollectChannels;
    private String FILE_COLLECT_CHANNELS = "collect_channels.txt";
    
    public CollectManager(Context context)
    {
        mContext = context;
        loadCollectChannels();
    }
    
    public void addCollectChannel(String id, HashMap<String, Object> info)
    {
        mCollectChannels.put(id, info);
        mSettingChanged = true;
    }
    
    public void removeCollectChannel(String id)
    {
        mCollectChannels.remove(id);
        mSettingChanged = true;
    }
    
    public HashMap<String, HashMap<String, Object>> getCollectChannels()
    {
        return mCollectChannels;
    }
    
    public void shutDown()
    {
        if (mSettingChanged)
        {
            saveCollectChannels();
        }
    }
    
    private void saveCollectChannels()
    {
        try
        {
            FileOutputStream fos = mContext.openFileOutput(FILE_COLLECT_CHANNELS, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(mCollectChannels);
            oos.flush();
            oos.close();
            fos.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    @SuppressWarnings("unchecked")
    private void loadCollectChannels()
    {
        boolean loadSuccess = true;
        try
        {
            FileInputStream fis = mContext.openFileInput(FILE_COLLECT_CHANNELS);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Object obj = ois.readObject();
            if (obj instanceof HashMap<?, ?>)
            {
                mCollectChannels =  (HashMap<String, HashMap<String, Object>>) obj;
            }
            else 
            {
                loadSuccess = false;
            }
            ois.close();
            fis.close();
        }
        catch (StreamCorruptedException e)
        {
            loadSuccess = false;
            e.printStackTrace();
        }
        catch (FileNotFoundException e)
        {
            loadSuccess = false;
            e.printStackTrace();
        }
        catch (IOException e)
        {
            loadSuccess = false;
            e.printStackTrace();
        }
        catch (ClassNotFoundException e)
        {
            loadSuccess = false;
            e.printStackTrace();
        }
        
        if (loadSuccess == false)
        {
            mCollectChannels = new HashMap<String, HashMap<String,Object>>();
        }
    }
}
