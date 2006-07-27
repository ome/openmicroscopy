/*
 * ome.tools.hibernate.UpdateFilter
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

package ome.tools.hibernate;

// Java imports
import java.util.Collection;
import java.util.Date;
import java.util.Map;

// Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;

// Application-internal dependencies
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.model.IObject;
import ome.model.internal.Permissions;
import ome.util.ContextFilter;
import ome.util.Filterable;

/** responsible for correlating entity identities during multiple calls to 
 * merge. This occurs when {@link Collection collections} or arrays are passed
 * into the {@link ome.logic.UpdateImpl} save methods. 
 * 
 * @author  Josh Moore, josh.moore at gmx.de
 * @version $Revision$, $Date$
 * @see     ome.api.IUpdate
 * @see     ome.logic.UpdateImpl
 * @since   3.0-M3
 */
@RevisionDate("$Date$")
@RevisionNumber("$Revision$")
public class UpdateFilter extends ContextFilter 
{

    /** provides an external hook to unload all files which have already been
     * merged. 
     * <p>
     * Merging produces a copy of an entity, so that all old entities should
     * be considered stale. By unloading them, one is forcing the API user
     * to use the 
     * {@link ome.model.internal.GraphHolder#getReplacement() replacement}
     *  instead.
     *  </p>
     *  <p>
     *  The replacement is set by {@link MergeEventListener} and this is the 
     *  signal that that entity can be unloaded. Usually, this method is 
     *  invoked by {@link ome.logic.UpdateImpl} 
     *  </p>
     *
     * @see MergeEventListener
     * @see ome.logic.UpdateImpl
     * @see IObject#unload()
     */
    public void unloadReplacedObjects( )
    {
        for ( Object obj : _cache.keySet() )
        {
            if ( hasReplacement( obj ))
            {
                ((IObject) obj).unload();
            }
        }
    }
    
    /** overrides {@link ContextFilter#filter(String, Object)} to allow only
     * certain types to enter the Hibernate system
     */
    @Override
    public Object filter(String fieldId, Object o)
    {
        
        if ( alreadySeen( o ))
            return returnSeen( o );

        Object result;
            
        if (o == null) {
            return null;
        } else if (o instanceof Filterable) {
            result = filter(fieldId, (Filterable) o);
        } else if (o instanceof Collection) {
            result = filter(fieldId, (Collection) o);
        } else  if (o instanceof Map) {
            result = filter(fieldId, (Map) o);
        } else if (
                o instanceof Number
                || o instanceof String
                || o instanceof Date
                || o instanceof Boolean
                || o instanceof Permissions )
        { 
            result = o;
        } else {
            throw new RuntimeException(
                    "Update Filter cannot allow unknown types to be saved." +
                    o.getClass().getName()+" is not in {IObject,Collection,Map}"); 
        }
        return result;
    }
    
    /** overrides {@link ContextFilter#filter(String, Filterable)} to return
     * previously merged or previously checked items.
     */
    @Override
    public Filterable filter(String fieldId, Filterable f)
    {
        if (alreadySeen( f ))
            return (Filterable) returnSeen( f );
        
        return super.filter(fieldId, f);
        
    }

    /** overrides {@link ContextFilter#filter(String, Collection)} to return
     * previously checked {@link Collection collections}.
     */
    @Override
    public Collection filter(String fieldId, Collection c)
    {
        
        if ( alreadySeen( c ))
            return (Collection) returnSeen( c );
        
        return super.filter(fieldId, c);
        
    }

    // Helpers
    //  =======================================================

    protected boolean hasReplacement( Object o )
    {
        if ( o instanceof IObject)
        {
            IObject obj = (IObject)  o;
            if ( obj.getGraphHolder().getReplacement() != null )
                return true;
        }
        return false;
    }
    
    protected boolean alreadySeen( Object o )
    {
        if ( o == null ) return false;
        if ( ! Hibernate.isInitialized( o )) return true;
        return hasReplacement( o ) ? true : _cache.containsKey( o );
    }
    
    protected Object returnSeen( Object o )
    {
        if ( o == null) return null;
        if ( ! Hibernate.isInitialized( o )) return o;
        if ( hasReplacement( o ))
        {   
            IObject obj = (IObject) o;
            IObject replacement = obj.getGraphHolder().getReplacement();
            obj.unload();
            return replacement; 
        }
        return o;
    }
    
}
