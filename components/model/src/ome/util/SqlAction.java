/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import ome.conditions.InternalException;
import ome.model.core.Channel;
import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.model.meta.ExperimenterGroup;
import ome.model.stats.StatsInfo;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * Single wrapper for all JDBC activities.
 *
 * This interface is meant to replace <em>all</em> uses of both
 * {@link SimpleJdbcTemplate} and
 * {@link org.hibernate.Session#createSQLQuery(String)} for the entire OMERO
 * code base.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.2.1
 * @see <a href="http://trac.openmicroscopy.org.uk/ome/ticket/73">#73</a>
 * @see <a href="http://trac.openmicroscopy.org.uk/ome/ticket/2684">#2684</a>
 */
public interface SqlAction {

    public static class IdRowMapper implements RowMapper<Long> {
        public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getLong(1);
        }
    }

    public static class LoggingSqlAction implements MethodInterceptor {

        final private static Log log = LogFactory.getLog(SqlAction.class);

        public Object invoke(MethodInvocation arg0) throws Throwable {
            if (log.isDebugEnabled()) {
                log.debug(String.format("%s.%s(%s)",
                        arg0.getThis(),
                        arg0.getMethod().getName(),
                        Arrays.deepToString(arg0.getArguments())));
            }
            return arg0.proceed();
        }

    }
    
    /**
     * Stores the current event context information in a temporary table
     * so that triggers can make use of them.
     *
     * @param eventId
     * @param userId
     * @param groupId
     */
    void prepareSession(long eventId, long userId, long groupId);

    /**
     * Allows the specific database implementations a chance to modify
     * queries.
     *
     * @param query String query (non-null) which is in effect.
     * @param key Key of the argument e.g. (:ids)
     * @param value value which has been passed in for that parameter.
     * @return Returns a query replacement.
     * @see ticket:3961
     */
    String rewriteHql(String query, String key, Object value);

    /**
     * Creates a temporary table filled with the given ids and returns its
     * name. The table is only available for the period if the transaction.
     */
    String createIdsTempTable(Collection<Long> ids);

    /**
     * Returns true if the given string is the UUID of a session that is
     * currently active.
     *
     * @param sessionUUID
     *            NOT NULL.
     * @return
     */
    boolean activeSession(String sessionUUID);

    /**
     * Returns the permissions for the given group id.
     */
    long getGroupPermissions(long id);

    /**
     * Return a mostly unloaded {@link ExperimenterGroup} object containing
     * only the id, name, and permissions.
     */
    ExperimenterGroup groupInfoFor(String table, long id);

    String fileRepo(long fileId);

    /**
     * Similar to {@link #fileRepo(long)}, but only returns values for files
     * which are also scripts. Null may be returned
     *
     * @param fileId
     * @return
     */
    String scriptRepo(long fileId);

    int synchronizeJobs(List<Long> ids);

    Long findRepoFile(String uuid, String dirname, String basename,
            String string);

    String findRepoFilePath(String uuid, long id);

    List<Long> findRepoPixels(String uuid, String dirname, String basename);

    Long findRepoImageFromPixels(long id);

    int repoScriptCount(String uuid);

    Long nextSessionId();

    List<Long> fileIdsInDb(String uuid);

    Map<String, Object> repoFile(long value);

    /**
     * Returns arrays of longs for the following SQL return values:
     * <code>experimenter, eventlog, entityid as pixels, rownumber</code>
     *
     * The oldest N eventlogs with action = "PIXELDATA" and entitytype = "ome.model.core.Pixels"
     * is found <em>per user</em> and returned. Multiple eventlogs are returned
     * per user in order to support multi-threading. Duplicate pixel ids
     * are stripped.
     */
    List<long[]> nextPixelsDataLogForRepo(String repo, long lastEventId, int howmany);

    long countFormat(String name);

    int insertFormat(String name);

    int closeSessions(String uuid);

    int closeNodeSessions(String uuid);

    int closeNode(String uuid);

    long nodeId(String internal_uuid);

    int insertSession(Map<String, Object> params);

    Long sessionId(String uuid);

    int isFileInRepo(String uuid, long id);

    int removePassword(Long id);

    Date now();

    int updateConfiguration(String key, String value);

    String dbVersion();

    String configValue(String name);

    int delConfigValue(String name);

    int updateOrInsertConfigValue(String name, String value);

    String dbUuid();

    long selectCurrentEventLog(String key);

    void setCurrentEventLog(long id, String key);

    void delCurrentEventLog(String key);

    /**
     * The implementation of this method guarantees that even if the current
     * transaction fails that the value found will not be used by another
     * transaction. Database implementations can choose whether to do this
     * at the procedure level or by using transaction PROPAGATION settings
     * in Java.
     *
     * @param segmentName
     * @param incrementSize
     * @return
     * @see ticket:3697
     * @see ticket:3253
     */
    long nextValue(String segmentName, int incrementSize);

    long currValue(String segmentName);

    void insertLogs(List<Object[]> batchData);

    List<Map<String, Object>> roiByImageAndNs(final long imageId,
            final String ns);

    List<Long> getShapeIds(long roiId);

    String dnForUser(Long id);

    List<Map<String, Object>> dnExperimenterMaps();

    void setUserDn(Long experimenterID, String dn);

    boolean setUserPassword(Long experimenterID, String password);

    String getPasswordHash(Long experimenterID);

    Long getUserId(String userName);

    List<String> getUserGroups(String userName);

    void setFileRepo(long id, String repoId);

    void setPixelsNamePathRepo(long pixId, String name, String path,
            String repoId);

    long setStatsInfo(Channel ch, StatsInfo si);

    // TODO this should probably return an iterator.
    List<Long> getDeletedIds(String entityType);

    void createSavepoint(String savepoint);

    void releaseSavepoint(String savepoint);

    void rollbackSavepoint(String savepoint);

    void deferConstraints();

    //
    // Previously PgArrayHelper
    //

    /**
     * Returns only the (possibly empty) keys which are set on the given
     * original file. If the given original file cannot be found, null is
     * returned.
     */
    List<String> getPixelsParamKeys(long id) throws InternalException;

    /**
     * Loads all the (possibly empty) params for the given original file. If the
     * id is not found, null is returned.
     */
    Map<String, String> getPixelsParams(final long id) throws InternalException;

    /**
     * Retrieves the name, path and repo for the given pixels set. If the
     * id is not found, null is returned.
     */
    List<String> getPixelsNamePathRepo(final long id) throws InternalException;

    /**
     * Resets the entire original file "params" field.
     */
    int setPixelsParams(final long id, Map<String, String> params);

    /**
     * Appends "{key, value}" onto the original file "params" field or replaces
     * the value if already present.
     */
    int setPixelsParam(final long id, final String key, final String value);

    /**
     * Returns only the (possibly empty) keys which are set on the given
     * original file. If the given original file cannot be found, null is
     * returned.
     */
    List<String> getFileParamKeys(long id) throws InternalException;

    /**
     * Loads all the (possibly empty) params for the given original file. If the
     * id is not found, null is returned.
     */
    Map<String, String> getFileParams(final long id) throws InternalException;

    /**
     * Resets the entire original file "params" field.
     */
    int setFileParams(final long id, Map<String, String> params);

    /**
     * Appends "{key, value}" onto the original file "params" field or replaces
     * the value if already present.
     */
    int setFileParam(final long id, final String key, final String value);

    Set<String> currentUserNames();

    int changeGroupPermissions(Long id, Long internal);

    int changeTablePermissionsForGroup(String table, Long id, Long internal);

    //
    // End PgArrayHelper
    //

    /**
     * Base implementation which can be used
     */
    public static abstract class Impl implements SqlAction {

        protected final Log log = LogFactory.getLog(this.getClass());

        protected abstract SimpleJdbcOperations _jdbc();

        protected abstract String _lookup(String key);

        protected String printThrowable(Throwable t) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            pw.close();
            return sw.toString();
        }

        public String rewriteHql(String query, String key, Object value) {
            if (value instanceof Collection) {
                @SuppressWarnings({ "rawtypes" })
                Collection l = (Collection) value;
                if (l.size() > 1000) {
                    for (Object o : l) {
                        if (!(o instanceof Long)) {
                            log.debug("Not replacing query; non-long");
                            return query;
                        }
                    }
                    if (query.contains("(:ids)")) {
                        @SuppressWarnings("unchecked")
                        String temp = createIdsTempTable(l);
                        String repl = "temp_ids_cursor('"+temp+"')";
                        query = query.replace("(:ids)", "(" + repl + ")");
                    }
                }
            }
            return query;
        }


        public String createIdsTempTable(Collection<Long> ids) {
            String name = UUID.randomUUID().toString().replaceAll("-", "");
            List<Object[]> batch = new ArrayList<Object[]>();
            for (Long id : ids) {
                batch.add(new Object[]{name, id});
            }
            _jdbc().batchUpdate("insert into temp_ids (key, id) values (?, ?)", batch);
            return name;
        }

        //
        // SECURITY
        //

        public int closeNodeSessions(String uuid) {
            return _jdbc().update(
                    _lookup("update_node_sessions"), uuid); //$NON-NLS-1$
        }

        public int closeNode(String uuid) {
            return _jdbc().update(
                    _lookup("update_node"), uuid); //$NON-NLS-1$
        }


        public boolean setUserPassword(Long experimenterID, String password) {
            int results = _jdbc().update(_lookup("update_password"), //$NON-NLS-1$
                    password, experimenterID);
            if (results < 1) {
                results = _jdbc().update(_lookup("insert_password"), //$NON-NLS-1$
                        experimenterID, password, null);
            }
            return results >= 1;
        }

        public int changeGroupPermissions(Long id, Long internal) {
            return _jdbc().update(_lookup("update_permissions_for_group"),
                    internal, id);
        }

        public int changeTablePermissionsForGroup(String table, Long id, Long internal) {
            String sql = _lookup("update_permissions_for_table");
            sql = String.format(sql, table);
            return _jdbc().update(sql, internal, id);
        }

        //
        // FILES
        //

        public List<long[]> nextPixelsDataLogForRepo(String repo, long lastEventId, int rows) {
            final RowMapper<long[]> rm = new RowMapper<long[]>() {
                public long[] mapRow(ResultSet arg0, int arg1)
                        throws SQLException {
                    long[] rv = new long[4];
                    rv[0] = arg0.getLong(1);
                    rv[1] = arg0.getLong(2);
                    rv[2] = arg0.getLong(3);
                    rv[3] = arg0.getLong(4);
                    return rv;
                }};
            try {
                if (repo == null) {
                    return _jdbc().query(
                            _lookup("find_next_pixels_data_per_user_for_null_repo"), // $NON-NLS-1$
                            rm, lastEventId, rows);
                } else {
                    return _jdbc().query(
                            _lookup("find_next_pixels_data__per_user_for_repo"), // $NON-NLS-1$
                            rm, lastEventId, repo, rows);
                }
            } catch (EmptyResultDataAccessException erdae) {
                return null;
            }
        }

        public long getGroupPermissions(long groupId) {
            return _jdbc().queryForObject(
                    _lookup("get_group_permissions"), Long.class, //$NON-NLS-1$
                    groupId);
        }

        public ExperimenterGroup groupInfoFor(String table, long id) {
            try {
                return _jdbc().queryForObject(String.format(
                   _lookup("get_group_info"), table), //$NON-NLS-1$
                    new RowMapper<ExperimenterGroup>() {
                        /*@Override - JDK5 support */
                        public ExperimenterGroup mapRow(ResultSet arg0, int arg1)
                            throws SQLException {
                            ExperimenterGroup group = new ExperimenterGroup();
                            group.setId(arg0.getLong(1));
                            group.setName(arg0.getString(2));
                            Permissions p = Utils.toPermissions(arg0.getLong(3));
                            group.getDetails().setPermissions(p);
                            return group;
                        }
                    }, id);
            } catch (EmptyResultDataAccessException erdae) {
                return null;
            }
        }

        public String fileRepo(long fileId) {
            return _jdbc().queryForObject(
                    _lookup("file_repo"), String.class, //$NON-NLS-1$
                    fileId);
        }

        public String scriptRepo(long fileId) {
            try {
                return _jdbc().queryForObject(
                    _lookup("file_repo_of_script"), String.class, //$NON-NLS-1$
                    fileId);
            } catch (EmptyResultDataAccessException erdae) {
                return null;
            }
        }

        //
        // PIXELS
        //

        public long setStatsInfo(Channel ch, StatsInfo si) {
            final Details d = ch.getDetails();
            final long id = nextValue("seq_statsinfo", 1);
            _jdbc().update(_lookup("stats_info_creation"), //$NON-NLS-1$
                    id, Utils.internalForm(d.getPermissions()),
                    si.getGlobalMax(), si.getGlobalMin(),
                    d.getCreationEvent().getId(), d.getGroup().getId(),
                    d.getOwner().getId(), d.getUpdateEvent().getId());
            _jdbc().update(_lookup("stats_info_set_on_channel"), //$NON-NLS-1$
                    id, ch.getId());
            return id;
        }


        //
        // CONFIGURATION
        //

        public String configValue(String key) {
            try {
                return _jdbc().queryForObject(_lookup("config_value_select"), //$NON-NLS-1$
                        String.class, key);
            } catch (EmptyResultDataAccessException erdae) {
                return null;
            }
        }

        public int delConfigValue(String key) {
            return _jdbc().update(_lookup("config_value_delete"), //$NON-NLS-1$
                    key);
        }

        public int updateOrInsertConfigValue(String name, String value) {
            int count = _jdbc().update(_lookup("config_value_update"), // $NON-NLS-1$
                   value, name);
            if (count == 0) {
                count = _jdbc().update(_lookup("config_value_insert"), // $NON-NLS-1$
                        name, value);
            }
            return count;
        }

        public long selectCurrentEventLog(String key) {
            String value = _jdbc().queryForObject(
                _lookup("log_loader_query"), String.class, key); //$NON-NLS-1$
            return Long.valueOf(value);
        }

        public void setCurrentEventLog(long id, String key) {

            int count = _jdbc().update(
                _lookup("log_loader_update"), Long.toString(id), //$NON-NLS-1$
                key);

            if (count == 0) {
                _jdbc().update(
                        _lookup("log_loader_insert"),  //$NON-NLS-1$
                        key, Long.toString(id));
            }
        }

        public void delCurrentEventLog(String key) {
            _jdbc().update(
                _lookup("log_loader_delete"), key); //$NON-NLS-1$

        }

        //
        // DISTINGUISHED NAME (DN)
        // These methods guarantee that an empty or whitespace only string
        // will be treated as a null DN. See #4833. For maximum protection,
        // we are performing checks here in code as well as in the SQL.
        //

        public String dnForUser(Long id) {
            String dn;
            try {
                dn = _jdbc().queryForObject(
                        _lookup("dn_for_user"), String.class, id); //$NON-NLS-1$
            } catch (EmptyResultDataAccessException e) {
                dn = null; // This means there's not one.
            }

            if (dn == null || dn.trim().length() == 0) {
                return null;
            }
            return dn;
        }

        public List<Map<String, Object>> dnExperimenterMaps() {
            List<Map<String, Object>> maps = _jdbc().queryForList(_lookup("dn_exp_maps")); //$NON-NLS-1$
            List<Map<String, Object>> copy = new ArrayList<Map<String, Object>>();
            for (Map<String, Object> map : maps) {
                if (map.keySet().iterator().next().trim().length() > 0) {
                    copy.add(map);
                }
            }
            return copy;
        }

        public void setUserDn(Long experimenterID, String dn) {

            if (dn != null && dn.trim().length() == 0) {
                dn = null; // #4833
            }

            int results = _jdbc().update(_lookup("set_user_dn"), //$NON-NLS-1$
                    dn, experimenterID);
            if (results < 1) {
                results = _jdbc().update(_lookup("insert_password"), //$NON-NLS-1$
                        experimenterID, null, dn);
            }

        }
    }

}
