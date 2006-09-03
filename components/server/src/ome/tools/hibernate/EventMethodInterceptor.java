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
import java.lang.reflect.Method;

//Third-party imports
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.event.AbstractEvent;

// Application-internal dependencies
import ome.conditions.InternalException;

/**
 * filter which can be added to any event type in order to catch execution.
 */
public class EventMethodInterceptor implements MethodInterceptor
{

	public static class Action 
	{
		public Object call(MethodInvocation mi)
		{
			if ( mi.getMethod().getReturnType().equals( boolean.class ))
			{
				return Boolean.FALSE;
			}
			return null;
		}
	}
	
	public static class DisableAction extends Action
	{
		@Override
		public Object call(MethodInvocation mi) {
			if (disabled(mi)) throw createException(mi);
			return super.call(mi);
		}

		protected boolean disabled(MethodInvocation mi)
		{
			return true;
		}
		
		protected InternalException createException(MethodInvocation mi)
		{
			return new InternalException(String.format(
					"\nHibernate %s events have been disabled.",
					getType(mi)));
		}
		
		protected String getType(MethodInvocation mi)
		{
			Object event = mi.getArguments()[0];
			String type = "(unknown)";
			if (AbstractEvent.class.isAssignableFrom(event.getClass()))
			{
				type = event.getClass().getName();
			}
			return type;
		}

	}
	
	static volatile String last = null;
	static volatile int count = 1;
	
	private static Log log = LogFactory.getLog(EventMethodInterceptor.class);

	protected boolean verbose = false;
	
	protected Action action;
	
	public EventMethodInterceptor()
	{
		this.action = new Action();
	}
	
	public EventMethodInterceptor( Action action )
	{
		this.action = action;
	}

	// ~ Injectors
	// =========================================================================
	
	public void setDebug( boolean debug )
	{
		this.verbose = debug;
	}
	
	public Object invoke(MethodInvocation arg0) throws Throwable 
	{
		Object[] args = arg0.getArguments();
		Method method = arg0.getMethod();
		
		if (verbose && method.getName().startsWith("on")) 
		{
			log(String.format("%s.%s called.",
					method.getDeclaringClass().getName(),
					method.getName()));
		}

		return action.call(arg0);
	}

	// ~ Helpers
	// =========================================================================
	
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
