/*
 * ome.services.util.CountCollector
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

package ome.services.util;

//Java imports
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import ome.model.IObject;
import ome.model.internal.Details;
import ome.tools.hibernate.ProxySafeFilter;


/** filter implementation which collects the ids of certain fields.  
 */
public class CountCollector extends ProxySafeFilter {
    
    protected String[] fields;
    protected Set[] ids;
    protected Map[] lookup;
    
    public CountCollector(String[] targetFields){
        
        if (targetFields == null) 
            throw new IllegalArgumentException("Expecting non null argument.");
        
        this.fields = new String[targetFields.length];
        System.arraycopy(targetFields,0,this.fields,0,targetFields.length);
        Arrays.sort(targetFields);
        
        ids = new Set[fields.length];
        lookup = new Map[fields.length];
        
    }

    public void collect(Object target) 
    {
        super.filter(null,target);
    }
    
    protected void addIfHit(String field){

        Object o = previousContext( 1 );
        
        if (o instanceof IObject) {

            IObject ctx = (IObject) o;
            
            int idx = Arrays.binarySearch(fields,field);
            if (idx >= 0) {
            
                Set<Long> s;
                if (ids[idx] == null) {
                    s = new HashSet<Long>();
                    ids[idx] = s;
                } else {
                    s = ids[idx];
                }
                s.add(ctx.getId());
                
                Map<Long,IObject> m;
                if (lookup[idx] == null) {
                    m = new HashMap<Long, IObject>();
                    lookup[idx] = m;
                } else {
                    m = lookup[idx];
                }
                m.put(ctx.getId(), ctx);
                
            }
        
        }
        
    }

    public Set<Long> getIds(String field)
    {
        int idx = Arrays.binarySearch(fields,field);
        if (idx < 0) 
            return null;
        
        return ids[idx];
        
    }
    
    public void addCounts(String field, Long id, Integer count) 
    {
        
        int idx = Arrays.binarySearch(fields,field);
        if (idx < 0) 
            return;

        if (count == null)
            return;
        
        Map<Long, IObject> l = lookup[idx];
        
        if ( l == null )
            return; 
        
        IObject obj = l.get(id);
        
        if (obj == null)
            return;
        
        if (obj.getDetails() == null)
            obj.setDetails(new Details());

        Map counts;
        if (obj.getDetails().getCounts() == null)
        {
            counts = new HashMap();
            obj.getDetails().setCounts(counts);
        } else {
            counts = obj.getDetails().getCounts();
        }
            
        Integer previous = (Integer) counts.get(field);
        if ( previous != null)
        {
            counts.put(field,new Integer(count.intValue()+previous.intValue()));
        } else {
            counts.put(field,count);
        }
        
        
    }

   @Override
    protected void beforeFilter(String fieldId, Object o)
    {
       super.beforeFilter( fieldId, o ); // Enter context;
       addIfHit( fieldId );
    } 

}