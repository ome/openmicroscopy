/*
 * ome.api.local.LocalUpdate
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.api.local;

/**
 * Provides local (internal) extensions for updating
 * 
 * @author <br>
 *         Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de"> josh.moore@gmx.de</a>
 * @since OMERO3.0
 */
public interface LocalUpdate extends ome.api.IUpdate {

    void flush();

}
