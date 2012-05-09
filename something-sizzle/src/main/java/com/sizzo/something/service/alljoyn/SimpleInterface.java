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

import org.alljoyn.bus.BusException;
import org.alljoyn.bus.annotation.BusInterface;
import org.alljoyn.bus.annotation.BusMethod;

/*
 * The BusInterface annotation is used to tell the code that this interface is an AllJoyn interface.
 *
 * The 'name' value is used to specify by which name this interface will be known.  If the name is
 * not given the fully qualified name of the Java interface is be used.  In most instances its best
 * to assign an interface name since it helps promote code reuse.
 */
@BusInterface(name = "org.alljoyn.bus.samples.simple.SimpleInterface")
public interface SimpleInterface {
	/*
	 * Name used as the well-known name and the advertised name. This name must
	 * be a unique name both to the bus and to the network as a whole. The name
	 * uses reverse URL style of naming.
	 */
	public static final String SERVICE_NAME = "org.alljoyn.bus.samples.simple";
	public static final short CONTACT_PORT = 42;

	/*
	 * The BusMethod annotation signifies that this function should be used as
	 * part of the AllJoyn interface. The runtime is smart enough to figure out
	 * what the input and output of the method is based on the input/output
	 * arguments of the Ping method.
	 * 
	 * All methods that use the BusMethod annotation can throw a BusException
	 * and should indicate this fact.
	 */
	@BusMethod
	String Ping(String inStr) throws BusException;
}
