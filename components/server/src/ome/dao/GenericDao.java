/*
 * org.openmicroscopy.omero.logic.GenericDao
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

package ome.dao;

import java.util.List;
import java.util.Map;

//Java imports

//Third-party libraries

//Application-internal dependencies


/** data access object for basic objects.
 * 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 1.0
 */
public interface GenericDao {
    
	public Object getUniqueByExample(Object example);
	public List getListByExample(Object example);
	public Object getByName(Class klazz, String name);
	public Object getById(Class klazz, int id);
	public void persist(Object[] objects);
	@Deprecated
	public Object queryUnique(String query, Object[] params);
	@Deprecated
	public List queryList(String query, Object[] params);
	
	public Object getUniqueByMap(Class klazz, Map constraints);
	public List getListByMap(Class klazz, Map constraints);
}