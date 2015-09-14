/*
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz;

import java.util.concurrent.locks.ReentrantLock;

import ome.system.OmeroContext;
import ome.util.messages.ShutdownMessage;
import ome.util.messages.UserSignalMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 * OMERO.blitz startup code. Replaces the standard <code>Main</code> class as
 * the main application entry point. Uses Sun-specific APIs to handle signals.
 */
public class Entry {

    private final static Logger log = LoggerFactory.getLogger(Entry.class);

    /**
     * Return code status. Initially -1. On successful start, 0. > 1 on
     * exception.
     */
    int status = -1;

    /**
     * Name of the {@link OmeroContext} to
     */
    final String name;

    /**
     * Prevents multiple calls to {@link #shutdown()}
     */
    final ReentrantLock lock = new ReentrantLock();

    /**
     * Context for the OMERO application
     */
    volatile OmeroContext ctx = null;

    /**
     * {@link Ice.Communicator} which will be waited on.
     */
    volatile Ice.Communicator ic = null;

    private static void waitOnStartup() {
        int ms = 10000; // 10 seconds by default
        try {
            String prop = System.getenv("OMERO_STARTUP_WAIT");
            ms = Integer.valueOf(prop);
        } catch (Exception e) {
            log.debug(e.toString()); // slf4j migration: toString()
        }

        try {
            log.info(String.format("Waiting %s ms on startup", ms));
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            log.debug(e.toString()); // slf4j migration: toString()
        }
    }

    /**
     * Entry point to the server. The first argument on the command line will be
     * used as the name for the {@link OmeroContext}. Other options include:
     * 
     * -s Check status (all args passed to {@link Ice.Util#initialize(String[])}
     * 
     */
    public static void main(final String[] args) {
        String name = "OMERO.blitz";
        if (args != null && args.length > 0) {
            if ("-s".equals(args[0])) {
                try {
                    new Status(args).run();
                } catch (Throwable t) {
                    t.printStackTrace();
                    System.exit(1);
                }
                System.exit(0);
            }
            // Now we find the first non-"--Ice.Config" argument and
            // pass that to main(). The last --Ice.Config value will be
            // seen by the Ice.Communicator. The last non-Ice.Config argument
            // will be used as a beanRefContext lookup.
            for (String string : args) {
                if (string.startsWith("--Ice.Config")) {
                    System.setProperty("ICE_CONFIG", string.substring(13));
                } else {
                    name = string;
                    waitOnStartup();
                }
            }
        }

        final Entry instance = new Entry(name);

        SignalHandler handler = new SignalHandler() {
            public void handle(Signal sig) {
                log.info(sig.getName() + ": Shutdown requested.");
                instance.lock.lock();
                try {
                    instance.status = sig.getNumber();
                    instance.shutdown(true);
                } finally {
                    // As with the try/finally block around shutdown in the
                    // start method, execution should never reach this point.
                    // But just in case, future code changes should introduce
                    // an exception (and to make findbugs happy) we'll add the
                    // try/finally
                    instance.lock.unlock();
                }
            }
        };

        registerSignal(handler, "INT");
        registerSignal(handler, "TERM");
        registerSignal(handler, "BREAK");

        // ticket:#4210
        class UserSignalHandler implements SignalHandler {
            int signal;
            UserSignalHandler(int signal) {
                this.signal = signal;
            }
            public void handle(Signal arg0) {
                try {
                    UserSignalMessage msg = new UserSignalMessage(this, signal);
                    instance.ctx.publishMessage(msg);
                } catch (Throwable e) {
                    log.error("Error on user signal " + signal, e);
                }
            }
        };
        registerSignal(new UserSignalHandler(1), "USR1");
        registerSignal(new UserSignalHandler(2), "USR2");

        instance.start();
    }

    private static void registerSignal(SignalHandler handler, String sig) {
        try {
            Signal.handle(new Signal(sig), handler);
        } catch (IllegalArgumentException iae) {
            // Ok. BREAK will not exist on non-Windows systems, for example.
        }
    }

    /**
     * Stores name of the {@link OmeroContext} which is to be used by this
     * instance.
     */
    public Entry(String name) {
        this.name = name;
    }

    /**
     * Obtains the {@link #name named} {@link OmeroContext}, creating it if
     * necessary, and then delegates to
     * {@link Ice.Communicator#waitForShutdown()} until either it is externally
     * shutdown, or until a signal is caught.
     */
    public void start() {
        try {
            log.info("Creating " + name + ". Please wait...");

            // TEMPORARY WORKAROUND
            String ICE_CONFIG = System.getProperty("ICE_CONFIG");
            
            // Parse out any omero.* properties from ICE_CONFIG
            // and set them in the System
            Ice.InitializationData id = new Ice.InitializationData();
            id.properties = Ice.Util.createProperties();
            if (ICE_CONFIG != null) {
                id.properties.load(ICE_CONFIG);
                for (String k : id.properties.getPropertiesForPrefix("omero").keySet()) {
                    System.setProperty(k, id.properties.getProperty(k));
                }
            }
            
            ctx = OmeroContext.getInstance(name);
            if (ctx.containsBean("Ice.Communicator")) {
                ic = (Ice.Communicator) ctx.getBean("Ice.Communicator");
            } else {
                // TODO This should be adapted to work for any process
                // that doesn't need to add servants. Here "Indexer" could
                // be replaced by omero.name or similar.
                ic = Ice.Util.initialize(id);
                String adapterName = ctx.getBean("adapterName", String.class);
                Ice.ObjectAdapter oa = ic.createObjectAdapter(adapterName);
                oa.activate();
            }
            log.info(name + " now accepting connections.");
            ic.waitForShutdown();
            status = 0;
        } catch (Ice.LocalException e) {
            log.error("Error on startup.", e);
            status = 1;
        } catch (Exception e) {
            log.error("Error on startup.", e);
            status = 2;
        }
        System.out.flush();
        System.err.flush();
        lock.lock();
        try {
            shutdown(true);
        } finally {
            // This will never be called since System.exit is called in
            // shutdown where no exception can be thrown, but just in case
            // the code paths are ever changed and the exit doesn't get called
            // we'll unlock so other threads won't hang the server.
            lock.unlock();
        }
    }

    public int status() {
        return status;
    }

    /**
     * Calls {@link OmeroContext#closeAll()} to recursively close all
     * OMERO.blitz resources in the reverse order that they were created.
     * 
     * Throws no exceptions.
     * 
     * If true is passed for callSystemExit, then {@link System#exit(int)} will
     * be called with the current status.
     */
    public void shutdown(final boolean callSystemExit) {

        // Finally shutdown the whole context
        if (ctx != null) {
            try {
                ctx.publishMessage(new ShutdownMessage(this));
                log.info("Calling close on context " + name);
                OmeroContext forClose = ctx;
                ctx = null;
                forClose.closeAll();
                log.info("Finished shutdown.");
            } catch (Throwable t) {
                log.error("Error shutting down " + name, t);
                status = 3;
            }
        }
        
        if (callSystemExit) {
            System.exit(status);
        }
    }
}
