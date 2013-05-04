package com.tools.tvguide.utils;

import com.tools.tvguide.activities.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
		.setIcon(R.drawable.ic_launcher)
		.setMessage("Network is not available\nPlease check!")
		.setNeutralButton("OK", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
			}
		}).show();
		return false;
	}
}
