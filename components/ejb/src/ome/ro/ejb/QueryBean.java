/* ome.ro.ejb.QueryBean
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
import java.util.List;

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
import ome.api.IQuery;
import ome.api.local.LocalQuery;
import ome.conditions.ApiUsageException;
import ome.conditions.ValidationException;
import ome.model.IObject;
import ome.parameters.Filter;
import ome.parameters.Parameters;
import ome.services.dao.Dao;
import ome.services.query.Query;


@Stateless
@Remote(IQuery.class)
@RemoteBinding (jndiBinding="omero/remote/ome.api.IQuery")
@Local(LocalQuery.class)
@LocalBinding (jndiBinding="omero/local/ome.api.local.LocalQuery")
@SecurityDomain("OmeroSecurity")
public class QueryBean extends AbstractBean implements LocalQuery
{

    LocalQuery delegate;
    
    public QueryBean(){
        super();
        delegate = (LocalQuery) applicationContext.getBean("queryService");
    }
    
    @PreDestroy
    public void destroy()
    {
        delegate = null;
        super.destroy();
    }

    @RolesAllowed("user") public boolean checkProperty(String arg0, String arg1)
    {
        return delegate.checkProperty(arg0, arg1);
    }

    @RolesAllowed("user") public boolean checkType(String arg0)
    {
        return delegate.checkType(arg0);
    }

    @RolesAllowed("user") public void evict(Object arg0)
    {
        delegate.evict(arg0);
    }
    
    @RolesAllowed("user") public Object execute(Query arg0)
    {
        return delegate.execute(arg0);
    }

    @RolesAllowed("user") public IObject find(Class arg0, long arg1)
    {
        return delegate.find(arg0, arg1);
    }

    @RolesAllowed("user") public List findAll(Class arg0, Filter arg1)
    {
        return delegate.findAll(arg0, arg1);
    }

    @RolesAllowed("user") public List findAllByExample(IObject arg0, Filter arg1)
    {
        return delegate.findAllByExample(arg0, arg1);
    }

    @RolesAllowed("user") public List findAllByQuery(String arg0, Parameters arg1)
    {
        return delegate.findAllByQuery(arg0, arg1);
    }

    @RolesAllowed("user") public List findAllByString(Class arg0, String arg1, String arg2, boolean arg3, Filter arg4)
    {
        return delegate.findAllByString(arg0, arg1, arg2, arg3, arg4);
    }

    @RolesAllowed("user") public IObject findByExample(IObject arg0) throws ApiUsageException
    {
        return delegate.findByExample(arg0);
    }

    @RolesAllowed("user") public IObject findByQuery(String arg0, Parameters arg1) throws ValidationException
    {
        return delegate.findByQuery(arg0, arg1);
    }

    @RolesAllowed("user") public IObject findByString(Class arg0, String arg1, String arg2) throws ApiUsageException
    {
        return delegate.findByString(arg0, arg1, arg2);
    }

    @RolesAllowed("user") public IObject get(Class arg0, long arg1) throws ValidationException
    {
        return delegate.get(arg0, arg1);
    }

    @RolesAllowed("user") public void initialize(Object arg0)
    {
        delegate.initialize(arg0);
    }

}
