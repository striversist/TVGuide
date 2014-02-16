package com.tools.tvguide.views;

import java.util.ArrayList;
import java.util.List;

import com.tools.tvguide.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SearchHotwordsView extends RelativeLayout 
{
    public static final String TAG = "SearchHotwordsView";
    public static final int MAX_WORDS = 6;
    private List<String> mWordList;
    private LinearLayout mLineLayout1;
    private LinearLayout mLineLayout2;
    private TextView mWordTextView1;
    private TextView mWordTextView2;
    private TextView mWordTextView3;
    private TextView mWordTextView4;
    private TextView mWordTextView5;
    private TextView mWordTextView6;
    private List<TextView> mTextViewList;
    private OnItemClickListener mListener;
    
    public static interface OnItemClickListener
    {
        public void onItemClick(String string);
    }
    
    public SearchHotwordsView(Context context, AttributeSet attrs) 
    {
        super(context, attrs);
        
        LayoutInflater.from(context).inflate(R.layout.search_hotwords_layout, this);
        mWordList = new ArrayList<String>();
        mTextViewList = new ArrayList<TextView>();
        
        mLineLayout1 = (LinearLayout) findViewById(R.id.hot_words_line1);
        mLineLayout2 = (LinearLayout) findViewById(R.id.hot_words_line2);
        mWordTextView1 = (TextView) findViewById(R.id.hot_words_1);
        mWordTextView2 = (TextView) findViewById(R.id.hot_words_2);
        mWordTextView3 = (TextView) findViewById(R.id.hot_words_3);
        mWordTextView4 = (TextView) findViewById(R.id.hot_words_4);
        mWordTextView5 = (TextView) findViewById(R.id.hot_words_5);
        mWordTextView6 = (TextView) findViewById(R.id.hot_words_6);
        
        mTextViewList.add(mWordTextView1);
        mTextViewList.add(mWordTextView2);
        mTextViewList.add(mWordTextView3);
        mTextViewList.add(mWordTextView4);
        mTextViewList.add(mWordTextView5);
        mTextViewList.add(mWordTextView6);
        
        for (int i=0; i<mTextViewList.size(); ++i)
        {
            mTextViewList.get(i).setOnClickListener(new OnClickListener() 
            {
                @Override
                public void onClick(View v) 
                {
                    String clickWord = (String) ((TextView) v).getText();
                    if (mListener != null && clickWord != null && clickWord.trim().length() > 0)
                        mListener.onItemClick(clickWord);
                }
            });
        }
    }
    
    public void setOnItemClickListener(OnItemClickListener listener)
    {
        assert (listener != null);
        mListener = listener;
    }
    
    public void setWords(String[] strArray)
    {
        assert (strArray != null);
        mWordList.clear();
        for (int i=0; i<strArray.length; ++i)
        {
            if (mWordList.size() < MAX_WORDS)
                mWordList.add(strArray[i]);
        }
        update();
    }
    
    private void update()
    {
        if (mWordList.size() <= 3)
            mLineLayout2.setVisibility(View.GONE);
        else
            mLineLayout2.setVisibility(View.VISIBLE);
        
        for (int i=0; i<mWordList.size(); ++i)
            mTextViewList.get(i).setText(mWordList.get(i));
    }
}




