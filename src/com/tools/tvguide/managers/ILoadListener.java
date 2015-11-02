package com.tools.tvguide.managers;

import android.webkit.JavascriptInterface;

public interface ILoadListener {
    @JavascriptInterface
    public void processHTML(String html);
}
