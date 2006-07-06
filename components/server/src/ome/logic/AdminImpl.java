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

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

//Third-party libraries
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jmx.support.JmxUtils;
import org.springframework.transaction.annotation.Transactional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.security.Util;

//Application-internal dependencies
import ome.api.IAdmin;
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
import ome.security.CurrentDetails;
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
public class AdminImpl extends AbstractLevel2Service implements IAdmin {

    private static Log log = LogFactory.getLog(AdminImpl.class);

    protected SimpleJdbcTemplate jdbc;
    
    @Override
    protected String getName()
    {
        return IAdmin.class.getName();
    }
    
    public void setJdbcTemplate( SimpleJdbcTemplate jdbcTemplate )
    {
    	jdbc = jdbcTemplate;
    }
    
    // ~ LOCAL PUBLIC METHODS
    // =========================================================================

    
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
    	return null;
    }

    public ExperimenterGroup[] containedGroups(Long experimenterId)
    {
        // TODO Auto-generated method stub
        return null;
        
    }

    public Experimenter createUser(Experimenter newUser)
    {
    	Experimenter e = createExperimenter(newUser, lookupGroup("user"),null);
    	return e;
    }

    public Experimenter createSystemUser(Experimenter newSystemUser)
    {
    	Experimenter e = createExperimenter(
    			newSystemUser, 
    			lookupGroup("system"), 
    			new ExperimenterGroup[]{ lookupGroup("user") } );
    	return e;
    }

    public Experimenter createExperimenter(Experimenter experimenter, ExperimenterGroup defaultGroup, ExperimenterGroup[] otherGroups)
    {
//    	 TODO check that no other group is default
	
    	Experimenter e = copyUser( experimenter );
    	    	
    	if ( defaultGroup == null || defaultGroup.getId() == null )
    	{
    		throw new ApiUsageException("Default group may not be null.");
    	} 

    	GroupExperimenterMap defaultGroupMap = new GroupExperimenterMap();
		defaultGroupMap.link( getGroup(defaultGroup.getId()), e);
		defaultGroupMap.setDefaultGroupLink(Boolean.TRUE);
		e.addGroupExperimenterMap(defaultGroupMap, false);

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
    			groupMap.link( getGroup(group.getId()), e);
    			e.addGroupExperimenterMap(groupMap, false);
    		}
    	}
    	
    	e = iUpdate.saveAndReturnObject( e );
    	changeUserPassword(e.getOmeName()," ");
    	return lookupExperimenter(e.getOmeName());    
    }

    public ExperimenterGroup createGroup(ExperimenterGroup group)
    {
    	group = copyGroup( group );
    	ExperimenterGroup g = iUpdate.saveAndReturnObject( group );
    	return g;
    }

    public void addGroups(Experimenter user, ExperimenterGroup[] groups)
    {
        // TODO Auto-generated method stub
        
    }

    public void removeGroups(Experimenter user, ExperimenterGroup[] groups)
    {
        // TODO Auto-generated method stub
        
    }

    public void setDefaultGroup(Experimenter user, ExperimenterGroup group)
    {
        // TODO Auto-generated method stub
        
    }

    public void changeOwner(IObject iObject, String omeName)
    {
        // TODO Auto-generated method stub
        
    }

    public void changeGroup(IObject iObject, String groupName)
    {
        // TODO Auto-generated method stub
        
    }

    public void changePermissions(IObject iObject, Permissions perms)
    {
        // TODO Auto-generated method stub
        
    }

    public void changePassword(String newPassword)
    {
    	internalChangeUserPasswordById(
    			//  TODO when AdminBean+AdminImpl then use EventContext.
    			CurrentDetails.getOwner().getId(),
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
    	// TODO make ShallowCopy-like which ignores collections and details.
    	// if possible, values should be validated. i.e. iTypes should say what
    	// is non-null
    	return copy;
    }
    
    protected ExperimenterGroup copyGroup(ExperimenterGroup g)
    {
    	if ( g.getName() == null )
    	{
    		throw new ValidationException("OmeName may not be null.");
    	}
    	ExperimenterGroup copy = new ExperimenterGroup();
    	copy.setDescription( g.getDescription() );
    	copy.setName( g.getName() );
    	// TODO see shallow copy comment on copy user
    	return copy;
    }
    
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
				