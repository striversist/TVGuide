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

public class CacheManager 
{
    private Context mContext;
    private HashMap<String, List<HashMap<String, String>>> mCategories; 
    private String FILE_CACHED_CATEGORIES = "categories.txt";
        
    public CacheManager(Context context)
    {
        mContext = context;
    }
    
    /*
     * return: true: success, false: fail
     */
    @SuppressWarnings("unchecked")
    public boolean loadCategories(final String type, final List<HashMap<String, String>> result)
    {
        mCategories = (HashMap<String, List<HashMap<String, String>>>) loadObjectFromFile(FILE_CACHED_CATEGORIES);
        
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
    
    public void saveCatgegories(String type, List<HashMap<String, String>> categories)
    {
        mCategories.put(type, categories);
        try
        {
            FileOutputStream fos = mContext.openFileOutput(FILE_CACHED_CATEGORIES, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(mCategories);
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
}
