/*
 * ome.logic.TypesImpl
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

/*------------------------------------------------------------------------------
 *
 * Written by:    Josh Moore <josh.moore@gmx.de>
 *
 *------------------------------------------------------------------------------
 */

package ome.logic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;

import ome.api.ITypes;
import ome.api.ServiceInterface;
import ome.api.local.LocalUpdate;
import ome.conditions.ApiUsageException;
import ome.model.IEnum;
import ome.model.IObject;
import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.security.SecureAction;
import ome.services.util.OmeroAroundInvoke;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.springframework.transaction.annotation.Transactional;

/**
 * implementation of the ITypes service interface.
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since OMERO 3.0
 */
@TransactionManagement(TransactionManagementType.BEAN)
@Transactional
@Stateless
@Remote(ITypes.class)
@RemoteBindings({
    @RemoteBinding(jndiBinding = "omero/remote/ome.api.ITypes"),
    @RemoteBinding(jndiBinding = "omero/secure/ome.api.ITypes",
		   clientBindUrl="sslsocket://0.0.0.0:3843")
})
@Local(ITypes.class)
@LocalBinding(jndiBinding = "omero/local/ome.api.ITypes")
@SecurityDomain("OmeroSecurity")
@Interceptors( { OmeroAroundInvoke.class, SimpleLifecycle.class })
public class TypesImpl extends AbstractLevel2Service implements ITypes {

	protected transient SessionFactory sf;
	
	/** injector for usage by the container. Not for general use */
    public final void setSessionFactory(SessionFactory sessions) {
        getBeanHelper().throwIfAlreadySet(this.sf, sessions);
        sf = sessions;
    }

    public final Class<? extends ServiceInterface> getServiceInterface() {
        return ITypes.class;
    }

    // ~ Service methods
    // =========================================================================

    @RolesAllowed("user")
    public <T extends IEnum> T createEnumeration(T newEnum) {
        final LocalUpdate up = iUpdate;

        // TODO should this belong to root?
        Details d = getSecuritySystem().newTransientDetails(newEnum);
        newEnum.getDetails().copy(d);
        return getSecuritySystem().doAction(new SecureAction() {
            public IObject updateObject(IObject... iObjects) {
                return up.saveAndReturnObject(iObjects[0]);
            }
        }, newEnum);
    }
    
    @RolesAllowed("system")
    public <T extends IEnum> T updateEnumeration(T oEnum) {
        return iUpdate.saveAndReturnObject(oEnum);
    }
    
    @RolesAllowed("system")
    public <T extends IEnum> void updateEnumerations(List<T> listEnum) {
    	// should be changed to saveAndReturnCollection(Collection graph)
    	// when method is implemented
    	
    	Collection<IObject> colEnum = new ArrayList<IObject>();
    	for (Object o : listEnum) {
            IObject obj = (IObject) o;
            colEnum.add(obj);
        }
    	iUpdate.saveCollection(colEnum);
    }
    
    @RolesAllowed("system")
    public <T extends IEnum> void deleteEnumeration(T oEnum) {
        iUpdate.deleteObject(oEnum);
    }

    @RolesAllowed("user")
    public <T extends IEnum> List<T> allEnumerations(Class<T> k) {
        return iQuery.findAll(k, null);
    }

    @RolesAllowed("user")
    public <T extends IEnum> T getEnumeration(Class<T> k, String string) {
        IEnum e = iQuery.findByString(k, "value", string);
        iQuery.initialize(e);
        if (e == null) {
            throw new ApiUsageException(String.format(
                    "An %s enum does not exist with the value: %s",
                    k.getName(), string));
        }
        return k.cast(e);
    }
    
    @RolesAllowed("system")
    public <T extends IEnum> List<Class<T>> getEnumerationTypes()  {
    	
    	List<Class<T>> list = new ArrayList<Class<T>>();
    	
    	Map<String, ClassMetadata> m = sf.getAllClassMetadata();
    	for (String key : m.keySet()) {    		
    		try {
    			Class klass = Class.forName(m.get(key).getEntityName());
				boolean r = IEnum.class.isAssignableFrom(klass);
				if(r) {
					list.add(klass);
				}
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("Class not found. Exception: "+e.getMessage());
			}
    	}
    	return list;
    }
    
    @RolesAllowed("system")
    public <T extends IEnum> Map<Class<T>, List<T>> getEnumerationsWithEntries()  {
    	
    	Map<Class<T>, List<T>> map = new HashMap<Class<T>, List<T>>();
    	for(Class klass : getEnumerationTypes()) {
    		List<T> entryList = allEnumerations(klass);
    		map.put(klass, entryList);
    	}
    	return map;
    }
    
    @RolesAllowed("system")
    public <T extends IEnum> List<T> allOryginalEnumerations(Class<T> klass) {
    	throw new RuntimeException("Not implemented yet.");
    }

    
    @RolesAllowed("user")
    public <T extends IObject> List<Class<T>> getResultTypes() {
        // TODO Auto-generated method stub
        return null;

    }

    @RolesAllowed("user")
    public <T extends IObject> List<Class<T>> getAnnotationTypes() {
        // TODO Auto-generated method stub
        return null;

    }

    @RolesAllowed("user")
    public <T extends IObject> List<Class<T>> getContainerTypes() {
        // TODO Auto-generated method stub
        return null;

    }

    @RolesAllowed("user")
    public <T extends IObject> List<Class<T>> getPojoTypes() {
        // TODO Auto-generated method stub
        return null;

    }

    @RolesAllowed("user")
    public <T extends IObject> List<Class<T>> getImportTypes() {
        // TODO Auto-generated method stub
        return null;

    }

    @RolesAllowed("user")
    public <T extends IObject> Permissions permissions(Class<T> k) {
        // TODO Auto-generated method stub
        return null;

    }

}
