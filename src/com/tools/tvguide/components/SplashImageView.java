package com.tools.tvguide.components;

import com.tools.tvguide.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.view.WindowManager;
import android.widget.ImageView;

public class SplashImageView extends ImageView 
{
    private final static String     TAG                 = "SplashImageView";
    private Context                 mContext;
    private Bitmap                  mSplashBitmap       = null;
    private int                     mWindowWidth        = 0;
    private int                     mWindowHeight       = 0;
    private PaintFlagsDrawFilter    mPaintFlags         = new PaintFlagsDrawFilter(0, Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG);
    
    public SplashImageView(Context context)
    {
        super(context);
        mContext = context;
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mWindowWidth = wm.getDefaultDisplay().getWidth();
        mWindowHeight = wm.getDefaultDisplay().getHeight();

        configSplash();
    }
    
    // 配置闪屏
    private void configSplash()
    {
        setSplashBitmap();
    }
    
    // 绘制闪屏
    @Override
    public void draw(Canvas canvas)
    {
        canvas.setDrawFilter(mPaintFlags);

        int itemWidth = Math.min(mWindowWidth, mWindowHeight);
        int itemHeight = Math.max(mWindowWidth, mWindowHeight);
        final int bitmapWidth = mSplashBitmap.getWidth();
        final int bitmapHeight = mSplashBitmap.getHeight();
        final int dWidth = (itemWidth - bitmapWidth) / 2;
        final int dHeight = (itemHeight - bitmapHeight) / 2;

        Paint paint = new Paint();
        canvas.drawBitmap(mSplashBitmap, dWidth, dHeight, paint);
    }
    
    // 重新设置闪屏属性
    private void setSplashBitmap()
    {
        // 解析原始闪数据
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        
        mSplashBitmap = getDefaultSplashBmp();
        opt.outHeight = mSplashBitmap.getHeight();
        opt.outWidth = mSplashBitmap.getWidth();
        
        // 计算窗口相关参数
        int width = Math.min(mWindowWidth, mWindowHeight);
        int height = Math.max(mWindowWidth, mWindowHeight);
        int sbar = 0;   // Math.round(getResources().getDimension(R.dimen.statebar_height));

        // 计算闪屏应该显示的宽高
        int dstWidth = 0;
        int dstHeight = 0;
        if ((float) width / ((float) height - sbar) > (float) opt.outWidth / (float) opt.outHeight)
        {
            dstWidth = width;
            dstHeight = (int) (opt.outHeight * width / opt.outWidth);
        }
        else
        {
            dstWidth = (int) (opt.outWidth * ((float) height - sbar) / opt.outHeight);
            dstHeight = ((int) height - sbar);
        }
        opt.inJustDecodeBounds = false;
        
        // 判断是否需要缩放
        if (dstWidth != mSplashBitmap.getWidth() || dstHeight != mSplashBitmap.getHeight())
            mSplashBitmap = Bitmap.createScaledBitmap(mSplashBitmap, dstWidth, dstHeight, true);
    }
    
    private Bitmap getDefaultSplashBmp()
    {
        if (mSplashBitmap == null)
        {
            mSplashBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.splash_bkg);
        }
        return mSplashBitmap;
    }

    public void recycle()
    {
        if (null != mSplashBitmap)
        {
            mSplashBitmap.recycle();
            mSplashBitmap = null;
        }
    }
}
