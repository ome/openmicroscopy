/*
 * ome.services.hooks.Startup
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.hooks;

//Java imports

//Third-party libraries

//Application-internal dependencies
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;

/** 
 * JMX-Start method. This interface needs to be public for JMX to work properly.
 * Otherwise, this interface is unimportant. See {@link StartupHook} for more
 * information.
 * 
 * @author  Josh Moore, josh.moore at gmx.de
 * @version $Revision$, $Date$
 * @since   3.0-Beta1
 * @see     StartupHook
 */
@RevisionDate("$Date$")
@RevisionNumber("$Revision$")
public interface Startup {
    
	/**
	 * Called by the application server when the service is started and all 
	 * the services it depends on are started.
	 * 
	 * @throws Exception
	 */
    void start() throws Exception;

}
