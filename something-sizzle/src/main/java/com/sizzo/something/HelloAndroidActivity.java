package com.sizzo.something;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class HelloAndroidActivity extends Activity {

	private static final int ID_MENU_DYNAMIC_USER_HOME = 0;
	private static String TAG = "something-sizzle";

	/**
	 * Called when the activity is first created.
	 * 
	 * @param savedInstanceState
	 *            If the activity is being re-initialized after previously being
	 *            shut down then this Bundle contains the data it most recently
	 *            supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it
	 *            is null.</b>
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate");
		setContentView(R.layout.main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// creates a menu inflater
		MenuInflater inflater = getMenuInflater();
		// generates a Menu from a menu resource file
		// R.menu.main_menu represents the ID of the XML resource file
		inflater.inflate(R.menu.main_menu, menu);

		appendDynamicOptionsMenu(menu);
		return true;
	}

	private void appendDynamicOptionsMenu(Menu menu) {
		// the menu option text is defined in resources
		menu.add(Menu.NONE, ID_MENU_DYNAMIC_USER_HOME, Menu.NONE, R.string.dynamicUserHomeOption);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// check selected menu item
		// R.id.exit is @+id/exit
		if (item.getItemId() == R.id.exit) {
			// close the Activity
			this.finish();
			return true;
		} else if (item.getItemId() == ID_MENU_DYNAMIC_USER_HOME) {
			Toast.makeText(this.getApplicationContext(),
					"You just select RAINBOW menu, will redirect to rainbow home page", 30).show();
		}
		return false;
	}

}
