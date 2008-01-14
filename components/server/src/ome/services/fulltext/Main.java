/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.fulltext;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ome.api.IQuery;
import ome.model.IObject;
import ome.model.meta.EventLog;
import ome.parameters.Filter;
import ome.parameters.Parameters;
import ome.services.util.Executor;
import ome.system.OmeroContext;

import org.hibernate.SessionFactory;

/**
 * Driver for various full text actions. Commands include:
 * <ul>
 * <li>full - Index full database</li
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

    public static void init() {
        context = OmeroContext.getManagedServerContext();
        executor = (Executor) context.getBean("executor");
        factory = (SessionFactory) context.getBean("sessionFactory");
        rawQuery = (IQuery) context.getBean("internal:ome.api.IQuery");

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
        final FullTextIndexer fti = new FullTextIndexer(executor, loader);

        while (loader.more()) {
            fti.run();
        }
    }

    public static void indexAllEvents() {
        init();
        final AllEventLogsLoader loader = new AllEventLogsLoader();
        loader.setQueryService(rawQuery);
        final FullTextIndexer fti = new FullTextIndexer(executor, loader);

        while (loader.more()) {
            fti.run();
        }
    }
}

class AllEntitiesPseudoLogLoader<T extends IObject> extends EventLogLoader {

    List<String> classes;
    String current = null;
    long last = -1;

    public void setClasses(Set<String> classes) {
        this.classes = new ArrayList<String>(classes);
    }

    @Override
    protected EventLog query() {
        if (current == null) {
            if (!more()) {
                return null;
            }
            current = classes.remove(0);
        }

        final String query = String.format(
                "select obj from %s obj where obj.id > %d", current, last);
        final IObject obj = queryService.findByQuery(query, new Parameters(
                new Filter().page(0, 1)));

        if (obj != null) {
            last = obj.getId();
            // Here we pass the string to prevent $$ CGLIB style issues
            return wrap(current, obj);
        } else {
            // If no object, then reset and recurse
            current = null;
            last = -1;
            return query();
        }
    }

    public boolean more() {
        return classes.size() > 0;
    }

    protected EventLog wrap(String cls, IObject obj) {
        EventLog el = new EventLog();
        el.setEntityType(cls);
        el.setEntityId(obj.getId());
        el.setAction("UPDATE");
        return el;
    }
}

class AllEventLogsLoader extends EventLogLoader {

    long previous = 0;
    long max = -1;
    private boolean more = true;

    @Override
    protected EventLog query() {
        if (max < 0) {
            final IObject lastLog = queryService.findByQuery(
                    "select el from EventLog el order by id desc",
                    new Parameters(new Filter().page(0, 1)));
            max = lastLog.getId();
        }

        EventLog el = queryService.findByQuery("select el from EventLog el "
                + "where el.id > :id order by id", new Parameters(new Filter()
                .page(0, 1)).addId(previous));

        if (el == null) {
            previous = Long.MAX_VALUE;
        } else {
            previous = el.getId();
        }

        if (previous >= max) {
            more = false;
        }
        return el;
    }

    public boolean more() {
        return more;
    }
}