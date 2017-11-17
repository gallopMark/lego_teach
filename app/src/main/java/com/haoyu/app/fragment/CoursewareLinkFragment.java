package com.haoyu.app.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.haoyu.app.lego.teach.R;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.view.ProgressWebView;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 创建日期：2017/11/16.
 * 描述:课程学习教学课件外链查看
 * 作者:xiaoma
 */

public class CoursewareLinkFragment extends Fragment {

    private FrameLayout fl_content;
    private FrameLayout fl_failure;
    private ProgressWebView webView;
    private Context context;
    private String url;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        Bundle bundle = getArguments();
        url = bundle.getString("url");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_webviewer, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        fl_content = view.findViewById(R.id.fl_content);
        fl_failure = view.findViewById(R.id.fl_failure);
        configWebview(url);
    }

    private void configWebview(String url) {
        webView = new ProgressWebView(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        webView.setLayoutParams(params);
        fl_content.addView(webView);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                try {
                    return handleOkHttp(url);
                } catch (Exception e) {
                    return super.shouldInterceptRequest(view, url);
                }
            }

            private WebResourceResponse handleOkHttp(String url) throws Exception {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .addHeader("Referer", Constants.REFERER)
                        .url(url).build();
                Call call = client.newCall(request);
                final Response response = call.execute();
                String mimeType = response.header("content-type", response.body().contentType().type());
                if (mimeType != null && mimeType.contains(";"))
                    mimeType = mimeType.substring(0, mimeType.indexOf(";"));
                String encoding = response.body().contentType().charset().name();
                return new WebResourceResponse(mimeType, encoding, response.body().byteStream());
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, android.net.http.SslError error) {
                handler.proceed();
            }

            @Override
            public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
                return super.shouldOverrideKeyEvent(view, event);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                fl_content.setVisibility(View.GONE);
                fl_failure.setVisibility(View.VISIBLE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                fl_failure.setVisibility(View.GONE);
                fl_failure.setVisibility(View.VISIBLE);
            }
        });
        webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        webView.loadUrl(url);
        webView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {  //表示按返回键
                        webView.goBack();   //后退
                        return true;    //已处理
                    }
                }
                return false;
            }
        });
        fl_failure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fl_content.setVisibility(View.VISIBLE);
                fl_failure.setVisibility(View.GONE);
                webView.reload();
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
        fl_content.removeAllViews();
        if (webView != null) {
            webView.stopLoading();
            webView.removeAllViews();
            webView.destroy();
            webView = null;
        }
    }
}
