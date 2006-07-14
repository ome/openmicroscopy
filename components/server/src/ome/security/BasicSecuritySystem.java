/*
 * ome.security.BasicSecuritySystem
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

package ome.security;

//Java imports

//Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//Application-internal dependencies
import ome.api.IAdmin;
import ome.api.IQuery;
import ome.api.ITypes;
import ome.conditions.ApiUsageException;
import ome.conditions.InternalException;
import ome.conditions.SecurityViolation;
import ome.conditions.ValidationException;
import ome.model.IEnum;
import ome.model.IMutable;
import ome.model.IObject;
import ome.model.enums.EventType;
import ome.model.internal.Details;
import ome.model.meta.Event;
import ome.model.meta.EventDiff;
import ome.model.meta.EventLog;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.system.EventContext;
import ome.system.ServiceFactory;
import ome.util.Utils;

/** 
 * simplest implementation of {@link SecuritySystem}. Uses an ctor-injected 
 * {@link EventContext} and the {@link ThreadLocal ThreadLocal-}based 
 * {@link CurrentDetails} to provide the security infrastructure.
 * 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 1.0
 */
public class BasicSecuritySystem implements SecuritySystem
{
	private final static Log log = LogFactory.getLog(BasicSecuritySystem.class);
	
	private ServiceFactory sf;
	
	private EventContext ec;
	
	public BasicSecuritySystem( 
			ServiceFactory factory, 
			EventContext eventContext )
	{
		this.sf = factory;
		this.ec = eventContext;
	}
	
	public boolean isReady( )
	{
		// TODO could check for open session.
		if ( currentUser() != null && currentGroup() != null 
				&& currentEvent() != null )
		{
			return true;
		}
		return false;
	}
	
	/** classes without owners and events */
	public boolean isGlobal( Class<? extends IObject> klass)
	{
		if ( klass == null ) return false;
		if ( Experimenter.class.isAssignableFrom( klass )) return true;
		if ( Event.class.isAssignableFrom( klass )) return true;
		return false;
	}
	
	public boolean isSystemType( Class<? extends IObject> klass )
	{
		if ( klass == null ) return false;
		if ( Experimenter.class.isAssignableFrom( klass )) return true;
		if ( Event.class.isAssignableFrom( klass )) return true;
		if ( EventLog.class.isAssignableFrom( klass )) return true;
		if ( EventDiff.class.isAssignableFrom( klass )) return true;
		if ( IEnum.class.isAssignableFrom( klass )) return true;
		return false;
	}
	
	public boolean isPrivileged( IObject obj )
	{
		if ( obj instanceof EventLog )
		{
			return true;
		}
		return false;
	}
	
	public void setDetails( IObject iObject )
	{
		if (iObject == null) return;
	}
	
	public boolean canUpdate( IObject iObject )
	{
		return false;
	}
	
	// ~ Group definitions
	// =========================================================================
	// This information is also encoded at:
	// TODO
	//
	public boolean isSystemGroup( ExperimenterGroup group )
	{
		return false;
	}
	
	public String getSystemGroupName( )
	{
		return "system";
	}

	public String getUserGroupName( )
	{
		return "user";
	}
	
	// ~ Details (for UpdateFilter)
	// =========================================================================
	   /*
     * FIXME check for valid type creation i.e. no creating types, users,
     * etc.
     */

    // TODO is this natural? perhaps permissions don't belong in details
    // details are the only thing that users can change the rest is
    // read only...
    /** creates a new secure {@link IObject#getDetails() details} for 
     * transient entities. Non-privileged users can only edit the 
     * {@link Details#getPermissions() Permissions} field. Privileged users
     * can use the {@link Details} object as a single-step <code>chmod</code>
     * and <code>chgrp</code>. 
     * 
     * {@link #transientDetails(IObject) transientDetails} 
     * always returns a non-null Details.
     */ 
    public Details transientDetails( IObject obj )
    {
    	
    	checkReady("transientDetails");
    	
    	if ( isPrivileged( obj ) ) return obj.getDetails(); // EARLY EXIT
    	
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

        return currentDetails;

    }

