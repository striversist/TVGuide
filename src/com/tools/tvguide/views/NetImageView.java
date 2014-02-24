package com.tools.tvguide.views;

import java.lang.ref.WeakReference;

import com.tools.tvguide.utils.Utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.widget.ImageView;

public class NetImageView extends ImageView
{
    public static interface ImageLoadListener
    {
        void onImageLoaded(Bitmap bitmap);
    }
    
    public NetImageView(Context context, AttributeSet attrs) 
    {
        super(context, attrs);
    }

    public void loadImage(String url)
    {
        if (url == null)
            return;
        
        ImageUrlAsyncTask task = new ImageUrlAsyncTask(this, null);
        task.execute(url);
    }
    
    private class ImageUrlAsyncTask extends AsyncTask<String, Void, Bitmap>
    {
        private WeakReference<ImageView> mImageViewRef;
        private ImageLoadListener mListener;
        
        public ImageUrlAsyncTask(ImageView imageView, ImageLoadListener listener)
        {
            assert (imageView != null);
            mImageViewRef = new WeakReference<ImageView>(imageView);
            mListener = listener;
        }
        
        @Override
        protected Bitmap doInBackground(String... urls) 
        {
            Bitmap bitmap = Utility.getNetworkImage(urls[0]);
            return bitmap;
        }
        
        @Override
        protected void onPostExecute(Bitmap bitmap)
        {
            if (bitmap == null)
                return;
            
            if (mImageViewRef.get() != null)
                mImageViewRef.get().setImageBitmap(bitmap);
            
            if (mListener != null)
                mListener.onImageLoaded(bitmap);
        }
    }
}
