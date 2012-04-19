package com.sizzo.something;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.sizzo.something.menu.OptionsMenu;
import com.sizzo.something.wifi.WiFiScanReceiver;

public class MainActivity extends Activity {

	private static String TAG = "something-sizzle";
	OptionsMenu optionsMenu;

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
		optionsMenu = new OptionsMenu(this,menu);
		return optionsMenu.createOptionsMenu();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return optionsMenu.optionsItemSelected(item);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}
	
	
	
}
