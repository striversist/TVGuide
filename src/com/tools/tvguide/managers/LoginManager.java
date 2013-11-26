package com.tools.tvguide.managers;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URLEncoder;

import com.tools.tvguide.components.DefaultNetDataGetter;
import com.tools.tvguide.utils.NetDataGetter;

import android.content.Context;

public class LoginManager 
{
    // millisecond
    public static final int KEEP_ALIVE_INTERVAL = 15000;
    private Context mContext;
    private Thread  mKeepAliveThread;
    private boolean mDone;
    private long    mLastActive = System.currentTimeMillis();
    
    public LoginManager(Context context)
    {
        mContext = context;
        mDone = false;
    }
    
    public void shutDown()
    {
        mDone = true;
    }
    
    public void login()
    {
        new Thread()
        {
            @Override
            public void run()
            {
                try 
                {
                    String loginUrl = AppEngine.getInstance().getUrlManager().tryToGetDnsedUrl(UrlManager.URL_LOGIN);
                    loginUrl = addUrlGetParam(loginUrl, "UIP", AppEngine.getInstance().getDnsManager().getDeviceIpAddress(), true);
                    loginUrl = addUrlGetParam(loginUrl, "UL", AppEngine.getInstance().getDnsManager().getDeviceLocation(), false);
                    if (AppEngine.getInstance().getUpdateManager().getAppChannelName() != null)
                        loginUrl = addUrlGetParam(loginUrl, "APP_CHANNEL", AppEngine.getInstance().getUpdateManager().getAppChannelName(), false);

                    NetDataGetter getter = new DefaultNetDataGetter(loginUrl);
                    getter.setHeader("UA", AppEngine.getInstance().getBootManager().getUserAgent());
                    getter.getStringData();     // Just send the request
                    if (AppEngine.getInstance().getUpdateManager().getGUID() == null)   // First use
                    {
                        String guid = getter.getFirstHeader("GUID");
                        if (guid != null)
                        {
                            AppEngine.getInstance().getUpdateManager().setGUID(guid);
                        }
                    }
                }
                catch (MalformedURLException e) 
                {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private String addUrlGetParam(String url, String paramName, String paramValue, boolean isFirstParam)
    {
        if (url == null || paramName == null)
            return url;
     
        String newUrl = url;
        if (paramValue == null)
            paramValue = "";
        
        if (isFirstParam)
            newUrl += "?";
        else
            newUrl += "&";
        
        try 
        {
            newUrl += paramName + "=" + URLEncoder.encode(paramValue, "UTF-8");
        } 
        catch (UnsupportedEncodingException e) 
        {
            newUrl = url;
            e.printStackTrace();
        }
        
        return newUrl;
    }
    
    public void startKeepAliveProcess()
    {
        int keepAliveInterval = KEEP_ALIVE_INTERVAL;
        if (keepAliveInterval > 0)
        {
            if (mKeepAliveThread == null || !mKeepAliveThread.isAlive())
            {
                KeepAliveTask task = new KeepAliveTask(KEEP_ALIVE_INTERVAL);
                mKeepAliveThread = new Thread(task);
                task.setThread(mKeepAliveThread);
                mKeepAliveThread.setDaemon(true);
                mKeepAliveThread.setName("Keep Alive");
                mKeepAliveThread.start();
            }
        }
    }
    
    /**
     * 定时发送心跳包，维持热链路
     */
    private class KeepAliveTask implements Runnable
    {

        private int     mDelay;
        private Thread  mThread;
        private Socket  mSocket;
        private OutputStream mOutputStream;

        public KeepAliveTask(int delay)
        {
            this.mDelay = delay;
            mSocket = new Socket();
        }

        protected void setThread(Thread thread)
        {
            this.mThread = thread;
        }

        public void run()
        {
            try
            {
                Thread.sleep(1500);
                mSocket.connect(new InetSocketAddress("www.bing.com", 80));
                mSocket.setSoTimeout(mDelay);
                mOutputStream = mSocket.getOutputStream();
            }
            catch (InterruptedException ie)
            {
                // Do nothing
            } 
            catch (IOException e) 
            {
                // Do nothing
                return;
            }
            while (!mDone && mKeepAliveThread == mThread)
            {
                if (System.currentTimeMillis() - mLastActive >= mDelay)
                {
                    try 
                    {
                        mOutputStream.write(" ".getBytes());
                        mOutputStream.flush();
                    } 
                    catch (IOException e) 
                    {
                        // Do nothing
                    }
                    mLastActive = System.currentTimeMillis();
                }
                try
                {
                    Thread.sleep(mDelay);
                }
                catch (InterruptedException ie)
                {
                    // Do nothing
                }
            }
        }
    }
}
