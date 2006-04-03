/*
 * ome.model.ILink
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
package ome.model;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * interface for all mutable domain objects. Provides access to the version
 * property which the backend uses for optimistic locking. An object with an id
 * but without a version passed to the backend is considered an error, since
 * some backends will silently create a new object in the database. 
 * 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 *               <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 3.0
 * @author josh
 *
 */
public interface IMutable { // TODO extends IObject? fix mapping.vm then.
	
    /** optimistic-lock version. Usually managed by the backend. */
	public Integer getVersion();
    
    /** use with caution. In general, the version should only be altered by 
     * the backend. In the best case, an exception will be thrown for a version
     * not equal to the current DB value. In the worst (though rare) case, 
     * the new version could match the database, and override optimistic lock
     * checks that are supposed to prevent data loss. 
     * 
     * @param version Value for this objects version.
     */
    public void setVersion( Integer version );
	// TODO public Event getUpdateEvent();
	// TODO public void setUpdateEvent(Event e);
	
}
