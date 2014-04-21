package com.tools.tvguide.data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.text.TextUtils;

public class TimeStampString {
	long 	mDate;
	String 	mString = "";
	
	public TimeStampString() {
	}
	
	public TimeStampString(long date, String string) {
		mDate = date;
		mString = string;
	}
	
	public void setDate(long date) {
		mDate = date;
	}
	
	public long getDate() {
		return mDate;
	}
	
	public void setString(String html) {
		mString = html;
	}
	
	public String getString() {
		return mString;
	}
	
	public boolean isExpired() {
		return isTheSameDay(mDate, System.currentTimeMillis());
	}
	
	private boolean isTheSameDay(long date1, long date2) {
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
		String date1String = sf.format(new Date(date1));
		String date2String = sf.format(new Date(date2));
		
		return TextUtils.equals(date1String, date2String);
	}
}
