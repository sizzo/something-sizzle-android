package com.sizzo.something;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sizzo.something.menu.OptionsMenu;

public class PeersActivity extends Activity {
	private static String TAG = "something-sizzle";
	OptionsMenu optionsMenu;
	private BroadcastReceiver wifiStateReceiver;
	private WifiManager wifiManager;
	WifiConnect wifiConnect;
	List<Map<String, Object>> wifiConfigurationAdapts = new ArrayList<Map<String, Object>>();
	MyArrayAdapter<Map<String, Object>> adapter;
	Map<String, Object> currentWifiInfoMap = new HashMap<String, Object>();

	enum ItemType {
		ME, WIFI, PEER
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.peers);
		adapter = new MyArrayAdapter<Map<String, Object>>(this, R.layout.peersitem, R.id.listitem_content,
				wifiConfigurationAdapts);
		((ListView) findViewById(R.id.peerListView)).setAdapter(adapter);
		this.initListView();
		OpenWifi();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		optionsMenu = new OptionsMenu(this, menu);

		return optionsMenu.createOptionsMenu();
	}

	@Override
	protected void onPause() {
		unregisterWifiStateReceiver();
		super.onPause();
	}

	@Override
	protected void onRestart() {
		registerWifiStateReceiver();
		super.onRestart();
		OpenWifi();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return optionsMenu.optionsItemSelected(item);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		this.initListView();

		registerWifiStateReceiver();
		super.onRestoreInstanceState(savedInstanceState);
		OpenWifi();
	}

	private boolean OpenWifi() {
		boolean bRet = true;
		if (!this.getWifiManager().isWifiEnabled()) {
			bRet = this.getWifiManager().setWifiEnabled(true);
		}
		WifiInfo currentWifiInfo = this.getWifiManager().getConnectionInfo();
		if (currentWifiInfo == null || currentWifiInfo.getSupplicantState() != null
				&& currentWifiInfo.getSupplicantState() != SupplicantState.COMPLETED) {
			Intent i = new Intent(Settings.ACTION_WIFI_SETTINGS);
			PendingIntent pending = PendingIntent.getBroadcast(this.getApplicationContext(), 0, i,
					PendingIntent.FLAG_CANCEL_CURRENT);
			((AlarmManager) this.getSystemService(Context.ALARM_SERVICE)).setInexactRepeating(AlarmManager.RTC_WAKEUP,
					Calendar.getInstance().getTimeInMillis(), 1, pending);
			Toast.makeText(this.getApplicationContext(), "Starting Wifi....wait....", 50).show();
		}
		return bRet;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		unregisterWifiStateReceiver();
		super.onSaveInstanceState(outState);
	}

	private void registerWifiStateReceiver() {
		if (wifiStateReceiver == null) {
			wifiStateReceiver = new WiFiStateReceiver();
			registerReceiver(wifiStateReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
		}
	}

	private WifiManager getWifiManager() {
		if (wifiManager == null) {
			wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		}
		return wifiManager;
	}

	private void unregisterWifiStateReceiver() {
		if (wifiStateReceiver != null) {
			unregisterReceiver(wifiStateReceiver);
			wifiStateReceiver = null;
		}
	}

	private void initListView() {
		final Activity activity = this;

		initCurrentMyInfo();

		initCurrentWifiInfo();

		initCurrentPeersInfo();

		((ListView) findViewById(R.id.peerListView)).setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
				if (wifiConfigurationAdapts != null && wifiConfigurationAdapts.get(position) != null) {
					Map map = wifiConfigurationAdapts.get(position);
					ItemType type = (ItemType) map.get("TYPE");
					switch (type) {
					case ME:
						handleMeItem(activity, wifiConfigurationAdapts, position);
						break;
					case WIFI:
						handleWifiItem(activity, wifiConfigurationAdapts, position);
						break;
					case PEER:
						handlePeerItem(activity, wifiConfigurationAdapts, position);
						break;
					default:
						handleDefaultItem(activity, wifiConfigurationAdapts, position);
						break;
					}
				}
			}

			private void handleMeItem(final Activity activity, final List<Map<String, Object>> wifiConfigurationAdapts,
					final int position) {

				Intent i = new Intent(activity, MyRolesActivity.class);
				i.putExtra("url", "http://m.hao123.com?q=" + currentWifiInfoMap.get("SSID"));
				activity.startActivity(i);
				
				Map map = wifiConfigurationAdapts.get(position);
				map.put("DETAIL", "Change my role.....");
				adapter.notifyDataSetChanged();
			}

			private void handleWifiItem(final Activity activity,
					final List<Map<String, Object>> wifiConfigurationAdapts, final int position) {
				if (currentWifiInfoMap != null) {
					Intent i = new Intent(activity, BrowserActivity.class);
					i.putExtra("url", "http://m.hao123.com?q=" + currentWifiInfoMap.get("SSID"));
					activity.startActivity(i);
				}
			}

			private void handlePeerItem(final Activity activity,
					final List<Map<String, Object>> wifiConfigurationAdapts, final int position) {
				Intent i = new Intent(activity, PeerChatActivity.class);
				activity.startActivity(i);
			}

			private void handleDefaultItem(final Activity activity,
					final List<Map<String, Object>> wifiConfigurationAdapts, final int position) {
				new AlertDialog.Builder(activity).setTitle("Dialog for default item").show();
			}

		});
	}

	private void initCurrentPeersInfo() {
		Map<String, Object> map;
		// proximity peers
		for (int i = 0; i < 10; i++) {
			map = new HashMap<String, Object>();
			map.put("PIC", R.drawable.pic);
			map.put("TITLE", "Lulu 00" + i);
			map.put("DETAIL", "主页：http://www.lulu00" + i + ".com.cn");
			map.put("TYPE", ItemType.PEER);
			wifiConfigurationAdapts.add(map);
			adapter.notifyDataSetChanged();
		}
	}

	private void initCurrentMyInfo() {
		// my current login info
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("PIC", R.drawable.pic);
		map.put("TITLE", "#5桌 Conan Chen");
		map.put("DETAIL", "主页：http://www.conanchen.com.cn");
		map.put("TYPE", ItemType.ME);
		wifiConfigurationAdapts.add(map);
		adapter.notifyDataSetChanged();
	}

	private void initCurrentWifiInfo() {
		currentWifiInfoMap.put("TYPE", ItemType.WIFI);
		wifiConfigurationAdapts.add(currentWifiInfoMap);
		updateCurrentWifiInfo();
	}

	private void updateCurrentWifiInfo() {
		WifiInfo currentWifiConnection = getWifiManager().getConnectionInfo();
		if (currentWifiConnection != null) {
			currentWifiInfoMap.put("PIC", R.drawable.pic);
			currentWifiInfoMap.put("TITLE", currentWifiConnection.getBSSID());
			currentWifiInfoMap.put("DETAIL",
					"Elevent香港茶餐廳主页：http://www.conanchen.com.cn" + currentWifiConnection.toString());
		}
		// current wifi info
		adapter.notifyDataSetChanged();
	}

	private class MyArrayAdapter<T> extends ArrayAdapter<T> {

		public MyArrayAdapter(Context context, int resource, int textViewResourceId, List<T> objects) {
			super(context, resource, textViewResourceId, objects);
		}

		@SuppressWarnings("unchecked")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View itemView = super.getView(position, convertView, parent);
			ImageView imageView = (ImageView) itemView.findViewById(R.id.listitem_pic);
			imageView.setImageResource((position % 2 == 0) ? R.drawable.logo : R.drawable.pic);
			TextView titleView = (TextView) itemView.findViewById(R.id.listitem_title);
			Map<String, Object> item = (Map<String, Object>) getItem(position);
			titleView.setText((CharSequence) item.get("TITLE"));
			TextView contentView = (TextView) itemView.findViewById(R.id.listitem_content);
			contentView.setText((CharSequence) item.get("DETAIL"));
			return itemView;
		}
	}

	private class WiFiStateReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context c, Intent intent) {
			updateCurrentWifiInfo();
		}
	}
}
