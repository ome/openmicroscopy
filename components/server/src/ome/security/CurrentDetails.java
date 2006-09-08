/*
 * ome.security.CurrentDetails
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
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

//Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//Application-internal dependencies
import ome.model.IObject;
import ome.model.enums.EventType;
import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.model.internal.Token;
import ome.model.meta.Event;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;

/** Stores information related to the security context of the current thread.
 * Code calling into the server must setup CurrentDetails properly. An existing
 * user must be set (the creation of a new user is only allowed if the current
 * user is set to root; root always exists. QED.) The event must also be set.
 * Umask is optional.
 * 
 * This information is stored in a Details object, but unlike Details which 
 * assumes that an empty value signifies increased security levels, empty values
 * here signifiy reduced security levels. E.g.,
 * 
 * Details:         user == null ==> object belongs to root
 * CurrentDetails:  user == null ==> current user is "nobody" (anonymous)
 * 
 */
abstract class CurrentDetails
{
    private static Log log = LogFactory.getLog(CurrentDetails.class);
    
    private static ThreadLocal<Details> detailsHolder = 
        new ThreadLocal<Details>();

    private static ThreadLocal<Permissions> umaskHolder = 
        new ThreadLocal<Permissions>();

    private static ThreadLocal<Boolean> isAdminHolder = 
    	new ThreadLocal<Boolean>();

    private static ThreadLocal<Collection<Long>> memberOfGroupsHolder =
    	new ThreadLocal<Collection<Long>>();
    
    private static ThreadLocal<Collection<Long>> leaderOfGroupsHolder =
    	new ThreadLocal<Collection<Long>>();
    
    private static ThreadLocal<Set<String>> disabledSubsystemsHolder =
    	new ThreadLocal<Set<String>>();
    
    private static ThreadLocal<Set<IObject>> lockCandidatesHolder = 
    	new ThreadLocal<Set<IObject>>();
    
    /** removes all current context. This must stay in sync with the instance
     * fields. If a new {@link ThreadLocal} is added, {@link ThreadLocal#remove()}
     * <em>must</em> be called.
     */
    public static void clear(){
        detailsHolder.remove();
        isAdminHolder.remove();
        umaskHolder.remove();
        leaderOfGroupsHolder.remove();
        memberOfGroupsHolder.remove();
        disabledSubsystemsHolder.remove();
        lockCandidatesHolder.remove();
    }
    
    // ~ Internals
    // ================================================================

    protected static void setDetails(Details details)
    {
        detailsHolder.set(details);
    }

    protected static Details getDetails()
    {
        Details details = detailsHolder.get();
        if (details == null)
        {
            details = new Details();
            setDetails(details);
        }
        return details;
    }

    // ~ Main methods
    // =================================================================
    public static void newEvent(EventType type, Token token) // TODO keep up with stack here?
    {
        Event e = new Event();
        e.setType(type);
        e.setTime(new Timestamp(System.currentTimeMillis()));
        e.getGraphHolder().setToken(token, token);
        e.getDetails().setPermissions( Permissions.IMMUTABLE );
        setCreationEvent(e);
    }
    
    public static void clearLogs()
    {
        getCreationEvent().clearLogs();
    }
    
    public static Details createDetails()
    {
        Details d = new Details();
        d.setCreationEvent(getCreationEvent());
        d.setOwner(getOwner());
        d.setGroup(getGroup());
        d.setPermissions(getUmask());
        return d;
    }
    
    // ~ Umask
    // =========================================================================
    public static Permissions getUmask()
    {
        Permissions umask = umaskHolder.get();
        if (umask == null)
        {
            umask = new Permissions();
            setUmask(umask);
        }
        return umask; 
        /* FIXME
         * should be configurable
         * see https://trac.openmicroscopy.org.uk/omero/ticket/179
         * getOwner().getProfile().getUmask
         * object.getDetails().getUmask()
         * CurrentDetails.getDetails().getUmask();
         */
    }
    
    public static void setUmask(Permissions umask)
    {
        umaskHolder.set(umask);
    }
    
    // ~ Delegation FIXME possibly remove setters for set(Exp,Grp)
    // =========================================================================
    
    public static Event getCreationEvent()
    {
        return getDetails().getCreationEvent();
    }

