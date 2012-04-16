package com.sizzo.something.wifi;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class WiFiScanReceiver extends BroadcastReceiver {
	private static final String TAG = "WiFiScanReceiver";
	private ListView listView;

	public WiFiScanReceiver(final ListView listView) {
		super();
		this.listView = listView;
		 listView.setOnItemClickListener(new OnItemClickListener() {
    		@Override
    		public void onItemClick(AdapterView<?> parent, View view,
    			int position, long id) {
    			Toast.makeText(listView.getContext(),
    				"Will popup dialog to ask the user connect to this wifi. Click ListItem Number " + position, Toast.LENGTH_LONG)
    				.show();
    		}
    	});
	}

	@Override
	public void onReceive(Context c, Intent intent) {
		List<ScanResult> wifiList = ((WifiManager) c.getSystemService(Context.WIFI_SERVICE)).getScanResults();
		ScanResult bestSignal = null;
		for (ScanResult result : wifiList) {
			if (bestSignal == null || WifiManager.compareSignalLevel(bestSignal.level, result.level) < 0)
				bestSignal = result;
		}

		String message = String.format("%s networks found. %s is the strongest.", wifiList.size(), bestSignal.SSID);
		Toast.makeText(c, message, Toast.LENGTH_LONG).show();

		listView.setAdapter(new ArrayAdapter<ScanResult>(c, android.R.layout.test_list_item , wifiList));

		Log.d(TAG, "onReceive() message: " + message);
	}

}
