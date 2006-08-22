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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

//Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.util.Assert;

//Application-internal dependencies
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.api.ITypes;
import ome.api.IUpdate;
import ome.api.local.LocalAdmin;
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
import static ome.model.internal.Permissions.Right.*;
import static ome.model.internal.Permissions.Role.*;
import ome.model.internal.Token;
import ome.model.meta.Event;
import ome.model.meta.EventDiff;
import ome.model.meta.EventLog;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.GroupExperimenterMap;
import ome.system.EventContext;
import ome.system.Principal;
import ome.system.ServiceFactory;
import ome.tools.hibernate.MergeEventListener;
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
	
	/** inner private class for holding one-time tokens in a {@link ThreadLocal}
	 * one-time tokens are currently not used because of the difficulty of 
	 * guaranteeing just one access.
	 */
	private OneTimeTokens oneTimeTokens = new OneTimeTokens();
	
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
		if ( CurrentDetails.getCreationEvent() != null 
				&& CurrentDetails.getGroup() != null 
				&& CurrentDetails.getOwner() != null )
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
		if ( ExperimenterGroup.class.isAssignableFrom( klass )) return true;
		if ( GroupExperimenterMap.class.isAssignableFrom( klass )) return true;
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
	 * the user in the current Thread context as parameters.
	 * 
	 * Note: the {@link Session} argument cannot be used to load objects, either
	 * directly via {@link Session#createQuery(String)} etc. or indirectly via
	 * lazy loading, while the {@link Filter} is being enabled.
	 * 
	 * @see SecurityFilter
	 */
	public void enableReadFilter( Object session ) 
	{
		if ( session == null || ! (session instanceof Session) )
		{
			throw new ApiUsageException("The Object argument to enableReadFilter" +
					" in the BasicSystemSecurity implementation must be a " +
					" non-null org.hibernate.Session.");
		}
		
		checkReady("enableReadFilter");
	// beware http://opensource.atlassian.com/projects/hibernate/browse/HHH-1932	
		Session sess = (Session) session;
		sess.enableFilter(SecurityFilter.filterName)
			.setParameter(
					SecurityFilter.is_admin,
					currentUserIsAdmin())
			.setParameter(
					SecurityFilter.current_user,
					currentUserId())
			.setParameterList(
					SecurityFilter.current_groups, 
					memberOfGroups())
			.setParameterList(
					SecurityFilter.leader_of_groups,
					leaderOfGroups());
	}
	
	/** implements {@link SecuritySystem#disableReadFilter(Object)}
	 * Turns of the secu
	 */
	public void disableReadFilter( Object session ) 
	{	
		checkReady("disableReadFilter");
			
		Session sess = (Session) session;
		sess.disableFilter(SecurityFilter.filterName);
	}
	
	// ~ Write security (mostly for the Hibernate events)
	// =========================================================================

	/**
	 * 
	 * 
	 * delegates to SecurityFilter because that is where the logic is 
	 * defined for the {@link #enableReadFilter(Object) read filter}
	 */
	public boolean allowLoad( Class<? extends IObject> klass, Details d )
	{
		Assert.notNull(klass);
		Assert.notNull(d);
		if ( isSystemType(klass)) return true;
		return SecurityFilter.passesFilter(this, d);
	}

	public void throwLoadViolation( IObject iObject ) throws SecurityViolation 
	{
		Assert.notNull(iObject);
		throw new SecurityViolation(
				"Cannot read "+iObject.getClass().getName());
	}
	
	public boolean allowCreation( IObject iObject )
	{
		Assert.notNull(iObject);
		Class cls = iObject.getClass();
		
		if ( isPrivileged( iObject ) || currentUserIsAdmin() )
		{
			return true;
		}
		
		else if ( isSystemType( (Class<? extends IObject>) cls ))
		{
			return false;
		}
		
		return true;
	}

	public void throwCreationViolation( IObject iObject ) throws SecurityViolation 
	{
		Assert.notNull(iObject);
		throw new SecurityViolation(
				iObject.getClass().getName()
				+" is a System-type, and may only be " 
				+"created through privileged APIs.");
	}
	
	public boolean allowUpdate( IObject iObject, Details trustedDetails )
	{
		return allowUpdateOrDelete(iObject,trustedDetails);
	}
	
	public void throwUpdateViolation( IObject iObject ) throws SecurityViolation 
	{
		Assert.notNull(iObject);
		throw new SecurityViolation( "Updating "+iObject+" not allowed." );
	}
	
	public boolean allowDelete( IObject iObject, Details trustedDetails )
	{
		return allowUpdateOrDelete(iObject,trustedDetails);
	}
	
	public void throwDeleteViolation( IObject iObject ) throws SecurityViolation 
	{
		Assert.notNull(iObject);
		throw new SecurityViolation( "Deleting "+iObject+" not allowed." );
	}
	
	private boolean allowUpdateOrDelete(IObject iObject, Details trustedDetails) 
	{
		Assert.notNull(iObject);
		if ( isPrivileged( iObject ) || currentUserIsAdmin() ) return true;		
		else if ( isSystemType( (Class<? extends IObject>) iObject.getClass()))
		{
			return false;
		}
		
		// previously we were taking the details directly from iObject
		// iObject, however, is in a critical state. Values such as 
		// Permissions, owner, and group may have been changed.
		Details d = trustedDetails;
		
		// this can now only happen if a table doesn't have permissions
		// and there aren't any of those. so let it be updated.
		if ( d == null ) return true;

		Long o = d.getOwner() == null ? null : d.getOwner().getId();
		Long g = d.getGroup() == null ? null : d.getGroup().getId();

		if ( g != null && leaderOfGroups().contains(g)) return true;
		
		Permissions p = d.getPermissions();
		if ( p.isGranted(USER, WRITE) && o!=null && o.equals(currentUserId()))
			return true;
		if ( p.isGranted(GROUP, WRITE) && g!=null && g.equals(currentGroupId()))
			return true;
		if ( p.isGranted(WORLD,WRITE)) return true;
		
		return false;
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
        
        if ( source != null )
        {
            // TODO everyone is allowed to set the umask if desired.
            if (source.getPermissions() != null)
            {
                newDetails.setPermissions( source.getPermissions() );
            }
            
            // users *aren't* allowed to set the owner of an item.
            if (source.getOwner() != null 
                    && ! source.getOwner().getId().equals( 
                            newDetails.getOwner().getId() ))
            {
                // but this is root
                if ( currentUserIsAdmin() )
                {
                    newDetails.setOwner( source.getOwner() );
                } else {
                    throw new SecurityViolation(String.format(
                        "You are not authorized to set the ExperimenterID" +
                        " for %s to %d", obj, source.getOwner().getId() 
                        ));
                }
                
            }

            // users are only allowed to set to another of their groups
            if (source.getGroup() != null && source.getGroup().getId() != null)
            {
            	// users can change to their own group
                if (memberOfGroups().contains( source.getGroup().getId()))
                {
                	newDetails.setGroup( source.getGroup() );
                }
                
                // and admin can change it too
                else if ( currentUserIsAdmin() )
                {
                    newDetails.setGroup( source.getGroup() );
                }
                  
                // oops. boom!
                else {
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
        
        // This happens if all fields of details are null.
        if ( previousDetails == null ) 
        {
            if ( currentDetails != null )
            {
                newDetails = null;
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
            
            // TODO everyone is allowed to set the umask if desired.
            if (currentDetails.getPermissions() != null)
            {
                newDetails.setPermissions( currentDetails.getPermissions() );
                altered = true;
            }
        	
            if ( ! isGlobal( iobj.getClass() )) // implies that owner doesn't matter 
            {
            	altered |= managedOwner( 
            			iobj, previousDetails, currentDetails, newDetails);
            }

            if ( ! isGlobal( iobj.getClass() )) // implies that group doesn't matter
            {
            	altered |= managedGroup( 
            			iobj, previousDetails, currentDetails, newDetails);
            }
            
            if ( ! isGlobal( iobj.getClass() )) // implies that event doesn't matter
    		{
            	altered |= managedEvent( 
            			iobj, previousDetails, currentDetails, newDetails);
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
    		Details previousDetails, Details currentDetails, Details newDetails)
    {
    	
   		if (! idEqual( 
                previousDetails.getOwner(), 
                currentDetails.getOwner() ))
        {
            
            if ( currentUserIsAdmin() )
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
   		
   		else {
   			
   			// values are the same. ensure they are the same for 
   			// newDetails as well
   			newDetails.setOwner(previousDetails.getOwner());
   		}
   		return false;
    }
    
    protected boolean managedGroup( IObject obj, 
    		Details previousDetails, Details currentDetails, Details newDetails)
    {
    	// previous and current have different ids. either change it and return
    	// true if permitted, or throw an exception.
		if (! idEqual( 
                previousDetails.getGroup(), 
                currentDetails.getGroup() ))
        {

			// even root can't set them to null.
			if ( currentDetails.getGroup() == null )
            {
                    newDetails.setGroup( previousDetails.getGroup() );
                    return true;
            }

			// if user is a member of the group or the current user is an admin
			// then use the current group.
			else if ( memberOfGroups().contains(
						currentDetails.getGroup().getId())
					|| currentUserIsAdmin() ) 
			{
				newDetails.setGroup( currentDetails.getGroup());
				return true;
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
		
		// previous and current are the same, but we need to set
		// that value on newDetails.
		else {

			// This doesn't need to return true, because it'll only
			// be used if something else was changed.
			newDetails.setGroup( previousDetails.getGroup());

		}
		return false;
    }
    
    protected boolean managedEvent( IObject obj, 
    		Details previousDetails, Details currentDetails, Details newDetails)
    {
   		if ( ! idEqual( 
                previousDetails.getCreationEvent(), 
                currentDetails.getCreationEvent()))
        {
            // even root can't set them to null.
            if ( currentDetails.getCreationEvent() == null )
            {
                newDetails.setCreationEvent( 
                        previousDetails.getCreationEvent() );
                return true;
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
   		
   		// they are equal meaning no change was intended but in case other
   		// changes took place, we have to make sure newDetails has the correct
   		// value
   		else 
   		{
   			newDetails.setCreationEvent(previousDetails.getCreationEvent());
   		}
   		return false;
    }
	
	// ~ CurrentDetails delegation (ensures proper settings of Tokens)
	// =========================================================================
	    
	public void setCurrentDetails()
    {
		LocalAdmin localAdmin = (LocalAdmin) sf.getAdminService();
		ITypes iTypes = sf.getTypesService();
		IUpdate iUpdate = sf.getUpdateService();
		
		clearCurrentDetails();

        if ( ec == null ) 
            throw new InternalException(
                    "EventContext is null in EventContext. Invalid configuration."
            );
        
        if ( ec.getPrincipal() == null )
            throw new InternalException(
                    "Principal is null in EventContext. Security system failure."
            );
        
        if (ec.getPrincipal().getName() == null)
            throw new InternalException(
                    "Principal.name is null in EventContext. Security system failure.");

        final Principal p = ec.getPrincipal();
        
        // Experimenter
        
        final Experimenter exp = localAdmin.lookupExperimenter(p.getName());
        exp.getGraphHolder().setToken(token, token);
        CurrentDetails.setOwner(exp);
        
        // Member of Groups
        List<Long> memberOfGroupsIds = 
        	exp.eachLinkedExperimenterGroup(new IdBlock());
        CurrentDetails.setMemberOfGroups(memberOfGroupsIds);
        
        // Leader of Groups
        List<Long> leaderOfGroupsIds = localAdmin.getLeaderOfGroupIds(exp);
        CurrentDetails.setLeaderOfGroups(leaderOfGroupsIds);
        
        // Active group
        
        if (p.getGroup() == null)
            throw new InternalException(
            "Principal.group is null in EventContext. Security system failure.");

        ExperimenterGroup grp = localAdmin.groupProxy(p.getGroup()); 
        grp.getGraphHolder().setToken(token, token);
        CurrentDetails.setGroup(grp);

        // isAdmin
        
        if (isSystemGroup(grp))
        {
        	CurrentDetails.setAdmin(true);
        }
        
        // Event 
        
        if (p.getEventType() == null)
            throw new InternalException(
            "Principal.eventType is null in EventContext. Security system failure.");

        EventType type = iTypes.getEnumeration(EventType.class,p.getEventType());
        type.getGraphHolder().setToken(token, token);
        CurrentDetails.newEvent(type,token);
        
        Event event = getCurrentEvent();
        event.getGraphHolder().setToken(token, token);
        try 
        {
        	setCurrentEvent(iUpdate.saveAndReturnObject(event));
        } catch (InvalidDataAccessApiUsageException ex) { 
        	// TODO check for read-only bef. exception
        	log.warn("Attempt to save event in SecuritySystem failed. " +
        			"Using unsaved.",ex);
        	setCurrentEvent( event );
        }

    }
	
	public void newEvent( EventType type )
	{
		CurrentDetails.newEvent( type, token );
	}
	
    public void addLog( String action, Class klass, Long id )
    {

    	Assert.notNull(action);
    	Assert.notNull(klass);
    	Assert.notNull(id);
    	
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
	
	public Long currentGroupId()
	{
		checkReady("currentGroupId");
		return CurrentDetails.getGroup().getId();
	}
	
	public Collection<Long> leaderOfGroups()
	{
		checkReady("leaderOfGroups");
		return CurrentDetails.getLeaderOfGroups();
	}
	
	public Collection<Long> memberOfGroups()
	{
		checkReady("memberOfGroups");
		return CurrentDetails.getMemberOfGroups();
	}
	
	public Experimenter currentUser()
	{
		checkReady("currentUser");
		return CurrentDetails.getOwner();
	}

	public ExperimenterGroup currentGroup()
	{
		checkReady("currentGroup");
		return CurrentDetails.getGroup();
	}

	public Event currentEvent()
	{
		checkReady("currentEvent");
		return CurrentDetails.getCreationEvent();
	}
	
	public Event getCurrentEvent()
	{
		checkReady("getCurrentEvent");
		return CurrentDetails.getCreationEvent();
	}
	
	public boolean currentUserIsAdmin()
	{
		checkReady("currentUserIsAdmin");
		return CurrentDetails.isAdmin();
	}
	
	// ~ Tokens & Actions
	// =========================================================================	
	
	class OneTimeTokens 
	{
		private ThreadLocal<IdentityHashMap<Token, Token>> tokens 
		= new ThreadLocal<IdentityHashMap<Token,Token>>();
		
		public Set<Token> allTokens()
		{
			if ( tokens.get() == null) return Collections.emptySet();
			return tokens.get().keySet();
		}
		
		public void put(Token token)
		{
			if ( tokens.get() == null ) tokens.set( new IdentityHashMap<Token,Token>() );
			tokens.get().put(token, token);
		}

		public void remove(Token t)
		{
			if ( tokens.get() == null ) return;
			tokens.get().remove(t);
		}
		
		public Token find(GraphHolder gh)
		{
			if ( tokens.get() == null ) return null;
			for (Token t : allTokens()) {
				if (gh.tokenMatches(t))
				{
					return t; 
				}
			}
			return null;
		}
		
	}
	
	/** 
	 * 
	 * It would be better to catch the {@link SecureAction#updateObject(IObject)}
	 * method in a try/finally block, but since flush can be so poorly controlled
	 * that's not possible. instead, we use the one time token which is removed
	 * this Object is checked for {@link #isPrivileged(IObject) privileges}. 
	 */
	public <T extends IObject> T doAction(T obj, SecureAction action) {
		Assert.notNull(obj);
		Assert.notNull(action);

//		FIXME
//		Token oneTimeToken = new Token();
//		oneTimeTokens.put(oneTimeToken);
		obj.getGraphHolder().setToken(token,token);//oneTimeToken);
		
		T retVal;
		try 
		{
			retVal = action.updateObject(obj);
		} finally {
			obj.getGraphHolder().setToken(token,null);	
		}
		return retVal;
	}
	
	public void runAsAdmin( AdminAction action )
	{
		Assert.notNull(action);
		CurrentDetails.setAdmin(true);
		try
		{
			action.runAsAdmin();
		} finally {
			CurrentDetails.setAdmin(false);
		}
	}
	
	/**
	 * copy a token from one {@link IObject} to another. This is currently 
	 * insecure and should take a third token implying the rights to copy. 
	 * Should only be called by {@link MergeEventListener}
	 */
	public void copyToken(IObject source, IObject copy) {
		
		if ( source == null || copy == null || source == copy)
			return;
		
		GraphHolder gh1 = source.getGraphHolder();
		GraphHolder gh2 = copy.getGraphHolder();
		
		// try our token first
		if ( gh1.tokenMatches(token)) 
		{
			gh2.setToken(token,token);
		}
			
		else // now we'll have to loop through
		{
			Token t = oneTimeTokens.find(gh1);
			if ( t != null )
			{
				gh2.setToken(t, t);
			}
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
			Token t = oneTimeTokens.find(gh);
			if ( t != null )
			{
				// it does have the token, so it is privileged for one action
				// set token to null for future checks.
		
				gh.setToken(t,null);
				oneTimeTokens.remove(t);
				return true;
			}		
		}
		return false;
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
	{	
		return group == null || group.getId() == null? false : 
			group.getId().equals( getSystemGroupId() );
	}
	
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
