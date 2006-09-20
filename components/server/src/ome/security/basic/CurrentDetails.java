/*
 * ome.security.basic.CurrentDetails
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

package ome.security.basic;

//Java imports
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import ome.model.meta.EventLog;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.security.basic.BasicEventContext;
import ome.system.EventContext;
import ome.system.Principal;
import ome.system.SimpleEventContext;

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
class CurrentDetails
{
    private static Log log = LogFactory.getLog(CurrentDetails.class);

    private ThreadLocal<BasicEventContext> data = 
    	new ThreadLocal<BasicEventContext>(){
    	@Override
    	protected BasicEventContext initialValue() { return new BasicEventContext(); };
    };
    
    /** removes all current context. This must stay in sync with the instance
     * fields. If a new {@link ThreadLocal} is added, {@link ThreadLocal#remove()}
     * <em>must</em> be called.
     */
    public void clear(){
    	data.remove();
    }
    
    public EventContext getCurrentEventContext()
    {
    	return data.get();
    }
    
    // ~ Internals
    // ================================================================

    protected void setDetails(Details details)
    {
        data.get().details = details;
    }

    protected Details getDetails()
    {
        Details details = data.get().details;
        if (details == null)
        {
            details = new Details();
            setDetails(details);
        }
        return details;
    }

    // ~ Events and Details
    // =================================================================
    public void newEvent(EventType type, Token token) // TODO keep up with stack here?
    {
        Event e = new Event();
        e.setType(type);
        e.setTime(new Timestamp(System.currentTimeMillis()));
        e.getGraphHolder().setToken(token, token);
        e.getDetails().setPermissions( Permissions.READ_ONLY );
        setCreationEvent(e);
    }
    
    public void addLog( String action, Class klass, Long id )
    {
    	Map<Class, Map<String, EventLog>> map = data.get().logs;
    	if ( map == null )
    	{
    		map = new HashMap<Class, Map<String, EventLog>>();
    		data.get().logs = map;
    	}
    	
    	Map<String,EventLog> m = map.get( klass );
    	if ( m == null )
    	{
    		m = new HashMap<String, EventLog>();
    		map.put( klass, m );
    	}
    	
    	EventLog l = m.get( action );
    	if ( l == null )
    	{
			l = new EventLog();
			l.setAction(action);
			l.setType(klass.getName()); // TODO could be id to Type entity
			l.setIdList(id.toString());
			l.setEvent(getCreationEvent());
			m.put( action, l );
    	} else {
    		l.setIdList( l.getIdList() + " " +id.toString() );
    	}	
    }
    
    public Map<Class,Map<String,EventLog>> getLogs()
    { // TODO defensive copy
    	return data.get().logs == null ? 
    			new HashMap<Class,Map<String,EventLog>>() : 
    				data.get().logs;
    }
    
    public void clearLogs()
    {
    	data.get().logs = null;
//        getCreationEvent().clearLogs();
    }
    
    // TODO move to BSS
    public Details createDetails()
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
    public Permissions getUmask()
    {
        Permissions umask = data.get().umask;
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
    
    public void setUmask(Permissions umask)
    {
        data.get().umask = umask;
    }
    
    // ~ Delegation FIXME possibly remove setters for set(Exp,Grp)
    // =========================================================================
    
    public Event getCreationEvent()
    {
        return getDetails().getCreationEvent();
    }

    public Experimenter getOwner()
    {
        return getDetails().getOwner();
    }

    public Permissions getPermissions()
    {
        return getDetails().getPermissions();
    }

    public Event getUpdateEvent()
    {
        return getDetails().getUpdateEvent();
    }

    public void setCreationEvent(Event e)
    {
        getDetails().setCreationEvent(e);
    }

    public void setOwner(Experimenter exp)
    {
        getDetails().setOwner(exp);
    }

    public void setPermissions(Permissions perms)
    {
        getDetails().setPermissions(perms);
    }

    // TODO hide these specifics. possibly also Owner->User & CreationEvent -> Event
    public void setUpdateEvent(Event e)
    {
        getDetails().setUpdateEvent(e);
    }
    
    public ExperimenterGroup getGroup()
    {
        return getDetails().getGroup();
    }
    
    public void setGroup(ExperimenterGroup group)
    {
        getDetails().setGroup(group);
    }
    
    // ~ Admin
	// =========================================================================
 
    public void setAdmin( boolean isAdmin )
    {
    	data.get().isAdmin = isAdmin;
    }
    
    public boolean isAdmin( )
    {
    	return data.get().isAdmin;
    }
    
    // ~ ReadOnly
	// =========================================================================
 
    public void setReadOnly( boolean isReadOnly )
    {
    	data.get().isReadOnly = isReadOnly;
    }
    
    public boolean isReadOnly( )
    {
    	return data.get().isReadOnly;
    }
        
    // ~ Groups
	// =========================================================================
    
    public void setMemberOfGroups( Collection<Long> groupIds )
    {
    	data.get().memberOfGroups = groupIds;
    }
    
    public Collection<Long> getMemberOfGroups( )
    {
		Collection<Long> c = data.get().memberOfGroups;    	
    	if ( c == null || c.size() == 0 )
    	{
    		c = Collections.singletonList(Long.MIN_VALUE); // FIXME hack as well.
    	}
    	return c;
    }
    
    public void setLeaderOfGroups( Collection<Long> groupIds )
    {
    	data.get().leaderOfGroups = groupIds;
    }
    
    public Collection<Long> getLeaderOfGroups( )
    {
		Collection<Long> c = data.get().leaderOfGroups;    	
    	if ( c == null || c.size() == 0 )
    	{
    		c = Collections.singletonList(Long.MIN_VALUE); // FIXME hack as well.
    	}
    	return c;
    }
    
    // ~ Subsystems
	// =========================================================================
    
    public boolean addDisabled(String id)
    {
    	Set<String> s = data.get().disabledSubsystems;
    	if ( s == null )
    	{
    		s = new HashSet<String>();
    		data.get().disabledSubsystems = s;
    		return s.add(id);
    	}
    	return false;	

    }
    
    public boolean addAllDisabled(String...ids)
    {
    	Set<String> s = data.get().disabledSubsystems;
    	if ( s == null )
    	{
    		s = new HashSet<String>();
    		data.get().disabledSubsystems = s;
    	}
    	if (ids != null )
    	{
    		return Collections.addAll(s, ids);
    	}
    	return false;	

    }
    
    public boolean removeDisabled(String id)
    {
    	Set<String> s = data.get().disabledSubsystems;
    	if ( s != null && id != null )
    	{
    		return s.remove(id);
    	}
    	return false;	
    }
    
    public boolean removeAllDisabled(String...ids)
    {
    	Set<String> s = data.get().disabledSubsystems;
    	if ( s != null && ids != null )
    	{
    		boolean changed = false;
    		for (String string : ids) {
				changed |= s.remove(string);
			}
    	}
    	return false;	
    }
    
    public void clearDisabled()
    {
    	data.get().disabledSubsystems = null;
    }
    
    public boolean isDisabled(String id)
    {
    	Set<String> s = data.get().disabledSubsystems;
    	if ( s == null || id == null || ! s.contains(id)) return false;
    	return true;
    	
    }
    
    // ~ Locks
	// =========================================================================
    
    public Set<IObject> getLockCandidates()
    {
    	Set<IObject> s = data.get().lockCandidates;
    	if ( s == null ) return new HashSet<IObject>();
    	return s;
    }
    
    public void appendLockCandidates( Set<IObject> set )
    {
    	Set<IObject> s = data.get().lockCandidates;
    	if ( s == null ) 
    	{
    		s = new HashSet<IObject>( );
    		data.get().lockCandidates = s;
    	}
    	s.addAll( set );
    }

    
} 
