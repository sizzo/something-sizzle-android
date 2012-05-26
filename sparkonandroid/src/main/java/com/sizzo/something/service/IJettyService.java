//========================================================================
//$Id: IJettyService.java 474 2012-01-23 03:07:14Z janb.webtide $
//Copyright 2008 Mort Bay Consulting Pty. Ltd.
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at 
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================

package com.sizzo.something.service;

import spark.Filter;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.examples.books.Books;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * IJettyService
 *
 * Android Service which runs the Jetty server, maintaining it in the active Notifications so that
 * the user can return to the IJetty Activity to control it at any time.
 */
public class IJettyService extends Service
{

	private static final String TAG = "IJettyService";

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	


	@Override
	public void onCreate() {
		Log.i(TAG, "IJettyService service onCreate()");
		


	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "IJettyService service onStartCommand()");
	      Spark.before(new Filter(){
	          @Override
	          public void handle(Request request, Response response) {
	             response.header("FOZ", "BAZ");
	          }
	       });
	       
			Log.i(TAG, "IJettyService service Books.main()");
	       Books.main(null);
	       
	       Spark.after(new Filter(){
	          @Override
	          public void handle(Request request, Response response) {
	             response.header("FOO", "BAR");
	          }
	       });
			Log.i(TAG, "IJettyService service START_STICKY");
			return START_STICKY;
	}

	public static boolean isRunning() {
		return Spark.isRunning();
	}
}
