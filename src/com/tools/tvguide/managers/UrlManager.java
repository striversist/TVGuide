package com.tools.tvguide.managers;

import android.content.Context;

public class UrlManager 
{
    public static final String TAG                      = "UrlManager";
    public static final String HOST                     = "striversist.oicp.net";
    public static final String BASE_PATH                = "/TV";
    public static final int    PORT                     = 80;
    public static final String URL_CATEGORIES           = "http://" + HOST + BASE_PATH + "/json/categories.php";
    public static final String URL_CHANNELS             = "http://" + HOST + BASE_PATH + "/json/channels.php";
    public static final String URL_ON_PLAYING_PROGRAM   = "http://" + HOST + BASE_PATH + "/json/onplaying_program.php";
    public static final String URL_ON_PLAYING_PROGRAMS  = "http://" + HOST + BASE_PATH + "/json/onplaying_programs.php";
    public static final String URL_CHOOSE               = "http://" + HOST + BASE_PATH + "/json/choose.php";
    public static final String URL_SEARCH               = "http://" + HOST + BASE_PATH + "/json/search.php";
    public static final String URL_HOT                  = "http://" + HOST + BASE_PATH + "/json/hot.php";
    
    private Context mContext;
    
    public UrlManager(Context context)
    {
        mContext = context;
    }
}
