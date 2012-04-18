package com.sizzo.something;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;

import com.sizzo.something.browser.SomethingWebView;

public class BrowseActivity extends Activity {

	private static String TAG = "something-browse";
	private SomethingWebView somethingWebView  ;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browser);

		somethingWebView = new SomethingWebView((WebView) findViewById(R.id.webview));
		
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && somethingWebView.webview.canGoBack()) {
			somethingWebView.webview.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.dynamicUserHome) {
			somethingWebView.webview.loadUrl("http://m.google.cn");			
		} else if (item.getItemId() == R.id.wifis) {
			launchWifiList();
		} else if (item.getItemId() == R.id.history) {
		}
		Log.i(TAG, "Selected Menu Item=[" + item.getTitle() + "]");
		return false;
	}
	

	protected void launchWifiList() {
		Intent i = new Intent(this, MainActivity.class);
		this.startActivity(i);
	}
}
