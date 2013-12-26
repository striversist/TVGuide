package com.tools.tvguide.utils;

import com.tools.tvguide.R;

import org.acra.ACRA;
import org.acra.ErrorReporter;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

@ReportsCrashes(formKey="",
	mailTo = "bigeyecow@qq.com",
	customReportContent = { ReportField.APP_VERSION_CODE, ReportField.APP_VERSION_NAME, ReportField.ANDROID_VERSION, ReportField.PHONE_MODEL, ReportField.CUSTOM_DATA, ReportField.STACK_TRACE, ReportField.LOGCAT },
	mode = ReportingInteractionMode.TOAST,
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
	}
}
