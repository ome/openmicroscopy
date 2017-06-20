/*
 * Copyright (C) 2017 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.security.basic;

import java.util.Collection;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ome.util.SqlAction;

/**
 * Periodically clean up old entries from the <tt>_current_admin_privileges</tt> database table.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.4.0
 */
public class LightAdminPrivilegesCleanup implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(LightAdminPrivilegesCleanup.class);

    private final SqlAction sqlAction;
    private final long delayMs;

    private Collection<Long> transactionIds = Collections.emptyList();

    /**
     * Start a new scheduled repeating task for cleaning up the <tt>_current_admin_privileges</tt> database table.
     * @param sqlAction the SQL action to use for executing the cleanup JDBC
     * @param delay the interval to wait in between cleanups, in seconds
     */
    public LightAdminPrivilegesCleanup(SqlAction sqlAction, int delay) {
        this.sqlAction = sqlAction;
        delayMs = 1000L * delay;
    }

    @Override
    public void run() {
        LOGGER.debug("running periodic cleanup of _current_admin_privileges table");
        sqlAction.deleteOldAdminPrivileges(transactionIds);
        transactionIds = sqlAction.findOldAdminPrivileges();
        try {
            /* simple emulation of ThreadPoolTaskScheduler.scheduleWithFixedDelay */
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            LOGGER.debug("interrupted after cleanup of _current_admin_privileges table");
        }
    }
}
