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
import java.util.Collections;
import java.util.IdentityHashMap;

//Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.taskdefs.condition.IsReference;
import org.hibernate.Session;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.util.Assert;

//Application-internal dependencies
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.api.IAdmin;
import ome.api.ITypes;
import ome.api.IUpdate;
import ome.conditions.ApiUsageException;
import ome.conditions.InternalException;
import ome.conditions.SecurityViolation;
import ome.conditions.ValidationException;
import ome.model.IEnum;
import ome.model.IMutable;
import ome.model.IObject;
import ome.model.enums.EventType;
import ome.model.internal.Details;
import ome.model.internal.GraphHolder;
import ome.model.internal.Permissions;
import ome.model.internal.Token;
import ome.model.meta.Event;
import ome.model.meta.EventDiff;
import ome.model.meta.EventLog;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.system.EventContext;
import ome.system.Principal;
import ome.system.ServiceFactory;
import ome.tools.hibernate.SecurityFilter;
import ome.util.IdBlock;

/** 
 * simplest implementation of {@link SecuritySystem}. Uses an ctor-injected 
 * {@link EventContext} and the {@link ThreadLocal ThreadLocal-}based 
 * {@link CurrentDetails} to provide the security infrastructure.
 * 
 * @author  Josh Moore, josh.moore at gmx.de
 * @version $Revision$, $Date$
 * @see 	Token
 * @see     SecuritySystem
 * @see     Details
 * @see     Permissions
 * @since   3.0-M3
 */
@RevisionDate("$Date$")
@RevisionNumber("$Revision$")
public class BasicSecuritySystem implements SecuritySystem
{
	private final static Log log = LogFactory.getLog(BasicSecuritySystem.class);
	
	/** private token for identifying loose "ownership" of certain objects.
	 * 
	 * @see IObject#getGraphHolder()
	 * @see GraphHolder#hasToken()
	 */
	private Token token = new Token();
	
	private ThreadLocal<IdentityHashMap<Token, Token>> oneTimeTokens 
		= new ThreadLocal<IdentityHashMap<Token,Token>>();
	
	/** {@link ServiceFactory} for accessing all available services */
	private ServiceFactory sf;
	
	/** conduit for login information from the outer-most levels. 
	 * Session bean implementations and other in-JVM clients can fill the 
	 * {@link EventContext}. Note, however, the execution must pass through
	 * the {@link EventHandler} for proper calls to be made to the 
	 * {@link SecuritySystem}
	 */
	private EventContext ec;

	/** only public constructor for this {@link SecuritySystem} implementation.
	 * 
	 * @param factory Not null. 
	 * @param eventContext Not null.
	 */
	public BasicSecuritySystem( 
			ServiceFactory factory, 
			EventContext eventContext)
	{
		Assert.notNull(factory);
		Assert.notNull(eventContext);
		this.sf = factory;
		this.ec = eventContext;
	}
	
	
	/** implements {@link SecuritySystem#isReady()}. Simply checks for null
	 * values in all the relevant fields of {@link CurrentDetails}
	 */
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
	
	/** implements {@link SecuritySystem#isReady()}
	 * classes without owners and events */
	public boolean isGlobal( Class<? extends IObject> klass)
	{
		if ( klass == null ) return false;
		if ( Experimenter.class.isAssignableFrom( klass )) return true;
		if ( Event.class.isAssignableFrom( klass )) return true;
		return false;
	}
	
	/** classes which cannot be created by regular users. 
	 * @see <a href="https://trac.openmicroscopy.org.uk/omero/ticket/156">ticket156</a> 
	 */
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
		
	// ~ Read security
	// =========================================================================
	/** implements {@link SecuritySystem#enableReadFilter(Object)} 
	 * Turns on the read filter defined by {@link SecurityFilter} using 
	 * 
	 * @see Filte
	 */
	public void enableReadFilter( Object session ) 
	{
		Session sess = (Session) session;
		sess.enableFilter(SecurityFilter.filterName)
			.setParameter(
					SecurityFilter.current_user,
					currentUserId())
			.setParameterList(
					SecurityFilter.current_groups, 
					currentUser().eachLinkedExperimenterGroup(new IdBlock()))
			.setParameterList(
					SecurityFilter.leader_of_groups,
					Collections.singleton(1000L)); // FIXME
	}
	
	public void disableReadFilter( Object session ) {
		Session sess = (Session) session;
		sess.disableFilter(SecurityFilter.filterName);
	}
	
