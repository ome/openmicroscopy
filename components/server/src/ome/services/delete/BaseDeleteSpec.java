/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.delete;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import ome.api.IDelete;
import ome.tools.hibernate.ExtendedMetadata;
import ome.tools.hibernate.QueryBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.ListableBeanFactory;

/**
 * {@link DeleteSpec} which takes the id of some id as the root of deletion.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.2.1
 * @see IDelete
 */
public class BaseDeleteSpec implements DeleteSpec, BeanNameAware {

    private final static Log log = LogFactory.getLog(BaseDeleteSpec.class);

    //
    // Bean-creation time values
    //

    /**
     * The paths which make up this delete specification. These count as the
     * steps which will be performed by multiple calls to
     * {@link #delete(Session, int)}
     */
    protected final List<DeleteEntry> entries;

    private/* final */ExtendedMetadata em;

    private/* final */String beanName = null;

    //
    // Initialization-time values
    //

    /**
     * The id of the root type which will be deleted. Note: if this delete comes
     * from a subspec, then the id points to the type of the supertype not the
     * type for this entry itself. For example, if this is "/Dataset" but it is
     * being deleted as a part of "/Project" then the id refers to the project
     * and not the dataset.
     */
    protected long id = -1;

    /**
     * Path of the superspec.
     */
    protected String superspec;

    /**
     * Options passed to the {@link #initialize(long, Map)} method, which may be
     * used during {@link #delete(Session, int)} to alter behavior.
     */
    protected Map<String, String> options;

    /**
     * Simplified constructor, primarily used for testing.
     */
    public BaseDeleteSpec(String name, String... entries) {
        this(Arrays.asList(entries));
        this.beanName = name;
    }

