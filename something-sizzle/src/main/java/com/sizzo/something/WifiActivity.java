package com.sizzo.something;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.sizzo.something.menu.OptionsMenu;

public class WifiActivity extends Activity {

	private static String TAG = "something-sizzle";
	OptionsMenu optionsMenu;
	private BroadcastReceiver wifiScanReceiver;
	private WifiManager wifiManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		registerWifiScanReceiver();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		optionsMenu = new OptionsMenu(this, menu);
		return optionsMenu.createOptionsMenu();
	}

	@Override
	protected void onPause() {
		unregisterWifiScanReceiver();
		super.onPause();
	}

	@Override
	protected void onRestart() {
		registerWifiScanReceiver();
		super.onRestart();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return optionsMenu.optionsItemSelected(item);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		registerWifiScanReceiver();
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		unregisterWifiScanReceiver();
		super.onSaveInstanceState(outState);
	}

	private void registerWifiScanReceiver() {
		if (!getWifiManager().isWifiEnabled()) {
			getWifiManager().setWifiEnabled(true);
		}
		if (wifiScanReceiver == null) {
			wifiScanReceiver = new WiFiScanReceiver(this, (ListView) findViewById(R.id.wifiListView));
			registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
			getWifiManager().startScan();

		}
	}

	private WifiManager getWifiManager() {
		if (wifiManager == null) {
			wifiManager = ((WifiManager) getSystemService(Context.WIFI_SERVICE));
		}
		return wifiManager;
	}

	private void unregisterWifiScanReceiver() {
		if (wifiScanReceiver != null) {
			unregisterReceiver(wifiScanReceiver);
			wifiScanReceiver = null;
		}
	}

	private class WiFiScanReceiver extends BroadcastReceiver {
		private static final String TAG = "WiFiScanReceiver";
		private ListView listView;
		private Activity activity;
		private List<ScanResult> wifiList;

		public WiFiScanReceiver(Activity activity, final ListView listView) {
			super();
			this.activity = activity;
			this.listView = listView;
			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					launchBrowser(position);
				}
			});
		}

		protected void launchBrowser(int position) {
			if (wifiList != null && wifiList.get(position) != null) {
				Intent i = new Intent(activity, BrowserActivity.class);
				i.putExtra("url", "http://m.hao123.com?q=" + wifiList.get(position).SSID);
				activity.startActivity(i);
			}
		}

		@Override
		public void onReceive(Context c, Intent intent) {
			wifiList = ((WifiManager) c.getSystemService(Context.WIFI_SERVICE)).getScanResults();
			ScanResult bestSignal = null;
			for (ScanResult result : wifiList) {
				if (bestSignal == null || WifiManager.compareSignalLevel(bestSignal.level, result.level) < 0)
					bestSignal = result;
			}

			String message = String.format("%s networks found. %s is the strongest.", wifiList.size(), bestSignal.SSID);
			// Toast.makeText(c, message, Toast.LENGTH_LONG).show();
			List<Map<String, Object>> contents = new ArrayList<Map<String, Object>>();

			for (ScanResult result : wifiList) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("PIC", R.drawable.pic);
				map.put("TITLE", result.SSID);
				map.put("CONTENT", result.toString());
				contents.add(map);
			}
			
			SimpleAdapter adapter = new SimpleAdapter(activity, (List<Map<String, Object>>) contents,
					R.layout.listitem, new String[] { "PIC", "TITLE", "CONTENT" }, new int[] { R.id.listitem_pic,
							R.id.listitem_title, R.id.listitem_content });

			listView.setAdapter(adapter);
			Log.d(TAG, "onReceive() message: " + message);
		}
	}
}
