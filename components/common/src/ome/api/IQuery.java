/*
 * ome.api.IQuery
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

//Java imports
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies


/** 
 * Provides methods for directly querying object graphs. 
 * 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 3.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 3.0
 */
public interface IQuery {
    
    // ~ Simple Queries
    Object getById(Class klazz, long id);
    List getByClass(Class klazz);
    
    // ~ Field-based Queries
	Object getUniqueByExample(Object example);
	List getListByExample(Object example);
	Object getUniqueByFieldILike(Class klazz, String field, String value);
	List getListByFieldILike(Class klazz, String field, String value);
	Object getUniqueByFieldEq(Class klazz, String field, Object value);
	List getListByFieldEq(Class klazz, String field, Object value);
	Object getUniqueByMap(Class klazz, Map constraints);
	List getListByMap(Class klazz, Map constraints);
	
    // ~ String-based Queries
    Object queryUnique(String query, Object[] params);
	List queryList(String query, Object[] params);
	Object queryUniqueMap(String query, Map params);
	List queryListMap(String query, Map params);
}
