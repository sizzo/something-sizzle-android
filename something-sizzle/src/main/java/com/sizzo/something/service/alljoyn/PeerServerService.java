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

package com.sizzo.something.service.alljoyn;

import java.util.ArrayList;
import java.util.List;

import org.alljoyn.bus.BusAttachment;
import org.alljoyn.bus.BusListener;
import org.alljoyn.bus.BusObject;
import org.alljoyn.bus.Mutable;
import org.alljoyn.bus.SessionOpts;
import org.alljoyn.bus.SessionPortListener;
import org.alljoyn.bus.Status;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class PeerServerService extends Service {
	/* Load the native alljoyn_java library. */
	static {
		System.loadLibrary("alljoyn_java");
	}

	private static final String TAG = "SimpleService";

	private static final int MESSAGE_PING = 1;
	private static final int MESSAGE_PING_REPLY = 2;
	private static final int MESSAGE_POST_TOAST = 3;

	/* The AllJoyn object that is our service. */
	private SimpleService mSimpleService;

	/* Handler used to make calls to AllJoyn methods. See onCreate(). */
	private Handler mBusHandler;
	private HandlerThread busThread = null;
	private List<String> mListViewArrayAdapter = new ArrayList<String>();

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_PING:
				String ping = (String) msg.obj;
				mListViewArrayAdapter.add("Ping:  " + ping);
				// save to database
				break;
			case MESSAGE_PING_REPLY:
				String reply = (String) msg.obj;
				mListViewArrayAdapter.add("Reply:  " + reply);
				break;
			default:
				break;
			}
		}
	};

	@Override
	public void onCreate() {
		Log.v(TAG, "alljoyn server service Created");

		org.alljoyn.bus.alljoyn.DaemonInit.PrepareDaemon(getApplicationContext());

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.v(TAG, "Peer Server service -- onStartCommand()");
		startService();
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}

	private void startService() {
		/*
		 * Make all AllJoyn calls through a separate handler thread to prevent
		 * blocking the UI.
		 */
		if (busThread == null) {
			busThread = new HandlerThread("BusHandler");
		}
		if (!busThread.isAlive()) {
			busThread.start();
		}
		if (mBusHandler == null) {
			mBusHandler = new BusHandler(busThread.getLooper());
		}
		if (mSimpleService == null) {
			/* Start our service. */
			mSimpleService = new SimpleService();
		}
		mBusHandler.sendEmptyMessage(BusHandler.CONNECT);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		/* Disconnect to prevent any resource leaks. */
		mBusHandler.sendEmptyMessage(BusHandler.DISCONNECT);
	}

	/* The class that is our AllJoyn service. It implements the SimpleInterface. */
	class SimpleService implements SimpleInterface, BusObject {

		/*
		 * This is the code run when the client makes a call to the Ping method
		 * of the SimpleInterface. This implementation just returns the received
		 * String to the caller.
		 * 
		 * This code also prints the string it received from the user and the
		 * string it is returning to the user to the screen.
		 */
		public String Ping(String inStr) {
			String pongStr = "PeerServerService Pong:" + inStr;
			sendUiMessage(MESSAGE_PING, inStr);

			/* Simply echo the ping message. */
			sendUiMessage(MESSAGE_PING_REPLY, pongStr);
			return pongStr;
		}

		/* Helper function to send a message to the UI thread. */
		private void sendUiMessage(int what, Object obj) {
			mHandler.sendMessage(mHandler.obtainMessage(what, obj));
		}
	}

	/* This class will handle all AllJoyn calls. See onCreate(). */
	class BusHandler extends Handler {

		private BusAttachment mBus = null;

		/* These are the messages sent to the BusHandler from the UI. */
		public static final int CONNECT = 1;
		public static final int DISCONNECT = 2;

		public BusHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			Status status;
			switch (msg.what) {
			/* Connect to the bus and start our service. */
			case CONNECT: {
				/*
				 * All communication through AllJoyn begins with a
				 * BusAttachment.
				 * 
				 * A BusAttachment needs a name. The actual name is unimportant
				 * except for internal security. As a default we use the class
				 * name as the name.
				 * 
				 * By default AllJoyn does not allow communication between
				 * devices (i.e. bus to bus communication). The second argument
				 * must be set to Receive to allow communication between
				 * devices.
				 */
				if (mBus == null) {
					mBus = new BusAttachment(getPackageName(), BusAttachment.RemoteMessage.Receive);

					/*
					 * Create a bus listener class
					 */
					mBus.registerBusListener(new BusListener());
					/*
					 * To make a service available to other AllJoyn peers, first
					 * register a BusObject with the BusAttachment at a specific
					 * path.
					 * 
					 * Our service is the SimpleService BusObject at the
					 * "/SimpleService" path.
					 */
					status = mBus.registerBusObject(mSimpleService, "/SimpleService");
					logStatus("BusAttachment.registerBusObject()", status);
					if (status != Status.OK) {
						return;
					}

					/*
					 * The next step in making a service available to other
					 * AllJoyn peers is to connect the BusAttachment to the bus
					 * with a well-known name.
					 */
					/*
					 * connect the BusAttachement to the bus
					 */
					status = mBus.connect();
					logStatus("BusAttachment.connect()", status);
					if (status != Status.OK) {
						return;
					}

					/*
					 * Create a new session listening on the contact port of the
					 * chat service.
					 */
					Mutable.ShortValue contactPort = new Mutable.ShortValue(SimpleInterface.CONTACT_PORT);

					SessionOpts sessionOpts = new SessionOpts();
					sessionOpts.traffic = SessionOpts.TRAFFIC_MESSAGES;
					sessionOpts.isMultipoint = false;
					sessionOpts.proximity = SessionOpts.PROXIMITY_ANY;
					sessionOpts.transports = SessionOpts.TRANSPORT_ANY;

					status = mBus.bindSessionPort(contactPort, sessionOpts, new SessionPortListener() {
						@Override
						public boolean acceptSessionJoiner(short sessionPort, String joiner, SessionOpts sessionOpts) {
							if (sessionPort == SimpleInterface.CONTACT_PORT) {
								return true;
							} else {
								return false;
							}
						}
					});
					logStatus(
							String.format("BusAttachment.bindSessionPort(%d, %s)", contactPort.value,
									sessionOpts.toString()), status);
					if (status != Status.OK) {
						return;
					}

					/*
					 * request a well-known name from the bus
					 */
					int flag = BusAttachment.ALLJOYN_REQUESTNAME_FLAG_REPLACE_EXISTING
							| BusAttachment.ALLJOYN_REQUESTNAME_FLAG_DO_NOT_QUEUE;

					status = mBus.requestName(SimpleInterface.SERVICE_NAME, flag);
					logStatus(
							String.format("BusAttachment.requestName(%s, 0x%08x)", SimpleInterface.SERVICE_NAME, flag),
							status);
					if (status == Status.OK) {
						/*
						 * If we successfully obtain a well-known name from the
						 * bus advertise the same well-known name
						 */
						status = mBus.advertiseName(SimpleInterface.SERVICE_NAME, SessionOpts.TRANSPORT_ANY);
						logStatus(String.format("BusAttachement.advertiseName(%s)", SimpleInterface.SERVICE_NAME),
								status);
						if (status != Status.OK) {
							/*
							 * If we are unable to advertise the name, release
							 * the well-known name from the local bus.
							 */
							status = mBus.releaseName(SimpleInterface.SERVICE_NAME);
							logStatus(String.format("BusAttachment.releaseName(%s)", SimpleInterface.SERVICE_NAME),
									status);
							return;
						}
					}
				}

				break;
			}

				/* Release all resources acquired in connect. */
			case DISCONNECT: {
				/*
				 * It is important to unregister the BusObject before
				 * disconnecting from the bus. Failing to do so could result in
				 * a resource leak.
				 */
				mBus.unregisterBusObject(mSimpleService);
				mBus.disconnect();
				mBusHandler.getLooper().quit();
				mBus = null;
				mBusHandler = null;
				mSimpleService = null;
				break;
			}

			default:
				break;
			}
		}
	}

	private void logStatus(String msg, Status status) {
		String log = String.format("%s: %s", msg, status);
		if (status == Status.OK) {
			Log.i(TAG, log);
		} else {
			Message toastMsg = mHandler.obtainMessage(MESSAGE_POST_TOAST, log);
			mHandler.sendMessage(toastMsg);
			Log.e(TAG, log);
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}
