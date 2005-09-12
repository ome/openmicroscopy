/*
 * ome.util.ContextFilter
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//Java imports

//Third-party libraries

//Application-internal dependencies


/** modified (hierarchical) visitor pattern. See http://c2.com/cgi/wiki?HierarchicalVisitorPattern
 * for more information. (A better name may be "contextual visitor pattern" for graph traversing.) 
 * 
 * The modifications to Visitor make use of a model graph (here: the ome generated model classes)
 * implementing Filterable. As documented in Filterable, model objects are responsible for calling 
 * <code>filter.filter(someField)</code> for all fields <b>and setting the value of that field to the
 * return value of the method call</b>. 
 * 
 *  The Filter itself is responsible for returning a compatible object and (optionally) stepping into objects and keeping up with context.  
 * 
 * Implementation notes:
 * - nulls are already "seen"  
 */
public class ContextFilter implements Filter {

	private static Log log = LogFactory.getLog(ContextFilter.class);
	
	protected ThreadLocal cache = new ThreadLocal();
	protected ThreadLocal context = new ThreadLocal();
	
	class Entry {
		Object key;
		Object value;
		public String toString(){
			return "("+key+":"+value+")";
		}
		Entry(Object key, Object value){
			this.key = key;
			this.value = value;
		}
		
	}
	
	/** 
	 * template method to filter domain objects.
	 * The standard idiom is: 
	 * <code>
	 *   if (hasntSeen(m)){
	 *     enter(m); // Provides context
	 *     seen(m); // Prevents looping
	 *     m.acceptFilter(this); // Visits all fields
	 *     exit(m);
	 *   }
	 *     
	 * </code> 
	 */
	public Filterable filter(String fieldId, Filterable f) {
		//log.info("Filtering Model "+f);
		
		if (hasntSeen(f)){
			//log.info("Haven't seen. Stepping into "+f);
			enter(f);
			seen(f);
			f.acceptFilter(this);
			exit(f);
		} 
		
		return f;// null;
		
	}
	
	/** iterates over the contents of the collection and filters each.
	 * Adds itself to the context. This is somewhat dangerous.
	 */
	public Collection filter(String fieldId, Collection c){
		
		//log.info("Filtering collection "+c);
		
		List add = new ArrayList();
		
		if (hasntSeen(c)){
			//log.info("Haven't seen. Stepping into "+c);
			seen(c);
			enter(c);
			for (Iterator iter = c.iterator(); iter.hasNext();) {
				Object item = iter.next();
				Object result = filter(fieldId, item);
				if (null == result){
					iter.remove();
				} else if (result != item) {
					iter.remove();
					add.add(result);
				}
			}
			exit(c);
			c.addAll(add);
		} 
		
		return c;
	}
	
	/** filters both the key and value sets of the map.
	 * Adds itself to the context. Somewhat dangerous.
	 */
	public Map filter(String fieldId, Map m){
		//log.info("Filter "+m);
		
		Map add = new HashMap();
		
		if (hasntSeen(m)){
			//log.info("Haven't seen. Stepping into "+m);
			seen(m);
			enter(m);
			for (Iterator iter = m.entrySet().iterator(); iter.hasNext();) {
				Map.Entry _entry = (Map.Entry) iter.next();
				Entry entry = new Entry(_entry.getKey(),_entry.getValue());
				Entry result = filter(fieldId, entry);
				if (null == result){
					iter.remove();
				} else if (result.key != entry.key||result.value!=entry.value){
					iter.remove();
					add.put(result.key,result.value);
				} 
			}
			exit(m);
			m.putAll(add);

		} 
		return m;
	}

	/** used when type is unknown. this is possibly omittable with generics */
	public Object filter(String fieldId, Object o){
		Object result;
		if (o instanceof Filterable) {
			result = filter(fieldId, (Filterable) o);
		} else if (o instanceof Collection) {
			result = filter(fieldId, (Collection) o);
		} else  if (o instanceof Map) {
			result = filter(fieldId, (Map) o);
		} else {
			result = o;
		}
		return result;
	}
	
	/** doesn't return a new entry. only changes key and value */
	protected Entry filter(String fieldId, Entry entry){
		//log.info("Filter "+entry);
		
		if (hasntSeen(entry)){
			//log.info("Stepping into "+entry);
			seen(entry);
			enter(entry);
			Object key = filter(fieldId, entry.key);
			Object value = filter(fieldId, entry.value);
			exit(entry);

			entry.key=key;
			entry.value=value;
			
		} 
		return entry;
		
	}
	
	public boolean enter(Object o) {
		push(o);
		//log.info("Context In:"+(LinkedList)context.get());
		return true;
	}

	public boolean exit(Object o) {
		pop(o);
		//log.info("Context Out:"+(LinkedList)context.get());
		return true;
	}

	void newContext(){
		context.set(new LinkedList());
	}

	void newCache(){
		Set set = new HashSet();
		set.add(null); 
		
		cache.set(set);
	}
	
	void push(Object o){
		if (context.get()==null) newContext();
		LinkedList l = (LinkedList) context.get();
		l.addLast(o);
	}
	
	// beware: context is being changed during filtering. Eek! FIXME
	void pop(Object o){
		if (context.get()==null) newContext();
		LinkedList l = (LinkedList) context.get();
		Object last = l.removeLast();
		if (o != last){
			throw new IllegalStateException("Context is invalid. Trying to remove Object "+o+" and removed Object "+last);
		}
	}

	void seen(Object o){
		if (cache.get()==null) newCache();
		( (Set) cache.get()).add(o);
	}
	
	boolean hasntSeen(Object o) {
		if (cache.get()==null)newCache();
		return ! ((Set) cache.get()).contains(o);
	}
	
	
}
