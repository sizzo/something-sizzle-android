package com.sizzo.something.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sizzo.something.service.jmdns.DiscoverService;

public class StartServiceReceiver   extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent service = new Intent(context, DiscoverService.class);
		context.startService(service);
	}
}