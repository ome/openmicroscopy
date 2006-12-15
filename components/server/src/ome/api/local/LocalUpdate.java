/*
 * ome.api.local.LocalUpdate
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.api.local;

// Java imports

// Third-party libraries

// Application-internal dependencies

/**
 * Provides local (internal) extensions for updating
 * 
 * @author <br>
 *         Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de"> josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Revision$ $Date$)
 *          </small>
 * @since OMERO3.0
 */
public interface LocalUpdate extends ome.api.IUpdate {

    void rollback();

    void flush();

    void commit();

}
