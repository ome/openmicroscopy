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

import ome.conditions.InternalException;
import ome.model.IObject;
import ome.model.internal.Permissions;
import ome.model.internal.Permissions.Right;
import ome.model.internal.Permissions.Role;
import ome.model.meta.ExperimenterGroup;
import ome.system.EventContext;
import ome.tools.hibernate.ExtendedMetadata;
import ome.tools.hibernate.QueryBuilder;
import ome.util.SqlAction;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.ListableBeanFactory;

/**
 * {@link GraphSpec} which takes the id of some id as the root of action.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.2.1
 * @see IGraph
 * @deprecated will be removed in OMERO 5.2, so use the
 * <a href="http://www.openmicroscopy.org/site/support/omero5.1/developers/Server/ObjectGraphs.html">new graphs implementation</a>
 */
@Deprecated
@SuppressWarnings("deprecation")
public class BaseGraphSpec implements GraphSpec, BeanNameAware {

    private final static Logger log = LoggerFactory.getLogger(BaseGraphSpec.class);

    //
    // Bean-creation-time values
    //

    /**
     * The paths which make up this graph specification. These count as the
     * steps which will be performed by multiple calls to {@link GraphState#execute(int)}
     */
    protected final List<GraphEntry> entries;

    protected/* final */ExtendedMetadata em;

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
    public IObject load(Session session) throws GraphException {

        final GraphEntry subpath = new GraphEntry(this, this.beanName);
        final String[] sub = subpath.path(superspec);
        final QueryBuilder qb = new QueryBuilder();

        qb.select("ROOT"+(sub.length-1));
        walk(sub, subpath, qb);
        qb.where();
        // From queryBackupIds
        qb.and("ROOT0.id = :id");
        qb.param("id", id);

        return (IObject) qb.query(session).uniqueResult();

    }

    public ExperimenterGroup groupInfo(SqlAction sql) {
        final GraphEntry subpath = new GraphEntry(this, this.beanName);
        final String[] path = subpath.path(superspec);

        return sql.groupInfoFor(path[0], id);
    }

    protected QueryBuilder createQueryBuilder(String[] sub, GraphEntry subpath) {
        QueryBuilder qb = new QueryBuilder();
        qb.select("distinct");
        return qb;
    }

    protected Query buildQuery(String[] sub, GraphEntry subpath,
            QueryBuilder qb, QueryBuilder and, Session session) throws GraphException {

        final List<String> which = new ArrayList<String>();
        for (int i = 0; i < sub.length; i++) {
            which.add("ROOT" + i + ".id as ROOT" + i);
        }
        qb.select(which.toArray(new String[sub.length]));
        walk(sub, subpath, qb);

        qb.where();
        // Moving to seqParams due to SQL weirdness.
        qb.and("ROOT0.id = ?");
        qb.param(0, id);
        if (and != null) {
            qb.and("");
            qb.subselect(and);
        }
        return qb.query(session);
    }

    protected List<List<Long>> runQuery(String[] sub, GraphEntry subpath, Query q, Session session) {
        StopWatch sw = new Slf4JStopWatch();

        @SuppressWarnings("unchecked")
        List<List<Long>> results = q.list();
        sw.stop("omero.graph.query." + StringUtils.join(sub, "."));

        if (results == null) {
            log.warn(logmsg(subpath, results));
        } else {
            if (log.isDebugEnabled()) {
                log.debug(logmsg(subpath, results));
            }
        }
        return results;
    }

    protected long[][] parseResults(String[] sub, GraphEntry subpath, List<List<Long>> results) {

        // If only one result is returned, results == List<Long> and otherwise
        // List<Object[]>. Parsing into List<List<Long>>
        long[][] rv = new long[results.size()][sub.length];
        for (int i = 0; i < results.size(); i++) {
            Object v = results.get(i);
            Class<?> k = v == null ? Object.class : v.getClass();
            long[] arr = new long[sub.length];
            if (Long.class.isAssignableFrom(k)) {
                arr[0] = (Long) v;
            } else if (Object[].class.isAssignableFrom(k)) {
                Object[] objs = (Object[]) v;
                for (int j = 0; j < arr.length; j++) {
                    arr[j] = (Long) objs[j];
                }
            } else if (v instanceof List) {
                @SuppressWarnings("unchecked")
                List<Long> l = (List<Long>) v;
                for (int j = 0; j < arr.length; j++) {
                    arr[j] = l.get(j);
                }
            } else {
                throw new IllegalArgumentException("Unknown type:" + v);
            }
            rv[i] = arr;
        }
        return rv;
    }

    /*
     * See interface documentation.
     */
    public long[][] queryBackupIds(Session session, int step, GraphEntry subpath, QueryBuilder and)
        throws GraphException {

        final String[] sub = subpath.path(superspec);
        final QueryBuilder qb = createQueryBuilder(sub, subpath);
        final Query q = buildQuery(sub, subpath, qb, and, session);
        final List<List<Long>> results = runQuery(sub, subpath, q, session);
        return parseResults(sub, subpath, results);

    }
    public QueryBuilder chgrpQuery(EventContext ec, String table, GraphOpts opts) {
        final QueryBuilder qb = new QueryBuilder();
        qb.update(table);
        qb.append("set details.group.id = :grp ");
        qb.where();
        qb.and("id = :id");
        if (!opts.isForce()) {
            permissionsClause(ec, qb, false);
        }
        return qb;
    }

