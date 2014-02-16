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

    private final static String CURRENT_ID_FILE = INDEX_DIR
        + "current_id.properties";

    private final static int MAX_LOAD_ATTEMPTS = 3;

    /**
     * Key used to look configuration value; 'name'
     */
    protected String key;

    protected String attemptsKey;

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
        this.attemptsKey = key + ".attempts";
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

        EventLog el = nextEventLog(current_id);
        if (el != null) {
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
            } catch (IOException e) {
                log.warn("Problem writting current event log ID file: " + e);
                log.info("Falling back to database for ID tracking.");
                this.useFileLog = false;
                return currentIdFromDatabase();
            }
        } else {
            return currentIdFromDatabase();
        }
    }

    public long currentIdFromFile() throws IOException {
        if (dataDir == null) {
            log.warn("Attempted to use a file to track event log progress, but "
                        + "the location of the OMERO data directory is not"
                        + "set. Falling back to database for tracking.");
            this.useFileLog = false;
            return currentIdFromDatabase();
        }

        long current_id;
        File currIdFile = new File(dataDir + CURRENT_ID_FILE);
        if (currIdFile.exists()) {
            Properties currIdProps = new Properties();
            FileReader currIdReader = new FileReader(currIdFile);
            int attempts;
            try {
                currIdProps.load(currIdReader);

                current_id = new Long(currIdProps.getProperty(key));

                try {
                    attempts = new Integer(currIdProps.getProperty(attemptsKey));
                } catch (NumberFormatException e) {
                    attempts = 0;
                }
            } finally {
                currIdReader.close();
            }

            if (attempts > MAX_LOAD_ATTEMPTS) {
                log.debug("Exceeded MAX_LOAD_ATTEMPTS trying to load event ID "
                        + "{}. Progressing to next event.", current_id);
                current_id += 1;
            }
        } else {
            // We could be in an upgrade situation, in which case the ID will be
            // in the database.
            try {
                current_id = currentIdFromDatabase();
            } catch (InternalException e) {
                // Problem getting id from database, start from the beginning.
                current_id = -1;
                setCurrentId(-1);
                initialize();
            }
        }
        return current_id;
    }

    public long currentIdFromDatabase() {
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
            } catch (IOException e) {
                log.warn("Problem writting current event log ID file: " + e);
                log.info("Falling back to database for ID tracking.");
                this.useFileLog = false;
                setCurrentIdDatabase(id);
            }
        } else {
            setCurrentIdDatabase(id);
        }
    }

    public void setCurrentIdFile(long id) throws IOException {
        if (dataDir == null) {
            log.warn("Attempted to use a file to track event log progress, but "
                        + "the location of the OMERO data directory is not"
                        + "set. Falling back to database for ID tracking.");
            this.useFileLog = false;
            setCurrentIdDatabase(id);
        }

        File currIdFile = new File(dataDir + CURRENT_ID_FILE);
        Properties currIdProps = new Properties();
        int attempts = 1;
        if (currIdFile.exists()) {
            long last_id;
            FileReader currIdReader = new FileReader(currIdFile);
            try {
                currIdProps.load(currIdReader);
                last_id = new Long(currIdProps.getProperty(key));
                attempts = new Integer(currIdProps.getProperty(attemptsKey));
                if (last_id == id) {
                    attempts += 1;
                }
            } catch (NumberFormatException e) {
                // Something went wrong reading properties, so just assume this
                // is the first attempt at reading `id`.
                log.debug("Problem loading last indexed ID and/or number of "
                        + "attempts from current ID log file: " + e);
            } finally {
                currIdReader.close();
            }
        }

        FileWriter currIdWriter = new FileWriter(currIdFile);
        try {
            currIdProps.setProperty(key, Long.toString(id));
            currIdProps.setProperty(attemptsKey, Integer.toString(attempts));
            currIdProps.store(currIdWriter, null);
            log.debug("Attempted to load event ID #{} {} time{}.",
                    id, attempts, attempts > 1 ? "s" : "");
        } finally {
            currIdWriter.close();
        }
    }

    public void setCurrentIdDatabase(long id) {
        sql.setCurrentEventLog(id, key);
    }

    public void deleteCurrentId() {
        if (useFileLog) {
            File currIdFile = new File(dataDir + CURRENT_ID_FILE);
            currIdFile.delete();
        } else {
            sql.delCurrentEventLog(key);
        }
    }

    @Override
    public long more() {
        long diff = lastEventLog().getEntityId() - getCurrentId();
        return diff < 0 ? 0 : diff;
    }

}
