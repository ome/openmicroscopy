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
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

//Third-party libraries
import org.springframework.jmx.support.JmxUtils;
import org.springframework.transaction.annotation.Transactional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//Application-internal dependencies
import ome.api.IAdmin;
import ome.model.IObject;
import ome.model.internal.Permissions;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;

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

    @Override
    protected String getName()
    {
        return IAdmin.class.getName();
    }
    
    // ~ LOCAL PUBLIC METHODS
    // =========================================================================

    
    // ~ INTERFACE METHODS
    // =========================================================================

    public void synchronizeLoginCache()
    {

        MBeanServer mbeanServer = JmxUtils.locateMBeanServer();
        String[] params = { "jmx-console" };
        String[] signature = { "java.lang.String" };

        ObjectName name;
        try
        {
            name = new ObjectName(
                    "jboss.security:service=JaasSecurityManager");
            mbeanServer.invoke(
                    name, "flushAuthenticationCache", params, signature);
        } catch (MalformedObjectNameException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new RuntimeException("Unimplemented exception.");
        } catch (NullPointerException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new RuntimeException("Unimplemented exception.");
        } catch (InstanceNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new RuntimeException("Unimplemented exception.");
        } catch (MBeanException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new RuntimeException("Unimplemented exception.");
        } catch (ReflectionException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new RuntimeException("Unimplemented exception.");
        }
        
        
    }

    public Experimenter getExperimenter(Long id)
    {
        // TODO Auto-generated method stub
        return null;
        
    }

    public Experimenter lookupExperimenter(String omeName)
    {
        // TODO Auto-generated method stub
        return null;
        
    }

    public ExperimenterGroup getGroup(Long id)
    {
        // TODO Auto-generated method stub
        return null;
        
    }

    public ExperimenterGroup lookupGroup(String groupName)
    {
        // TODO Auto-generated method stub
        return null;
        
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
        // TODO Auto-generated method stub
        
    }

    public void changeUserPassword(String omeName, String newPassword)
    {
        // TODO Auto-generated method stub
        
    }

}
				