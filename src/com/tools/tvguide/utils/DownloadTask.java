package com.tools.tvguide.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class DownloadTask extends AsyncTask<String, Integer, DownloadTask.Status>
{
	public enum Status
	{
		SUCCESS,
		FAILED,
		CANCELED,
	}
	private ProgressDialog dialog = null;
	private Context mContext = null;
	private OnDownloadListener mListener = null;
	private String mFilePath = null;
	private String mFileName = null;
	public DownloadTask(Context context, String saveFilePath, String saveFileName)
	{
		if(context == null)
		{
			Log.e("DownloadTask", "context is null");
		}
		mContext = context;
		mFilePath = saveFilePath;
		mFileName = saveFileName;
	}
	
	public void setOnDownloadListener(OnDownloadListener listener)
	{
		mListener = listener;
	}
	
	@Override
	protected void onPreExecute()
	{
		dialog = new ProgressDialog(mContext);
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setTitle("下载");
        dialog.setMessage("下载文件");
        dialog.setIndeterminate(false);
        dialog.setCancelable(true);
        dialog.setButton("取消", new DialogInterface.OnClickListener() 
        {
            public void onClick(DialogInterface dialog, int which) 
            {
            	Log.d("onPreExecute", "cancel dialog");
                dialog.cancel();
                cancel(true);
            }
        });
        dialog.show();
	}

	@Override
	protected Status doInBackground(String... params)
	{
		if(mFilePath == null || mFileName == null)
		{
			return Status.FAILED;
		}
		File file = new File(mFilePath);
		if(file.mkdirs() == false)
		{
			if(file.exists() == false)
			{
				Log.e("doInBackground", "create path failed: " + mFilePath);
				return Status.FAILED;
			}
		}
		try
		{
			URL mUrl = new URL(params[0]);
			HttpParams httpParameters = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParameters, 3000);
			HttpConnectionParams.setSoTimeout(httpParameters, 5000);
			
			HttpClient client = new DefaultHttpClient(httpParameters);
	    	HttpPost post = new HttpPost(mUrl.toString());
	    	HttpEntity entity = client.execute(post).getEntity();

	    	OutputStream output = new FileOutputStream(mFilePath + File.separator + mFileName);
			int maxSize = (int)entity.getContentLength();
			InputStream input = entity.getContent();
	    	int nowSize = 0;
	    	byte buf[] = new byte[4096];
			do
			{
				int numRead = input.read(buf);
				if(numRead <= 0)
				{
					Log.d("doInBackground", "read finished");
					break;
				}
				nowSize += numRead;
				output.write(buf, 0, numRead);
				publishProgress(maxSize, nowSize);
			}while(!isCancelled());
			input.close();
			output.close();
		}
    	catch (MalformedURLException e)
		{
    		Log.e("doInBackground", "MalformedURLException " + e.toString());
			e.printStackTrace();
			return Status.FAILED;
		}
		catch (ClientProtocolException e)
		{
			Log.e("doInBackground", "ClientProtocolException " + e.toString());
			e.printStackTrace();
			return Status.FAILED;
		}
		catch (IOException e)
		{
			Log.e("doInBackground", "IOException " + e.toString());
			e.printStackTrace();
			return Status.FAILED;
		}
		
		Log.d("doInBackground", "isCancelled = " + isCancelled());
		if(isCancelled())
		{
			return Status.CANCELED;
		}
		else
		{
			return Status.SUCCESS;
		}
	}
	
	@Override
	protected void onProgressUpdate(Integer... values)
	{
		if(dialog == null)
		{
			Log.e("onProgressUpdate", "dialog is null");
			return;
		}
		int maxSize = values[0];
		int nowSize = values[1];
		
		dialog.setMax(maxSize);
		dialog.setProgress(nowSize);
	}
	
	@Override
	protected void onPostExecute(Status result)
	{
		Log.d("onPostExecute", "Begin");
		dialog.cancel();
		String message = null;
		if(result == Status.SUCCESS)
		{
			message = "下载成功";
		}
		else if(result == Status.FAILED)
		{
			message = "下载失败";
		}
		else if(result == Status.CANCELED)
		{
			message = "下载被取消";
		}
		Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
		if(mListener != null)
		{
			mListener.notifyStatus(result);
		}
	}
	
	protected void onCancelled()
	{
		Log.d("onCancelled", "Begin");
		onPostExecute(Status.CANCELED);
	}
	
	public interface OnDownloadListener
	{
		public void notifyStatus(DownloadTask.Status status);
	}
}
