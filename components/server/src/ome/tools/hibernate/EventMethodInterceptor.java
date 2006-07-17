/* ome.tools.hibernate.EventMethodInterceptor
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

package ome.tools.hibernate;

//Java imports

//Third-party imports
import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.event.AbstractEvent;
import org.springframework.aop.MethodBeforeAdvice;

// Application-internal dependencies
import ome.conditions.InternalException;

/**
 * filter which can be added to any event type in order to catch execution.
 */
public class EventMethodInterceptor implements MethodInterceptor
{

	static volatile String last = null;
	static volatile int count = 1;
	
	private static Log log = LogFactory.getLog(EventMethodInterceptor.class);

	protected boolean disableAll = false;
	
	protected boolean verbose = false;
	
	public void setDisableAll( boolean disable )
	{
		this.disableAll = disable;
	}
	
	public void setDebug( boolean debug )
	{
		this.verbose = debug;
	}
	
	public Object invoke(MethodInvocation arg0) throws Throwable 
	{
		Object[] args = arg0.getArguments();
		Method method = arg0.getMethod();
		
		if (disableAll) throw createException(args[0]);
		if (verbose && method.getName().startsWith("on")) 
		{
			log(String.format("%s.%s called.",
					method.getDeclaringClass().getName(),
					method.getName()));
		}

		if ( method.getReturnType().equals( boolean.class ))
		{
			return Boolean.FALSE;
		}
		return null;
	}
	
	protected InternalException createException(Object event)
	{
		return new InternalException(String.format(
				"\nThe SessionFactory has been configured \n" +
				"so that %s events \nwill not be tolerated.",
				getType(event)));
	}

	protected String getType(Object event)
	{
		String type = "Hibernate event";
		if (AbstractEvent.class.isAssignableFrom(event.getClass()))
		{
			type = event.getClass().getName();
		}
		return type;
	}
	
	protected void log(String msg)
	{
		if ( msg.equals(last))
		{
			count++;
		}
	
		else if ( log.isInfoEnabled() )
		{
			String times = " ( "+count+" times )";
			log.info(msg+times);
			last = msg;
			count = 1;
		}
	}

}
