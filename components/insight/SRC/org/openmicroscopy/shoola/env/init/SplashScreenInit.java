/*
 * org.openmicroscopy.shoola.env.init.SplashScreenInit
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.env.init;

//Java imports

//Third-party libraries

//Application-internal dependencies
import java.text.NumberFormat;

import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.login.LoginConfig;
import org.openmicroscopy.shoola.env.data.login.LoginService;
import org.openmicroscopy.shoola.env.data.login.UserCredentials;
import org.openmicroscopy.shoola.env.ui.SplashScreen;
import org.openmicroscopy.shoola.env.ui.UIFactory;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.CommonsLangUtils;

/** 
 * Does some configuration required for the initialization process to run.
 * Loads L&F, registers for initialization progress notification and
 * pops up the splash screen. When the initialization process is finished,
 * we wait until user's credentials are available and then try to log into
 * <i>OMEDS</i>.
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

public final class SplashScreenInit
	extends InitializationTask
	implements InitializationListener
{
	
	/** The splash screen component. */
	private SplashScreen	splashScreen;
	
	/** The total number of tasks to execute. */
	private int 			totalTasks;
	
	/** Constructor required by superclass. */
	SplashScreenInit() {}

	/** 
	 * Returns an empty string, as this task does nothing but configuration
	 * (we don't want a name to pop up in the splash screen for nothing).
	 * @see InitializationTask#getName()
	 */
	String getName() { return ""; }

	/**
	 * Registers for initialization progress notification and
	 * pops up the splash screen.
	 * @see InitializationTask#configure()
	 */
	void configure()
	{
        initializer.register(this);
	}

	/** 
	 * Does nothing, as this task only requires configuration.
	 * @see InitializationTask#execute()
	 */
	void execute() 
		throws StartupException
	{
		splashScreen = UIFactory.makeSplashScreen(container);
		splashScreen.open();
		splashScreen.setTotalTasks(totalTasks);
	}
	
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
		this.totalTasks = totalTasks;
	}

	/** 
	 * Updates the splash screen to the task currently being executed and
	 * to the number of tasks that have been executed so far.
	 * 
	 * @see InitializationListener#onExecute(String)
	 */
	public void onExecute(String taskName)
	{
		if (splashScreen != null)
		splashScreen.updateProgress(taskName);
		//NOTE: post increment b/c this task hasn't been executed yet.
	}

	/** 
	 * Waits until user's credentials are available and then tries to log onto
	 * <i>OMERO</i>.
	 * @see InitializationListener#onEnd()
	 */
	public void onEnd()
	{
		//Last update with total number of tasks executed.
		splashScreen.updateProgress("");
		//Try to log onto OMEDS and retry upon failure for at most as many
        //times as specified in the Container's configuration.
        Registry reg = container.getRegistry();
        
        boolean b = false;
        Integer v = (Integer) reg.lookup(LookupNames.ENTRY_POINT);
		if (v != null) {
		    b = v.intValue() == LookupNames.INSIGHT_ENTRY ||
	                    v.intValue() == LookupNames.IMPORTER_ENTRY;
		}
		
        if (!b) {
        	splashScreen.close();
        	return;
        }
        LoginConfig cfg = new LoginConfig(reg);
        int max = cfg.getMaxRetry();
        LoginService loginSvc = (LoginService) reg.lookup(LookupNames.LOGIN);

        int index = max;
        UserCredentials uc;
        UserNotifier un = UIFactory.makeUserNotifier(container);
        String jnlpHost = System.getProperty("jnlp.omero.host");
        String jnlpPort = System.getProperty("jnlp.omero.port");
        String jnlpSession = System.getProperty("jnlp.omero.sessionid");
        while (0 < max--) {
            if (CommonsLangUtils.isNotBlank(jnlpSession)) {
                uc = new UserCredentials(jnlpSession,
                        jnlpSession, jnlpHost, UserCredentials.HIGH);
                try {
                    NumberFormat nf = NumberFormat.getInstance();
                    uc.setPort(nf.parse(jnlpPort).intValue());
                } catch (Exception e) {}//use the default port.
            } else {
                uc = splashScreen.getUserCredentials((max == index-1));
            }
            //needed b/c need to retrieve user's details later.
            reg.bind(LookupNames.USER_CREDENTIALS, uc);

            switch (loginSvc.login(uc)) {
				case LoginService.CONNECTED:
	                max = 0;
	                break;
	
				case LoginService.TIMEOUT:
					loginSvc.notifyLoginTimeout();
					break;
				case LoginService.NOT_CONNECTED:
					if (max != 0) {
						loginSvc.notifyLoginFailure();
						splashScreen.onLoginFailure();
						jnlpSession = null;
		        	} else if (max == 0) {
		        		//Exit if we couldn't manage to log in.
		        		 un.notifyError("Login Failure", 
		        				 "A valid connection to the OMERO "+
		                         "server could not be established. \n" +
		                         "The application will exit.");
		                 container.exit();
		         	}
			}
        }
        //Now get rid of the Splash Screen.
        splashScreen.close();
    }
    
}
