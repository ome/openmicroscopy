/*
 * ome.api.IUpdate
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

package ome.api;

// Java imports
import java.util.Collection;
import java.util.Map;

// Third-party libraries

// Application-internal dependencies
import ome.model.IObject;

/**
 * Provides methods for directly updating object graphs. 
 * 
 * @author <br>
 *         Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de"> josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Revision: $ $Date: $)
 *          </small>
 * @since OMERO3.0
 */
public interface IUpdate {

    void saveCollection(Collection graph);
    void saveObject(IObject graph);
    void saveMap(Map graph);
    void saveArray(IObject[] graph);
 
    Collection  saveAndReturnCollection(Collection graph);
    Map         saveAndReturnMap(Map map);
    IObject     saveAndReturnObject(IObject graph);
    IObject[]   saveAndReturnArray(IObject[] graph);

    void deleteObject(IObject row);
    
}
