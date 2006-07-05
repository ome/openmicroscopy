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
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

//Third-party libraries
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jmx.support.JmxUtils;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Transactional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.jboss.security.Util;

//Application-internal dependencies
import ome.api.IAdmin;
import ome.conditions.ApiUsageException;
import ome.conditions.InternalException;
import ome.model.IObject;
import ome.model.internal.Permissions;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.security.CurrentDetails;


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
public class AdminImpl extends AbstractLevel1Service implements IAdmin {

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
    	// using Spring utilities to get MBeanServer
        MBeanServer mbeanServer = JmxUtils.locateMBeanServer();
        log.debug("Acquired MBeanServer.");
        ObjectName name;
        try
        {
        	// defined in app/resources/jboss-service.xml
            name = new ObjectName("omero:service=LoginConfig");
            mbeanServer.invoke(
            name, "flushAuthenticationCaches", new Object[]{}, new String[]{});       
            log.debug("Flushed authentication caches.");
        } catch (Exception e) {
        	InternalException ie = new InternalException(e.getMessage());
        	ie.setStackTrace(e.getStackTrace());
        	throw ie;
        }
    }

    public Experimenter getExperimenter(Long id)
    {
        // TODO Auto-generated method stub
        return null;
        
    }

    public Experimenter lookupExperimenter(final String omeName)
    {
        return (Experimenter) getHibernateTemplate().execute(new HibernateCallback(){
            public Object doInHibernate(Session session) 
            throws HibernateException ,SQLException {
                org.hibernate.Query q = session.createQuery("select e from " +
                        "Experimenter e " +
                        "left join fetch e.groupExperimenterMap m " +
                        "left join fetch m.parent g " +
                        "where e.omeName = :name");
                q.setParameter("name",omeName);
                Object o = q.uniqueResult();

                if (o == null)
                    throw new RuntimeException("No such experimenter: "
                            + omeName);

                return o; // TODO make level2 and use iQuery?
            };
        });
    }
            
    public ExperimenterGroup getGroup(Long id)
    {
        // TODO Auto-generated method stub
        return null;
        
    }

    public ExperimenterGroup lookupGroup(final String groupName)
    {
        return (ExperimenterGroup) getHibernateTemplate().execute(
                new HibernateCallback(){
            public Object doInHibernate(Session session) 
            throws HibernateException ,SQLException {
                org.hibernate.Query q = session.createQuery("select g from " +
                        "ExperimenterGroup g " +
                        "left join fetch g.groupExperimenterMap m " +
                        "left join fetch m.child user " +
                        "where g.name = :name");
                
                q.setParameter("name",groupName);
                Object o = q.uniqueResult();
                
                if (o == null)
                    throw new RuntimeException("No such experimenter: "
                            + groupName);

                return o;
            };
        });    
    }

    public Experimenter[] containedExperimenters(Long groupId)
    {
        // TODO Auto-generated method stub
        return null;
        
    }

    public ExperimenterGroup[] containedGroups(Long experimenterId)
    {
        // TODO Auto-generated method stub
        return null;
        
    }

    public Experimenter createUser(Experimenter newUser)
    {
        // TODO Auto-generated method stub
        return null;
        
    }

    public Experimenter createSystemUser(Experimenter newSystemUser)
    {
        // TODO Auto-generated method stub
        return null;
        
    }

    public Experimenter createExperimenter(Experimenter experimenter, ExperimenterGroup defaultGroup, ExperimenterGroup[] otherGroups)
    {
        // TODO Auto-generated method stub
        return null;
        
    }

    public ExperimenterGroup createGroup(ExperimenterGroup group)
    {
        // TODO Auto-generated method stub
        return null;
        
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
        int results = jdbc.update(
        		"update password set hash = ? " +
        		"where experimenter_id = ? ",
        		newPassword == null ? null : passwordDigest(newPassword),
        		CurrentDetails.getOwner().getId()	
        		); // TODO when AdminBean+AdminImpl then use EventContext.
        synchronizeLoginCache();
    }

    public void changeUserPassword(String omeName, String newPassword)
    {
    	Experimenter e = lookupExperimenter(omeName);
    	int results = jdbc.update(
        		"update password set hash = ? " +
        		"where experimenter_id = ? ",
        		newPassword == null ? null : passwordDigest(newPassword),
        		e.getId()	
        		); 
    	synchronizeLoginCache();
    }
    
    // ~ Helpers
	// =========================================================================

    protected String passwordDigest( String clearText )
    {
    	if ( clearText == null )
    	{
    		throw new ApiUsageException("Value for digesting may not be null");
    	}
    	
    	// This allows empty passwords to be considered "open-access"
    	if ( clearText.trim().length() == 0 )
    	{
    		return clearText;
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
}
				