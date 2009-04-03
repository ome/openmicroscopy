/*
 * org.openmicroscopy.shoola.util.ui.OSMenuHandler 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.util.ui;


//Java imports
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

//Third-party libraries

//Application-internal dependencies

/** 
 * Hooks existing about/quit functionality from an
 * application into handlers for the Mac OS X application menu.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class OSMenuAdapter 
	implements InvocationHandler
{

	/** The reference class. */
	private static final String APPLE_CLASS = "com.apple.eawt.Application";

	/** The reference class listener. */
	private static final String APPLE_CLASS_LISTENER = 
		"com.apple.eawt.ApplicationListener";

	/** The object that will perform the task. */
	private Object 			targetObject;
	
	/** The method to call */
	private Method 			targetMethod;
	
	/** The name of the EAWT method it intends to listen for. */
	private String 			proxySignature;

	/** The application. */
	private static Object 	macOSXApplication;

	/**
	 * Creates an adapter to the handle the <code>Quit</code>
	 * behaviour.
	 * 
	 * @param target	The object that will perform the task.
	 * @param handler	The method to call.
	 * @throws Throwable If an error occurs while creating the adapter.
	 */
	public static void setQuitHandler(Object target, Method handler) 
		throws Throwable
	{
		setHandler(new OSMenuAdapter("handleQuit", target, handler));
	}

	/**
	 * Creates an adapter to the handle the <code>About</code>
	 * behaviour.
	 * 
	 * @param target	The object that will perform the task.
	 * @param handler	The method to call.
	 * @throws Throwable If an error occurs while creating the adapter.
	 */
	public static void setAboutHandler(Object target, Method handler)
		throws Throwable
	{
		boolean enableAboutMenu = (target != null && handler != null);
		if (enableAboutMenu) {
			setHandler(new OSMenuAdapter("handleAbout", target, handler));
		}
		// If we're setting a handler, enable the About menu item by calling
		// com.apple.eawt.Application reflectively
		Method enableAboutMethod = 
			macOSXApplication.getClass().getDeclaredMethod(
					"setEnabledAboutMenu", new Class[] { boolean.class });
		enableAboutMethod.invoke(macOSXApplication, new Object[] { 
				Boolean.valueOf(enableAboutMenu) });
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param proxySignature The name of the EAWT method it intends to listen 
	 * 						 for e.g. handleQuit.
	 * @param target		 The object that will perform the task.
	 * @param handler		 The method to call.
	 */
	private OSMenuAdapter(String proxySignature, Object target, Method handler)
	{
		this.proxySignature = proxySignature;
		this.targetObject = target;
		this.targetMethod = handler;
	}

	/**
	 * Sets the adapter.
	 * 
	 * @param adapter The adapter to set.
	 * @throws Throwable If the class is not found of the method does not exist.
	 */
	private static void setHandler(OSMenuAdapter adapter) 
		throws Throwable
	{
		Class klass = Class.forName(APPLE_CLASS);
		if (macOSXApplication == null) 
			macOSXApplication = klass.getConstructor(
					(Class[]) null).newInstance((Object[]) null);

		Class listenerClass = Class.forName(APPLE_CLASS_LISTENER);
		Method addListenerMethod = 
			klass.getDeclaredMethod("addApplicationListener", 
					new Class[] { listenerClass });
		// Create a proxy object around this handler that can 
		//be reflectively added as an Apple ApplicationListener
		Object proxy = Proxy.newProxyInstance(
				OSMenuAdapter.class.getClassLoader(), 
				new Class[] { listenerClass }, adapter);
		addListenerMethod.invoke(macOSXApplication, new Object[] { proxy });
	}

	/**
	 * Performs any operation on the event.
	 * Returns <code>true</code> if the event is <code>null</code> or
	 * returns the value of the invocation.
	 * 
	 * @param event The event to handle.
	 * @return See above.
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	private boolean callTarget(Object event) 
		throws InvocationTargetException, IllegalAccessException 
	{
		Object result = targetMethod.invoke(targetObject, (Object[]) null);
		if (result == null) {
			if (proxySignature.equals("handleQuit")) return false;
			return true;
		}
		return Boolean.valueOf(result.toString()).booleanValue();
	}

	/**
	 * Controls if the passed method is the correct one e.g. handleAbout.
	 * 
	 * @param method The method to handle.
	 * @param args	 The passed arguments.
	 * @return <code>true</code> if the method is the correct one, 
	 * 			<code>false</code> otherwise.
	 */
	private boolean isCorrectMethod(Method method, Object[] args)
	{
		return (targetMethod != null && 
				proxySignature.equals(method.getName()) && args.length == 1);
	}

	/** 
	 * Notifies that the event is handled and cancels the default behaviour.
	 * 
	 * @param event 	The event to handle.
	 * @param handled 	The value to pass to the method e.g. <code>true</code>
	 * 					for <code>About</code>, <code>false</code> 
	 * 					for <code>Quit</code>.
	 * @throws Throwable if the adapter was not able to handle the application
	 * 					 event.
	 */
	private void setApplicationEventHandled(Object event, boolean handled)
		throws Throwable
	{
		if (event == null) return;
		Class klass = event.getClass();
		Method setHandledMethod = klass.getDeclaredMethod("setHandled", 
				new Class[] { boolean.class });
		setHandledMethod.invoke(event, 
				new Object[] { Boolean.valueOf(handled) });
	}

	/**
	 * Implemented as specified by the {@link InvocationHandler} interface.
	 * @see InvocationHandler#invoke(Object, Method, Object[])
	 */
	public Object invoke(Object proxy, Method method, Object[] args) 
		throws Throwable
	{
		if (isCorrectMethod(method, args)) 
			setApplicationEventHandled(args[0], callTarget(args[0]));
		return null;
	}

}
