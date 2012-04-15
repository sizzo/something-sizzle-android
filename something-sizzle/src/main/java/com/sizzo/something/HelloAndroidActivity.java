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
import android.widget.Toast;

import com.sizzo.something.wifi.WiFiScanReceiver;

public class HelloAndroidActivity extends Activity {

	private static String TAG = "something-sizzle";
	private WiFiScanReceiver wifiScanReceiver;
	private WifiManager wifiManager;

	/**
	 * Called when the activity is first created.
	 * 
	 * @param savedInstanceState
	 *            If the activity is being re-initialized after previously being
	 *            shut down then this Bundle contains the data it most recently
	 *            supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it
	 *            is null.</b>
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate");
		setContentView(R.layout.main);
		registerReceiver(getWifiScanReceiver(), new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// creates a menu inflater
		MenuInflater inflater = getMenuInflater();
		// generates a Menu from a menu resource file
		// R.menu.main_menu represents the ID of the XML resource file
		inflater.inflate(R.menu.main_menu, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// check selected menu item

		if (item.getItemId() == R.id.dynamicUserHome) {
			Toast.makeText(this.getApplicationContext(),
					"You just select RAINBOW menu, will redirect to rainbow home page", 30).show();
		} else if (item.getItemId() == R.id.wifis) {
			Toast.makeText(this.getApplicationContext(),
					"You just select WIFIs menu, will scan the wifis and show them in list", 30).show();
			getWifiManager().startScan();
		} else if (item.getItemId() == R.id.history) {
			Toast.makeText(this.getApplicationContext(), "You just select History menu, will list your history", 30)
					.show();
		}
		return false;
	}

	public WiFiScanReceiver getWifiScanReceiver() {
		if (wifiScanReceiver == null) {
			wifiScanReceiver = new WiFiScanReceiver();
		}
		return wifiScanReceiver;
	}

	public WifiManager getWifiManager() {
		if (wifiManager == null) {
			wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		}
		return wifiManager;
	}

}
