package com.tools.tvguide.managers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;

public class UpdateManager 
{
    private Context mContext;
    private String  mGuid;
    private final String FILE_GUID = "GUID.txt";
    
    public UpdateManager(Context context)
    {
        mContext = context;
        load();
    }
    
    public void setGUID(String guid)
    {
        mGuid = guid;
        try 
        {
            FileOutputStream fos = mContext.openFileOutput(FILE_GUID, Context.MODE_PRIVATE);
            fos.write(mGuid.getBytes());
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
    
    public String getGUID()
    {
        return mGuid;
    }
    
    private void load()
    {
        try
        {
            FileInputStream fis = mContext.openFileInput(FILE_GUID);
            byte[] tmpBytes = new byte[100];
            fis.read(tmpBytes);
            mGuid = new String(tmpBytes);
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
}
