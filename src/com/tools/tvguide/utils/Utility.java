package com.tools.tvguide.utils;

import java.io.IOException;
import java.io.InputStream;

import com.tools.tvguide.R;

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
}
