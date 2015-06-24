/*
 * org.openmicroscopy.shoola.env.data.AdminServiceImpl
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
package org.openmicroscopy.shoola.env.data;


//Java imports
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.openmicroscopy.shoola.util.CommonsLangUtils;

import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.sys.Roles;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.AgentInfo;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.events.DSCallAdapter;
import org.openmicroscopy.shoola.env.data.login.UserCredentials;
import org.openmicroscopy.shoola.env.data.model.AdminObject;
import org.openmicroscopy.shoola.env.data.model.DiskQuota;
import org.openmicroscopy.shoola.env.data.util.PojoMapper;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;

import pojos.DataObject;
import pojos.ExperimenterData;
import pojos.GroupData;

/** 
 * Implementation of the {@link AdminService} I/F.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
class AdminServiceImpl
	implements AdminService
{

	/** Uses it to gain access to the container's services. */
	private Registry context;

	/** Reference to the entry point to access the <i>OMERO</i> services. */
	private OMEROGateway gateway;
	
	/**
	 * Updates the experimenter.
	 * 
	 * @param exp The experimenter to update.
	 * @param group The current group to set if any.
	 * @param asAdmin Pass <code>true</code> if updated as an administrator,
	 * 				  <code>false</code> otherwise.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service.
	 */
	private ExperimenterData updateExperimenter(SecurityContext ctx,
			ExperimenterData exp, GroupData group, boolean asAdmin) 
		throws DSOutOfServiceException, DSAccessException
	{
		ExperimenterData currentUser = (ExperimenterData)
		context.lookup(LookupNames.CURRENT_USER_DETAILS);
		if (!asAdmin && exp.getId() != currentUser.getId()) return exp;
		UserCredentials uc = (UserCredentials) 
		context.lookup(LookupNames.USER_CREDENTIALS);
		gateway.updateExperimenter(ctx, exp.asExperimenter(),
				currentUser.getId());
		ExperimenterData data;
		if (group != null && exp.getDefaultGroup().getId() != group.getId()) {
			gateway.changeCurrentGroup(ctx, exp, group.getId());
		}
		String userName = uc.getUserName();
		if (asAdmin) userName = exp.getUserName();
		data = gateway.getUserDetails(ctx, userName, true);
		if (currentUser.getId() != exp.getId()) 
			return data;

		context.bind(LookupNames.CURRENT_USER_DETAILS, data);
		//	Bind user details to all agents' registry.
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
		if (uc == null) return "";
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
	 * @see AdminService#getQuota(SecurityContext, Class, long)
	 */
	public DiskQuota getQuota(SecurityContext ctx, Class type, long id)
		throws DSOutOfServiceException, DSAccessException
	{
		long v = 1000;
		long used = 0;//gateway.getUsedSpace(type, id);
		long available = gateway.getFreeSpace(ctx, type, id);
		int t = DiskQuota.USER;
		if (GroupData.class.equals(type))
			t = DiskQuota.GROUP;
		return new DiskQuota(t, id, used*v, available*v);
	}
	
	/**
	 * Implemented as specified by {@link AdminService}.
	 * @see AdminService#changePassword(SecurityContext, String, String)
	 */
	public Boolean changePassword(SecurityContext ctx, String oldPassword,
			String newPassword) 
		throws DSOutOfServiceException, DSAccessException 
	{
		if (CommonsLangUtils.isBlank(newPassword))
			throw new IllegalArgumentException("Password not valid.");
		UserCredentials uc = (UserCredentials) 
		context.lookup(LookupNames.USER_CREDENTIALS);
		if (!uc.getPassword().equals(oldPassword)) 
			return Boolean.valueOf(false);

		gateway.changePassword(ctx, newPassword, oldPassword);
		uc.resetPassword(newPassword);
		return Boolean.valueOf(true);
	}
	
	/**
	 * Implemented as specified by {@link AdminService}.
	 * @see AdminService#changeExperimenterGroup(SecurityContext, ExperimenterData, long)
	 */
	public ExperimenterData changeExperimenterGroup(SecurityContext ctx,
			ExperimenterData exp, long groupID)
		throws DSOutOfServiceException, DSAccessException
	{
		if (!gateway.isServerRunning(ctx)) return null;
		UserCredentials uc = (UserCredentials) 
		context.lookup(LookupNames.USER_CREDENTIALS);
		if (exp == null) {
			exp = gateway.getUserDetails(ctx, uc.getUserName(), false);
		}
		if (groupID < 0)
			throw new DSAccessException("No group specified.");
		if (exp.getDefaultGroup().getId() != groupID) {
			gateway.changeCurrentGroup(ctx, exp, groupID);
		}
		ExperimenterData data = gateway.getUserDetails(ctx, uc.getUserName(),
				true);
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
	 * @see AdminService#updateExperimenter(SecurityContext, ExperimenterData, GroupData)
	 */
	public ExperimenterData updateExperimenter(SecurityContext ctx,
			ExperimenterData exp, GroupData group) 
		throws DSOutOfServiceException, DSAccessException 
	{
		return updateExperimenter(ctx, exp, group, false);
	}

	/**
	 * Implemented as specified by {@link AdminService}.
	 * @see AdminService#createExperimenters(SecurityContext, AdminObject)
	 */
	public List<ExperimenterData> createExperimenters(SecurityContext ctx,
			AdminObject object)
		throws DSOutOfServiceException, DSAccessException
	{
		if (object == null)
			throw new IllegalArgumentException("No object.");
		Map<ExperimenterData, UserCredentials> m = object.getExperimenters();
		if (m == null || m.size() == 0)
			throw new IllegalArgumentException("No experimenters to create.");
		Roles roles = (Roles) context.lookup(LookupNames.SYSTEM_ROLES);
		return gateway.createExperimenters(ctx, object, roles);
	}

	/**
	 * Implemented as specified by {@link AdminService}.
	 * @see AdminService#addExperimenters(SecurityContext, GroupData, List)
	 */
	public void addExperimenters(SecurityContext ctx, GroupData group,
			List<ExperimenterData> experimenters)
		throws DSOutOfServiceException, DSAccessException
	{
		if (group == null)
			throw new IllegalArgumentException("No group to add " +
					"the experimenters to.");
		if (experimenters == null || experimenters.size() == 0)
			throw new IllegalArgumentException("No experimenters to add.");
		gateway.addExperimenters(ctx, group, experimenters);
	}
	
	/**
	 * Implemented as specified by {@link AdminService}.
	 * @see AdminService#createGroup(SecurityContext, AdminObject)
	 */
	public GroupData createGroup(SecurityContext ctx, AdminObject object)
		throws DSOutOfServiceException, DSAccessException
	{
		if (object == null)
			throw new IllegalArgumentException("No object.");
		if (object.getGroup() == null)
			throw new IllegalArgumentException("No group.");
		Roles roles = (Roles) context.lookup(LookupNames.SYSTEM_ROLES);
		return gateway.createGroup(ctx, object, roles);
	}

	/**
	 * Implemented as specified by {@link AdminService}.
	 * @see AdminService#loadExperimenters(SecurityContext, long)
	 */
	public List<ExperimenterData> loadExperimenters(SecurityContext ctx,
			long groupID)
		throws DSOutOfServiceException, DSAccessException
	{
		return gateway.loadExperimenters(ctx, groupID);
	}

	/**
	 * Implemented as specified by {@link AdminService}.
	 * @see AdminService#loadGroups(SecurityContext, long)
	 */
	public List<GroupData> loadGroups(SecurityContext ctx, long id) 
		throws DSOutOfServiceException, DSAccessException
	{
		return gateway.loadGroups(ctx, id);
	}
	
	/**
	 * Implemented as specified by {@link AdminService}.
	 * @see AdminService#loadGroupsForExperimenter(SecurityContext, long)
	 */
	public List<GroupData> loadGroupsForExperimenter(SecurityContext ctx,
			long id) 
		throws DSOutOfServiceException, DSAccessException
	{
		return gateway.loadGroupsForExperimenter(ctx, id);
	}
	
	/**
	 * Implemented as specified by {@link AdminService}.
	 * @see AdminService#deleteExperimenters(List)
	 */
	public List<ExperimenterData> deleteExperimenters(SecurityContext ctx,
			List<ExperimenterData> experimenters)
		throws DSOutOfServiceException, DSAccessException
	{
		if (CollectionUtils.isEmpty(experimenters))
			throw new IllegalArgumentException("No experimenters to delete.");
		return gateway.deleteExperimenters(ctx, experimenters);
	}
	
	/**
	 * Implemented as specified by {@link AdminService}.
	 * @see AdminService#deleteGroups(SecurityContext, List)
	 */
	public List<GroupData> deleteGroups(SecurityContext ctx,
			List<GroupData> groups)
		throws DSOutOfServiceException, DSAccessException
	{
	    if (CollectionUtils.isEmpty(groups))
			throw new IllegalArgumentException("No groups to delete.");
		return gateway.deleteGroups(ctx, groups);
	}

	/**
	 * Implemented as specified by {@link AdminService}.
	 * @see AdminService#updateGroup(SecurityContext, GroupData, int)
	 */
	public GroupData updateGroup(SecurityContext ctx, GroupData group)
			throws DSOutOfServiceException, DSAccessException
	{
		if (group == null)
			throw new IllegalArgumentException("No group to update.");
		gateway.updateGroup(ctx, group);
		gateway.joinSession();

		return reloadGroup(ctx, group);
	}
	
	/**
         * Implemented as specified by {@link AdminService}.
         * @see AdminService#updateGroupPermissions(SecurityContext, GroupData, int, DSCallAdapter)
         */
        public void updateGroupPermissions(SecurityContext ctx, GroupData group,
                        int permissions, DSCallAdapter adapter)
                        throws DSOutOfServiceException, DSAccessException
        {
                if (group == null)
                        throw new IllegalArgumentException("No group to update.");
                
                RequestCallback cb = gateway.updateGroupPermissions(ctx, group, permissions);
                cb.setAdapter(adapter);
        }
	
        /**
         * Implemented as specified by {@link AdminService}.
         * @see AdminService#reloadGroup(SecurityContext, GroupData)
         */
        public GroupData reloadGroup(SecurityContext ctx, GroupData group)
                throws DSOutOfServiceException, DSAccessException {
            group = gateway.loadGroups(ctx, group.getGroupId()).get(0);
            // Review update
            Collection groups = (Collection) context
                    .lookup(LookupNames.USER_GROUP_DETAILS);
            Iterator j = groups.iterator();
            GroupData g;
            Set available = new HashSet<GroupData>();
            while (j.hasNext()) {
                g = (GroupData) j.next();
                if (g.getId() == group.getId())
                    available.add(group);
                else
                    available.add(g);
            }
            context.bind(LookupNames.USER_GROUP_DETAILS, available);
            List agents = (List) context.lookup(LookupNames.AGENTS);
            Iterator i = agents.iterator();
            AgentInfo agentInfo;
            while (i.hasNext()) {
                agentInfo = (AgentInfo) i.next();
                if (agentInfo.isActive()) {
                    agentInfo.getRegistry().bind(LookupNames.USER_GROUP_DETAILS,
                            available);
                }
            }
            return group;
        }
	
	/**
	 * Implemented as specified by {@link AdminService}.
	 * @see AdminService#copyExperimenters(SecurityContext, GroupData, Set)
	 */
	public List<ExperimenterData> copyExperimenters(SecurityContext ctx,
			GroupData group, Collection experimenters) 
		throws DSOutOfServiceException, DSAccessException
	{
		if (group == null)
			throw new IllegalArgumentException("No group specified.");
		if (CollectionUtils.isEmpty(experimenters))
			return new ArrayList<ExperimenterData>();
		return gateway.copyExperimenters(ctx, group, experimenters);
	}

	/**
	 * Implemented as specified by {@link AdminService}.
	 * @see AdminService#cutAndPasteExperimenters(SecurityContext, Map, Map)
	 */
	public List<ExperimenterData> cutAndPasteExperimenters(
			SecurityContext ctx, Map toPaste, Map toCut)
			throws DSOutOfServiceException, DSAccessException
	{
		if (toPaste == null) toPaste = new HashMap();
		if (toCut == null) toCut = new HashMap();
		Iterator i;
		Object parent;
		Entry entry;
		List<ExperimenterData> r = new ArrayList<ExperimenterData>();
		i = toCut.entrySet().iterator();
		while (i.hasNext()) {
			entry = (Entry) i.next();
			parent = entry.getKey();
			if (parent instanceof GroupData)
				r.addAll(gateway.removeExperimenters(ctx, (GroupData) parent, 
						(Set) entry.getValue()));
		}

		i = toPaste.entrySet().iterator();

		while (i.hasNext()) {
			entry = (Entry) i.next();
			parent = entry.getKey();
			if (parent instanceof GroupData) //b/c of orphaned container
				r.addAll(copyExperimenters(ctx, (GroupData) parent, 
						(Set) entry.getValue()));
		}
		
		return r;
	}

	/**
	 * Implemented as specified by {@link AdminService}.
	 * @see AdminService#countExperimenters(SecurityContext, List)
	 */
	public Map<Long, Long> countExperimenters(SecurityContext ctx,
			List<Long> ids)
			throws DSOutOfServiceException, DSAccessException
	{
		if (CollectionUtils.isEmpty(ids))
			return new HashMap<Long, Long>();
		return gateway.countExperimenters(ctx, ids);
	}

	/**
	 * Implemented as specified by {@link AdminService}.
	 * @see AdminService#updateExperimenters(SecurityContext, GroupData, Map)
	 */
	public Map<ExperimenterData, Exception> updateExperimenters(
			SecurityContext ctx, GroupData group,
			Map<ExperimenterData, UserCredentials> experimenters)
			throws DSOutOfServiceException, DSAccessException
	{
		if (experimenters == null)
			throw new IllegalArgumentException("No experimenters to update");
		Entry entry;
		Iterator i = experimenters.entrySet().iterator();
		Map<ExperimenterData, Exception> 
		l = new HashMap<ExperimenterData, Exception>();
		List<Experimenter> ownersToAdd = new ArrayList<Experimenter>();
		List<Experimenter> ownersToRemove = new ArrayList<Experimenter>();
		List<ExperimenterData> administratorsToAdd = 
			new ArrayList<ExperimenterData>();
		List<ExperimenterData> 
		administratorsToRemove = new ArrayList<ExperimenterData>();
		List<ExperimenterData> toActivate = new ArrayList<ExperimenterData>();
		List<ExperimenterData> toDeactivate = new ArrayList<ExperimenterData>();
		
		ExperimenterData exp;
		UserCredentials uc;
		Boolean b;
		boolean reset = false;
		while (i.hasNext()) {
			entry = (Entry) i.next();
			exp = (ExperimenterData) entry.getKey();
			uc = (UserCredentials) entry.getValue();
			try {
				updateExperimenter(ctx, exp, group, true);
				//b = uc.isOwner();
				group = uc.getGroupToHandle();
				b = uc.isGroupOwner(group);
				if (b != null) {
					if (b.booleanValue()) ownersToAdd.add(exp.asExperimenter());
					else ownersToRemove.add(exp.asExperimenter());
				}
				b = uc.isAdministrator();
				if (b != null) {
					if (b.booleanValue()) 
						administratorsToAdd.add(exp);
					else administratorsToRemove.add(exp);
				}
				b = uc.isActive();
				if (b != null) {
					if (b.booleanValue()) 
						toActivate.add(exp);
					else toDeactivate.add(exp);
				}
				//Check owner
				//reset login name
				if (!exp.getUserName().equals(uc.getUserName())) {
					reset = gateway.resetUserName(ctx, uc.getUserName(), exp);
					if (!reset) {
						l.put(exp, new Exception(
								"The selected User Name is already taken."));
					}
				}
			} catch (Exception e) {
				l.put(exp, e);
			}
		}
		if (group != null) {
			if (ownersToAdd.size() > 0)
				gateway.handleGroupOwners(ctx, true, group.asGroup(),
						ownersToAdd);
			if (ownersToRemove.size() > 0)
				gateway.handleGroupOwners(ctx, false, group.asGroup(),
						ownersToRemove);
		}
		if (toActivate.size() > 0)
			gateway.modifyExperimentersRoles(ctx, true, toActivate,
					GroupData.USER);
		if (toDeactivate.size() > 0)
			gateway.modifyExperimentersRoles(ctx, false, toDeactivate,
					GroupData.USER);
		if (administratorsToAdd.size() > 0)
			gateway.modifyExperimentersRoles(ctx, true, administratorsToAdd,
					GroupData.SYSTEM);
		if (administratorsToRemove.size() > 0)
			gateway.modifyExperimentersRoles(ctx, false, administratorsToRemove,
					GroupData.SYSTEM);

		return l;
	}

	/**
	 * Implemented as specified by {@link AdminService}.
	 * @see AdminService#resetExperimentersPassword(SecurityContext, AdminObject)
	 */
	public List<ExperimenterData> resetExperimentersPassword(
			SecurityContext ctx, AdminObject object)
			throws DSOutOfServiceException, DSAccessException
	{
		if (object == null)
			throw new IllegalArgumentException("No experimenters" +
					" specified");
		if (AdminObject.RESET_PASSWORD != object.getIndex())
			throw new IllegalArgumentException("Index not valid");
		Map<ExperimenterData, UserCredentials> map = object.getExperimenters();
		if (map == null) 
			throw new IllegalArgumentException("No experimenters specified");
			
		List<ExperimenterData> l = new ArrayList<ExperimenterData>();
		UserCredentials uc;
		Entry<ExperimenterData, UserCredentials> entry;
		ExperimenterData exp;
		Iterator<Entry<ExperimenterData, UserCredentials>> i = map.entrySet().iterator();
		while (i.hasNext()) {
			entry = i.next();
			exp = entry.getKey();
			uc = entry.getValue();
			try {
				//check that the user is not ldap
				String ldap = gateway.lookupLdapAuthExperimenter(ctx,
						exp.getId());
				if (CommonsLangUtils.isNotBlank(ldap)) l.add(exp);
				else 
					gateway.resetPassword(ctx, exp.getUserName(), exp.getId(), 
						uc.getPassword());
			} catch (Exception e) {
				l.add(exp);
			}
		}
		return l;
	}

	/**
	 * Implemented as specified by {@link AdminService}.
	 * @see AdminService#activateExperimenters(SecurityContext, AdminObject)
	 */
	public List<ExperimenterData> activateExperimenters(SecurityContext ctx,
			AdminObject object)
			throws DSOutOfServiceException, DSAccessException
	{
		if (object == null)
			throw new IllegalArgumentException("No experimenters" +
					" specified");
		if (AdminObject.ACTIVATE_USER != object.getIndex())
			throw new IllegalArgumentException("Index not valid.");
		Map<ExperimenterData, UserCredentials> map = object.getExperimenters();
		if (map == null) 
			throw new IllegalArgumentException("No experimenters specified");
			
		List<ExperimenterData> l = new ArrayList<ExperimenterData>();
		UserCredentials uc;
		Entry entry;
		ExperimenterData exp;
		Iterator i = map.entrySet().iterator();
		List<ExperimenterData> toActivate = new ArrayList<ExperimenterData>();
		List<ExperimenterData> toDeactivate = new ArrayList<ExperimenterData>();
		
		while (i.hasNext()) {
			entry = (Entry) i.next();
			exp = (ExperimenterData) entry.getKey();
			uc = (UserCredentials) entry.getValue();
			if (uc.isActive()) toActivate.add(exp);
			else toDeactivate.add(exp);
		}
		if (toActivate.size() > 0)
			gateway.modifyExperimentersRoles(ctx, true, toActivate,
					GroupData.USER);
		if (toDeactivate.size() > 0)
			gateway.modifyExperimentersRoles(ctx, false, toDeactivate, 
					GroupData.USER);
		return l;
	}
	
	/**
	 * Implemented as specified by {@link AdminService}.
	 * @see AdminService#reloadPIGroups(SecurityContext, ExperimenterData)
	 */
	public List<GroupData> reloadPIGroups(SecurityContext ctx,
			ExperimenterData exp)
			throws DSOutOfServiceException, DSAccessException
	{
		Set<GroupData> groups;
		Set<GroupData> available;
		UserCredentials uc = (UserCredentials)
			context.lookup(LookupNames.USER_CREDENTIALS);
		List<ExperimenterData> exps = new ArrayList<ExperimenterData>();
		groups = gateway.getAvailableGroups(ctx, exp);
		//Check if the current experimenter is an administrator 
		Iterator<GroupData> i = groups.iterator();
		GroupData g;
		available = new HashSet<GroupData>();
		Roles roles = (Roles) context.lookup(LookupNames.SYSTEM_ROLES);
		while (i.hasNext()) {
			g = i.next();
			if (!isSecuritySystemGroup(g.getId())) {
				available.add(g);
			} else {
				if (g.getId() == roles.systemGroupId)
					uc.setAdministrator(true);
			}
		}
		context.bind(LookupNames.USER_GROUP_DETAILS, available);
		List<Long> ids = new ArrayList<Long>();
		i = available.iterator();
		Set set;
		Iterator j;
		ExperimenterData e;
		while (i.hasNext()) {
			g = (GroupData) i.next();
			set = g.getExperimenters();
			j = set.iterator();
			while (j.hasNext()) {
				e = (ExperimenterData) j.next();
				if (!ids.contains(e.getId())) {
					ids.add(e.getId());
					exps.add(e);
				}
			}
		}
		context.bind(LookupNames.USERS_DETAILS, exps);	
		List<GroupData> result = new ArrayList<GroupData>();
		Iterator<GroupData> k = available.iterator();
		while (k.hasNext()) {
			result.add(k.next());
		}
		
        //Bind user details to all agents' registry.
        List agents = (List) context.lookup(LookupNames.AGENTS);
		j = agents.iterator();
		AgentInfo agentInfo;
		Registry reg;
		while (i.hasNext()) {
			agentInfo = (AgentInfo) j.next();
			if (agentInfo.isActive()) {
				reg = agentInfo.getRegistry();
				reg.bind(LookupNames.USER_GROUP_DETAILS, available);
				reg.bind(LookupNames.USERS_DETAILS, exps);
			}
		}
		return result;
	}

	/**
	 * Implemented as specified by {@link AdminService}.
	 * @see AdminService#isExistingExperimenter(SecurityContext, String)
	 */
	public ExperimenterData lookupExperimenter(SecurityContext ctx,
			String name)
			throws DSOutOfServiceException, DSAccessException
	{
		Experimenter value = gateway.lookupExperimenter(ctx, name);
		if (value != null) 
			return (ExperimenterData) PojoMapper.asDataObject(value);
		return null;
	}

	/**
	 * Implemented as specified by {@link AdminService}.
	 * @see AdminService#isExistingGroup(SecurityContext, String)
	 */
	public GroupData lookupGroup(SecurityContext ctx, String name) 
		throws DSOutOfServiceException, DSAccessException
	{
		ExperimenterGroup value = gateway.lookupGroup(ctx, name);
		if (value != null) 
			return (GroupData) PojoMapper.asDataObject(value);
		return null;
	}

	/**
	 * Implemented as specified by {@link AdminService}.
	 * @see AdminService#uploadUserPhoto(SecurityContext, File, String, experimenter)
	 */
	public BufferedImage uploadUserPhoto(SecurityContext ctx, File f,
			String format, ExperimenterData experimenter)
			throws DSOutOfServiceException, DSAccessException
	{
		if (experimenter == null)
			throw new IllegalArgumentException("No experimenter specified.");
		if (f == null)
			throw new IllegalArgumentException("No photo specified.");
		
		long id = gateway.uploadExperimenterPhoto(ctx, f, format,
				experimenter.getId());
		if (id < 0) return null;
		List<DataObject> exp = new ArrayList<DataObject>();
		exp.add(experimenter);
		Map<DataObject, BufferedImage> map = 
			context.getImageService().getExperimenterThumbnailSet(ctx, exp, 0);
		return map.get(experimenter);
	}

	/**
	 * Implemented as specified by {@link AdminService}.
	 * @see AdminService#isConnected()
	 */
	public boolean isConnected() { return gateway.isConnected(); }
	
	/**
	 * Implemented as specified by {@link AdminService}.
	 * @see AdminService#getUserDetails()
	 */
	public ExperimenterData getUserDetails()
	{
		return (ExperimenterData) context.lookup(
				LookupNames.CURRENT_USER_DETAILS);
	}
	
	/**
	 * Implemented as specified by {@link AdminService}.
	 * @see AdminService#getAvailableUserGroups()
	 */
	public Collection<GroupData> getAvailableUserGroups()
	{
		return (Collection<GroupData>) context.lookup(
				LookupNames.USER_GROUP_DETAILS);
	}

    /**
     * Implemented as specified by {@link AdminService}.
     * @see AdminService#isSecuritySystemGroup(long)
     */
    public boolean isSecuritySystemGroup(long groupID)
    {
        Roles roles = (Roles) context.lookup(LookupNames.SYSTEM_ROLES);
        if (roles == null) return false;
        return (roles.guestGroupId == groupID ||
                roles.systemGroupId == groupID ||
                roles.userGroupId == groupID);
    }

    /**
     * Implemented as specified by {@link AdminService}.
     * @see AdminService#isSecuritySystemGroup(long, String)
     */
    public boolean isSecuritySystemGroup(long groupID, String key)
    {
        Roles roles = (Roles) context.lookup(LookupNames.SYSTEM_ROLES);
        if (roles == null) return false;
        if (GroupData.USER.equals(key)) {
            return roles.userGroupId == groupID;
        } else if (GroupData.SYSTEM.equals(key)) {
            return roles.systemGroupId == groupID;
        } else if (GroupData.GUEST.equals(key)) {
            return roles.guestGroupId == groupID;
        }
        throw new IllegalArgumentException("Key not valid.");
    }

    /**
     * Implemented as specified by {@link AdminService}.
     * @see AdminService#isSystemUser(long)
     */
    public boolean isSystemUser(long userID)
    {
        Roles roles = (Roles) context.lookup(LookupNames.SYSTEM_ROLES);
        if (roles == null) return false;
        return (roles.rootId == userID || roles.guestId == userID);
    }

    /**
     * Implemented as specified by {@link AdminService}.
     * @see AdminService#isSystemUser(long, String)
     */
    public boolean isSystemUser(long userID, String key)
    {
        Roles roles = (Roles) context.lookup(LookupNames.SYSTEM_ROLES);
        if (roles == null) return false;
        if (GroupData.SYSTEM.equals(key)) {
            return roles.rootId == userID;
        } else if (GroupData.GUEST.equals(key)) {
            return roles.guestId == userID;
        }
        throw new IllegalArgumentException("Key not valid.");
    }

    /**
     * Implemented as specified by {@link AdminService}.
     * @see AdminService#lookupLdapAuthExperimenter(SecurityContext, long)
     */
    public String lookupLdapAuthExperimenter(SecurityContext ctx, long userID)
            throws DSOutOfServiceException, DSAccessException
    {
        return gateway.lookupLdapAuthExperimenter(ctx, userID);
    }
}
