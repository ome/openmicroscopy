/*
 * ome.logic.TypesImpl
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

/*------------------------------------------------------------------------------
 *
 * Written by:    Josh Moore <josh.moore@gmx.de>
 *
 *------------------------------------------------------------------------------
 */

package ome.logic;

//Java imports
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;

//Third-party libraries
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.springframework.transaction.annotation.Transactional;

//Application-internal dependencies
import ome.api.ITypes;
import ome.api.ServiceInterface;
import ome.api.local.LocalUpdate;
import ome.conditions.ApiUsageException;
import ome.model.IEnum;
import ome.model.IObject;
import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.security.SecureAction;

/**
 * implementation of the ITypes service interface.
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since OMERO 3.0
 */
@TransactionManagement(TransactionManagementType.BEAN)
@Transactional
@Stateless
@Remote(ITypes.class)
@RemoteBinding (jndiBinding="omero/remote/ome.api.ITypes")
@Local(ITypes.class)
@LocalBinding (jndiBinding="omero/local/ome.api.ITypes")
@SecurityDomain("OmeroSecurity")
@Interceptors({SimpleLifecycle.class})
public class TypesImpl extends AbstractLevel2Service implements ITypes
{

    @Override
    protected final Class<? extends ServiceInterface> getServiceInterface()
    {
        return ITypes.class;
    }

    // ~ Service methods
    // =========================================================================
    
    @RolesAllowed("user")
    public <T extends IEnum> T createEnumeration( T newEnum )
    {
    	final LocalUpdate up = iUpdate;

    	// TODO should this belong to root?
    	Details d = getSecuritySystem().newTransientDetails(newEnum);
    	newEnum.setDetails(d);
    	return getSecuritySystem().doAction(newEnum, new SecureAction(){
    		public IObject updateObject(IObject iObject) {
    			return up.saveAndReturnObject(iObject);
    		}
    	});
    }
    
    @RolesAllowed("user")
    public <T extends IEnum> List<T> allEnumerations(Class<T> k)
    {
        return iQuery.findAll(k,null);
    }

    @RolesAllowed("user")
    public <T extends IEnum> T getEnumeration(Class<T> k, String string)
    {
        IEnum e = iQuery.findByString(k,"value",string);
        iQuery.initialize(e);
        if ( e == null )
        {
        	throw new ApiUsageException(String.format(
        			"An %s enum does not exist with the value: %s",
        			k.getName(),string));
        }
        return k.cast(e);
    }

    @RolesAllowed("user")
    public <T extends IObject> List<Class<T>> getResultTypes()
    {
        // TODO Auto-generated method stub
        return null;
        
    }

    @RolesAllowed("user")
    public <T extends IObject> List<Class<T>> getAnnotationTypes()
    {
        // TODO Auto-generated method stub
        return null;
        
    }

    @RolesAllowed("user")
    public <T extends IObject> List<Class<T>> getContainerTypes()
    {
        // TODO Auto-generated method stub
        return null;
        
    }

    @RolesAllowed("user")
    public <T extends IObject> List<Class<T>> getPojoTypes()
    {
        // TODO Auto-generated method stub
        return null;
        
    }

    @RolesAllowed("user")
    public <T extends IObject> List<Class<T>> getImportTypes()
    {
        // TODO Auto-generated method stub
        return null;
        
    }

    @RolesAllowed("user")
    public <T extends IObject> Permissions permissions(Class<T> k)
    {
        // TODO Auto-generated method stub
        return null;
        
    }

}
