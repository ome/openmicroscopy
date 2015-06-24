/*
 * org.openmicroscopy.shoola.env.data.views.AdminViewImpl 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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
import java.io.File;
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.login.UserCredentials;
import org.openmicroscopy.shoola.env.data.model.AdminObject;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.calls.AdminLoader;
import org.openmicroscopy.shoola.env.data.views.calls.AdminSaver;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import pojos.DataObject;
import pojos.ExperimenterData;
import pojos.GroupData;

/** 
 * Implementation of the {@link AdminView} implementation.
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
class AdminViewImpl 
	implements AdminView
{

	/**
	 * Implemented as specified by the {@link AdminView} interface.
	 * @see AdminView#updateExperimenter(SecurityContext, ExperimenterData,
	 * 										AgentEventListener)
	 */
	public CallHandle updateExperimenter(SecurityContext ctx,
			ExperimenterData exp, AgentEventListener observer)
	{
		BatchCallTree cmd = new AdminLoader(ctx, exp,
				AdminLoader.EXPERIMENTER_UPDATE);
		return cmd.exec(observer);
	}
	
	/**
         * Implemented as specified by the {@link AdminView} interface.
         * @see AdminView#updateGroup(SecurityContext, GroupData, AgentEventListener)
         */
        public CallHandle updateGroup(SecurityContext ctx, GroupData group, AgentEventListener observer)
        {
                BatchCallTree cmd = new AdminLoader(ctx, group);
                return cmd.exec(observer);
        }
	
	/**
	 * Implemented as specified by the {@link AdminView} interface.
	 * @see AdminView#changePassword(SecurityContext, String, String, AgentEventListener)
	 */
	public CallHandle changePassword(SecurityContext ctx, String oldPassword,
			String newPassword, AgentEventListener observer)
	{
		BatchCallTree cmd = new AdminLoader(ctx, oldPassword, newPassword);
		return cmd.exec(observer);
	}
	
	/**
	 * Implemented as specified by the {@link AdminView} interface.
	 * @see AdminView#getDiskSpace(SecurityContext, Class, long, AgentEventListener)
	 */
	public CallHandle getDiskSpace(SecurityContext ctx, Class type, long id,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new AdminLoader(ctx, type, id);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the {@link AdminView} interface.
	 * @see AdminView#createExperimenters(SecurityContext, AdminObject, AgentEventListener)
	 */
	public CallHandle createExperimenters(SecurityContext ctx,
			AdminObject object, AgentEventListener observer)
	{
		BatchCallTree cmd = new AdminSaver(ctx, object);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the {@link AdminView} interface.
	 * @see AdminView#createGroup(SecurityContext, AdminObject, AgentEventListener)
	 */
	public CallHandle createGroup(SecurityContext ctx, AdminObject object,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new AdminSaver(ctx, object);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the {@link AdminView} interface.
	 * @see AdminView#loadExperimenterGroups(SecurityContext, long, AgentEventListener)
	 */
	public CallHandle loadExperimenterGroups(SecurityContext ctx, long groupID,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new AdminLoader(ctx, groupID, AdminLoader.GROUPS);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the {@link AdminView} interface.
	 * @see AdminView#loadExperimenters(SecurityContext, long, AgentEventListener)
	 */
	public CallHandle loadExperimenters(SecurityContext ctx, long groupID,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new AdminLoader(ctx, groupID,
				AdminLoader.EXPERIMENTERS);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the {@link AdminView} interface.
	 * @see AdminView#deleteObjects(SecurityContext, List, AgentEventListener)
	 */
	public CallHandle deleteObjects(SecurityContext ctx,
			List<DataObject> objects, AgentEventListener observer)
	{
		BatchCallTree cmd = new AdminSaver(ctx, objects, AdminSaver.DELETE);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the {@link AdminView} interface.
	 * @see AdminView#updateExperimenters(SecurityContext, Map, AgentEventListener)
	 */
	public CallHandle updateExperimenters(SecurityContext ctx, GroupData group,
			Map<ExperimenterData, UserCredentials> experimenters,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new AdminLoader(ctx, group, experimenters);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the {@link AdminView} interface.
	 * @see AdminView#resetExperimentersPassword(SecurityContext, AdminObject, 
	 * AgentEventListener)
	 */
	public CallHandle resetExperimentersPassword(SecurityContext ctx, 
			AdminObject object, AgentEventListener observer)
	{
		BatchCallTree cmd = new AdminSaver(ctx, object);
		return cmd.exec(observer);
	}
	
	/**
	 * Implemented as specified by the {@link AdminView} interface.
	 * @see AdminView#activateExperimenters(SecurityContext, AdminObject, AgentEventListener)
	 */
	public CallHandle activateExperimenters(SecurityContext ctx,
			AdminObject object, AgentEventListener observer)
	{
		BatchCallTree cmd = new AdminSaver(ctx, object);
		return cmd.exec(observer);
	}
	
	/**
	 * Implemented as specified by the {@link AdminView} interface.
	 * @see AdminView#loadExperimenterPhoto(SecurityContext, ExperimenterData,
	 * AgentEventListener)
	 */
	public CallHandle loadExperimenterPhoto(SecurityContext ctx,
			ExperimenterData experimenter, AgentEventListener observer)
	{
		BatchCallTree cmd = new AdminLoader(ctx, experimenter, 
				AdminLoader.EXPERIMENTER_PHOTO);
		return cmd.exec(observer);
	}
	
	/**
	 * Implemented as specified by the {@link AdminView} interface.
	 * @see AdminView#uploadExperimenterPhoto(SecurityContext, ExperimenterData, File, String
	 * AgentEventListener)
	 */
	public CallHandle uploadExperimenterPhoto(SecurityContext ctx,
			ExperimenterData experimenter, File photo, String format,
			AgentEventListener observer)
	{
		BatchCallTree cmd = new AdminLoader(ctx, experimenter, photo, format);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the {@link AdminView} interface.
	 * @see AdminView#getDiskSpace(SecurityContext, Class, List, AgentEventListener)
	 */
	public CallHandle getDiskSpace(SecurityContext ctx, Class type,
			List<Long> ids, AgentEventListener observer)
	{
		BatchCallTree cmd = new AdminLoader(ctx, type, ids);
		return cmd.exec(observer);
	}

	/**
	 * Implemented as specified by the {@link AdminView} interface.
	 * @see AdminView#loadAdministrators(SecurityContext, AgentEventListener)
	 */
	public CallHandle loadAdministrators(SecurityContext ctx,
			AgentEventListener observer)
	{
		return null;
	}

	/**
	 * Implemented as specified by the {@link AdminView} interface.
	 * @see AdminView#loadAdministrators(SecurityContext, AgentEventListener)
	 */
	public CallHandle changeGroup(SecurityContext ctx, GroupData group,
            ExperimenterData experimenter, AgentEventListener observer)
	{
	    BatchCallTree cmd = new AdminLoader(ctx, group, experimenter);
        return cmd.exec(observer);
	}

    /**
     * Implemented as specified by the {@link AdminView} interface.
     * @see AdminView#loadAdministrators(SecurityContext, AgentEventListener)
     */
    public CallHandle lookupLdapAuthExperimenter(SecurityContext ctx,
            long userID, AgentEventListener observer)
    {
        BatchCallTree cmd = new AdminLoader(ctx, userID);
        return cmd.exec(observer);
    }
}
