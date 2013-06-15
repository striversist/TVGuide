package com.tools.tvguide.managers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.util.Pair;

public class CacheManager 
{
    private Context mContext;
    private HashMap<String, List<HashMap<String, String>>> mCategories;
    private HashMap<String, List<Pair<String, String>>> mChannels;
    private final String FILE_CACHED_CATEGORIES = "categories.txt";
    private final String FILE_CACHED_CHANNELS   = "channels.txt";
        
    public CacheManager(Context context)
    {
        mContext = context;
    }
    
    /*
     * return: true on success; false on fail
     */
    @SuppressWarnings("unchecked")
    public boolean loadCategoriesByType(final String type, final List<HashMap<String, String>> result)
    {
        mCategories = (HashMap<String, List<HashMap<String, String>>>) loadObjectFromFile(FILE_CACHED_CATEGORIES);
        if (mCategories == null)
        {
            mCategories = new HashMap<String, List<HashMap<String,String>>>();
            return false;
        }
        
        List<HashMap<String, String>> categories = mCategories.get(type);
        if (categories == null || categories.isEmpty())
        {
            return false;
        }
        for (int i=0; i<categories.size(); ++i)
        {
            result.add(categories.get(i));
        }
        return true;
    }
    
    /*
     * return: true on success; false on fail
     */
    public boolean saveCatgegoriesByType(String type, List<HashMap<String, String>> categories)
    {
        mCategories.put(type, categories);
        return saveObjectToFile(mCategories, FILE_CACHED_CATEGORIES);
    }
    
    @SuppressWarnings("unchecked")
    public boolean loadChannelsByCategory(final String categoryId, final List<HashMap<String, String>> result)
    {
        mChannels = (HashMap<String, List<Pair<String, String>>>) loadObjectFromFile(FILE_CACHED_CHANNELS);
        if (mChannels == null)
        {
            mChannels = new HashMap<String, List<Pair<String,String>>>();
            return false;
        }
        
        List<Pair<String, String>> channels = mChannels.get(categoryId);
        if (channels == null || channels.isEmpty())
        {
            return false;
        }
        for (int i=0; i<channels.size(); ++i)
        {
//            result.add(channels.get(i));
        }
        return true;
    }
    
    public boolean saveChannelsByCategory(final String categoryId, final List<Pair<String, String>> channels)
    {
        mChannels.put(categoryId, channels);
        return saveObjectToFile(mChannels, FILE_CACHED_CHANNELS);
    }
    
    private Object loadObjectFromFile(String fileName)
    {
        try
        {
            FileInputStream fis = mContext.openFileInput(fileName);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Object obj = ois.readObject();
            ois.close();
            fis.close();
            return obj;
        }
        catch (StreamCorruptedException e)
        {
            e.printStackTrace();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        
        return null;
    }
    
    private boolean saveObjectToFile(Object obj, String fileName)
    {
        try
        {
            FileOutputStream fos = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(obj);
            oos.flush();
            oos.close();
            fos.close();
            return true;
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
        return false;
    }
}
