/*
 * org.openmicroscopy.shoola.env.data.AdminServiceImpl 
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
package org.openmicroscopy.shoola.env.data;


//Java imports
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import omero.model.ExperimenterGroup;
import omero.model.Permissions;
import omero.model.PermissionsI;

import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.AgentInfo;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.login.UserCredentials;
import org.openmicroscopy.shoola.env.data.model.AdminObject;
import org.openmicroscopy.shoola.env.data.util.PojoMapper;

import pojos.ExperimenterData;
import pojos.GroupData;
import pojos.PermissionData;

/** 
 * Implementation of the {@link AdminService} I/F.
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
class AdminServiceImpl
	implements AdminService
{

	/** Uses it to gain access to the container's services. */
	private Registry                context;

	/** Reference to the entry point to access the <i>OMERO</i> services. */
	private OMEROGateway            gateway;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param gateway   Reference to the OMERO entry point.
	 *                  Mustn't be <code>null</code>.
	 * @param registry  Reference to the registry. Mustn't be <code>null</code>.
	 */
	AdminServiceImpl(OMEROGateway gateway, Registry registry)
	{
		if (registry == null)
			throw new IllegalArgumentException("No registry.");
		if (gateway == null)
			throw new IllegalArgumentException("No gateway.");
		context = registry;
		this.gateway = gateway;
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#getServerName()
	 */
	public String getServerName() 
	{
		UserCredentials uc = (UserCredentials) 
		context.lookup(LookupNames.USER_CREDENTIALS);
		return uc.getHostName();
	}

	/**
	 * Implemented as specified by {@link AdminService}.
	 * @see AdminService#getServerVersion()
	 */
	public String getServerVersion()
	{
		try {
			return gateway.getServerVersion();
		} catch (Exception e) {
			//ignore it.
		}
		return "";
	}

	/**
	 * Implemented as specified by {@link AdminService}.
	 * @see AdminService#getLoggingName()
	 */
	public String getLoggingName() 
	{
		UserCredentials uc = (UserCredentials) 
		context.lookup(LookupNames.USER_CREDENTIALS);
		return uc.getUserName();
	}
	
	/**
	 * Implemented as specified by {@link AdminService}.
	 * @see AdminService#getSpace(int, long)
	 */
	public long getSpace(int index, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		switch (index) {
			case USED: return gateway.getUsedSpace();
			case FREE: return gateway.getFreeSpace();
		}
		return -1;
	}
	
	/**
	 * Implemented as specified by {@link AdminService}.
	 * @see AdminService#changePassword(String, String)
	 */
	public Boolean changePassword(String oldPassword, String newPassword) 
		throws DSOutOfServiceException, DSAccessException 
	{
		if (newPassword == null || newPassword.trim().length() == 0)
			throw new IllegalArgumentException("Password not valid.");
		UserCredentials uc = (UserCredentials) 
		context.lookup(LookupNames.USER_CREDENTIALS);
		if (!uc.getPassword().equals(oldPassword)) return Boolean.FALSE;

		gateway.changePassword(newPassword);
		uc.resetPassword(newPassword);
		return Boolean.TRUE;
	}
	
	/**
	 * Implemented as specified by {@link AdminService}.
	 * @see AdminService#changeExperimenterGroup(ExperimenterData, GroupData)
	 */
	public void changeExperimenterGroup(ExperimenterData exp, GroupData 
			group)
		throws DSOutOfServiceException, DSAccessException
	{
		if (exp == null) 
			throw new DSAccessException("No object to update.");
		if (group == null)
			throw new DSAccessException("No group specified.");
		if (group != null && exp.getDefaultGroup().getId() != group.getId()) {
			gateway.changeCurrentGroup(exp, group.getId());
		}
		UserCredentials uc = (UserCredentials) 
			context.lookup(LookupNames.USER_CREDENTIALS);
		ExperimenterData data = gateway.getUserDetails(uc.getUserName());
		
		context.bind(LookupNames.CURRENT_USER_DETAILS, data);
//		Bind user details to all agents' registry.
		List agents = (List) context.lookup(LookupNames.AGENTS);
		Iterator i = agents.iterator();
		AgentInfo agentInfo;
		while (i.hasNext()) {
			agentInfo = (AgentInfo) i.next();
			if (agentInfo.isActive()) {
				agentInfo.getRegistry().bind(
						LookupNames.CURRENT_USER_DETAILS, data);
			}
		}
	}
	
	/**
	 * Implemented as specified by {@link AdminService}.
	 * @see AdminService#updateExperimenter(ExperimenterData, GroupData)
	 */
	public ExperimenterData updateExperimenter(ExperimenterData exp, GroupData 
			group) 
		throws DSOutOfServiceException, DSAccessException 
	{
		//ADD control
		if (exp == null) 
			throw new DSAccessException("No object to update.");
		UserCredentials uc = (UserCredentials) 
			context.lookup(LookupNames.USER_CREDENTIALS);
		gateway.updateExperimenter(exp.asExperimenter());
		ExperimenterData data;
		if (group != null && exp.getDefaultGroup().getId() != group.getId()) {
			gateway.changeCurrentGroup(exp, group.getId());
		}
		data = gateway.getUserDetails(uc.getUserName());
		
		context.bind(LookupNames.CURRENT_USER_DETAILS, data);
//		Bind user details to all agents' registry.
		List agents = (List) context.lookup(LookupNames.AGENTS);
		Iterator i = agents.iterator();
		AgentInfo agentInfo;
		while (i.hasNext()) {
			agentInfo = (AgentInfo) i.next();
			if (agentInfo.isActive()) {
				agentInfo.getRegistry().bind(
						LookupNames.CURRENT_USER_DETAILS, data);
			}
		}
		return data;
	}

	/**
	 * Implemented as specified by {@link AdminService}.
	 * @see AdminService#createExperimenters(AdminObject)
	 */
	public List<ExperimenterData> createExperimenters(AdminObject object)
		throws DSOutOfServiceException, DSAccessException
	{
		if (object == null)
			throw new IllegalArgumentException("No object.");
		Map<ExperimenterData, UserCredentials> m = object.getExperimenters();
		if (m == null || m.size() == 0)
			throw new IllegalArgumentException("No experimenters to create.");
		return gateway.createExperimenters(object);
	}

	/**
	 * Implemented as specified by {@link AdminService}.
	 * @see AdminService#createGroup(AdminObject)
	 */
	public GroupData createGroup(AdminObject object)
		throws DSOutOfServiceException, DSAccessException
	{
		if (object == null)
			throw new IllegalArgumentException("No object.");
		if (object.getGroup() == null)
			throw new IllegalArgumentException("No group.");
		return gateway.createGroup(object);
	}

	/**
	 * Implemented as specified by {@link AdminService}.
	 * @see AdminService#loadExperimenters(long)
	 */
	public List<ExperimenterData> loadExperimenters(long id)
		throws DSOutOfServiceException, DSAccessException
	{
		List<ExperimenterData> l = new ArrayList<ExperimenterData>();
		return l;
	}

	/**
	 * Implemented as specified by {@link AdminService}.
	 * @see AdminService#loadGroups(long)
	 */
	public List<GroupData> loadGroups(long id) 
		throws DSOutOfServiceException, DSAccessException
	{
		return gateway.loadGroups(id);
	}
	
	/**
	 * Implemented as specified by {@link AdminService}.
	 * @see AdminService#deleteExperimenters(List)
	 */
	public List<ExperimenterData> deleteExperimenters(
			List<ExperimenterData> experimenters)
		throws DSOutOfServiceException, DSAccessException
	{
		if (experimenters == null || experimenters.size() == 0)
			throw new IllegalArgumentException("No experimenters to delete.");
		return gateway.deleteExperimenters(experimenters);
	}
	
	/**
	 * Implemented as specified by {@link AdminService}.
	 * @see AdminService#deleteGroups(List)
	 */
	public List<GroupData> deleteGroups(List<GroupData> groups)
		throws DSOutOfServiceException, DSAccessException
	{
		if (groups == null || groups.size() == 0)
			throw new IllegalArgumentException("No groups to delete.");
		return gateway.deleteGroups(groups);
	}

	/**
	 * Implemented as specified by {@link AdminService}.
	 * @see AdminService#getPermissionLevel()
	 */
	public int getPermissionLevel()
	{
		ExperimenterData exp = (ExperimenterData) context.lookup(
				LookupNames.CURRENT_USER_DETAILS);
		GroupData g = exp.getDefaultGroup();
		PermissionData perm = g.getPermissions();
		if (perm.isGroupRead()) {
			if (perm.isGroupWrite())  
				return AdminObject.PERMISSIONS_GROUP_READ_WRITE;
			return AdminObject.PERMISSIONS_GROUP_READ;
		}
		if (perm.isWorldRead()) {
			if (perm.isWorldWrite())  
				return AdminObject.PERMISSIONS_PUBLIC_READ_WRITE;
			return AdminObject.PERMISSIONS_PUBLIC_READ;
		}
		//Check if the user is owner of the group.
		Set leaders = g.getLeaders();
		if (leaders == null || leaders.size() == 0) 
			return AdminObject.PERMISSIONS_PRIVATE;
		Iterator j = leaders.iterator();
		long id = exp.getId();
		while (j.hasNext()) {
			exp = (ExperimenterData) j.next();
			if (exp.getId() == id) 
				return AdminObject.PERMISSIONS_GROUP_READ;
		}
		return AdminObject.PERMISSIONS_PRIVATE;
	}

	/**
	 * Implemented as specified by {@link AdminService}.
	 * @see AdminService#updateGroup(GroupData, int)
	 */
	public GroupData updateGroup(GroupData group, int permissions)
			throws DSOutOfServiceException, DSAccessException
	{
		if (group == null)
			throw new IllegalArgumentException("No group to update.");
		ExperimenterGroup g = group.asGroup();
		Permissions p = null;
		if (permissions != -1) {
			p = g.getDetails().getPermissions();
			switch (permissions) {
				case AdminObject.PERMISSIONS_GROUP_READ:
					p.setGroupRead(true);
					break;
				case AdminObject.PERMISSIONS_GROUP_READ_WRITE:
					p.setGroupRead(true);
					p.setGroupWrite(true);
					break;
				case AdminObject.PERMISSIONS_PUBLIC_READ:
					p.setWorldRead(true);
					break;
				case AdminObject.PERMISSIONS_PUBLIC_READ_WRITE:
					p.setWorldRead(true);
					p.setWorldWrite(true);
			}
		}
		return gateway.updateGroup(g, p);
	}
	
}
