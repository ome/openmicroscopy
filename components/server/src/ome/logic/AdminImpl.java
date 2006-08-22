/*
 * ome.logic.AdminImpl
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

package ome.logic;

//Java imports
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

//Third-party libraries
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jmx.support.JmxUtils;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.security.Util;

//Application-internal dependencies
import ome.annotations.NotNull;
import ome.api.IAdmin;
import ome.api.ServiceInterface;
import ome.api.local.LocalAdmin;
import ome.api.local.LocalUpdate;
import ome.conditions.ApiUsageException;
import ome.conditions.InternalException;
import ome.conditions.ValidationException;
import ome.model.IObject;
import ome.model.internal.Permissions;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.GroupExperimenterMap;
import ome.parameters.Filter;
import ome.parameters.Parameters;
import ome.security.AdminAction;
import ome.security.SecureAction;
import ome.services.query.Definitions;
import ome.services.query.Query;
import ome.services.query.QueryParameterDef;


/**  Provides methods for directly querying object graphs.
 * 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 3.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 3.0
 * 
 */
@Transactional
public class AdminImpl extends AbstractLevel2Service implements LocalAdmin {

    private static Log log = LogFactory.getLog(AdminImpl.class);

    protected SimpleJdbcTemplate jdbc;
    
    @Override
    protected Class<? extends ServiceInterface> getServiceInterface()
    {
        return IAdmin.class;
    }
    
    public void setJdbcTemplate( SimpleJdbcTemplate jdbcTemplate )
    {
    	jdbc = jdbcTemplate;
    }
    
    // ~ LOCAL PUBLIC METHODS
    // =========================================================================
    public Experimenter userProxy(final Long id)
    {
    	Experimenter e = iQuery.get(Experimenter.class, id);
    	return e;
    }

    public Experimenter userProxy(final String omeName)
    {
    	Experimenter e = iQuery.findByString(Experimenter.class,"omeName",omeName);
   
    	if (e == null) 
    	{
    		throw new ApiUsageException("No such experimenter: " + omeName);
    	}

    	return e;
    }
            
    public ExperimenterGroup groupProxy(Long id)
    {
    	ExperimenterGroup g = iQuery.get(ExperimenterGroup.class,id);
    	return g;
    }

    public ExperimenterGroup groupProxy(final String groupName)
    {
    	ExperimenterGroup g = iQuery.findByString(
    			ExperimenterGroup.class,"name",groupName);

    	if (g == null) 
    	{
    		throw new ApiUsageException("No such group: " + groupName);
    	}

    	return g;
    }
    
    public List<Long> getLeaderOfGroupIds( final Experimenter e )
    {
    	Assert.notNull(e);
    	Assert.notNull(e.getId());
    	
    	List<Long> groupIds = iQuery.execute(new HibernateCallback(){
        	public Object doInHibernate(Session session) 
        	throws HibernateException, SQLException {
        		org.hibernate.Query q = session.createQuery(
        		"select g.id from ExperimenterGroup g where g.details.owner.id = :id");
        		q.setParameter("id", e.getId());
        		return q.list();
        	}
        });
    	return groupIds;
    }
    
    // ~ INTERFACE METHODS
    // =========================================================================

    public void synchronizeLoginCache()
    {
    	String string = "omero:service=LoginConfig";
    	// using Spring utilities to get MBeanServer
        MBeanServer mbeanServer = JmxUtils.locateMBeanServer();
        log.debug("Acquired MBeanServer.");
        ObjectName name;
        try
        {
        	// defined in app/resources/jboss-service.xml
            name = new ObjectName(string);
            mbeanServer.invoke(
            name, "flushAuthenticationCaches", new Object[]{}, new String[]{});       
            log.debug("Flushed authentication caches.");
        } catch (InstanceNotFoundException infe) {
        	log.warn(string+" not found. Won't synchronize login cache.");
        } catch (Exception e) {
        	InternalException ie = new InternalException(e.getMessage());
        	ie.setStackTrace(e.getStackTrace());
        	throw ie;
        }
    }

