package com.sizzo.something;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

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
import android.widget.Toast;

import com.sizzo.something.menu.OptionsMenu;

public class WifiActivity extends Activity {

	private static String TAG = "something-sizzle";
	OptionsMenu optionsMenu;
	private BroadcastReceiver wifiScanReceiver;
	private WifiManager wifiManager;
	private JmDNS jmDNS=null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		registerWifiScanReceiver();

		startJmDNS();

	}

	private void stopJmDNS() {
		try {
			if (jmDNS != null) {
				jmDNS.close();
				jmDNS = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void startJmDNS() {
		try {
			if (jmDNS == null) {
				jmDNS = JmDNS.create();
	            jmDNS.addServiceListener("_touch._tcp.local.", new JmDNSListener(this));
	            jmDNS.addServiceListener("_http._tcp.local.", new JmDNSListener(this));
	            jmDNS.addServiceListener("_ssh._tcp.local.", new JmDNSListener(this));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void registerJmDNSServices(){
        Random random = new Random();
        int id = random.nextInt(100000);


        final HashMap<String, String> values = new HashMap<String, String>();
        values.put("DvNm", "Android-" + id);
        values.put("RemV", "10000");
        values.put("DvTy", "iPod");
        values.put("RemN", "Remote");
        values.put("txtvers", "1");
        byte[] pair = new byte[8];
        random.nextBytes(pair);
        values.put("Pair", toHex(pair));

        byte[] name = new byte[20];
        random.nextBytes(name);
//        System.out.println("Requesting pairing for " + toHex(name));
        ServiceInfo pairservice = ServiceInfo.create("_touch._tcp.local.", toHex(name), 1025, 0, 0, values);
        try {
			jmDNS.registerService(pairservice);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

    private static final char[] _nibbleToHex = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    private static String toHex(byte[] code) {
        StringBuilder result = new StringBuilder(2 * code.length);

        for (int i = 0; i < code.length; i++) {
            int b = code[i] & 0xFF;
            result.append(_nibbleToHex[b / 16]);
            result.append(_nibbleToHex[b % 16]);
        }

        return result.toString();
    }

	   class JmDNSListener implements ServiceListener {
		private Activity activity;
		public JmDNSListener(WifiActivity wifiActivity) {
			this.activity = wifiActivity;
		}

		@Override
		public void serviceAdded(ServiceEvent event) {
//			Toast.makeText(this.activity, "Service added   : " + event.getName() + "." + event.getType(),5000).show();
			Log.i(TAG, "Service added   : " + event.getName() + "." + event.getType());
		}

		@Override
		public void serviceRemoved(ServiceEvent event) {
			Toast.makeText(this.activity, "Service removed : " + event.getName() + "." + event.getType(),5000).show();
		}

		@Override
		public void serviceResolved(ServiceEvent event) {
			Toast.makeText(this.activity, "Service resolved: " + event.getInfo(),5000).show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		optionsMenu = new OptionsMenu(this, menu);
		return optionsMenu.createOptionsMenu();
	}

	@Override
	protected void onPause() {
		unregisterWifiScanReceiver();

		stopJmDNS();

		super.onPause();
	}

	@Override
	protected void onRestart() {
		registerWifiScanReceiver();

		startJmDNS();
		
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
