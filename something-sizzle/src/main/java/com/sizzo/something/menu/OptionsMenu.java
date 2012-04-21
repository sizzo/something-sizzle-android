package com.sizzo.something.menu;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.sizzo.something.BrowserActivity;
import com.sizzo.something.WifiActivity;
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
			Intent i = new Intent(activity, BrowserActivity.class);
			activity.startActivity(i);
		} else if (item.getItemId() == R.id.wifis) {
			Intent i = new Intent(activity, WifiActivity.class);
			activity.startActivity(i);
		} else if (item.getItemId() == R.id.history) {
		}
		Log.i(TAG, "Selected Menu Item=[" + item.getTitle() + "]");
		return false;
	}
}
