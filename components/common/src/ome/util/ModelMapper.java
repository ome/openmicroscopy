/*
 * ome.util.ModelMapper
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

package ome.util;

//Java imports
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

//Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//Application-internal dependencies
import ome.api.ModelBased;
import ome.api.OMEModel;


/** 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 1.0
 */
public abstract class ModelMapper extends ContextFilter {
	
	private static Log log = LogFactory.getLog(ModelMapper.class);
	
	protected abstract Map c2c();

	Map model2pojo = new HashMap();//FIXME not thread safe. rename 
	
	public ModelBased map (Filterable target){ // TODO take any object. just like filter()
		Filterable o = this.filter("MAPPING...",target);
		return (ModelBased) model2pojo.get(o);
	}
	
	public Object currentContext(){
		// TODO in ContextFilter filter out getContext(); and getCurrent!
		if (context.get() == null) newContext();
		LinkedList ll = (LinkedList) context.get();
		return ll.size()>0 ? ll.getLast() : null;
	}
	
	public Object filter(String fieldId, Object o) {
		Object result = super.filter(fieldId,o);
		doMapping(fieldId,result);
		return result;
	}
	
	public Map filter(String fieldId, Map m) {
		Map result = super.filter(fieldId,m);
		doMapping(fieldId,result);
		return result;		
	}

	public Collection filter(String fieldId, Collection c) {
		Collection result = super.filter(fieldId,c);
		doMapping(fieldId,result);
		return result;
	}
	
	public Filterable filter(String fieldId, Filterable input) {
		Filterable result = super.filter(fieldId,input);
		doMapping(fieldId,result);
		return result;
	}

	// TODO no longer need context !
	protected void doMapping(String fieldId, Object result){ 
		if (result instanceof Filterable) {
			Filterable current = (Filterable) result;
			ModelBased target = (ModelBased) findTarget(current);
			target.copy(((OMEModel)current),this);
		//FIXME need to unify Filterable and OMEModel (inheritance?)
		} else if (result instanceof Collection) {
			Collection current = (Collection) result;
			createCollection(current);
		} else if (result instanceof Map){
			Map current = (Map) result;
			createMap(current);
		}
	}
	
	public Object findTarget(Object current){
		// IMMUTABLES
		if (null == current |
				current instanceof Integer | 
				current instanceof String) // TODO can use findTarget then for these as well.
		{
			return current;
		} else 
		// Special cases TODO put into doFindTarget
		if (current instanceof Date){
			return new Timestamp(((Date)current).getTime());
		}
		
		Object target = model2pojo.get(current);
		if (null == target) {
			Class targetType = (Class) c2c().get(current.getClass());
			try {
				target = targetType.newInstance();
			} catch (Exception e) {
				throw new RuntimeException("Internal error: could not instantiate object of type "+targetType+" while trying to map "+current);
			}
			model2pojo.put(current,target);
		}
		return target;
	}
	
	public Timestamp date2timestamp(Date date){
		if (date==null) return null;
		return new Timestamp(date.getTime());
	}
	
	public int nullSafeInt(Integer id){
		if (id==null) return 0;
		return id.intValue();
	}
	
	public long nullSafeLong(Long id){
		if (id==null) return 0;
		return id.longValue();
	}
	
	public Collection createCollection(Collection c){
		if (c==null) return null;
		try { 
			Collection result = (Collection) c.getClass().newInstance();
			for (Iterator it = c.iterator(); it.hasNext();) {
				Object o = it.next();
				result.add(this.findTarget(o));
			}
			return result;
		} catch (InstantiationException ie){
			throw new RuntimeException(ie);
		} catch (IllegalAccessException iae){
			throw new RuntimeException(iae);
		}
	}
	
	public Map createMap(Map m){
		if (m==null) return null;
		try { 
			Map result = (Map) m.getClass().newInstance();
			for (Iterator it = m.keySet().iterator(); it.hasNext();) {
				Object o = it.next();
				result.put(findTarget(o),findTarget(m.get(o)));
			}
			return result;
		} catch (InstantiationException ie){
			throw new RuntimeException(ie);
		} catch (IllegalAccessException iae){
			throw new RuntimeException(iae);
		}
	}
	
}