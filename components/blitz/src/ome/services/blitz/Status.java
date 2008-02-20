/*   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz;

/**
 * Status check for OMERO.blitz. Uses the "StatusCheck" proxy as defined in the
 * Ice properties passed to main method.
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
            se.printStackTrace();
            System.exit(se.exit);
        }
        System.exit(0);
    }

    public Status(String[] args) {
        ic = Ice.Util.initialize(args);
    }

    public void run() {
        Ice.ObjectPrx base = ic.propertyToProxy("StatusCheck");
        try {
            Glacier2.SessionManagerPrx mgr = Glacier2.SessionManagerPrxHelper
                    .checkedCast(base);
            if (mgr == null) {
                throw new StatusException("Null proxy.", 1);
            }
        } catch (Ice.ConnectionRefusedException cre) {
            throw new StatusException("Connection refused.", 2);
        }

    }

}

class StatusException extends RuntimeException {
    int exit = Integer.MAX_VALUE;

    StatusException(String msg, int code) {
        super(msg);
        exit = code;
    }
}