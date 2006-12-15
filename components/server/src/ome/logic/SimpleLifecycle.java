/*
 * ome.logic.AbstractBean
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.logic;

//Java imports
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

//Third-party imports
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

//Application-internal dependencies
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.api.ServiceInterface;
import ome.conditions.ApiUsageException;
import ome.conditions.InternalException;
import ome.security.SecuritySystem;
import ome.services.query.QueryFactory;
import ome.system.OmeroContext;
import ome.system.Principal;
import ome.system.SelfConfigurableService;
import ome.system.ServiceFactory;
import ome.tools.spring.AOPAdapter;
import ome.tools.spring.InternalServiceFactory;

/**
 * abstract base class for creating 
 * 
 *
 * @author  Josh Moore, josh.moore at gmx.de
 * @version $Revision$, $Date$
 * @since   3.0-M3
 */
@RevisionDate("$Date$")
@RevisionNumber("$Revision$")
public class SimpleLifecycle 
{
	@PostConstruct
	public void postConstruct(InvocationContext ctx) {
		try {
			if (ctx.getBean() instanceof AbstractBean) {
				AbstractBean bean = (AbstractBean) ctx.getBean();
				bean.create();
			}
			ctx.proceed();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@PreDestroy
	public void preDestroy(InvocationContext ctx) {
		try {
			if (ctx.getBean() instanceof AbstractBean) {
				AbstractBean bean = (AbstractBean) ctx.getBean();
				bean.destroy();
			}
			ctx.proceed();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
