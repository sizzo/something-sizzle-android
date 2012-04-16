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

import com.sizzo.something.HelloAndroidActivity;

public class WiFiScanReceiver extends BroadcastReceiver {
	private static final String TAG = "WiFiScanReceiver";
	private HelloAndroidActivity helloAndroidActivity;
	private ListView listView1;

	public WiFiScanReceiver(HelloAndroidActivity activity) {
		super();
		this.helloAndroidActivity = activity;
		listView1 = this.helloAndroidActivity.getListView1();
		listView1.setOnItemClickListener(new OnItemClickListener() {
    		@Override
    		public void onItemClick(AdapterView<?> parent, View view,
    			int position, long id) {
    			Toast.makeText(helloAndroidActivity.getApplicationContext(),
    				"Will popup dialog to ask the user connect to this wifi. Click ListItem Number " + position, Toast.LENGTH_LONG)
    				.show();
    		}
    	});
	}

	@Override
	public void onReceive(Context c, Intent intent) {
		List<ScanResult> wifiList = helloAndroidActivity.getWifiManager().getScanResults();
		ScanResult bestSignal = null;
		for (ScanResult result : wifiList) {
			if (bestSignal == null || WifiManager.compareSignalLevel(bestSignal.level, result.level) < 0)
				bestSignal = result;
		}

		String message = String.format("%s networks found. %s is the strongest.", wifiList.size(), bestSignal.SSID);
		Toast.makeText(c, message, Toast.LENGTH_LONG).show();

		listView1.setAdapter(new ArrayAdapter<ScanResult>(c, android.R.layout.test_list_item , wifiList));

		Log.d(TAG, "onReceive() message: " + message);
	}

}
