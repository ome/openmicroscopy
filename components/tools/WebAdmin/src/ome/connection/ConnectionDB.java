/*
* ome.connection
*
*   Copyright 2007 University of Dundee. All rights reserved.
*   Use is subject to license terms supplied in LICENSE.txt
*/

package ome.connection;

// Java imports
import java.util.ArrayList;
import java.util.List;

import ome.api.IAdmin;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.system.Login;
import ome.system.Server;
import ome.system.ServiceFactory;
import ome.api.IQuery;
import ome.system.EventContext;

import javax.faces.context.FacesContext;
import ome.admin.controller.LoginBean;

import org.apache.log4j.Logger;

// Third-party libraries

// Application-internal dependencies

/**
 * ConnectionDB providing access to user/admin-only functionality based server access by {@link ome.system.ServiceFactory} and selected user functions. Most methods require membership in privileged.
 * @author Aleksandra Tarkowska &nbsp;&nbsp;&nbsp;&nbsp; <a href="mailto:A.Tarkowska@dundee.ac.uk">A.Tarkowska@dundee.ac.uk</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Revision$Date: $)</small>
 * @since OME3.0
 */
public class ConnectionDB {

    /**
     * log4j logger
     */
	static Logger logger = Logger.getLogger(ConnectionDB.class.getName());

    /**
     * IAdmin
     */
	private IAdmin adminService;

    /**
     * IAdmin
     */
	private IQuery queryService;

    /**
     * Current {@link ome.model.meta.Experimenter#getId()} as {@link java.lang.String}
     */
	private String userid;

	/**
     * Creates a new instance of ConnectionDB for {@link ome.admin.controller.LoginBean}.
     * @param username {@link ome.model.meta.Experimenter#getOmeName()}. Not null.
     * @param password Not-null. Might must pass validation in the security sub-system.
     * @param server Not null.
     * @param port Not null.
     */
	public ConnectionDB(String username, String password, String server,
			int port) {
		logger.info("Login - Service Factory connection to " + server + ":"
				+ port + " by " + username + " ...");
		try {

			Login l = new Login(username, password, "system", "User");
			Server s = new Server(server, port);
			ServiceFactory sf = new ServiceFactory(s, l);
			adminService = sf.getAdminService();
			queryService = sf.getQueryService();
			logger.info("Admin role for user "
					+ adminService.getEventContext().getCurrentUserId());

		} catch (Exception e) {

			Login l = new Login(username, password, "user", "User");
			Server s = new Server(server, port);
			ServiceFactory sf = new ServiceFactory(s, l);
			adminService = sf.getAdminService();
			queryService = sf.getQueryService();
			logger.info("User role for user "
					+ adminService.getEventContext().getCurrentUserId());

		}
	}

    /**
     * Creates a new instance of ConnectionDB.
     */
	public ConnectionDB() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		LoginBean lb = (LoginBean) facesContext.getApplication()
				.getVariableResolver().resolveVariable(facesContext,
						"LoginBean");
		String username = lb.getUsername();
		String password = lb.getPassword();
		String server = lb.getServer();
		int port = lb.getPort();

		this.userid = lb.getId();

		logger.info("Service Factory connection to " + server + ":" + port
				+ " by " + username + " ...");
		String role = "user";
		if (lb.getRole())
			role = "system";

