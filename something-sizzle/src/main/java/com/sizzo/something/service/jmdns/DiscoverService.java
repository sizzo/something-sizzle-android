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

package com.sizzo.something.service.jmdns;

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;


public class DiscoverService extends Service {
	public static final String TAG = "DiscoverService";
	private JmDNS jmDNS = null;

	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		Log.v(TAG, "jmdns discover service Created");
		


	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.v(TAG, "jmdns discover service -- onStartCommand()");
		startJmDNS();
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		Log.v(TAG, "jmdns discover service Destroyed");
		stopJmDNS();
	}
	
	private void stopJmDNS() {
		try {
			if (jmDNS != null) {
				jmDNS.close();
				jmDNS = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void startJmDNS() {
		try {
			if (jmDNS == null) {
				jmDNS = JmDNS.create();
	            jmDNS.addServiceListener("_touch._tcp.local.", new JmDNSListener( ));
	            jmDNS.addServiceListener("_http._tcp.local.", new JmDNSListener( ));
	            jmDNS.addServiceListener("_ssh._tcp.local.", new JmDNSListener( ));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    private static final char[] _nibbleToHex = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    private static String toHex(byte[] code) {
        StringBuilder result = new StringBuilder(2 * code.length);

        for (int i = 0; i < code.length; i++) {
            int b = code[i] & 0xFF;
            result.append(_nibbleToHex[b / 16]);
            result.append(_nibbleToHex[b % 16]);
        }

        return result.toString();
    }
	private void registerJmDNSServices(){
        Random random = new Random();
        int id = random.nextInt(100000);


        final HashMap<String, String> values = new HashMap<String, String>();
        values.put("DvNm", "Android-" + id);
        values.put("RemV", "10000");
        values.put("DvTy", "iPod");
        values.put("RemN", "Remote");
        values.put("txtvers", "1");
        byte[] pair = new byte[8];
        random.nextBytes(pair);
        values.put("Pair", toHex(pair));

        byte[] name = new byte[20];
        random.nextBytes(name);
//        System.out.println("Requesting pairing for " + toHex(name));
        ServiceInfo pairservice = ServiceInfo.create("_touch._tcp.local.", toHex(name), 1025, 0, 0, values);
        try {
			jmDNS.registerService(pairservice);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	static class JmDNSListener implements ServiceListener {
		@Override
		public void serviceAdded(ServiceEvent event) {
			Log.i(TAG,"External Service added   : " + event.getName() + "." + event.getType());
		}

		@Override
		public void serviceRemoved(ServiceEvent event) {
			Log.i(TAG,"External Service removed : " + event.getName() + "." + event.getType());
		}

		@Override
		public void serviceResolved(ServiceEvent event) {
			Log.i(TAG,"External Service resolved: " + event.getInfo());
		}
	}



}