    public Experimenter getExperimenter(final Long id)
    {
    	Experimenter e = iQuery.execute(
    			new UserQ( new Parameters( ).addId(id)));
   
    	if (e == null) 
    	{
    		throw new ApiUsageException("No such experimenter: " + id);
    	}

    	return e;
    }

    public Experimenter lookupExperimenter(final String omeName)
    {
    	Experimenter e = iQuery.execute(
    			new UserQ( new Parameters( )
    			.addString("name",omeName)));
   
    	if (e == null) 
    	{
    		throw new ApiUsageException("No such experimenter: " + omeName);
    	}

    	return e;
    }
            
    public ExperimenterGroup getGroup(Long id)
    {
    	ExperimenterGroup g = iQuery.execute(
    			new GroupQ( new Parameters( ).addId(id)));
   
    	if (g == null) 
    	{
    		throw new ApiUsageException("No such group: " + id);
    	}

    	return g;
    }

    public ExperimenterGroup lookupGroup(final String groupName)
    {
    	ExperimenterGroup g = iQuery.execute(
    			new GroupQ( new Parameters( ).addString("name",groupName)));
   
    	if (g == null) 
    	{
    		throw new ApiUsageException("No such group: " + groupName);
    	}

    	return g;
    }

    public Experimenter[] containedExperimenters(Long groupId)
    {
    	ExperimenterGroup g = iQuery.execute(
    			new GroupQ( new Parameters( ).addId(groupId)));
    	
    	if (g == null)
    	{
    		throw new ApiUsageException("No such group: "+groupId);
    	}
    	
    	int count = g.sizeOfGroupExperimenterMap();
    	if ( count < 1 )
    	{
    		return new Experimenter[]{};
    	}
    	
    	return (Experimenter[]) g.linkedExperimenterList()
    		.toArray(new Experimenter[count]);
    }

    public ExperimenterGroup[] containedGroups(Long experimenterId)
    {
    	Experimenter e = iQuery.execute(
    			new UserQ( new Parameters( ).addId(experimenterId)));
    	if (e == null)
    	{
    		throw new ApiUsageException("No such user: "+experimenterId);
    	}
    	
    	int count = e.sizeOfGroupExperimenterMap();
    	if ( count < 1 )
    	{
    		return new ExperimenterGroup[]{};
    	}
    	
    	return (ExperimenterGroup[]) e.linkedExperimenterGroupList()
    		.toArray(new ExperimenterGroup[count]);
    }

    public long createUser(Experimenter newUser)
    {
    	return createExperimenter(
    			newUser, groupProxy("user"),null);
    }

    public long createSystemUser(Experimenter newSystemUser)
    {
    	return createExperimenter(
    			newSystemUser, 
    			groupProxy("system"), 
    			new ExperimenterGroup[]{ groupProxy("user") } );
    }

    public long createExperimenter(
    		Experimenter experimenter, 
    		ExperimenterGroup defaultGroup, 
    		ExperimenterGroup[] otherGroups)
    {
//    	 TODO check that no other group is default
	
    	Experimenter e = copyUser( experimenter );
    	    	
    	if ( defaultGroup == null || defaultGroup.getId() == null )
    	{
    		throw new ApiUsageException("Default group may not be null.");
    	} 

    	SecureAction action = new SecureAction(){
    		public <T extends IObject> T updateObject(T obj) {
    			return iUpdate.saveAndReturnObject( obj );
    		}
    	};
    	
    	e = securitySystem.doAction(e, action);
    	
    	final GroupExperimenterMap defaultGroupMap = new GroupExperimenterMap();
		defaultGroupMap.link( groupProxy(defaultGroup.getId()), userProxy(e.getId()));
		defaultGroupMap.setDefaultGroupLink(Boolean.TRUE);
		defaultGroupMap.setDetails( securitySystem.transientDetails( defaultGroupMap ));
		securitySystem.doAction(defaultGroupMap,action);
		
    	if ( null != otherGroups )
    	{
    		for (ExperimenterGroup group : otherGroups) {
    			if ( group == null ) continue;
    			if ( group.getId() == null)
    			{
    				throw new ApiUsageException(
    						"Groups must be previously saved during " +
    						"Experimenter creation.");
    			}
    			GroupExperimenterMap groupMap = new GroupExperimenterMap();
    			groupMap.link( groupProxy(group.getId()), userProxy(e.getId()));
    			groupMap.setDefaultGroupLink(Boolean.FALSE);
    			groupMap.setDetails( securitySystem.transientDetails( groupMap ));
    			securitySystem.doAction(groupMap, action);
    		}
    	}
    	
    	changeUserPassword(e.getOmeName()," ");
    	return e.getId();
    }

