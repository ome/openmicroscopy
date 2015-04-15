/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package omero.gateway.facility;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import omero.gateway.Gateway;
import omero.log.LogMessage;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

//Java imports

/**
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.1
 */

public abstract class Facility {
    /** Holds references to the different facilities so that they can be reused */
    private static final Cache<String, Facility> cache = CacheBuilder.newBuilder().build();

    /** Reference to the {@link Gateway} */
    final Gateway gateway;

    /**
     * Creates a new instance
     * 
     * @param gateway
     *            Reference to the {@link Gateway}
     */
    Facility(Gateway gateway) {
        this.gateway = gateway;
    }

    /**
     * Get a reference to a certain Facility
     * 
     * @param type
     *            The type of the Facility
     * @param gateway
     *            Reference to the {@link Gateway}
     * @return See above
     * @throws ExecutionException
     */
    @SuppressWarnings("unchecked")
    public static <T extends Facility> T getFacility(final Class<T> type,
            final Gateway gateway) throws ExecutionException {

        return (T) cache.get(type.getSimpleName(), new Callable<Facility>() {

            @Override
            public Facility call() throws Exception {
                return type.getDeclaredConstructor(Gateway.class).newInstance(
                        gateway);
            }

        });
    }
    
    public void logDebug(Object originator, String msg, Throwable t) {
        gateway.getLogger().debug(originator, new LogMessage(msg, t));
    }
    
    public void logInfo(Object originator, String msg, Throwable t) {
        gateway.getLogger().info(originator, new LogMessage(msg, t));
    }
    
    public void logWarn(Object originator, String msg, Throwable t) {
        gateway.getLogger().warn(originator, new LogMessage(msg, t));
    }
    
    public void logError(Object originator, String msg, Throwable t) {
        gateway.getLogger().error(originator, new LogMessage(msg, t));
    }
    
}
