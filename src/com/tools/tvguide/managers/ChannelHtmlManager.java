package com.tools.tvguide.managers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.tools.tvguide.data.ChannelDate;
import com.tools.tvguide.data.GlobalData;
import com.tools.tvguide.data.Program;
import com.tools.tvguide.data.TimestampString;
import com.tools.tvguide.utils.HtmlUtils;

@SuppressLint("SetJavaScriptEnabled")
public class ChannelHtmlManager 
{
    public static final String TAG = ChannelHtmlManager.class.getSimpleName();
    private Context mContext;
    private WebView mWebView;
    private Handler mUiHandler;
    
    interface ILoadListener {
        @JavascriptInterface
        public void processHTML(String html);
    }
    
    public ChannelHtmlManager(Context context)
    {
        assert (context != null);
        mContext = context;
        mWebView = new WebView(mContext);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setUserAgentString(GlobalData.ChromeUserAgent);
        mWebView.getSettings().setBlockNetworkImage(true);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
            }

            public void onPageFinished(WebView view, String url) {
                view.loadUrl("javascript:window.HTMLOUT.processHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
            }
        });
        mUiHandler = new Handler(Looper.getMainLooper());
    }
    
    public interface ChannelDetailCallback
    {
        void onProgramsLoaded(int requestId, List<Program> programList);
        void onDateLoaded(int requestId, List<ChannelDate> channelDateList);
        void onError(int requestId, String errorMsg);
    }
    
    public void getChannelDetailFromFullWebAsync(final int requestId, final String channelUrl, final ChannelDetailCallback callback)
    {
        assert (callback != null);
        new Thread(new Runnable() 
        {
            @Override
            public void run() 
            {
                try 
                {
                    Document doc = loadHTMLDocument(channelUrl);
                    String protocol = new URL(channelUrl).getProtocol();
                    String host = new URL(channelUrl).getHost();
                    String prefix = protocol + "://" + host;
                    
                    Log.d(TAG, "getChannelDetailFromFullWebAsync: get html size " + doc.html().length());
                 
                    // -------------- 获取节目信息 --------------
                    // 返回结果
                    List<Program> retProgramList = new ArrayList<Program>();
                    Elements programs = doc.select("ul[id=pgrow] li");
                    if (programs.size() > 0) {
                        TimestampString tString = new TimestampString(System.currentTimeMillis(), doc.html());
                        AppEngine.getInstance().getDiskCacheManager().setTimestampString(makeKey_webview(channelUrl), tString);
                    }
                    for (int i=0; i<programs.size(); ++i) {
                        // 过滤结果
                        String time = "";
                        String title = "";
                        String trailer = "";
                        String link = null;
                        
                        Element program = programs.get(i);
                        time = program.select("span").text().trim();
                        if (time == null || time.length() == 0)
                            continue;
                        
                        title = program.text().trim().replaceFirst(time, "").trim();
                        Elements others = program.select("> a");
                        if (others.size() > 1)  // 去除多余的信息：如“剧照”、“演员表”等
                        {
                            for (int j=1; j<others.size(); ++j)
                            {
                                title = title.replaceFirst(others.get(j).text(), "").trim();
                            }
                        }
                        
                        Element dramaElement = program.select("> a[class=drama]").first();      // 直属的孩子
                        if (dramaElement != null)
                        {
                            String dramaString = dramaElement.ownText();
                            title = title.replaceFirst(dramaString + ".+", "").trim();
                        }
                        
                        Element trailer1 = program.select("div[class=tvgd]").first();
                        if (trailer1 != null)        // 存在节目预告
                        {
                            title = title.replace(trailer1.text(), "").trim();
                            
                            Elements pargfs = trailer1.select("p");
                            for (int j=0; j<pargfs.size(); ++j)
                            {
                                trailer += pargfs.get(j).ownText();
                                if (j < pargfs.size() - 1)
                                    trailer += "\n";
                            }
                        }
                        Element trailer2 = program.select("div[class=tvcgd]").first();
                        if (trailer2 != null)        // 存在节目预告
                        {
                            title = title.replace(trailer2.text(), "").trim();
                            
                            trailer += trailer2.ownText();
                            Elements pargfs = trailer2.select("p");
                            for (int j=0; j<pargfs.size(); ++j)
                            {
                                trailer += pargfs.get(j).ownText();
                                if (j < pargfs.size() - 1)
                                    trailer += "\n";
                            }
                        }
                        
                        Element linkElement = program.select("> a[href]").first();
                        if (linkElement != null)
                            link = prefix + linkElement.attr("href");
                        
                        Program addProgram = new Program();
                        addProgram.time = time;
                        addProgram.title = title;
                        addProgram.trailer = trailer;
                        // 在tvmao改版之后，drama和movie可以直接做link；tvcolumn要转为m.tvmao.com的link
                        if (link != null)
                        {
                            if (link.contains("drama") || link.contains("movie"))
                                addProgram.link = link;
                            else if (link.contains("tvcolumn"))
                                addProgram.link = link.replace("www.tvmao.com", "m.tvmao.com");
                        }
                        retProgramList.add(addProgram);
                    }
                    
                    callback.onProgramsLoaded(requestId, retProgramList);
                    
                    // -------------- 获取日期信息 --------------
                    // 返回结果
                    List<ChannelDate> retDateList = new ArrayList<ChannelDate>();
                    Element thisWeek = doc.select("nav.theweek").first();
                    if (thisWeek != null)
                    {
                        Elements dates = thisWeek.select("> a");
                        for (int i=0; i<dates.size(); ++i)
                        {
                            String name = dates.get(i).text();
                            if (name.startsWith("上周"))
                                continue;
                            
                            if (!name.startsWith("周"))
                                name = "周" + name;
                            
                            String href = prefix + dates.get(i).attr("href");
                            if (href == null || href.equals(""))    // 当前的页面
                            {
                                href = channelUrl;
                            }
                            
                            ChannelDate channelDate = new ChannelDate();
                            channelDate.name = name;
                            channelDate.link = href;
                            channelDate.setDay(i + 1);
                            retDateList.add(channelDate);
                        }
                    }
                    
                    Element nextWeek = doc.select("nav.nextweek").first();
                    if (nextWeek != null)
                    {
                        Elements dates = nextWeek.select("> a");
                        for (int i=0; i<dates.size(); ++i)
                        {
                            String name = dates.get(i).text();
                            if (!name.startsWith("周"))
                                name = "周" + name;
                            if (!name.startsWith("下"))
                                name = "下" + name;
                            
                            Element test = dates.get(i);
                            String testurl = prefix + test.attr("href");
                            Log.d("", testurl);
                            
                            String href = prefix + dates.get(i).attr("href");
                            if (href == null || href.equals(""))    // 当前的页面
                            {
                                href = channelUrl;
                            }
                            
                            ChannelDate channelDate = new ChannelDate();
                            channelDate.name = name;
                            channelDate.link = href;
                            channelDate.setDay(i + 7 + 1);
                            retDateList.add(channelDate);
                        }
                    }
                    
                    callback.onDateLoaded(requestId, retDateList);
                    
                }
                catch (MalformedURLException e) 
                {
                    e.printStackTrace();
                    callback.onError(requestId, e.toString());
                }
            }
        }).start();
    }
    
    public void getChannelDetailFromSimpleWebAsync(final int requestId, final String channelUrl, 
    		final ChannelDetailCallback callback) {
    	getChannelDetailFromSimpleWebAsync(requestId, channelUrl, callback, null);
    }
    
    public void getChannelDetailFromSimpleWebAsync(final int requestId, final String channelUrl, 
    		final ChannelDetailCallback callback, Handler handler)
    {
        assert (callback != null);
        Runnable runnable = new Runnable() {
        	@Override
            public void run() 
            {
                try 
                {   
                    Document doc = loadHTMLDocument(channelUrl);
                    String protocol = new URL(channelUrl).getProtocol();
                    String host = new URL(channelUrl).getHost();
                    String prefix = protocol + "://" + host;
                 
                    // -------------- 获取节目信息 --------------
                    // 返回结果
                    List<Program> retProgramList = new ArrayList<Program>();
                    Elements programs = doc.select("table.timetable tr");
                    if (programs.size() > 0) {
                        TimestampString tString = new TimestampString(System.currentTimeMillis(), doc.html());
                        AppEngine.getInstance().getDiskCacheManager().setTimestampString(makeKey_webview(channelUrl), tString);
                    }
                    for (int i=0; i<programs.size(); ++i) {
                        Elements tdElements = programs.get(i).select("td");
                        if (tdElements.size() == 2) {
                            Element timeElement = tdElements.get(0);
                            Element titleElement = tdElements.get(1);
                            
                            if (timeElement.text().contains("时间") && titleElement.text().contains("节目")) {  // header
                                continue;
                            }
                            
                            Program addProgram = new Program();
                            addProgram.time = timeElement.text();
                            addProgram.title = titleElement.text();
                            
                            Element linkElement = titleElement.select("a").first();
                            if (linkElement != null) {
                                addProgram.link = prefix + linkElement.attr("href");
                            }
                            retProgramList.add(addProgram);
                        }
                    }
                    callback.onProgramsLoaded(requestId, retProgramList);
                }
                catch (MalformedURLException e) 
                {
                    e.printStackTrace();
                }
            }
		};
		if (handler != null) {
			handler.post(runnable);
		} else {
			new Thread(runnable).start();
		}
    }
    
    private String makeKey_webview(String url) {
        return url + "_from_webview";
    }
    
    private Document loadHTMLDocument(String url) {
        TimestampString tString = AppEngine.getInstance().getDiskCacheManager().getTimestampString(makeKey_webview(url));
        if (tString != null && !HtmlUtils.isExpired(tString.getDate())) {
            return Jsoup.parse(tString.getString());
        }
        final CountDownLatch signal = new CountDownLatch(1);
        final StringBuffer buffer = new StringBuffer();
        ILoadListener listener = new ILoadListener() {
            @Override
            public void processHTML(String html) {
                if (html == null) {
                    html = "";
                }
                buffer.append(html);
                signal.countDown();
            }
        };
        loadHTMLStringFromWebView(url, listener);
        try {
            signal.await(120000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Log.e(TAG, "loadHTMLDocument exception: " + e.getMessage());
        }
        return Jsoup.parse(buffer.toString());
    }
    
    private void loadHTMLStringFromWebView(final String url, final ILoadListener listener) {
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                mWebView.removeJavascriptInterface("HTMLOUT");
                mWebView.stopLoading();
                
                mWebView.addJavascriptInterface(listener, "HTMLOUT");
                mWebView.loadUrl(url);
            }
        });
    }
}
