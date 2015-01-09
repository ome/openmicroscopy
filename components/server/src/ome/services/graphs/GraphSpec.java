/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.graphs;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ome.model.IObject;
import ome.model.meta.ExperimenterGroup;
import ome.security.basic.CurrentDetails;
import ome.system.EventContext;
import ome.tools.hibernate.QueryBuilder;
import ome.util.SqlAction;

import org.hibernate.Session;
import org.springframework.beans.factory.ListableBeanFactory;

/**
 * Specification of a graph operation. These instances are defined in
 * Spring XML files (e.g. ome/services/spec.xml) as non-singletons,
 * i.e each time a request is
 * made for a new {@link GraphSpecFactory} one of each {@link GraphSpec} is
 * initialized and gathered into the factory. A single thread, then, can
 * repeatedly call {@link #initialize(long, Map)} on the {@link GraphSpec}
 * instances.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.2.1
 * @see IGraph
 * @deprecated will be removed in OMERO 5.2, so use the
 * <a href="http://www.openmicroscopy.org/site/support/omero5.1/developers/Server/ObjectGraphs.html">new graphs implementation</a>
 */
@Deprecated
@SuppressWarnings("deprecation")
public interface GraphSpec {

    /**
     * The name of this specification. Usually the first component of the
     * entries in spec.xml
     */
    String getName();

    /**
     * Specification of where this {@link GraphSpec} is attached under another
     * {@GraphSpec}. The reverse of
     * {@link GraphEntry#getSubSpec()}.
     */
    String getSuperSpec();

    /**
     * Gives all specs a chance to reference subspecs.
     */
    void postProcess(ListableBeanFactory factory);

    /**
     * Called as each action is started. This instance will only be used
     * serially (i.e. by one thread) and so this is safe. When the last step is
     * reached, {@link GraphState#execute(int)} can take clean up actions.
     *
     * @param id
     *            identifier of the root object which defines the graph to be
     *            processed.
     * @param supersec
     *            points to the relationship between the root object and the
     *            current graph. In many cases, this value will be null so that
     *            the current object is taken to be the root, but if this is a
     *            subspec, or a non-standard naming is being used, then the
     *            superspec will be used.
     * @param options
     *            possibly null or empty map of options which can override the
     *            operations provided in the definition of the specification.
     *            For example, if the spec "/Image" defines "/Image/Annotation"
     *            as "HARD" (the default), then the options map could contain
     *
     *            <pre>
     * {"/Image/Annotation":"ORPHAN"}
     * </pre>
     *
     *            to modify that setting.
     * @return number of steps which are to be processed.
     */
    int initialize(long id, String supersec, Map<String, String> options)
            throws GraphException;

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

    /**
     * If a given path is processed before its sub-path, this points to a
     * one-to-one relationship. If the first object is processed without having
     * loaded the later one, then there will be no way to find the dangling
     * object. Therefore, we load those objects first.
     *
     * In the case of superspecs, we also store the root ids for the sub-spec to
     * handle cases such as links, etc.
     *
     * Returns a list of all root ids per step which should be processed. These are
     * precalculated on {@link #initialize(long, Map)} so that foreign key
     * constraints which require a higher level object to be processed first, can
     * be processed.
     *
     * For example,
     *
     * <pre>
     * /Channel
     * /Channel/StatsInfo
     * </pre>
     *
     * requires the Channel to be processed first, but without the Channel,
     * there's no way to detect which StatsInfo should be removed. Therefore,
     * {@link #backupIds} in this case would contain:
     *
     * <pre>
     * [
     *  null,      # Nothing for Channel.
     *  [1,2,3],   # The ids of all StatsInfo object which should be removed.
     * ]
     * </pre>
     *
     * @param paths
     *            Non-null, non-modifiable list of all paths for the current
     *            action graph. This is used to detect if a path in some subspec
     *            is going to detach a later graph which needs then to have its
     *            ids loaded.
     */
    long[][] queryBackupIds(Session session, int step, GraphEntry subpath, QueryBuilder and)
            throws GraphException;

    /**
     * Return a {@link QueryBuilder} which has been properly initialized to take the parameters "id"
     * and "grp" and then have {@link QueryBuilder#query(Session)} called.
     */
    QueryBuilder chgrpQuery(EventContext ec, String table, GraphOpts opts);

    QueryBuilder chmodQuery(EventContext ec, String table, GraphOpts opts);

    QueryBuilder deleteQuery(EventContext ec, String table, GraphOpts opts);

    /**
     * Returns an iterator over all subspecs and their subspecs, depth-first.
     */
    Iterator<GraphSpec> walk();

    /**
     * For some {@link GraphSpec} type/option combinations, a "KEEP" setting
     * may need to be overridden. This method allows implementors to say that
     * KEEPing must be performed on a per {@link GraphEntry} basis as opposed
     * to for the whole {@link GraphSpec subspec}.
     */
    boolean overrideKeep();

    /**
     * Returns a copy of the list of {@link GraphEntry} instances contained in
     * this {@link GraphSpec}
     */
    List<GraphEntry> entries();

    /**
     * Return the Hibernate type (ome.model.*) for the given table.
     */
    Class<IObject> getHibernateClass(String table);

    /**
     * Loads the object that this spec points to. Performs similar logic to
     * the {@link #queryBackupIds(Session, int, GraphEntry, QueryBuilder)}
     * but only returns the last element.
     *
     * @return possibly null.
     */
    IObject load(Session session) throws GraphException;

    /**
     * Like {@link #load(Session)} uses the current table and id information
     * to look up information on the given target.
     */
    ExperimenterGroup groupInfo(SqlAction sql);
}
