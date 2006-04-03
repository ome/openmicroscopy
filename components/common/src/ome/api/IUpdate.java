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
import ome.conditions.ValidationException;
import ome.model.IObject;

/**
 * Provides methods for directly updating object graphs. IUpdate is the lowest
 * level (level-1) interface which may make changes (INSERT, UPDATE, DELETE)
 * to the database. All other methods of changing the database may leave it
 * in an inconsistent state.
 *
 * <p>
 * All the save* methods act recursively on the entire object graph, replacing
 * placeholders and details where necessary, and then "merging" the final 
 * graph. This means that the objects that are passed into IUpdate.save* methods
 * are copied over to new instances which are then returned. The original 
 * objects <b>should be discarded</b>.  
 * </p>
 * 
 * <p> 
 * All methods throw {@link ome.conditions.ValidationException} if the input
 * objects do not pass validation, and 
 * {@link ome.conditions.OptimisticLockException} if the version of a given has
 * already been incremented. 
 * 
 * @author <br>
 *         Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de"> josh.moore@gmx.de</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $ $Date: $)
 *          </small>
 * @since OMERO3.0
 * @see ome.util.Validation
 * @see ome.logic.UpdateImpl
 * @see ome.model.internal.Details
 */
public interface IUpdate extends ServiceInterface {

    /** @see ome.api.IUpdate */ void saveCollection(Collection graph);
    /** @see ome.api.IUpdate */ void saveObject(IObject graph);
    /** @see ome.api.IUpdate */ void saveMap(Map graph);
    /** @see ome.api.IUpdate */ void saveArray(IObject[] graph);
 
    /** @see ome.api.IUpdate */ Collection  saveAndReturnCollection(Collection graph);
    /** @see ome.api.IUpdate */ Map         saveAndReturnMap(Map map);
    /** @see ome.api.IUpdate */ IObject     saveAndReturnObject(IObject graph);
    /** @see ome.api.IUpdate */ IObject[]   saveAndReturnArray(IObject[] graph);

    /** deletes a signle entity. Unlike the other IUpdate methods, deleteObject
     * does not propagate to related entites (e.g. foreign key relationships) 
     * and so calls to deleteObject must be properly ordered.
     *  
     * @param row an IObject to be deleted.
     * @throws ValidationException if the row is locked, has foreign key 
     *  constraints, or is otherwise marked un-deletable.
     */
    void deleteObject(IObject row) throws ValidationException;
    
}
