/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.delete;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ome.api.IDelete;
import ome.model.IObject;
import ome.security.basic.CurrentDetails;
import ome.system.EventContext;
import ome.tools.hibernate.ExtendedMetadata;
import ome.tools.hibernate.QueryBuilder;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.perf4j.StopWatch;
import org.perf4j.commonslog.CommonsLogStopWatch;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.ListableBeanFactory;

/**
 * Single value of the map entries from spec.xml. A value such as "HARD;/Roi"
 * specifies that the operation with the name "HARD" should be applied to the
 * given path, and that the given path should use a pre-existing specification.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.2.1
 * @see IDelete
 */
public class DeleteEntry {

    private final static Log log = LogFactory.getLog(DeleteEntry.class);
    
    public enum Op {

        /**
         * Default operation. If a delete is not possible, i.e. it fails with a
         * {@link org.hibernate.exception.ConstraintViolationException} or
         * similar, then the failure will cause the entire command to fail as an
         * error.
         */
        HARD,

        /**
         * Delete is attempted, but the exceptions which would make a
         * {@link #HARD} operation fail lead only to warnings.
         */
        SOFT,

        /**
         * Prevents the delete from being carried out. If an entry has a subspec,
         * then the entire subgraph will not be deleted. In some cases,
         * specifically {@link AnnotationDeleteSpec} this value may be
         * vetoed by {@link DeleteSpec#overrideKeep()}.
         */
        KEEP,

        /**
         * Permits the use of force to remove objects even against the
         * permission system. (This option cannot override low-level
         * DB constraints)
         */
        FORCE,

        REAP,

        ORPHAN,

        /**
         * Nulls a particular field of the target rather than deleting it.
         * This is useful for situations where one user has generated data
         * from another user, as with projections.
         *
         * <em>WARNING:</em>Currently, NULL can only be used for the
         * Pixels.relatedTo relationship.
         */
        NULL;
    }

    final public static Op DEFAULT = Op.HARD;

    final private static Pattern opRegex = Pattern
            .compile("^([^;]+?)(;([^;]*?))?(;([^;]*?))?$");

    final private DeleteSpec self;

    final private String name;

    final private String[] parts;

    final private String path;

    /**
     * Operation which should be performed for this entry.
     *
     * No longer protected since the {@link #initialize(String, Map)} phase can
     * change the operation based on the options map.
     */
    private Op op;

    /**
     * {@link DeleteSpec Subspec} found by looking for the {@link #name} of this
     * {@link DeleteEntry} during {@link #postProcess(ListableBeanFactory)}. If
     * this is non-null, then many actions will have to iterate over all the
     * {@link #subStepCount} steps of the {@link #subSpec}.
     */
    /* final */private DeleteSpec subSpec;
    
    /**
     * Number of steps in the {@link #subSpec}, calculated during
     * {@link #initialize(long, String, Map)}.
     */
    /* final */private int subStepCount = 0;
    
    /**
     * Value of the superspec passed in during {@link #initialize(long, String, Map)}.
     * This will be used to determine where this entry is in the overall graph,
     * as opposed to just within its own {@link #self DeleteSpec}.
     */
    /* final */private String superspec;

    /**
     * Value of the target id passed in during {@link #initialize(long, String, Map)}.
     */
    /* final */private long id;

    public DeleteEntry(DeleteSpec self, String value) {
        checkArgs(self, value);
        this.self = self;
        final Matcher m = getMatcher(value);
        this.name = getName(m);
        this.op = getOp(m);
        this.path = getPath(m);
        this.parts = split(name);
    }

    public DeleteEntry(DeleteSpec self, String name, DeleteEntry entry) {
        this.self = self;
        this.name = name;
        this.op = entry.op;
        this.path = entry.path;
        this.parts = split(name);
    }

    public String getName() {
        return name;
    }

    public Op getOp() {
        return op;
    }

    /**
     * Splits the name of the entry into the path components. Any suffixes
     * prefixed with a "+" are stripped.
     */
    private static String[] split(String name) {
        if (name == null) {
            return new String[0];
        }
        String[] parts0 = name.split("/");
        String part = null;
        for (int i = 0; i < parts0.length; i++) {
            part = parts0[i];
            int idx = part.indexOf("+");
            if (idx > 0) {
                parts0[i] = part.substring(0, idx);
            }
        }
        String[] parts1 = new String[parts0.length - 1];
        System.arraycopy(parts0, 1, parts1, 0, parts1.length);
        return parts1;
    }

    private static String[] prepend(String superspec, String path,
            String[] ownParts) {
        String[] superParts = split(superspec);
        String[] pathParts = split(path);
        String[] totalParts = new String[superParts.length + pathParts.length
                + ownParts.length];
        System.arraycopy(superParts, 0, totalParts, 0, superParts.length);
        System.arraycopy(pathParts, 0, totalParts, superParts.length,
                pathParts.length);
        System.arraycopy(ownParts, 0, totalParts, superParts.length
                + pathParts.length, ownParts.length);
        return totalParts;
    }