    public long createGroup(ExperimenterGroup group)
    {
    	group = copyGroup( group );
    	ExperimenterGroup g = securitySystem.doAction(group, new SecureAction(){
    		public <T extends IObject> T updateObject(T obj) {
    			return iUpdate.saveAndReturnObject( obj );
    		}
    	});
    	return g.getId();
    }

    public void addGroups(Experimenter user, ExperimenterGroup... groups)
    {
    	if (user == null) return; // Handled by annotations
    	if (groups == null) return;
    	
        Experimenter foundUser = userProxy(user.getId());
    	for (ExperimenterGroup group : groups) {
        	ExperimenterGroup foundGroup = 
        		groupProxy(group.getId());
        	GroupExperimenterMap map = new GroupExperimenterMap();
        	map.link(foundGroup,foundUser);
        	map.setDetails( securitySystem.transientDetails(map));
        	securitySystem.doAction(map,new SecureAction(){
        		public <T extends IObject> T updateObject(T obj) {
        			return iUpdate.saveAndReturnObject( obj );
        		}
        	});
		}
    	iUpdate.flush();
    }

    public void removeGroups(Experimenter user, ExperimenterGroup... groups)
    {
    	if (user == null) return;
    	if (groups == null) return;
    	
        Experimenter foundUser = getExperimenter(user.getId());
        List<Long> toRemove = new ArrayList<Long>();
        
        for (ExperimenterGroup g : groups) {
        	if (g.getId()!=null) toRemove.add(g.getId());
		}
        for (GroupExperimenterMap map : (List<GroupExperimenterMap>)
        		foundUser.collectGroupExperimenterMap(null)) 
        {
			if (toRemove.contains(map.parent().getId()))
			{
				map.child().removeGroupExperimenterMap(map, false);
				map.parent().removeGroupExperimenterMap(map, false);
	        	securitySystem.doAction(map,new SecureAction(){
	        		public <T extends IObject> T updateObject(T obj) {
	        			iUpdate.deleteObject( obj );
	        			return null;
	        		}
	        	});
			}
        }
        iUpdate.flush();
    }

    public void setDefaultGroup(Experimenter user, ExperimenterGroup group)
    {
        if (user == null) return;
        if (group == null) return;
        
        if (group.getId() == null) 
        {
        	throw new ApiUsageException("Group argument to setDefaultGroup " +
        			"must be managed (i.e. have an id)");
        }
        
        boolean newDefaultSet = false;
        Experimenter foundUser = getExperimenter(user.getId());
        for (GroupExperimenterMap map : (List<GroupExperimenterMap>)
        		foundUser.collectGroupExperimenterMap(null))
        {
        	if (map.parent().getId().equals(group.getId()))
        	{
        		map.setDefaultGroupLink(Boolean.TRUE);
        		newDefaultSet = true;
        	}
        	else 
        	{
        		map.setDefaultGroupLink(Boolean.FALSE);
        	}
		}
        
        if ( ! newDefaultSet)
        {
        	throw new ApiUsageException("Group "+group.getId()+" was not " +
        			"found for user "+user.getId());
        }
        
        iUpdate.flush();
    }

