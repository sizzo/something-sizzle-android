package com.sizzo.something;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import com.sizzo.something.wifi.WiFiScanReceiver;

public class MainActivity extends Activity {

	private static String TAG = "something-sizzle";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		registerReceiver(new WiFiScanReceiver(this, (ListView) findViewById(R.id.wifiListView)), new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

		((WifiManager) getSystemService(Context.WIFI_SERVICE)).startScan();
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
			launchBrowser();
		} else if (item.getItemId() == R.id.wifis) {
			((WifiManager) getSystemService(Context.WIFI_SERVICE)).startScan();
		} else if (item.getItemId() == R.id.history) {
		}
		Log.i(TAG, "Selected Menu Item=[" + item.getTitle() + "]");
		return false;
	}

	protected void launchBrowser() {
		Intent i = new Intent(this, BrowseActivity.class);
		this.startActivity(i);
	}
}
