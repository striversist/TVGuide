package com.tools.tvguide.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class NetDataGetter
{
	URL mUrl = null;
	//ProgressListener mProgressListener = null;
	List<BasicNameValuePair> mPairs = null;
	//String mRecvDataAsync = null;
	private int mConnectionTimeout = 6000;
	private int mSocketTimeout = 10000;
	
	public NetDataGetter()
	{
	}
	
	public NetDataGetter(String url) throws MalformedURLException
	{
		mUrl = new URL(url);
	}
	
	public int setUrl(String url)
	{
		try
		{
			mUrl = new URL(url);
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
			return -1;
		}
		
		return 0;
	}
	
	public int setConnectionTimeout(int timeout)
	{
		Log.d("NetDataGetter::setConnectionTimeout", "timeout = " + timeout);
		if(timeout <= 0)
		{
			Log.d("NetDataGetter::setConnectionTimeout", "timeout <= 0");
			return -1;
		}
		mConnectionTimeout = timeout;
		return 0;
	}
	
	public int setSocketTimeout(int timeout)
	{
		Log.d("NetDataGetter::setSocketTimeout", "timeout = " + timeout);
		if(timeout <= 0)
		{
			Log.d("NetDataGetter::setSocketTimeout", "timeout <= 0");
			return -1;
		}
		mSocketTimeout = timeout;
		return 0;
	}
	
	public InputStream getInputStream()
	{
		if(mUrl == null)
		{
			return null;
		}
		
		InputStream input = null;
		try
		{
			input =  mUrl.openStream();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
		
		Utility.emulateNetworkDelay();
		
		return input;
	}
	
	public String getStringData()
	{
		return getStringData(null);
	}
	
	public String getStringData(List<BasicNameValuePair> pairs)
	{
		if(mUrl == null)
		{
			return null;
		}
		if(Utility.isNetworkAvailable() == false)
		{
			return null;
		}

		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, mConnectionTimeout);
		HttpConnectionParams.setSoTimeout(httpParameters, mSocketTimeout);
		
		HttpClient client = new DefaultHttpClient(httpParameters);
    	HttpPost post = new HttpPost(mUrl.toString());
    	HttpResponse response = null;
    	HttpEntity entity = null;
    	String recvData = null;
    	
        try
		{
        	if(pairs != null)
        	{
        		post.setEntity(new UrlEncodedFormEntity(pairs, HTTP.UTF_8));
        	}
        	
        	response = client.execute(post);
        	if(response == null)
        	{
        		Log.e("Error", "Response Null");
				return null;
        	}
        	if(response.getStatusLine().getStatusCode() != 200)
        	{
        		Log.e("Error", "Status code = %d" + response.getStatusLine().getStatusCode());
        		return null;
        	}
        	
        	entity = response.getEntity();
        	if(entity == null)
        	{
        		Log.e("Error", "Status code = %d" + response.getStatusLine().getStatusCode());
        		return null;
        	}
        	
        	recvData = EntityUtils.toString(entity);
			if(recvData == null)
			{
				Log.e("Error", "Receive Null");
				return null;
			}
		}
		catch (UnsupportedEncodingException e1)
		{
			Log.e("Exception", "Set name pair failed!");
			e1.printStackTrace();
			return null;
		}
		catch (ClientProtocolException e)
		{
			Log.e("Exception", "Exec post protocol failed!");
			e.printStackTrace();
			return null;
		}
		catch (IOException e)
		{
			Log.e("Exception", "Exec post IO failed!");
			e.printStackTrace();
			return null;
		}
		catch (ParseException e)
		{
			Log.e("Error", "Entity parse failed");
			e.printStackTrace();
			return null;
		}

//        Utility.emulateNetworkDelay();
        
        return recvData;
	}
	
	public JSONObject getJSONsObject()
	{
		return getJSONsObject(null);
	}
	
	public JSONObject getJSONsObject(List<BasicNameValuePair> pairs)
	{
		JSONObject jsonObject = null;
		String recvData = getStringData(pairs);
		if(recvData == null)
		{
			Log.e("getJSONsData", "recvData is null");
			return null;
		}
		
    	try
		{
			jsonObject = new JSONObject(recvData);
		}
		catch (JSONException e)
		{
			Log.e("NetDataGetter::getJSONsData", "recvData = " + recvData);
			Log.e("getJSONsData", "create JSON failed " + e.toString());
			e.printStackTrace();
			
			return null;
		}
    	
    	return jsonObject;
	}
	
	/*	
	public void getStringDataAsync(Object userData)
	{
		new Thread()
		{
			@Override
			public void run()
			{
				mRecvDataAsync = getStringData();
			}
		}.start();
		if(mProgressListener != null)
		{
			mProgressListener.notifyStatus(this, ProgressListener.Progress.FINISHED, mRecvDataAsync, userData);
		}
	}
	
	public void getStringDataAsync(List<BasicNameValuePair> pairs, Object userData)
	{
		mPairs = pairs;
		new Thread()
		{
			@Override
			public void run()
			{
				mRecvDataAsync = getStringData(mPairs);
			}
		}.start();
		if(mProgressListener != null)
		{
			mProgressListener.notifyStatus(this, ProgressListener.Progress.FINISHED, mRecvDataAsync, userData);		
		}
	}
	
	public void setProgressListener(ProgressListener listener)
	{
		mProgressListener = listener;
	}
	
	public interface ProgressListener
	{
		public enum Progress
		{
			STARTING,
			FINISHED,
		}
		public void notifyStatus(NetDataGetter getter, ProgressListener.Progress status, String recvData, Object userData);
	}
	*/
}
