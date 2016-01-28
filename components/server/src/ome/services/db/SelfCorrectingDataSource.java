/*
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.db;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.sql.DataSource;

import ome.conditions.DatabaseBusyException;
import ome.util.messages.UserSignalMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.jdbc.datasource.DelegatingDataSource;

/**
 * {@link DataSource} delegate which wraps the
 */
public class SelfCorrectingDataSource extends DelegatingDataSource 
    implements ApplicationListener<UserSignalMessage> {

    private final static Logger log = LoggerFactory
            .getLogger(SelfCorrectingDataSource.class);

    /**
     * Length of time that errors are used in the calculation of
     * {@link DatabaseBusyException#backOff}
     */
    private final long errorTimeout;

    private final int maxRetries;

    private final long maxBackOff;

    private final List<Long> errorTimes = new ArrayList<Long>();

    public SelfCorrectingDataSource(DataSource delegate,
            long timeoutInMilliseconds) {
        this(delegate, timeoutInMilliseconds, 3, 10 * 1000L);
    }

    public SelfCorrectingDataSource(DataSource delegate,
            long timeoutInMilliseconds, int maxRetries, long maxBackOff) {
        super(delegate);
        this.errorTimeout = timeoutInMilliseconds;
        this.maxRetries = maxRetries;
        this.maxBackOff = maxBackOff;
    }

    public Connection getConnection() throws SQLException {
        return callWithRetries(null, null, false);
    }

    public Connection getConnection(String username, String password)
            throws SQLException {
        return callWithRetries(username, password, true);
    }

    /**
     * Handles the USR1 posix signal by calling close on the underlying
     * {@link #getTargetDataSource() data source} via reflection. The
     * assumption is that the next call to any methods will re-initialize
     * the data source. This is the case with
     * bitronix.tm.resource.jdbc.PoolingDataSource
     *
     * @see <a href="http://trac.openmicroscopy.org/ome/ticket/4210">ticket:4210</a>
     */
    public void onApplicationEvent(UserSignalMessage usm) {
        if (usm.signal == 1) {
            log.info("Received USR" + usm.signal + " - calling close()");
            try {
                Method m = getTargetDataSource().getClass().getMethod("close");
                m.invoke(getTargetDataSource());
            } catch (Exception e) {
                log.error("Failed to close " + getTargetDataSource(), e);
            }
        }
    }

    // Helpers
    // =========================================================================

    protected Connection callWithRetries(String username, String password,
            boolean useArgs) throws SQLException {
        int retries = 0;
        while (true) {
            try {
                return call(username, password, useArgs);
            } catch (SQLException sql) {
                long backOff = markAndSweep();
                retries++;
                if (retries < maxRetries) {
                    log.info("Sleeping for " + backOff + " then retry: "
                            + retries);
                    try {
                        Thread.sleep(backOff);
                        continue;
                    } catch (InterruptedException e) {
                        // Ok. Outer while loop while catch us.
                    }
                }
                log.error("Failed to acquire connection after retries="
                        + maxRetries, sql);
                throw new DatabaseBusyException("Cannot acquire connection",
                        backOff);
            }
        }
    }

    protected Connection call(String username, String password, boolean useArgs)
            throws SQLException {
        if (useArgs) {
            return super.getConnection(username, password);
        } else {
            return super.getConnection();
        }
    }

    /**
     * First removes all entries in {@link #errorTimes} that are older than some
     * given time and then uses the remaining number of errors to determine the
     * backoff : (#^1/2)*1000 milliseconds.
     */
    protected long markAndSweep() {
        final long timeAgo = System.currentTimeMillis() - errorTimeout;
        synchronized (errorTimes) {
            int location = Collections.binarySearch(errorTimes, timeAgo);
            log.info("Found location in errorTimes: " + location);
            if (location < 0) {
                location = -location - 1; // search returns -insertion-1
            }

            List<Long> subList = new ArrayList<Long>(errorTimes.subList(
                    location, errorTimes.size()));

            int eSize = errorTimes.size();
            int sSize = subList.size();

            log.info("Removing " + (eSize - sSize) + " from errorTimes");
            errorTimes.clear();
            errorTimes.addAll(subList);
            log.warn("Registering error with list: Current size: " + sSize);
            errorTimes.add(System.currentTimeMillis());
            return calculateBackOff(sSize);
        }
    }

    protected long calculateBackOff(int numberOfErrors) {
        long backOff = 1000L * Math.round(Math.sqrt(numberOfErrors));
        if (backOff > maxBackOff) {
            return maxBackOff;
        } else {
            return backOff;
        }
    }

}