    /* TODO what else should be preserved?
     * TODO should move to @Validation.
     * should be able to switch group if member, e.g. */
    /** checks that a non-privileged user has not attempted to edit the 
     * entity's {@link IObject#getDetails() security details}. Privileged 
     * users can set fields on {@link Details} as a single-step 
     * <code>chmod</code> and <code>chgrp</code>.
     * 
     * {@link #checkManagedState(IObject, String[], Object[], Object[]) checkManagedState}
     * may alter the given entity and state. If so, it will return true. 
     * Otherwise false.
     */
    public Details managedDetails( IObject iobj, Details previousDetails )
    {
    	checkReady("managedDetails");
    	
        if ( iobj.getId() == null)
            throw new ValidationException(
                    "Id required on all detached instances.");

    	if ( isPrivileged( iobj )) return null; // EARLY EXIT
    	
    	boolean altered = false;

    	Details currentDetails = iobj.getDetails();
        Details newDetails = CurrentDetails.createDetails();
        Experimenter user = newDetails.getOwner();
        
        // This happens if all fields of details are null.
        if ( previousDetails == null ) 
        {
            if ( currentDetails != null )
            {
                newDetails = null; // FIXME FIXME this means no change!!
                altered = true;
                if ( log.isDebugEnabled() )
                {
                    log.debug("Setting details on "+
                            iobj+" to null like original");
                }
            }
        }

        // Probably common since users don't worry about this information.
        else if ( currentDetails == null )
        {
        	newDetails = new Details( previousDetails );
            altered = true;
            if ( log.isDebugEnabled() )
            {
                log.debug("Setting details on "+
                        iobj+" to copy of original details.");
            }
            
        // Now we have to make sure certain things do not happen:
        } else {
            
            if ( ! isGlobal( iobj.getClass() ) // implies that owner doesn't matter 
            		&& ! idEqual( 
                    previousDetails.getOwner(), 
                    currentDetails.getOwner() ))
            {
                
                if ( user.getId().equals( 0L ))
                {
                    // even root can't set them to null.
                    if ( currentDetails.getOwner() == null )
                    {
                        newDetails.setOwner( previousDetails.getOwner() );
                        altered = true;
                    }
                }
                // everyone else can't change them at all.
                else 
                {
                    throw new SecurityViolation(String.format(
                        "You are not authorized to change " +
                        "the owner for %s from %s to %s",
                          iobj, 
                          previousDetails.getOwner(), 
                          currentDetails.getOwner()
                        ));
                }
            }

            if ( ! isGlobal( iobj.getClass() ) // implies that group doesn't matter
            		&& ! idEqual( 
                    previousDetails.getGroup(), 
                    currentDetails.getGroup() ))
            {
                
                if ( user.getId().equals( 0L ))
                {
                    // even root can't set them to null.
                    if ( currentDetails.getGroup() == null )
                    {
                        newDetails.setGroup( previousDetails.getGroup() );
                        altered = true;
                    }
                }
                // everyone else can't change them at all.
                else                     
                {
                    throw new SecurityViolation(String.format(
                    	"You are not authorized to change " +
                    	"the group for %s from %s to %s",
                          iobj,
                          previousDetails.getGroup(),
                          currentDetails.getGroup()
                        ));
                }
            }
            
            // TODO should just be immutable even for root.
            if ( ! isGlobal( iobj.getClass() ) // implies that event doesn't matter
            		&& ! idEqual( 
                    previousDetails.getCreationEvent(), 
                    currentDetails.getCreationEvent()))
            {
                if ( user.getId().equals( 0L ))
                {
                    // even root can't set them to null.
                    if ( currentDetails.getCreationEvent() == null )
                    {
                        newDetails.setCreationEvent( 
                                previousDetails.getCreationEvent() );
                        altered = true;
                    }
                }
                // everyone else can't change them at all.
                else                 {
                    throw new SecurityViolation(String.format(
                        	"You are not authorized to change " +
                        	"the creation event for %s from %s to %s", 
                          iobj,
                          previousDetails.getCreationEvent(),
                          currentDetails.getCreationEvent()
                        ));
                }
            }
            
        }
        
        if ( iobj instanceof IMutable )
        {
            Integer version = ((IMutable) iobj).getVersion();
            if ( version == null || version.intValue() < 0 );
//                throw new ValidationException(
//                        "Version must properly be set on managed objects :\n"+
//                        obj.toString()
//                        );
                //TODO
        }
        
        if ( ! altered )
        {
        	return null;
        }
        return newDetails;
            
    }
	
	// ~ CurrentDetails delegation
	// =========================================================================
	
