/*
 * org.openmicroscopy.shoola.env.data.views.calls.AdminLoader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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


import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openmicroscopy.shoola.util.CommonsLangUtils;

import org.openmicroscopy.shoola.env.data.AdminService;
import org.openmicroscopy.shoola.env.data.OmeroImageService;
import org.openmicroscopy.shoola.env.data.login.UserCredentials;
import org.openmicroscopy.shoola.env.data.model.DiskQuota;
import omero.gateway.SecurityContext;
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
     * @param ctx The security context.
     * @param type Either ExperimenterData or GroupData class.
     * @param id The id of the user, group or <code>-1</code>.
     * @param type Either experimenter or group.
     * @return The {@link BatchCall}.
     */
    private BatchCall availableSpaceCall(final SecurityContext ctx,
    		final Class type, final long id)
    {
        return new BatchCall("Loading disk space information") {
            public void doCall() throws Exception
            {
                AdminService os = context.getAdminService();
                result = os.getQuota(ctx, type, id);
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to retrieve the available and used
     * disk space.
     * 
     * @param ctx The security context.
     * @param type Either ExperimenterData or GroupData class.
     * @param ids The id of the users or groups.
     * @param type Either experimenter or group.
     * @return The {@link BatchCall}.
     */
    private BatchCall availableSpaceCall(final SecurityContext ctx,
    		final Class type, final List<Long> ids)
    {
        return new BatchCall("Loading disk space information") {
            public void doCall() throws Exception
            {
                AdminService os = context.getAdminService();
                List<DiskQuota> list = new ArrayList<DiskQuota>();
                Iterator<Long> i = ids.iterator();
                while (i.hasNext()) {
                	list.add(os.getQuota(ctx, type, i.next()));
				}
                result = list;
            }
        };
    }
    
    
    /**
     * Creates a {@link BatchCall} to change the password
     * of the user currently logged in.
     * 
     * @param ctx The security context.
	 * @param oldPassword 	The password used to log in.
     * @param newPassword	The new password value.
     * @return The {@link BatchCall}.
     */
    private BatchCall changePassword(final SecurityContext ctx,
    		final String oldPassword, final String newPassword)
    {
        return new BatchCall("Change password") {
            public void doCall() throws Exception
            {
            	AdminService os = context.getAdminService();
                result = os.changePassword(ctx, oldPassword, newPassword);
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to update the specified experimenter.
     * 
     * @param ctx The security context.
	 * @param exp 	The experimenter to update.
     * @return The {@link BatchCall}.
     */
    private BatchCall updateExperimenter(final SecurityContext ctx,
    		final ExperimenterData exp)
    {
        return new BatchCall("Update experimenter") {
            public void doCall() throws Exception
            {
            	AdminService os = context.getAdminService();
                result = os.updateExperimenter(ctx, exp, null);
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to update the specified group.
     * 
     * @param ctx The security context.
	 * @param group The group to update.
	 * @param permissions The desired permissions level or <code>-1</code>.
     * @return The {@link BatchCall}.
     */
    private BatchCall updateGroup(final SecurityContext ctx,
    		final GroupData group)
    {
        return new BatchCall("Update group") {
            public void doCall() throws Exception
            {
            	AdminService os = context.getAdminService();
                result = os.updateGroup(ctx, group);
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to update the specified group.
     * 
     * @param ctx The security context.
     * @param group The group to update.
     * @param experimenter The user to update.
     * @return The {@link BatchCall}.
     */
    private BatchCall changeGroup(final SecurityContext ctx,
            final GroupData group, final ExperimenterData experimenter)
    {
        return new BatchCall("Change the default group") {
            public void doCall() throws Exception
            {
                AdminService os = context.getAdminService();
                result = os.changeExperimenterGroup(ctx, experimenter,
                        group.getId());
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to load all the available group if the 
     * passed parameter is <code>-1</code>, load the group otherwise.
     * 
     * @param ctx The security context.
	 * @param id The id of the group.
     * @return The {@link BatchCall}.
     */
    private BatchCall loadGroup(final SecurityContext ctx, final long id)
    {
        return new BatchCall("Load groups") {
            public void doCall() throws Exception
            {
            	AdminService os = context.getAdminService();
                result = os.loadGroups(ctx, id);
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to load the experimenters contained 
     * within the specified group.
     * 
     * @param ctx The security context.
	 * @param id The id of the group.
     * @return The {@link BatchCall}.
     */
    private BatchCall loadExperimenters(final SecurityContext ctx,
    		final long id)
    {
        return new BatchCall("Load experimenters") {
            public void doCall() throws Exception
            {
            	AdminService os = context.getAdminService();
                result = os.loadExperimenters(ctx, id);
            }
        };
    }

    /**
     * Creates a {@link BatchCall} to load the experimenters contained 
     * within the specified group.
     * 
     * @param ctx The security context.
     * @param id The id of the user.
     * @return The {@link BatchCall}.
     */
    private BatchCall lookupExperimenter(final SecurityContext ctx,
            final long id)
    {
        return new BatchCall("lookupLdapAuthExperimenter") {
            public void doCall() throws Exception
            {
                AdminService os = context.getAdminService();
                result = os.lookupLdapAuthExperimenter(ctx, id);
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to update the specified experimenters.
     * 
     * @param ctx The security context.
	 * @param experimenters The experimenters to update. 
     * @return The {@link BatchCall}.
     */
    private BatchCall updateExperimenters(final SecurityContext ctx,
    	final GroupData group, 
    	final Map<ExperimenterData, UserCredentials> experimenters)
    {
        return new BatchCall("Update experimenters") {
            public void doCall() throws Exception
            {
            	AdminService os = context.getAdminService();
                result = os.updateExperimenters(ctx, group, experimenters);
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to load the photo.
     * 
     * @param ctx The security context.
	 * @param experimenter The experimenter to handle. 
     * @return The {@link BatchCall}.
     */
    private BatchCall loadExperimenterPhoto(final SecurityContext ctx,
    		final ExperimenterData experimenter)
    {
        return new BatchCall("Load photo") {
            public void doCall() throws Exception
            {
            	OmeroImageService os = context.getImageService();
            	List<DataObject> exps = new ArrayList<DataObject>();
            	exps.add(experimenter);
            	Map<DataObject, BufferedImage> map = 
            		os.getExperimenterThumbnailSet(ctx, exps, 96);
               result = map.get(experimenter);
           }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to load the photo.
     * 
     * @param ctx The security context.
	 * @param experimenter The experimenter to handle.
	 * @param photo The photo to upload.
	 * @param format The format of the file.
     * @return The {@link BatchCall}.
     */
    private BatchCall uploadExperimenterPhoto(final SecurityContext ctx,
    		final ExperimenterData experimenter, final File photo,
    		final String format)
    {
        return new BatchCall("Update experimenters") {
            public void doCall() throws Exception
            {
            	AdminService svc = context.getAdminService();
            	result = svc.uploadUserPhoto(ctx, photo, format, experimenter);
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
     * @param id		The id of the user or <code>-1</code>.
     * @param index 	One of the constants defined by this class.
     */
    public AdminLoader(SecurityContext ctx, long id, int index)
    {
    	switch (index) {
			case SPACE:
				loadCall = availableSpaceCall(ctx, ExperimenterData.class, id);
				break;
			case GROUPS:
				loadCall = loadGroup(ctx, id);
				break;
			case EXPERIMENTERS:
				loadCall = loadExperimenters(ctx, id);
		}
    }
    
    /** 
     * Creates a new instance. 
     * 
     * @param ctx The security context.
     * @param type	Either <code>ExperimenterData</code> or
     * 				<code>GroupData</code>
     * @param id	The id of the user or <code>-1</code>.
     */
    public AdminLoader(SecurityContext ctx, Class type, long id)
    {
    	loadCall = availableSpaceCall(ctx, type, id);
    }

    /** 
     * Creates a new instance. 
     * 
     * @param ctx The security context.
     * @param type	Either <code>ExperimenterData</code> or
     * 				<code>GroupData</code>
     * @param ids	The id of the user or groups.
     */
    public AdminLoader(SecurityContext ctx, Class type, List<Long> ids)
    {
    	loadCall = availableSpaceCall(ctx, type, ids);
    }
    
    /**
     * Creates a new instance.
     *
     * @param ctx The security context.
     * @param oldPassword The password used to log in.
     * @param newPassword The new password value.
     */
    public AdminLoader(SecurityContext ctx, String oldPassword,
            String newPassword)
    {
        if (CommonsLangUtils.isBlank(newPassword) ||
                CommonsLangUtils.isBlank(oldPassword))
            throw new IllegalArgumentException("Password not valid.");
        loadCall = changePassword(ctx, oldPassword, newPassword);
    }

    /**
     * Creates a new instance.
     * 
     * @param ctx The security context.
     * @param exp The experimenter to update. Mustn't be <code>null</code>.
     */
    public AdminLoader(SecurityContext ctx, ExperimenterData exp, int index)
    {
    	if (exp == null)
    		throw new IllegalArgumentException("Experimenter not valid.");
    	switch (index) {
    		case EXPERIMENTER_UPDATE:
				loadCall = updateExperimenter(ctx, exp);
				break;
			case EXPERIMENTER_PHOTO:
				loadCall = loadExperimenterPhoto(ctx, exp);
		}
    }
    
    /**
     * Creates a new instance.
     * 
     * @param ctx The security context.
     * @param exp 	The experimenter to update. Mustn't be <code>null</code>.
     * @param photo The photo to upload. Mustn't be <code>null</code>.
     * @param format The format of the photo to upload.
     */
    public AdminLoader(SecurityContext ctx, ExperimenterData exp, File photo,
    		String format)
    {
    	if (exp == null)
    		throw new IllegalArgumentException("Experimenter not valid.");
    	if (photo == null)
    		throw new IllegalArgumentException("Photo not valid.");
    	loadCall = uploadExperimenterPhoto(ctx, exp, photo, format);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param ctx The security context.
     * @param group The group to update. Mustn't be <code>null</code>.
     */
    public AdminLoader(SecurityContext ctx, GroupData group)
    {
    	if (group == null)
    		throw new IllegalArgumentException("Group not valid.");
    	loadCall = updateGroup(ctx, group);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param ctx The security context.
     * @param group The default group to set.
     * @param experimenters The experimenters to update.
     * 						Mustn't be <code>null</code>.
     */
    public AdminLoader(SecurityContext ctx, GroupData group,
    		Map<ExperimenterData, UserCredentials> experimenters)
    {
    	if (experimenters == null)
    		throw new IllegalArgumentException("No experimenters to update.");
    	loadCall = updateExperimenters(ctx, group, experimenters);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param ctx The security context.
     * @param group The default group to set.
     * @param experimenter The experimenter to update the default group.
     */
    public AdminLoader(SecurityContext ctx, GroupData group,
            ExperimenterData experimenter)
    {
        if (group == null)
            throw new IllegalArgumentException("No group indicated.");
        loadCall = changeGroup(ctx, group, experimenter);
    }

    /**
     * Creates a new instance.
     * 
     * @param ctx The security context.
     * @param userID The experimenter to handle.
     */
    public AdminLoader(SecurityContext ctx, long userID)
    {
        loadCall = lookupExperimenter(ctx, userID);
    }

}
