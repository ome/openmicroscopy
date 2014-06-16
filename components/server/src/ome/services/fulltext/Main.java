/*
 *   $Id$
 *
 *   Copyright 2008-14 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.fulltext;

import java.util.HashSet;
import java.util.Set;

import ome.api.IQuery;
import ome.services.eventlogs.AllEntitiesPseudoLogLoader;
import ome.services.eventlogs.AllEventsLogLoader;
import ome.services.eventlogs.EventLogLoader;
import ome.services.eventlogs.PersistentEventLogLoader;
import ome.services.sessions.SessionManager;
import ome.services.util.Executor;
import ome.system.OmeroContext;
import ome.system.Principal;
import ome.system.ServiceFactory;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.transaction.annotation.Transactional;

/**
 * Commandline entry-point for various full text actions. Commands include:
 * <ul>
 * <li>full - Index full database</li>
 * <li>events - Index all events</li>
 * </ul>
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */

public class Main {

    static String uuid;
    static String[] excludes;
    static OmeroContext context;
    static Executor executor;
    static SessionFactory factory;
    static IQuery rawQuery;
    static SessionManager manager;
    static FullTextBridge bridge;
    static PersistentEventLogLoader loader;

    // Setup

    public static void init() {
        context = OmeroContext.getInstance("ome.fulltext");
        try {
            // Now that we're using the fulltext context we need
            // to disable the regular processing, otherwise there
            // are conflicts.
            context.getBean("scheduler", Scheduler.class).pauseAll();
        } catch (SchedulerException se) {
            throw new RuntimeException(se);
        }
        uuid = context.getBean("uuid", String.class);
        executor = (Executor) context.getBean("executor");
        factory = (SessionFactory) context.getBean("sessionFactory");
        rawQuery = (IQuery) context.getBean("internal-ome.api.IQuery");
        manager = (SessionManager) context.getBean("sessionManager");
        bridge = (FullTextBridge) context.getBean("fullTextBridge");
        loader =  (PersistentEventLogLoader) context.getBean("eventLogLoader");
        String excludesStr = context.getProperty("omero.search.excludes");
        if (excludesStr != null) {
            excludes = excludesStr.split(",");
        } else {
            excludes = new String[]{};
        }
    }

    protected static FullTextThread createFullTextThread(EventLogLoader loader) {
        return createFullTextThread(loader, false);
    }

    protected static FullTextThread createFullTextThread(EventLogLoader loader,
            boolean dryRun) {
        final FullTextIndexer fti = new FullTextIndexer(loader);
        fti.setDryRun(dryRun);
        final FullTextThread ftt = new FullTextThread(manager, executor, fti,
                bridge);
        return ftt;
    }

    // Public usage

    public static void usage() {
        StringBuilder sb = new StringBuilder();
        sb.append("usage: [-Dlogback.configurationFile=stderr.xml] ");
        sb.append("ome.service.fulltext.Main [help|standalone|events|full|");
        sb.append("reindex class1 class2 class3 ...]\n");
        System.out.println(sb.toString());
        System.exit(-2);
    }

    public static void main(String[] args) throws Throwable {

        int rc = 0;
        try {
            if (args == null || args.length == 0) {
                usage();
            } else if ("reset".equals(args[0])) {
                reset(args);
            } else if ("dryrun".equals(args[0])) {
                sequential(true, args);
            } else if ("sequential".equals(args[0])) {
                sequential(false, args);
            } else if ("standalone".equals(args[0])) {
                standalone(args);
            } else if ("events".equals(args[0])) {
                indexAllEvents();
            } else if ("full".equals(args[0])) {
                indexFullDb();
            } else if ("reindex".equals(args[0])) {
                if (args.length < 2) {
                    usage(); // EARLY EXIT
                }
                Set<String> set = new HashSet<String>();
                for (int i = 1; i < args.length; i++) {
                    set.add(args[i]);
                }
                indexByClass(set);
            } else {
                usage();
            }
        } catch (Throwable t) {
            rc = 1;
            t.printStackTrace();
        } finally {
            if (context != null) {
                context.close();
            }
            System.exit(rc);
        }
    }

    public static void indexFullDb() {
        init();
        final AllEntitiesPseudoLogLoader loader = new AllEntitiesPseudoLogLoader();
        loader.setQueryService(rawQuery);
        loader.setExcludes(excludes);
        loader.setClasses(factory.getAllClassMetadata().keySet());
        final FullTextThread ftt = createFullTextThread(loader);
        while (loader.more() > 0) {
            ftt.run();
        }
    }

    public static void indexByClass(Set<String> set) {
        init();
        final AllEntitiesPseudoLogLoader loader = new AllEntitiesPseudoLogLoader();
        loader.setQueryService(rawQuery);
        loader.setClasses(set);
        final FullTextThread ftt = createFullTextThread(loader);
        while (loader.more() > 0) {
            ftt.run();
        }
    }

    public static void indexAllEvents() {
        init();
        final AllEventsLogLoader loader = new AllEventsLogLoader();
        loader.setExcludes(excludes);
        loader.setQueryService(rawQuery);
        final FullTextThread ftt = createFullTextThread(loader);

        while (loader.more() > 0) {
            ftt.run();
        }
    }

    /**
     * Can be used to reset the value that the {@link PersistentEventLogLoader}
     * would read if started now.
     */
    public static void reset(String[] args) {
        init();
        long oldValue = -1;
        long newValue = 0;
        if (args == null || args.length != 2) {
            System.out.println("Using 0 as reset target");
        } else {
            newValue = Long.valueOf(args[1]);
        }

        oldValue = loader.getCurrentId();
        loader.setCurrentId(newValue);
        System.out.println("=================================================");
        System.out.println(String.format("Value reset to %s. Was %s",
                newValue, oldValue));
        System.out.println("=================================================");
    }

    /**
     * Uses a {@link PersistentEventLogLoader} and cycles through all
     * the remaining logs.
     */
    public static void sequential(boolean dryrun, String[] args) {
        init();
        final FullTextThread ftt = createFullTextThread(loader, dryrun);

        long loops = 0;
        long current = current(loader);
        while (true) {
            // Quartz usually would wait 3 seconds here.
            loops++;
            ftt.run();
            long newCurrent = current(loader);
            if (newCurrent == current) {
                break;
            } else {
                current = newCurrent;
            }
        }
        System.out.println("=================================================");
        System.out.println(String.format(
                "Done in %s loops. Now at: %s", loops, current));
        System.out.println("=================================================");
    }

    /**
     * Starts up and simply waits until told by the grid to disconnect.
     */
    public static void standalone(String[] args) {
        Ice.Communicator ic = Ice.Util.initialize(args);
        Ice.ObjectAdapter oa = ic.createObjectAdapter("IndexerAdapter");
        oa.activate();
        String cron = ic.getProperties().getProperty("omero.search.cron");
        if (cron == null || cron.length() == 0) {
            System.out.println("Using default cron value.");
        } else {
            System.setProperty("omero.search.cron",cron);
        }
        try {
            init(); // Starts cron
        } finally {
            ic.waitForShutdown();
        }
    }

    private static long current(final PersistentEventLogLoader loader) {
        Principal p = new Principal(uuid);
        return (Long) executor.execute(p, new Executor.SimpleWork(loader, "more"){
            @Override
            @Transactional(readOnly=false)
            public Object doWork(Session session, ServiceFactory sf) {
                return loader.getCurrentId();
            }
        });
    }
}
