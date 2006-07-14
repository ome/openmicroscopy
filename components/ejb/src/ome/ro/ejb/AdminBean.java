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
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

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

    @PostConstruct
    public void create()
    {
        super.create();
        delegate = serviceFactory.getAdminService();
    }
    
    @AroundInvoke
    public Object invoke( InvocationContext context ) throws Exception
    {
        return wrap( context, IAdmin.class );
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

    @RolesAllowed("user")
    public Experimenter getExperimenter(Long arg0)
    {
    	return delegate.getExperimenter(arg0);
    }

    @RolesAllowed("user")
    public Experimenter lookupExperimenter(String arg0)
    {
    	return delegate.lookupExperimenter(arg0);
    }

    @RolesAllowed("user")
    public ExperimenterGroup getGroup(Long arg0)
    {
    	return delegate.getGroup(arg0);
    }

    @RolesAllowed("user")
    public ExperimenterGroup lookupGroup(String arg0)
    {
    	return delegate.lookupGroup(arg0);
    }

    @RolesAllowed("user")
    public Experimenter[] containedExperimenters(Long arg0)
    {
    	return delegate.containedExperimenters(arg0);
    }

    @RolesAllowed("user")
    public ExperimenterGroup[] containedGroups(Long arg0)
    {
    	return delegate.containedGroups(arg0);
    }

    @RolesAllowed("system")
    public Experimenter createUser(Experimenter arg0)
    {
    	return delegate.createUser(arg0);
    }

    @RolesAllowed("system")
    public Experimenter createSystemUser(Experimenter arg0)
    {
    	return delegate.createSystemUser(arg0);
    }

    @RolesAllowed("system")
    public Experimenter createExperimenter(Experimenter arg0, ExperimenterGroup arg1, ExperimenterGroup[] arg2)
    {
    	return delegate.createExperimenter(arg0, arg1, arg2);
    }

    @RolesAllowed("user")
    public ExperimenterGroup createGroup(ExperimenterGroup arg0)
    {
    	return delegate.createGroup(arg0);
    }

    @RolesAllowed("system")
    public void addGroups(Experimenter arg0, ExperimenterGroup[] arg1)
    {
	    delegate.addGroups(arg0, arg1);    
    }

    @RolesAllowed("sytem")
    public void removeGroups(Experimenter arg0, ExperimenterGroup[] arg1)
    {
    	delegate.removeGroups(arg0, arg1);
    }

    @RolesAllowed("system")
    public void setDefaultGroup(Experimenter arg0, ExperimenterGroup arg1)
    {
    	delegate.setDefaultGroup(arg0, arg1);
    }

    @RolesAllowed("system")
	public void deleteExperimenter(Experimenter user)
    {
    	delegate.deleteExperimenter(user);
    }
    
    @RolesAllowed("system")
    public void changeOwner(IObject arg0, String arg1)
    {
    	delegate.changeOwner(arg0, arg1);
    }

    @RolesAllowed("system")
    public void changeGroup(IObject arg0, String arg1)
    {
    	delegate.changeGroup(arg0, arg1);
    }

    @RolesAllowed("system")
    public void changePermissions(IObject arg0, Permissions arg1)
    {
    	delegate.changePermissions(arg0, arg1);   
    }

    @RolesAllowed("user")
    public void changePassword(String arg0)
    {

    	delegate.changePassword(arg0);    
    }

    @RolesAllowed("system")
    public void changeUserPassword(String arg0, String arg1)
    {
    	delegate.changeUserPassword(arg0, arg1);
    }

}
