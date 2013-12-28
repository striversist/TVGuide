package com.tools.tvguide.utils;

import com.tools.tvguide.R;
import com.tools.tvguide.managers.AppEngine;

import org.acra.ACRA;
import org.acra.ErrorReporter;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

@ReportsCrashes(
    formKey = "",
    formUri = "http://192.168.1.100:5984/acra-myapp/_design/acra-storage/_update/report",
    reportType = org.acra.sender.HttpSender.Type.JSON,
    httpMethod = org.acra.sender.HttpSender.Method.PUT,
    formUriBasicAuthLogin = "reporter",
    formUriBasicAuthPassword = "reporter",
    mode = ReportingInteractionMode.TOAST,
	customReportContent = { ReportField.APP_VERSION_CODE
          , ReportField.APP_VERSION_NAME
          , ReportField.ANDROID_VERSION
          , ReportField.PACKAGE_NAME
          , ReportField.REPORT_ID
          , ReportField.BUILD
          , ReportField.PHONE_MODEL
          , ReportField.STACK_TRACE
          , ReportField.CUSTOM_DATA
//		  , ReportField.LOGCAT
		  },
	forceCloseDialogAfterToast = false, // optional, default false
	resToastText = R.string.crash_toast_text)

public class MyApplication extends Application
{
	public static MyApplication sInstance = null;
	
	public static MyApplication getInstance()
	{
	    assert (sInstance != null);
		return sInstance;
	}
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		sInstance = this;
		
		ACRA.init(this);
		
		AppEngine.getInstance().setApplicationContext(getApplicationContext());
		if (AppEngine.getInstance().getUpdateManager().getGUID() != null)
		    ErrorReporter.getInstance().putCustomData("GUID", AppEngine.getInstance().getUpdateManager().getGUID());
	}
}
