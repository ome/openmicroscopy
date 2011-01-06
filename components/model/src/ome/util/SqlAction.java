/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ome.conditions.InternalException;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.UncategorizedSQLException;
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
 * @see <a href="http://trac.openmicroscopy.org.uk/omero/ticket/73">#73</a>
 * @see <a href="http://trac.openmicroscopy.org.uk/omero/ticket/2684">#2684</a>
 */
public interface SqlAction {

    public static class IdRowMapper implements RowMapper<Long> {
        public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
            return (Long) rs.getLong(1);
        }
    }

    /**
     * Returns true if the given string is the UUID of a session that is
     * currently active.
     *
     * @param sessionUUID
     *            NOT NULL.
     * @return
     */
    boolean activeSession(String sessionUUID);

    String fileRepo(long fileId);

    int synchronizeJobs(List<Long> ids);

    Long findRepoFile(String uuid, String dirname, String basename,
            String string);

    int repoScriptCount(String uuid);

    Long nextSessionId();

    List<Long> fileIdsInDb(String uuid);

    Map<String, Object> repoFile(long value);

    long countFormat(String name);

    int insertFormat(String name);

    int closeSessions(String uuid);

    long nodeId(String internal_uuid);

    int insertSession(Map<String, Object> params);

    Long sessionId(String uuid);

    int isFileInRepo(String uuid, long id);

    int removePassword(Long id);

    Date now();

    int updateConfiguration(String key, String value);

    String dbVersion();

    String configValue(String key);

    String dbUuid();

    long selectCurrentEventLog(String key);

    void setCurrentEventLog(long id, String key);

    void delCurrentEventLog(String key);

    long nextValue(String segmentValue, int incrementSize);

    void insertLogs(List<Object[]> batchData);

    List<Map<String, Object>> roiByImageAndNs(final long imageId,
            final String ns);

    List<Long> getShapeIds(long roiId);

    String dnForUser(Long id);

    List<Map<String, Object>> dnExperimenterMaps();

    void setUserDn(Long experimenterID, String dn);

    void setFileRepo(long id, String repoId);

    void setPixelsNamePathRepo(long pixId, String name, String path,
            String repoId);

    // TODO this should probably return an iterator.
    List<Long> getDeletedIds(String entityType);

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

    //
    // End PgArrayHelper
    //

    public class PostgresSqlAction implements SqlAction {

        private final SimpleJdbcOperations jdbc;

        public PostgresSqlAction(SimpleJdbcOperations jdbc) {
            this.jdbc = jdbc;
        }

        public boolean activeSession(String sessionUUID) {
            int count = jdbc
                    .queryForInt(Messages.getString("pg.active_session"), //$NON-NLS-1$
                            sessionUUID);
            return count > 0;
        }

        public String fileRepo(long fileId) {
            return jdbc.queryForObject(
                    Messages.getString("pg.file_repo"), String.class, //$NON-NLS-1$
                    fileId);
        }

        private final static String synchronizeJobsSql = Messages.getString("pg.sync_jobs"); //$NON-NLS-1$

        public int synchronizeJobs(List<Long> ids) {
            int count = 0;
            if (ids.size() > 0) {
                Map<String, Object> m = new HashMap<String, Object>();
                m.put("ids", ids); //$NON-NLS-1$
                count += jdbc.update(synchronizeJobsSql
                        + Messages.getString("pg.id_not_in"), m); //$NON-NLS-1$
            } else {
                count += jdbc.update(synchronizeJobsSql);
            }
            return count;
        }

        private final static String findRepoFileSql = Messages.getString("pg.find_repo_file"); //$NON-NLS-1$

        public Long findRepoFile(String uuid, String dirname, String basename,
                String mimetype) {

            if (mimetype != null) {
                return jdbc.queryForLong(findRepoFileSql + Messages.getString("pg.and_mimetype"), //$NON-NLS-1$
                        uuid, dirname, basename, mimetype);
            } else {
                return jdbc.queryForLong(findRepoFileSql, uuid, dirname,
                        basename);

            }
        }

        public int repoScriptCount(String uuid) {
            return jdbc.queryForInt(Messages.getString("pg.repo_script_count"), uuid); //$NON-NLS-1$

        }

        public Long nextSessionId() {
            return jdbc.queryForLong(Messages.getString("pg.next_session")); //$NON-NLS-1$
        }

        public List<Long> fileIdsInDb(String uuid) {
            return jdbc.query(Messages.getString("pg.file_id_in_db"), //$NON-NLS-1$
                    new RowMapper<Long>() {
                        public Long mapRow(ResultSet arg0, int arg1)
                                throws SQLException {
                            return arg0.getLong(1);
                        }
                    }, uuid);
        }

        public Map<String, Object> repoFile(long value) {
            return jdbc.queryForMap(Messages.getString("pg.repo_file"), value); //$NON-NLS-1$

        }

        public long countFormat(String name) {
            long count = jdbc.queryForLong(
                    Messages.getString("pg.count_format"), name); //$NON-NLS-1$
            return count;
        }

        // Copied from data.vm
        public final static String insertFormatSql = Messages.getString("pg.insert_format"); //$NON-NLS-1$

        public int insertFormat(String name) {
            int inserts = jdbc.update(insertFormatSql, name);
            return inserts;
        }

        public int closeSessions(String uuid) {
            int count = jdbc.update(Messages.getString("pg.update_format"), uuid); //$NON-NLS-1$
            return count;
        }

        public long nodeId(String internal_uuid) {
            return jdbc.queryForLong(Messages.getString("pg.internal_uuid"), //$NON-NLS-1$
                    internal_uuid);
        }

        public int insertSession(Map<String, Object> params) {
            int count = jdbc.update(Messages.getString("pg.insert_session"), params); //$NON-NLS-1$
            return count;
        }

        public Long sessionId(String uuid) {
            Long id = jdbc.queryForLong(
                    Messages.getString("pg.session_id"), uuid); //$NON-NLS-1$
            return id;
        }

        public int isFileInRepo(String uuid, long id) {
            int count = jdbc
                    .queryForInt(
                            Messages.getString("pg.is_file_in_repo"), //$NON-NLS-1$
                            uuid, id);
            return count;
        }

        public int removePassword(Long id) {
            return jdbc.update(
                    Messages.getString("pg.remove_pass"), id); //$NON-NLS-1$
        }

        public Date now() {
            return jdbc.queryForObject(Messages.getString("pg.now"), Date.class); //$NON-NLS-1$
        }

        public int updateConfiguration(String key, String value) {
            return jdbc.update(
                    Messages.getString("pg.update_config"), value, //$NON-NLS-1$
                    key);
        }

        public String dbVersion() {
            return jdbc.query(
                    Messages.getString("pg.db_version"), //$NON-NLS-1$
                    new RowMapper<String>() {
                        public String mapRow(ResultSet arg0, int arg1)
                                throws SQLException {
                            String v = arg0.getString("currentversion"); //$NON-NLS-1$
                            int p = arg0.getInt("currentpatch"); //$NON-NLS-1$
                            return v + "__" + p; //$NON-NLS-1$
                        }

                    }).get(0);

        }

        public String configValue(String key) {
            return jdbc.queryForObject(
                    Messages.getString("pg.config_value"), //$NON-NLS-1$
                    String.class, key);
        }

        public String dbUuid() {
            return jdbc
                    .query(Messages.getString("pg.db_uuid"), //$NON-NLS-1$
                            new RowMapper<String>() {
                                public String mapRow(ResultSet arg0, int arg1)
                                        throws SQLException {
                                    String s = arg0.getString("value"); //$NON-NLS-1$
                                    return s;
                                }

                            }).get(0);

        }

        private final static String logLoaderQuerySql = Messages.getString("pg.log_loader_query"); //$NON-NLS-1$
        private final static String logLoaderInsertSql = Messages.getString("pg.log_loader_insert"); //$NON-NLS-1$
        private final static String logLoaderUpdateSql = Messages.getString("pg.log_loader_update"); //$NON-NLS-1$
        private final static String logLoaderDeleteSql = Messages.getString("pg.log_loader_delete"); //$NON-NLS-1$

        public long selectCurrentEventLog(String key) {
            return jdbc.queryForLong(logLoaderQuerySql, key);
        }

        public void setCurrentEventLog(long id, String key) {
            int count = jdbc.update(logLoaderUpdateSql, id, key);
            if (count == 0) {
                jdbc.update(logLoaderInsertSql, key, id);
            }
        }

        public void delCurrentEventLog(String key) {
            jdbc.update(logLoaderDeleteSql, key);

        }

        public long nextValue(String segmentValue, int incrementSize) {
            // FIXME take the datasource or similar???
            return jdbc.queryForLong(Messages.getString("pg.next_val"), segmentValue, //$NON-NLS-1$
                    incrementSize);

        }

        public void insertLogs(List<Object[]> batchData) {
            jdbc.batchUpdate(Messages.getString("pg.insert_logs"), batchData); //$NON-NLS-1$

        }

        public List<Map<String, Object>> roiByImageAndNs(final long imageId,
                final String ns) {
            String queryString;
            queryString = Messages.getString("pg.roi_by_image_and_ns"); //$NON-NLS-1$
            List<Map<String, Object>> mapList = jdbc.queryForList(queryString, imageId, ns);
            return mapList;
        }

        public List<Long> getShapeIds(long roiId) {

            return jdbc.query(Messages.getString("pg.shape_ids"), //$NON-NLS-1$
                    new IdRowMapper(), roiId);
        }

        public String dnForUser(Long id) {
            return jdbc.queryForObject(Messages.getString("pg.dn_for_user"), String.class, id); //$NON-NLS-1$
        }

        public List<Map<String, Object>> dnExperimenterMaps() {
            return jdbc
            .queryForList(
                    Messages.getString("pg.dn_exp_maps")); //$NON-NLS-1$

        }

        public void setUserDn(Long experimenterID, String dn) {
            int results = jdbc.update(
                    Messages.getString("pg.set_user_dn"), //$NON-NLS-1$
                    dn, experimenterID);
            if (results < 1) {
                results = jdbc.update(Messages.getString("pg.insert_password"), //$NON-NLS-1$
                        experimenterID, null, dn);
            }

        }

        public void setFileRepo(long id, String repoId) {
            jdbc.update(Messages.getString("pg.set_file_repo"), //$NON-NLS-1$
                    repoId, id);
        }

        public void setPixelsNamePathRepo(long pixId, String name, String path,
                String repoId) {
            jdbc.update(Messages.getString("pg.update_pixels_name"), name, pixId); //$NON-NLS-1$
            jdbc.update(Messages.getString("pg.update_pixels_path"), path, pixId); //$NON-NLS-1$
            jdbc.update(Messages.getString("pg.update_pixels_repo"), repoId, //$NON-NLS-1$
                    pixId);
        }

        public List<Long> getDeletedIds(String entityType) {
            List<Long> list;

            String sql = Messages.getString("pg.get_delete_ids"); //$NON-NLS-1$

            RowMapper<Long> mapper = new RowMapper<Long>() {
                public Long mapRow(ResultSet resultSet, int rowNum)
                        throws SQLException {
                    Long id = new Long(resultSet.getString(1));
                    return id;
                }
            };

            list = jdbc.query(sql, mapper, new Object[] { entityType });

            return list;

        }

        public Set<String> currentUserNames() {
            List<String> names = jdbc.query(
                    Messages.getString("pg.current_user_names"), new RowMapper<String>() { //$NON-NLS-1$
                        public String mapRow(ResultSet arg0, int arg1)
                                throws SQLException {
                            return arg0.getString(1); // Bleck
                        }
                    });
            return new HashSet<String>(names);
        }

        //
        // Formerly PgArrayHelper
        //

        public int setFileParam(final long id, final String key,
                final String value) {
            Map<String, String> params = getFileParams(id);
            if (params == null) {
                params = new HashMap<String, String>();
            }
            params.put(key, value);
            // Alternative would be to do an either-or with a concat
            // "set params = params || array[array[?,?]] where id = ?"
            return setFileParams(id, params);
        }

        public int setFileParams(final long id, Map<String, String> params) {
            if (params == null || params.size() == 0) {
                return jdbc.update(
                        Messages.getString("pg.set_file_params_null"), //$NON-NLS-1$
                        id);
            } else {
                boolean first = true;
                StringBuilder sb = new StringBuilder();
                List<Object> list = new ArrayList<Object>();
                sb.append(Messages.getString("pg.set_file_params_1")); //$NON-NLS-1$
                for (String key : params.keySet()) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append(Messages.getString("pg.set_file_params_2")); //$NON-NLS-1$
                    }
                    sb.append(Messages.getString("pg.set_file_params_3")); //$NON-NLS-1$
                    list.add(key);
                    list.add(params.get(key));
                }
                sb.append(Messages.getString("pg.set_file_params4")); //$NON-NLS-1$
                list.add(id);
                return jdbc.update(sb.toString(),
                        (Object[]) list.toArray(new Object[list.size()]));
            }
        }

        @SuppressWarnings("unchecked")
        public Map<String, String> getFileParams(final long id)
                throws InternalException {
            try {
                return jdbc.queryForObject(
                        Messages.getString("pg.get_file_params"), //$NON-NLS-1$
                        new RowMapper<Map<String, String>>() {
                            public Map<String, String> mapRow(ResultSet arg0,
                                    int arg1) throws SQLException {
                                Map<String, String> params = new HashMap<String, String>();
                                Array arr1 = (Array) arg0.getArray(1);
                                if (arr1 == null) {
                                    return params;
                                }
                                String[][] arr2 = (String[][]) arr1.getArray();
                                for (int i = 0; i < arr2.length; i++) {
                                    params.put(arr2[i][0], arr2[i][1]);
                                }
                                return params;
                            }
                        }, id);
            } catch (EmptyResultDataAccessException e) {
                return null;
            } catch (UncategorizedSQLException e) {
                throw new InternalException("Potential jdbc jar error."); //$NON-NLS-1$
            }
        }

        public List<String> getFileParamKeys(long id) throws InternalException {
            try {
                return jdbc.queryForObject(
                        Messages.getString("pg.get_file_param_keys"), //$NON-NLS-1$
                        new RowMapper<List<String>>() {
                            public List<String> mapRow(ResultSet arg0, int arg1)
                                    throws SQLException {
                                final List<String> keys = new ArrayList<String>();
                                Array arr1 = (Array) arg0.getArray(1);
                                if (arr1 == null) {
                                    return keys;
                                }
                                String[][] arr2 = (String[][]) arr1.getArray();
                                for (int i = 0; i < arr2.length; i++) {
                                    keys.add(arr2[i][0]);
                                }
                                return keys;
                            }
                        }, id);
            } catch (EmptyResultDataAccessException e) {
                return null;
            } catch (UncategorizedSQLException e) {
                throw new InternalException("Potential jdbc jar error."); //$NON-NLS-1$
            }
        }

        public int setPixelsParam(final long id, final String key,
                final String value) {
            Map<String, String> params = getPixelsParams(id);
            if (params == null) {
                params = new HashMap<String, String>();
            }
            params.put(key, value);
            // Alternative would be to do an either-or with a concat
            // "set params = params || array[array[?,?]] where id = ?"
            return setPixelsParams(id, params);
        }

        public int setPixelsParams(final long id, Map<String, String> params) {
            if (params == null || params.size() == 0) {
                return jdbc.update(
                        Messages.getString("pg.set_pixel_params_null"), id); //$NON-NLS-1$
            } else {
                boolean first = true;
                StringBuilder sb = new StringBuilder();
                List<Object> list = new ArrayList<Object>();
                sb.append(Messages.getString("pg.set_pixels_params_1")); //$NON-NLS-1$
                for (String key : params.keySet()) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append(Messages.getString("pg.set_pixels_params_2")); //$NON-NLS-1$
                    }
                    sb.append(Messages.getString("pg.set_pixels_params_3")); //$NON-NLS-1$
                    list.add(key);
                    list.add(params.get(key));
                }
                sb.append(Messages.getString("pg.set_pixels_params_4")); //$NON-NLS-1$
                list.add(id);
                return jdbc.update(sb.toString(),
                        (Object[]) list.toArray(new Object[list.size()]));
            }
        }

        @SuppressWarnings("unchecked")
        public Map<String, String> getPixelsParams(final long id)
                throws InternalException {
            try {
                return jdbc.queryForObject(
                        Messages.getString("pg.get_pixels_params"), //$NON-NLS-1$
                        new RowMapper<Map<String, String>>() {
                            public Map<String, String> mapRow(ResultSet arg0,
                                    int arg1) throws SQLException {
                                Map<String, String> params = new HashMap<String, String>();
                                Array arr1 = (Array) arg0.getArray(1);
                                if (arr1 == null) {
                                    return params;
                                }
                                String[][] arr2 = (String[][]) arr1.getArray();
                                for (int i = 0; i < arr2.length; i++) {
                                    params.put(arr2[i][0], arr2[i][1]);
                                }
                                return params;
                            }
                        }, id);
            } catch (EmptyResultDataAccessException e) {
                return null;
            } catch (UncategorizedSQLException e) {
                throw new InternalException("Potential jdbc jar error."); //$NON-NLS-1$
            }
        }

        public List<String> getPixelsParamKeys(long id)
                throws InternalException {
            try {
                return jdbc.queryForObject(
                        Messages.getString("pg.get_pixels_params_keys"), //$NON-NLS-1$
                        new RowMapper<List<String>>() {
                            public List<String> mapRow(ResultSet arg0, int arg1)
                                    throws SQLException {
                                final List<String> keys = new ArrayList<String>();
                                Array arr1 = (Array) arg0.getArray(1);
                                if (arr1 == null) {
                                    return keys;
                                }
                                String[][] arr2 = (String[][]) arr1.getArray();
                                for (int i = 0; i < arr2.length; i++) {
                                    keys.add(arr2[i][0]);
                                }
                                return keys;
                            }
                        }, id);
            } catch (EmptyResultDataAccessException e) {
                return null;
            } catch (UncategorizedSQLException e) {
                throw new InternalException("Potential jdbc jar error."); //$NON-NLS-1$
            }
        }

        //
        // End PgArrayHelper
        //
    }


}
