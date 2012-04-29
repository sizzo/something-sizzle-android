package com.sizzo.something;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.sizzo.something.data.SizzoContentProvider;
import com.sizzo.something.data.SizzoSchema;
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
			wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
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
		private final Activity activity;
		List<Map<String, Object>> wifiConfigurationAdapts = null;

		public WiFiScanReceiver(final Activity activity, final ListView listView) {
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
			if (wifiConfigurationAdapts != null && wifiConfigurationAdapts.get(position) != null) {
				Intent i = new Intent(activity, BrowserActivity.class);
				i.putExtra("url", "http://m.hao123.com?q=" + wifiConfigurationAdapts.get(position).get("SSID"));
				activity.startActivity(i);
			}
		}

		@Override
		public void onReceive(Context c, Intent intent) {
			TextView wifiCurrentView = (TextView) findViewById(R.id.wifiCurrentView);
			WifiInfo currentWifiConnection = wifiManager.getConnectionInfo();
			if (currentWifiConnection != null) {
				wifiCurrentView.setText("You are connecting to WIFI: macAddress="
						+ currentWifiConnection.getMacAddress() + " BSSID=" + currentWifiConnection.getBSSID()
						+ " sSID=" + currentWifiConnection.getSSID() + " supplicantState="
						+ currentWifiConnection.getSupplicantState());

				ContentValues editedValues = new ContentValues();
				editedValues.put(SizzoSchema.Content.UID.name(), currentWifiConnection.getBSSID());
				editedValues.put(SizzoSchema.Content.TITLE.name(), currentWifiConnection.getSSID());
				editedValues.put(SizzoSchema.Content.DETAIL.name(), currentWifiConnection.toString());
				getContentResolver().update(ContentUris.withAppendedId(SizzoContentProvider.WIFIS_URI, 2), editedValues,
						null, null);
			} else {
				wifiCurrentView.setText("You are not connecting to any WIFI");
			}

			populateWifiListView(c);

		}

		private void populateWifiListView(Context context) {
			wifiConfigurationAdapts = new ArrayList<Map<String, Object>>();
			List<ScanResult> wifiList = ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).getScanResults();
			for (ScanResult result : wifiList) {
				ContentValues values = new ContentValues();
				
				values.put(SizzoSchema.Content.UID.name(), result.BSSID);
				values.put(SizzoSchema.Content.TITLE.name(), result.SSID);
				values.put(SizzoSchema.Content.DETAIL.name(), result.toString());
				Uri uri = getContentResolver().insert(SizzoContentProvider.WIFIS_URI, values);
				values.clear();
			}


			Cursor cursor = managedQuery(SizzoContentProvider.WIFIS_URI, null, null, null, SizzoSchema.Content.TITLE.name()+" desc");
			if (cursor.moveToFirst()) {
				do {
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("PIC", R.drawable.pic);
					map.put("TITLE", cursor.getString(cursor.getColumnIndex(SizzoSchema.Content.TITLE.name())));
					map.put("DETAIL", cursor.getString(cursor.getColumnIndex(SizzoSchema.Content.DETAIL.name())));
					wifiConfigurationAdapts.add(map);
				} while (cursor.moveToNext());
			}
			SimpleAdapter adapter = new SimpleAdapter(activity, (List<Map<String, Object>>) wifiConfigurationAdapts,
					R.layout.listitem, new String[] { "PIC", "TITLE", "DETAIL" }, new int[] { R.id.listitem_pic,
							R.id.listitem_title, R.id.listitem_content });

			listView.setAdapter(adapter);

			getContentResolver().delete(ContentUris.withAppendedId(SizzoContentProvider.WIFIS_URI, 2), null, null);

		}

	}
}
