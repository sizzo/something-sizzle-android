package com.sizzo.something;

import android.app.Application;
import android.util.Log;

public class SizzoApplication extends Application {
	private static String TAG = "SizzoApplication";

	@Override
	public void onCreate() {
		Log.i(TAG, "SizzoApplication onCreate()....");
		super.onCreate();
	}

}
