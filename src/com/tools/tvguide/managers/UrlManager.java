package com.tools.tvguide.managers;

import android.content.Context;

public class UrlManager 
{
    public static final String TAG = "UrlManager";
    public static final String URL_CATEGORIES = "http://192.168.1.103/projects/TV/json/categories.php";
    public static final String URL_CHANNELS = "http://192.168.1.103/projects/TV/json/channels.php";
    public static final String URL_ON_PLAYING_PROGRAM = "http://192.168.1.103/projects/TV/json/onplaying_program.php";
    public static final String URL_ON_PLAYING_PROGRAMS = "http://192.168.1.103/projects/TV/json/onplaying_programs.php";
    public static final String URL_CHOOSE = "http://192.168.1.103/projects/TV/json/choose.php";
    public static final String URL_SEARCH = "http://192.168.1.103/projects/TV/json/search.php";
    
    private Context mContext;
    
    public UrlManager(Context context)
    {
        mContext = context;
    }
}