    public DeleteSpec getSubSpec() {
        return subSpec;
    }

    public String[] path(String superspec) {
        return prepend(superspec, path, parts);
    }

    //
    // Helpers
    //

    protected void checkArgs(Object... values) {
        for (Object value : values) {
            if (value == null) {
                throw new FatalBeanException("Null argument");
            }
        }
    }

    protected Matcher getMatcher(String operation) {
        Matcher m = opRegex.matcher(operation);
        if (!m.matches()) {
            throw new FatalBeanException(String.format(
                    "Operation %s does not match pattern %s", operation,
                    opRegex));
        }
        return m;
    }

    protected String getName(Matcher m) {
        String name = m.group(1);
        if (name == null || name.length() == 0) { // Should be prevent by regex
            throw new FatalBeanException("Empty name");
        }
        return name;
    }

    protected Op getOp(Matcher m) {
        String name = null;
        name = m.group(3);
        if (name == null || name.length() == 0) {
            return DEFAULT;
        }

        try {
            return Op.valueOf(name);
        } catch (IllegalArgumentException iae) {
            throw new FatalBeanException(String.format(
                    "Unknown operation %s for entry %s", name, name));
        }
    }

    protected String getPath(Matcher m) {
        String path = m.group(5);
        if (path == null) {
            return "";
        }
        return path;
    }

    /**
     * Load the spec which has the same name as this entry, but do not load the
     * spec if the name matches {@link #name}. This is called early in the
     * {@link DeleteEntry} lifecycle, by {@link DeleteSpec}.
     */
    protected void postProcess(ListableBeanFactory factory) {
        if (name.equals(self.getName())) {
            return;
        } else if (factory.containsBean(name)) {
            this.subSpec = factory.getBean(name, DeleteSpec.class);
            this.subSpec.postProcess(factory);
        }
    }

    /**
     * Called during {@link DeleteSpec#initialize(long, String, Map)} to give
     * the entry a chance to modify its {@link #op} based on the options and to
     * initialize subspecs.
     *
     * The superspec is passed in so that both the absolute path as well as the
     * last path element can be checked. Further, a key of "/" apply to all
     * entries.
     */
    public int initialize(long id, String superspec, Map<String, String> options) throws DeleteException {
        
        this.id = id;
        this.superspec = superspec;
        
        if (options != null) {
            final String[] path = path(superspec);
            final String absolute = "/" + StringUtils.join(path, "/");
            final String last = "/" + path[path.length - 1];

            String option = null;
            for (String string : Arrays.asList(absolute, last, "/")) {
                option = options.get(string);
                if (option != null) {
                    String[] parts = option.split(";"); // Just in case
                    op = Op.valueOf(parts[0]);
                    break;
                }

            }
        }

        if (subSpec != null) {
            if (subSpec == this) {
                throw new DeleteException(true, "Self-reference subspec:"
                        + this);
            }
            subStepCount = subSpec.initialize(id, superspec + this.path,
                    options);
        }
        return subStepCount;

    }


    /**
     * A KEEP setting is a way of putting a KEEP suggestion to vote. If there is
     * a subspec, however, that vote must be passed down. If the KEEP is vetoed,
     * it is the responsiblity of the subspec to make sure that only the proper
     * parts are kept or not kept.
     */
    public boolean skip() {
        if (Op.KEEP.equals(this.getOp())) {
            DeleteSpec spec = this.getSubSpec();
            if (spec != null) {
                return ! spec.overrideKeep();
            }
        }
        return false;
    }
    
    public String delete(Session session, CurrentDetails details,
            ExtendedMetadata em, String superspec, DeleteIds deleteIds, List<Long> ids)
        throws DeleteException {
        
        if (skip()) {
            if (log.isDebugEnabled()) {
                log.debug("skipping " + this);
            }
            return ""; // EARLY EXIT!
        }
        
        
        StringBuilder sb = new StringBuilder();

        DeleteSpec subSpec = getSubSpec();
        if (subSpec == null) {
            sb.append(execute(session, details, em, deleteIds, ids));
        } else {
            for (int i = 0; i < subStepCount; i++) {
                // TODO refactor this into a single location with
                // execute below. This may require setting the
                // "superOp" on subSpecs. Is there a need for
                // handling longer chains of ops??
                String cause = null;
                try {
                    cause = subSpec.delete(session, i, deleteIds);
                } catch (ConstraintViolationException cve) {
                    if (Op.SOFT.equals(this.getOp())) {
                        sb.append(cause);
                    } else {
                        throw cve;
                    }
                }
            }
        }

        return sb.toString();
    }

