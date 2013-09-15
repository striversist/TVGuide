package com.tools.tvguide.managers;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;

import com.tools.tvguide.components.DefaultNetDataGetter;
import com.tools.tvguide.utils.NetDataGetter;

import android.content.Context;
import android.webkit.WebSettings;
import android.webkit.WebView;

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
        final String ua = getUserAgent();
        new Thread()
        {
            @Override
            public void run()
            {
                try 
                {
                    NetDataGetter getter = new DefaultNetDataGetter(AppEngine.getInstance().getUrlManager().getUrl(UrlManager.URL_LOGIN));
                    getter.setHeader("UA", ua);
                    getter.getStringData();
                } 
                catch (MalformedURLException e) 
                {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    // This API should be called in UI main thread
    private String getUserAgent() 
    {
        WebView webView = new WebView(mContext);
        webView.layout(0, 0, 0, 0);
        WebSettings settings = webView.getSettings();
        return settings.getUserAgentString();
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
                UrlManager urlManager = AppEngine.getInstance().getUrlManager();
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
