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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.orm.hibernate3.HibernateTemplate;

// Third-party libraries

// Application-internal dependencies
import ome.api.IQuery;
import ome.model.IObject;
import ome.model.internal.Details;
import ome.model.meta.Event;
import ome.model.meta.EventDiff;
import ome.model.meta.EventLog;
import ome.security.CurrentDetails;
import ome.tools.lsid.LsidUtils;
import ome.util.ContextFilter;
import ome.util.Filter;
import ome.util.Filterable;
import ome.util.Validation;

/**
 * enforces the detached-graph re-attachment "Commandments" as outlined in TODO.
 * Objects that are transient (no ID) are given details from the CurrentDetails.
 * Objects that are managed (with ID) are checked for validity. (TODO implement)
 * Collections that ...
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 1.0
 */
public class UpdateFilter extends ContextFilter 
{

    protected HibernateTemplate ht;

    private UpdateFilter(){} // We need the template
    
    public UpdateFilter(HibernateTemplate template)
    {
        this.ht = template;
    }

    @Override
    public Object filter(String fieldId, Object o)
    {
        Object result;
        if (o == null) {
            return null;
        } else if (o instanceof IObject) {
            result = filter(fieldId, (Filterable) o);
        } else if (o instanceof Collection) {
            result = filter(fieldId, (Collection) o);
        } else  if (o instanceof Map) {
            result = filter(fieldId, (Map) o);
        } else if (
                o instanceof Details
                || o instanceof Number
                || o instanceof String
                || o instanceof Date
                || o instanceof Boolean)
        { 
            result = o;
        } else {
            throw new RuntimeException(
                    "Update Filter cannot allow unknown types to be saved." +
                    o.getClass().getName()+" is not in {IObject,Collection,Map}"); 
        }
        return result;
    }
    
    @Override
    public Filterable filter(String fieldId, Filterable f)
    {
        Filterable result = f; // Don't reuse f
        if (result instanceof IObject)
        {
            IObject obj = (IObject) result;
            switch (getEntityState(obj)) // can't be null
            {
                case UNLOADED:
                    result = loadUnloadedEntity(fieldId,obj);
                    break;
                case TRANSIENT:
                    transferDetails(obj);
                    result = super.filter(fieldId,obj);
                    break;
                case MANAGED:
                    reloadDetails(obj);
                    result = super.filter(fieldId,obj);
                default:
                    break;
            }

            // FOR ALL OBJECTS now that in session!
            // First need to check that we won't validate internal objects.
            Validation v = Validation.VALID(); // FIXME result.validate();
            if (!v.isValid())
                throw new RuntimeException(v.toString()); // TODO validation exception
            
            /*
             * Need to check if it's a hibernate proxy and NOT walk it,
             * but otherwise validate! 
             */
            
//            // TODO DELETE PERHAPS?
//            if (!(currentContext() instanceof Details) 
//                    && (obj instanceof Event 
//                    || obj instanceof EventLog 
//                    || obj instanceof EventDiff))
//                throw new RuntimeException(
//                        "Events can only be managed through the Details object"); 

        }

        return result;
        
    }

    @Override
    public Collection filter(String fieldId, Collection c)
    {
        Collection result = c; // Don't reuse c
        Object o = this.currentContext();
        if (o instanceof IObject) 
        {
            IObject ctx = (IObject) o;
            switch (getCollectionState(ctx, fieldId, result))
            {
                case MANAGED:   // Don't need to load.
                case TRANSIENT: // Can't load.
                    result = super.filter(fieldId, result);
                    break;
                case NULL:
                case FILTERED:
                    // TODO possible check for NEW items
                    result = collectionIsUnloaded(fieldId, ctx);
                default:
                    break;
            }
        } 
        else
        {
            throw new RuntimeException("Not handled yet: nonIObject context");
        }
        
        
        return result;
        
    }

    // State Detection
    // ===================================================

    public enum EntityState
    {
        MANAGED, TRANSIENT, UNLOADED
    };

    public EntityState getEntityState(IObject obj)
    {
        if (obj.getId() == null) return EntityState.TRANSIENT;
        if (!obj.isLoaded()) return EntityState.UNLOADED;
        return EntityState.MANAGED;
    }

    public enum CollectionState
    {
        NULL, MANAGED, TRANSIENT, FILTERED
    };

