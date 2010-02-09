/*
 * org.openmicroscopy.shoola.env.data.views.calls.AdminLoader 
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
package org.openmicroscopy.shoola.env.data.views.calls;


//Java imports
import java.util.ArrayList;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.AdminService;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import pojos.ExperimenterData;

/** 
 * Retrieves the available user groups other than system.
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
public class AdminLoader 
	extends BatchCallTree
{

	/** Identifies to load the available and used space. */
	public static final int SPACE = 0;
	
	/** Identifies to load the available groups. */
	public static final int GROUPS = 1;
	
	/** Identifies to load the experimenters within a group. */
	public static final int EXPERIMENTERS = 2;
	
    /** The result of the call. */
    private Object		result;
    
    /** Loads the specified experimenter groups. */
    private BatchCall   loadCall;
    
    /**
     * Creates a {@link BatchCall} to retrieve the available and used
     * disk space.
     * 
     * @param userID	The id of the user or <code>-1</code>.
     * @return The {@link BatchCall}.
     */
    private BatchCall availableSpaceCall(final long userID)
    {
        return new BatchCall("Loading disk space information") {
            public void doCall() throws Exception
            {
                AdminService os = context.getAdminService();
                List<Long> l = new ArrayList<Long>();
                l.add(os.getSpace(AdminService.FREE, userID));
                l.add(os.getSpace(AdminService.USED, userID));
                result = l;
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to change the password
     * of the user currently logged in.
     * 
	 * @param oldPassword 	The password used to log in.  
     * @param newPassword	The new password value.
     * @return The {@link BatchCall}.
     */
    private BatchCall changePassword(final String oldPassword, 
    								final String newPassword)
    {
        return new BatchCall("Change password") {
            public void doCall() throws Exception
            {
            	AdminService os = context.getAdminService();
                result = os.changePassword(oldPassword, newPassword);
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to update the specified experimenter.
     * 
	 * @param exp 	The experimenter to update.
     * @return The {@link BatchCall}.
     */
    private BatchCall updateExperimenter(final ExperimenterData exp)
    {
        return new BatchCall("Update experimenter") {
            public void doCall() throws Exception
            {
            	AdminService os = context.getAdminService();
                result = os.updateExperimenter(exp, null);
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to load all the available group if the 
     * passed parameter is <code>-1</code>, load the group otherwise.
     * 
	 * @param id The id of the group.
     * @return The {@link BatchCall}.
     */
    private BatchCall loadGroup(final long id)
    {
        return new BatchCall("Load groups") {
            public void doCall() throws Exception
            {
            	AdminService os = context.getAdminService();
                result = os.loadGroups(id);
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to load the experimenters contained 
     * within the specified group.
     * 
	 * @param id The id of the group.
     * @return The {@link BatchCall}.
     */
    private BatchCall loadExperimenters(final long id)
    {
        return new BatchCall("Load experimenters") {
            public void doCall() throws Exception
            {
            	AdminService os = context.getAdminService();
                result = os.loadExperimenters(id);
            }
        };
    }
    
	 /**
     * Adds the {@link #loadCall} to the computation tree.
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree() { add(loadCall); }

    /**
     * Returns, in a <code>Set</code>, the root nodes of the found trees.
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return result; }
    
    /** 
     * Creates a new instance. 
     * 
     * @param id		The id of the user, or group or <code>-1</code>.
     * @param index 	One of the constants defined by this class.
     */
    public AdminLoader(long id, int index)
    {
    	switch (index) {
			case SPACE:
				loadCall = availableSpaceCall(id);
				break;
			case GROUPS:
				loadCall = loadGroup(id);
				break;
			case EXPERIMENTERS:
				loadCall = loadExperimenters(id);
		}
    }

    /**
     * Creates a new instance.
     * 
     * @param oldPassword 	The password used to log in.  
     * @param newPassword	The new password value.
     */
    public AdminLoader(String oldPassword, String newPassword)
    {
    	if (newPassword == null || newPassword.trim().length() == 0)
    		throw new IllegalArgumentException("Password not valid.");
    	if (oldPassword == null || oldPassword.trim().length() == 0)
    		throw new IllegalArgumentException("Password not valid.");
    	loadCall = changePassword(oldPassword, newPassword);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param exp The experimenter to update. Mustn't be <code>null</code>.
     */
    public AdminLoader(ExperimenterData exp)
    {
    	if (exp == null)
    		throw new IllegalArgumentException("Experimenter not valid.");
    	loadCall = updateExperimenter(exp);
    }
    
}
