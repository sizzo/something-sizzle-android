package com.sizzo.something.wifi;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.sizzo.something.HelloAndroidActivity;

public class WiFiScanReceiver extends BroadcastReceiver {
	private static final String TAG = "WiFiScanReceiver";
	private HelloAndroidActivity helloAndroidActivity;

	public WiFiScanReceiver(HelloAndroidActivity activity) {
		super();
		this.helloAndroidActivity = activity;
		
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

		helloAndroidActivity.getListView1().setAdapter(new ArrayAdapter<ScanResult>(c, android.R.layout.test_list_item , wifiList));

		Log.d(TAG, "onReceive() message: " + message);
	}

}
