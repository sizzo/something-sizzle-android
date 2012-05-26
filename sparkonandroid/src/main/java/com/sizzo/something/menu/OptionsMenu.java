package com.sizzo.something.menu;

import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.sizzo.something.R;

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
		Log.i(TAG, "Selected Menu Item=[" + item.getTitle() + "]");
		return false;
	}
}
