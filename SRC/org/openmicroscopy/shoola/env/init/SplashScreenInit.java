/*
 * org.openmicroscopy.shoola.env.init.SplashScreenInit
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
import javax.swing.UIManager;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.ui.SplashScreen;
import org.openmicroscopy.shoola.env.ui.UIFactory;
import org.openmicroscopy.shoola.env.ui.UserCredentials;

/** 
 * Does some configuration required for the initialization process to run.
 * Loads L&F, registers for initialization progress notification and
 * pops up the splash screen.
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

final class SplashScreenInit
	extends InitializationTask
	implements InitializationListener
{

	/** 
	 * Reference to the command processor so that we can register for 
	 * initialization progress notification.
	 */
	private Initializer		initManager;
	
	/** The splash screen component. */
	private SplashScreen	splashScreen;
	
	/** The current number of tasks that have been executed. */
	private int				doneTasks;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param c	The singleton {@link Container}.
	 * @param initManager	The command processor.
	 */
	SplashScreenInit(Container c, Initializer initManager)
	{
		super(c);
		this.initManager = initManager;
		doneTasks = 0;
	}

	/** 
	 * Returns an empty string, as this task does nothing but configuration
	 * (we don't want a name to pop up in the splash screen for nothing).
	 * 
	 * @see InitializationTask#getName()
	 */
	String getName()
	{
		return "";
	}

	/**
	 * Loads L&F, registers for initialization progress notification and
	 * pops up the splash screen.
	 * 
	 * @see InitializationTask#configure()
	 */
	void configure()
	{
		try {
			UIManager.setLookAndFeel("com.birosoft.liquid.LiquidLookAndFeel");
		} catch(Exception e) { 
			//Ignore, we'll use the default L&F.
		}
		initManager.register(this);
		splashScreen = UIFactory.makeSplashScreen();
		splashScreen.open();
	}

	/** 
	 * Does nothing, as this task only requires configuration.
	 * @see InitializationTask#execute()
	 */
	void execute() throws StartupException {}
	
	/** 
	 * Does nothing.
	 * @see InitializationTask#rollback()
	 */
	void rollback() {}

	/** 
	 * Lets the splash screen component know how many tasks are going
	 * to be executed.
	 * 
	 * @see InitializationListener#onStart(int)
	 */
	public void onStart(int totalTasks)
	{
		splashScreen.setTotalTasks(totalTasks);
	}

	/** 
	 * Updates the splash screen to the task currently being executed and
	 * to the number of tasks that have been executed so far.
	 * 
	 * @see InitializationListener#onExecute(java.lang.String)
	 */
	public void onExecute(String taskName)
	{
		splashScreen.updateProgress(taskName);
		//NOTE: post increment b/c this task hasn't been executed yet.
	}

	/* (non-Javadoc)
	 * @see org.openmicroscopy.shoola.env.init.InitializationListener#onEnd()
	 */
	public void onEnd()
	{
		//Last update with total number of tasks executed.
		splashScreen.updateProgress("");
		
		//Wait until the user enters their credentials for logging into OME.
		UserCredentials uc = splashScreen.getUserCredentials();
	
		//Add credentials to the registry.
		Registry reg = container.getRegistry();
		reg.bind(LookupNames.USER_CREDENTIALS, uc);
	
		splashScreen.close();
	}

}
