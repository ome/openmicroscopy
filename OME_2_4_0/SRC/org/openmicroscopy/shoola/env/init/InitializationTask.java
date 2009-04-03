/*
 * org.openmicroscopy.shoola.env.init.InitializationTask
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

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.Container;

/** 
 * Declares the interface for configuring and executing an initialization
 * task.
 * <p>Tasks are encapsulated by objects and treated as commands by the
 * {@link Initializer}, which first configures them and then executes them all.
 * </p>
 * <p>This class factors out a reference to the {@link Container} singleton, as
 * this object is needed by all initialization tasks to perform their activity.
 * It also factors out a reference to the {@link Initializer} which is needed
 * by some tasks.</p>
 * <p>Because task objects are created through reflection, every subclass will
 * have to provide a public no-parms constructor.</p>
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */

abstract class InitializationTask
{
    
	/** Reference to the singleton {@link Container}. */
	protected Container		container;
    
    /** Reference to the command processor. */
    protected Initializer   initializer;
	
    
	/**
     * Links this object to the {@link Container}.
	 * Called by the reflection code in the {@link Initializer} to set
     * the reference to the {@link Container} when the object is created.
	 * 
	 * @param c	Reference to the singleton {@link Container}.
	 */
	void linkContainer(Container c)
	{
		container = c;
	}
    
    /**
     * Links this object to the {@link Initializer}.
     * Called by the reflection code in the {@link Initializer} to set
     * the reference to the {@link Initializer} when the object is created.
     * 
     * @param i Reference to the {@link Initializer}.
     */
    void linkInitializer(Initializer i)
    {
        initializer = i;
    }
	
	/**
	 * Returns the name of this task.
	 * The returned string should be something meaningful to the user, as
	 * it will be displayed on the splash screen.
	 * This method is called after {@link #configure()}, but before 
	 * {@link #execute()}. 
	 * 
	 * @return	See above.
	 */
	abstract String getName();
	
	/**
	 * Prepare the task for execution.
	 * This method is called before {@link #execute()}. 
	 *
	 */
	abstract void configure();
	
	/**
	 * Carries out the initialization task.
	 * 
	 * @throws StartupException	If an error occurs.
	 */
	abstract void execute() throws StartupException;
	
	/**
	 * Rolls back the initialization task.
	 * This method is typically implemented by those tasks that require
	 * to be undone if an error occurs during the initialization procedure
     * &#151; this allows for the container to exit gracefully.  For example,
     * the data management service has to release any acquired network 
     * resources before the program exits.
	 */
	abstract void rollback();
	
}
