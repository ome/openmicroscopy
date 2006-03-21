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
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//Java imports

//Third-party libraries

//Application-internal dependencies


/** modified (hierarchical) visitor pattern. See 
 * http://c2.com/cgi/wiki?HierarchicalVisitorPattern for more information. 
 * (A better name may be "contextual visitor pattern" for graph traversing.) 
 * 
 * The modifications to Visitor make use of a model graph (here: the ome 
 * generated model classes) implementing Filterable. As documented in 
 * Filterable, model objects are responsible for calling 
 * <code>filter.filter(someField)</code> for all fields <b>and setting 
 * the value of that field to the return value of the method call</b>. 
 * 
 *  The Filter itself is responsible for returning a compatible object and 
 *  (optionally) stepping into objects and keeping up with context.  
 * 
 * Note: This class is not thread-safe.
 * 
 * template method to filter domain objects.
 * The standard idiom is: 
 * <code>
 *  if (m != null && hasntSeen(m)){
 *      enter(m); // Provides context
 *      addSeen(m); // Prevents looping
 *      m.acceptFilter(this); // Visits all fields
 *      exit(m); // Remove from context
 *  }
 * 
 * </code> 

 * 
 * Implementation notes:
 * - nulls are already "seen"  
 */
public class ContextFilter implements Filter {

	private static Log log = LogFactory.getLog(ContextFilter.class);

    private Object dummy;
    
    protected Map _cache = new IdentityHashMap();
    
    protected LinkedList _context = new LinkedList();

    protected void beforeFilter( String fieldId, Object o )
    {
        enter( o );
        addSeen( o );
    }

    protected void doFilter( String fieldId, Object o )
    {
        // nothing 
    }
    
    protected void doFilter( String fieldId, Filterable f )
    {
        f.acceptFilter( this );
    }

    protected void doFilter( String fieldId, Collection c )
    {
        List add = new ArrayList();
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
        c.addAll(add);
    }
    
    protected void afterFilter( String fieldId, Object o )
    {
        exit( o );
    }
    
	public Filterable filter(String fieldId, Filterable f) {
		
		if (f != null && hasntSeen(f)){
            beforeFilter( fieldId, f );
			doFilter( fieldId, f );
			afterFilter( fieldId, f );
		} 
		
		return f;// null;
		
	}
    
	/** iterates over the contents of the collection and filters each.
	 * Adds itself to the context. This is somewhat dangerous.
	 */
	public Collection filter(String fieldId, Collection c){
		
		if (c != null && hasntSeen(c)){
            beforeFilter( fieldId, c );
            doFilter( fieldId, c );
            afterFilter( fieldId, c );
		} 
		
		return c;
	}
	
	/** filters both the key and value sets of the map.
	 * Adds itself to the context. Somewhat dangerous.
	 */
	public Map filter(String fieldId, Map m){
		
        if (m != null && hasntSeen(m)){
            beforeFilter( fieldId, m );
            Map add = new HashMap();
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
			m.putAll(add);
            afterFilter( fieldId, m );

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
            beforeFilter( fieldId, o );
            doFilter( fieldId, o );
            afterFilter( fieldId, o );
			result = o;
		}
		return result;
	}
	
	/** doesn't return a new entry. only changes key and value */
	protected Entry filter(String fieldId, Entry entry){

        if (entry != null && hasntSeen(entry)){
			addSeen(entry);
			enter(entry);
			Object key = filter(fieldId, entry.key);
			Object value = filter(fieldId, entry.value);
			exit(entry);

			entry.key=key;
			entry.value=value;
			
		} 
		return entry;
	}

    // ~ CONTEXT METHODS
    // =========================================================================
    
	public boolean enter(Object o) {
		push(o);
		return true;
	}

	public boolean exit(Object o) {
		pop(o);
		return true;
	}

    public Object currentContext(){
        return _context.size()>0 ? _context.getLast() : null;
    }
    
    public Object previousContext(int index)
    {
        if (index < 0 || index >= _context.size()) return null;
        return _context.get(_context.size() - index - 1);
    }
    
	protected void push(Object o){
		_context.addLast(o);
	}
	
	// beware: context is being changed during filtering. Eek! FIXME
	protected void pop(Object o){
		Object last = _context.removeLast();
		if (o != last){
			throw new IllegalStateException(
                    "Context is invalid. Trying to remove Object "+o+
                    " and removed Object "+last);
		}
	}

	protected void addSeen(Object o){
		_cache.put(o,dummy);
	}
	
	protected boolean hasntSeen(Object o) {
		return ! _cache.containsKey(o);
	}

    /*
     * simple Entry type for dealing with maps.
     */
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

	
}