    public ExperimenterGroup getDefaultGroup( @NotNull Long experimenterId )
    {
    	ExperimenterGroup g = 
    	iQuery.findByQuery(
    			"select g from ExperimenterGroup g " +
    			"join fetch g.groupExperimenterMap m " +
    			"join fetch m.child  e " +
    			"where e.id = :id " +
    			"and m.defaultGroupLink = true",
    			new Parameters().addId(experimenterId));
    	if (g==null)
    	{
    		throw new ValidationException(
    				"The user "+experimenterId+" has no default group set.");
    	}
    	return g;
    }
        
    public void deleteExperimenter( Experimenter user )
    {
    	Experimenter e = userProxy(user.getId());
    	int count = 
    		jdbc.update("delete from password where experimenter_id = ?", e.getId());
    	
    	if ( count == 0 )
    	{
    		log.info("No password found for user "
    				+e.getOmeName()
    				+". Cannot delete.");
    	}
    	
    	iUpdate.deleteObject(e);
    }
    
    // ~ chown / chgrp / chmod
	// =========================================================================

    public void changeOwner(IObject iObject, String omeName)
    {
    	// should take an Owner
    	IObject copy = iQuery.get(iObject.getClass(), iObject.getId());
    	Experimenter owner = userProxy(omeName);
    	copy.getDetails().setOwner(owner);
    	iUpdate.saveObject(copy);
    }

    public void changeGroup(IObject iObject, String groupName)
    {
    	final LocalUpdate update = iUpdate;
    	// should take a group
    	final IObject copy = iQuery.get(iObject.getClass(), iObject.getId());
    	ExperimenterGroup group = groupProxy(groupName);
    	copy.getDetails().setGroup(group);
    	securitySystem.doAction(copy, new SecureAction(){
    		public IObject updateObject(IObject obj)
    		{
    			update.flush(); return null;
    		}
    	});
    }

    public void changePermissions(final IObject iObject, final Permissions perms)
    {
    	AdminAction action = new AdminAction(){
    		public void runAsAdmin() {
    	    	IObject copy = iQuery.get(iObject.getClass(), iObject.getId());
    	    	Permissions p = new Permissions(perms); // FIXME ticket:215
    	    	copy.getDetails().setPermissions(p);
    	    	iUpdate.saveObject(copy);    
    		}
    	};
    	securitySystem.runAsAdmin(action);
    }

    // ~ Passwords
	// =========================================================================
    
    public void changePassword(String newPassword)
    {
    	internalChangeUserPasswordById(
    			//  TODO when AdminBean+AdminImpl then use EventContext.
    			securitySystem.currentUserId(),
    			newPassword);
    }

    public void changeUserPassword(String omeName, String newPassword)
    {
    	Experimenter e = lookupExperimenter(omeName);
    	internalChangeUserPasswordById(e.getId(),newPassword);
    }
    
    // ~ Helpers
	// =========================================================================

    protected Experimenter copyUser(Experimenter e)
    {
    	if ( e.getOmeName() == null )
    	{
    		throw new ValidationException("OmeName may not be null.");
    	}
    	Experimenter copy = new Experimenter();
    	copy.setOmeName( e.getOmeName() );
    	copy.setFirstName( e.getOmeName() );
    	copy.setLastName( e.getLastName() );
    	copy.setEmail( e.getEmail() );
    	if (e.getDetails()!=null && e.getDetails().getPermissions()!=null)
    	{
    		copy.getDetails().setPermissions(e.getDetails().getPermissions());
    	}
    		// TODO make ShallowCopy-like which ignores collections and details.
    	// if possible, values should be validated. i.e. iTypes should say what
    	// is non-null
    	return copy;
    }
    
