/*
 * Copyright 2010-2011, Qualcomm Innovation Center, Inc.
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.sizzo.something;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sizzo.something.service.alljoyn.PeerClientService;

public class PeerChatActivity extends Activity {
	/* Load the native alljoyn_java library. */
	static {
		System.loadLibrary("alljoyn_java");
	}

	private static final int MESSAGE_PING = 1;
	private static final int MESSAGE_PING_REPLY = 2;
	private static final int MESSAGE_POST_TOAST = 3;
	private static final int MESSAGE_START_PROGRESS_DIALOG = 4;
	private static final int MESSAGE_STOP_PROGRESS_DIALOG = 5;

	private static final String TAG = "PeerChatActivity";

	private EditText mEditText;
	private ArrayAdapter<String> mListViewArrayAdapter;
	private ListView mListView;
	private Menu menu;

	private ProgressDialog mDialog;

	/** Messenger for communicating with the service. */
	Messenger mMessenger = null;

	/** Flag indicating whether we have called bind on the service. */
	boolean mBound;

	/**
	 * Class for interacting with the main interface of the service.
	 */
	private ServiceConnection mServiceConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			// This is called when the connection with the service has been
			// established, giving us the object we can use to
			// interact with the service. We are communicating with the
			// service using a Messenger, so here we get a client-side
			// representation of that from the raw IBinder object.
			mMessenger = new Messenger(service);
			mBound = true;
		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			mMessenger = null;
			mBound = false;
		}
	};
	private Handler handler = new Handler() {
		public void handleMessage(Message message) {
			Bundle data = message.getData();
			if (message.arg1 == RESULT_OK && data != null) {
				String text = data.getString(PeerClientService.RESULTPATH);
				Toast.makeText(PeerChatActivity.this, text, Toast.LENGTH_LONG).show();
			}
		}
	};

	protected void onResume() {
		super.onResume();
		Toast.makeText(this, "OnResume called", Toast.LENGTH_SHORT).show();
		Intent intent = null;
		intent = new Intent(this, PeerClientService.class);
		// Create a new Messenger for the communication back
		// From the Service to the Activity
		Messenger messenger = new Messenger(handler);
		intent.putExtra("MESSENGER", messenger);

		bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onPause() {
		super.onPause();
		unbindService(mServiceConnection);
	}

	private void ping(TextView view) {
		if (!mBound)
			return;
		// Create and send a message to the service, using a supported 'what'
		// value
		Message msg = Message.obtain(null, PeerClientService.MESSAGE_PING, 0, 0);
		try {
			Bundle bundle = new Bundle();
			bundle.putString(PeerClientService.FILENAME, view.getText().toString());
			bundle.putString(PeerClientService.URLPATH, "http://www.vogella.com/index.html");
			msg.setData(bundle);
			mMessenger.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.peerchat);

		mListViewArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
		mListView = (ListView) findViewById(R.id.ListView);
		mListView.setAdapter(mListViewArrayAdapter);

		mEditText = (EditText) findViewById(R.id.EditText);
		mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
					ping(view);
				}
				return true;
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.chatmenu, menu);
		this.menu = menu;
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.peerHome:
			Intent i = new Intent(this, BrowserActivity.class);
			i.putExtra("url", "http://m.hao123.com?q=");
			this.startActivity(i);
			return true;
		case R.id.peerRecord:
			finish();
			return true;
		case R.id.peerCall:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