    public boolean emptyDetails( )
    {
    	return CurrentDetails.getOwner() == null 
    		&& CurrentDetails.getGroup() == null
    		&& CurrentDetails.getCreationEvent() == null;
    }
    
    public void addLog( String action, Class klass, Long id )
    {

        if ( EventLog.class.isAssignableFrom( klass )  
        		|| EventDiff.class.isAssignableFrom( klass ) ) 
        {
        	log.debug( "Not logging creation of logging type:");
        } 
        
        else 
        {
        	checkReady("addLog");
        	
        	log.info("Adding log:"+action+","+klass+","+id);
            
            EventLog l = new EventLog();
            l.setAction(action);
            l.setType(klass.getName()); // TODO could be id to Type entity
            l.setIdList(id.toString());
            l.setDetails(CurrentDetails.createDetails());
    
            CurrentDetails.getCreationEvent().addEventLog( l );
        }
    }
    
	public Long currentUserId()
	{
		checkReady("currentUserId");
		
		return CurrentDetails.getOwner().getId();
	}
	
	public Experimenter currentUser()
	{
		return CurrentDetails.getOwner();
	}

	public ExperimenterGroup currentGroup()
	{
		return CurrentDetails.getGroup();
	}

	public Event currentEvent()
	{
		return CurrentDetails.getCreationEvent();
	}

	public void newEvent( EventType type )
	{
		CurrentDetails.newEvent( type );
	}
	
	public Event getCurrentEvent()
	{
		return CurrentDetails.getCreationEvent();
	}
	
	public void setCurrentEvent( Event event )
	{
		CurrentDetails.setCreationEvent( event );
	}
	
	public void clearCurrentDetails()
	{
		CurrentDetails.clear();
	}
	
	public void setCurrentDetails()
    {
		IAdmin iAdmin = sf.getAdminService();
		ITypes iTypes = sf.getTypesService();
		
		clearCurrentDetails();

        if ( ec == null ) 
            throw new InternalException(
                    "EventContext is null in EventHandler. Invalid configuration."
            );
        
        if ( ec.getPrincipal() == null )
            throw new InternalException(
                    "Principal is null in EventHandler. Security system failure."
            );
        
        if (getName() == null)
            throw new InternalException(
                    "Principal.name is null in EventHandler. Security system failure.");

        Experimenter exp = iAdmin.lookupExperimenter(getName());
        CurrentDetails.setOwner(exp);
        
        if (getGroup() == null)
            throw new InternalException(
            "Principal.group is null in EventHandler. Security system failure.");

        ExperimenterGroup grp = iAdmin.lookupGroup(getGroup()); 
        CurrentDetails.setGroup(grp);

        if (getType() == null)
            throw new InternalException(
            "Principal.eventType is null in EventHandler. Security system failure.");

        EventType type = iTypes.getEnumeration(EventType.class,getType()); 
        CurrentDetails.newEvent(type);

    }

	// ~ Helpers
	// =========================================================================
	
    private String getName()
    {
        return ec.getPrincipal().getName();
    }

    private String getGroup()
    {
        return ec.getPrincipal().getGroup();
    }

    private String getType()
    {
        return ec.getPrincipal().getEventType();
    }	

    protected boolean idEqual( IObject arg1, IObject arg2 )
    {
    	// arg1 is null
    	if ( arg1 == null )
    	{
    		// both are null
    		if ( arg2 == null )
    		{
    			return true;
    		}
    		
    		// just arg1 is null
    		return false;
    	}
    	
    	// just arg2 is null
    	else if ( arg2 == null )
    	{
    		return false;
    	}
    	
    	// neither argument is null    	
    	Long arg1_id = arg1.getId();
    	Long arg2_id = arg2.getId();
    	
    	// arg1_id is null
    	if ( arg1_id == null )
    	{
    		// both are null
    		if ( arg2_id == null ) 
    		{
    			return true;
    		}
    		
    		// just arg2_id is null
    		return false;
    	}
    	
    	// just arg2_id null
    	else if ( arg2_id == null )
    	{
            return false;
    	}
    	
    	// neither null
        else
        {
            return arg1_id.equals( arg2_id );
        }
    }
    
    protected void checkReady(String method)
    {
    	if (!isReady())
    	{
    		throw new ApiUsageException("The security system is not ready.\n" +
    				"Cannot execute: "+method);
    	}

    }

}
