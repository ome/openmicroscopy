/*   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz;

import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ome.system.OmeroContext;

class RouterControl extends Thread {
	
	/**
	 * Necessary constructor for subclasses.
	 */
	RouterControl(ThreadGroup group, String name) {
		super(group, name);
	}

	/** 
     * {@link Router} instance which can be added via {@link Main#setRouter(Router)}
     * to have the {@link Router} lifecycle managed.
     */
    protected Router router = null;
    
    /** 
     * Mutex used for all access to the {@link #router}.
     */
    final protected Object r_mutex = new Object();
    
    /**
     * Used by {@link Main} to set the {@link Router} on this instance.
     */
    public void setRouter(Router router) {
    	synchronized(r_mutex) {
    		this.router = router;
    	}
    }	
}

/**
 * Startup {@link Thread} for OMERO.blitz.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
class Startup extends RouterControl {

    final private Log log;

    /**
     * A {@link Thread}-implementation which gets registered via
     * {@link Runtime#addShutdownHook(Thread)} if and only if the
     * {@link OmeroContext} was successfully obtained.
     */
    final private Shutdown shutdown;

    /** 
     * A flag that gets set after startup has successfully succeeded. If
     * this value is true and stop is not true, then the server is ready. 
     */
    volatile boolean started = false;
    
    /**
	 * A flag that gets set on a request to shutdown or if startup throws an
	 * exception. This should only happen if the {@link OmeroContext} is somehow
	 * improperly configured. This includes database connections, local files,
	 * and the classpath. If true, OMERO.blitz will shutdown.
	 */
    volatile boolean stop = false;
    
    Startup(ThreadGroup group, String name, Log log, Shutdown shutdown) {
        super(group, name);
        this.shutdown = shutdown;
        this.log = log;
    }

    @Override
    public void run() {
        log.info("Creating OMERO.blitz. Please wait...");
        try {
            OmeroContext ctx = OmeroContext.getInstance("OMERO.blitz");
            // If a router has been registered, we start it now. A failure
            // to start the router counts as a failed startup.
            synchronized (r_mutex) {
            	if (router != null) {
            		router.start();
            		log.info("Glacier2router started.");
            	}
            }
            // Now that we've successfully gotten the context
            // add a shutdown hook.
            Runtime.getRuntime().addShutdownHook(shutdown);
            log.info("OMERO.blitz now accepting connections.");
            started = true;
        } catch (Exception e) {
            log.error("Error during startup. Stopping.", e);
            stop = true;
        }
    }
};

/**
 * Shutdown-{@link Thread} for OMERO.blitz. This will obtain the
 * {@link OmeroContext} instance and call {@link OmeroContext#close()},
 * therefore it is necessary that the context have been successfully created. To
 * check for this, this {@link Thread} is first registered with
 * {@link Runtime#addShutdownHook(Thread)} by the {@link Startup}-Thread.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
class Shutdown extends RouterControl {

    final private Log log;
    
    Shutdown(ThreadGroup group, String name, Log log) {
        super(group, name);
        this.log = log;
    }

    @Override
    public void run() {
        log.info("Running shutdown hook.");
        OmeroContext.getInstance("OMERO.blitz").close();
        synchronized(r_mutex) {
        	if (router != null) {
        		boolean active = router.stop();
        		if (active) { 
        			log.info("Glacier2router stopped.");
        		} else {
        			log.info("Glacier2router was not running. Can't stop.");
        		}
        	}
        }
        log.info("Shutdown hook finished.");
    }
};

/**
 * OMERO.blitz entry point.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta1
 */
public class Main implements Runnable {

    private final static Log log = LogFactory.getLog("OMERO.blitz");

    private final ThreadGroup root = new ThreadGroup("OMERO.blitz") {
        // could do exception handling.
    };

    private final Shutdown shutdown = new Shutdown(root,
            "OMERO.destroy", log);

    private final Startup startup = new Startup(root, "OMERO.startup",
            log, shutdown);

    /**
     *
     * @param args
     */
    public static void main(final String[] args) {
    	new Main();
    }
    
    /**
     * Before calling {@link #run()} it is possible to set a {@link Router} 
     * instance which will also be managed by OMERO.blitz.
     */
    public void setRouter(Router router) {
    	startup.setRouter(router);
    	shutdown.setRouter(router);
    }
    
    public void run() {
        startup.start();
        // From omeis.env.Env (A.Falconi)
        // Now the main thread exits and the bootstrap procedure is run within
        // the Initializer thread which belongs to root. As a consequence of
        // this, any other thread created thereafter will belong to root or a
        // subgroup of root.

        waitForQuit();
    }

    /**
     * Setups the {@link Startup#stop} to true, so that the server threads
     * will exit.
     */
    public void stop() {
    	startup.stop = true;
    }
    
    public void waitForStartup() {
    	while (!startup.started && !startup.stop) { 
    		try {
				Thread.sleep(500L);
			} catch (InterruptedException e) {
				// ok
			}
    	}
    }
    
    protected void waitForQuit() {
        System.out.println("");
        System.out.println("**********************************************");
        System.out.println(" OMERO.blitz console:");
        System.out.println(" Waiting for user input; log output may follow.");
        System.out.println(" Enter q[uit] to stop server or use Ctrl-C");
        System.out.println("**********************************************");
        System.out.println("");
        Scanner s = new Scanner(System.in);
        while (!startup.stop) {
            String line = s.nextLine().toLowerCase();
            if (line.startsWith("q")) {
                s.close();
                System.exit(0);
            }
            try {
                Thread.sleep(200L);
            } catch (InterruptedException e) {
                // Continue with loop.
            }
        }
    }
}
