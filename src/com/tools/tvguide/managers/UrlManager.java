package com.tools.tvguide.managers;

import java.net.UnknownHostException;

import com.tools.tvguide.utils.NetworkManager;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

public class UrlManager 
{
    public static final String TAG                      = "UrlManager";
    public static final String URL_CHINAZ_IP            = "http://ip.chinaz.com";
    
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
        
    private static final boolean ENABLE_TEST            = true;
    private String  mHostName                           = "striversist.oicp.net";
    private String  mHostIP;
    private String  BASE_PATH;
    private int     PORT;
    
    private static final String PATH_CATEGORIES           = "/json/categories.php";
    private static final String PATH_CHANNELS             = "/json/channels.php";
    private static final String PATH_ON_PLAYING_PROGRAM   = "/json/onplaying_program.php";
    private static final String PATH_ON_PLAYING_PROGRAMS  = "/json/onplaying_programs.php";
    private static final String PATH_CHOOSE               = "/json/choose.php";
    private static final String PATH_SEARCH               = "/json/search.php";
    private static final String PATH_HOT                  = "/json/hot.php";
    private static final String PATH_UPDATE               = "/update/update.php";
    private static final String PATH_FEEDBACK             = "/feedback.php";
    private static final String PATH_LOGIN                = "/login.php";
    private static final String PATH_REPORT               = "/report.php";
    
    private Context mContext;
    
    private Handler initHandler;
    public interface OnInitCompleteCallback
    {
        void OnInitComplete(int result);
    }
    
    public UrlManager(Context context)
    {
        mContext = context;
        initHandler = new Handler();
        if (ENABLE_TEST)
        {
            mHostIP = "192.168.1.102";
            BASE_PATH = "/projects/TV";
            PORT = 80;
        }
        else
        {
            mHostIP = null;
            BASE_PATH = "/TV";
            PORT = 9999;
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
}
