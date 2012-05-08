package com.sizzo.something;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sizzo.something.menu.OptionsMenu;

public class MyRolesActivity extends Activity {
	private static String TAG = "something-sizzle";
	OptionsMenu optionsMenu;
	List<Map<String, Object>> wifiConfigurationAdapts = new ArrayList<Map<String, Object>>();
	MyArrayAdapter<Map<String, Object>> adapter;

	enum RoleType {
		FREE, VERIFIED
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.peers);
		adapter = new MyArrayAdapter<Map<String, Object>>(this, R.layout.peersitem, R.id.listitem_content,
				wifiConfigurationAdapts);
		((ListView) findViewById(R.id.peerListView)).setAdapter(adapter);
		this.initListView();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		optionsMenu = new OptionsMenu(this, menu);

		return optionsMenu.createOptionsMenu();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return optionsMenu.optionsItemSelected(item);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		this.initListView();

		super.onRestoreInstanceState(savedInstanceState);
	}
 

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}



	private void initListView() {
		final Activity activity = this;

		initCurrentMyInfo();


		initMyRolesInfo();

		((ListView) findViewById(R.id.peerListView)).setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
				if (wifiConfigurationAdapts != null && wifiConfigurationAdapts.get(position) != null) {
					Map map = wifiConfigurationAdapts.get(position);
					RoleType type = (RoleType) map.get("TYPE");
					switch (type) {
					case FREE:
						handleFreeRoleItem(activity, wifiConfigurationAdapts, position);
						break;
					case VERIFIED:
						handleVerifiedRoleItem(activity, wifiConfigurationAdapts, position);
						break;
					default:
						handleDefaultItem(activity, wifiConfigurationAdapts, position);
						break;
					}
				}
			}

			private void handleFreeRoleItem(final Activity activity, final List<Map<String, Object>> wifiConfigurationAdapts,
					final int position) {

				new AlertDialog.Builder(activity).setTitle("Dialog for me to choose different role").show();
				Map map = wifiConfigurationAdapts.get(position);
				map.put("DETAIL", "Change my role.....");
				adapter.notifyDataSetChanged();
			}



			private void handleVerifiedRoleItem(final Activity activity,
					final List<Map<String, Object>> wifiConfigurationAdapts, final int position) {
				//change my role
				Intent i = new Intent(activity, PeersActivity.class);
				i.putExtra("myCurrentRoleId", "5343" );
				activity.startActivity(i);
			}

			private void handleDefaultItem(final Activity activity,
					final List<Map<String, Object>> wifiConfigurationAdapts, final int position) {
				new AlertDialog.Builder(activity).setTitle("Dialog for default item").show();
			}

		});
	}

	private void initMyRolesInfo() {
		Map<String, Object> map;
		// proximity peers
		for (int i = 0; i < 10; i++) {
			map = new HashMap<String, Object>();
			map.put("PIC", R.drawable.pic);
			map.put("TITLE", "Role 00" + i);
			map.put("DETAIL", "主页：http://www.lulu00" + i + ".com.cn");
			map.put("TYPE", RoleType.VERIFIED);
			wifiConfigurationAdapts.add(map);
			adapter.notifyDataSetChanged();
		}
	}

	private void initCurrentMyInfo() {
		// my current login info
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("PIC", R.drawable.pic);
		map.put("TITLE", "#5桌 Conan Chen");
		map.put("DETAIL", "主页：http://www.conanchen.com.cn");
		map.put("TYPE", RoleType.FREE);
		wifiConfigurationAdapts.add(map);
		adapter.notifyDataSetChanged();
	}

	private class MyArrayAdapter<T> extends ArrayAdapter<T> {

		public MyArrayAdapter(Context context, int resource, int textViewResourceId, List<T> objects) {
			super(context, resource, textViewResourceId, objects);
		}

		@SuppressWarnings("unchecked")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View itemView = super.getView(position, convertView, parent);
			ImageView imageView = (ImageView) itemView.findViewById(R.id.listitem_pic);
			imageView.setImageResource((position % 2 == 0) ? R.drawable.logo : R.drawable.pic);
			TextView titleView = (TextView) itemView.findViewById(R.id.listitem_title);
			Map<String, Object> item = (Map<String, Object>) getItem(position);
			titleView.setText((CharSequence) item.get("TITLE"));
			TextView contentView = (TextView) itemView.findViewById(R.id.listitem_content);
			contentView.setText((CharSequence) item.get("DETAIL"));
			return itemView;
		}
	}

}
