package com.sizzo.something;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.sizzo.something.data.SizzoSchema;
import com.sizzo.something.data.SizzoUriMatcher;
import com.sizzo.something.menu.OptionsMenu;

public class WifiActivity extends Activity {

	private static String TAG = "something-sizzle";
	OptionsMenu optionsMenu;
	private BroadcastReceiver wifiScanReceiver;
	private WifiManager wifiManager;
	WifiConnect wifiConnect;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		initView();
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
		initView();

		registerWifiScanReceiver();
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		unregisterWifiScanReceiver();
		super.onSaveInstanceState(outState);
	}

	private void initView() {
		this.initListView();
		this.updateWifiCurrentView();
	}

	private void registerWifiScanReceiver() {
		if (wifiScanReceiver == null) {
			wifiScanReceiver = new WiFiScanReceiver();
			registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		}
	}

	private WifiManager getWifiManager() {
		if (wifiManager == null) {
			wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		}
		return wifiManager;
	}

	private WifiConnect getWifiConnect() {
		if (wifiConnect == null) {
			wifiConnect = new WifiConnect(getWifiManager());
		}
		return wifiConnect;
	}

	private void unregisterWifiScanReceiver() {
		if (wifiScanReceiver != null) {
			unregisterReceiver(wifiScanReceiver);
			wifiScanReceiver = null;
		}
	}

	private void initListView() {
		final Activity activity = this;
		final List<Map<String, Object>> wifiConfigurationAdapts = new ArrayList<Map<String, Object>>();

		Cursor cursor = managedQuery(SizzoUriMatcher.WIFIS_URI, null, null, null, SizzoSchema.CONTENTS.Columns.TITLE
				+ " desc");
		if (cursor.moveToFirst()) {
			do {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("PIC", R.drawable.pic);
				map.put("TITLE", cursor.getString(cursor.getColumnIndex(SizzoSchema.CONTENTS.Columns.TITLE)));
				map.put("DETAIL", cursor.getString(cursor.getColumnIndex(SizzoSchema.CONTENTS.Columns.DETAIL)));
				wifiConfigurationAdapts.add(map);
			} while (cursor.moveToNext());
		} else {
			if (getWifiConnect().OpenWifi()) {
				getWifiConnect().startScan();
			}
		}
		SimpleAdapter adapter = new SimpleAdapter(this, (List<Map<String, Object>>) wifiConfigurationAdapts,
				R.layout.listitem, new String[] { "PIC", "TITLE", "DETAIL" }, new int[] { R.id.listitem_pic,
						R.id.listitem_title, R.id.listitem_content });

		((ListView) findViewById(R.id.wifiListView)).setAdapter(adapter);
		((ListView) findViewById(R.id.wifiListView)).setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
				if (wifiConfigurationAdapts != null && wifiConfigurationAdapts.get(position) != null) {
					final String ssid = (String) wifiConfigurationAdapts.get(position).get("TITLE");
					final EditText editText = new EditText(activity);
					new AlertDialog.Builder(activity).setTitle(ssid + "\n  Please enter password")
					// .setIcon(android.R.drawable.ic_dialog_info)
							.setView(editText).setPositiveButton("Connect", new OnClickListener() {
								public void onClick(DialogInterface arg0, int arg1) {
									String password = editText.getText().toString();
									if (getWifiConnect().Connect(ssid, password,
											WifiConnect.WifiCipherType.WIFICIPHER_WPA)) {
										updateWifiCurrentView();
										getWifiConnect().startScan();
										
									}
								}
							}).setNeutralButton("Home", new OnClickListener() {
								public void onClick(DialogInterface arg0, int arg1) {
									launchBrowser(position);
								}
							}).setNegativeButton("Disconnect", new OnClickListener() {
								public void onClick(DialogInterface arg0, int arg1) {
									if(getWifiManager().setWifiEnabled(false)){
										updateWifiCurrentView();
										Intent i = new Intent(Settings.ACTION_WIFI_SETTINGS);
										activity.startActivity(i);
									}
								}
							}).show();
				}
			}

			protected void launchBrowser(int position) {
				if (wifiConfigurationAdapts != null && wifiConfigurationAdapts.get(position) != null) {
					Intent i = new Intent(activity, BrowserActivity.class);
					i.putExtra("url", "http://m.hao123.com?q=" + wifiConfigurationAdapts.get(position).get("SSID"));
					activity.startActivity(i);
				}
			}
		});
	}

	private void updateWifiCurrentView() {
		TextView wifiCurrentView = (TextView) findViewById(R.id.wifiCurrentView);
		WifiInfo currentWifiConnection = getWifiManager().getConnectionInfo();
		if (currentWifiConnection != null) {
			wifiCurrentView.setText("You are connecting to WIFI: macAddress=" + currentWifiConnection.getMacAddress()
					+ " BSSID=" + currentWifiConnection.getBSSID() + " sSID=" + currentWifiConnection.getSSID()
					+ " supplicantState=" + currentWifiConnection.getSupplicantState());

			ContentValues editedValues = new ContentValues();
			editedValues.put(SizzoSchema.CONTENTS.Columns.UID, currentWifiConnection.getBSSID());
			editedValues.put(SizzoSchema.CONTENTS.Columns.TITLE, currentWifiConnection.getSSID());
			editedValues.put(SizzoSchema.CONTENTS.Columns.DETAIL, currentWifiConnection.toString());
			getContentResolver().update(Uri.parse("content://" + SizzoUriMatcher.AUTHORITY + "/wifis/u/" + currentWifiConnection.getBSSID()), editedValues, null,
					null);
		} else {
			wifiCurrentView.setText("You are not connecting to any WIFI");
		}
	}

	private class WiFiScanReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context c, Intent intent) {
			updateWifiCurrentView();
			saveScanWifis(c);
		}

		private void saveScanWifis(Context context) {
			List<ScanResult> wifiList = ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).getScanResults();
			for (ScanResult result : wifiList) {
				ContentValues values = new ContentValues();

				values.put(SizzoSchema.CONTENTS.Columns.UID, result.BSSID);
				values.put(SizzoSchema.CONTENTS.Columns.TITLE, result.SSID);
				values.put(SizzoSchema.CONTENTS.Columns.DETAIL, result.toString());
				Uri uri = getContentResolver().insert(SizzoUriMatcher.WIFIS_URI_ID, values);
				values.clear();
			}
		}

	}
}
