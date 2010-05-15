/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.db;

import java.sql.ResultSet;
import java.sql.Array;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;

/**
 * Strategy class for setting array values in the database. Must be used from
 * within an existing transaction.
 */
public class PgArrayHelper {

    private final SimpleJdbcOperations jdbc;

    public PgArrayHelper(SimpleJdbcOperations jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Appends "{key, value}" onto the original file "params" field or replaces
     * the value if already present.
     */
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

    /**
     * Resets the entire original file "params" field.
     */
    public int setFileParams(final long id, Map<String, String> params) {
        if (params == null || params.size() == 0) {
            return jdbc.update(
                    "update originalfile set params = null where id = ?", id);
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
            return jdbc.update(sb.toString(), (Object[]) list
                    .toArray(new Object[list.size()]));
        }
    }

    /**
     * Loads all the (possibly empty) params for the given original file. If the
     * id is not found, null is returned.
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> getFileParams(final long id) {
        try {
            return jdbc.queryForObject(
                    "select params from originalfile where id = ?",
                    new RowMapper<Map<String, String>>() {
                        public Map<String, String> mapRow(ResultSet arg0, int arg1)
                                throws SQLException {
                            Map<String, String> params = new HashMap<String, String>();
                            String[][] arr = (String[][]) arg0.getArray(1)
                                    .getArray();
                            for (int i = 0; i < arr.length; i++) {
                                params.put(arr[i][0], arr[i][1]);
                            }
                            return params;
                        }
                    }, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Returns only the (possibly empty) keys which are set on the given
     * original file. If the given original file cannot be found, null is
     * returned.
     */
    public List<String> getFileParamKeys(long id) {
        try {
            return jdbc.queryForObject(
                    "select params[1:array_upper(params,1)][1:1] "
                            + "from originalfile where id = ?",
                    new RowMapper<List<String>>() {
                        public List<String> mapRow(ResultSet arg0, int arg1)
                                throws SQLException {
                            final List<String> keys = new ArrayList<String>();
                            String[][] arr = (String[][]) arg0.getArray(1)
                                    .getArray();
                            for (int i = 0; i < arr.length; i++) {
                                keys.add(arr[i][0]);
                            }
                            return keys;
                        }
                    }, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }


    /**
     * Appends "{key, value}" onto the original file "params" field or replaces
     * the value if already present.
     */
    public int setPixelsParam(final long id, final String key, final String value) {
        Map<String, String> params = getPixelsParams(id);
        if (params == null) {
            params = new HashMap<String, String>();
        }
        params.put(key, value);
        // Alternative would be to do an either-or with a concat
        // "set params = params || array[array[?,?]] where id = ?"
        return setPixelsParams(id, params);
    }

    /**
     * Resets the entire original file "params" field.
     */
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
            return jdbc.update(sb.toString(), (Object[]) list
                    .toArray(new Object[list.size()]));
        }
    }

    /**
     * Loads all the (possibly empty) params for the given original file. If the
     * id is not found, null is returned.
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> getPixelsParams(final long id) {
        try {
            return jdbc.queryForObject(
                    "select params from pixels where id = ?",
                    new RowMapper<Map<String, String>>() {
                        public Map<String, String> mapRow(ResultSet arg0, int arg1)
                                throws SQLException {
                            Map<String, String> params = new HashMap<String, String>();
                            Array arr1 = arg0.getArray(1);
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
        }
    }

    /**
     * Returns only the (possibly empty) keys which are set on the given
     * original file. If the given original file cannot be found, null is
     * returned.
     */
    public List<String> getPixelsParamKeys(long id) {
        try {
            return jdbc.queryForObject(
                    "select params[1:array_upper(params,1)][1:1] "
                            + "from pixels where id = ?",
                    new RowMapper<List<String>>() {
                        public List<String> mapRow(ResultSet arg0, int arg1)
                                throws SQLException {
                            final List<String> keys = new ArrayList<String>();
                            String[][] arr = (String[][]) arg0.getArray(1)
                                    .getArray();
                            for (int i = 0; i < arr.length; i++) {
                                keys.add(arr[i][0]);
                            }
                            return keys;
                        }
                    }, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

}