		try {
			Login l = new Login(username, password, role, "User");
			Server s = new Server(server, port);
			ServiceFactory sf = new ServiceFactory(s, l);
			adminService = sf.getAdminService();
			queryService = sf.getQueryService();
		} catch (Exception e) {
			e.printStackTrace();

		}
	}

    /**
     * Changs the password for current {@link ome.model.meta.Experimenter}.
     * @param password Not-null. Might must pass validation in the security sub-system.
     */
	public void changeMyPassword(String password) {
		logger.info("changeMyPassword by user ID: " + userid);
		adminService.changePassword(password);
	}

    /**
     * Gets current {@link ome.system.EventContext}.
     * @return {@link ome.system.EventContext}.
     */
	public EventContext getCurrentEventContext() {
		return adminService.getEventContext();
	}

    /**
     * Changs the password for {@link ome.model.meta.Experimenter}.
     * @param username The {@link ome.model.meta.Experimenter#getOmeName()} . Not null.
     * @param password Not-null. Might must pass validation in the security sub-system.
     */
	public void changePassword(String username, String password) {
		logger.info("changePassword by user ID: " + userid);
		adminService.changeUserPassword(username, password);
	}

    /**
     * Gets {@link java.util.List} of {@link ome.model.meta.ExperimenterGroup} which was add for select default group list.
     * @return {@link java.util.List}<{@link ome.model.meta.ExperimenterGroup}>.
     */
	public List<ExperimenterGroup> lookupGroupsAdd() {
		return filterAdd(adminService.lookupGroups());
	}

    /**
     * Gets {@link java.util.List} of {@link ome.model.meta.ExperimenterGroup} for select others group list.
     * @return {@link java.util.List}<{@link ome.model.meta.ExperimenterGroup}>.
     */
	public List<ExperimenterGroup> lookupGroups() {
		return filter(adminService.lookupGroups());
	}

    /**
     * Gets {@link java.util.List} of {@link ome.model.meta.Experimenter}.
     * @return {@link java.util.List}<{@link ome.model.meta.Experimenter}>.
     */
	public List<Experimenter> lookupExperimenters() {
		return adminService.lookupExperimenters();
	}

    /**
     * Gets {@link ome.model.meta.Experimenter} details.
     * @param omename {@link ome.model.meta.Experimenter#getOmeName()}.
     * @return {@link ome.model.meta.Experimenter}.
     */
	public Experimenter lookupExperimenter(String omename) {
		return adminService.lookupExperimenter(omename);
	}

    /**
     * Gets {@link ome.model.meta.ExperimenterGroup} details by {@link ome.model.meta.ExperimenterGroup#getId()}.
     * @param id {@link ome.model.meta.ExperimenterGroup#getId()}.
     * @return {@link ome.model.meta.ExperimenterGroup}.
     */
	public ExperimenterGroup getGroup(Long id) {
		ExperimenterGroup exg = new ExperimenterGroup();
		exg = adminService.getGroup(id);
		return exg;
	}

    /**
     * Gets {@link ome.model.meta.ExperimenterGroup} details by {@link ome.model.meta.ExperimenterGroup#getName()}.
     * @param name {@link ome.model.meta.ExperimenterGroup#getName()}.
     * @return {@link ome.model.meta.ExperimenterGroup}.
     */
	public ExperimenterGroup getGroup(String name) {
		ExperimenterGroup exg = new ExperimenterGroup();
		exg = queryService.findByString(ExperimenterGroup.class, "name", name);
		return exg;
	}

    /**
     * Updates {@link ome.model.meta.ExperimenterGroup}.
     * @param experimenterGroup {@link ome.model.meta.ExperimenterGroup}
     */
	public void updateGroup(ExperimenterGroup experimenterGroup) {
		logger.info("updateGroup by user ID: " + userid);
		try {
			adminService.updateGroup(experimenterGroup);
		} catch (Exception e) {
			logger.info(e.getMessage());
		}
	}

    /**
     * Deletes {@link ome.model.meta.ExperimenterGroup}.
     * @param id {@link ome.model.meta.ExperimenterGroup#getId()}
     */
	public void deleteGroup(Long id) {
		logger.info("deleteGroup by user ID: " + userid);
		System.out.println("no method in adminService");
	}

    /**
     * Deletes {@link ome.model.meta.Experimenter}
     * @param id {@link ome.model.meta.Experimenter#getId()}
     */
	public void deleteExperimenter(Long id) {
		logger.info("deleteExperimenter by user ID: " + userid);
		try {
			adminService.deleteExperimenter(adminService.getExperimenter(id));
		} catch (Exception e) {
			logger.info(e.getMessage());
		}
	}

    /**
     * Updates {@link ome.model.meta.Experimenter}
     * @param experimenter {@link ome.model.meta.Experimenter}. Not null.
     */
	public void updateExperimenter(Experimenter experimenter) {
		logger.info("updateExperimenter by user ID: " + userid);
		try {
			adminService.updateExperimenter(experimenter);
		} catch (Exception e) {
			logger.info(e.getMessage());
		}

	}

    /**
     * Gets {@link ome.model.meta.Experimenter} details by {@link ome.model.meta.Experimenter#getId()}
     * @param id {@link ome.model.meta.Experimenter#getId()}. Not null.
     * @return {@link ome.model.meta.Experimenter}
     */
	public Experimenter getExperimenter(Long id) {
		return adminService.getExperimenter(id);
	}

    /**
     * Creates {@link ome.model.meta.ExperimenterGroup}
     * @param group {@link ome.model.meta.ExperimenterGroup}
     * @return {@link ome.model.meta.ExperimenterGroup#getId()}
     */
	public Long createGroup(ExperimenterGroup group) {
		logger.info("createGroup by user ID: " + userid);
		Long id = 0L;
		try {
			id = adminService.createGroup(group);
		} catch (Exception e) {
			logger.info(e.getMessage());
		}
		return id;
	}

    /**
     * Creates {@link ome.model.meta.Experimenter}
     * @param experimenter {@link ome.model.meta.Experimenter}. Not null.
     * @param defaultGroup {@link ome.model.meta.ExperimenterGroup}. Not null.
     * @param groups {@link ome.model.meta.ExperimenterGroup} []
     * @return {@link ome.model.meta.Experimenter#getId()}
     */
	public Long createExperimenter(Experimenter experimenter,
			ExperimenterGroup defaultGroup, ExperimenterGroup... groups) {
		logger.info("createExperimenter by user ID: " + userid);
		Long id = 0L;
		try {
			id = adminService.createExperimenter(experimenter, defaultGroup,
					groups);
		} catch (Exception e) {
			logger.info(e.getMessage());
		}
		return id;
	}

    /**
     * Checks existing {@link ome.model.meta.Experimenter#getOmeName()} on the database.
     * @param omeName {@link ome.model.meta.Experimenter#getOmeName()}
     * @return boolean
     */
	public boolean checkExperimenter(String omeName) {
		if (queryService.findByString(Experimenter.class, "omeName", omeName) != null)
			return true;
		return false;
	}

    /**
     * Checks existing {@link ome.model.meta.Experimenter#getEmail()} in the database.
     * @param email {@link ome.model.meta.Experimenter#getEmail()}
     * @return boolean
     */
	public boolean checkEmail(String email) {
		if (queryService.findByString(Experimenter.class, "email", email) != null)
			return true;
		return false;
	}

    /**
     * Checks System permition for{@link ome.model.meta.Experimenter#getId()}.
     * @param experimenterId {@link ome.model.meta.Experimenter#getId()}
     * @return boolean
     */
	public boolean isAdmin(Long experimenterId) {
		boolean role = false;
		ExperimenterGroup[] exg = adminService.containedGroups(experimenterId);
		for (int i = 0; i < exg.length; i++) {
			if (exg[i].getName().equals("system"))
				role = true;
		}
		return role;
	}

    /**
     * Checks User perimition for {@link ome.model.meta.Experimenter#getId()}.
     * @param experimenterId {@link ome.model.meta.Experimenter#getId()}
     * @return boolean
     */
	public boolean isUser(Long experimenterId) {
		boolean role = false;
		ExperimenterGroup[] exg = adminService.containedGroups(experimenterId);
		for (int i = 0; i < exg.length; i++) {
			if (exg[i].getName().equals("user"))
				role = true;
		}
		return role;
	}

    /**
     * Gets {@link ome.model.meta.ExperimenterGroup} [] for all of the {@link ome.model.meta.Experimenter#getId()} without "system", default" and "user" groups.
     * @param experimenterId {@link ome.model.meta.Experimenter#getId()}
     * @return {@link ome.model.meta.ExperimenterGroup} []
     */
	public ExperimenterGroup[] containedGroups(Long experimenterId) {
		ExperimenterGroup[] exg = adminService.containedGroups(experimenterId);
		return filter(exg);
	}

    /**
     * Gets {@link ome.model.meta.ExperimenterGroup} [] for all of the {@link ome.model.meta.Experimenter#getId()} without "system" and "user" groups.
     * @param experimenterId {@link ome.model.meta.Experimenter#getId()}
     * @return {@link ome.model.meta.Experimenter} []
     */
	public ExperimenterGroup[] containedMyGroups(Long experimenterId) {
		ExperimenterGroup[] exg = adminService.containedGroups(experimenterId);
		return filterMy(exg);
	}

    /**
     * Gets "default group" for {@link ome.model.meta.Experimenter#getId()}
     * @param experimenterId {@link ome.model.meta.Experimenter#getId()}
     * @return {@link ome.model.meta.ExperimenterGroup}
     */
	public ExperimenterGroup getDefaultGroup(Long experimenterId) {
		ExperimenterGroup exg = new ExperimenterGroup();
		try {
			exg = adminService.getDefaultGroup(experimenterId);
		} catch (Exception e) {
			logger.info(e.getMessage());
		}
		return exg;
	}

    /**
     * Set "default group" for {@link ome.model.meta.Experimenter}
     * @param experimenter {@link ome.model.meta.Experimenter}
     * @param defaultGroup {@link ome.model.meta.ExperimenterGroup}
     */
	public void setDefaultGroup(Experimenter experimenter, ExperimenterGroup defaultGroup) {
		logger.info("setDefaultGroup by user ID: " + userid);
		try {
			adminService.setDefaultGroup(experimenter, defaultGroup);
		} catch (Exception e) {
			logger.info(e.getMessage());
		}
		logger.info("default defaultGroup " + defaultGroup.getName() + "[id:" + defaultGroup.getId()
				+ "] for user: " + experimenter.getOmeName() + "[id:" + experimenter.getId()
				+ "] was set");
	}

    /**
     * Set "other groups" for {@link ome.model.meta.Experimenter}
     * @param experimenter {@link ome.model.meta.Experimenter}
     * @param groups {@link ome.model.meta.ExperimenterGroup} []
     * @param defaultGroup {@link ome.model.meta.ExperimenterGroup}
     * @param userRole boolean
     * @param adminRole boolean
     */
	public void setOtherGroups(Experimenter experimenter, ExperimenterGroup[] groups,
			ExperimenterGroup defaultGroup, boolean userRole, boolean adminRole) {
		logger.info("setOtherGroups by user ID: " + userid);
		try {
			ExperimenterGroup[] old = adminService
					.containedGroups(experimenter.getId());
			adminService.addGroups(experimenter, filterAd(old, groups, defaultGroup));

			adminService.setDefaultGroup(experimenter, defaultGroup);
			adminService.removeGroups(experimenter, filterRm(old, groups, defaultGroup));

			if (!isAdmin(experimenter.getId()) == adminRole) {
				ExperimenterGroup adminGroup = getGroup("system");
				if (adminRole) {
					adminService.addGroups(experimenter, adminGroup);
				} else {
					adminService.removeGroups(experimenter, adminGroup);
				}
			}

			if (!isUser(experimenter.getId()) == userRole) {
				ExperimenterGroup userGroup = getGroup("user");
				if (userRole) {
					adminService.addGroups(experimenter, userGroup);
				} else {
					adminService.removeGroups(experimenter, userGroup);
				}
			}
		} catch (Exception e) {
			logger.info(e.getMessage());
		}

	}

    /**
     * 
     * @param old 
     * @param newGroups 
     * @param defaultGroup 
     * @return 
     */
	private ExperimenterGroup[] filterAd(ExperimenterGroup[] old,
			ExperimenterGroup[] newGroups, ExperimenterGroup defaultGroup) {
		List<ExperimenterGroup> fNewGroups = new ArrayList<ExperimenterGroup>();
		for (int i = 0; i < newGroups.length; i++) {
			if (!newGroups[i].getName().equals("user")
					&& !newGroups[i].getName().equals("system")) {
				fNewGroups.add(newGroups[i]);

			}
		}

		List<ExperimenterGroup> fOld = new ArrayList<ExperimenterGroup>();
		for (int i = 0; i < old.length; i++) {
			if (!old[i].getName().equals("user")
					&& !old[i].getName().equals("system")) {
				fOld.add(old[i]);

			}
		}

		for (int i = 0; i < fNewGroups.size(); i++) {
			int flag = 0;
			for (int j = 0; j < fOld.size(); j++) {
				if (fNewGroups.get(i).getId().equals(fOld.get(j).getId())) {
					flag++;
				}
			}
			if (flag > 0) {
				fNewGroups.remove(i);
				i--;
			} else {
				logger.info("group to add " + fNewGroups.get(i).getName()
						+ "[id:" + fNewGroups.get(i).getId() + "]");

			}
		}
		return fNewGroups.toArray(new ExperimenterGroup[fNewGroups.size()]);
	}

    /**
     * 
     * @param old 
     * @param newGroups 
     * @param defaultGroup 
     * @return 
     */
	private ExperimenterGroup[] filterRm(ExperimenterGroup[] old,
			ExperimenterGroup[] newGroups, ExperimenterGroup defaultGroup) {
		List<ExperimenterGroup> fOldGroups = new ArrayList<ExperimenterGroup>();
		for (int i = 0; i < old.length; i++) {
			if (!old[i].getName().equals("default")
					&& !old[i].getName().equals("user")
					&& !old[i].getName().equals("system")) {
				fOldGroups.add(old[i]);

			}
		}

		List<ExperimenterGroup> fNew = new ArrayList<ExperimenterGroup>();
		for (int i = 0; i < newGroups.length; i++) {
			if (!newGroups[i].getName().equals("default")
					&& !newGroups[i].getName().equals("user")
					&& !newGroups[i].getName().equals("system")) {
				fNew.add(newGroups[i]);

			}
		}

		for (int i = 0; i < fOldGroups.size(); i++) {
			int flag = 0;
			for (int j = 0; j < fNew.size(); j++) {
				if (fOldGroups.get(i).getId().equals(fNew.get(j).getId())) {
					flag++;
				}
			}
			if (flag > 0) {
				fOldGroups.remove(i);
				i--;
			} else {
				logger.info("group to remove " + fOldGroups.get(i).getName()
						+ "[id:" + fOldGroups.get(i).getId() + "]");

			}

		}

		return fOldGroups.toArray(new ExperimenterGroup[fOldGroups.size()]);
	}

    /**
     * 
     * @param groups 
     * @return 
     */
	private ExperimenterGroup[] filter(ExperimenterGroup[] groups) {
		List<ExperimenterGroup> filteredGroups = new ArrayList<ExperimenterGroup>();
		for (int i = 0; i < groups.length; i++) {
			if (!groups[i].getName().equals("default")
					&& !groups[i].getName().equals("user")
					&& !groups[i].getName().equals("system")) {
				filteredGroups.add(groups[i]);
			}
		}
		return filteredGroups.toArray(new ExperimenterGroup[filteredGroups
				.size()]);
	}

    /**
     * 
     * @param groups 
     * @return 
     */
	private ExperimenterGroup[] filterMy(ExperimenterGroup[] groups) {
		List<ExperimenterGroup> filteredGroups = new ArrayList<ExperimenterGroup>();
		for (int i = 0; i < groups.length; i++) {
			if (!groups[i].getName().equals("user")
					&& !groups[i].getName().equals("system")) {
				filteredGroups.add(groups[i]);
			}
		}
		return filteredGroups.toArray(new ExperimenterGroup[filteredGroups
				.size()]);
	}

    /**
     * 
     * @param groups 
     * @return 
     */
	private List<ExperimenterGroup> filter(List<ExperimenterGroup> groups) {
		List<ExperimenterGroup> filteredGroups = new ArrayList<ExperimenterGroup>();
		for (int i = 0; i < groups.size(); i++) {
			if (!groups.get(i).getName().equals("default")
					&& !groups.get(i).getName().equals("user")
					&& !groups.get(i).getName().equals("system")) {
				filteredGroups.add(groups.get(i));
			}
		}
		return filteredGroups;
	}

    /**
     * 
     * @param groups 
     * @return 
     */
	private List<ExperimenterGroup> filterAdd(List<ExperimenterGroup> groups) {
		List<ExperimenterGroup> filteredGroups = new ArrayList<ExperimenterGroup>();
		for (int i = 0; i < groups.size(); i++) {
			if (!groups.get(i).getName().equals("user")
					&& !groups.get(i).getName().equals("system")) {
				filteredGroups.add(groups.get(i));
			}
		}
		return filteredGroups;
	}

}
