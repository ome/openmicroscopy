/*
 * Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
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

package ome.services.util;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Provides a timer for having a thread sleep that prematurely awakens when the server is to shut down,
 * so the sleeping does not keep the server running.

 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.0.3
 */
public class SleepTimer {
    /* Only when this bean is being destroyed do permits on this semaphore become available. */
    private static volatile Semaphore SHARED_SEMAPHORE = new Semaphore(0);

    /**
     * Sleep for the given number of milliseconds. Returns early if the server is shutting down.
     * Do <em>not</em> use in methods that block bean destruction.
     * @param milliseconds for how long to sleep if the server remains running
     * @return if the sleep was terminated prematurely
     */
    public static boolean sleepFor(long milliseconds) {
        final Semaphore semaphoreCopy = SHARED_SEMAPHORE;
        if (semaphoreCopy == null) {
            return true;
        }
        try {
            return semaphoreCopy.tryAcquire(milliseconds, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return true;
        }
    }

    /**
     * This method is called by Spring during server shut-down.
     */
    public void destroy() {
        final Semaphore semaphoreCopy = SHARED_SEMAPHORE;
        if (semaphoreCopy != null) {
            SHARED_SEMAPHORE = null;
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                // does not matter
            }
            while (semaphoreCopy.hasQueuedThreads()) {
                semaphoreCopy.release();
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    // does not matter
                }
            }
        }
    }
}
