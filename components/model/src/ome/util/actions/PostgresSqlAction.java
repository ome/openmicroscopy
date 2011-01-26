/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util.actions;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ome.conditions.InternalException;
import ome.util.SqlAction;
import ome.util.SqlAction.IdRowMapper;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

public class PostgresSqlAction extends SqlAction.Impl {

    private final SimpleJdbcOperations jdbc;

    public PostgresSqlAction(SimpleJdbcOperations jdbc) {
        this.jdbc = jdbc;
    }

    //
    // Impl methods
    //

    @Override
    protected SimpleJdbcOperations _jdbc() {
        return this.jdbc;
    }

    @Override
    protected String _lookup(String key) {
        return PsqlStrings.getString("sql_action." + key);
    }

    //
    // Interface methods
    //

    public boolean activeSession(String sessionUUID) {
        int count = jdbc.queryForInt(
                PsqlStrings.getString("sql_action.active_session"), //$NON-NLS-1$
                sessionUUID);
        return count > 0;
    }

    private final static String synchronizeJobsSql = PsqlStrings
            .getString("sql_action.sync_jobs"); //$NON-NLS-1$

    public int synchronizeJobs(List<Long> ids) {
        int count = 0;
        if (ids.size() > 0) {
            Map<String, Object> m = new HashMap<String, Object>();
            m.put("ids", ids); //$NON-NLS-1$
            count += jdbc
                    .update(synchronizeJobsSql
                            + PsqlStrings.getString("sql_action.id_not_in"), m); //$NON-NLS-1$
        } else {
            count += jdbc.update(synchronizeJobsSql);
        }
        return count;
    }

    private final static String findRepoFileSql = PsqlStrings
            .getString("sql_action.find_repo_file"); //$NON-NLS-1$

    public Long findRepoFile(String uuid, String dirname, String basename,
            String mimetype) {

        if (mimetype != null) {
            return jdbc.queryForLong(
                    findRepoFileSql + PsqlStrings.getString("sql_action.and_mimetype"), //$NON-NLS-1$
                    uuid, dirname, basename, mimetype);
        } else {
            return jdbc.queryForLong(findRepoFileSql, uuid, dirname, basename);

        }
    }

    public String findRepoFilePath(String uuid, long id) {
        return (String) jdbc.queryForObject(PsqlStrings.getString("sql_action.find_repo_file_path"), //$NON-NLS-1$
                String.class, id, uuid);
    }

    public List<Long> findRepoPixels(String uuid, String dirname, String basename){
        return jdbc.query(PsqlStrings.getString("sql_action.find_repo_pixels"), //$NON-NLS-1$
                new RowMapper<Long>() {
                    public Long mapRow(ResultSet arg0, int arg1)
                            throws SQLException {
                        return arg0.getLong(1);
                    }
                }, uuid, dirname, basename);
    }

    public Long findRepoImageFromPixels(long id) {
        return jdbc.queryForLong(PsqlStrings.getString("sql_action.find_repo_file_path"), id); //$NON-NLS-1$
    }

    public int repoScriptCount(String uuid) {
        return jdbc.queryForInt(
                PsqlStrings.getString("sql_action.repo_script_count"), uuid); //$NON-NLS-1$

    }

    public Long nextSessionId() {
        return jdbc.queryForLong(PsqlStrings.getString("sql_action.next_session")); //$NON-NLS-1$
    }

    public List<Long> fileIdsInDb(String uuid) {
        return jdbc.query(PsqlStrings.getString("sql_action.file_id_in_db"), //$NON-NLS-1$
                new RowMapper<Long>() {
                    public Long mapRow(ResultSet arg0, int arg1)
                            throws SQLException {
                        return arg0.getLong(1);
                    }
                }, uuid);
    }

    public Map<String, Object> repoFile(long value) {
        return jdbc.queryForMap(PsqlStrings.getString("sql_action.repo_file"), value); //$NON-NLS-1$

    }

    public long countFormat(String name) {
        long count = jdbc.queryForLong(
                PsqlStrings.getString("sql_action.count_format"), name); //$NON-NLS-1$
        return count;
    }

    // Copied from data.vm
    public final static String insertFormatSql = PsqlStrings
            .getString("sql_action.insert_format"); //$NON-NLS-1$

    public int insertFormat(String name) {
        int inserts = jdbc.update(insertFormatSql, name);
        return inserts;
    }

    public int closeSessions(String uuid) {
        int count = jdbc
                .update(PsqlStrings.getString("sql_action.update_session"), uuid); //$NON-NLS-1$
        return count;
    }

    public long nodeId(String internal_uuid) {
        return jdbc.queryForLong(PsqlStrings.getString("sql_action.internal_uuid"), //$NON-NLS-1$
                internal_uuid);
    }

    public int insertSession(Map<String, Object> params) {
        int count = jdbc.update(
                PsqlStrings.getString("sql_action.insert_session"), params); //$NON-NLS-1$
        return count;
    }

