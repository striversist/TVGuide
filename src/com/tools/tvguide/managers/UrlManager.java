package com.tools.tvguide.managers;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.acra.ACRA;

import android.content.Context;

public class UrlManager 
{
    public static final String TAG                      = "UrlManager";
    public static final String URL_CHINAZ_IP            = "http://ip.chinaz.com";
    public static final String URL_IPCN                 = "http://www.ip.cn";
    public static final String URL_PUB_HOT              = "http://m.tvsou.com/juqing.asp";
    public static final String URL_HOT_DRAMA            = "http://m.tvmao.com/hot/drama/default/week";
    public static final String URL_HOT_TVCOLUMN         = "http://m.tvmao.com/hot/tvcolumn/default/week";
    public static final String URL_HOT_MOVIE            = "http://m.tvmao.com/hot/movie/default/week";
    public static final String DEV_IP                   = "192.168.1.100";
    public static final String REAL_HOST                = "striversist.oicp.net";
    
    public static final String ACRA_PROXY_REAL          = "http://" + REAL_HOST + ":59840/acra-tvguide/_design/acra-storage/_update/report";
    public static final String ACRA_PROXY_DEV           = "http://" + DEV_IP + ":5984/acra-tvguide/_design/acra-storage/_update/report";
    
    public enum ProxyUrl {Categories, Channels, OnPlayingProgram, OnPlayingPrograms, Choose, Search,
    					  Hot, Update, Feedback, Login, Report, Query, Logout}
        
    private static final boolean ENABLE_TEST            = EnvironmentManager.isDevelopMode;
    private String  mProxyHostName                      = REAL_HOST;
    private String  mProxyHostIP;
    private String  BASE_PATH;
    private int     PORT;
    
    private static final String PATH_CATEGORIES           = "/public/json/categories.php";
    private static final String PATH_CHANNELS             = "/public/json/channels.php";
    private static final String PATH_ON_PLAYING_PROGRAM   = "/public/json/onplaying_program.php";
    private static final String PATH_ON_PLAYING_PROGRAMS  = "/public/json/onplaying_programs.php";
    private static final String PATH_CHOOSE               = "/public/json/programs.php";
    private static final String PATH_SEARCH               = "/public/json/search.php";
    private static final String PATH_HOT                  = "/public/json/hot.php";
    private static final String PATH_QUERY                = "/public/json/query.php";
    
    private static final String PATH_UPDATE               = "/public/update/update.php";
    private static final String PATH_FEEDBACK             = "/public/feedback.php";
    private static final String PATH_LOGIN                = "/public/login.php";
    private static final String PATH_REPORT               = "/public/report.php";
    private static final String PATH_LOGOUT               = "/public/logout.php";
    
