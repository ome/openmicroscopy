/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.util;

import java.io.File;
import java.io.FileFilter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ome.services.sessions.events.UserGroupUpdateEvent;
import ome.services.util.Executor.SimpleStatelessWork;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;
import org.springframework.transaction.annotation.Transactional;

/**
 * Hook run by the context at startup to create a drop-box directory per user.
 * Also listens for {@link UserGroupUpdateEvent} messages and creates a new
 * directory if necessary.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.0.0
 */
public class DropBoxDirectoryCheck implements ApplicationListener, Runnable {

    public final static Log log = LogFactory
            .getLog(DropBoxDirectoryCheck.class);

    final String omeroDataDir;

    final SimpleJdbcOperations isolatedJdbc;

    public DropBoxDirectoryCheck(String omeroDataDir, SimpleJdbcOperations jdbc) {
        this.omeroDataDir = omeroDataDir;
        this.isolatedJdbc = jdbc;
    }

    public void onApplicationEvent(ApplicationEvent arg0) {
        if (arg0 instanceof UserGroupUpdateEvent) {
            run();
        }
    }

    /**
     * Synchronizes the ${omero.data.dir}/DropBox directory by creating a
     * directory for any user who does not have one. Does not currently remove
     * directories.
     */
    public void run() {
        Set<String> users = getCurrentUserNames();
        int added = createUserDirectories(users);
        log.info("Synchronizing user directories. Added " + added);
    }

    @SuppressWarnings("unchecked")
    public Set<String> getCurrentUserNames() {
        List<String> names = isolatedJdbc.query(
                "select omename from experimenter",
                new ParameterizedRowMapper<String>() {
                    public String mapRow(ResultSet arg0, int arg1)
                            throws SQLException {
                        return arg0.getString(1); // Bleck
                    }
                });
        return new HashSet<String>(names);
    }

    public int createUserDirectories(Set<String> users) {
        int count = 0;
        for (String name : users) {
            // ticket:1398 - ignoring empty user names
            if (name == null || name.length() == 0) {
                continue;
            }
            String firstLetter = name.substring(0, 1);
            if (!firstLetter.matches("[a-zA-Z0-9]")) {
                firstLetter = "other";
            }
            File f = new File(omeroDataDir + File.separator + "DropBox"
                    + File.separator + firstLetter + File.separator + name);
            if (f.mkdirs()) {
                log.debug("Created " + f);
                count++;
            }
        }
        return count;
    }

}