    protected ExperimenterGroup copyGroup(ExperimenterGroup g)
    {
    	if ( g.getName() == null )
    	{
    		throw new ValidationException("Group name may not be null.");
    	}
    	ExperimenterGroup copy = new ExperimenterGroup();
    	copy.setDescription( g.getDescription() );
    	copy.setName( g.getName() );
    	copy.setDetails( securitySystem.transientDetails(g));
    	// TODO see shallow copy comment on copy user
    	return copy;
    }
    
    // ~ Password access
	// =========================================================================
    
    protected void internalChangeUserPasswordById(Long id, String password)
    {
    	int results = jdbc.update(
        		"update password set hash = ? " +
        		"where experimenter_id = ? ",
        		preparePassword(password),id	
        		); 
    	if ( results < 1 )
    	{
    		results = jdbc.update("insert into password values (?,?) ",
    				id,preparePassword(password));
    	}
    	synchronizeLoginCache();    	
    }
    
	protected String preparePassword(String newPassword) {
		// This allows setting passwords to "null" - locked account.
		return newPassword == null ? null 
		    	// This allows empty passwords to be considered "open-access"
				: newPassword.trim().length() == 0 ? newPassword
						// Regular MD5 digest.
						: passwordDigest(newPassword);
	}
    
    protected String passwordDigest( String clearText )
    {
    	if ( clearText == null )
    	{
    		throw new ApiUsageException("Value for digesting may not be null");
    	}
    	
    	// These constants are also defined in app/resources/jboss-login.xml
    	// and this method is called from {@link JBossLoginModule}
    	String hashedText = Util
    	.createPasswordHash("MD5", "base64", "ISO-8859-1", null, clearText, null);
    	
		if ( hashedText == null )
		{
			throw new InternalException("Failed to obtain digest.");
		}
		return hashedText;
    }
    
    // ~ Queries for pulling full experimenter/experimenter group graphs
	// =========================================================================
    
    static abstract class BaseQ<T> extends Query<T>
    {
    	static Definitions defs = new Definitions(
    			new QueryParameterDef("name",String.class,true),
    			new QueryParameterDef("id",Long.class,true)
    			);
    	
    	public BaseQ(Parameters params)
    	{
    		super(defs,new Parameters( new Filter().unique() ).addAll( params ));
    	}
    	    	
    }
    
    static class UserQ extends BaseQ<Experimenter> 
    {
    	public UserQ(Parameters params)
    	{
    		super(params);
    	}
    	
    	@Override
    	protected void buildQuery(Session session) 
    	throws HibernateException, SQLException {
    		Criteria c = session.createCriteria(Experimenter.class);
    		
    		Criteria m = c.createCriteria("groupExperimenterMap",Query.LEFT_JOIN);
    		Criteria g = m.createCriteria("parent",Query.LEFT_JOIN);
    		
    		if (value("name") != null)
    		{
    			c.add( Restrictions.eq("omeName", value("name")));
    		} 
    		
    		else if (value("id") != null)
    		{
    			c.add( Restrictions.eq("id", value("id")));
    		}
    		
    		else 
    		{
    			throw new InternalException(
    					"Name and id are both null for user query.");
    		}
    		setCriteria( c );
    		
    	}
    }
    
    static class GroupQ extends BaseQ<ExperimenterGroup> 
    {
    	public GroupQ(Parameters params)
    	{
    		super(params);
    	}
    	
    	@Override
    	protected void buildQuery(Session session) 
    	throws HibernateException, SQLException {
    		Criteria c = session.createCriteria(ExperimenterGroup.class);    		
    		Criteria m = c.createCriteria("groupExperimenterMap",Query.LEFT_JOIN);
    		Criteria e = m.createCriteria("child",Query.LEFT_JOIN);
    		
    		if (value("name") != null)
    		{
    			c.add( Restrictions.eq("name", value("name")));
    		} 
    		
    		else if (value("id") != null)
    		{
    			c.add( Restrictions.eq("id", value("id")));
    		}

    		else 
    		{
    			throw new InternalException(
    					"Name and id are both null for group query.");
    		}
    		setCriteria( c );
    		
    	}
    }
}
				