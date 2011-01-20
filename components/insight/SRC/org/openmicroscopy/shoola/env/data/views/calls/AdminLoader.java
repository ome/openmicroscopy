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
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.AdminService;
import org.openmicroscopy.shoola.env.data.OmeroImageService;
import org.openmicroscopy.shoola.env.data.login.UserCredentials;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;

import pojos.DataObject;
import pojos.ExperimenterData;
import pojos.GroupData;

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
	
	/** Identifies to load the photo of the experimenter. */
	public static final int EXPERIMENTER_PHOTO = 3;
	
	/** Identifies to update the experimenter. */
	public static final int EXPERIMENTER_UPDATE = 4;
	
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
     * Creates a {@link BatchCall} to update the specified group.
     * 
	 * @param group The group to update.
	 * @param permissions The desired permissions level or <code>-1</code>.
     * @return The {@link BatchCall}.
     */
    private BatchCall updateGroup(final GroupData group, final int permissions)
    {
        return new BatchCall("Update group") {
            public void doCall() throws Exception
            {
            	AdminService os = context.getAdminService();
                result = os.updateGroup(group, permissions);
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
     * Creates a {@link BatchCall} to update the specified experimenters.
     * 
	 * @param experimenters The experimenters to update. 
     * @return The {@link BatchCall}.
     */
    private BatchCall updateExperimenters(final GroupData group, 
    		final Map<ExperimenterData, UserCredentials> experimenters)
    {
        return new BatchCall("Update experimenters") {
            public void doCall() throws Exception
            {
            	AdminService os = context.getAdminService();
                result = os.updateExperimenters(group, experimenters);
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to load the photo.
     * 
	 * @param experimenter The experimenter to handle. 
     * @return The {@link BatchCall}.
     */
    private BatchCall loadExperimenterPhoto(final ExperimenterData experimenter)
    {
        return new BatchCall("Load photo") {
            public void doCall() throws Exception
            {
            	OmeroImageService os = context.getImageService();
            	List<DataObject> exps = new ArrayList<DataObject>();
            	exps.add(experimenter);
            	Map<DataObject, BufferedImage> map = 
            		os.getExperimenterThumbnailSet(exps, 96);
               result = map.get(experimenter);
           }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to load the photo.
     * 
	 * @param experimenter The experimenter to handle.
	 * @param photo The photo to upload.
	 * @param format The format of the file.
     * @return The {@link BatchCall}.
     */
    private BatchCall uploadExperimenterPhoto(
    		final ExperimenterData experimenter, final File photo,
    		final String format)
    {
        return new BatchCall("Update experimenters") {
            public void doCall() throws Exception
            {
            	AdminService svc = context.getAdminService();
            	result = svc.uploadUserPhoto(photo, format, experimenter);
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
    public AdminLoader(ExperimenterData exp, int index)
    {
    	if (exp == null)
    		throw new IllegalArgumentException("Experimenter not valid.");
    	switch (index) {
    		case EXPERIMENTER_UPDATE:
				loadCall = updateExperimenter(exp);
				break;
			case EXPERIMENTER_PHOTO:
				loadCall = loadExperimenterPhoto(exp);
		}
    }
    
    /**
     * Creates a new instance.
     * 
     * @param exp 	The experimenter to update. Mustn't be <code>null</code>.
     * @param photo The photo to upload. Mustn't be <code>null</code>.
     * @param format The format of the photo to upload.
     */
    public AdminLoader(ExperimenterData exp, File photo, String format)
    {
    	if (exp == null)
    		throw new IllegalArgumentException("Experimenter not valid.");
    	if (photo == null)
    		throw new IllegalArgumentException("Photo not valid.");
    	loadCall = uploadExperimenterPhoto(exp, photo, format);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param group The group to update. Mustn't be <code>null</code>.
     * @param permissions The desired permissions level or <code>-1</code>.
     */
    public AdminLoader(GroupData group, int permissions)
    {
    	if (group == null)
    		throw new IllegalArgumentException("Group not valid.");
    	loadCall = updateGroup(group, permissions);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param group The default group to set.
     * @param experimenters The experimenters to update. 
     * 						Mustn't be <code>null</code>.
     */
    public AdminLoader(GroupData group,
    		Map<ExperimenterData, UserCredentials> experimenters)
    {
    	if (experimenters == null)
    		throw new IllegalArgumentException("No experimenters to update.");
    	loadCall = updateExperimenters(group, experimenters);
    }
    
}
