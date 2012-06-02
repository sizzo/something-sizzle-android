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

package com.sizzo.something.service.http;

import spark.netty.http.HttpServer;
import net.tomp2p.p2p.Peer;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class HttpServerService extends Service {
	public static final String TAG = "HttpServerService";

	@Override
	public void onCreate() {
		Log.v(TAG, "http server service Created");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.v(TAG, "http server service -- onStartCommand()");
		startHttpServer();

		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}

	private void startHttpServer() {
		if (!HttpServer.isServerStarted()) {
			try {
				HttpServer httpServer = new HttpServer(8080);
				httpServer.run();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onDestroy() {
		Log.v(TAG, "http server service Destroyed");
		stopHttpServer();
	}

	private void stopHttpServer() {
		// TODO Auto-generated method stub

	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