	// ~ Write security (mostly for the Hibernate events)
	// =========================================================================
	
	public boolean allowCreation( IObject iObject )
	{
		Class cls = iObject.getClass();
		
		if ( isPrivileged( iObject ))
		{
			return true;
		}
		
		else if ( isSystemType( (Class<? extends IObject>) cls )
				&& ! currentUserId().equals( getRootId() ) )
		{
			return false;
		}
		
		return true;
	}

	public void throwCreationViolation( IObject iObject ) throws SecurityViolation {
		throw new SecurityViolation(
				iObject.getClass().getName()
				+" is a System-type, and may only be " 
				+"created through privileged APIs.");
	}
	
	public boolean allowUpdate( IObject iObject )
	{
		if ( isPrivileged( iObject ))
		{
			return true;
		}
		
		else if ( isSystemType( (Class<? extends IObject>) iObject.getClass())
			&& (! currentUserId().equals( getRootId() )))
		{
			return false;
		}

		return true;
	}
	
	public void throwUpdateViolation( IObject iObject ) throws SecurityViolation {
		throw new SecurityViolation( "Updating "+iObject+" not allowed." );
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
     * always returns a non-null Details that is not equivalent (==) to the 
     * Details argument. 
     */ 
    public Details transientDetails( IObject obj )
    {
    	
    	checkReady("transientDetails");
    	
    	if ( isPrivileged( obj ) ) return obj.getDetails(); // EARLY EXIT
    	
        Details source = obj.getDetails();
        Details newDetails = CurrentDetails.createDetails();
        Experimenter user = newDetails.getOwner();
        ExperimenterGroup group = newDetails.getGroup();
        
        if ( source != null )
        {
            // TODO everyone is allowed to set the umask if desired.
            if (source.getPermissions() != null)
            {
                newDetails.setPermissions( source.getPermissions() );
            }
            
            // users *aren't* allowed to set the owner/group of an item.
            if (source.getOwner() != null 
                    && ! source.getOwner().getId().equals( 
                            newDetails.getOwner().getId() ))
            {
                // but this is root
                if ( user.getId().equals( 0L ))
                {
                    newDetails.setOwner( source.getOwner() );
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
                            newDetails.getGroup().getId() ))
            {
                
                // but this is root
                if ( user.getId().equals( 0L ))
                {
                    newDetails.setGroup( source.getGroup() );
                } else {
                    throw new SecurityViolation(String.format(
                        "You are not authorized to set the ExperimenterGroupID"+
                        " for %s to %d", obj, source.getGroup().getId() 
                        ));
                }
            }
            
        }

        return newDetails;

    }

    /* TODO what else should be preserved?
     * TODO should move to @Validation.
     * should be able to switch group if member, e.g. */
    /** checks that a non-privileged user has not attempted to edit the 
     * entity's {@link IObject#getDetails() security details}. Privileged 
     * users can set fields on {@link Details} as a single-step 
     * <code>chmod</code> and <code>chgrp</code>.
     * 
     * {@link #managedDetails(IObject, Details) managedDetails}
     * may create a new Details instance and return that if needed. If the returned
     * Details is not equivalent (==) to the argument Details, then values
     * have been changed.
     */
    public Details managedDetails( IObject iobj, Details previousDetails )
    {
    	checkReady("managedDetails");
    	
        if ( iobj.getId() == null)
            throw new ValidationException(
                    "Id required on all detached instances.");

    	if ( isPrivileged( iobj )) return iobj.getDetails(); // EARLY EXIT
    	
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
            
            if ( ! isGlobal( iobj.getClass() )) // implies that owner doesn't matter 
            {
            	altered |= managedOwner( 
            			iobj, previousDetails, currentDetails, newDetails, user);
            }

            if ( ! isGlobal( iobj.getClass() )) // implies that group doesn't matter
            {
            	altered |= managedGroup( 
            			iobj, previousDetails, currentDetails, newDetails, user );
            }
            
            // TODO should just be immutable even for root.
            if ( ! isGlobal( iobj.getClass() )) // implies that event doesn't matter
    		{
            	altered |= managedEvent( 
            			iobj, previousDetails, currentDetails, newDetails, user );
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
        
        return altered ? newDetails : previousDetails;
            
    }
    
    protected boolean managedOwner( IObject obj, 
    		Details previousDetails, Details currentDetails, Details newDetails, 
    		Experimenter user)
    {
    	
   		if (! idEqual( 
                previousDetails.getOwner(), 
                currentDetails.getOwner() ))
        {
            
            if ( user.getId().equals( 0L ))
            {
                // even root can't set them to null.
                if ( currentDetails.getOwner() == null )
                {
                    newDetails.setOwner( previousDetails.getOwner() );
                    return true;
                }
            }
            // everyone else can't change them at all.
            else 
            {
                throw new SecurityViolation(String.format(
                    "You are not authorized to change " +
                    "the owner for %s from %s to %s",
                      obj, 
                      previousDetails.getOwner(), 
                      currentDetails.getOwner()
                    ));
            }
        } 
   		return false;
    }
    
    protected boolean managedGroup( IObject obj, 
    		Details previousDetails, Details currentDetails, Details newDetails, 
    		Experimenter user)
    {
		if (! idEqual( 
                previousDetails.getGroup(), 
                currentDetails.getGroup() ))
        {
            
            if ( user.getId().equals( 0L ))
            {
                // even root can't set them to null.
                if ( currentDetails.getGroup() == null )
                {
                    newDetails.setGroup( previousDetails.getGroup() );
                    return true;
                }
            }
            // everyone else can't change them at all.
            else                     
            {
                throw new SecurityViolation(String.format(
                	"You are not authorized to change " +
                	"the group for %s from %s to %s",
                      obj,
                      previousDetails.getGroup(),
                      currentDetails.getGroup()
                    ));
            }
        }
		return false;
    }
    
    protected boolean managedEvent( IObject obj, 
    		Details previousDetails, Details currentDetails, Details newDetails, 
    		Experimenter user)
    {
   		if ( ! idEqual( 
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
                    return true;
                }
            }
            // everyone else can't change them at all.
            else                 {
                throw new SecurityViolation(String.format(
                    	"You are not authorized to change " +
                    	"the creation event for %s from %s to %s", 
                      obj,
                      previousDetails.getCreationEvent(),
                      currentDetails.getCreationEvent()
                    ));
            }
        }
   		return false;
    }
	
	// ~ CurrentDetails delegation (ensures proper settings of Tokens)
	// =========================================================================
	    
	public void setCurrentDetails()
    {
		IAdmin iAdmin = sf.getAdminService();
		ITypes iTypes = sf.getTypesService();
		IUpdate iUpdate = sf.getUpdateService();
		
		clearCurrentDetails();

        if ( ec == null ) 
            throw new InternalException(
                    "EventContext is null in EventHandler. Invalid configuration."
            );
        
        if ( ec.getPrincipal() == null )
            throw new InternalException(
                    "Principal is null in EventHandler. Security system failure."
            );
        
        if (ec.getPrincipal().getName() == null)
            throw new InternalException(
                    "Principal.name is null in EventHandler. Security system failure.");

        Principal p = ec.getPrincipal();
        
        Experimenter exp = iAdmin.lookupExperimenter(p.getName());
        exp.getGraphHolder().setToken(token, token);
        CurrentDetails.setOwner(exp);
        
        if (p.getGroup() == null)
            throw new InternalException(
            "Principal.group is null in EventHandler. Security system failure.");

        ExperimenterGroup grp = iAdmin.lookupGroup(p.getGroup()); 
        exp.getGraphHolder().setToken(token, token);
        CurrentDetails.setGroup(grp);

        if (p.getEventType() == null)
            throw new InternalException(
            "Principal.eventType is null in EventHandler. Security system failure.");

        EventType type = iTypes.getEnumeration(EventType.class,p.getEventType());
        type.getGraphHolder().setToken(token, token);
        CurrentDetails.newEvent(type,token);
        // hack
        Event orig = CurrentDetails.getCreationEvent();
        HibernateTemplate ht=
        (HibernateTemplate) sf.getContext().getBean("hibernateTemplate");
        try 
        {
        	Object retVal = ht.merge(CurrentDetails.getCreationEvent());
        	CurrentDetails.setCreationEvent((Event)retVal);
        } catch (InvalidDataAccessApiUsageException ex) {
        	// probably read-only
        	setCurrentEvent(orig);
        }
    }
	
	public void newEvent( EventType type )
	{
		CurrentDetails.newEvent( type, token );
	}
	
    public void addLog( String action, Class klass, Long id )
    {

        if ( Event.class.isAssignableFrom( klass )
        		|| EventLog.class.isAssignableFrom( klass )  
        			|| EventDiff.class.isAssignableFrom( klass ) ) 
        {
        	log.debug( "Not logging creation of logging type:"+klass);
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
            l.getGraphHolder().setToken(null, token);
    
            CurrentDetails.getCreationEvent().addEventLog( l );
        }
    }
    	
	public void setCurrentEvent( Event event )
	{
		CurrentDetails.setCreationEvent( event );
	}
	
	public void clearCurrentDetails()
	{
		CurrentDetails.clear();
	}
	
	// read-only ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    public boolean emptyDetails( )
    {
    	return CurrentDetails.getOwner() == null 
    		&& CurrentDetails.getGroup() == null
    		&& CurrentDetails.getCreationEvent() == null;
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
	
	public Event getCurrentEvent()
	{
		return CurrentDetails.getCreationEvent();
	}
	
	// ~ Actions
	// =========================================================================	
	/** 
	 * 
	 * It would be better to catch the {@link SecureAction#updateObject(IObject)}
	 * method in a try/finally block, but since flush can be so poorly controlled
	 * that's not possible. instead, we use the one time token which is removed
	 * this Object is checked for {@link #isPrivileged(IObject) privileges}. 
	 */
	public <T extends IObject> T doAction(T obj, SecureAction action) {
		// setup
		IdentityHashMap<Token, Token> map = oneTimeTokens.get();
		if (map == null)
		{
			map = new IdentityHashMap<Token, Token>();
			oneTimeTokens.set(map);
		}
		Token oneTimeToken = new Token();
		map.put(oneTimeToken, oneTimeToken);
		obj.getGraphHolder().setToken(token,oneTimeToken);
		return action.updateObject(obj);
		
	}
	
	// ~ Privileged accounts
	// =========================================================================
	// TODO This information is also encoded at:

	public long getRootId() 			{	return 0L;		}
	public long getSystemGroupId() 		{	return 0L;		}	
	public long getUserGroupId() 		{	return 1L;		}
	public String getRootName()			{	return "root";	}
	public String getSystemGroupName( )	{	return "system";}
	public String getUserGroupName( )	{	return "user";	}
	public boolean isSystemGroup( ExperimenterGroup group )
										{	return false;	}
	
	// ~ Helpers
	// =========================================================================
	
	/** calls {@link #isReady()} and if not throws an 
	 * {@link ApiUsageException}. The {@link SecuritySystem} must be in a 
	 * valid state to perform several functions. 
	 */
    protected void checkReady(String method)
    {
    	if (!isReady())
    	{
    		throw new ApiUsageException("The security system is not ready.\n" +
    				"Cannot execute: "+method);
    	}

    }
    
    /** checks that the {@link IObject} argument has been granted a 
     * {@link Token} by the {@link SecuritySystem}. 
     */
	private boolean isPrivileged( IObject obj )
	{
		GraphHolder gh = obj.getGraphHolder();
		
		// most objects will not have a token
		if ( gh.hasToken() )
		{
			// check if truly secure.
			if (gh.tokenMatches(token)) return true;
			
			// oh well, now see if this object has a one-time token.
			IdentityHashMap<Token, Token> map = oneTimeTokens.get();
			if ( map == null ) return false;
			for (Token t : map.values()) {
				if (gh.tokenMatches(t))
				{
					// it does have the token, so it is privileged for one action
					// set token to null for future checks.
					gh.setToken(t, null);
					return true; 
				}
			}
			
		}
		return false;
	}

	// ~ Details checks. Used by to examine transient and managed Details.
	// =========================================================================
	
    protected boolean idEqual( IObject arg1, IObject arg2 )
    {
    	
    	// arg1 is null
    	if ( arg1 == null )
    	{
    		// both are null, therefore equal
    		if ( arg2 == null ) return true;
    		
    		// just arg1 is null, can't be equal
    		return false;
    	}
    	
    	// just arg2 is null, also can't be equal
    	else if ( arg2 == null ) return false;
    	
    	// neither argument is null, 
    	// so let's move a level down.	
    	
    	Long arg1_id = arg1.getId();
    	Long arg2_id = arg2.getId();
    	
    	// arg1_id is null
    	if ( arg1_id == null ) {
    		
    		// both are null, therefore equal
    		if ( arg2_id == null ) return true;
    		
    		// just arg2_id is null, can't be equal
    		return false;
    	}
    	
    	// just arg2_id null, and also can't be equal
    	else if ( arg2_id == null ) return false;
    	
    	// neither null, then we can just test the ids.
        else return arg1_id.equals( arg2_id );
    }
    
}
