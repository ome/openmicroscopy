/*
 * Created on Jun 28, 2005
*/
package org.openmicroscopy.omero.logic;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.openmicroscopy.omero.OMEModel;

/**
 * @author josh
 */
public class DaoCleanUpHibernate implements MethodInterceptor {

    DaoUtils daoUtils;

    public DaoCleanUpHibernate(DaoUtils daoUtils) {
        this.daoUtils = daoUtils;
    }
    
    /**
     * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
     */
    public Object invoke(MethodInvocation arg0) throws Throwable {
        return clean(arg0.proceed());
    }
    
    Object clean(Object obj) {
        //TODO push OMEModel down into all calls
        if (null != obj) {
            if (obj instanceof OMEModel) {
                daoUtils.clean((OMEModel) obj);
            } else if (obj instanceof Set) {
                daoUtils.clean((Set) obj);
            } else if (obj instanceof Map) {
                //daoUtils.clean(((Map) obj).keySet());TODO here only integers, but...
                daoUtils.clean(new HashSet(((Map) obj).values()));                
            } else {
                String msg = "Instances of " + obj.getClass().getName()
                + " not supported.";
                throw new IllegalArgumentException(msg);
            }
        }
        return obj;
    }

}
