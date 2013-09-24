package com.tools.tvguide.managers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import com.tools.tvguide.components.VersionController;

import android.content.Context;

public class UpdateManager 
{
    private Context mContext;
    private String  mGuid;
    private boolean mChecked = false;
    private boolean mIsNeedUpdate = false;
    private final String FILE_GUID = "GUID.txt";
    private VersionController mVersionController;
    
    public interface IOCompleteCallback
    {
        void OnIOComplete(int result);
        final int NEED_UPDATE = 0;
        final int NO_NEED_UPDATE = 1;
    }
    public UpdateManager(Context context)
    {
        mContext = context;
        mVersionController = new VersionController(context);
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
    
    public boolean checkUpdate(final IOCompleteCallback callback)
    {
        new Thread()
        {
            public void run()
            {
                if (mVersionController.checkLatestVersion())
                {
                    mIsNeedUpdate = true;
                    callback.OnIOComplete(IOCompleteCallback.NEED_UPDATE);
                }
                else
                {
                    callback.OnIOComplete(IOCompleteCallback.NO_NEED_UPDATE);
                }
                mChecked = true;
            }
        }.start();
        return false;
    }
    
    public boolean isNeedUpdate()
    {
        assert(mChecked);
        return mIsNeedUpdate;
    }
    
    public String currentVersionName()
    {
        return mVersionController.getCurrentVersionName();
    }
    
    public String latestVersionName()
    {
        assert(mChecked);
        return mVersionController.getLatestVersionName();
    }
    
    public String getUrl()
    {
        assert(mChecked);
        return mVersionController.getUrl();
    }
    
    public int getLatestChannelVersion()
    {
        assert(mChecked);
        return mVersionController.getLatestChannelVersion();
    }
    
    private void load()
    {
        try
        {           
            FileReader reader = new FileReader(mContext.getFileStreamPath(FILE_GUID));
            BufferedReader bufferedReader = new BufferedReader(reader);
            mGuid = bufferedReader.readLine();
            bufferedReader.close();
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
