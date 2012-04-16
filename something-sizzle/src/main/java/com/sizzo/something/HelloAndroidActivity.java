package com.sizzo.something;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.sizzo.something.wifi.WiFiScanReceiver;

public class HelloAndroidActivity extends Activity {

	private static String TAG = "something-sizzle";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		registerReceiver(new WiFiScanReceiver((ListView) findViewById(R.id.wifiListView)), new IntentFilter(
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
			Toast.makeText(this.getApplicationContext(),
					"You just select RAINBOW menu, will redirect to rainbow home page", 30).show();
		} else if (item.getItemId() == R.id.wifis) {
			Toast.makeText(this.getApplicationContext(),
					"You just select WIFIs menu, will scan the wifis and show them in list", 30).show();
			((WifiManager) getSystemService(Context.WIFI_SERVICE)).startScan();
		} else if (item.getItemId() == R.id.history) {
			Toast.makeText(this.getApplicationContext(), "You just select History menu, will list your history", 30)
					.show();
		}
		Log.i(TAG, "Selected Menu Item=["+item.getItemId()+"]");
		return false;
	}
}
