package com.tools.tvguide.data;

public class TimestampString {
	long 	mDate;
	String 	mString = "";
	
	public TimestampString() {
	}
	
	public TimestampString(long date, String string) {
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
}
