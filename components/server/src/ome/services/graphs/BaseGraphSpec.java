/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.graphs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import ome.model.IObject;
import ome.security.basic.CurrentDetails;
import ome.tools.hibernate.ExtendedMetadata;
import ome.tools.hibernate.QueryBuilder;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.perf4j.StopWatch;
import org.perf4j.commonslog.CommonsLogStopWatch;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.ListableBeanFactory;

/**
 * {@link GraphSpec} which takes the id of some id as the root of action.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.2.1
 * @see IGraph
 */
public class BaseGraphSpec implements GraphSpec, BeanNameAware {

    private final static Log log = LogFactory.getLog(BaseGraphSpec.class);

    //
    // Bean-creation time values
    //

    /**
     * The paths which make up this graph specification. These count as the
     * steps which will be performed by multiple calls to {@link GraphState#execute(int)}
     */
    protected final List<GraphEntry> entries;

    private/* final */ExtendedMetadata em;

    /**
     * Current user information for determining if the current caller
     * is an admin or the owner of an object.
     */
    private /*final*/ CurrentDetails details;

    private/* final */String beanName = null;

    //
    // Initialization-time values
    //

    /**
     * The id of the root type which will be processed. Note: if this action comes
     * from a subspec, then the id points to the type of the supertype not the
     * type for this entry itself. For example, if this is "/Dataset" but it is
     * being processed as a part of "/Project" then the id refers to the project
     * and not the dataset.
     */
    protected long id = -1;

    /**
     * Path of the superspec.
     */
    protected String superspec;

    /**
     * Options passed to the {@link #initialize(long, Map)} method, which may be
     * used during {@link GraphState#execute(int)} to alter behavior.
     */
    protected Map<String, String> options;

    /**
     * Simplified constructor, primarily used for testing.
     */
    public BaseGraphSpec(String name, String... entries) {
        this(Arrays.asList(entries));
        this.beanName = name;
    }

