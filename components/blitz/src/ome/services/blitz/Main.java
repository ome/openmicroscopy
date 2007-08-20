/*   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import ome.system.OmeroContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** 
 * Simple base case which allows {@link Startup} and {@link Shutdown}
 * to control a {@link Router} instance *if present*.
 *
 */
class RouterControl extends Thread {
	
    final protected Log log;
	
	/**
	 * Necessary constructor for subclasses.
	 */
	RouterControl(ThreadGroup group, String name, Log log) {
		super(group, name);
		this.log = log;
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
    
    protected void startRouter() {
        synchronized (r_mutex) {
        	if (router != null) {
        		router.start();
        		log.info("Glacier2router started.");
        	}
        }
    }
    
    protected void stopRouter(Ice.Communicator ic) {
        synchronized(r_mutex) {
        	if (router != null) {
        		boolean active = router.shutdown(ic);
        		if (active) { 
        			log.info("Glacier2router stopped.");
        		} else {
        			log.info("Glacier2router was not running. Can't stop.");
        		}
        	}
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

	/** 
	 * Name for this {@link Thread} which matches the name of
	 * the {@link OmeroContext} chosen by {@link Main}
	 */
	final private String name;
	
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
        super(group, name, log);
        this.name = name;
        this.shutdown = shutdown;
    }

    @Override
    public void run() {
        log.info("Creating "+this.name+". Please wait...");
        try {
            OmeroContext ctx = OmeroContext.getInstance(name);
            // If a router has been registered, we start it now. A failure
            // to start the router counts as a failed startup.
            startRouter();
            // Now that we've successfully gotten the context
            // add a shutdown hook.
            Runtime.getRuntime().addShutdownHook(shutdown);
            log.info(name + " now accepting connections.");
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

    final String contextName;
    
    Shutdown(ThreadGroup group, String threadName, String contextName, Log log) {
        super(group, threadName, log);
        this.contextName = contextName;
    }

    @Override
    public void run() {
        log.info("Running shutdown hook.");
        Main main = Main.IN_USE.get(contextName);
        if (main != null) {
        	main.shutdown();
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

	/**
	 * This keeps tracks of which servers have already been started
	 * to prevent multiple allocation. The {@link Shutdown} hook will
	 * be used to remove the entry from the map. It is still possible 
	 * for someone to access the given {@link OmeroContext}, but at 
	 * least the {@link Main} wrapper will provide feedback.
	 */
	final static Map<String, Main> IN_USE = 
		Collections.synchronizedMap(new HashMap<String, Main>());
	
	private final static String DEFAULT_NAME = "OMERO.blitz";
	
	private final String name;
	
    private final Log log;

    private final ThreadGroup root;

    private final Shutdown shutdown;
    
    private final Startup startup;

    /**
     * Entry point to the server. The first argument on the command line
     * will be used as the name for the {@link OmeroContext} via
     * {@link Main#Main(String)}. 
     */
    public static void main(final String[] args) {
    	if (args != null && args.length > 0) {
    		new Main(args[0]);
    	} else {
    		new Main();
    	}
    }
    
    /**
     * Empty constructor which passes {@link #DEFAULT_NAME} to {@link Main#Main(String)} 
     */
    public Main() {
    	this(DEFAULT_NAME);
    }
    
    /**
     * Main constructor which uses the given name as the {@link OmeroContext} lookup
     * for this server. It only makes sense to start one server with a given name
     */
    public Main(String name) {
    	this.name = name;
    	this.log = LogFactory.getLog(this.name);
    	this.root = new ThreadGroup(this.name) {
            // could do exception handling.
        };
        this.shutdown = new Shutdown(root,
                "OMERO.destroy", this.name, log);
        this.startup = new Startup(root, this.name,
                log, shutdown);
        IN_USE.put(this.name, this);
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
    
    /**
     * Blocks the current {@link Thread} until either the 
     * {@link #startup} {@link Thread} has finished 
     * {@link Startup#started startup} or
     * as been {@link Startup#stop stopped} due to an exception.
     * 
     * The method returns whether false if {@link Startup#stop shutdown}
     * has been requested.
     */
    public boolean waitForStartup() {
    	while (!startup.started && !startup.stop) { 
    		try {
				Thread.sleep(500L);
			} catch (InterruptedException e) {
				// ok
			}
    	}
    	return ! startup.stop;
    }
    
    /**
     * Used after {@link #waitForQuit()} or {@link Shutdown} finishes.
     */
    public void shutdown() {
    	IN_USE.remove(name);
    	startup.stop = true;
    	OmeroContext ctx = OmeroContext.getInstance(name);
    	Ice.Communicator ic = (Ice.Communicator) ctx.getBean("Ice.Communicator");
    	log.info("Calling stop router.");
    	shutdown.stopRouter(ic);
    	log.info("Calling close context on "+name);
        ctx.close();
        log.info("Finished shutdown.");
    }
    
    protected void waitForQuit() {
        System.out.println("");
        System.out.println("**********************************************");
        System.out.println(" " + name + " console:");
        System.out.println(" Waiting for user input; log output may follow.");
        System.out.println(" Enter q[uit] to stop server or use Ctrl-C");
        System.out.println("**********************************************");
        System.out.println("");
        Scanner s = new Scanner(System.in);
        while (!startup.stop) {
            try {
                Thread.sleep(200L);
            } catch (InterruptedException e) {
                // Continue with loop.
            }
            String line = s.nextLine().toLowerCase();
            if (line.startsWith("q")) {
                s.close();
                shutdown();
            }
        }
    }
}
