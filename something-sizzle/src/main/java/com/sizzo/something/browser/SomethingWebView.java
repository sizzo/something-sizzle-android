package com.sizzo.something.browser;

import android.webkit.WebView;
import android.webkit.WebViewClient;

public class SomethingWebView {
	public WebView webview;

	public SomethingWebView(WebView webview) {
		this.webview = webview;
		initWebView();
	}
	
	private void initWebView() {
		webview.setWebViewClient(new SomethingWebViewClient());
		webview.getSettings().setJavaScriptEnabled(true);
		webview.getSettings().setBlockNetworkImage(true);
		webview.getSettings().setBuiltInZoomControls(true);
		webview.loadUrl("file:///android_asset/html/home.html");
//		webview.loadUrl("http://m.google.cn");
	}

	private class SomethingWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}
	}

}