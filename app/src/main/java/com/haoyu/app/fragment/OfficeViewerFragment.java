package com.haoyu.app.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.haoyu.app.lego.teach.R;
import com.haoyu.app.view.LoadFailView;

/**
 * 创建日期：2017/11/16.
 * 描述:office文档查看
 * 作者:xiaoma
 */

public class OfficeViewerFragment extends Fragment {

    private FrameLayout fl_content;
    private WebView webView;
    private ProgressBar progressBar;
    private LoadFailView loadFailView;
    private String url;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Bundle bundle = getArguments();
        String url = bundle.getString("url");
        this.url = "https://view.officeapps.live.com/op/view.aspx?src=" + url;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_webviewer, container, false);
        initView(view);
        setListener();
        return view;
    }

    private void initView(View view) {
        fl_content = view.findViewById(R.id.fl_content);
        loadFailView = view.findViewById(R.id.loadFailView);
        webView = view.findViewById(R.id.webView);
        progressBar = view.findViewById(R.id.progressBar);
        loadFailView.setErrorMsg("网页加载失败，请点击重试！");
        configWebview(url);
    }

    private void configWebview(String url) {
        initWebViewSettings();
        webView.setWebViewClient(new X5WebViewClient());
        webView.setWebChromeClient(new X5WebChromeClient());
        webView.loadUrl(url);
    }

    private void initWebViewSettings() {
        WebSettings webSetting = webView.getSettings();
        webSetting.setSupportZoom(true);
        webSetting.setLoadWithOverviewMode(true);
        webSetting.setUseWideViewPort(true);//设置此属性，可任意比例缩放
        //调用JS方法.安卓版本大于17,加上注解 @JavascriptInterface
        webSetting.setJavaScriptEnabled(true);
        webSetting.setSupportMultipleWindows(true);
        webSetting.setBuiltInZoomControls(true);
        // 支持通过js打开新的窗口
        webSetting.setJavaScriptCanOpenWindowsAutomatically(true);
        webSetting.setPluginState(WebSettings.PluginState.ON);
        //不显示webview缩放按钮
        webSetting.setDisplayZoomControls(false);
        webSetting.setDefaultZoom(WebSettings.ZoomDensity.FAR);// 屏幕自适应网页,如果没有这个，在低分辨率的手机上显示可能会异常
        webSetting.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSetting.setDomStorageEnabled(true);
        webSetting.setDomStorageEnabled(true);
    }

    private class X5WebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            fl_content.setVisibility(View.GONE);
            loadFailView.setVisibility(View.VISIBLE);
        }
    }

    private class X5WebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress == 100) {
                progressBar.setVisibility(View.GONE);
                if (loadFailView.getVisibility() != View.GONE) {
                    loadFailView.setVisibility(View.VISIBLE);
                }
            } else {
                if (progressBar.getVisibility() != View.VISIBLE) {
                    progressBar.setVisibility(View.VISIBLE);
                }
                progressBar.setProgress(newProgress);
            }
            super.onProgressChanged(view, newProgress);
        }
    }

    private void setListener() {
        webView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
                    webView.goBack();// 返回前一个页面
                    return true;
                }
                return false;
            }
        });
        loadFailView.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                webView.reload();
                fl_content.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (webView != null) {
            webView.onResume();
            webView.resumeTimers();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (webView != null) {
            webView.onPause();
            webView.pauseTimers(); //小心这个！！！暂停整个 WebView 所有布局、解析、JS。
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            webView.stopLoading();
            webView.removeAllViews();
            webView.destroy();
            webView = null;
        }
    }
}
