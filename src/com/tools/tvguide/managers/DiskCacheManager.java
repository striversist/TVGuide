package com.tools.tvguide.managers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.jakewharton.disklrucache.DiskLruCache;
import com.tools.tvguide.components.Shutter;
import com.tools.tvguide.data.TimestampString;
import com.tools.tvguide.utils.Utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;

public class DiskCacheManager implements Shutter, Callback 
{
    private static final int DISK_CACHE_VERSION = 1;
    private static final int DISK_CACHE_MAX_SIZE = 20 * 1024 * 1024;    // 20MB
    private Context mContext;
    private DiskLruCache mDiskLruCache;
    private String mExternalCachePath;
    private HandlerThread mHandlerThread;
    private Handler	mHandler;
    private enum SelfMessage { Store_Bitmap, Clear_All };
    
    public DiskCacheManager(Context context)
    {
        assert (context != null);
        mContext = context;
        
        openDiskCache();
        if (mContext.getExternalCacheDir() != null) {
            mExternalCachePath = mContext.getExternalCacheDir().getAbsolutePath();
            File cacheDir = new File(mExternalCachePath);
            if (!cacheDir.exists())
                cacheDir.mkdirs();
        } else {
            mExternalCachePath = mContext.getCacheDir().getAbsolutePath();      // 若没有外存，就使用手机cache替代
        }
        
    	mHandlerThread = new HandlerThread("DiskCacheThread");
    	mHandlerThread.start();
    	mHandler = new Handler(mHandlerThread.getLooper(), this);
    }
    
    public void clearAll()
    {
    	mHandler.removeMessages(SelfMessage.Store_Bitmap.ordinal());
    	mHandler.obtainMessage(SelfMessage.Clear_All.ordinal()).sendToTarget();
        
        try {
        	if (mDiskLruCache == null)
                return;
        	if (mDiskLruCache.isClosed()) {    // delete不能在closed状态下进行
        	    openDiskCache();
        	}
            mDiskLruCache.delete();
            mDiskLruCache = null;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
        }
    }
    
    public synchronized String getString(String key)
    {
    	if (mDiskLruCache == null && !openDiskCache())
    		return null;
        
        String result = null;
        try 
        {
            DiskLruCache.Editor editor = mDiskLruCache.edit(String.valueOf(key.hashCode()));
            if (editor != null)
            {
                result = editor.getString(0);
                editor.abort();
            }
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
        
        return result;
    }
    
    public synchronized boolean setString(String key, String value)
    {
    	if (mDiskLruCache == null && !openDiskCache())
			return false;

        if (key == null || value == null)
            return false;
        
        boolean result = false;
        try 
        {
            DiskLruCache.Editor editor = mDiskLruCache.edit(String.valueOf(key.hashCode()));
            if (editor != null)
            {
                editor.set(0, value);
                editor.set(1, "");
                editor.commit();
                result = true;
            }
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
        
        return result;
    }
    
    public synchronized boolean setTimestampString(String key, TimestampString timeStampString) {
    	if (mDiskLruCache == null && !openDiskCache())
			return false;

        if (key == null || timeStampString == null)
            return false;
        
        boolean result = false;
        try 
        {
            DiskLruCache.Editor editor = mDiskLruCache.edit(String.valueOf(key.hashCode()));
            if (editor != null)
            {
                editor.set(0, timeStampString.getString());
                editor.set(1, String.valueOf(timeStampString.getDate()));
                editor.commit();
                result = true;
            }
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
        
    	return result;
    }
    
    public synchronized TimestampString getTimestampString(String key) {
    	if (mDiskLruCache == null && !openDiskCache())
    		return null;
        
        TimestampString timeStampString = new TimestampString();
        try 
        {
            DiskLruCache.Editor editor = mDiskLruCache.edit(String.valueOf(key.hashCode()));
            if (editor != null)
            {
            	timeStampString.setString(editor.getString(0));
            	long date = 0;
            	if (editor.getString(1) != null) {
            	    date = Long.valueOf(editor.getString(1));
            	}
            	timeStampString.setDate(date);
                editor.abort();
            }
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
        return timeStampString;
    }
    
    public Bitmap getBitmap(String fileName)
    {
    	if (fileName == null)
    		return null;
    	
    	if (mExternalCachePath == null)
    	    return null;
    	
    	return BitmapFactory.decodeFile(mExternalCachePath + File.separator + fileName);
    }
    
    public void setBitmap(String fileName, Bitmap bitmap)
    {
    	if (fileName == null || bitmap == null)
    		return;
    	
    	ExternalBitmap externalBitmap = new ExternalBitmap();
    	externalBitmap.fileName = fileName;
    	externalBitmap.bitmap = bitmap;
    	mHandler.obtainMessage(SelfMessage.Store_Bitmap.ordinal(), externalBitmap).sendToTarget();
    }
    
    private void storeBitmap(String fileName, Bitmap bitmap)
    {
    	if (fileName == null || bitmap == null)
    		return;
    	
    	if (mExternalCachePath == null)
    	    return;
    	
    	File f = new File(mExternalCachePath + File.separator + fileName);
		if (f.exists()) {
		   f.delete();
		}
		try {
		   FileOutputStream out = new FileOutputStream(f);
		   bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
		   out.flush();
		   out.close();
		} catch (FileNotFoundException e) {
		   e.printStackTrace();
		} catch (IOException e) {
		   e.printStackTrace();
		}
    }

    @Override
    public void onShutDown() 
    {
        if (mDiskLruCache != null)
        {
            try {
                mDiskLruCache.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        if (mHandlerThread != null)
        	mHandlerThread.getLooper().quit();
    }
    
    private boolean openDiskCache()
    {
    	if (mDiskLruCache != null)
    		return true;
    	
    	try 
        {
            mDiskLruCache = DiskLruCache.open(mContext.getCacheDir(), DISK_CACHE_VERSION, 2, DISK_CACHE_MAX_SIZE);
            if (mDiskLruCache != null)
            	return true;
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    	return false;
    }

	@Override
	public boolean handleMessage(Message msg) 
	{
		SelfMessage selfMsg = SelfMessage.values()[msg.what];
		switch (selfMsg)
		{
			case Store_Bitmap:
				ExternalBitmap externalBitmap = (ExternalBitmap) msg.obj;
				storeBitmap(externalBitmap.fileName, externalBitmap.bitmap);
				break;
			case Clear_All:
			    if (mExternalCachePath != null) {
			        Utility.deleteFile(new File(mExternalCachePath));
			    }
				break;
		}
				
		return true;
	}
	
	private static class ExternalBitmap
	{
		String fileName;
		Bitmap bitmap;
	}
}
