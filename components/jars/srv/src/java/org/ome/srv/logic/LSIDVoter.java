/*
 * Created on Mar 6, 2005
*/
package org.ome.srv.logic;

import java.util.Iterator;

import org.aopalliance.intercept.MethodInvocation;
import org.ome.model.LSID;
import org.ome.model.OMEObject;

import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.ConfigAttribute;
import net.sf.acegisecurity.ConfigAttributeDefinition;
import net.sf.acegisecurity.vote.AccessDecisionVoter;

/**
 * @author josh
 */
public class LSIDVoter extends AbstractService implements AccessDecisionVoter {

    /* (non-Javadoc)
     * @see net.sf.acegisecurity.vote.AccessDecisionVoter#supports(net.sf.acegisecurity.ConfigAttribute)
     */
    public boolean supports(ConfigAttribute attribute) {
        return true;
    }

    /* (non-Javadoc)
     * @see net.sf.acegisecurity.vote.AccessDecisionVoter#supports(java.lang.Class)
     */
    public boolean supports(Class clazz) {
            return (MethodInvocation.class.isAssignableFrom(clazz));
    }

    /* (non-Javadoc)
     * @see net.sf.acegisecurity.vote.AccessDecisionVoter#vote(net.sf.acegisecurity.Authentication, java.lang.Object, net.sf.acegisecurity.ConfigAttributeDefinition)
     */
    public int vote(Authentication auth, Object methInv, ConfigAttributeDefinition conf) {
        boolean lookup = false;
    	for (Iterator iter = conf.getConfigAttributes(); iter.hasNext();) {
            ConfigAttribute attribute = (ConfigAttribute) iter.next();
            if ((attribute.getAttribute() != null) && attribute.getAttribute().startsWith("LOOKUP")) {
                lookup = true;
                break;
            }
        }
    	
    	if (!lookup){
    	    return ACCESS_ABSTAIN;
    	}
    	
        MethodInvocation method = (MethodInvocation) methInv;

        int access = ACCESS_ABSTAIN;
        // Attempt to find a matching granted authority
        for (int i = 0; i < auth.getAuthorities().length;i++) {
            if (auth.getAuthorities()[i].getAuthority().equalsIgnoreCase("ROLE_ADMIN")) {
                access = ACCESS_GRANTED;
            }
        }
        if (access!=ACCESS_ABSTAIN){
            return access;
        }

        Object[] args = method.getArguments();
        if (args.length > 0){
            if (args[0] instanceof LSID) {
                LSID lsid = (LSID) args[0]; //FIXME this isn't enough!
                OMEObject lsobj = (OMEObject) retrieveObject(lsid);
                if (lsobj.isOwner(auth.getName())){
                    return ACCESS_GRANTED;
                }
                return ACCESS_DENIED;
            }
        }
        return access;
    }

}
