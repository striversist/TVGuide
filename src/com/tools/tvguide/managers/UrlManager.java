package com.tools.tvguide.managers;

import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import android.content.Context;
import android.os.Handler;

public class UrlManager 
{
    public static final String TAG                      = "UrlManager";
    public static final String URL_CHINAZ_IP            = "http://ip.chinaz.com";
    public static final String URL_IPCN                 = "http://www.ip.cn";
    
    public static final int URL_CATEGORIES              = 1;
    public static final int URL_CHANNELS                = 2;
    public static final int URL_ON_PLAYING_PROGRAM      = 3;
    public static final int URL_ON_PLAYING_PROGRAMS     = 4;
    public static final int URL_CHOOSE                  = 5;
    public static final int URL_SEARCH                  = 6;
    public static final int URL_HOT                     = 7;
    public static final int URL_UPDATE                  = 8;
    public static final int URL_FEEDBACK                = 9;
    public static final int URL_LOGIN                   = 10;
    public static final int URL_REPORT                  = 11;
        
    private static final boolean ENABLE_TEST            = false;
    private String  mHostName                           = "striversist.oicp.net";
    private String  mHostIP;
    private String  BASE_PATH;
    private int     PORT;
    
    private static final String PATH_CATEGORIES           = "/public/json/categories.php";
    private static final String PATH_CHANNELS             = "/public/json/channels.php";
    private static final String PATH_ON_PLAYING_PROGRAM   = "/public/json/onplaying_program.php";
    private static final String PATH_ON_PLAYING_PROGRAMS  = "/public/json/onplaying_programs.php";
    private static final String PATH_CHOOSE               = "/public/json/choose.php";
    private static final String PATH_SEARCH               = "/public/json/search.php";
    private static final String PATH_HOT                  = "/public/json/hot.php";
    private static final String PATH_UPDATE               = "/public/update/update.php";
    private static final String PATH_FEEDBACK             = "/public/feedback.php";
    private static final String PATH_LOGIN                = "/public/login.php";
    private static final String PATH_REPORT               = "/public/report.php";
    
    private Context mContext;
    private Handler initHandler                           = new Handler();
    private ReentrantLock mLock                           = new ReentrantLock();
    private Condition mCondition;
    private boolean mHasInit                              = false;
    public interface OnInitCompleteCallback
    {
        void OnInitComplete(int result);
    }
    
    public UrlManager(Context context)
    {
        mContext = context;
        mCondition = mLock.newCondition();
        
        if (ENABLE_TEST)
        {
            mHostIP = "192.168.1.102";
            BASE_PATH = "/projects/TV";
            PORT = 80;
        }
        else
        {
            mHostIP = null;
            BASE_PATH = "/projects/TV";
            PORT = 52719;
        }
    }
    
    public void init(final OnInitCompleteCallback callback)
    {
        if (mHostIP != null)
        {
            if (callback != null)
            {
                initHandler.post(new Runnable() 
                {
                    @Override
                    public void run() 
                    {
                        callback.OnInitComplete(0);
                    }
                });
            }
            mHasInit = true;
            return;
        }
        
        new Thread() 
        {        
            @Override
            public void run() 
            {
                try 
                {
                    mHostIP = AppEngine.getInstance().getDnsManager().getIPAddress(mHostName);
                    mLock.lock();
                    mCondition.signalAll();
                    mLock.unlock();
                    mHasInit = true;
                    if (callback != null)
                    {
                        initHandler.post(new Runnable() 
                        {
                            @Override
                            public void run() 
                            {
                                callback.OnInitComplete(0);
                            }
                        });
                    }
                } 
                catch (UnknownHostException e) 
                {
                }
            }
        }.start();
    }
    
    public String tryToGetDnsedUrl(int type)
    {
        if (mHostIP != null || mHasInit)
            return getUrl(type);
        
        // 等待HostIP解析完毕
        try 
        {
            mLock.lock();
            mCondition.await(20000, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException e) 
        {
            e.printStackTrace();
        }
        finally
        {
            mLock.unlock();
        }
        return getUrl(type);
    }
    
    public String getUrl(int type)
    {
        String url = "http://";
        if (mHostIP != null)
            url += mHostIP;
        else
            url += mHostName;
        
        url += ":" + PORT;
        url += BASE_PATH;
        switch (type)
        {
            case URL_CATEGORIES:
                url += PATH_CATEGORIES;
                break;
            case URL_CHANNELS:
                url += PATH_CHANNELS;
                break;
            case URL_ON_PLAYING_PROGRAM:
                url += PATH_ON_PLAYING_PROGRAM;
                break;
            case URL_ON_PLAYING_PROGRAMS:
                url += PATH_ON_PLAYING_PROGRAMS;
                break;
            case URL_CHOOSE:
                url += PATH_CHOOSE;
                break;
            case URL_SEARCH:
                url += PATH_SEARCH;
                break;
            case URL_HOT:
                url += PATH_HOT;
                break;
            case URL_UPDATE:
                url += PATH_UPDATE;
                break;
            case URL_FEEDBACK:
                url += PATH_FEEDBACK;
                break;
            case URL_LOGIN:
                url += PATH_LOGIN;
                break;
            case URL_REPORT:
                url += PATH_REPORT;
                break;
            default:
                assert false: "Not reach here";
                break;
        }
        return url;
    }
    
    public String getHost()
    {
        if (mHostIP != null)
            return mHostIP;
        return mHostName;
    }
    
    public int getPort()
    {
        return PORT;
    }
    
    public String tryToReplaceHostNameToIP(String url)
    {
        if (url == null)
            return null;
        if (mHostIP == null)
            return url;
        return url.replace(mHostName, mHostIP);
    }
}
