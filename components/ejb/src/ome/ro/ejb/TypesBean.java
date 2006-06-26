/* ome.ro.ejb.TypesBean
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
import ome.api.ITypes;
import ome.model.IObject;
import ome.model.internal.Permissions;

@Stateless
@Remote(ITypes.class)
@RemoteBinding (jndiBinding="omero/remote/ome.api.ITypes")
@Local(ITypes.class)
@LocalBinding (jndiBinding="omero/local/ome.api.ITypes")
@SecurityDomain("OmeroSecurity")
public class TypesBean extends AbstractBean implements ITypes
{

    ITypes delegate;
    
    @PostConstruct
    public void create()
    {
        super.create();
        delegate = (ITypes) applicationContext.getBean(
                ITypes.class.getName());
    }
    
    @AroundInvoke
    public Object invoke( InvocationContext context ) throws Exception
    {
        return wrap( context, "&typesService" );
    }
    
    @PreDestroy
    public void destroy()
    {
        delegate = null;
        super.destroy();
    }

    // ~ DELEGATION
    // =========================================================================
    
    @RolesAllowed("user") 
    public IObject[] allEnumerations(Class k)
    {
        return delegate.allEnumerations(k);
    }

    @RolesAllowed("user") 
    public Class[] getAnnotationTypes()
    {
        return delegate.getAnnotationTypes();
    }

    @RolesAllowed("user") 
    public Class[] getContainerTypes()
    {
        return delegate.getContainerTypes();
    }

    @RolesAllowed("user") 
    public IObject getEnumeration(Class k, String string)
    {
        return delegate.getEnumeration(k, string);
    }

    @RolesAllowed("user") 
    public Class[] getImportTypes()
    {
        return delegate.getImportTypes();
    }

    @RolesAllowed("user") 
    public Class[] getPojoTypes()
    {
        return delegate.getPojoTypes();
    }

    @RolesAllowed("user") 
    public Class[] getResultTypes()
    {
        return delegate.getResultTypes();
    }

    @RolesAllowed("user") 
    public Permissions permissions(Class k)
    {
        return delegate.permissions(k);
    }

}