    public static Experimenter getOwner()
    {
        return getDetails().getOwner();
    }

    public static Permissions getPermissions()
    {
        return getDetails().getPermissions();
    }

    public static Event getUpdateEvent()
    {
        return getDetails().getUpdateEvent();
    }

    public static void setCreationEvent(Event e)
    {
        getDetails().setCreationEvent(e);
    }

    public static void setOwner(Experimenter exp)
    {
        getDetails().setOwner(exp);
    }

    public static void setPermissions(Permissions perms)
    {
        getDetails().setPermissions(perms);
    }

    // TODO hide these specifics. possibly also Owner->User & CreationEvent -> Event
    public static void setUpdateEvent(Event e)
    {
        getDetails().setUpdateEvent(e);
    }
    
    public static ExperimenterGroup getGroup()
    {
        return getDetails().getGroup();
    }
    
    public static void setGroup(ExperimenterGroup group)
    {
        getDetails().setGroup(group);
    }
 
    public static void setAdmin( boolean isAdmin )
    {
    	isAdminHolder.set( Boolean.valueOf(isAdmin));
    }
    
    public static boolean isAdmin( )
    {
    	return isAdminHolder.get() == null ? false : 
    		isAdminHolder.get().booleanValue();
    }
        
    public static void setMemberOfGroups( Collection<Long> groupIds )
    {
    	memberOfGroupsHolder.set( groupIds );
    }
    
    public static Collection<Long> getMemberOfGroups( )
    {
		Collection<Long> c = memberOfGroupsHolder.get();    	
    	if ( c == null || c.size() == 0 )
    	{
    		c = Collections.singletonList(Long.MIN_VALUE); // FIXME hack as well.
    	}
    	return c;
    }
    
    public static void setLeaderOfGroups( Collection<Long> groupIds )
    {
    	leaderOfGroupsHolder.set( groupIds );
    }
    
    public static Collection<Long> getLeaderOfGroups( )
    {
		Collection<Long> c = leaderOfGroupsHolder.get();    	
    	if ( c == null || c.size() == 0 )
    	{
    		c = Collections.singletonList(Long.MIN_VALUE); // FIXME hack as well.
    	}
    	return c;
    }
    
    public static boolean addDisabled(String id)
    {
    	Set<String> s = disabledSubsystemsHolder.get( );
    	if ( s == null )
    	{
    		s = new HashSet<String>();
    		disabledSubsystemsHolder.set( s );
    		return s.add(id);
    	}
    	return false;	

    }
    
    public static boolean addAllDisabled(String...ids)
    {
    	Set<String> s = disabledSubsystemsHolder.get( );
    	if ( s == null )
    	{
    		s = new HashSet<String>();
    		disabledSubsystemsHolder.set( s );
    	}
    	if (ids != null )
    	{
    		return Collections.addAll(s, ids);
    	}
    	return false;	

    }
    
    public static boolean removeDisabled(String id)
    {
    	Set<String> s = disabledSubsystemsHolder.get( );
    	if ( s != null && id != null )
    	{
    		return s.remove(id);
    	}
    	return false;	
    }
    
    public static boolean removeAllDisabled(String...ids)
    {
    	Set<String> s = disabledSubsystemsHolder.get( );
    	if ( s != null && ids != null )
    	{
    		boolean changed = false;
    		for (String string : ids) {
				changed |= s.remove(string);
			}
    	}
    	return false;	
    }
    
    public static void clearDisabled()
    {
    	disabledSubsystemsHolder.remove();
    }
    
    public static boolean isDisabled(String id)
    {
    	Set<String> s = disabledSubsystemsHolder.get( );
    	if ( s == null || id == null || ! s.contains(id)) return false;
    	return true;
    	
    }
    
    public static Set<IObject> getLockCandidates()
    {
    	Set<IObject> s = lockCandidatesHolder.get();
    	if ( s == null ) return new HashSet<IObject>();
    	return s;
    }
    
    public static void appendLockCandidates( Set<IObject> set )
    {
    	Set<IObject> s = lockCandidatesHolder.get();
    	if ( s == null ) 
    	{
    		s = new HashSet<IObject>( );
    		lockCandidatesHolder.set( s );
    	}
    	s.addAll( set );
    }
    
} 
