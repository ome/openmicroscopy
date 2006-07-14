/* ome.ro.ejb.UpdateBean
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
import java.util.Collection;
import java.util.Map;

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
import ome.api.IUpdate;
import ome.api.local.LocalUpdate;
import ome.model.IObject;

@Stateless
@Remote(IUpdate.class)
@RemoteBinding (jndiBinding="omero/remote/ome.api.IUpdate")
@Local(LocalUpdate.class)
@LocalBinding (jndiBinding="omero/local/ome.api.local.LocalUpdate")
@SecurityDomain("OmeroSecurity")
public class UpdateBean extends AbstractBean implements LocalUpdate
{

    @PostConstruct
    public void create()
    {
        super.create();
    }
    
    @AroundInvoke
    public Object invoke( InvocationContext context ) throws Exception
    {
        return wrap( context, IUpdate.class );
    }
    
    @PreDestroy
    public void destroy()
    {
        localUpdate = null;
        super.destroy();
    }
    
    // ~ DELEGATION
    // =========================================================================
    
    @RolesAllowed("user")
    public void saveArray(IObject[] arg0)
    {
        localUpdate.saveArray(arg0);
    }

    @RolesAllowed("user")
    public IObject[] saveAndReturnArray(IObject[] graph)
    {
        return localUpdate.saveAndReturnArray(graph);
    }

    @RolesAllowed("user")
    public Collection saveAndReturnCollection(Collection graph)
    {
        return localUpdate.saveAndReturnCollection(graph);
    }

    @RolesAllowed("user")
    public Map saveAndReturnMap(Map map)
    {
        return localUpdate.saveAndReturnMap(map);
    }

    @RolesAllowed("user")
    public IObject saveAndReturnObject(IObject graph)
    {
        return localUpdate.saveAndReturnObject(graph);
    }

    @RolesAllowed("user")
    public void saveObject(IObject graph)
    {
        localUpdate.saveObject(graph);
    }

    @RolesAllowed("user")
    public void saveCollection(Collection arg0)
    {
        localUpdate.saveCollection(arg0);
    }

    @RolesAllowed("user")
    public void saveMap(Map arg0)
    {
        localUpdate.saveMap(arg0);
    }

    @RolesAllowed("user")
    public void deleteObject(IObject row)
    {
        localUpdate.deleteObject(row);
    }

    @RolesAllowed("user")
    public void commit()
    {
        localUpdate.commit();
    }

    @RolesAllowed("user")
    public void flush()
    {
        localUpdate.flush();
    }

    @RolesAllowed("user")
    public void rollback()
    {
        localUpdate.rollback();
    }

}
