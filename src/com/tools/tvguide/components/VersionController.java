package com.tools.tvguide.components;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Locale;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.tools.tvguide.managers.AppEngine;
import com.tools.tvguide.managers.UrlManager;
import com.tools.tvguide.utils.MyApplication;
import com.tools.tvguide.utils.NetDataGetter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;

public class VersionController
{
	private Context mContext;
	private int mCurrentVersionCode;
	private int mLatestVersionCode;
	private String mCurrentVersionName;
	private String mLatestVersionName;
	private String mUrl = null;
	private int mLatestChannelVersion = 1;
	private String mAppChannelName;
	
	public VersionController(Context context)
	{
	    mContext = context;
	    try
	    {
            mCurrentVersionCode = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode;
            mCurrentVersionName = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
            ApplicationInfo appInfo = mContext.getPackageManager().getApplicationInfo(mContext.getPackageName(), PackageManager.GET_META_DATA);
            mAppChannelName = appInfo.metaData.getString("APP_CHANNEL");
        }
	    catch (NameNotFoundException e) 
        {
            e.printStackTrace();
        }
	}
	
	public boolean checkLatestVersion()
    {
        try
        {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            String xmlData = new DefaultNetDataGetter(AppEngine.getInstance().getUrlManager().tryToGetDnsedUrl(UrlManager.ProxyUrl.Update)).getStringData();
            if(xmlData == null)
            {
                return false;
            }
            Version versionHandler = new Version();
            parser.parse(new ByteArrayInputStream(xmlData.getBytes()), versionHandler);
            mLatestVersionCode = versionHandler.getVersionCode();
            mLatestVersionName = versionHandler.getVersionName();
            mUrl = versionHandler.getUrl();
            mLatestChannelVersion = versionHandler.getChannelVersion();
            if (mLatestVersionCode > mCurrentVersionCode)
                return true;
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch (ParserConfigurationException e)
        {
            e.printStackTrace();
        }
        catch (SAXException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return false;
    }
	
	public String getUrl()
	{
		return mUrl;
	}
	
	public String getCurrentVersionName()
    {
        return mCurrentVersionName;
    }
	
	public String getLatestVersionName()
	{
	    return mLatestVersionName;
	}
	
	public int getLatestChannelVersion()
	{
	    return mLatestChannelVersion;
	}
	
	public String getAppChannelName()
	{
	    return mAppChannelName;
	}
	
	private class Version extends DefaultHandler
    {
    	private static final String VERSION_CODE_TAG = "VersionCode";
    	private static final String VERSION_NAME_TAG = "VersionName";
    	private static final String URL_TAG = "Url";
    	private static final String CHANNEL_VERSION = "ChannelVersion";
    	private String mVersionCode;
    	private String mVersionName;
    	private String mUrl = null;
    	private String mChannelVersion;
    	private String mCurrentTag = null;
    	private boolean mIsStartElement = false;
    	
    	public int getVersionCode()
    	{
    		if (mVersionCode == null)
    		    return -1;
    		return Integer.valueOf(mVersionCode).intValue();
    	}
    	
    	public String getVersionName()
    	{
    	    return mVersionName;
    	}
    	
    	public String getUrl()
    	{
    		return mUrl;
    	}
    	
    	public int getChannelVersion()
    	{
    	    if (mVersionCode == null)
                return 1;
            return Integer.valueOf(mChannelVersion).intValue();
    	}
    	
    	@Override
    	public void startDocument() throws SAXException
    	{
    	}
    	
    	@Override
    	public void characters(char[] ch, int start, int length) throws SAXException
    	{
    		if(mCurrentTag != null && mIsStartElement)
    		{
    			String data = new String(ch, start, length);
    			if (mCurrentTag.toLowerCase(Locale.ENGLISH).equals(VERSION_CODE_TAG.toLowerCase(Locale.ENGLISH)))
    			{
	    			mVersionCode = data;
    			}
    			else if (mCurrentTag.toLowerCase(Locale.ENGLISH).equals(VERSION_NAME_TAG.toLowerCase(Locale.ENGLISH)))
    			{
    			    mVersionName = data;
    			}
    			else if (mCurrentTag.toLowerCase(Locale.ENGLISH).equals(URL_TAG.toLowerCase(Locale.ENGLISH)))
    			{
					mUrl = data;
    			}
    			else if (mCurrentTag.toLowerCase(Locale.ENGLISH).equals(CHANNEL_VERSION.toLowerCase(Locale.ENGLISH)))
    			{
    			    mChannelVersion = data;
    			}
    		}
    	}
    	
    	@Override
    	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException
    	{
    		mCurrentTag = localName;
    		mIsStartElement = true;
    	}
    	
    	@Override
    	public void endElement(String uri, String localName, String name) throws SAXException
    	{
    		mIsStartElement = false;
    	}
    	
    	@Override
    	public void endDocument()
    	{
    	}
    }
}
