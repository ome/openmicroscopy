/* ome.ro.ejb.PojosBean
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
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.PreDestroy;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.security.SecurityDomain;

//Third-party imports

//Application-internal dependencies
import ome.api.IPojos;
import ome.model.ILink;
import ome.model.IObject;

@Stateless
@Remote(IPojos.class)
@RemoteBinding (jndiBinding="omero/remote/ome.api.IPojos")
@Local(IPojos.class)
@LocalBinding (jndiBinding="omero/local/ome.api.IPojos")
@SecurityDomain("OmeroSecurity")
public class PojosBean extends AbstractBean implements IPojos
{

    IPojos delegate;
    
    public PojosBean(){
        super();
        delegate = (IPojos) applicationContext.getBean("pojosService");
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
    public IObject createDataObject(IObject object, Map options)
    {
        return delegate.createDataObject(object, options);
    }

    @RolesAllowed("user") 
    public IObject[] createDataObjects(IObject[] dataObjects, Map options)
    {
        return delegate.createDataObjects(dataObjects, options);
    }

    @RolesAllowed("user") 
    public void deleteDataObject(IObject dataObject, Map options)
    {
        delegate.deleteDataObject(dataObject, options);
    }

    @RolesAllowed("user") 
    public void deleteDataObjects(IObject[] dataObjects, Map options)
    {
        delegate.deleteDataObjects(dataObjects, options);
    }

    @RolesAllowed("user") 
    public Map findAnnotations(Class rootNodeType, Set rootNodeIds, Set annotatorIds, Map options)
    {
        return delegate.findAnnotations(rootNodeType, rootNodeIds, annotatorIds, options);
    }

    @RolesAllowed("user") 
    public Set findCGCPaths(Set imgIds, String algorithm, Map options)
    {
        return delegate.findCGCPaths(imgIds, algorithm, options);
    }

    @RolesAllowed("user") 
    public Set findContainerHierarchies(Class rootNodeType, Set imagesIds, Map options)
    {
        return delegate.findContainerHierarchies(rootNodeType, imagesIds, options);
    }

    @RolesAllowed("user") 
    public Map getCollectionCount(String type, String property, Set ids, Map options)
    {
        return delegate.getCollectionCount(type, property, ids, options);
    }

    @RolesAllowed("user") 
    public Set getImages(Class rootNodeType, Set rootNodeIds, Map options)
    {
        return delegate.getImages(rootNodeType, rootNodeIds, options);
    }

    @RolesAllowed("user") 
    public Map getUserDetails(Set names, Map options)
    {
        return delegate.getUserDetails(names, options);
    }

    @RolesAllowed("user") 
    public Set getUserImages(Map options)
    {
        return delegate.getUserImages(options);
    }

    @RolesAllowed("user") 
    public ILink[] link(ILink[] dataObjectLinks, Map options)
    {
        return delegate.link(dataObjectLinks, options);
    }

    @RolesAllowed("user") 
    public Set loadContainerHierarchy(Class rootNodeType, Set rootNodeIds, Map options)
    {
        return delegate.loadContainerHierarchy(rootNodeType, rootNodeIds, options);
    }

    @RolesAllowed("user") 
    public Collection retrieveCollection(IObject dataObject, String collectionName, Map options)
    {
        return delegate.retrieveCollection(dataObject, collectionName, options);
    }

    @RolesAllowed("user") 
    public void unlink(ILink[] dataOjectLinks, Map options)
    {
        delegate.unlink(dataOjectLinks, options);
    }

    @RolesAllowed("user") 
    public IObject updateDataObject(IObject dataObject, Map options)
    {
        return delegate.updateDataObject(dataObject, options);
    }

    @RolesAllowed("user") 
    public IObject[] updateDataObjects(IObject[] dataObjects, Map options)
    {
        return delegate.updateDataObjects(dataObjects, options);
    }

    

}
