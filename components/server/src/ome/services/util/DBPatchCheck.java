/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import ome.conditions.InternalException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * Hook run by the context. This hook tests the database version against the
 * software version on {@link #start()}.
 * 
 * @author Josh Moore, josh at glencoesoftwarecom
 * @since 3.0-Beta3
 */
public class DBPatchCheck {

    public final static Log log = LogFactory.getLog(DBPatchCheck.class);

    ResourceBundle bundle = ResourceBundle.getBundle("omero");

    SimpleJdbcTemplate jdbc;

    public void setSimpleJdbcTemplate(SimpleJdbcTemplate template) {
        this.jdbc = template;
    }

    private final static String line = "***************************************************************************************\n";
    private final static String see = "See https://trac.openmicroscopy.org.uk/omero/wiki/DatabaseUpgrades\n";
    private final static String no_table = mk("Error connecting to database table dbpatch. You may need to bootstrap.\n");
    private final static String wrong_version = mk("DB version (%s) does not match omero.properties (%s). Please apply a db upgrade.\n");

    private static String mk(String msg) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append(line);
        sb.append(msg);
        sb.append(see);
        sb.append(line);
        return sb.toString();
    }

    /**
     * Attempts twice to connect to the server to overcome any initial
     * difficulties.
     * 
     * @see <a
     *      href="https://trac.openmicroscopy.org.uk/omero/ticket/444">ticket:444</a>
     */
    public void start() throws Exception {

        try {
            patchLevel();
        } catch (Exception e) {
            // ok. This causes the strange JBoss/Postgres driver exception
            // which will be logged but does not prevent execution.
        }

        String patch = null;
        try {
            patch = patchLevel();
        } catch (Exception e) {
            log.fatal(no_table, e);
            InternalException ie = new InternalException(no_table);
            throw ie;
        }

        String version = bundle.getString("omero.dbversion");
        String dbpatch = bundle.getString("omero.dbpatch");
        String omero = version + "__" + dbpatch;
        if (patch == null || !patch.equals(omero)) {
            String str = String.format(wrong_version, patch, omero);
            log.fatal(str);
            InternalException ie = new InternalException(str);
            throw ie;
        }

        log.info(String.format("Verified database patch: %s", patch));
    }

    /**
     * Retrieves the newest database patch. Also functions a simple DB ping.
     */
    protected String patchLevel() {
        return jdbc.query(
                "select currentversion, currentpatch from dbpatch "
                        + "order by id desc limit 1",
                new ParameterizedRowMapper<String>() {
                    public String mapRow(ResultSet arg0, int arg1)
                            throws SQLException {
                        String v = arg0.getString("currentversion");
                        int p = arg0.getInt("currentpatch");
                        return v + "__" + p;
                    }

                }).get(0);
    }

}
