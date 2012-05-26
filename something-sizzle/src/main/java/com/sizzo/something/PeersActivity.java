package com.sizzo.something;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

import com.sizzo.something.menu.OptionsMenu;

public class PeersActivity extends Activity {
	private static String TAG = "PeersActivity";
	OptionsMenu optionsMenu;
	List<Map<String, Object>> listDataAdapts = new ArrayList<Map<String, Object>>();
	MyArrayAdapter<Map<String, Object>> adapter;

	enum ItemType {
		ME, WIFI, PEER
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.peers);
		adapter = new MyArrayAdapter<Map<String, Object>>(this, R.layout.peersitem, R.id.listitem_content,
				listDataAdapts);
		((ListView) findViewById(R.id.peerListView)).setAdapter(adapter);
		this.initListView();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		optionsMenu = new OptionsMenu(this, menu);

		return optionsMenu.createOptionsMenu();
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



		initCurrentPeersInfo();

		((ListView) findViewById(R.id.peerListView)).setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
				if (listDataAdapts != null && listDataAdapts.get(position) != null) {
					Map map = listDataAdapts.get(position);
					ItemType type = (ItemType) map.get("TYPE");
					switch (type) {
					case PEER:
						handlePeerItem(activity, listDataAdapts, position);
						break;
					default:
						handleDefaultItem(activity, listDataAdapts, position);
						break;
					}
				}
			}

			private void handlePeerItem(final Activity activity, final List<Map<String, Object>> listDataAdapts,
					final int position) {
				Intent i = new Intent(activity, BrowserActivity.class);
				i.putExtra("url", "http://m.hao123.com?q=" );
				activity.startActivityIfNeeded(i,Intent.FLAG_ACTIVITY_NEW_TASK);
			}

			private void handleDefaultItem(final Activity activity, final List<Map<String, Object>> listDataAdapts,
					final int position) {
				new AlertDialog.Builder(activity).setTitle("Dialog for default item").show();
			}

		});
	}

	private void initCurrentPeersInfo() {
		Map<String, Object> map;
		// proximity peers
		for (int i = 0; i < 10; i++) {
			map = new HashMap<String, Object>();
			map.put("PIC", R.drawable.pic);
			map.put("TITLE", "Lulu 00" + i);
			map.put("DETAIL", "涓婚〉锛歨ttp://www.lulu00" + i
					+ ".com.cn 绠�粙绠�粙绠�粙绠�粙绠�粙绠�粙绠�粙绠�粙绠�粙绠�粙绠�粙绠�粙绠�粙绠�粙绠�粙绠�粙绠�粙绠�粙绠�粙浠嬬畝浠嬬畝浠嬬畝浠嬬畝浠嬬畝浠嬬畝浠嬬畝浠嬬畝浠嬬畝浠嬬畝浠嬬畝浠嬬畝浠嬬畝浠嬬畝浠");
			map.put("TYPE", ItemType.PEER);
			listDataAdapts.add(map);
			adapter.notifyDataSetChanged();
		}
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
