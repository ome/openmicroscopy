/*
 * org.openmicroscopy.shoola.env.init.Initializer
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
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

package org.openmicroscopy.shoola.env.init;

//Java imports
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * <br><b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class Initializer
{
	/** Queue to order the tasks to be executed. */
	private List				processingQueue;
	
	private InitializationTask	currentTask;
	
	private Set					initListeners;
	
	
	public Initializer()
	{
		processingQueue = new ArrayList();
		initListeners = new HashSet();
	}
	
	public void configure()
	{
		//TODO: create tasks, call configure() for each of them and 
		//		queue them up properly.
	}
	
	public void doInit()
		throws StartupException
	{
		Iterator i = processingQueue.iterator();
		notifyStart();
		while (i.hasNext()) {
			currentTask = (InitializationTask) i.next();
			notifyExecute();
			currentTask.execute();
		}
		notifyEnd();
		currentTask = null;
		processingQueue = null;
		initListeners = null;
	}
	
	/**
	 * Adds the specified initialization listener to the notification set.
	 * 
	 * @param subscriber	The listener.
	 * @return	<code>true</code> if <code>subscriber</code> is not already in 
	 * 			the notification set, <code>false</code> otherwise.
	 * @throws	NullPointerException If <code>null</code>is passed in. 
	 */
	public boolean register(InitializationListener subscriber) 
	{
		if (subscriber == null)		throw new NullPointerException();
		return initListeners.add(subscriber);
	}
	
	private void notifyStart()
	{
		int	size = processingQueue.size();
		Iterator i = initListeners.iterator();
		InitializationListener subscriber;
		while (i.hasNext()) {
			subscriber = (InitializationListener) i.next();
			subscriber.onStart(size);
		}
	}
	
	private void notifyExecute()
	{
		String name = currentTask.getName();
		Iterator i = initListeners.iterator();
		InitializationListener subscriber;
		while (i.hasNext()) {
			subscriber = (InitializationListener) i.next();
			subscriber.onExecute(name);
		}
	}
	
	private void notifyEnd()
	{
		Iterator i = initListeners.iterator();
		InitializationListener subscriber;
		while (i.hasNext()) {
			subscriber = (InitializationListener) i.next();
			subscriber.onEnd();
		}
	}
}
