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
    
	public Object getUniqueByExample(Object example);
	public List getListByExample(Object example);
	public Object getUniqueByFieldILike(Class klazz, String field, String value);
	public List getListByFieldILike(Class klazz, String field, String value);
	public Object getUniqueByFieldEq(Class klazz, String field, Object value);
	public List getListByFieldEq(Class klazz, String field, Object value);
	public Object getById(Class klazz, long id);
	public void persist(Object[] objects);
	public Object getUniqueByMap(Class klazz, Map constraints);
	public List getListByMap(Class klazz, Map constraints);
	public Object queryUnique(String query, Object[] params);
	public List queryList(String query, Object[] params);
	public Object queryUniqueMap(String query, Map params);
	public List queryListMap(String query, Map params);
}
