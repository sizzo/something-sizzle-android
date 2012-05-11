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

import org.alljoyn.bus.BusAttachment;
import org.alljoyn.bus.BusException;
import org.alljoyn.bus.BusListener;
import org.alljoyn.bus.Mutable;
import org.alljoyn.bus.OnJoinSessionListener;
import org.alljoyn.bus.ProxyBusObject;
import org.alljoyn.bus.SessionListener;
import org.alljoyn.bus.SessionOpts;
import org.alljoyn.bus.Status;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class PeerClientService extends Service {
	/* Load the native alljoyn_java library. */
	static {
		System.loadLibrary("alljoyn_java");
	}

	public static final int MESSAGE_PING = 1;
	private static final int MESSAGE_PING_REPLY = 2;
	private static final int MESSAGE_POST_TOAST = 3;
	private static final int MESSAGE_START_PROGRESS_DIALOG = 4;
	private static final int MESSAGE_STOP_PROGRESS_DIALOG = 5;

	public static final String FILENAME = "fileName";
	public static final String URLPATH = "urlPath";
	public static final String RESULTPATH = "urlPath";

	private static final String TAG = "PeerChatActivity";

	// private EditText mEditText;
	private ArrayAdapter<String> mListViewArrayAdapter;

	/* Handler used to make calls to AllJoyn methods. See onCreate(). */
	private BusHandler mBusHandler;

	private ProgressDialog mDialog;

	private Handler mIncomingHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_PING:
				Log.e("MESSAGE", "Got message");
				Bundle data = msg.getData();
				String urlPath = data.getString(PeerClientService.URLPATH);
				String fileName = data.getString(PeerClientService.FILENAME);

				Message backMsg = Message.obtain();
				backMsg.arg1 = Activity.RESULT_OK;
				Bundle bundle = new Bundle();
				bundle.putString(RESULTPATH, "PeerClientService Pong:" + urlPath);
				backMsg.setData(bundle);

				try {
					outMessenger.send(backMsg);
				} catch (android.os.RemoteException e1) {
					Log.w(getClass().getName(), "Exception sending message", e1);
				}
				break;
			case MESSAGE_PING_REPLY:
				String ret = (String) msg.obj;
				mListViewArrayAdapter.add("Reply:  " + ret);
				// mEditText.setText("");
				break;
			case MESSAGE_POST_TOAST:
				Toast.makeText(getApplicationContext(), (String) msg.obj, Toast.LENGTH_LONG).show();
				break;
			case MESSAGE_START_PROGRESS_DIALOG:
				mDialog = ProgressDialog.show(PeerClientService.this, "", "Finding Simple Service.\nPlease wait...",
						true, true);
				break;
			case MESSAGE_STOP_PROGRESS_DIALOG:
				mDialog.dismiss();
				break;
			default:
				break;
			}
		}
	};
	// Used to receive messages from the Activity
	final Messenger inMessenger = new Messenger(mIncomingHandler);
	// Use to send message to the Activity
	private Messenger outMessenger;

	@Override
	public IBinder onBind(Intent intent) {
		Bundle extras = intent.getExtras();
		// Get messager from the Activity
		if (extras != null) {
			outMessenger = (Messenger) extras.get("MESSENGER");
		}
		// Return our messenger to the Activity to get commands
		return inMessenger.getBinder();
	}

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
		if (mBusHandler == null) {
			/*
			 * Make all AllJoyn calls through a separate handler thread to
			 * prevent blocking the UI.
			 */
			HandlerThread busThread = new HandlerThread("BusHandler");
			busThread.start();
			mBusHandler = new BusHandler(busThread.getLooper());

			/* Connect to an AllJoyn object. */
			mBusHandler.sendEmptyMessage(BusHandler.CONNECT);
			mIncomingHandler.sendEmptyMessage(MESSAGE_START_PROGRESS_DIALOG);
			Intent i = new Intent(this, PeerServerService.class);
			this.startService(i);
		}
	}

	@Override
	public void onDestroy() {
		/* Disconnect to prevent resource leaks. */
		mBusHandler.sendEmptyMessage(BusHandler.DISCONNECT);
		mBusHandler = null;
		super.onDestroy();

	}

	/* This class will handle all AllJoyn calls. See onCreate(). */
	class BusHandler extends Handler {
		/*
		 * Name used as the well-known name and the advertised name of the
		 * service this client is interested in. This name must be a unique name
		 * both to the bus and to the network as a whole.
		 * 
		 * The name uses reverse URL style of naming, and matches the name used
		 * by the service.
		 */
		private static final String SERVICE_NAME = "org.alljoyn.bus.samples.simple";
		private static final short CONTACT_PORT = 42;

		private BusAttachment mBus;
		private ProxyBusObject mProxyObj;
		private SimpleInterface mSimpleInterface;

		private int mSessionId;
		private boolean mIsInASession;
		private boolean mIsConnected;
		private boolean mIsStoppingDiscovery;

		/* These are the messages sent to the BusHandler from the UI. */
		public static final int CONNECT = 1;
		public static final int JOIN_SESSION = 2;
		public static final int DISCONNECT = 3;
		public static final int PING = 4;

		public BusHandler(Looper looper) {
			super(looper);

			mIsInASession = false;
			mIsConnected = false;
			mIsStoppingDiscovery = false;
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			/*
			 * Connect to a remote instance of an object implementing the
			 * SimpleInterface.
			 */
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
				mBus = new BusAttachment(getPackageName(), BusAttachment.RemoteMessage.Receive);

				/*
				 * Create a bus listener class
				 */
				mBus.registerBusListener(new BusListener() {
					@Override
					public void foundAdvertisedName(String name, short transport, String namePrefix) {
						logInfo(String.format("MyBusListener.foundAdvertisedName(%s, 0x%04x, %s)", name, transport,
								namePrefix));
						/*
						 * This client will only join the first service that it
						 * sees advertising the indicated well-known name. If
						 * the program is already a member of a session (i.e.
						 * connected to a service) we will not attempt to join
						 * another session. It is possible to join multiple
						 * session however joining multiple sessions is not
						 * shown in this sample.
						 */
						if (!mIsConnected) {
							Message msg = obtainMessage(JOIN_SESSION, name);
							sendMessage(msg);
						}
					}
				});

				/*
				 * To communicate with AllJoyn objects, we must connect the
				 * BusAttachment to the bus.
				 */
				Status status = mBus.connect();
				logStatus("BusAttachment.connect()", status);
				if (Status.OK != status) {
					return;
				}

				/*
				 * Now find an instance of the AllJoyn object we want to call.
				 * We start by looking for a name, then connecting to the device
				 * that is advertising that name.
				 * 
				 * In this case, we are looking for the well-known SERVICE_NAME.
				 */
				status = mBus.findAdvertisedName(SERVICE_NAME);
				logStatus(String.format("BusAttachement.findAdvertisedName(%s)", SERVICE_NAME), status);
				if (Status.OK != status) {
					return;
				}

				break;
			}
			case (JOIN_SESSION): {
				/*
				 * If discovery is currently being stopped don't join to any
				 * other sessions.
				 */
				if (mIsStoppingDiscovery) {
					break;
				}

				/*
				 * In order to join the session, we need to provide the
				 * well-known contact port. This is pre-arranged between both
				 * sides as part of the definition of the chat service. As a
				 * result of joining the session, we get a session identifier
				 * which we must use to identify the created session
				 * communication channel whenever we talk to the remote side.
				 */
				short contactPort = CONTACT_PORT;
				SessionOpts sessionOpts = new SessionOpts();
				Mutable.IntegerValue sessionId = new Mutable.IntegerValue();
				Object userContext = null;
				Status status = mBus.joinSession((String) msg.obj, contactPort, sessionOpts, new SessionListener() {

					@Override
					public void sessionLost(int sessionId) {
						mIsConnected = false;
						logInfo(String.format("MyBusListener.sessionLost(%d)", sessionId));
						mIncomingHandler.sendEmptyMessage(MESSAGE_START_PROGRESS_DIALOG);
					}

					@Override
					public void sessionMemberAdded(int sessionId, String uniqueName) {
						logInfo(String.format("MyBusListener.sessionMemberAdded(sessionId=%d uniqueName=%s)",
								sessionId, uniqueName));
						super.sessionMemberAdded(sessionId, uniqueName);
					}

					@Override
					public void sessionMemberRemoved(int sessionId, String uniqueName) {
						logInfo(String.format("MyBusListener.sessionMemberRemoved(sessionId=%d uniqueName=%s)",
								sessionId, uniqueName));
						super.sessionMemberRemoved(sessionId, uniqueName);
					}

				}, new OnJoinSessionListener() {

					@Override
					public void onJoinSession(Status status, int sessionId, SessionOpts sessionOpts, Object userContext) {
						logInfo(String.format("MyBusListener.onJoinSession(sessionId=%d status=%s)", sessionId,
								status.name()));
						super.onJoinSession(status, sessionId, sessionOpts, userContext);
					}
				}, userContext);

				logStatus("BusAttachment.joinSession() - sessionId: " + sessionId.value, status);

				if (status == Status.OK) {
					/*
					 * To communicate with an AllJoyn object, we create a
					 * ProxyBusObject. A ProxyBusObject is composed of a name,
					 * path, sessionID and interfaces.
					 * 
					 * This ProxyBusObject is located at the well-known
					 * SERVICE_NAME, under path "/SimpleService", uses sessionID
					 * of CONTACT_PORT, and implements the SimpleInterface.
					 */
					mProxyObj = mBus.getProxyBusObject(SERVICE_NAME, "/SimpleService", sessionId.value,
							new Class<?>[] { SimpleInterface.class });

					/*
					 * We make calls to the methods of the AllJoyn object
					 * through one of its interfaces.
					 */
					mSimpleInterface = mProxyObj.getInterface(SimpleInterface.class);

					mSessionId = sessionId.value;
					mIsConnected = true;
					mIncomingHandler.sendEmptyMessage(MESSAGE_STOP_PROGRESS_DIALOG);
				}
				break;
			}

				/* Release all resources acquired in the connect. */
			case DISCONNECT: {
				mIsStoppingDiscovery = true;
				if (mIsConnected) {
					Status status = mBus.leaveSession(mSessionId);
					logStatus("BusAttachment.leaveSession()", status);
				}
				mBus.disconnect();
				getLooper().quit();
				break;
			}

				/*
				 * Call the service's Ping method through the ProxyBusObject.
				 * 
				 * This will also print the String that was sent to the service
				 * and the String that was received from the service to the user
				 * interface.
				 */
			case PING: {
				try {
					if (mSimpleInterface != null) {
						sendUiMessage(MESSAGE_PING, msg.obj);
						String reply = mSimpleInterface.Ping((String) msg.obj);
						sendUiMessage(MESSAGE_PING_REPLY, reply);
					}
				} catch (BusException ex) {
					logException("SimpleInterface.Ping()", ex);
				}
				break;
			}
			default:
				break;
			}
		}

		/* Helper function to send a message to the UI thread. */
		private void sendUiMessage(int what, Object obj) {
			mIncomingHandler.sendMessage(mIncomingHandler.obtainMessage(what, obj));
		}
	}

	private void logStatus(String msg, Status status) {
		String log = String.format("%s: %s", msg, status);
		if (status == Status.OK) {
			Log.i(TAG, log);
		} else {
			Message toastMsg = mIncomingHandler.obtainMessage(MESSAGE_POST_TOAST, log);
			mIncomingHandler.sendMessage(toastMsg);
			Log.e(TAG, log);
		}
	}

	private void logException(String msg, BusException ex) {
		String log = String.format("%s: %s", msg, ex);
		Message toastMsg = mIncomingHandler.obtainMessage(MESSAGE_POST_TOAST, log);
		mIncomingHandler.sendMessage(toastMsg);
		Log.e(TAG, log, ex);
	}

	/*
	 * print the status or result to the Android log. If the result is the
	 * expected result only print it to the log. Otherwise print it to the error
	 * log and Sent a Toast to the users screen.
	 */
	private void logInfo(String msg) {
		Log.i(TAG, msg);
	}

}
