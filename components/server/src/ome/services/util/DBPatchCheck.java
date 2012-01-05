/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.util;

import ome.api.local.LocalConfig;
import ome.conditions.InternalException;
import ome.system.PreferenceContext;
import ome.util.SqlAction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * Hook run by the context. This hook tests the database version against the
 * software version on {@link #start()}.
 * 
 * @author Josh Moore, josh at glencoesoftwarecom
 * @since 3.0-Beta3
 */
public class DBPatchCheck {

    public final static Log log = LogFactory.getLog(DBPatchCheck.class);

    final SqlAction sql;
    final PreferenceContext prefs;

    public DBPatchCheck(SqlAction sql, PreferenceContext prefs) {
        this.sql = sql;
        this.prefs = prefs;
    }

    private final static String line = "***************************************************************************************\n";
    private final static String see = "See https://www.openmicroscopy.org/site/support/omero4/server/upgrade\n";
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
     */
    public void start() throws Exception {

        final String[] results = new String[3];
        try {
            results[0] = sql.dbVersion();
            results[1] = prefs.getProperty("omero.db.version");
            results[2] = prefs.getProperty("omero.db.patch");
        } catch (Exception e) {
            log.fatal(no_table, e);
            InternalException ie = new InternalException(no_table);
            throw ie;
        }

        String patch = results[0];
        String version = results[1];
        String dbpatch = results[2];
        String omero = version + "__" + dbpatch;
        if (patch == null || !patch.equals(omero)) {
            String str = String.format(wrong_version, patch, omero);
            log.fatal(str);
            InternalException ie = new InternalException(str);
            throw ie;
        }

        log.info(String.format("Verified database patch: %s", patch));
    }

}