    public Long sessionId(String uuid) {
        Long id = jdbc.queryForLong(
                PsqlStrings.getString("sql_action.session_id"), uuid); //$NON-NLS-1$
        return id;
    }

    public int isFileInRepo(String uuid, long id) {
        int count = jdbc.queryForInt(
                PsqlStrings.getString("sql_action.is_file_in_repo"), //$NON-NLS-1$
                uuid, id);
        return count;
    }

    public int removePassword(Long id) {
        return jdbc.update(PsqlStrings.getString("sql_action.remove_pass"), id); //$NON-NLS-1$
    }

    public Date now() {
        return jdbc.queryForObject(PsqlStrings.getString("sql_action.now"), Date.class); //$NON-NLS-1$
    }

    public int updateConfiguration(String key, String value) {
        return jdbc.update(PsqlStrings.getString("sql_action.update_config"), value, //$NON-NLS-1$
                key);
    }

    public String dbVersion() {
        return jdbc.query(PsqlStrings.getString("sql_action.db_version"), //$NON-NLS-1$
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
        return jdbc.queryForObject(PsqlStrings.getString("sql_action.config_value"), //$NON-NLS-1$
                String.class, key);
    }

    public String dbUuid() {
        return jdbc.query(PsqlStrings.getString("sql_action.db_uuid"), //$NON-NLS-1$
                new RowMapper<String>() {
                    public String mapRow(ResultSet arg0, int arg1)
                            throws SQLException {
                        String s = arg0.getString("value"); //$NON-NLS-1$
                        return s;
                    }

                }).get(0);

    }

    private final static String logLoaderQuerySql = PsqlStrings
            .getString("sql_action.log_loader_query"); //$NON-NLS-1$
    private final static String logLoaderInsertSql = PsqlStrings
            .getString("sql_action.log_loader_insert"); //$NON-NLS-1$
    private final static String logLoaderUpdateSql = PsqlStrings
            .getString("sql_action.log_loader_update"); //$NON-NLS-1$
    private final static String logLoaderDeleteSql = PsqlStrings
            .getString("sql_action.log_loader_delete"); //$NON-NLS-1$

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
        return jdbc.queryForLong(
                PsqlStrings.getString("sql_action.next_val"), segmentValue, //$NON-NLS-1$
                incrementSize);

    }

    public long currValue(String segmentName) {
        try {
            long next_value = jdbc.queryForLong(
                    PsqlStrings.getString("sql_action.curr_val"), //$NON-NLS-1$
                    segmentName);
            return next_value;
        } catch (EmptyResultDataAccessException erdae) {
            return -1l;
        }
    }

    public void insertLogs(List<Object[]> batchData) {
        jdbc.batchUpdate(PsqlStrings.getString("sql_action.insert_logs"), batchData); //$NON-NLS-1$

    }

    public List<Map<String, Object>> roiByImageAndNs(final long imageId,
            final String ns) {
        String queryString;
        queryString = PsqlStrings.getString("sql_action.roi_by_image_and_ns"); //$NON-NLS-1$
        List<Map<String, Object>> mapList = jdbc.queryForList(queryString,
                imageId, ns);
        return mapList;
    }

    public List<Long> getShapeIds(long roiId) {

        return jdbc.query(PsqlStrings.getString("sql_action.shape_ids"), //$NON-NLS-1$
                new IdRowMapper(), roiId);
    }

    public String dnForUser(Long id) {
        String expire;
        try {
            expire = jdbc.queryForObject(
                    PsqlStrings.getString("sql_action.dn_for_user"), String.class, id); //$NON-NLS-1$
        } catch (EmptyResultDataAccessException e) {
            expire = null; // This means there's not one.
        }
        return expire;
    }

    public List<Map<String, Object>> dnExperimenterMaps() {
        return jdbc.queryForList(PsqlStrings.getString("sql_action.dn_exp_maps")); //$NON-NLS-1$

    }

    public void setUserDn(Long experimenterID, String dn) {
        int results = jdbc.update(PsqlStrings.getString("sql_action.set_user_dn"), //$NON-NLS-1$
                dn, experimenterID);
        if (results < 1) {
            results = jdbc.update(PsqlStrings.getString("sql_action.insert_password"), //$NON-NLS-1$
                    experimenterID, null, dn);
        }

    }

    public boolean setUserPassword(Long experimenterID, String password) {
        int results = jdbc.update(_lookup("update_password"), //$NON-NLS-1$
                password, experimenterID);
        if (results < 1) {
            results = jdbc.update(PsqlStrings.getString("sql_action.insert_password"), //$NON-NLS-1$
                    experimenterID, password, null);
        }
        return results >= 1;
    }

    public String getPasswordHash(Long experimenterID) {
        String stored;
        try {
            stored = jdbc.queryForObject(
                    PsqlStrings.getString("sql_action.password_hash"), //$NON-NLS-1$
                    String.class, experimenterID);
        } catch (EmptyResultDataAccessException e) {
            stored = null; // This means there's not one.
        }
        return stored;
    }

