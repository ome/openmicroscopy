/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.api;

import ome.model.meta.Event;
import ome.system.EventContext;

/**
 * OMERO API Interface with stateful semantics.
 *
 * As of 4.0, each stateful service is responsible for providing its
 * own passivation/activation logic in the similarly named methods.
 *
 * @author <br>
 *         Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de"> josh.moore@gmx.de</a>
 * @version 1.0
 * @since OME3.0
 */
public interface StatefulServiceInterface extends ServiceInterface {

    
    /**
     * Perform whatever passivation is possible or throw an exception.
     * A good passivation method will free up as much memory as
     * possible, most likely by storing it to disk. A call to
     * passivate should be safe even if the service is already
     * passivated.
     */
    void passivate();
    
    /**
     * Completely restore this service for active use from whatever
     * passivation it has implemented. A call to activate should be
     * safe even if the service is already activated.
     */
    void activate();

    /**
     * signals the end of the service lifecycle. Resources such as Sessions can
     * be released. All further calls will throw an exception.
     */
    void close();

    /**
     * Returns the current {@link EventContext} for this instance. This is
     * useful for later identifying changes made by this {@link Event}.
     */
    EventContext getCurrentEventContext();
}
