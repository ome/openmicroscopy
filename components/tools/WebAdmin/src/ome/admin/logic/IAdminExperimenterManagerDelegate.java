/*
* ome.admin.logic
*
*   Copyright 2007 University of Dundee. All rights reserved.
*   Use is subject to license terms supplied in LICENSE.txt
*/

package ome.admin.logic;

// Java imports
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// Third-party libraries
import org.apache.commons.beanutils.BeanUtils;

// Application-internal dependencies
import ome.connection.ConnectionDB;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;

/**
 * Delegate of experimenter mangement.
 * @author Aleksandra Tarkowska &nbsp;&nbsp;&nbsp;&nbsp; <a href="mailto:A.Tarkowska@dundee.ac.uk">A.Tarkowska@dundee.ac.uk</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Revision$Date: $)</small>
 * @since OME3.0
 */
public class IAdminExperimenterManagerDelegate implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    /**
     * {@link java.util.List}<{@link ome.model.meta.Experimenter}>.
     */
	private List<Experimenter> experimenters = new ArrayList<Experimenter>();

    /**
     * {@link java.lang.String}
     */
	private String sortByProperty = "firstName";

    /**
     * {@link java.util.Comparator}
     */
	private transient final Comparator propertyAscendingComparator = new Comparator() {
		public int compare(Object object1, Object object2) {
			try {
				String property1 = BeanUtils.getProperty(object1,
						IAdminExperimenterManagerDelegate.this.sortByProperty);
				String property2 = BeanUtils.getProperty(object2,
						IAdminExperimenterManagerDelegate.this.sortByProperty);

				return property1.toLowerCase().compareTo(
						property2.toLowerCase());
			} catch (Exception e) {
				return 0;
			}
		}
	};

    /**
     * {@link java.util.Comparator}
     */
	private transient final Comparator propertyDescendingComparator = new Comparator() {
		public int compare(Object object1, Object object2) {
			try {
				String property1 = BeanUtils.getProperty(object1,
						IAdminExperimenterManagerDelegate.this.sortByProperty);
				String property2 = BeanUtils.getProperty(object2,
						IAdminExperimenterManagerDelegate.this.sortByProperty);

				return property2.toLowerCase().compareTo(
						property1.toLowerCase());
			} catch (Exception e) {
				return 0;
			}
		}
	};
    /**
     * {@link ome.connection.ConnectionDB}
     */
	ConnectionDB db = new ConnectionDB();
	{
		getExperimenters();
	}

    /**
     * Gets {@link ome.model.meta.ExperimenterGroup} [] for all of the {@link ome.model.meta.Experimenter#getId()} without "system", default" and "user" groups.
     * @param experimenterId {@link ome.model.meta.Experimenter#getId()}
     * @return {@link ome.model.meta.ExperimenterGroup} []
     */
	public ExperimenterGroup[] containedGroups(Long experimenterId) {
		ExperimenterGroup[] exg = db.containedGroups(experimenterId);
		return exg;
	}

    /**
     * Gets {@link ome.model.meta.ExperimenterGroup} [] for all of the {@link ome.model.meta.Experimenter#getId()} without "system" and "user" groups.
     * @param experimenterId {@link ome.model.meta.Experimenter#getId()}
     * @return {@link ome.model.meta.Experimenter} []
     */
	public ExperimenterGroup[] containedMyGroups(Long experimenterId) {
		ExperimenterGroup[] exg = db.containedMyGroups(experimenterId);
		return exg;
	}

    /**
     * Gets "default group" for {@link ome.model.meta.Experimenter#getId()}
     * @param experimenterId {@link ome.model.meta.Experimenter#getId()}
     * @return {@link ome.model.meta.ExperimenterGroup}
     */
	public ExperimenterGroup getDefaultGroup(Long experimenterId) {
		ExperimenterGroup exg = db.getDefaultGroup(experimenterId);
		return exg;
	}

    /**
     * Changs the password for current {@link ome.model.meta.Experimenter}.
     * @param password Not-null. Might must pass validation in the security sub-system.
     */
	public void changeMyPassword(String password) {
		db.changeMyPassword(password);
	}

    /**
     * Changs the password for {@link ome.model.meta.Experimenter}.
     * @param username The {@link ome.model.meta.Experimenter#getOmeName()} . Not null.
     * @param password Not-null. Might must pass validation in the security sub-system.
     */
	public void changePassword(String username, String password) {
		db.changePassword(username, password);
	}

    /**
     * Checks System permition for{@link ome.model.meta.Experimenter#getId()}.
     * @param experimenterId {@link ome.model.meta.Experimenter#getId()}
     * @return boolean
     */
	public boolean isAdmin(Long experimenterId) {
		return db.isAdmin(experimenterId);
	}

    /**
     * Checks User perimition for {@link ome.model.meta.Experimenter#getId()}.
     * @param experimenterId {@link ome.model.meta.Experimenter#getId()}
     * @return boolean
     */
	public boolean isUser(Long experimenterId) {
		return db.isUser(experimenterId);
	}

    /**
     * Gets {@link java.util.List} of {@link ome.model.meta.ExperimenterGroup} for select others group list.
     * @return {@link java.util.List}<{@link ome.model.meta.ExperimenterGroup}>.
     */
	public List<ExperimenterGroup> getGroups() {
		List<ExperimenterGroup> exg = db.lookupGroups();
		return exg;
	}

    /**
     * Gets {@link java.util.List} of {@link ome.model.meta.ExperimenterGroup} which was add for select default group list.
     * @return {@link java.util.List}<{@link ome.model.meta.ExperimenterGroup}>.
     */
	public List<ExperimenterGroup> getGroupsAdd() {
		List<ExperimenterGroup> exg = db.lookupGroupsAdd();
		return exg;
	}

    /**
     * Gets {@link ome.model.meta.Experimenter} details by {@link ome.model.meta.Experimenter#getId()}
     * @param id {@link ome.model.meta.Experimenter#getId()}. Not null.
     * @return {@link ome.model.meta.Experimenter}
     */
	public Experimenter getExperimenterById(Long id) {
		return (Experimenter) db.getExperimenter(id);
	}

    /**
     * Gets {@link java.util.List} of {@link ome.model.meta.Experimenter}.
     * @return {@link java.util.List}<{@link ome.model.meta.Experimenter}>.
     */
	public List<Experimenter> getExperimenters() {
		this.experimenters = db.lookupExperimenters();
		return this.experimenters;
	}

    /**
     * Creates {@link ome.model.meta.Experimenter}
     * @param experimenter {@link ome.model.meta.Experimenter}. Not null.
     * @param group {@link ome.model.meta.ExperimenterGroup}. Not null.
     * @param otherGroups {@link ome.model.meta.ExperimenterGroup} [].
     * @param userRole boolean
     * @param adminRole boolean
     */
	public void createExperimenter(Experimenter experimenter, Long group,
			List otherGroups, boolean userRole, boolean adminRole) {
		ExperimenterGroup defaultGroup = new ExperimenterGroup(group);

		int c = 0;
		if (userRole)
			c++;
		if (adminRole)
			c++;

		ExperimenterGroup[] others = new ExperimenterGroup[(otherGroups.size())
				+ c];
		for (int i = 0; i < (otherGroups.size()); i++) {
			others[i] = db.getGroup(Long.parseLong(otherGroups.get(i)
					.toString()));
		}

		if (userRole) {
			others[otherGroups.size()] = db.getGroup("user");
			if (adminRole) {
				others[otherGroups.size() + 1] = db.getGroup("system");
			}
		} else if (adminRole)
			others[otherGroups.size()] = db.getGroup("system");

		db.createExperimenter(experimenter, defaultGroup, others);
		// if(adminRole) db.setDefaultGroup(experimenter,db.getGroup("system"));
	}

    /**
     * Checks existing {@link ome.model.meta.Experimenter#getOmeName()} on the database.
     * @param omeName {@link ome.model.meta.ExperimenterGroup#getOmeName()}
     * @return boolean
     */
	public boolean checkExperimenter(String omeName) {
		return db.checkExperimenter(omeName);
	}

    /**
     * Checks existing {@link ome.model.meta.Experimenter#getEmail()} in the database.
     * @param email {@link ome.model.meta.Experimenter#getEmail()}
     * @return boolean
     */
	public boolean checkEmail(String email) {
		return db.checkEmail(email);
	}

    /**
     * Sort {@link java.util.List} items
     * @param sortItem {@link java.lang.String}
     * @param sort {@link java.lang.String}
     * @return {@link java.util.List}<{@link ome.model.meta.Experimenter}>.
     */
	public List<Experimenter> sortItems(String sortItem, String sort) {
		this.experimenters = getExperimenters();
		sortByProperty = sortItem;
		if (sort.equals("asc"))
			sort(propertyAscendingComparator);
		else if (sort.equals("dsc"))
			sort(propertyDescendingComparator);
		return experimenters;
	}


    /**
     * Updates {@link ome.model.meta.Experimenter}
     * @param experimenter {@link ome.model.meta.Experimenter}. Not null.
     * @param dgroup {@link ome.model.meta.ExperimenterGroup#getId()}
     * @param otherGroups {@link java.util.List}
     * @param userRole boolean
     * @param adminRole boolean
     */
	public void updateExperimenter(Experimenter experimenter, Long dgroup,
			List otherGroups, boolean userRole, boolean adminRole) {
		db.updateExperimenter(experimenter);
		ExperimenterGroup[] others = new ExperimenterGroup[otherGroups.size()];
		for (int i = 0; i < otherGroups.size(); i++) {
			others[i] = db.getGroup(Long.parseLong(otherGroups.get(i)
					.toString()));
		}

		ExperimenterGroup defaultGroup = db.getGroup(dgroup);
		db.setOtherGroups(experimenter, others, defaultGroup, userRole,
				adminRole);

	}

    /**
     * Update current {@link ome.model.meta.Experimenter} details.
     * @param experimenter {@link ome.model.meta.Experimenter}
     * @param group {@link ome.model.meta.ExperimenterGroup#getId()}
     */
	public void updateMyAccount(Experimenter experimenter, Long group) {
		db.updateExperimenter(experimenter);
		ExperimenterGroup defaultGroup = db.getGroup(group);
		db.setDefaultGroup(experimenter, defaultGroup);

	}

    /**
     * Delete {@link ome.model.meta.Experimenter}
     * @param id {@link ome.model.meta.Experimenter#getId()}
     */
	public void deleteExperimenter(Long id) {
		db.deleteExperimenter(id);
	}

    /**
     * Sort {@link ome.model.meta.Experimenter} by {@link java.util.Comparator}
     * @param comparator {@link java.util.Comparator}
     */
	private void sort(Comparator comparator) {
		Collections.sort(experimenters, comparator);
	}

}
