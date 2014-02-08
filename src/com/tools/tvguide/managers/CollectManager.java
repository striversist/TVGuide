package com.tools.tvguide.managers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.tools.tvguide.components.Shutter;

import android.content.Context;

public class CollectManager implements Shutter
{
    public static final String TAG = "UserSettingManager";
    
    private Context mContext;
    private boolean mSettingChanged = false;
    private LinkedHashMap<String, HashMap<String, Object>> mCollectChannels;
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
    
    public void addCollectChannel(int position, String id, HashMap<String, Object> info)
    {
    	if (position > mCollectChannels.size() || id == null || info == null)
    		return;

    	if (isChannelCollected(id))		// 若已存在，则不会生效
    		return;
    	
    	if (position == mCollectChannels.size())	// 在尾部添加
    	{
    		mCollectChannels.put(id, info);
    	}
    	else
    	{
	    	int index = 0;
	    	LinkedHashMap<String, HashMap<String, Object>> newCollectChannels = new LinkedHashMap<String, HashMap<String,Object>>();
	    	Iterator<Entry<String, HashMap<String, Object>>> iter = mCollectChannels.entrySet().iterator();
	        while (iter.hasNext())
	        {
	        	if (index == position)
	        	{
	        		newCollectChannels.put(id, info);
	        	}
	        	else
	        	{
		            Map.Entry<String, HashMap<String, Object>> entry = (Map.Entry<String, HashMap<String,Object>>)iter.next();
		            String key = entry.getKey();
		            HashMap<String, Object> value = entry.getValue();
		            newCollectChannels.put(key, value);
	        	}
	            
	            index++;
	        }
	        mCollectChannels.clear();
	        mCollectChannels.putAll(newCollectChannels);
    	}
    }
    
    public HashMap<String, Object> removeCollectChannel(String id)
    {
    	HashMap<String, Object> removeChannelInfo = mCollectChannels.get(id);
        mCollectChannels.remove(id);
        mSettingChanged = true;
        return removeChannelInfo;
    }
    
    public HashMap<String, Object> getCollectChannel(String id)
    {
    	return mCollectChannels.get(id);
    }
    
    public HashMap<String, HashMap<String, Object>> getCollectChannels()
    {
        return mCollectChannels;
    }
    
    public boolean isChannelCollected(String id)
    {
        return mCollectChannels.containsKey(id);
    }
    
    @Override
    public void onShutDown()
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
                mCollectChannels =  (LinkedHashMap<String, HashMap<String, Object>>) obj;
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
            mCollectChannels = new LinkedHashMap<String, HashMap<String,Object>>();
        }
    }
}
