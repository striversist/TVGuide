package com.tools.tvguide.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.tools.tvguide.R;
import com.tools.tvguide.components.UANetDataGetter;
import com.tools.tvguide.managers.AppEngine;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class Utility
{
	public static void emulateNetworkDelay()
	{
		try
		{
			Thread.sleep(300);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	public static boolean isNetworkAvailable()
	{
		Context context = MyApplication.getInstance().getApplicationContext();
		if(context == null)
		{
			Log.d("Utility::isNetworkAvailable", "context is null");
			return false;
		}
		ConnectivityManager connManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if(connManager == null)
		{
			return false;
		}
		NetworkInfo info = connManager.getActiveNetworkInfo();
		if(info == null)
		{
			Log.d("Utility::isNetworkAvailable", "info is null");
			return false;
		}
		return info.isAvailable();
	}
	
	public static boolean checkNetwork(Context context)
	{
		if(isNetworkAvailable())
		{
			return true;
		}
		new AlertDialog.Builder(context)
		.setIcon(R.drawable.application)
		.setMessage("Network is not available\nPlease check!")
		.setNeutralButton("OK", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
			}
		}).show();
		return false;
	}
	
	public static boolean isWifi(Context mContext) 
	{  
	    ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);  
	    NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();  
	    if (activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) 
	    {  
	        return true;
	    }
	    return false;  
	} 
	
	public static Bitmap getImage(Context context, String fileName)
    {
        Bitmap bitmap = null;
        try
        {
            InputStream is = context.getAssets().open(fileName);
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
        
        return bitmap;
    }
	
	public static boolean isIPAddress(String hostName) 
	{  
        if (hostName == null)
            return false;

        try {
            String[] nums = hostName.split("\\.");
            int len = nums.length;
            if(len != 4)
            {
                return false;
            }
            for (int i = 0; i < len; i++) 
            {
                int ipNum = Integer.parseInt(nums[i]);
                if(ipNum > 255 || ipNum < 0)
                {
                    return false;
                }
            }
            return true;
        } 
        catch (Exception e) 
        {
        }
        return false;
    }
	
	public static String trimChineseSpace(String origin)
	{
		if (origin == null)
			return null;
		if (origin.equals(""))
			return origin;
		
		final String space = "　";
		int start = 0;
		int end = 0;
		for (int i=0; i<origin.length(); ++i)
		{
			if (space.equals(String.valueOf(origin.charAt(i))))
				continue;
			start = i;
			break;
		}
		
		for (int i=origin.length()-1; i>0; --i)
		{
			if (space.equals(String.valueOf(origin.charAt(i))))
				continue;
			end = i;
			break;
		}
		
		return origin.substring(start, end + 1);
	}
	
	/*
     * Transfer the day to the server host day: Monday~Sunday -> 1~7
     */
    public static int getProxyDay(int day)
    {
        assert(day >=1 && day <=7);
        int hostDay = 0;
        switch (day)
        {
            case Calendar.MONDAY:
                hostDay = 1;
                break;
            case Calendar.TUESDAY:
                hostDay = 2;
                break;
            case Calendar.WEDNESDAY:
                hostDay = 3;
                break;
            case Calendar.THURSDAY:
                hostDay = 4;
                break;
            case Calendar.FRIDAY:
                hostDay = 5;
                break;
            case Calendar.SATURDAY:
                hostDay = 6;
                break;
            case Calendar.SUNDAY:
                hostDay = 7;
                break;
        }
        return hostDay;
    }
    
    public static long getMillisSinceWeekBegin()
    {
        int nowWeekday = getProxyDay(Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
        int nowHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int nowMinute = Calendar.getInstance().get(Calendar.MINUTE);
        int nowSecond = Calendar.getInstance().get(Calendar.SECOND);
        int nowMilliSecond = Calendar.getInstance().get(Calendar.MILLISECOND);
        
        long now = System.currentTimeMillis();
        long daysMs = (nowWeekday - 1) * 3600 * 24 * 1000;
        long leftMs = (nowHour * 3600 + nowMinute * 60 + nowSecond) * 1000 + nowMilliSecond;
        
        return (now - daysMs - leftMs);
    }
    
    /**
     * 比较时间，两者必须是：HH:mm的格式
     * @param time1
     * @param time2
     * @return 1(time1 > time2); 0(time1 == time2); -1(time1 < time2)
     */
    public static int compareTime(String time1, String time2)
    {
        assert(time1 != null && time2 != null);
        
        try 
        {
            SimpleDateFormat df = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
            Date date1 = df.parse(time1);
            Date date2 = df.parse(time2);
            
            if (date1.getTime() > date2.getTime())
                return 1;
            else if (date1.getTime() < date2.getTime())
                return -1;
            else
                return 0;
        } 
        catch (ParseException e) 
        {
            e.printStackTrace();
        }
        
        return 0;
    }
    
    public static Bitmap getNetworkImage(String url, CacheControl control)
    {
    	if (url == null)
    		return null;
    	
        Bitmap bitmap = null;
        try
        {
        	if (control == CacheControl.Memory) {
        		bitmap = AppEngine.getInstance().getCacheManager().getBitmap(guessFileNameByUrl(url));
        	} else if (control == CacheControl.Disk) {
        		bitmap = AppEngine.getInstance().getDiskCacheManager().getBitmap(guessFileNameByUrl(url));
        	}
        	if (bitmap == null)	{
        		bitmap = BitmapFactory.decodeStream(new UANetDataGetter(url).getInputStream());
        		if (bitmap != null) {
        			if (control == CacheControl.Memory) {
        				AppEngine.getInstance().getCacheManager().setBitmap(guessFileNameByUrl(url), bitmap);
        			} else if (control == CacheControl.Disk) {
	        			AppEngine.getInstance().getDiskCacheManager().setBitmap(guessFileNameByUrl(url), bitmap);
	        		}
        		}
        	}
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
        return bitmap;
    }
    
    public static String guessFileNameByUrl(String paramUrl)
    {
    	String result = "";
    	try 
    	{
			URL url = new URL(paramUrl);
			result = url.getPath().replace("/", "_");
		} 
    	catch (MalformedURLException e) 
    	{
			e.printStackTrace();
		}
    	return result;
    }
    
    public static String addUrlGetParam(String url, String paramName, String paramValue, boolean isFirstParam)
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
    
    public static boolean isUrlExist(String url)
    {
    	HttpURLConnection.setFollowRedirects(false);
    	try {
			HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
			con.setRequestMethod("HEAD");
			return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
		} catch (MalformedURLException e) {
//			e.printStackTrace();
			return false;
		} catch (IOException e) {
//			e.printStackTrace();
			return false;
		}
    }
    
    public static void deleteFile(File file)
    {
    	if (file == null)
    		return;
    	
    	if (!file.exists()) {
    		return;
    	}
    	
        if (file.isFile()) {
            file.delete();
        } else if (file.isDirectory()) {
            File files[] = file.listFiles();
            if (files != null) {
	            for (int i=0; i<files.length; i++) {
	                deleteFile(files[i]);
	            }
            }
            file.delete();
        }
    }
}
