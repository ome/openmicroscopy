/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.eventlogs;

import java.lang.Long;
import java.lang.Integer;
import java.lang.NumberFormatException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import ome.api.ITypes;
import ome.conditions.InternalException;
import ome.model.IEnum;
import ome.model.meta.EventLog;
import ome.util.SqlAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;

/**
 * {@link EventLogLoader} implementation which keeps tracks of the last
 * {@link EventLog} instance, and always provides the next unindexed instance.
 * Reseting that saved value would restart indexing.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public abstract class PersistentEventLogLoader extends EventLogLoader {

    private final static String INDEX_DIR = File.separator + "FullText"
        + File.separator;

    private final static String PROPERTIES_FILE = INDEX_DIR + "id.properties";

    private final static int MAX_LOAD_ATTEMPTS = 3;

    /**
     * Key used to look configuration value; 'name'
     */
    protected String key;

    protected String atmptKey;

    protected String prevKey;

    protected ITypes types;

    protected SqlAction sql;

    /**
     * If true, then progress through the event log will be tracked in a log
     * file stored in the {@link dataDir} instead of a field in the database.
     * Defaults to false.
     */
    protected boolean useFileLog = false;

    protected String dataDir;

    public void setKey(String key) {
        this.key = key;
        this.atmptKey = key + ".attempts";
        this.prevKey = key + ".previous";
    }
    public void setTypes(ITypes types) {
        this.types = types;
    }

    public void setUseFileLog(boolean useFileLog) {
        this.useFileLog = useFileLog;
    }

    public void setDataDir(String dataDir) {
        this.dataDir = dataDir;
    }

    public void setSqlAction(SqlAction sql) {
        this.sql = sql;
    }

    @Override
    protected EventLog query() {

        long current_id = getCurrentId();
        log.debug("Current event ID is: {}", current_id);

        EventLog el = nextEventLog(current_id);
        if (el != null) {
            log.debug("Next event ID to process is: {}", el.getId());
            setCurrentId(el.getId());
        }
        return el;

    }

    /**
     * Called when the configuration database does not contain a valid
     * current_id.
     */
    public abstract void initialize();

    /**
     * Get current {@link EventLog} id. If the lookup throws an exception,
     * either the configuration has been deleted or renamed, in which we need to
     * reinitialize, or the table is missing and something is wrong.
     */
    public long getCurrentId() {
        if (useFileLog) {
            try {
                return currentIdFromFile();
            } catch (Exception e) {
                log.debug("Problem reading current event log ID file: " + e);
                log.info("Falling back to database for ID tracking.");
                this.useFileLog = false;
                return currentIdFromDatabase();
            }
        } else {
            return currentIdFromDatabase();
        }
    }

    private long currentIdFromFile() throws IOException {
        Long curr = null;
        File idFile = new File(dataDir + PROPERTIES_FILE);
        if (idFile.exists()) {
            FileReader idReader = new FileReader(idFile);
            try {
                Properties idProperties = new Properties();
                idProperties.load(idReader);

                if (fresh()) {
                    // This is the start of a new batch.
                    String prev = idProperties.getProperty(prevKey);
                    if (prev != null) {
                        // If there is a previous ID in properties, that implies
                        // the last batch did not finish cleanly, so retry the
                        // previous ID so long as we've not exceeded the max
                        // number of attempts.
                        Integer a;
                        try {
                            curr = new Long(idProperties.getProperty(prevKey));
                            a = new Integer(idProperties.getProperty(atmptKey));
                            if (a > MAX_LOAD_ATTEMPTS) {
                                log.debug("Exceeded MAX_LOAD_ATTEMPTS trying to "
                                        + "load event ID {}. Progressing to "
                                        + "next event.", curr);
                                a = 1;
                                curr = null;
                            } else {
                                a += 1;
                            }
                        } catch (NumberFormatException e) { a = 1; }
                        idProperties.setProperty(atmptKey, a.toString());
                    }
                }

                if (curr == null) {
                    // Either last batch finished cleanly, or we're in the
                    // middle of a batch. Load from file normally:
                    try {
                        curr = new Long(idProperties.getProperty(key));
                    } catch (NumberFormatException e) {
                        log.debug("Problem loading current ID from file: " + e);
                        curr = currentIdFromMigration();
                    }
                }

                // Need to re-store the current ID since `#query()` calls
                // `#setCurrentId()` with the value of the next ID immediately
                // following this method, and `#setCurrentID()` will move the
                // value of the current ID property into the previous ID
                // property (so if we've just loaded the previous ID, we need to
                // temporarily make it the current ID long enough for it to get
                // moved back to the previous ID property).
                FileWriter idWriter = new FileWriter(idFile);
                try {
                    idProperties.setProperty(key, curr.toString());
                    idProperties.store(idWriter, null);
                } finally { idWriter.close(); }
            } finally { idReader.close(); }
        } else { return currentIdFromMigration(); }
        return curr.longValue();
    }

    private long currentIdFromMigration() {
        // We are in a migration situation, in which case the ID will be in the
        // database. Retrieve it so that it can be used and stored in the
        // properties file.
        long curr;
        try {
            curr = currentIdFromDatabase();
        } catch (InternalException e) {
            // Problem getting id from database, start from the beginning.
            curr = -1;
            setCurrentId(-1);
            initialize();
        }
        return curr;
    }

    private long currentIdFromDatabase() {
        long current_id;
        try {
            current_id = sql.selectCurrentEventLog(key);
        } catch (EmptyResultDataAccessException erdae) {
            // This event log loader has never been run. Initialize
            current_id = -1;
            setCurrentId(-1);
            initialize();
        } catch (DataAccessException dae) {
            // Most likely there's no configuration table.
            throw new InternalException(
                    "The configuration table seems to be missing \n"
                            + "from your database. Please check your server installation instructions \n"
                            + "for possible reasons.");
        }
        return current_id;
    }

    public void setCurrentId(long id) {
        if (useFileLog) {
            try {
                setCurrentIdFile(id);
            } catch (Exception e) {
                log.warn("Problem writing current event log ID file: " + e);
                log.info("Falling back to database for ID tracking.");
                this.useFileLog = false;
                setCurrentIdDatabase(id);
            }
        } else {
            setCurrentIdDatabase(id);
        }
    }

    private void setCurrentIdFile(long id) throws IOException {
        // Argument passed in is actually the next ID we will attempt to index.
        // If indexing fails, then when the indexer resumes it will load the
        // previous ID we store here, then look in the database for the next ID
        // (which should be the same as the `id` argument to this function) and
        // resume indexing.
        File idFile = new File(dataDir + PROPERTIES_FILE);
        Properties idProperties = new Properties();
        if (idFile.exists()) {
            FileReader idReader = new FileReader(idFile);
            try {
                idProperties.load(idReader);
                String prev = idProperties.getProperty(key);
                if (prev != null) {
                    // Move old current to previous
                    idProperties.setProperty(prevKey, prev);
                }
            } finally { idReader.close(); }
        }
        FileWriter idWriter = new FileWriter(idFile);
        try {
            idProperties.setProperty(key, (new Long(id)).toString());
            idProperties.store(idWriter, null);
        } finally { idWriter.close(); }
    }

    private void setCurrentIdDatabase(long id) {
        sql.setCurrentEventLog(id, key);
    }

    public void deleteCurrentId() {
        if (useFileLog) {
            File idFile = new File(dataDir + PROPERTIES_FILE);
            idFile.delete();
        } else {
            sql.delCurrentEventLog(key);
        }
    }

    @Override
    public long more() {
        long diff = lastEventLog().getEntityId() - getCurrentId();
        return diff < 0 ? 0 : diff;
    }

    @Override
    protected void reset() {
        super.reset();
        if (useFileLog) {
            try {
                // Clear the previous ID and set attempts back to 1.
                File idFile = new File(dataDir + PROPERTIES_FILE);
                if (idFile.exists()) {
                    FileReader idReader = new FileReader(idFile);
                    FileWriter idWriter = new FileWriter(idFile);
                    try {
                        Properties idProperties = new Properties();
                        idProperties.load(idReader);
                        idProperties.remove(prevKey);
                        idProperties.setProperty(atmptKey, "1");
                        idProperties.store(idWriter, null);
                    } finally {
                        idReader.close();
                        idWriter.close();
                    }
                }
            } catch (Exception e) {
                log.debug("Error resetting properties: {}", e);
            }
        }
    }
}
