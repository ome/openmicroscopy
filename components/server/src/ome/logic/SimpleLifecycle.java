/*
 * ome.logic.AbstractBean
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.logic;

// Java imports
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.interceptor.InvocationContext;

// Application-internal dependencies
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.system.SelfConfigurableService;

/**
 * abstract base class for creating
 * 
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @version $Revision$, $Date$
 * @since 3.0-M3
 */
@RevisionDate("$Date$")
@RevisionNumber("$Revision$")
public class SimpleLifecycle {
    
    @PostConstruct
    public void postConstruct(InvocationContext ctx) {
        try {
            if (ctx.getBean() instanceof SelfConfigurableService) {
                SelfConfigurableService bean = (SelfConfigurableService) ctx.getBean();
                bean.selfConfigure();
            }
            ctx.proceed();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    public void preDestroy(InvocationContext ctx) {
        try {
            // currently doing nothing. All state is transient.
            ctx.proceed();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
