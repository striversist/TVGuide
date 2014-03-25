package com.tools.tvguide.uninstall;

public class NativeFileObserver
{
    private static final String TAG = "NativeFileObserver";
    private String mPath;
    private int mHandler;
    
    public NativeFileObserver(String path) 
    {
        mPath = path;
    	mHandler = nativeCreateHandler();
    }
    
    public void startWatching()
    {
        nativeStartWatching(mHandler, mPath);
    }
    
    public void stopWatching()
    {
        nativeStopWatching(mHandler);
    }
	public void setHttpRequestOnDelete(String url)
	{
        nativeSetOnDeleteRequestInfo(mHandler, url);
	}

	private native int nativeCreateHandler();
	private native void nativeStartWatching(int handler, String path);
	private native void nativeStopWatching(int handler);
	private native void nativeSetOnDeleteRequestInfo(int handler, String url);
}
