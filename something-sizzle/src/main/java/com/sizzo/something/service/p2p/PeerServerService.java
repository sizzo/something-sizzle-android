// Licensed under Apache License version 2.0
// Original license LGPL

//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.sizzo.something.service.p2p;

import java.io.IOException;

import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDHT;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerMaker;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

public class PeerServerService extends Service {
	public static final String TAG = "PeerService";
	private Peer peer = null;

	@Override
	public void onCreate() {
		Log.v(TAG, "peer server service Created");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.v(TAG, "peer server service -- onStartCommand()");
		String peerId = intent.getExtras().getString("peerId");
		startPeer(peerId);

		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		Log.v(TAG, "peer server service Destroyed");
		stopPeer();
	}

	private void stopPeer() {
		peer.shutdown();
		peer = null;
	}

	private void startPeer(String peerId) {
		if (peer == null || !peer.isRunning() || !peer.isListening()) {
			try {
				peer = new PeerMaker(Number160.createHash(peerId)).setPorts(4101).buildAndListen();
				FutureBootstrap fb = this.peer.bootstrapBroadcast(4101);
				fb.awaitUninterruptibly();
				this.broadcastNameAndIp(peerId, "192.193.194.195");
			} catch (IOException e) {
				Log.v(TAG, e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public String findIpByName(String name) throws ClassNotFoundException, IOException {
		FutureDHT futureDHT = peer.get(Number160.createHash(name));
		futureDHT.awaitUninterruptibly();
		if (futureDHT.isSuccess()) {
			return futureDHT.getDataMap().values().iterator().next().getObject().toString();
		}
		return "not found";
	}

	private void broadcastNameAndIp(String name, String ip) throws IOException {
		peer.put(Number160.createHash(name), new Data(ip)).awaitUninterruptibly();
	}

	// Used to receive messages from the Activity
	final Messenger inMessenger = new Messenger(new Handler() {
		public void handleMessage(Message msg) {
			String ip = "unknow ip for test.android";
			try {
				ip = Build.MANUFACTURER+" Found ip for test.android="+findIpByName("test.android");
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				ip = "unknow ip for test.android";
			} finally {

				Log.e("MESSAGE", "Got message");
				Bundle data = msg.getData();
				// String urlPath =
				// data.getString(DownloadServiceMessenger.URLPATH);
				// String fileName =
				// data.getString(DownloadServiceMessenger.FILENAME);
				// String outputPath = download(urlPath, fileName);

				Message backMsg = Message.obtain();
				backMsg.arg1 = android.app.Activity.RESULT_OK;
				Bundle bundle = new Bundle();
				bundle.putString("RESULTPATH", "outputPath from PeerServerService "+ip);
				backMsg.setData(bundle);
				try {
					outMessenger.send(backMsg);
				} catch (android.os.RemoteException e1) {
					Log.w(getClass().getName(), "Exception sending message", e1);
				}
			}
		}
	});
	// Use to send message to the Activity
	private Messenger outMessenger;

	public IBinder onBind(Intent intent) {
		Bundle extras = intent.getExtras();
		// Get messager from the Activity
		if (extras != null) {
			outMessenger = (Messenger) extras.get("MESSENGER");
		}
		// Return our messenger to the Activity to get commands
		return inMessenger.getBinder();
	}

}