    /**
     * If ids are non-empty, then calls a simple
     * "delete TABLE where id in (:ids)"; otherwise, generates a query via
     * {@link #buildQuery(DeleteEntry)} and uses the root "id"
     *
     * Originally copied from DeleteBean.
     */
    protected String execute(final Session session, final CurrentDetails details, 
            final ExtendedMetadata em, final DeleteIds deleteIds, final List<Long> ids)
        throws DeleteException {

        final String[] path = this.path(superspec);
        final String table = path[path.length - 1];
        final String str = StringUtils.join(path, "/");

        if (ids.size() == 0) {
            if (log.isDebugEnabled()) {
                log.debug("No ids found for " + str);
            }
            return ""; // Early exit!
        }
        

        // Hardcoded nulling of references to the given Pixels
        // The NULL operation should actually be set on
        // /Image/Pixels/Pixels not /Image/Pixels
        // BUT requires reversing the relationship:
        // /Image/Pixels/<>/Pixels
        QueryBuilder nullOp = null;
        if (Op.NULL.equals(getOp())) {
            // If this is a null operation, we don't want to delete the row,
            // but just modify a value. NB: below we also prevent this from
            // being raised as a delete event. TODO: refactor out to Op
            nullOp = new QueryBuilder();
            nullOp.update(table);
            nullOp.append("set relatedTo = null");
            nullOp.where();
            nullOp.and("relatedTo.id = :id");
        }

        final QueryBuilder qb = new QueryBuilder();
        qb.delete(table);
        qb.where();
        qb.and("id = :id");
        if (!Op.FORCE.equals(getOp())) {
            permissionsClause(details, qb);
        }

        final StringBuilder rv = new StringBuilder();
        final List<Long> actualDeletes = new ArrayList<Long>();

        Query q;
        final StopWatch sw = new CommonsLogStopWatch();
        for (Long id : ids) {
            String sp = savepoint(session);
            try {

                if (nullOp != null) {
                    nullOp.param("id", id);
                    log.fatal(nullOp.toString());
                    q = nullOp.query(session);
                    q.executeUpdate();
                }

                qb.param("id", id);
                q = qb.query(session);
                int count = q.executeUpdate();
                if (count > 0) {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Deleted %s from %s: root=%s", id, str, this.id));
                    }
                } else {
                    if (log.isInfoEnabled()) {
                        log.warn(String.format("Missing delete %s from %s: root=%s", id, str, this.id));
                    }
                }
                release(session, sp);
                actualDeletes.add(id); // After release!
                rv.append("");
            } catch (ConstraintViolationException cve) {
                rollback(session, sp);
                String cause = "ConstraintViolation: " + cve.getConstraintName();
                if (DeleteEntry.Op.SOFT.equals(this.getOp())) {
                    log.debug(String.format("Could not delete softly %s: %s due to %s",
                            str, this.id, cause));
                    rv.append(cause);
                } else {
                    log.info(String.format("Failed to delete %s: %s due to %s",
                            str, this.id, cause));
                    throw cve;
                }
            } finally {
                sw.stop("omero.delete." + table + "." + id);
            }
        }

        // When full NULL operation is activated, the following if statement
        // will be necessary.
        // if (!Op.NULL.equals(getOp())) {
        // Since this isn't a delete operation, we don't want
        // to pass this ID out.
        // }

        Class<IObject> k = em.getHibernateClass(table);
        deleteIds.addDeletedIds(table, k, actualDeletes);

        return rv.toString();
    }

    void call(Session session, String call, String savepoint) {
        try {
            session.connection().prepareCall(call + savepoint).execute();
        } catch (Exception e) {
            RuntimeException re = new RuntimeException("Failed to '" + call
                    + savepoint + "'");
            re.initCause(e);
            throw re;
        }
    }

    String savepoint(Session session) {
        String savepoint = UUID.randomUUID().toString();
        savepoint = savepoint.replaceAll("-", "");
        call(session, "SAVEPOINT DEL", savepoint);
        return savepoint;
    }

    void release(Session session, String savepoint) {
        call(session, "RELEASE SAVEPOINT DEL", savepoint);
    }

    void rollback(Session session, String savepoint) {
        call(session, "ROLLBACK TO SAVEPOINT DEL", savepoint);
    }

    //
    // MISC
    //

    @Override
    public String toString() {
        return "DeleteEntry [name=" + name + ", parts="
                + Arrays.toString(parts) + ", op=" + op + ", path=" + path
                + (subSpec == null ? "" : ", subSpec=" + subSpec.getName())
                + "]";
    }
    
    
    /**
     * Appends a clause to the {@link QueryBuilder} based on the current user.
     * 
     * If the user is an admin like root, then nothing is appened, and any
     * delete is permissible. If the user is a leader of the current group,
     * then the object must be in the current group. Otherwise, the object
     * must belong to the current user.
     */
    public static void permissionsClause(CurrentDetails details, QueryBuilder qb) {
        EventContext ec = details.getCurrentEventContext();
        if (!ec.isCurrentUserAdmin()) {
            if (ec.getLeaderOfGroupsList().contains(ec.getCurrentGroupId())) {
                qb.and("details.group.id = :gid");
                qb.param("gid", ec.getCurrentGroupId());
            } else {
                // This is only a regular user, then the object must belong to
                // him/her
                qb.and("details.owner.id = :oid");
                qb.param("oid", ec.getCurrentUserId());
            }
        }
    }

}
