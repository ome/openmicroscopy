/*   $Id: Server.java 1201 2007-01-18 21:54:35Z jmoore $
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.icy;

import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ome.system.OmeroContext;

/**
 * 
 *
 * @author josh
 */
public class Main {
    
    private final static Log log = LogFactory.getLog("OMERO.blitz");
    
    /**
     * 
     * @param args
     */
    public static void main(final String[] args) {

        ThreadGroup root = new ThreadGroup("OMERO.root") {
            // could do exception handling.
        };

        
        Runtime.getRuntime().addShutdownHook(new Thread(root, "OMERO.destroy") {
            @Override
            public void run() {
                log.info("Running shutdown hook.");
                OmeroContext.getInstance("OMERO.blitz").close();
                log.info("Shutdown hook finished.");
            }
        });
        
        Runnable r = new Runnable() {
            public void run() {
                log.info("Creating OMERO.blitz. Please wait...");
                OmeroContext ctx = OmeroContext.getInstance("OMERO.blitz");
                log.info("OMERO.blitz now accepting connections.");
            }
        };

        Thread t = new Thread(root, r, "OMERO.startup");
        t.start();
        // From omeis.env.Env (A.Falconi)
        // Now the main thread exits and the bootstrap procedure is run within
        // the Initializer thread which belongs to root. As a consequence of
        // this, any other thread created thereafter will belong to root or a
        // subgroup of root.
        
        waitForQuit();
    }

    protected static void waitForQuit() {
        System.out.println("");
        System.out.println("**********************************************");
        System.out.println(" OMERO.blitz console:");
        System.out.println(" Waiting for user input; log output may follow.");
        System.out.println(" Enter q[uit] to stop server or use Ctrl-C");
        System.out.println("**********************************************");
        System.out.println("");
        Scanner s = new Scanner(System.in);
        while (true) {
            String line = s.nextLine().toLowerCase();
            if (line.startsWith("q")) {
                s.close();
                System.exit(0);
            }
        }
    }
}
