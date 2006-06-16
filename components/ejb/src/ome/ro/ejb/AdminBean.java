/* ome.ro.ejb.AdminBean
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

package ome.ro.ejb;

//Java imports
import javax.annotation.PreDestroy;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;

//Third-party imports
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.security.SecurityDomain;

//Application-internal dependencies
import ome.api.IAdmin;
import ome.model.IObject;
import ome.model.internal.Permissions;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;

@Stateless
@Remote(IAdmin.class)
@RemoteBinding (jndiBinding="omero/remote/ome.api.IAdmin")
@Local(IAdmin.class)
@LocalBinding (jndiBinding="omero/local/ome.api.IAdmin")
@SecurityDomain("OmeroSecurity")
public class AdminBean extends AbstractBean implements IAdmin
{

    IAdmin delegate;
    
    public AdminBean(){
        super();
        delegate = (IAdmin) applicationContext.getBean("adminService");
    }
    
    @PreDestroy
    public void destroy()
    {
        delegate = null;
        super.destroy();
    }

    // ~ DELEGATION
    // =========================================================================
    
    @RolesAllowed("system")
    public void synchronizeLoginCache()
    {
        delegate.synchronizeLoginCache();
    }

    public Experimenter getExperimenter(Long arg0)
    {
        // TODO Auto-generated method stub
        return null;
        
    }

    public Experimenter lookupExperimenter(String arg0)
    {
        // TODO Auto-generated method stub
        return null;
        
    }

    public ExperimenterGroup getGroup(Long arg0)
    {
        // TODO Auto-generated method stub
        return null;
        
    }

    public ExperimenterGroup lookupGroup(String arg0)
    {
        // TODO Auto-generated method stub
        return null;
        
    }

    public Experimenter[] containedExperimenters(Long arg0)
    {
        // TODO Auto-generated method stub
        return null;
        
    }

    public ExperimenterGroup[] containedGroups(Long arg0)
    {
        // TODO Auto-generated method stub
        return null;
        
    }

    public Experimenter createUser(Experimenter arg0)
    {
        // TODO Auto-generated method stub
        return null;
        
    }

    public Experimenter createSystemUser(Experimenter arg0)
    {
        // TODO Auto-generated method stub
        return null;
        
    }

    public Experimenter createExperimenter(Experimenter arg0, ExperimenterGroup arg1, ExperimenterGroup[] arg2)
    {
        // TODO Auto-generated method stub
        return null;
        
    }

    public ExperimenterGroup createGroup(ExperimenterGroup arg0)
    {
        // TODO Auto-generated method stub
        return null;
        
    }

    public void addGroups(Experimenter arg0, ExperimenterGroup[] arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void removeGroups(Experimenter arg0, ExperimenterGroup[] arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void setDefaultGroup(Experimenter arg0, ExperimenterGroup arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void changeOwner(IObject arg0, String arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void changeGroup(IObject arg0, String arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void changePermissions(IObject arg0, Permissions arg1)
    {
        // TODO Auto-generated method stub
        
    }

    public void changePassword(String arg0)
    {
        // TODO Auto-generated method stub
        
    }

    public void changeUserPassword(String arg0, String arg1)
    {
        // TODO Auto-generated method stub
        
    }


}
