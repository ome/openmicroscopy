/*
 * Created on Mar 6, 2005
 */
package org.ome.srv.logic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.aopalliance.intercept.MethodInvocation;
import org.ome.model.OMEObject;

import net.sf.acegisecurity.AccessDeniedException;
import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.ConfigAttribute;
import net.sf.acegisecurity.ConfigAttributeDefinition;
import net.sf.acegisecurity.afterinvocation.AfterInvocationProvider;

/**
 * @author josh
 */
public class AfterQueryInvocation extends AbstractService implements AfterInvocationProvider {

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.acegisecurity.afterinvocation.AfterInvocationProvider#decide(net.sf.acegisecurity.Authentication,
     *      java.lang.Object, net.sf.acegisecurity.ConfigAttributeDefinition,
     *      java.lang.Object)
     */
public Object decide(Authentication authentication, Object object, ConfigAttributeDefinition config, Object returnedObject) throws AccessDeniedException {

    	boolean query = false;
    	for (Iterator iter = config.getConfigAttributes(); iter.hasNext();) {
            ConfigAttribute attribute = (ConfigAttribute) iter.next();
            if ((attribute.getAttribute() != null) && attribute.getAttribute().startsWith("QUERY")) {
                query = true;
                break;
            }
        }
    
    	if (!query){
    	    return returnedObject;
    	}
    	
    	if (!(returnedObject instanceof List)){
            throw new IllegalArgumentException("QUERY methods can only return lists.");
        }
        
        if (null == returnedObject){
            return null;
        }
        
        /* TODO put this code in helper class along with that from LSIDVoter */
        for (int i = 0; i < authentication.getAuthorities().length;i++) {
            if (authentication.getAuthorities()[i].getAuthority().equalsIgnoreCase("ROLE_ADMIN")) {
                return returnedObject;
            }
        }
        
        List lsobjs = (List) returnedObject;
        List newReturn = new ArrayList();
        
        for (Iterator iter = lsobjs.iterator(); iter.hasNext();) {
            OMEObject lsobj = (OMEObject) iter.next();
            if (lsobj.isOwner(authentication.getName())){
                newReturn.add(lsobj);
            }
        }
        
        return newReturn;
        
        
        
    }
    /*
     * (non-Javadoc)
     * 
     * @see net.sf.acegisecurity.afterinvocation.AfterInvocationProvider#supports(net.sf.acegisecurity.ConfigAttribute)
     */
    public boolean supports(ConfigAttribute attribute) {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.acegisecurity.afterinvocation.AfterInvocationProvider#supports(java.lang.Class)
     */
    public boolean supports(Class clazz) {
        return (MethodInvocation.class.isAssignableFrom(clazz));
    }

}