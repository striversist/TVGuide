package com.tools.tvguide.components;

import android.util.Log;

public class NativeFileObserver
{
	private static final String TAG = "NativeFileObserver";
	private String mPath;
	
	public NativeFileObserver(String path) 
	{
		mPath = path;
	}
	
	public void startWatching()
	{
        nativeStartWatching(mPath);
	}
	
	public void stopWatching()
	{
        nativeStopWatching();
	}
	
	public void setHttpRequestOnDelete(String url, String guid, String version)
	{
        nativeSetOnDeleteRequestInfo(url, guid, version);
	}

	private native void nativeStartWatching(String path);
	private native void nativeStopWatching();
	private native void nativeSetOnDeleteRequestInfo(String url, String guid, String version);
}
