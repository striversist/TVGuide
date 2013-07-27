package com.tools.tvguide.components;

import java.io.File;

import com.tools.tvguide.activities.R;
import com.tools.tvguide.utils.DownloadTask;
import com.tools.tvguide.utils.MyApplication;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

public class PackageInstaller
{
    private Context mContext;
	public static final String FILE_NAME = MyApplication.getInstance().getString(R.string.app_name) + ".apk";
	public static final String FILE_PATH = Environment.getExternalStorageDirectory() + File.separator
			+ MyApplication.getInstance().getString(R.string.app_name) + File.separator;
	
	public PackageInstaller(Context context)
	{
	    mContext = context;
	}
	
	public int installRemotePackage(String url)
	{
		assert(url != null);
		DownloadTask task = new DownloadTask(mContext, FILE_PATH, FILE_NAME);
        task.setOnDownloadListener(new DownloadTask.OnDownloadListener()
		{
			public void notifyStatus(DownloadTask.Status status)
			{
				switch(status)
				{
					case SUCCESS:
						install(new File(FILE_PATH + File.separator + FILE_NAME));
						break;
					case CANCELED:
						break;
					case FAILED:
						break;
				}
			}
		});
        task.execute(url);
		
		return 0;
	}
    
    private void install(File file)
    {
    	assert(file != null);
    	String mimeType = "application/vnd.android.package-archive";
    	Intent intent = new Intent();
    	intent.setAction(Intent.ACTION_VIEW);
    	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	intent.setDataAndType(Uri.fromFile(file), mimeType);
    	mContext.startActivity(intent);
    }
}
