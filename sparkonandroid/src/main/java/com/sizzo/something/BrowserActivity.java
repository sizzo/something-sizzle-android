package com.sizzo.something;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.sizzo.something.menu.OptionsMenu;

public class BrowserActivity extends Activity {

	private static String TAG = "something-browse";
	private WebView webview;
	OptionsMenu optionsMenu;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		getWindow().requestFeature(Window.FEATURE_PROGRESS);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browser);

		initWebView();
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		
		SharedPreferences sharedPreferences = this.getPreferences(Context.MODE_WORLD_READABLE);
		this.setTitle(sharedPreferences.getString("browser.title", "Rainbow"));
		webview.loadUrl(sharedPreferences.getString("lastUrl", "file:///android_asset/html/home.html"));
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {

		Editor e = this.getPreferences(Context.MODE_WORLD_READABLE).edit();
		e.putString ("browser.title", (String) this.getTitle());
		e.putString ("lastUrl", (String) webview.getUrl());
		e.commit();

		// outState.putString("lastUrl", webview.getUrl());
		super.onSaveInstanceState(outState);
	}

	private void initWebView() {
		webview = (WebView) findViewById(R.id.webview);

		webview.getSettings().setJavaScriptEnabled(true);
		final Activity activity = this;
		webview.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
				activity.setProgress(progress * 1000);
			}
		});

		webview.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}
		});
		webview.getSettings().setJavaScriptEnabled(true);
		webview.getSettings().setBlockNetworkImage(true);
		webview.getSettings().setBuiltInZoomControls(true);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			String url = extras.getString("url");
			webview.loadUrl(url);
			// webview.loadUrl("http://m.google.cn");
		} else {
			webview.loadUrl("file:///android_asset/html/home.html");
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) {
			webview.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		optionsMenu = new OptionsMenu(this, menu);
		return optionsMenu.createOptionsMenu();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return optionsMenu.optionsItemSelected(item);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		String url = intent.getExtras().getString("url");
		if(url!=null){
			webview.loadUrl(url);			
		}
		super.onNewIntent(intent);
	}
	
	
}
