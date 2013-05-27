package com.tools.tvguide.managers;

import android.content.Context;

public class UrlManager 
{
    public static final String TAG                      = "UrlManager";
    private static final String HOST                    = "192.168.1.101";
    public static final String URL_CATEGORIES           = "http://" + HOST + "/projects/TV/json/categories.php";
    public static final String URL_CHANNELS             = "http://" + HOST + "/projects/TV/json/channels.php";
    public static final String URL_ON_PLAYING_PROGRAM   = "http://" + HOST + "/projects/TV/json/onplaying_program.php";
    public static final String URL_ON_PLAYING_PROGRAMS  = "http://" + HOST + "/projects/TV/json/onplaying_programs.php";
    public static final String URL_CHOOSE               = "http://" + HOST + "/projects/TV/json/choose.php";
    public static final String URL_SEARCH               = "http://" + HOST + "/projects/TV/json/search.php";
    
    private Context mContext;
    
    public UrlManager(Context context)
    {
        mContext = context;
    }
}