    public CollectionState getCollectionState(IObject ctx, String fieldId,
            Collection c)
    {
        if (ctx == null) 
            return CollectionState.TRANSIENT;

        switch (getEntityState(ctx))
        {
            case TRANSIENT:
            case UNLOADED:
                return CollectionState.TRANSIENT;
            default:
                break;
        }
        
        if (ctx.getDetails().isFiltered(fieldId))
            return CollectionState.FILTERED;

        if (c == null) 
            return CollectionState.NULL;
        
        return CollectionState.MANAGED;
    }

    // Actions
    // ====================================================

    protected Filterable loadUnloadedEntity(String fieldId, IObject obj)
    {
        if ( obj != null && obj.getId() != null)
        {
           return loadFromUnloaded(obj); 
        }
        
        else if (currentContext() instanceof IObject)
        {
            IObject ctx = (IObject) currentContext();
            EntityState ctxState = getEntityState(ctx);
            if (!EntityState.MANAGED.equals(ctxState))
                throw new IllegalStateException(
                        "UNLOADED entity cannot be found "
                                + "in an non-MANAGED entity!");

            // EARLY EXIT!
            return (IObject) loadFromEntityField(ctx, fieldId);

        } 
        
        else  
        {
            // TODO: InvalidException?
            throw new IllegalStateException(
                "Impossible to load an entity from a collection without an id."
            ); 
        }
    }
    
    protected void saveTransientEntity(IObject entity)
    {
        //ht.save(entity);
    }
    
    protected IObject mergeDetachedEntity(IObject entity)
    {
        return entity;//(IObject) ht.merge(entity);
    }

    private Collection collectionIsUnloaded(String fieldId, IObject ctx)
    {
        Collection current = (Collection) loadFromEntityField(ctx, fieldId);
        
        if (current == null)
            return null; // EARLY EXIT
        
        Collection copied = makeNew(current);
        for (Iterator it = current.iterator(); it.hasNext();)
        {
            Object o = (Object) it.next();
            copied.add(o); // TODO needed?
        }
        return copied;
    }
    
    protected void transferDetails(IObject m)
    {
        Details template = CurrentDetails.createDetails();
        copyAllowedDetails(template,m.getDetails());
        m.setDetails(template);

        /*
         * FIXME check for valid type creation i.e. no creating types, users,
         * etc.
         */
    }
    
    protected void reloadDetails(IObject m)
    {
        IObject obj = (IObject) ht.load(m.getClass(),m.getId());
        Details template = obj.getDetails();
        copyAllowedDetails(template, m.getDetails());
        m.setDetails(template);
            
        /* TODO what else should be preserved?
         * should be able to switch group if member, e.g. */
        
    }

    private void copyAllowedDetails(Details template, Details target)
    {
        // TODO is this natural? perhaps permissions don't belong in details
        // details are the only thing that users can change the rest is
        // read only...

        if (target != null)
        {
            if (target.getPermissions() != null)
                template.setPermissions(target.getPermissions());

            for (Iterator it = target.filteredSet().iterator(); it.hasNext();)
            {
                String fieldName = (String) it.next();
                template.addFiltered(fieldName);

                // Don't need to keep up with Filtered for new objects; 
                // can't be filtered. Too complicated?

            }
            
        }
        
    }

    // Loading
    // =======================================================
    /** used to load the context object and then find its named field.
     * Should only be called with detached or managed objects (id needed).
     */
    protected Object loadFromEntityField(IObject ctx, String fieldId)
    {

// TODO test       IObject context = (IObject) ht.find(
//                " select target from "+ctx.getClass().getName()+
//                " target left outer join fetch target."+
//                LsidUtils.parseField(fieldId)+
//                " where target.id = ?",ctx.getId()).get(0);//TODO error checking.
        IObject context = (IObject) ht.load(ctx.getClass(),ctx.getId());
        return context.retrieve(fieldId);
    }

    protected IObject loadFromUnloaded(IObject ctx)
    {
        IObject result = (IObject) ht.load(ctx.getClass(),ctx.getId());
        return result;
    }
    
    // Helpers
    //  =======================================================
    /**
     * @param example not null collection
     */
    private Collection makeNew(Collection example)
    {
        if (example instanceof Set)
        {
            return new HashSet(example.size());
        } else if (example instanceof List)
        {
            return new ArrayList(example.size());
        }
        
        throw new RuntimeException("Unknown collection type:"+example.getClass());
        
    }
    
    

}
