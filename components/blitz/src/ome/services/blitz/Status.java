/*   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz;

import ome.system.OmeroContext;

/**
 * Status check for OMERO.blitz. Uses the server settings as defined in omero/server.xml
 * to contact a hopefully functioning server.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class Status implements Runnable {

	Ice.Communicator ic;
	
    /**
     * Uses the passed args to create an {@link Ice.Communicator}.
     */
    public static void main(final String[] args) {
    	Status status = new Status(args);
    	try {
    		status.run();
    	} catch (StatusException se) {
    		System.exit(se.exit);
    	}
    	System.exit(0);
    }
    
    
    public Status(String[] args) {
    	ic = Ice.Util.initialize(args);
    }
    
    public void run() {
    	Ice.ObjectPrx base = ic.stringToProxy("Manager:tcp -h 127.0.0.1 -p 9999");
    	try {
    		Glacier2.SessionManagerPrx mgr = Glacier2.SessionManagerPrxHelper.checkedCast( base );
    		if (mgr == null) {
    			throw new StatusException(1);
    		}
    	} catch (Ice.ConnectionRefusedException cre) {
    		throw new StatusException(2);
    	}

    }
    
}

class StatusException extends RuntimeException {
	int exit = Integer.MAX_VALUE;
	StatusException(int code) {
		exit = code;
	}
}