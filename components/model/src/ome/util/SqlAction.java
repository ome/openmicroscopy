/*
 *   Copyright 2010-2014 Glencoe Software, Inc. All rights reserved.
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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import com.google.common.collect.ImmutableMap;

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

        final private static Logger log = LoggerFactory.getLogger(SqlAction.class);

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
     * Creates an insert trigger of the given name, for the given table,
     * with the given procedure. No error handling is performed.
     */
    void createInsertTrigger(String name, String table, String procedure);

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
     * @param mimetypes null implies all files are checked
     * @return
     */
    String scriptRepo(long fileId, Set<String> mimetypes);

    int synchronizeJobs(List<Long> ids);

    /**
     * Calls {@link #findRepoFile(String, String, String, Set<String>)}
     * passing null.
     */
    Long findRepoFile(String uuid, String dirname, String basename);

    /**
     * Calls {@link #findRepoFile(String, String, String, Set<String>)}
     */
    Long findRepoFile(String uuid, String dirname, String basename,
            String mimetype);

    /**
     * Lookup the id of an {@link OriginalFile} in a given repository or
     * return null if none is found.
     *
     * @param uuid The UUID of the repository (originalfile.sha1)
     * @param dirname the full directory path minus the file name.
     * @param basename i.e. the filename without any directory path
     * @param mimetype if null, then no mimetype query fragement will be added.
     * @return null if no {@link OriginalFile} is found, otherwise the id.
     */
    Long findRepoFile(String uuid, String dirname, String basename,
            Set<String> mimetypes);

    /**
     * Return a list of original file ids that all have a path value matching
     * the passed dirname in the given repository.
     *
     * @param uuid
     * @param dirname
     * @return possibly empty list of ids.
     */
    List<Long> findRepoFiles(String repoUuid, String dirname);

    /**
     * Record-class which matches _fs_deletelog. It will be used both as the
     * search template for {@link findRepoDeleteLogs(DeleteLog)} as well
     * as {@link deleteRepoDeleteLogs(DeleteLog)}. As a template, any of the
     * fields can be null. As a return value, none of the fields will be null.
     */
    static class DeleteLog implements RowMapper<DeleteLog> {
        public Long eventId;
        public Long fileId;
        public Long ownerId;
        public Long groupId;
        public String path;
        public String name;
        public String repo;
        public DeleteLog mapRow(ResultSet rs, int arg1) throws SQLException {
            DeleteLog dl = new DeleteLog();
            dl.eventId = rs.getLong("event_id");
            dl.fileId = rs.getLong("file_id");
            dl.ownerId = rs.getLong("owner_id");
            dl.groupId = rs.getLong("group_id");
            dl.path = rs.getString("path");
            dl.name = rs.getString("name");
            dl.repo = rs.getString("repo");
            return dl;
        }
        public SqlParameterSource args() {
            MapSqlParameterSource source = new MapSqlParameterSource();
            source.addValue("eid", eventId, java.sql.Types.BIGINT);
            source.addValue("eid", eventId, java.sql.Types.BIGINT);
            source.addValue("fid", fileId, java.sql.Types.BIGINT);
            source.addValue("oid", ownerId, java.sql.Types.BIGINT);
            source.addValue("gid", groupId, java.sql.Types.BIGINT);
            source.addValue("p", path, java.sql.Types.VARCHAR);
            source.addValue("n", name, java.sql.Types.VARCHAR);
            source.addValue("r", repo, java.sql.Types.VARCHAR);
            return source;
        }
        public String toString() {
            boolean first = true;
            StringBuilder sb = new StringBuilder();
            sb.append("DeleteLog<");
            append(sb, first, "event", eventId);
            append(sb, first, "file", fileId);
            append(sb, first, "owner", ownerId);
            append(sb, first, "group", groupId);
            append(sb, first, "path", path);
            append(sb, first, "name", name);
            append(sb, first, "repo", repo);
            sb.append(">");
            return sb.toString();
        }
        private boolean append(final StringBuilder sb, final boolean first,
                final String name, final Object o) {
            if (o != null) {
                if (!first) {
                    sb.append(",");
                }
                sb.append(name);
                sb.append("=");
                sb.append(o.toString());
                return false;
            }
            return first; // No change
        }

    }

    /**
     * Find all {@link DeleteLog} entries which match all of the non-null
     * fields provided in the template.
     *
     * @param template non-null.
     * @return
     */
    List<DeleteLog> findRepoDeleteLogs(DeleteLog template);

    /**
     * Delete all {@link DeleteLog} entries which match all of the non-null
     * fields provided in the template.
     *
     * @param template not-null
     * @return the number of rows deleted.
     */
    int deleteRepoDeleteLogs(DeleteLog template);

    /**
     * Find the path of the repository root.
     * @param uuid a repository UUID
     * @return the repository root
     */
    String findRepoRootPath(String uuid);

    String findRepoFilePath(String uuid, long id);

    List<Long> findRepoPixels(String uuid, String dirname, String basename);

    Long findRepoImageFromPixels(long id);

    /**
     * @param uuid repository identifier
     * @param mimetypes file mimetypes to check; if null, all files;
     */
    int repoScriptCount(String uuid, Set<String> mimetypes);

    Long nextSessionId();

    /**
     * Return all IDs matching the given mimetypes, or all IDs if mimetypes is null.
     */
    List<Long> fileIdsInDb(String uuid, Set<String> mimetypes);

    /**
     * Find the original file IDs among those given that are in the given repository.
     * @param uuid a repository UUID
     * @param fileIds IDs of original files
     * @return those IDs among those given whose original files are in the given repository
     */
    List<Long> filterFileIdsByRepo(String uuid, List<Long> fileIds);

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

    /**
     * @param uuid Repository identifier
     * @param id file identifier
     * @param mimetypes Set of mimetypes to check; if null, all files.
     */
    int isFileInRepo(String uuid, long id, Set<String> mimetypes);

    int removePassword(Long id);

    Date now();

    int updateConfiguration(String key, String value);

    String dbVersion();

    String configValue(String name);

    int delConfigValue(String name);

    int updateOrInsertConfigValue(String name, String value);

    String dbUuid();

    long selectCurrentEventLog(String key);

    /**
     * Returns the percent (e.g. 0-100%) as calculated by the number of rows
     * represented as completed by the configuration table row of this key
     * divided by the total number of rows in the event log. Since this
     * method executes 2 counts over the event log table, it can take a
     * significant amount of time.
     *
     * @param key
             PersistentEventLogLoader key for lookup in the configuration table
     * @return float
     *      value between 0 and 100 of the percent completed
     */
    float getEventLogPercent(String key);

    /**
     * Loads up to "limit" event logs using partioning so that only the
     * <em>last</em>  event log of a particular (type, id) pair is returned.
     * The contents of the object array are:
     * <ol>
     * <li>the id of the event log (Long)</li>
     * <li>the entity type of the event log (String)</li>
     * <li>the entity id of the event log (Long)</li>
     * <li>the action of the event log (String)</li>
     * <li>the number of skipped event logs (Integer)</li>
     * </ol>
     * @param types Collection of entityType strings which should be queried
     * @param actions Collection of ACTION strings which should be queried
     * @param offset Offset to the row which should be queried first
     * @param limit Maximum number of rows (after partionting) which should
     *        be returned.
     * @return
     */
    List<Object[]> getEventLogPartitions(Collection<String> types,
            Collection<String> actions, long offset, long limit);

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

    Map<String, Long> getGroupIds(Collection<String> names);

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

    /**
     * Returns a map of Share ID to Share data blob.
     *
     * @param ids
     *            IDs of Shares for which data blobs are to be returned.
     * @return map of ID to data blob.
     */
    Map<Long, byte[]> getShareData(List<Long> ids);

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

    /**
     * Add a unique message to the DB patch table within the current patch.
     * This method marks the start of the corresponding DB adjustment process.
     * @param version the version of the current DB
     * @param patch the patch of the current DB
     * @param message the new message to note
     */
    void addMessageWithinDbPatchStart(String version, int patch, String message);

    /**
     * Add a unique message to the DB patch table within the current patch.
     * This method marks the end of the corresponding DB adjustment process.
     * @param version the version of the current DB
     * @param patch the patch of the current DB
     * @param message the new message to note
     */
    void addMessageWithinDbPatchEnd(String version, int patch, String message);

    //
    // End PgArrayHelper
    //

    /**
     * Base implementation which can be used
     */
    public static abstract class Impl implements SqlAction {

        protected final Logger log = LoggerFactory.getLogger(this.getClass());

        protected abstract SimpleJdbcOperations _jdbc();

        protected abstract String _lookup(String key);

        protected String printThrowable(Throwable t) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            pw.close();
            return sw.toString();
        }

        public void createInsertTrigger(String name, String table, String procedure) {
            _jdbc().update(String.format("DROP TRIGGER IF EXISTS %s ON %s",
                    name, table));
            _jdbc().update(String.format("CREATE TRIGGER %s AFTER INSERT ON " +
                    "%s FOR EACH ROW EXECUTE PROCEDURE %s",
                    name, table, procedure));
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
        // FILE & MIMETYPE METHODS
        //

        /**
         * Returns the "and_mimetype" clause which must be appended to a given
         * query. Note: the rest of the SQL statement to which this clause is
         * appended must use named SQL parameters otherwise "Can't infer the
         * SQL type to use" will be raised.
         *
         * @param mimetypes If null, then "" will be returned.
         * @param params sql parameter source to be passed to JDBC methods.
         * @return Possibly empty String, but never null.
         */
        protected String addMimetypes(Collection<String> mimetypes, Map<String, Object> params) {
            if (mimetypes != null) {
                params.put("mimetypes", mimetypes);
                return _lookup("and_mimetype"); //$NON-NLS-1$
            }
            return "";
        }

        public Long findRepoFile(String uuid, String dirname, String basename) {
            return findRepoFile(uuid, dirname, basename, (Set<String>) null);
        }

        public Long findRepoFile(String uuid, String dirname, String basename,
                String mimetype) {
            return findRepoFile(uuid, dirname, basename,
                    mimetype == null ? null : Collections.singleton(mimetype));
        }

        public Long findRepoFile(String uuid, String dirname, String basename,
                Set<String> mimetypes) {

            String findRepoFileSql = _lookup("find_repo_file"); //$NON-NLS-1$
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("repo", uuid);
            params.put("path", dirname);
            params.put("name", basename);
            findRepoFileSql += addMimetypes(mimetypes, params);
            try {
                return _jdbc().queryForLong(findRepoFileSql, params);
            } catch (EmptyResultDataAccessException e) {
                return null;
            }
        }

        public int repoScriptCount(String uuid, Set<String> mimetypes) {
            String query = _lookup("repo_script_count"); //$NON-NLS-1$
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("repo", uuid);
            query += addMimetypes(mimetypes, params);
            return _jdbc().queryForInt(query, params);
        }


        public int isFileInRepo(String uuid, long id, Set<String> mimetypes) {
            String query = _lookup("is_file_in_repo"); //$NON-NLS-1$
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("repo", uuid);
            params.put("file", id);
            query += addMimetypes(mimetypes, params);
            return _jdbc().queryForInt(query, params);
        }

        public List<Long> fileIdsInDb(String uuid, Set<String> mimetypes) {
            String query = _lookup("file_id_in_db"); //$NON-NLS-1$
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("repo", uuid);
            query += addMimetypes(mimetypes, params);
            return _jdbc().query(query, new IdRowMapper(), params);
        }

        public List<Long> filterFileIdsByRepo(String uuid, List<Long> fileIds) {
            final Map<String, Object> arguments = ImmutableMap.of("repo", uuid, "ids", fileIds);
            try {
                return _jdbc().query(_lookup("find_files_in_repo"), new IdRowMapper(), arguments);
            } catch (EmptyResultDataAccessException e) {
                return Collections.emptyList();
            }
        }

        //
        // OTHER FILE METHODS
        //

        public List<Long> findRepoFiles(String uuid, String dirname) {
            try {
                return _jdbc().query(_lookup("find_repo_files"),
                        new IdRowMapper(), uuid, dirname);
            } catch (EmptyResultDataAccessException e) {
                return Collections.emptyList();
            }
        }

        public List<DeleteLog> findRepoDeleteLogs(DeleteLog template) {
            try {
                return _jdbc().query(_lookup("find_repo_delete_logs"),
                        template, template.args());
            } catch (EmptyResultDataAccessException e) {
                return Collections.emptyList();
            }
        }

        public int deleteRepoDeleteLogs(DeleteLog template) {
            return _jdbc().update(_lookup("delete_repo_delete_logs"),
                    template.args());
        }

        public String findRepoRootPath(String uuid) {
            try {
                return _jdbc().queryForObject(_lookup("find_repo_root_path"), //$NON-NLS-1$
                        String.class, uuid);
            } catch (EmptyResultDataAccessException erdae) {
                return null;
            }
        }

        public String findRepoFilePath(String uuid, long id) {
            try {
                return _jdbc().queryForObject(_lookup("find_repo_file_path"), //$NON-NLS-1$
                        String.class, id, uuid);
            } catch (EmptyResultDataAccessException erdae) {
                return null;
            }
        }

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

        public Map<String, Long> getGroupIds(Collection<String> names) {
            final Map<String, Long> rv = new HashMap<String, Long>();
            if (names == null || names.size() == 0) {
                return rv;
            }

            final Map<String, Collection<String>> params =
                    new HashMap<String, Collection<String>>();
            params.put("names", names);

            RowMapper<Object> mapper = new RowMapper<Object>(){
                @Override
                public Object mapRow(ResultSet arg0, int arg1)
                        throws SQLException {
                    Long id = arg0.getLong(1);
                    String name = arg0.getString(2);
                    rv.put(name, id);
                    return null;
                }};
            _jdbc().query(_lookup("get_group_ids"), //$NON-NLS-1$
                    mapper, params);
            return rv;
        }

        public String getPasswordHash(Long experimenterID) {
            String stored;
            try {
                stored = _jdbc().queryForObject(
                        _lookup("password_hash"), //$NON-NLS-1$
                        String.class, experimenterID);
            } catch (EmptyResultDataAccessException e) {
                stored = null; // This means there's not one.
            }
            return stored;
        }

        public Long getUserId(String userName) {
            Long id;
            try {
                id = _jdbc().queryForObject(_lookup("user_id"), //$NON-NLS-1$
                        Long.class, userName);
            } catch (EmptyResultDataAccessException e) {
                id = null; // This means there's not one.
            }
            return id;
        }

        public List<String> getUserGroups(String userName) {
            List<String> roles;
            try {
                roles = _jdbc().query(_lookup("user_groups"), //$NON-NLS-1$
                        new RowMapper<String>() {
                            public String mapRow(ResultSet rs, int rowNum)
                                    throws SQLException {
                                return rs.getString(1);
                            }
                        }, userName);
            } catch (EmptyResultDataAccessException e) {
                roles = null; // This means there's not one.
            }
            return roles == null ? new ArrayList<String>() : roles;
        }

        public ExperimenterGroup groupInfoFor(String table, long id) {
            try {
                return _jdbc().queryForObject(String.format(
                   _lookup("get_group_info"), table), //$NON-NLS-1$
                    new RowMapper<ExperimenterGroup>() {
                        @Override
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

        public String scriptRepo(long fileId, Set<String> mimetypes) {

            String query = _lookup("file_repo_of_script"); //$NON-NLS-1$
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("file", fileId);
            query += addMimetypes(mimetypes, params);

            try {
                return _jdbc().queryForObject(query, String.class, params);
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

        public float getEventLogPercent(String key) {
            Float value = _jdbc().queryForObject(
                _lookup("log_loader_percent"), Float.class, key); //$NON-NLS-1$
            return value;
        }

        public List<Object[]> getEventLogPartitions(Collection<String> types,
                Collection<String> actions, long offset, long limit) {
            final String query = _lookup("log_loader_partition"); // $NON_NLS-1$
            final Map<String, Object> params = new HashMap<String, Object>();
            params.put("types", types);
            params.put("actions", actions);
            params.put("currentid", offset);
            params.put("max", limit);
            return _jdbc().query(query,
                new RowMapper<Object[]>() {
                    @Override
                    public Object[] mapRow(ResultSet arg0, int arg1)
                            throws SQLException {
                        return new Object[] {
                            arg0.getLong(1),
                            arg0.getString(2),
                            arg0.getLong(3),
                            arg0.getString(4),
                            arg0.getInt(5)
                        };
                    }}, params);
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

        @Override
        public void addMessageWithinDbPatchStart(String version, int patch, String message) {
            final Map<String, Object> parameters =
                    ImmutableMap.<String, Object>of("version", version, "patch", patch, "message", message);
            _jdbc().update(_lookup("adjust_within_patch.start"), parameters);
        }

        @Override
        public void addMessageWithinDbPatchEnd(String version, int patch, String message) {
            final Map<String, Object> parameters =
                    ImmutableMap.<String, Object>of("version", version, "patch", patch, "message", message);
            _jdbc().update(_lookup("adjust_within_patch.end"), parameters);
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

        public Map<Long, byte[]> getShareData(List<Long> ids) {
            final Map<Long, byte[]> rv = new HashMap<Long, byte[]>();
            if (ids == null || ids.isEmpty()) {
                return rv;
            }

            final Map<String, List<Long>> params = new HashMap<String, List<Long>>();
            params.put("ids", ids);

            RowMapper<Object> mapper = new RowMapper<Object>() {
                @Override
                public Object mapRow(ResultSet arg0, int arg1)
                        throws SQLException {
                    Long id = arg0.getLong(1);
                    byte[] data = arg0.getBytes(2);
                    rv.put(id, data);
                    return null;
                }
            };
            _jdbc().query(_lookup("share_data"), //$NON-NLS-1$
                    mapper, params);
            return rv;
        }
    }

}
