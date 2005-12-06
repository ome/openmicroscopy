/*
 * ome.logic.HierarchyBrowsingImpl
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

/*------------------------------------------------------------------------------
 *
 * Written by:    Josh Moore <josh.moore@gmx.de>
 *
 *------------------------------------------------------------------------------
 */

package ome.logic;

//Java imports

//Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//Application-internal dependencies
import ome.api.IO;
import ome.io.bdb.Datastore;




/**
 * implementation of the Pojos service interface
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since OMERO 2.0
 */
public class IOImpl implements IO {

    private static Log log = LogFactory.getLog(IOImpl.class);

    private Datastore store;
    
    public IOImpl(Datastore datastore){
        store = datastore;
    }

    public void deletePixels(long id)
    {
        store.deletePixels(id);
    }

    public void deletePlane(long id, int z, int c, int t)
    {
        store.deletePlane(id, z, c, t);
    }

    public byte[][][][] getPixels(long id)
    {
        return store.getPixels(id);
    }

    public byte[] getPlane(long id, int z, int c, int t, int offset, int length)
    {
        return store.getPlane(id, z, c, t, offset, length);
    }

    public byte[] getPlane(long id, int z, int c, int t)
    {
        return store.getPlane(id, z, c, t);
    }

    public boolean pixelsExist(long id)
    {
        return store.pixelsExist(id);
    }

    public boolean planeExists(long id, int z, int c, int t)
    {
        return store.planeExists(id, z, c, t);
    }

    public void putPixels(byte[][][][] pixels, long id)
    {
        store.putPixels(pixels, id);
    }

    public void putPlane(byte[] plane, long id, int z, int c, int t, int offset, int length)
    {
        store.putPlane(plane, id, z, c, t, offset, length);
    }

    public void putPlane(byte[] plane, long id, int z, int c, int t)
    {
        store.putPlane(plane, id, z, c, t);
    }
    
    
  
}

