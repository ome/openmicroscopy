/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.fulltext;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import ome.api.IQuery;
import ome.services.sessions.SessionManager;
import ome.services.util.Executor;
import ome.system.OmeroContext;
import ome.services.eventlogs.*;

import org.apache.log4j.xml.DOMConfigurator;
import org.hibernate.SessionFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.ResourceUtils;

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

    static String[] excludes;
    static OmeroContext context;
    static Executor executor;
    static SessionFactory factory;
    static IQuery rawQuery;
    static SessionManager manager;
    static FullTextBridge bridge;

    // Setup

    public static void init() {
        context = OmeroContext.getManagedServerContext();
        executor = (Executor) context.getBean("executor");
        factory = (SessionFactory) context.getBean("sessionFactory");
        rawQuery = (IQuery) context.getBean("internal-ome.api.IQuery");
        manager = (SessionManager) context.getBean("sessionManager");
        bridge = (FullTextBridge) context.getBean("fullTextBridge");
        String excludesStr = context.getProperty("omero.search.excludes");
        if (excludesStr != null) {
            excludes = excludesStr.split(",");
        } else {
            excludes = new String[]{};
        }
    }

    protected static FullTextThread createFullTextThread(EventLogLoader loader) {
        final FullTextIndexer fti = new FullTextIndexer(loader);
        final FullTextThread ftt = new FullTextThread(manager, executor, fti,
                bridge);
        return ftt;
    }

    // Public usage

    public static void usage() {
        StringBuilder sb = new StringBuilder();
        sb
                .append("usage: ome.service.fulltext.Main [help|standalone|events|full|reindex class1 class2 class3 ...]\n");
        System.out.println(sb.toString());
        System.exit(-2);
    }

    public static void main(String[] args) throws Throwable {

        // Copied from blitz Entry
        try {
            String log4j_xml = System.getProperty("log4j.configuration", "");
            if (log4j_xml.length() == 0) {
                File file = ResourceUtils.getFile("classpath:log4j.xml");
                log4j_xml = file.getAbsolutePath();
            }
            DOMConfigurator.configureAndWatch(log4j_xml);
        } catch (Exception e) {
            String msg = "CANNOT INITIALIZE LOGGING. Set -Dlog4j.configuration=...";
            throw new RuntimeException(msg, e);
        }

        int rc = 0;
        try {
            if (args == null || args.length == 0) {
                usage();
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

}
