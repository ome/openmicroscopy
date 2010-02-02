/*
 * org.openmicroscopy.shoola.env.data.views.AdminView 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.env.data.views;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import pojos.ExperimenterData;

/** 
 * Provides method to handle groups and users.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public interface AdminView
	extends DataServicesView
{

	/**
	 * Loads the used and free disk space for the specified user if any,
	 * pass <code>-1</code> to retrieve the whole disk space.
	 * 
	 * @param userID	The id of the user or <code>-1</code>.
	 * @param observer	Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle getDiskSpace(long userID, AgentEventListener observer);
	
	/**
	 * Updates the specified experimenter.
	 * 
	 * @param exp The experimenter to update. Mustn't be <code>null</code>.
	 * @param observer	Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle updateExperimenter(ExperimenterData exp, 
			AgentEventListener observer);
	
	/**
	 * Modifies the password of the user currently logged in.
	 * 
	 * @param oldPassword 	The password used to log in.  
	 * @param newPassword	The new password value.
	 * @param observer	Call-back handler.
	 * @return A handle that can be used to cancel the call.
	 */
	public CallHandle changePassword(String oldPassword, String newPassword, 
			AgentEventListener observer);
	
}
