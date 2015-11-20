package com.tools.tvguide.utils;

import com.tencent.bugly.crashreport.CrashReport;
import com.tools.tvguide.R;
import com.tools.tvguide.managers.AppEngine;
import com.tools.tvguide.managers.EnvironmentManager;
import com.tools.tvguide.managers.UrlManager;

import org.acra.ACRA;
import org.acra.ErrorReporter;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;

import android.app.Application;
import android.util.Log;

@ReportsCrashes(
    formKey = "",
    formUri = UrlManager.ACRA_PROXY_REAL,
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
//          , ReportField.BUILD
          , ReportField.PHONE_MODEL
          , ReportField.STACK_TRACE
          , ReportField.CUSTOM_DATA
//	      , ReportField.LOGCAT
		  },
	forceCloseDialogAfterToast = false, // optional, default false
	resToastText = R.string.crash_toast_text)

public class MyApplication extends Application
{
    private static final String TAG = "MyApplication";
	private static MyApplication sInstance = null;
	
	public static MyApplication getInstance()
	{
	    assert (sInstance != null);
		return sInstance;
	}
	
	@SuppressWarnings("deprecation")
    @Override
	public void onCreate()
	{
		super.onCreate();
		sInstance = this;
		
		AppEngine.getInstance().setApplicationContext(getApplicationContext());
		CrashReport.initCrashReport(getApplicationContext(), "900012517", false);
		if (EnvironmentManager.enableACRA)
		{
			ACRA.init(this);
	
			String url;
			if (EnvironmentManager.isDevelopMode)
			{
			    url = UrlManager.ACRA_PROXY_DEV;
			}
			else
			{
			    url = UrlManager.ACRA_PROXY_REAL;
			}
			ACRA.getConfig().setFormUri(url);
			
			// 自定义上报数据
			// GUID
			if (AppEngine.getInstance().getUpdateManager().getGUID() != null)
			    ErrorReporter.getInstance().putCustomData("GUID", AppEngine.getInstance().getUpdateManager().getGUID());
			
			// 网络状态
			if (Utility.isWifi(MyApplication.getInstance()))
			    ErrorReporter.getInstance().putCustomData("NETWORK", "WIFI");
			else
			    ErrorReporter.getInstance().putCustomData("NETWORK", "non-WIFI");
			
			// 渠道
			if (AppEngine.getInstance().getUpdateManager().getAppChannelName() != null)
			    ErrorReporter.getInstance().putCustomData("APP_CHANNEL", AppEngine.getInstance().getUpdateManager().getAppChannelName());

			if (EnvironmentManager.enableACRALog) {
    			ErrorReporter.getInstance().addReportSender(new ReportSender() {
                    @Override
                    public void send(CrashReportData reportData) throws ReportSenderException {
                        Log.e(TAG, reportData.toString());
                    }
                });
			}
		}
	}
}
