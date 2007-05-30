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

/**
 * Startup {@link Thread} for OMERO.blitz.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
class Startup extends Thread {

    final private Log log;

    /**
     * A {@link Thread}-implementation which gets registered via
     * {@link Runtime#addShutdownHook(Thread)} if and only if the
     * {@link OmeroContext} was successfully obtained.
     */
    final private Shutdown shutdown;

    /**
     * A flag that gets set if startup throws an exception. This should only
     * happen if the {@link OmeroContext} is somehow improperly configured. This
     * includes database connections, local files, and the classpath. If true,
     * OMERO.blitz will shutdown.
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
            // Now that we've successfully gotten the context
            // add a shutdown hook.
            Runtime.getRuntime().addShutdownHook(shutdown);
            log.info("OMERO.blitz now accepting connections.");
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
class Shutdown extends Thread {

    final private Log log;

    Shutdown(ThreadGroup group, String name, Log log) {
        super(group, name);
        this.log = log;
    }

    @Override
    public void run() {
        log.info("Running shutdown hook.");
        OmeroContext.getInstance("OMERO.blitz").close();
        log.info("Shutdown hook finished.");
    }
};

/**
 * OMERO.blitz entry point.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta1
 */
public class Main {

    private final static Log log = LogFactory.getLog("OMERO.blitz");

    private final static ThreadGroup root = new ThreadGroup("OMERO.blitz") {
        // could do exception handling.
    };

    private final static Shutdown shutdown = new Shutdown(root,
            "OMERO.destroy", log);

    private final static Startup startup = new Startup(root, "OMERO.startup",
            log, shutdown);

    /**
     *
     * @param args
     */
    public static void main(final String[] args) {

        startup.start();
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
