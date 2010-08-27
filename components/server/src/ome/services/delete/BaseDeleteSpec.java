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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ome.api.IDelete;
import ome.tools.hibernate.ExtendedMetadata;
import ome.tools.hibernate.QueryBuilder;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.beans.factory.BeanNameAware;

/**
 * {@link DeleteSpec} which takes the id of an image as the root of deletion.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.2.1
 * @see IDelete
 */
public class BaseDeleteSpec implements DeleteSpec, BeanNameAware {

    /**
     * The paths which make up this delete specification. These count as the
     * steps which will be performed by multiple calls to
     * {@link #delete(Session, int)}
     */
    protected final List<DeleteEntry> entries;

    private/* final */ExtendedMetadata em;

    private/* final */String beanName = null;

    /**
     * The id of the root type which will be deleted. Note: if this delete
     * comes from a subspec, then the id points to the type of the supertype
     * not the type for this entry itself. For example, if this is "/Dataset"
     * but it is being deleted as a part of "/Project" then the id refers to the
     * project and not the dataset.
     */
    private/* final */long id;

    /**
     * Path of the superspec.
     */
    private/* final */String superspec;

    /**
     * Options passed to the {@link #initialize(long, Map)} method, which may be
     * used during {@link #delete(Session, int)} to alter behavior.
     */
    private/* final */Map<String, String> options;

    /**
     * A list of ids per step which should be deleted. These are precalculated
     * on {@link #initialize(long, Map)} so that foreign key constraints which
     * require a higher level object to be deleted first, can be removed.
     *
     * For example,
     *
     * <pre>
     * /Channel
     * /Channel/StatsInfo
     * </pre>
     *
     * requires the Channel to be deleted first, but without the Channel,
     * there's no way to detect which StatsInfo should be removed. Therefore,
     * {@link #backupIds} in this case would contain:
     *
     * <pre>
     * [
     *  null,      # Nothing for Channel.
     *  [1,2,3],   # The ids of all StatsInfo object which should be removed.
     * ]
     * </pre>
     */
    private/* final */List<List<Long>> backupIds;

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

    public void postProcess(Map<String, DeleteSpec> specs) {
        for (DeleteEntry entry : entries) {
            entry.postProcess(specs);
        }
    }

    public int initialize(long id, String superspec, Map<String, String> options)
            throws DeleteException {
        this.id = id;
        this.options = options;
        this.superspec = superspec == null ? "" : superspec;
        return entries.size();
    }

    public String delete(Session session, int step) throws DeleteException {

        if (step == 0) {
            this.backupIds = backupIds(session);
        }

        try {
            DeleteEntry entry = entries.get(step);
            DeleteSpec subSpec = entry.getSubSpec();
            if (subSpec != null) {
                if (subSpec == this) {
                    throw new DeleteException(true, "Self-reference subspec:" + this);
                }

                int subStep = subSpec.initialize(id, superspec + entry.path, options);
                for (int i = 0; i < subStep; i++) {
                    subSpec.delete(session, i);
                }
            } else {
                List<Long> ids = backupIds.get(step);
                execute(session, entry, ids);
            }

            return null; // No warning
        } finally {

            // Free the backupIds value after this step.
            backupIds.set(step, null);

            // If this is the final step, free memory.
            if (step == entries.size()) {
                this.backupIds = null;
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
     * If a given path is deleted before it's a subpath, this points to a
     * one-to-one relationship. If the first object is deleted without having
     * loaded the later one, then there will be no way to find the dangling
     * object. Therefore, we load those objects first.
     */
    public List<List<Long>> backupIds(Session session)
            throws DeleteException {

        List<List<Long>> rv = new ArrayList<List<Long>>();

        for (int s = 0; s < entries.size(); s++) {
            // initially set to null
            rv.add(null);
        }

        for (int s = 0; s < entries.size(); s++) {
            DeleteEntry current = entries.get(s);
            Map<DeleteEntry, Integer> subpaths = new HashMap<DeleteEntry, Integer>();
            for (int i = s + 1; i < entries.size(); i++) { // won't check self
                DeleteEntry possSubPath = entries.get(i);
                if (possSubPath.name.startsWith(current.name)) {
                    subpaths.put(possSubPath, i);
                }
            }

            // if we've found something replace the null.
            if (subpaths.size() > 0) {
                Map<DeleteEntry, List<Long>> m = queryBackupIds(session,
                        current, subpaths.keySet());
                for (Map.Entry<DeleteEntry, List<Long>> x : m.entrySet()) {
                    final DeleteEntry subpath = x.getKey();
                    final List<Long> ids = x.getValue();
                    final int idx = subpaths.get(subpath).intValue();
                    if (rv.get(idx) != null) {
                        throw new DeleteException(true,
                                "Found multiple routes to path:" + subpath);
                    } else {
                        rv.set(idx, ids);
                    }
                }
            }
        }

        return rv;
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
    protected Map<DeleteEntry, List<Long>> queryBackupIds(Session session,
            DeleteEntry entry, Set<DeleteEntry> subpaths)
            throws DeleteException {

        final Map<DeleteEntry, List<Long>> rv = new HashMap<DeleteEntry, List<Long>>();
        final String[] path = entry.path(superspec);

        for (DeleteEntry subpath : subpaths) {
            final String[] sub = subpath.path(superspec);

            final QueryBuilder qb = new QueryBuilder();
            qb.select("SUB.id");
            walk(qb, entry);

            if ((path.length + 1) != sub.length) {
                throw new DeleteException(true,
                        "Currently only a single subpath step is supported: "
                                + subpath);
            }

            int which = path.length - 1;
            String last = path[which];
            String lastsub = sub[sub.length-1];

            join(qb, last, "ROOT" + which, lastsub, "SUB");

            qb.where();
            qb.and("ROOT0.id = :id");
            qb.param("id", id);

            Query q = qb.query(session);
            @SuppressWarnings("unchecked")
            List<Long> results = q.list();
            rv.put(subpath, results);

        }
        return rv;
    }

    /**
     * Walks the parts given adding a new relationship between each.
     */
    private void walk(final QueryBuilder qb, final DeleteEntry entry)
            throws DeleteException {
        String[] path = entry.path(superspec);
        qb.from(path[0], "ROOT0");
        for (int p = 1; p < path.length; p++) {
            String p_1 = path[p-1];
            String p_0 = path[p];
            join(qb, p_1, "ROOT" + (p - 1), p_0, "ROOT"+ p);
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
        qb.delete(path[path.length-1]);
        qb.where();
        qb.and("id in ");
        qb.subselect(sub);
        return qb;
    }

    /**
     * If ids are non-empty, then calls a simple
     * "delete TABLE where id in (:ids)"; otherwise, generates a query via
     * {@link #buildQuery(DeleteEntry)} and uses the root "id"
     *
     * Originally copied from DeleteBean.
     */
    private int execute(final Session session,
            DeleteEntry entry, List<Long> ids) throws DeleteException {

        Query q;
        final String[] path = entry.path(superspec);
        final String table = path[path.length-1];

        if (ids == null) {
            q = buildQuery(entry).query(session);
            q.setParameter("id", id);
        } else {
            if (ids.size() == 0) {
                return 0;
            }
            q = session.createQuery("delete " + table + " where id in (:ids)");
            q.setParameterList("ids", ids);

        }
        return q.executeUpdate();
    }

}
