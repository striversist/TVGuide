package com.tools.tvguide.views;

import com.tools.tvguide.R;
import android.content.Context;
import android.content.res.Resources;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.TranslateAnimation;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.ArrayList;

public class SlidingMenuView extends RelativeLayout implements View.OnClickListener 
{
    private TextView mCurTextView;
    private HorizontalScrollView mHoriScroll;
    private ImageView mImgTransBg;
    private OnSlidingMenuSelectListener mSelectListener;
    private LinearLayout mSlidingMainLL;
    private ArrayList<TextView> mSlidingTVList;
    private int mOffset = 0;

    public static abstract interface OnSlidingMenuSelectListener 
    {
        public abstract void onItemSelect(int paramInt, Object paramObject);
    }

    class SlidingTag 
    {
        public int index;
        public Object obj;
    }
    
    public SlidingMenuView(Context paramContext, AttributeSet paramAttributeSet) 
    {
        super(paramContext, paramAttributeSet);
        LayoutInflater.from(paramContext).inflate(R.layout.slidingmenu, this);
        mHoriScroll = ((HorizontalScrollView) findViewById(R.id.sliding_h_scrollview));
        mImgTransBg = ((ImageView) findViewById(R.id.sliding_bg_iv));
        mSlidingMainLL = ((LinearLayout) findViewById(R.id.sliding_main_ll));
        mImgTransBg.setVisibility(View.INVISIBLE);
        mSlidingTVList = new ArrayList<TextView>();
    }

    public void addMenu(String paramString, Object paramObject) 
    {
        SlidingTag localSlidingTag = new SlidingTag();
        localSlidingTag.index = mSlidingTVList.size();
        localSlidingTag.obj = paramObject;
        TextView localTextView = (TextView) View.inflate(getContext(), R.layout.slidingmenu_textview, null);
        localTextView.setText(paramString);
        localTextView.setTag(localSlidingTag);
        localTextView.setOnClickListener(this);
        mSlidingMainLL.addView(localTextView);
        mSlidingTVList.add(localTextView);
        if (mSlidingTVList.size() != 1)
            return;
        mCurTextView = localTextView;
        mCurTextView.setTextColor(getResources().getColor(R.color.dark_blue));

        // 获取文字大致宽度，设定mImgTransBg初始宽度
        if (paramString.length() > 0)
        {
            float textWidth = localTextView.getPaint().measureText(paramString);
            if (textWidth > 0) 
            {
                ViewGroup.LayoutParams localLayoutParams = mImgTransBg.getLayoutParams();
                localLayoutParams.width = (16 + (int)textWidth);
                mImgTransBg.setLayoutParams(localLayoutParams);
            }
        }
        mImgTransBg.setVisibility(View.VISIBLE);
    }
    
    public void setOnSlidingMenuSelectListener(OnSlidingMenuSelectListener onnSlidingMenuSelectListener) 
    {
        mSelectListener = onnSlidingMenuSelectListener;
    }

    public int getMenuCount() 
    {
        return mSlidingTVList.size();
    }

    public int getSelectIndex() 
    {
        SlidingTag localSlidingTag = null;
        if (mCurTextView != null) 
        {
            localSlidingTag = (SlidingTag) mCurTextView.getTag();
            if (localSlidingTag == null)
                return -1;
        }

        return localSlidingTag.index;
    }
    
    public void setSelectIndex(int index) 
    {
        if (index >= mSlidingTVList.size())
            return;
        ((TextView) mSlidingTVList.get(index)).performClick();
    }

    public void onClick(View paramView) 
    {
        SlidingTag slidingTag = (SlidingTag) paramView.getTag();
        if (slidingTag == null)
            return;
        imgBgTrans((TextView) mSlidingTVList.get(slidingTag.index));
        if (mSelectListener != null)
        {
            mSelectListener.onItemSelect(slidingTag.index, slidingTag.obj);
        }
    }

    public void removeAllMenus() 
    {
        mSlidingMainLL.removeAllViews();
        mSlidingTVList.clear();
        mCurTextView = null;
    }
    
    // 移动mImgTransBg到相应TextView的位置
    private void imgBgTrans(TextView paramTextView) 
    {
        int newImgBgWidth = -1;
        if (paramTextView.getWidth() > 0) 
        {
            ViewGroup.LayoutParams localLayoutParams = mImgTransBg.getLayoutParams();
            localLayoutParams.width = (4 + paramTextView.getWidth());
            mImgTransBg.setLayoutParams(localLayoutParams);
            newImgBgWidth = localLayoutParams.width;
        }
        int i = mCurTextView.getLeft() + mCurTextView.getWidth() / 2 - mImgTransBg.getWidth() / 2;
        int j = paramTextView.getLeft() + paramTextView.getWidth() / 2 - (newImgBgWidth == -1 ? mImgTransBg.getWidth() : newImgBgWidth) / 2;
        TranslateAnimation localTranslateAnimation = new TranslateAnimation(i, j, 0.0F, 0.0F);
        localTranslateAnimation.setDuration(100L);
        localTranslateAnimation.setFillAfter(true);
        mImgTransBg.startAnimation(localTranslateAnimation);
        int[] arrayOfInt = new int[2];
        paramTextView.getLocationOnScreen(arrayOfInt);
        if (arrayOfInt[0] - mOffset < 0)
            mHoriScroll.smoothScrollTo(paramTextView.getLeft(), 0);
        
        mCurTextView.setTextColor(getResources().getColor(R.color.gray));
        paramTextView.setTextColor(getResources().getColor(R.color.dark_blue));
        mCurTextView = paramTextView;
    }
}
