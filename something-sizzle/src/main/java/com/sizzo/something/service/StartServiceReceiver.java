package com.sizzo.something.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sizzo.something.service.jmdns.DiscoverService;

public class StartServiceReceiver extends BroadcastReceiver {
	private static final String TAG = "StartServiceReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "StartServiceReceiver.onReceive().....");
		Intent service = new Intent(context, DiscoverService.class);
		context.startService(service);
	}
}