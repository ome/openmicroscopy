/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.fulltext;

import ome.api.IQuery;
import ome.services.sessions.SessionManager;
import ome.services.util.Executor;
import ome.system.OmeroContext;

import org.hibernate.SessionFactory;

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

    static OmeroContext context;
    static Executor executor;
    static SessionFactory factory;
    static IQuery rawQuery;
    static SessionManager manager;

    public static void init() {
        context = OmeroContext.getManagedServerContext();
        executor = (Executor) context.getBean("executor");
        factory = (SessionFactory) context.getBean("sessionFactory");
        rawQuery = (IQuery) context.getBean("internal:ome.api.IQuery");
        manager = (SessionManager) context.getBean("sessionManager");
    }

    public static void usage() {
        StringBuilder sb = new StringBuilder();
        sb.append("usage: ome.service.fulltext.Main [events|full|help]\n");
        System.out.println(sb.toString());
        System.exit(-2);
    }

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            usage();
        } else if ("events".equals(args[0])) {
            indexAllEvents();
        } else if ("full".equals(args[0])) {
            indexFullDb();
        } else {
            usage();
        }
        context.close();
        System.exit(0);
    }

    public static void indexFullDb() {
        init();
        final AllEntitiesPseudoLogLoader loader = new AllEntitiesPseudoLogLoader();
        loader.setQueryService(rawQuery);
        loader.setClasses(factory.getAllClassMetadata().keySet());
        final FullTextThread ftt = createFullTextThread(loader);
        while (loader.more()) {
            ftt.run();
        }
    }

    public static void indexAllEvents() {
        init();
        final AllEventsLogLoader loader = new AllEventsLogLoader();
        loader.setQueryService(rawQuery);
        final FullTextThread ftt = createFullTextThread(loader);

        while (loader.more()) {
            ftt.run();
        }
    }

    protected static FullTextThread createFullTextThread(EventLogLoader loader) {
        final FullTextBridge ftb = new FullTextBridge();
        final FullTextIndexer fti = new FullTextIndexer(loader);
        final FullTextThread ftt = new FullTextThread(manager, executor, fti,
                ftb);
        return ftt;
    }
}
