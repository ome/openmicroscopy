/*
 * ome.api.StatefulServiceInterface
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.api;

// Java imports

// Third-party libraries

// Application-internal dependencies
import ome.model.meta.Event;
import ome.system.EventContext;

/**
 * OMERO API Interface with stateful semantics.
 * 
 * @author <br>
 *         Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de"> josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Revision$ $Date$)
 *          </small>
 * @since OME3.0
 */
public interface StatefulServiceInterface extends ServiceInterface {

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
