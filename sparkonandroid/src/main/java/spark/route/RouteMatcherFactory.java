/*
 * Copyright 2011- Per Wendel
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package spark.route;

import android.util.Log;


/**
 * RouteMatcherFactory
 *
 * @author Per Wendel
 */
public class RouteMatcherFactory {


    private static final String TAG = "RouteMatcherFactory";
	private static RouteMatcher routeMatcher = null;

    public static synchronized RouteMatcher get() {
        if (routeMatcher == null) {
            Log.i(TAG,"creates RouteMatcher");
            routeMatcher = new SimpleRouteMatcher();
//            routeMatcher = new RouteMatcherImpl();
        }
        return routeMatcher;
    }

}
