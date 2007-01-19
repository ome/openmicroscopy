/*
 * org.openmicroscopy.shoola.env.ui.UIFactory
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

package org.openmicroscopy.shoola.env.ui;

//Java imports
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.data.login.LoginConfig;

/** 
 * Factory for the various windows and widgets used within the container.
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

public class UIFactory 
{	
	
	/**
	 * Creates the splash screen that is used at initialization.
	 * 
     * @param listener  A listener for {@link SplashScreenView#cancel} button.
     * @param c			Reference to the singleton {@link Container}.
	 * @return	        The splash screen.
	 */
	public static SplashScreen makeSplashScreen(ActionListener listener, 
												Container c)
	{
		return new SplashScreenProxy(listener, c);
	}
	
	/**
	 * Creates the {@link TaskBar}.
	 * 
	 * @param c	Reference to the singleton {@link Container}.
	 * @return	The {@link TaskBar}.
	 */
	public static TaskBar makeTaskBar(Container c)
	{
		TaskBarManager tbm = new TaskBarManager(c);
		return tbm.getView();
	}
	
	/**
	 * Creates the {@link UserNotifier}.
	 *
	 * @param c	Reference to the singleton {@link Container}.
	 * @return	The {@link UserNotifier}.
	 */
	public static UserNotifier makeUserNotifier(Container c)
	{
		return new UserNotifierImpl(c);
	}
	
	/**
	 * Creates the {@link UserNotifier}.
	 *
	 * @return	The {@link UserNotifier}.
	 */
	//public static UserNotifier makeUserNotifier()
	//{
	//	return new UserNotifierImpl();
	//}
	
    /**
     * Returns an array of the available servers.
     * 
     * @return See above.
     */
    public static String[] getServersAsArray()
    {
        String[] listOfServers = null;
        Preferences prefs = Preferences.userNodeForPackage(LoginConfig.class);
        String servers = prefs.get(LoginConfig.OMERO_SERVER, null);
        if (servers == null || servers.length() == 0) {
            listOfServers = new String[1];
            listOfServers[0] = LoginConfig.DEFAULT_SERVER;
        } else {
            String[] l = servers.split(LoginConfig.SERVER_NAME_SEPARATOR, 0);
            if (l == null) {
                listOfServers = new String[1];
                listOfServers[0] = LoginConfig.DEFAULT_SERVER;
            } else {
                listOfServers = new String[l.length+1];
                int index;
                for (index = 0; index < l.length; index++)
                    listOfServers[index] = l[index].trim();
                index = l.length;
                listOfServers[index] = LoginConfig.DEFAULT_SERVER;
            }
        }   
        return listOfServers;
    }
    
    /**
     * Returns a collection of available servers.
     * 
     * @return See above.
     */
    public static List getServers()
    {
    	Preferences prefs = Preferences.userNodeForPackage(LoginConfig.class);
        String servers = prefs.get(LoginConfig.OMERO_SERVER, null);
        if (servers == null || servers.length() == 0)  return null;
        String[] l = servers.split(LoginConfig.SERVER_NAME_SEPARATOR, 0);
        if (l == null) return null;
        List listOfServers = new ArrayList(l.length);
        int index;
        for (index = 0; index < l.length; index++)
        	listOfServers.add(l[index].trim());
        return listOfServers; 
    }
    
}