    public BaseDeleteSpec(List<String> entries) {
        this.entries = makeList(entries);
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public void setExtendedMetadata(ExtendedMetadata em) {
        this.em = em;
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

    public void postProcess(ListableBeanFactory factory) {
        for (DeleteEntry entry : entries) {
            entry.postProcess(factory);
        }
    }

    public int initialize(long id, String superspec, Map<String, String> options)
            throws DeleteException {

        if (this.id >= 0) {
            throw new IllegalStateException("Currently initialized!: " + this);
        }

        for (int i = 0; i < entries.size(); i++) {
            DeleteEntry entry = entries.get(i);
            entry.initialize(id, superspec, options);
        }

        this.id = id;
        this.options = options;
        this.superspec = superspec == null ? "" : superspec;
        return entries.size();
    }

    public Iterator<DeleteSpec> walk() {
        return new SubSpecIterator(this);
    }

    public List<DeleteEntry> entries() {
        return new ArrayList<DeleteEntry>(entries);
    }

    public String delete(Session session, int step, DeleteIds deleteIds)
            throws DeleteException {

        final DeleteEntry entry = entries.get(step);
        
        try {
            List<Long> foundIds = deleteIds.getFoundIds(this, step);
            return entry.delete(session, em, superspec, deleteIds, foundIds);
        } finally {

            // If this is the final step, free memory.
            if (step == entries.size()) {
                this.superspec = null;
                this.options = null;
                this.id = -1;
            }

        }
    }



    //
    // Helpers
    //

    private List<DeleteEntry> makeList(List<String> entries) {
        List<DeleteEntry> rv = new ArrayList<DeleteEntry>();
        if (entries != null) {
            for (String entry : entries) {
                rv.add(new DeleteEntry(this, entry));
            }
        }
        return Collections.unmodifiableList(rv);
    }

    /**
     * See interface for documentation.
     */
    public List<List<Long>> backupIds(Session session, List<String[]> paths)
            throws DeleteException {

        List<List<Long>> rv = new ArrayList<List<Long>>();

        for (int s = 0; s < entries.size(); s++) {
            final List<Long> allIds = queryBackupIds(session, s, entries.get(s), null);
            rv.add(allIds);
        }

        return rv;
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

    /**
     * Returns a list of ids for each of the subpaths that was found for the
     * given path. For example, if the entries are:
     *
     * <pre>
     * /Image/Pixels/Channel
     * /Image/Pixels/Channel/StatsInfo
     * /Image/Pixels/Channel/LogicaChannel
     * </pre>
     *
     * then this method would be called with
     *
     * <pre>
     * queryBackupIds(..., ..., "/Image/Pixels/Channel",
     *              ["/Image/Pixels/Channel/StatsInfo", ...]);
     * </pre>
     *
     * and should return something like:
     *
     * <pre>
     * {
     *   "/Image/Pixels/StatsInfo": [1,2,3],
     *   "/Image/Pixels/LogicalChannel": [3,5,6]
     * }
     * </pre>
     *
     * by making calls something like:
     *
     * <pre>
     * select SUB.id from Channel ROOT2
     * join ROOT2.statsInfo SUB
     * join ROOT2.pixels ROOT1
     * join ROOT1.image ROOT0
     * where ROOT0.id = :id
     * </pre>
     *
     * If a superspec of "/Dataset" was the query would be of the form:
     *
     * <pre>
     * select SUB.id from Channel ROOT4
     * join ROOT4.statsInfo SUB
     * join ROOT4.pixels ROOT3
     * join ROOT3.image ROOT2
     * join ROOT2.datasetLinks ROOT1
     * join ROOT1.parent ROOT0
     * where ROOT0.id = :id
     * </pre>
     */
    protected List<Long> queryBackupIds(Session session, int step, DeleteEntry subpath, QueryBuilder and)
        throws DeleteException {

        final String[] sub = subpath.path(superspec);
        final QueryBuilder qb = new QueryBuilder();

        int which = sub.length - 1;
        qb.select("ROOT" + which + ".id");
        walk(qb, subpath);

        qb.where();
        qb.and("ROOT0.id = :id");
        qb.param("id", id);
        if (and != null) {
            qb.and("");
            qb.subselect(and);
        }

        Query q = qb.query(session);
        @SuppressWarnings("unchecked")
        List<Long> results = q.list();

        if (results == null) {
            log.warn(logmsg(subpath, results));
        } else {
            if (log.isDebugEnabled()) {
                log.debug(logmsg(subpath, results));
            }
        }

        return results;

    }

    private String logmsg(DeleteEntry subpath, List<Long> results) {
        String msg = String.format("Found %s id(s) for %s (%s)",
                (results == null ? "null" : results.size()),
                Arrays.asList(subpath.path(superspec)),
                subpath.getOp());
        return msg;
    }

    /**
     * Walks the parts given adding a new relationship between each.
     */
    private void walk(final QueryBuilder qb, final DeleteEntry entry)
            throws DeleteException {
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
     * returned by that value is null, a {@link DeleteException} will be thrown.
     * Otherwise something of the form:
     *
     * <pre>
     * join FROM.rel as TO
     * </pre>
     *
     * will be added to the {@link QueryBuilder}
     */
    protected void join(QueryBuilder qb, String from, String fromAlias,
            String to, String toAlias) throws DeleteException {

        String rel = em.getRelationship(from, to);

        if (rel == null) {
            throw new DeleteException(true, String.format(
                    "Null relationship: %s->%s", from, to));
        }
        qb.join(fromAlias + "." + rel, toAlias, false, false);
    }

    /**
     * Assuming an entry of the form "/A/B/C" is passed, this method generates
     * the query:
     *
     * <pre>
     * delete ROOT2 where id in (select ROOT2.id from C join C.b ROOT1 join b.a ROOT0 where ROOT0.id = :id)
     * </pre>
     */
    private QueryBuilder buildQuery(DeleteEntry entry) throws DeleteException {
        final QueryBuilder sub = new QueryBuilder();

        String[] path = entry.path(superspec);
        String target = "ROOT" + (path.length - 1);
        sub.select(target + ".id");
        walk(sub, entry);
        sub.where();
        sub.and("ROOT0.id = :id");

        final QueryBuilder qb = new QueryBuilder();
        qb.delete(path[path.length - 1]);
        qb.where();
        qb.and("id in ");
        qb.subselect(sub);
        return qb;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("BaseDeleteSpec [" + beanName + ", id=" + id
                + (superspec == null ? "" : ", superspec=" + superspec));
        sb.append("]");
        return sb.toString();
    }

    /**
     * {@link Iterator} which walks returns all {@link DeleteSpec}s which are
     * reachable from the given spec, depth first including the spec itself. A
     * {@link DeleteSpec} is "reachable" if it is the subspec of a
     * {@link DeleteEntry} for a spec.
     */
    public static class SubSpecIterator implements Iterator<DeleteSpec> {

        final DeleteSpec spec;

        final List<DeleteEntry> entries;

        SubSpecIterator sub;

        DeleteSpec subSpec;

        int step = 0;

        boolean done = false;

        public SubSpecIterator(DeleteSpec spec) {
            this.spec = spec;
            this.entries = spec.entries();
            nextIterator();
        }

        private void nextIterator() {
            sub = null;
            subSpec = null;
            for (int i = step; i < entries.size(); i++) {
                step = i + 1;
                DeleteEntry entry = entries.get(i);
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

        public DeleteSpec next() {
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
