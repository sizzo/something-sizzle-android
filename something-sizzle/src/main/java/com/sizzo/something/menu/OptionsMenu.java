package com.sizzo.something.menu;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.sizzo.something.BrowseActivity;
import com.sizzo.something.MainActivity;
import com.sizzo.something.R;

public class OptionsMenu {
	private static String TAG = "something-sizzle";
	private Activity activity;
	private Menu menu;

	public OptionsMenu(Activity activity, Menu menu) {
		this.activity = activity;
		this.menu = menu;
	}

	public boolean createOptionsMenu() {
		MenuInflater inflater = activity.getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;

	}

	public boolean optionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.dynamicUserHome) {
			Intent i = new Intent(activity, BrowseActivity.class);
			activity.startActivity(i);
			// launchBrowser();
		} else if (item.getItemId() == R.id.wifis) {
			Intent i = new Intent(activity, MainActivity.class);
			activity.startActivity(i);
			// ((WifiManager)
			// getSystemService(Context.WIFI_SERVICE)).startScan();
		} else if (item.getItemId() == R.id.history) {
		}
		Log.i(TAG, "Selected Menu Item=[" + item.getTitle() + "]");
		return false;
	}

}
