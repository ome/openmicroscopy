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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.springframework.orm.hibernate3.HibernateOperations;

// Application-internal dependencies
import ome.conditions.SecurityViolation;
import ome.conditions.ValidationException;
import ome.model.IMutable;
import ome.model.IObject;
import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.security.CurrentDetails;
import ome.util.ContextFilter;
import ome.util.Filterable;
import ome.util.Utils;
import ome.util.Validation;

/**
 * enforces the detached-graph re-attachment "Commandments" as outlined in TODO.
 * Objects that are transient (no ID) are unchanged; objects that are managed 
 * (with ID) are checked for validity (i.e. must have a version); and 
 * unloaded/filtered objects & collections are re-filled.
 * 
 * Various other actions are taken in {@link ome.tools.hibernate.GlobalListener}
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 1.0
 */
public class UpdateFilter extends ContextFilter 
{

    private static Log log = LogFactory.getLog(UpdateFilter.class);
    
    protected HibernateOperations hibernateOps;

    private UpdateFilter(){} // We need the template
    
    public UpdateFilter(HibernateOperations hibernateOperations)
    {
        this.hibernateOps = hibernateOperations;
    }
    
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
     * @see UpdateImpl
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
    
    @Override
    public Filterable filter(String fieldId, Filterable f)
    {
        if (alreadySeen( f ))
            return (Filterable) returnSeen( f );
        
        Filterable result = f; // Don't reuse f
        
        if ( result instanceof Details )
        {
            result = super.filter( fieldId, result );
            // TODO any other clean up? "replacement", etc. 
        }
        else if ( result instanceof IObject )
        {
            IObject obj = (IObject) result;
            switch ( getEntityState(obj) ) // can't be null
            {
                case UNLOADED:  
                    result = loadUnloadedEntity( fieldId, obj ); 
                    break;
                case TRANSIENT:
                    transferDetails( obj );
                    result = super.filter( fieldId, obj );  
                    break;
                case MANAGED:
                    checkManagedState( obj );
                    result = super.filter( fieldId, obj );
                    break;
                default:
                    throw new RuntimeException("Unkown state:"+getEntityState(obj));
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
        
        if ( alreadySeen( c ))
            return (Collection) returnSeen( c );
        
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
        
        if (ctx.getDetails() != null) { /* FIXME should NOT be null */
            if ( ctx.getDetails().isFiltered(fieldId) )
                return CollectionState.FILTERED;
        } else {
            log.warn( "Details null for: " + ctx);
        }
        
        if (c == null) 
            return CollectionState.NULL;
        
        return CollectionState.MANAGED;
    }

    // Actions
    // ====================================================


    /*
     * FIXME check for valid type creation i.e. no creating types, users,
     * etc.
     */

    // TODO is this natural? perhaps permissions don't belong in details
    // details are the only thing that users can change the rest is
    // read only...
    /** fills in security details ({@link IObject#getDetails()}) for all 
     * transient entities. Non-privileged users can only edit the 
     * {@link Details#getPermissions() Permissions} field. Privileged users
     * can use the {@link Details} object as a single-step <code>chmod</code>
     * and <code>chgrp</code>. 
     */ 
    protected void transferDetails( IObject obj )
    {
        Details source = obj.getDetails();
        Details currentDetails = CurrentDetails.createDetails();
        Experimenter user = currentDetails.getOwner();
        ExperimenterGroup group = currentDetails.getGroup();
        
        if ( source != null )
        {
            // TODO everyone is allowed to set the umask if desired.
            if (source.getPermissions() != null)
            {
                currentDetails.setPermissions( source.getPermissions() );
            }
            
            // users *aren't* allowed to set the owner/group of an item.
            if (source.getOwner() != null 
                    && ! source.getOwner().getId().equals( 
                            currentDetails.getOwner().getId() ))
            {
                // but this is root
                if ( user.getId().equals( 0L ))
                {
                    currentDetails.setOwner( source.getOwner() );
                } else {
                    throw new SecurityViolation(String.format(
                        "You are not authorized to set the ExperimenterID" +
                        " for %s to %d", obj, source.getOwner().getId() 
                        ));
                }
                
            }

            // users *argen't allowed to set the owner/group of an item
            if (source.getGroup() != null 
                    && ! source.getGroup().getId().equals( 
                            currentDetails.getGroup().getId() ))
            {
                
                // but this is root
                if ( user.getId().equals( 0L ))
                {
                    currentDetails.setGroup( source.getGroup() );
                } else {
                    throw new SecurityViolation(String.format(
                        "You are not authorized to set the ExperimenterGroupID"+
                        " for %s to %d", obj, source.getGroup().getId() 
                        ));
                }
            }
            
        }

        obj.setDetails( currentDetails );
        
    }

    /* TODO what else should be preserved?
     * TODO should move to @Validation.
     * should be able to switch group if member, e.g. */
    /** checks that a non-privileged user has not attempted to edit the 
     * entity's {@link IObject#getDetails() security details}. Privileged 
     * users can set fields on {@link Details} as a single-step 
     * <code>chmod</code> and <code>chgrp</code>.
     */
    protected void checkManagedState( IObject obj )
    {
        
        Details currentDetails = CurrentDetails.createDetails();
        Experimenter user = currentDetails.getOwner();
        ExperimenterGroup group = currentDetails.getGroup();
        
        if ( obj.getId() == null)
            throw new ValidationException(
                    "Id required on all detached instances.");

        // Throws an exception if does not exist
        IObject original = (IObject) hibernateOps.load( 
                    Utils.trueClass( obj.getClass() ), 
                    obj.getId() );
        
        if ( obj == original )
            return; // Early exit. Obj is in session.
        
        Details oldDetails = original.getDetails(); 
        Details updatedDetails = obj.getDetails();
        
        // This happens if all fields of details are null.
        if ( oldDetails == null ) 
        {
            if ( obj.getDetails() != null )
            {
                obj.setDetails( null );
                if ( log.isDebugEnabled() )
                {
                    log.debug("Setting details on "+
                            obj+" to null like original");
                }
            }
        }

        // Probably common since users don't worry about this information.
        else if ( updatedDetails == null )
        {
            obj.setDetails( new Details( oldDetails ) );
            if ( log.isDebugEnabled() )
            {
                log.debug("Setting details on "+
                        obj+" to copy of original details.");
            }
            
        // Now we have to make sure certain things do not happen:
        } else {
            
            if ( ! idEqual( 
                    oldDetails.getOwner(), 
                    updatedDetails.getOwner() ))
            {
                
                if ( user.getId().equals( 0L ))
                {
                    // even root can't set them to null.
                    if ( updatedDetails.getOwner() == null )
                    {
                        updatedDetails.setOwner( oldDetails.getOwner() );
                    }
                }
                // everyone else can't change them at all.
                else 
                {
                    throw new SecurityViolation(String.format(
                        "Owner id changed for %s", obj 
                        ));
                }
            }

            if ( ! idEqual( 
                    oldDetails.getGroup(), 
                    updatedDetails.getGroup() ))
            {
                
                if ( user.getId().equals( 0L ))
                {
                    // even root can't set them to null.
                    if ( updatedDetails.getGroup() == null )
                    {
                        updatedDetails.setGroup( oldDetails.getGroup() );
                    }
                }
                // everyone else can't change them at all.
                else                     
                {
                    throw new SecurityViolation(String.format(
                        "Group id changed for %s", obj 
                        ));
                }
            }
            
            // TODO should just be immutable even for root.
            if ( ! idEqual( 
                    oldDetails.getCreationEvent(), 
                    updatedDetails.getCreationEvent()))
            {
                if ( user.getId().equals( 0L ))
                {
                    // even root can't set them to null.
                    if ( updatedDetails.getCreationEvent() == null )
                    {
                        updatedDetails.setCreationEvent( 
                                oldDetails.getCreationEvent() );
                    }
                }
                // everyone else can't change them at all.
                else                 {
                    throw new SecurityViolation(String.format(
                        "Creation event changed for %s", obj 
                        ));
                }
            }
            
        }
        
        if ( obj instanceof IMutable )
        {
            Integer version = ((IMutable) obj).getVersion();
            if ( version == null || version.intValue() < 0 );
//                throw new ValidationException(
//                        "Version must properly be set on managed objects :\n"+
//                        obj.toString()
//                        );
                //TODO
        }
            
    }
    
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
 
    // Loading
    // =======================================================
    /** used to load the context object and then find its named field.
     * Should only be called with detached or managed objects (id needed).
     */
    protected Object loadFromEntityField(IObject ctx, String fieldId)
    {
        IObject context = (IObject) hibernateOps.load( 
                Utils.trueClass( ctx.getClass() ),
                ctx.getId());
        return context.retrieve( fieldId );
    }

    protected IObject loadFromUnloaded(IObject ctx)
    {
        IObject result = (IObject) hibernateOps.load(ctx.getClass(),ctx.getId());
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

   
    protected boolean idEqual( IObject arg1, IObject arg2 )
    {
        if ( arg1 == null || arg1.getId() == null )
            return false;
        
        else if ( arg2 == null || arg2.getId() == null )
            return false;
        
        else
            return arg1.getId().equals( arg2.getId() );
        
    }
    
}
