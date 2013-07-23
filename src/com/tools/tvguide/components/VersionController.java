package com.tools.tvguide.components;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
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
	
	public VersionController(Context context)
	{
	    mContext = context;
	    try
	    {
            mCurrentVersionCode = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode;
            mCurrentVersionName = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
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
            String xmlData = new NetDataGetter(AppEngine.getInstance().getUrlManager().getUrl(UrlManager.URL_UPDATE)).getStringData();
            if(xmlData == null)
            {
                return false;
            }
            Version versionHandler = new Version();
            parser.parse(new ByteArrayInputStream(xmlData.getBytes()), versionHandler);
            mLatestVersionCode = versionHandler.getVersionCode();
            mLatestVersionName = versionHandler.getVersionName();
            mUrl = versionHandler.getUrl();
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
	
	private class Version extends DefaultHandler
    {
    	private static final String VERSION_CODE_TAG = "versionCode";
    	private static final String VERSION_NAME_TAG = "versionName";
    	private static final String URL_TAG = "url";
    	private String mVersionCode;
    	private String mVersionName;
    	private String mUrl = null;
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
    			if (mCurrentTag.equals(VERSION_CODE_TAG))
    			{
	    			mVersionCode = data;
    			}
    			else if (mCurrentTag.equals(VERSION_NAME_TAG))
    			{
    			    mVersionName = data;
    			}
    			else if (mCurrentTag.equals(URL_TAG))
    			{
					mUrl = data;
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
