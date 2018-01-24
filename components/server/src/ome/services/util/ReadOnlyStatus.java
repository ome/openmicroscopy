/*
 *   Copyright 2017 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.util;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import ome.util.SqlAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.dao.InvalidDataAccessResourceUsageException;

/**
 * Hook run by the context as early as possible to detect if the DB
 * connection is read-only. This object can be used by other objects
 * to skip certain steps. Later actions may flip the state of the status.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @author m.t.b.carroll@dundee.ac.uk
 */
public class ReadOnlyStatus {

    private final static Logger log = LoggerFactory.getLogger(ReadOnlyStatus.class);

    private final boolean isReadOnlyDb, isReadOnlyRepo;

    public ReadOnlyStatus(boolean isReadOnly, boolean isReadOnlyDb, boolean isReadOnlyRepo,
            SqlAction sqlAction, String omeroDataDir, String omeroManagedDir) throws SQLException {
        this.isReadOnlyDb = isReadOnly || isReadOnlyDb || !canWriteDb(sqlAction);
        this.isReadOnlyRepo = isReadOnly || isReadOnlyRepo || !canWriteDir(omeroDataDir) || !canWriteDir(omeroManagedDir);
        log.info("read-only status: db={}, repo={}", this.isReadOnlyDb, this.isReadOnlyRepo);
    }

    private boolean canWriteDb(SqlAction sqlAction) {
        try {
            sqlAction.deleteCurrentAdminPrivileges();
            return true;
        } catch (InvalidDataAccessResourceUsageException idarue) {
            log.debug("cannot write to database", idarue);
            log.warn("not configured for read-only database but assuming read-only anyway");
            return false;
        }
    }

    private boolean canWriteDir(String directoryName) {
        try {
            /* do not trust File.can*() methods as the directory may be on a read-only mount */
            final File directory = new File(directoryName);
            directory.mkdirs();
            File.createTempFile(getClass().getSimpleName(), null, directory).delete();
            return true;
        } catch (IOException ioe) {
            log.debug("cannot write to binary repository", ioe);
            log.warn("not configured for read-only binary repository but assuming read-only anyway");
            return false;
        }
    }

    public boolean isReadOnlyDb() {
        return isReadOnlyDb;
    }

    public boolean isReadOnlyRepo() {
        return isReadOnlyRepo;
    }
}