    private static HashMap<String, String> sChannelLogoUrls = new HashMap<String, String>();
    private Context mContext;
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
            BASE_PATH = "/projects/TV";
            PORT = 80;
        }
        else
        {
            BASE_PATH = "/projects/TV";
            PORT = 52719;
        }
    }
    
    public void init(final OnInitCompleteCallback callback)
    {
        if (mProxyHostIP != null)
        {
            if (callback != null)
            {
                callback.OnInitComplete(0);
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
                    if (ENABLE_TEST)
                    {
                        mProxyHostIP = DEV_IP;
                        AppEngine.getInstance().getDnsManager().getProxyIPAddress(mProxyHostName);
                    }
                    else
                    {
                        mProxyHostIP = AppEngine.getInstance().getDnsManager().getProxyIPAddress(mProxyHostName);
                    }
                    
                    if (EnvironmentManager.enableACRA)
                    	ACRA.getConfig().setFormUri(tryToReplaceHostNameWithIP(ACRA.getConfig().formUri()));
                    
                    mLock.lock();
                    mCondition.signalAll();
                    mLock.unlock();
                    mHasInit = true;
                    if (callback != null)
                    {
                        callback.OnInitComplete(0);
                    }
                    AppEngine.getInstance().getDnsManager().preloadFrequentlyUsedDns();
                } 
                catch (UnknownHostException e) 
                {
                }
            }
        }.start();
    }
    
    public String tryToGetDnsedUrl(ProxyUrl type)
    {
        if (mProxyHostIP != null || mHasInit)
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
    
    public String getUrl(ProxyUrl type)
    {
        String url = "http://";
        if (ENABLE_TEST)
            url += DEV_IP;
        else if (mProxyHostIP != null)
            url += mProxyHostIP;
        else
            url += mProxyHostName;
        
        url += ":" + PORT;
        url += BASE_PATH;
        switch (type)
        {
            case Categories:
                url += PATH_CATEGORIES;
                break;
            case Channels:
                url += PATH_CHANNELS;
                break;
            case OnPlayingProgram:
                url += PATH_ON_PLAYING_PROGRAM;
                break;
            case OnPlayingPrograms:
                url += PATH_ON_PLAYING_PROGRAMS;
                break;
            case Choose:
                url += PATH_CHOOSE;
                break;
            case Search:
                url += PATH_SEARCH;
                break;
            case Hot:
                url = URL_PUB_HOT;
                break;
            case Update:
                url += PATH_UPDATE;
                break;
            case Feedback:
                url += PATH_FEEDBACK;
                break;
            case Login:
                url += PATH_LOGIN;
                break;
            case Report:
                url += PATH_REPORT;
                break;
            case Query:
                url += PATH_QUERY;
                break;
            case Logout:
                url += PATH_LOGOUT;
            default:
                assert false: "Not reach here";
                break;
        }
        return url;
    }
    
    public String getHost()
    {
        if (mProxyHostIP != null)
            return mProxyHostIP;
        return mProxyHostName;
    }
    
    public int getPort()
    {
        return PORT;
    }
    
    public static String getWebChannelUrl(String tvmaoId, int day)
    {        
        // eg. http://www.tvmao.com/program/CCTV-CCTV1-w6.html
        String prefix = "http://www.tvmao.com/program/";
        String sufix = ".html";
        return prefix + tvmaoId + "-w" + String.valueOf(day) + sufix;
    }
    
    public static String getSimpleWebChannelUrl(String tvmaoId, int day)
    {        
        // eg. http://m.tvmao.com/program/CCTV-CCTV1-w6.html
        String prefix = "http://m.tvmao.com/program/";
        String sufix = ".html";
        return prefix + tvmaoId + "-w" + String.valueOf(day) + sufix;
    }
    
    public static void setWebChannelLogoUrl(String tvmaoId, String url)
    {
        if (tvmaoId == null || url == null)
            return;
        
        sChannelLogoUrls.put(tvmaoId, url);
    }
    
    public static String[] guessWebChannelLogoUrls(String tvmaoId)
    {
    	if (tvmaoId == null)
    		return null;
    	
    	// eg. http://static.tvmao.cn/channel/logo/CCTV1.jpg
    	// eg. http://static.tvmao.cn/tvstation/logo/AHTV.gif
    	String format = "http://static.tvmao.cn/%1$s/logo/%2$s.%3$s";
    	String type = "";
    	String id = "";
    	String sufix = "";
    	
    	String idPart1 = "";
    	String idPart2 = "";
    	if (tvmaoId.contains("-")) {
    		idPart1 = tvmaoId.split("-", 2)[0];
    		idPart2 = tvmaoId.split("-", 2)[1];
    	} else {
    		idPart1 = tvmaoId;
    		idPart2 = tvmaoId;
    	}
    	
        List<String> urlList = new ArrayList<String>();
    	
    	if (sChannelLogoUrls.containsKey(tvmaoId))
    	{
    	    urlList.add(sChannelLogoUrls.get(tvmaoId)); 
    	}
    	
    	{
    		type = "channel";
    		id = idPart2;
    		sufix = "jpg";
    		urlList.add(String.format(format, type, id, sufix));
    	} 
    	
    	{
    		type = "tvstation";
    		id = idPart1;
    		sufix = "gif";
    		urlList.add(String.format(format, type, id, sufix));
    	}
    	
    	return urlList.toArray(new String[0]);
    }
    
    public static String tryToReplaceHostNameWithIP(String url)
    {
        if (url == null)
            return null;
        
        try 
        {
            String hostName = new URL(url).getHost();
            String ipAddress = AppEngine.getInstance().getDnsManager().getIpAddress(hostName);
            if (ENABLE_TEST)
                ipAddress = DEV_IP;
            if (ipAddress != null)
                return url.replace(hostName, ipAddress);
        } 
        catch (MalformedURLException e) 
        {
        } 
        catch (UnknownHostException e) 
        {
        }
        
        return url;
    }
}
