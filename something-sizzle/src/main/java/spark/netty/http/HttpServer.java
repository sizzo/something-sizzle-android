/*
 * Copyright 2011 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package spark.netty.http;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import android.util.Log;

public class HttpServer {

	private static final String TAG = "HttpServer";
	private final int port;
	private static boolean serverStarted = false;

	public HttpServer(int port) {
		this.port = port;
	}

	public void run() throws Exception {
		if (!serverStarted) {
			// configure the server
			ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
					Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
			// Set up the event pipeline factory
			bootstrap.setPipelineFactory(new HttpServerPipelineFactory());

			bootstrap.bind(new InetSocketAddress(port));
			serverStarted = true;
			Log.i(TAG, "HttpServer listen on port=" + port);
		}
	}

	public static void main(String[] args) throws Exception {
		int port;
		if (args.length > 0) {
			port = Integer.parseInt(args[0]);
		} else {
			port = 8080;
		}
		HttpServer httpServer = new HttpServer(port);
		httpServer.run();
	}

	public static boolean isServerStarted() {
		return serverStarted;
	}

}
