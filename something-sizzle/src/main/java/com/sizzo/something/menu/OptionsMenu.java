package com.sizzo.something.menu;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.sizzo.something.BrowserActivity;
import com.sizzo.something.HistoryActivity;
import com.sizzo.something.PeersActivity;
import com.sizzo.something.R;
import com.sizzo.something.SettingsActivity;

public class OptionsMenu {
	private static String TAG = "OptionsMenu";
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
		if (item.getItemId() == R.id.peers) {
			Intent i = new Intent(activity, PeersActivity.class);
//			i.putExtra("url", "http://wap.sohu.com" );
			activity.startActivity(i);
		} else if (item.getItemId() == R.id.history) {
			Intent i = new Intent(activity, HistoryActivity.class);
			activity.startActivity(i);
		} else if (item.getItemId() == R.id.settings) {
			Intent i = new Intent(activity, SettingsActivity.class);
			i.putExtra("url", "file:///android_asset/html/setting.html" );
			activity.startActivity(i);
		} 
		Log.i(TAG, "Selected Menu Item=[" + item.getTitle() + "]");
		return false;
	}
}
