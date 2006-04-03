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
 *  extension of {@link ome.model.IObject} for building object hierarchies.
 *  ILink represents a many-to-many relationship between two classes that
 *  take part in a containment relationship. 
 *   
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 *               <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 3.0
 * @see ome.model.core.Image
 * @see ome.model.containers.Dataset
 * @see ome.model.containers.Project
 * @see ome.model.containers.Category
 * @see ome.model.containers.CategoryGroup
 */
public interface ILink extends IObject{
    
    
	public IObject getParent();
    public void setParent(IObject parent);
    
    public IObject getChild();
    public void setChild(IObject child);
    
}
    