    public QueryBuilder chmodQuery(EventContext ec, String table, GraphOpts opts) {
            final QueryBuilder qb = new QueryBuilder();
            qb.update(table);
            qb.append("set owner_id = :usr   ");
            qb.where();
            qb.and("id = :id");
            if (!opts.isForce()) {
                permissionsClause(ec, qb, false);
            }
            return qb;
    }

    public QueryBuilder deleteQuery(EventContext ec, String table, GraphOpts opts) {
        final QueryBuilder qb = new QueryBuilder();
        qb.delete(table);
        qb.where();
        qb.and("id = :id");
        if (!opts.isForce()) {
            permissionsClause(ec, qb, false);
        }
        return qb;
    }

    /**
     * Appends a clause to the {@link QueryBuilder} based on the current user.
     *
     * If the user is an admin like root, then nothing is appended, and any
     * action is permissible. If the user is a leader of the current group, then
     * the object must be in the current group. Otherwise, the object must
     * belong to the current user.
     */
    public void permissionsClause(EventContext ec, QueryBuilder qb, boolean sqlQuery) {

        if (ec.isCurrentUserAdmin()) {
            return; // EARLY EXIT
        }

        final Permissions p = ec.getCurrentGroupPermissions();
        if (p == Permissions.DUMMY) {
            throw new InternalException("EventContext has DUMMY permissions");
        }

        // If this is less than a rwrw group and the user is not an admin,
        // then we want we require either ownership of the object or
        // leadership of the group.
        if (!p.isGranted(Role.GROUP, Right.WRITE)) {
            if (ec.getLeaderOfGroupsList().contains(ec.getCurrentGroupId())) {
                if (sqlQuery) {
                    qb.and("group_id = :gid");
                } else {
                    qb.and("details.group.id = :gid");
                }
                qb.param("gid", ec.getCurrentGroupId());
            } else {
                // This is only a regular user, then the object must belong to
                // him/her
                if (sqlQuery) {
                    qb.and("owner_id = :oid");
                } else {
                    qb.and("details.owner.id = :oid");
                }
                qb.param("oid", ec.getCurrentUserId());
            }
        }
    }

    private String logmsg(GraphEntry subpath, List<List<Long>> results) {
        String msg = String.format("Found %s id(s) for %s",
                (results == null ? "null" : results.size()),
                Arrays.asList(subpath.log(superspec)));
        return msg;
    }

    /**
     * Walks the parts given adding a new relationship between each. If this
     * is a synthetic "Fileset" relationship, use the workaround methods
     * {@link #joinDataset(QueryBuilder, String, String)} and
     * {@link #joinPlate(QueryBuilder, String, String)}.
     */
    protected void walk(String[] sub, final GraphEntry entry, final QueryBuilder qb)
            throws GraphException {

        qb.from(sub[0], "ROOT0");

        for (int p = 1; p < sub.length; p++) {
            String p_1 = sub[p - 1];
            String p_0 = sub[p];

            String r_1 = "ROOT" + (p-1);
            String r_0 = "ROOT" + (p);

            if ("Fileset".equals(p_0)) {
                if ("Dataset".equals(p_1)) {
                    joinDataset(qb, r_1, r_0);
                    continue;
                } else if ("Plate".equals(p_1)) {
                    joinPlate(qb, r_1, r_0);
                    continue;
                }
            }

            // Default operation
            join(qb, p_1, r_1, p_0, r_0);

        }

    }

    protected void joinDataset(QueryBuilder qb, String dataset, String fileset) {
        qb.join(dataset + ".imageLinks", "links", false, false);
        qb.join("links.child", "image", false, false);
        qb.join("image.fileset", fileset, false, false);
    }

    protected void joinPlate(QueryBuilder qb, String plate, String fileset) {
        qb.join(plate + ".wells", "well", false, false);
        qb.join("well.wellSamples", "wellSample", false, false);
        qb.join("wellSample.image", "image", false, false);
        qb.join("image.fileset", fileset, false, false);
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

            // Try the reverse first
            rel = em.getRelationship(to, from);

            if (rel == null) {
                throw new GraphException(String.format(
                     "Null relationship: %s->%s", from, to));
            }

            // If it exists, we do a
            qb.append(", ");
            qb.append(to);
            qb.appendSpace();
            qb.append(toAlias);
            qb.appendSpace();
            qb.where();
            qb.and(" ");
            qb.append(toAlias);
            qb.append(".");
            qb.append(rel);
            qb.append(".id = ");
            qb.append(fromAlias);
            qb.append(".id");
            qb.appendSpace();

        } else {
            qb.join(fromAlias + "." + rel, toAlias, false, false);
         }

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
            // If we currently have a sub, then we test it.
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