    public Long getUserId(String userName) {
        Long id;
        try {
            id = jdbc.queryForObject(PsqlStrings.getString("sql_action.user_id"), //$NON-NLS-1$
                    Long.class, userName);
        } catch (EmptyResultDataAccessException e) {
            id = null; // This means there's not one.
        }
        return id;
    }

    public List<String> getUserGroups(String userName) {
        List<String> roles;
        try {
            roles = jdbc.query(PsqlStrings.getString("sql_action.user_groups"), //$NON-NLS-1$
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

    public void setFileRepo(long id, String repoId) {
        jdbc.update(PsqlStrings.getString("sql_action.set_file_repo"), //$NON-NLS-1$
                repoId, id);
    }

    public void setPixelsNamePathRepo(long pixId, String name, String path,
            String repoId) {
        jdbc.update(PsqlStrings.getString("sql_action.update_pixels_name"), name, pixId); //$NON-NLS-1$
        jdbc.update(PsqlStrings.getString("sql_action.update_pixels_path"), path, pixId); //$NON-NLS-1$
        jdbc.update(PsqlStrings.getString("sql_action.update_pixels_repo"), repoId, //$NON-NLS-1$
                pixId);
    }

    public List<Long> getDeletedIds(String entityType) {
        List<Long> list;

        String sql = PsqlStrings.getString("sql_action.get_delete_ids"); //$NON-NLS-1$

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

    public void createSavepoint(String savepoint) {
        call("SAVEPOINT DEL", savepoint);
    }

    public void releaseSavepoint(String savepoint) {
        call("RELEASE SAVEPOINT DEL", savepoint);
    }

    public void rollbackSavepoint(String savepoint) {
        call("ROLLBACK TO SAVEPOINT DEL", savepoint);
    }

    private void call(final String call, final String savepoint) {
        jdbc.getJdbcOperations().execute(new ConnectionCallback() {
            public Object doInConnection(java.sql.Connection connection)
                    throws SQLException {
                connection.prepareCall(call + savepoint).execute(); // TODO Use
                                                                    // a
                                                                    // different
                                                                    // callback
                return null;
            }
        });
    }

    public void deferConstraints() {
        jdbc.getJdbcOperations().execute(new ConnectionCallback() {
            public Object doInConnection(java.sql.Connection connection)
                    throws SQLException {
                Statement statement = connection.createStatement();
                statement.execute("set constraints all deferred;");
                return null;
            }
        });
    }

    public Set<String> currentUserNames() {
        List<String> names = jdbc
                .query(PsqlStrings.getString("sql_action.current_user_names"), new RowMapper<String>() { //$NON-NLS-1$
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

    public int setFileParam(final long id, final String key, final String value) {
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
                    PsqlStrings.getString("sql_action.set_file_params_null"), //$NON-NLS-1$
                    id);
        } else {
            boolean first = true;
            StringBuilder sb = new StringBuilder();
            List<Object> list = new ArrayList<Object>();
            sb.append(PsqlStrings.getString("sql_action.set_file_params_1")); //$NON-NLS-1$
            for (String key : params.keySet()) {
                if (first) {
                    first = false;
                } else {
                    sb.append(PsqlStrings.getString("sql_action.set_file_params_2")); //$NON-NLS-1$
                }
                sb.append(PsqlStrings.getString("sql_action.set_file_params_3")); //$NON-NLS-1$
                list.add(key);
                list.add(params.get(key));
            }
            sb.append(PsqlStrings.getString("sql_action.set_file_params4")); //$NON-NLS-1$
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
                    PsqlStrings.getString("sql_action.get_file_params"), //$NON-NLS-1$
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
                    PsqlStrings.getString("sql_action.get_file_param_keys"), //$NON-NLS-1$
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
                    PsqlStrings.getString("sql_action.set_pixel_params_null"), id); //$NON-NLS-1$
        } else {
            boolean first = true;
            StringBuilder sb = new StringBuilder();
            List<Object> list = new ArrayList<Object>();
            sb.append(PsqlStrings.getString("sql_action.set_pixels_params_1")); //$NON-NLS-1$
            for (String key : params.keySet()) {
                if (first) {
                    first = false;
                } else {
                    sb.append(PsqlStrings.getString("sql_action.set_pixels_params_2")); //$NON-NLS-1$
                }
                sb.append(PsqlStrings.getString("sql_action.set_pixels_params_3")); //$NON-NLS-1$
                list.add(key);
                list.add(params.get(key));
            }
            sb.append(PsqlStrings.getString("sql_action.set_pixels_params_4")); //$NON-NLS-1$
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
                    PsqlStrings.getString("sql_action.get_pixels_params"), //$NON-NLS-1$
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

    public List<String> getPixelsParamKeys(long id) throws InternalException {
        try {
            return jdbc.queryForObject(
                    PsqlStrings.getString("sql_action.get_pixels_params_keys"), //$NON-NLS-1$
                    new RowMapper<List<String>>() {
                        public List<String> mapRow(ResultSet arg0, int arg1)
                                throws SQLException {
                            final List<String> keys = new ArrayList<String>();
                            Array arr1 = arg0.getArray(1);
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
