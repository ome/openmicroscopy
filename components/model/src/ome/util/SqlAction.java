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
                    .queryForInt("select count(id) from session s "
                            + "where s.closed is null " + "and s.uuid = ?",
                            sessionUUID);
            return count > 0;
        }

        public String fileRepo(long fileId) {
            return jdbc.queryForObject(
                    "select repo from OriginalFile where id = ?", String.class,
                    fileId);
        }

        private final static String synchronizeJobsSql = "update job set finished = now(), message = 'Forcibly closed', "
                + "status = (select id from jobstatus where value = 'Error') "
                + "where finished is null and "
                + "("
                + "(started < ( now() - interval '1 hour' )) "
                + "OR "
                + "(started is null and scheduledFor < ( now() - interval '1 day' ))"
                + ")";

        public int synchronizeJobs(List<Long> ids) {
            int count = 0;
            if (ids.size() > 0) {
                Map<String, Object> m = new HashMap<String, Object>();
                m.put("ids", ids);
                count += jdbc.update(synchronizeJobsSql
                        + "and id not in (:ids)", m);
            } else {
                count += jdbc.update(synchronizeJobsSql);
            }
            return count;
        }

        private final static String findRepoFileSql = "select id from originalfile "
                + "where repo = ? and path = ? and name = ? ";

        public Long findRepoFile(String uuid, String dirname, String basename,
                String mimetype) {

            if (mimetype != null) {
                return jdbc.queryForLong(findRepoFileSql + " and mimetype = ?",
                        uuid, dirname, basename, mimetype);
            } else {
                return jdbc.queryForLong(findRepoFileSql, uuid, dirname,
                        basename);

            }
        }

        public int repoScriptCount(String uuid) {
            return jdbc.queryForInt("select count(id) from originalfile "
                    + "where repo = ? and mimetype = 'text/x-python'", uuid);

        }

        public Long nextSessionId() {
            return jdbc.queryForLong("select ome_nextval('seq_session'::text)");
        }

        public List<Long> fileIdsInDb(String uuid) {
            return jdbc.query("select id from originalfile "
                    + "where repo = ? and mimetype = 'text/x-python'",
                    new RowMapper<Long>() {
                        public Long mapRow(ResultSet arg0, int arg1)
                                throws SQLException {
                            return arg0.getLong(1);
                        }
                    }, uuid);
        }

        public Map<String, Object> repoFile(long value) {
            return jdbc.queryForMap("select path, repo from originalfile "
                    + "where id = ?", value);

        }

        public long countFormat(String name) {
            long count = jdbc.queryForLong(
                    "select count(*) from format where value = ?", name);
            return count;
        }

        // Copied from data.vm
        public final static String insertFormatSql = "insert into format "
                + "(id,permissions,value)"
                + " select ome_nextval('seq_format'),-35,?";

        public int insertFormat(String name) {
            int inserts = jdbc.update(insertFormatSql, name);
            return inserts;
        }

        public int closeSessions(String uuid) {
            int count = jdbc.update("UPDATE session SET closed = now() "
                    + "WHERE uuid = ?", uuid);
            return count;
        }

        public long nodeId(String internal_uuid) {
            return jdbc.queryForLong("SELECT id FROM node where uuid = ?",
                    internal_uuid);
        }

        public int insertSession(Map<String, Object> params) {
            int count = jdbc.update("insert into session "
                    + "(id,permissions,timetoidle,timetolive,started,closed,"
                    + "defaulteventtype,uuid,owner,node)"
                    + "values (:sid,-35,:ttl,:tti,:start,null,"
                    + ":type,:uuid,:owner,:node)", params);
            return count;
        }

        public Long sessionId(String uuid) {
            Long id = jdbc.queryForLong(
                    "SELECT id FROM session WHERE uuid = ?", uuid);
            return id;
        }

        public int isFileInRepo(String uuid, long id) {
            int count = jdbc
                    .queryForInt(
                            "select count(id) from originalfile "
                                    + "where repo = ? and id = ? and mimetype = 'text/x-python'",
                            uuid, id);
            return count;
        }

        public int removePassword(Long id) {
            return jdbc.update(
                    "delete from password where experimenter_id = ?", id);
        }

        public Date now() {
            return jdbc.queryForObject("select now()", Date.class);
        }

        public int updateConfiguration(String key, String value) {
            return jdbc.update(
                    "update configuration set value = ? where name = ?", value,
                    key);
        }

        public String dbVersion() {
            return jdbc.query(
                    "select currentversion, currentpatch from dbpatch "
                            + "order by id desc limit 1",
                    new RowMapper<String>() {
                        public String mapRow(ResultSet arg0, int arg1)
                                throws SQLException {
                            String v = arg0.getString("currentversion");
                            int p = arg0.getInt("currentpatch");
                            return v + "__" + p;
                        }

                    }).get(0);

        }

        public String configValue(String key) {
            return jdbc.queryForObject(
                    "select value from configuration where name = ?",
                    String.class, key);
        }

        public String dbUuid() {
            return jdbc
                    .query("select value from configuration where name = 'omero.db.uuid' ",
                            new RowMapper<String>() {
                                public String mapRow(ResultSet arg0, int arg1)
                                        throws SQLException {
                                    String s = arg0.getString("value");
                                    return s;
                                }

                            }).get(0);

        }

        private final static String logLoaderQuerySql = "select value from configuration where name = ?";
        private final static String logLoaderInsertSql = "insert into configuration (name, value) values (?,?)";
        private final static String logLoaderUpdateSql = "update configuration set value = ? where name = ?";
        private final static String logLoaderDeleteSql = "delete from configuration where name = ?";

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
            return jdbc.queryForLong("select ome_nextval(?,?)", segmentValue,
                    incrementSize);

        }

        public void insertLogs(List<Object[]> batchData) {
            jdbc.batchUpdate("INSERT INTO eventlog "
                    + "(id, permissions, entityid,entitytype, action, event) "
                    + "values (?,?,?,?,?,?)", batchData);

        }

        public List<Map<String, Object>> roiByImageAndNs(final long imageId,
                final String ns) {
            String queryString;
            queryString = "select id from roi where image = " + imageId
                    + " and '" + ns + "'  = any (namespaces)";
            List<Map<String, Object>> mapList = jdbc.queryForList(queryString);
            return mapList;
        }

        public List<Long> getShapeIds(long roiId) {

            return jdbc.query("select id from shape where roi = ?",
                    new IdRowMapper(), roiId);
        }

        public String dnForUser(Long id) {
            return jdbc.queryForObject("select dn from password "
                    + "where experimenter_id = ? ", String.class, id);
        }

        public List<Map<String, Object>> dnExperimenterMaps() {
            return jdbc
            .queryForList(
                    "select dn, experimenter_id from password where dn is not null ");

        }

        public void setUserDn(Long experimenterID, String dn) {
            int results = jdbc.update(
                    "update password set dn = ? where experimenter_id = ? ",
                    dn, experimenterID);
            if (results < 1) {
                results = jdbc.update("insert into password values (?,?,?) ",
                        experimenterID, null, dn);
            }

        }

        public void setFileRepo(long id, String repoId) {
            jdbc.update("update originalfile set repo = ? where id = ?",
                    repoId, id);
        }

        public void setPixelsNamePathRepo(long pixId, String name, String path,
                String repoId) {
            jdbc.update("update pixels set name = ? where id = ?", name, pixId);
            jdbc.update("update pixels set path = ? where id = ?", path, pixId);
            jdbc.update("update pixels set repo = ? where id = ?", repoId,
                    pixId);
        }

        public List<Long> getDeletedIds(String entityType) {
            List<Long> list;

            String sql = "select entityid from eventlog "
                    + "where action = 'DELETE' and entitytype = ?";

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
                    "select distinct e.omename from experimenter e, "
                            + "groupexperimentermap m, experimentergroup g "
                            + "where e.id = m.child and m.parent = g.id and "
                            + "g.name = 'user'; ", new RowMapper<String>() {
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
                        "update originalfile set params = null where id = ?",
                        id);
            } else {
                boolean first = true;
                StringBuilder sb = new StringBuilder();
                List<Object> list = new ArrayList<Object>();
                sb.append("update originalfile set params = array[");
                for (String key : params.keySet()) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append(",");
                    }
                    sb.append("array[?,?]");
                    list.add(key);
                    list.add(params.get(key));
                }
                sb.append("] where id = ?");
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
                        "select params from originalfile where id = ?",
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
                throw new InternalException("Potential jdbc jar error.");
            }
        }

        public List<String> getFileParamKeys(long id) throws InternalException {
            try {
                return jdbc.queryForObject(
                        "select params[1:array_upper(params,1)][1:1] "
                                + "from originalfile where id = ?",
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
                throw new InternalException("Potential jdbc jar error.");
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
                        "update pixels set params = null where id = ?", id);
            } else {
                boolean first = true;
                StringBuilder sb = new StringBuilder();
                List<Object> list = new ArrayList<Object>();
                sb.append("update pixels set params = array[");
                for (String key : params.keySet()) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append(",");
                    }
                    sb.append("array[?,?]");
                    list.add(key);
                    list.add(params.get(key));
                }
                sb.append("] where id = ?");
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
                        "select params from pixels where id = ?",
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
                throw new InternalException("Potential jdbc jar error.");
            }
        }

        public List<String> getPixelsParamKeys(long id)
                throws InternalException {
            try {
                return jdbc.queryForObject(
                        "select params[1:array_upper(params,1)][1:1] "
                                + "from pixels where id = ?",
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
                throw new InternalException("Potential jdbc jar error.");
            }
        }

        //
        // End PgArrayHelper
        //
    }


}
