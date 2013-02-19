/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.db;

import ome.util.SqlAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the unique identity of this database, consisting of the
 * omero.db.authority and omero.db.uuid properties. Used primarily to fulfill
 * the LSID contract of globally unique identifiers. On database initialization:
 * <p>
 * 
 * <pre>
 *   bin/omero db script
 *   psql my_database &lt; script
 * </pre>
 * 
 * </p>
 * a UUID is added to the "configuration" table with the key "omero.db.uuid".
 * This value will be used in all objects exported from this database, so that
 * they can be cleanly re-imported.
 * 
 * This implies that it is <em>not safe</em> to copy a database and use it
 * actively while the original database is still running. Only use database
 * copies (or "dumps") as a backup in case of catastrophic failure.
 * 
 * A default authority of "export.openmicroscopy.org" is used to simplify
 * initial configuration, but you are welcome to use a domain belonging to you
 * as the authority. If you choose to do so, you will need to use the same
 * authority on any host which you may happen to migrate your database to.
 */
public class DatabaseIdentity {

    private static String uuid(SqlAction sql) {
        return sql.dbUuid();
    }

    private final static Logger log = LoggerFactory.getLogger(DatabaseIdentity.class);

    private final String authority;

    private final String uuid;

    private final String format;

    public DatabaseIdentity(String authority, SqlAction sql) {
        this(authority, uuid(sql));
    }

    public DatabaseIdentity(String authority, String uuid) {
        this.authority = authority;
        this.uuid = uuid;
        this.format = String.format("urn:lsid:%s:%%s:%s_%%s%%s", authority, uuid);
        log.info("Using LSID format: " + format);
    }

    public String getAuthority() {
        return authority;
    }

    public String getUuid() {
        return uuid;
    }

    public boolean valid(String lsid) {
        return false;
    }

    public boolean own(String lsid) {
        return false;
    }

    public String lsid(Class k, long id) {
        return String.format(format, parse(k), id, "");
    }

    public String lsid(Class k, long id, long version) {
        return String.format(format, parse(k), id, ":" + version);
    }

    public String lsid(String ns, String id) {
        return String.format(format, ns, id, "");
    }

    public String lsid(String ns, String id, String version) {
        return String.format(format, ns, id, version);
    }

    // Helpers
    // =========================================================================

    private String parse(Class k) {
        String name = k.getSimpleName();
        int last = name.length() - 1;
        if (name.substring(last).equals("I")) {
            return name.substring(0, last);
        } else {
            return name;
        }
    }
}
