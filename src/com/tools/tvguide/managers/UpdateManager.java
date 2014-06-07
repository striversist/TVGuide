package com.tools.tvguide.managers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import android.content.Context;
import android.text.TextUtils;

import com.tools.tvguide.components.VersionController;
import com.tools.tvguide.managers.UpdateManager.IOCompleteCallback.CheckResult;

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
        void OnIOComplete(CheckResult result);
        enum CheckResult {Need_Update, No_Need_Update};
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
        store();
    }
    
    public String getGUID()
    {
        // TODO: 这是为了能在多进程中调用采用的临时方案，以后需要重构
        if (mGuid == null)
            load();
        return mGuid;
    }
    
    public boolean checkUpdate(final IOCompleteCallback callback)
    {
        if (mChecked)
        {
            if (mIsNeedUpdate)
                callback.OnIOComplete(CheckResult.Need_Update);
            else
                callback.OnIOComplete(CheckResult.No_Need_Update);
            return true;
        }
        
        new Thread()
        {
            public void run()
            {
                if (mVersionController.checkLatestVersion())
                {
                    mIsNeedUpdate = true;
                    callback.OnIOComplete(CheckResult.Need_Update);
                }
                else
                {
                    callback.OnIOComplete(CheckResult.No_Need_Update);
                }
                mChecked = true;
            }
        }.start();
        return false;
    }
    
    public String getCurrentVersionName()
    {
        return mVersionController.getCurrentVersionName();
    }
    
    public String getLatestVersionName()
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
    
    public String getAppChannelName()
    {
        return mVersionController.getAppChannelName();
    }
    
    private void load() {
        try {
            String guidInternalPath = mContext.getFilesDir() + File.separator + FILE_GUID;
            String guidExternalPath = null;
            String guidPath = guidInternalPath;
            if (mContext.getExternalFilesDir(null) != null) {
                guidExternalPath = mContext.getExternalFilesDir(null).getAbsolutePath() + File.separator + FILE_GUID;
            }
            if (TextUtils.isEmpty(guidExternalPath) || !new File(guidExternalPath).exists()) {
                guidPath = guidInternalPath;
            } else {
                guidPath = guidExternalPath;
            }
            FileReader reader = new FileReader(new File(guidPath));
            BufferedReader bufferedReader = new BufferedReader(reader);
            mGuid = bufferedReader.readLine();
            bufferedReader.close();
        } catch (FileNotFoundException e) {
            // e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void store() {
        try {
            String guidInternalPath = mContext.getFilesDir() + File.separator + FILE_GUID;
            String guidExternalPath = null;
            if (mContext.getExternalFilesDir(null) != null) {   // 优先存入sdcard
                guidExternalPath = mContext.getExternalFilesDir(null).getAbsolutePath() + File.separator + FILE_GUID;
            }
            // 写入内部存储
            if (!TextUtils.isEmpty(guidInternalPath)) {
                FileOutputStream fos = mContext.openFileOutput(FILE_GUID, Context.MODE_PRIVATE);
                fos.write(mGuid.getBytes());
                fos.close();
            }
            // 写入sdcard
            if (!TextUtils.isEmpty(guidExternalPath)) {
                File guidExternalFile = new File(guidExternalPath);
                if (!guidExternalFile.exists()) {
                    guidExternalFile.createNewFile();
                }
                FileOutputStream fos = new FileOutputStream(guidExternalFile);
                fos.write(mGuid.getBytes());
                fos.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
