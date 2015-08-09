/*
 *   Copyright 2010-2014 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util.actions;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ome.conditions.InternalException;
import ome.util.SqlAction;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;

import com.google.common.collect.Iterables;

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

    /**
     * The temp_ids infrastructure was never properly put
     * in place for the "psql" profile. This method simply
     * bypasses all query rewriting until that's functional.
     *
     * @see <a href="https://trac.openmicroscopy.org/ome/ticket/3961">ticket 3961</a>
     * @see <a href="https://trac.openmicroscopy.org/ome/ticket/9077">ticket 9077</a>
     */
    public String rewriteHql(String query, String key, Object value) {
        return query;
    }

    public void prepareSession(final long eventId, final long userId, final long groupId) {
        JdbcTemplate jt = (JdbcTemplate) _jdbc().getJdbcOperations(); // FIXME
        SimpleJdbcCall call = new SimpleJdbcCall(jt).withFunctionName("_prepare_session");
        MapSqlParameterSource in = new MapSqlParameterSource();
        in.addValue("_event_id", eventId);
        in.addValue("_user_id", userId);
        in.addValue("_group_id", groupId);
        call.executeFunction(void.class, in);
    }

    public boolean activeSession(String sessionUUID) {
        int count = _jdbc().queryForInt(_lookup("active_session"), //$NON-NLS-1$
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
            count += _jdbc().update(
                            synchronizeJobsSql + _lookup("id_not_in"), m); //$NON-NLS-1$
        } else {
            count += _jdbc().update(synchronizeJobsSql);
        }
        return count;
    }

    public List<Long> findRepoPixels(String uuid, String dirname, String basename){
        return _jdbc().query(_lookup("find_repo_pixels"), //$NON-NLS-1$
                new RowMapper<Long>() {
                    public Long mapRow(ResultSet arg0, int arg1)
                            throws SQLException {
                        return arg0.getLong(1);
                    }
                }, uuid, dirname, basename);
    }

    public Long findRepoImageFromPixels(long id) {
        return _jdbc().queryForLong(_lookup("find_repo_image_from_pixels"), id); //$NON-NLS-1$
    }

    public Long nextSessionId() {
        return _jdbc().queryForLong(_lookup("next_session")); //$NON-NLS-1$
    }

    public Map<String, Object> repoFile(long value) {
        return _jdbc().queryForMap(_lookup("repo_file"), value); //$NON-NLS-1$
    }

    public long countFormat(String name) {
        return _jdbc().queryForLong(_lookup("count_format"), name); //$NON-NLS-1$
    }

    // Copied from data.vm
    public final static String insertFormatSql = PsqlStrings
            .getString("sql_action.insert_format"); //$NON-NLS-1$

    public int insertFormat(String name) {
        return _jdbc().update(insertFormatSql, name);
    }

    public int closeSessions(String uuid) {
        return _jdbc().update(_lookup("update_session"), uuid); //$NON-NLS-1$
    }

    public long nodeId(String internal_uuid) {
        return _jdbc().queryForLong(_lookup("internal_uuid"), //$NON-NLS-1$
                internal_uuid);
    }

    public int insertSession(Map<String, Object> params) {
        return _jdbc().update(_lookup("insert_session"), params); //$NON-NLS-1$
    }

    public Long sessionId(String uuid) {
        return _jdbc().queryForLong(_lookup("session_id"), uuid); //$NON-NLS-1$
    }

    public int removePassword(Long id) {
        return _jdbc().update(_lookup("remove_pass"), id); //$NON-NLS-1$
    }

    public Date now() {
        return _jdbc().queryForObject(_lookup("now"), Date.class); //$NON-NLS-1$
    }

    public int updateConfiguration(String key, String value) {
        return _jdbc().update(_lookup("update_config"), value, //$NON-NLS-1$
                key);
    }

    public String dbVersion() {
        return _jdbc().query(_lookup("db_version"), //$NON-NLS-1$
                new RowMapper<String>() {
                    public String mapRow(ResultSet arg0, int arg1)
                            throws SQLException {
                        String v = arg0.getString("currentversion"); //$NON-NLS-1$
                        int p = arg0.getInt("currentpatch"); //$NON-NLS-1$
                        return v + "__" + p; //$NON-NLS-1$
                    }
                }).get(0);
    }

    public String dbUuid() {
        return _jdbc().query(_lookup("db_uuid"), //$NON-NLS-1$
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
        return _jdbc().queryForLong(logLoaderQuerySql, key);
    }

    public void setCurrentEventLog(long id, String key) {
        int count = _jdbc().update(logLoaderUpdateSql, id, key);
        if (count == 0) {
            _jdbc().update(logLoaderInsertSql, key, id);
        }
    }

    public void delCurrentEventLog(String key) {
        _jdbc().update(logLoaderDeleteSql, key);
    }

    public long nextValue(String segmentValue, int incrementSize) {
        return _jdbc().queryForLong(_lookup("next_val"), //$NON-NLS-1$
                segmentValue, incrementSize);
    }

    public long currValue(String segmentName) {
        try {
            long next_value = _jdbc().queryForLong(_lookup("curr_val"), //$NON-NLS-1$
                    segmentName);
            return next_value;
        } catch (EmptyResultDataAccessException erdae) {
            return -1l;
        }
    }

    public void insertLogs(List<Object[]> batchData) {
        _jdbc().batchUpdate(_lookup("insert_logs"), batchData); //$NON-NLS-1$
    }

    public List<Map<String, Object>> roiByImageAndNs(final long imageId,
            final String ns) {
        String queryString;
        queryString = _lookup("roi_by_image_and_ns"); //$NON-NLS-1$
        List<Map<String, Object>> mapList = _jdbc().queryForList(queryString,
                imageId, ns);
        return mapList;
    }

    public List<Long> getShapeIds(long roiId) {
        return _jdbc().query(_lookup("shape_ids"), //$NON-NLS-1$
                new IdRowMapper(), roiId);
    }

    @Override
    public void setFileRepo(Collection<Long> ids, String repoId) {
       for (final List<Long> idsBatch : Iterables.partition(ids, 256)) {
           final Map<String, Object> parameters = new HashMap<String, Object>();
           parameters.put("ids", idsBatch);
           parameters.put("repo", repoId);
           _jdbc().update(_lookup("set_file_repo"), //$NON-NLS-1$
                   parameters);
       }
    }

    public void setPixelsNamePathRepo(long pixId, String name, String path,
            String repoId) {
        _jdbc().update(_lookup("update_pixels_name"), name, pixId); //$NON-NLS-1$
        _jdbc().update(_lookup("update_pixels_path"), path, pixId); //$NON-NLS-1$
        _jdbc().update(_lookup("update_pixels_repo"), repoId, //$NON-NLS-1$
                pixId);
    }

    public List<Long> getDeletedIds(String entityType) {
        List<Long> list;

        String sql = _lookup("get_delete_ids"); //$NON-NLS-1$

        RowMapper<Long> mapper = new RowMapper<Long>() {
            public Long mapRow(ResultSet resultSet, int rowNum)
                    throws SQLException {
                Long id = new Long(resultSet.getString(1));
                return id;
            }
        };

        list = _jdbc().query(sql, mapper, new Object[] { entityType });

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
        _jdbc().getJdbcOperations().execute(new ConnectionCallback() {
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
        _jdbc().getJdbcOperations().execute(new ConnectionCallback() {
            public Object doInConnection(java.sql.Connection connection)
                    throws SQLException {
                Statement statement = connection.createStatement();
                statement.execute("set constraints all deferred;");
                return null;
            }
        });
    }

    public Set<String> currentUserNames() {
        List<String> names = _jdbc().query(_lookup("current_user_names"),  //$NON-NLS-1$
                        new RowMapper<String>() {
                            public String mapRow(ResultSet arg0, int arg1)
                                    throws SQLException {
                                return arg0.getString(1); // Bleck
                            }
                        });
        return new HashSet<String>(names);
    }

    /* (non-Javadoc)
     * @see ome.util.SqlAction#getPixelsNamePathRepo(long)
     */
    public List<String> getPixelsNamePathRepo(long id)
            throws InternalException
    {
        try {
            return _jdbc().queryForObject(
                    _lookup("get_pixels_name_path_repo"), //$NON-NLS-1$
                    new RowMapper<List<String>>() {
                        public List<String> mapRow(ResultSet arg0, int arg1)
                                throws SQLException {
                            final List<String> values = new ArrayList<String>();
                            values.add(arg0.getString(1));
                            values.add(arg0.getString(2));
                            values.add(arg0.getString(3));
                            return values;
                        }
                    }, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (UncategorizedSQLException e) {
            handlePotentialPgArrayJarError(e);
            return null;
        }
    }

    //
    // End PgArrayHelper
    //

    //
    // Helpers
    //

    /**
     * If postgresql is installed with an older jdbc jar that is on the
     * bootstrap classpath, then it's possible that the use of pgarrays will
     * fail (I think). See #7432
     */
    protected void handlePotentialPgArrayJarError(UncategorizedSQLException e) {
        log.error(e.toString()); // slf4j migration: toString()
        throw new InternalException(
                "Potential jdbc jar error during pgarray access (See #7432)\n"
                + printThrowable(e));
    }
}
