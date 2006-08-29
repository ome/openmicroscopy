/* ome.logic.AbstractBean
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
