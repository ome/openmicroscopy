/*
 * ome.tools.spring.InternalServiceFactory
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

package ome.tools.spring;

//Java imports

//Third-party libraries
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

//Application-internal dependencies
import ome.system.OmeroContext;
import ome.system.ServiceFactory;


/** 
 * subclass of ome.system.ServiceFactory which retrieves managed  
 * {@link ome.api.ServiceInterface service-}instances. These have all the 
 * necessary layers of AOP interceptors for proper functioning. In fact, the
 * returned services behave almost exactly as if they were in an application 
 * server ("container").
 * 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 1.0
 */
public class ManagedServiceFactory 
extends ServiceFactory implements ApplicationContextAware{


	protected String getPrefix()
	{
		return "managed:";
	}
	
	/** returns null to prevent the lookup of any context, but rather wait
	 * on injection as a {@link ApplicationContextAware}
	 */
	protected String getDefaultContext()
    {
    	return null;
    }
	
	/** simple injector for the {@link ApplicationContext}
	 */
	public void setApplicationContext(ApplicationContext applicationContext) 
	throws BeansException {
		this.ctx = (OmeroContext) applicationContext;
	}
	
}