    public BaseGraphSpec(List<String> entries) {
        this.entries = makeList(entries);
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public void setExtendedMetadata(ExtendedMetadata em) {
        this.em = em;
    }

    public void setCurrentDetails(CurrentDetails details) {
        this.details = details;
    }

    public CurrentDetails getCurrentDetails() {
        return this.details;
    }

    //
    // Interface. See descriptions there.
    //

    public String getName() {
        return this.beanName;
    }

    public String getSuperSpec() {
        return this.superspec;
    }

    public Class<IObject> getHibernateClass(String table) {
        return this.em.getHibernateClass(table);
    }

    public void postProcess(ListableBeanFactory factory) {
        for (GraphEntry entry : entries) {
            entry.postProcess(factory);
        }
    }

    public int initialize(long id, String superspec, Map<String, String> options)
            throws GraphException {

        if (this.id >= 0) {
            throw new IllegalStateException("Currently initialized!: " + this);
        }

        for (int i = 0; i < entries.size(); i++) {
            GraphEntry entry = entries.get(i);
            entry.initialize(id, superspec, options);
        }

        this.id = id;
        this.options = options;
        this.superspec = superspec == null ? "" : superspec;
        return entries.size();
    }

    public Iterator<GraphSpec> walk() {
        return new SubSpecIterator(this);
    }

    public List<GraphEntry> entries() {
        return new ArrayList<GraphEntry>(entries);
    }

    //
    // Helpers
    //

    private List<GraphEntry> makeList(List<String> entries) {
        List<GraphEntry> rv = new ArrayList<GraphEntry>();
        if (entries != null) {
            for (String entry : entries) {
                rv.add(new GraphEntry(this, entry));
            }
        }
        return Collections.unmodifiableList(rv);
    }

    /**
     * Always returns false. See interface for documentation.
     */
    public boolean overrideKeep() {
        return false;
    }

    /**
     * Returns true iff lhs[i] == rhs[i] for all i in rhs. For example,
     *
     * <pre>
     * startsWith(new String[]{"a","b"}, new String[]{"a"}) == true;
     * startsWith(new String[]{"a","b"}, new String[]{"a", "c"}) == false;
     * </pre>
     *
     * Note: if this method returns true, and the arrays are of equal length
     * then the two arrays are equals.
     *
     * @param lhs
     *            not null array of all not null Strings
     * @param rhs
     *            not null array of all not null Strings
     */
    boolean startsWith(String[] lhs, String[] rhs) {
        if (rhs.length > lhs.length) {
            return false;
        }
        for (int i = 0; i < rhs.length; i++) {
            if (!rhs[i].equals(lhs[i])) {
                return false;
            }
        }
        return true;
    }


    /*
     * See interface documentation.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public IObject load(Session session) throws GraphException {

        final GraphEntry subpath = new GraphEntry(this, this.beanName);
        final String[] sub = subpath.path(superspec);
        final QueryBuilder qb = new QueryBuilder();

        qb.select("ROOT"+(sub.length-1));
        walk(qb, subpath);
        qb.where();
        // From queryBackupIds
        qb.and("ROOT0.id = :id");
        qb.param("id", id);

        return (IObject) qb.query(session).uniqueResult();

    }

    /*
     * See interface documentation.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public long[][] queryBackupIds(Session session, int step, GraphEntry subpath, QueryBuilder and)
        throws GraphException {

        final String[] sub = subpath.path(superspec);
        final QueryBuilder qb = new QueryBuilder();

        final List<String> which = new ArrayList<String>();
        for (int i = 0; i < sub.length; i++) {
            which.add("ROOT" + i + ".id");
        }
        qb.select(which.toArray(new String[sub.length]));
        walk(qb, subpath);

        qb.where();
        qb.and("ROOT0.id = :id");
        qb.param("id", id);
        if (and != null) {
            qb.and("");
            qb.subselect(and);
        }

        Query q = qb.query(session);
        StopWatch sw = new CommonsLogStopWatch();
        List<List<Long>> results = q.list();
        sw.stop("omero.graph.query." + StringUtils.join(sub, "."));

        if (results == null) {
            log.warn(logmsg(subpath, results));
        } else {
            if (log.isDebugEnabled()) {
                log.debug(logmsg(subpath, results));
            }
        }

        // If only one result is returned, results == List<Long> and otherwise
        // List<Object[]>. Parsing into List<List<Long>>
        long[][] rv = new long[results.size()][sub.length];
        for (int i = 0; i < results.size(); i++) {
            Object v = results.get(i);
            Class k = v == null ? Object.class : v.getClass();
            long[] arr = new long[sub.length];
            if (Long.class.isAssignableFrom(k)) {
                arr[0] = (Long) v;
            } else if (Object[].class.isAssignableFrom(k)) {
                Object[] objs = (Object[]) v;
                for (int j = 0; j < arr.length; j++) {
                    arr[j] = (Long) objs[j];
                }
            } else if (v instanceof List) {
                List l = (List) v;
                for (int j = 0; j < arr.length; j++) {
                    arr[j] = (Long) l.get(j);
                }
            } else {
                throw new IllegalArgumentException("Unknown type:" + v);
            }
            rv[i] = arr;
        }
        return rv;

    }

    private String logmsg(GraphEntry subpath, List<List<Long>> results) {
        String msg = String.format("Found %s id(s) for %s",
                (results == null ? "null" : results.size()),
                Arrays.asList(subpath.log(superspec)));
        return msg;
    }

    /**
     * Walks the parts given adding a new relationship between each.
     */
    private void walk(final QueryBuilder qb, final GraphEntry entry)
            throws GraphException {
        String[] path = entry.path(superspec);
        qb.from(path[0], "ROOT0");
        for (int p = 1; p < path.length; p++) {
            String p_1 = path[p - 1];
            String p_0 = path[p];
            join(qb, p_1, "ROOT" + (p - 1), p_0, "ROOT" + p);
        }
    }

    /**
     * Used to generate a join statement on the {@link QueryBuilder} making use
     * of {@link ExtendedMetadata#getRelationship(String, String). If the value
     * returned by that value is null, a {@link GraphException} will be thrown.
     * Otherwise something of the form:
     *
     * <pre>
     * join FROM.rel as TO
     * </pre>
     *
     * will be added to the {@link QueryBuilder}
     */
    protected void join(QueryBuilder qb, String from, String fromAlias,
            String to, String toAlias) throws GraphException {

        String rel = em.getRelationship(from, to);

        if (rel == null) {
            throw new GraphException(String.format(
                    "Null relationship: %s->%s", from, to));
        }
        qb.join(fromAlias + "." + rel, toAlias, false, false);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("BaseGraphSpec [" + beanName + ", id=" + id
                + (superspec == null ? "" : ", superspec=" + superspec));
        sb.append("]");
        return sb.toString();
    }

    /**
     * {@link Iterator} which walks returns all {@link GraphSpec}s which are
     * reachable from the given spec, depth first including the spec itself. A
     * {@link GraphSpec} is "reachable" if it is the subspec of a
     * {@link GraphEntry} for a spec.
     */
    public static class SubSpecIterator implements Iterator<GraphSpec> {

        final GraphSpec spec;

        final List<GraphEntry> entries;

        SubSpecIterator sub;

        GraphSpec subSpec;

        int step = 0;

        boolean done = false;

        public SubSpecIterator(GraphSpec spec) {
            this.spec = spec;
            this.entries = spec.entries();
            nextIterator();
        }

        private void nextIterator() {
            sub = null;
            subSpec = null;
            for (int i = step; i < entries.size(); i++) {
                step = i + 1;
                GraphEntry entry = entries.get(i);
                subSpec = entry.getSubSpec();
                if (subSpec != null) {
                    sub = new SubSpecIterator(subSpec);
                    break;
                }
            }
        }

        public boolean hasNext() {
            // If we curerntly have a sub, then we test it.
            if (sub != null) {
                return true;
            } else if (step < entries.size() - 1) {
                return true;
            } else {
                return !done;
            }
        }

        public GraphSpec next() {
            if (sub != null) {
                if (sub.hasNext()) {
                    return sub.next();
                } else {
                    nextIterator();
                    return next();
                }
            } else {
                if (!done) {
                    done = true;
                    return spec;
                }
            }
            throw new NoSuchElementException();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

}
