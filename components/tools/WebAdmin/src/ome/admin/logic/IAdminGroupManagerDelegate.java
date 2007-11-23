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

import javax.faces.context.FacesContext;

// Third-party libraries
import org.apache.commons.beanutils.BeanUtils;

// Application-internal dependencies
import ome.admin.data.ConnectionDB;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;

/**
 * Delegate of group mangement.
 * 
 * @author Aleksandra Tarkowska &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:A.Tarkowska@dundee.ac.uk">A.Tarkowska@dundee.ac.uk</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Revision$Date: $)</small>
 * @since OME3.0
 */
public class IAdminGroupManagerDelegate implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * {@link java.util.List} of {@link ome.model.meta.ExperimenterGroup}
	 */
	private List<ExperimenterGroup> groups = new ArrayList<ExperimenterGroup>();

	/**
	 * {@link java.lang.String} set by "name";
	 */
	private String sortByProperty = "name";

	private final static int scrollerSize = Integer.parseInt(FacesContext
			.getCurrentInstance().getExternalContext().getInitParameter(
					"scrollerSize"));

	/**
	 * {@link java.util.Comparator}
	 */
	private transient final Comparator propertyAscendingComparator = new Comparator() {
		public int compare(Object object1, Object object2) {
			try {
				String property1 = BeanUtils.getProperty(object1,
						IAdminGroupManagerDelegate.this.sortByProperty);
				String property2 = BeanUtils.getProperty(object2,
						IAdminGroupManagerDelegate.this.sortByProperty);

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
						IAdminGroupManagerDelegate.this.sortByProperty);
				String property2 = BeanUtils.getProperty(object2,
						IAdminGroupManagerDelegate.this.sortByProperty);

				return property2.toLowerCase().compareTo(
						property1.toLowerCase());
			} catch (Exception e) {
				return 0;
			}
		}
	};

	/**
	 * {@link ome.admin.data.ConnectionDB}
	 */
	private ConnectionDB db = new ConnectionDB();

	/**
	 * Creates a new instance of IAdminGroupManagerDelegate.
	 */
	public IAdminGroupManagerDelegate() {
		getGroups();
	}

	/**
	 * Allowes scroller to appear.
	 * @return boolean
	 */
	public boolean setScroller() {

		if (this.groups.size() > scrollerSize)
			return true;
		else
			return false;
	}

	/**
	 * Gets {@link ome.model.meta.Experimenter} details by
	 * {@link ome.model.meta.Experimenter#getId()}
	 * 
	 * @param id
	 *            {@link ome.model.meta.Experimenter#getId()}. Not null.
	 * @return {@link ome.model.meta.Experimenter}
	 */
	public Experimenter getExperimenter(Long id) {
		Experimenter experimenter = db.getExperimenter(id);
		return experimenter;
	}

	/**
	 * Gets {@link java.util.List} of {@link ome.model.meta.Experimenter}.
	 * 
	 * @return {@link java.util.List}<{@link ome.model.meta.Experimenter}>.
	 */
	public List<Experimenter> lookupExperimeters(Long groupId) {
		List<Experimenter> exps = db.lookupExperimenters();
		for (int i = 0; i < exps.size(); i++)
			setStarIfIsDefault(exps.get(i), groupId);
		return exps;
	}

	/**
	 * Gets {@link java.util.List} for all of the
	 * {@link ome.model.meta.ExperimenterGroup#getId()} as String
	 * 
	 * @param groupId
	 *            {@link ome.model.meta.ExperimenterGroup#getId()}
	 * @return {@link java.util.List}
	 */
	public List<String> containedExperimenters(Long groupId) {
		Experimenter[] expc = db.containedExperimenters(groupId);
		List<String> exps = new ArrayList<String>();
		for (int i = 0; i < expc.length; i++)
			exps.add(expc[i].getId().toString());
		return exps;
	}

	/**
	 * Sets "*" (star) if {@link ome.model.meta.ExperimenterGroup#getId()} of
	 * default group of {@link ome.model.meta.Experimenter} is equal param.
	 * 
	 * @param groupId
	 *            {@link ome.model.meta.ExperimenterGroup#getId()}
	 */
	private void setStarIfIsDefault(Experimenter exp, Long groupId) {
		if (db.getDefaultGroup(exp.getId()).getId().equals(groupId))
			exp.setOmeName(exp.getOmeName() + "*");
	}

	public void updateExperimenters(List<String> exps, Long groupId) {
		List<Experimenter> addExps = new ArrayList<Experimenter>();
		List<Experimenter> rmExps = new ArrayList<Experimenter>();

		Experimenter[] expc = db.containedExperimenters(groupId);
		List<String> exp = new ArrayList<String>();
		for (int j = 0; j < expc.length; j++)
			exp.add(expc[j].getId().toString());

		for (int i = 0; i < exp.size(); i++) {
			if (!exps.contains(exp.get(i))) {
				ExperimenterGroup dgroup = db.getDefaultGroup(Long
						.parseLong(exp.get(i)));
				if (!dgroup.getId().equals(groupId))
					rmExps.add(db.getExperimenter(Long.parseLong(exp.get(i))));
			}
		}

		for (int i = 0; i < exps.size(); i++) {

			List<ExperimenterGroup> exGr = db.containedGroupsList(Long
					.parseLong(exps.get(i)));
			List<String> exGrL = new ArrayList<String>();
			for (int j = 0; j < exGr.size(); j++)
				exGrL.add(exGr.get(j).getId().toString());

			if (!exGrL.contains(groupId.toString()))
				addExps.add(db.getExperimenter(Long.parseLong(exps.get(i))));

		}

		ExperimenterGroup group = db.getGroup(groupId);
		db.setExperimenters(addExps, rmExps, group);
	}

	/**
	 * Gets {@link ome.model.meta.ExperimenterGroup} by
	 * {@link ome.model.meta.ExperimenterGroup#getId()}.
	 * 
	 * @param id
	 *            {@link ome.model.meta.ExperimenterGroup#getId()}.
	 * @return {@link ome.model.meta.ExperimenterGroup}.
	 */
	public ExperimenterGroup getGroupById(Long id) {
		return (ExperimenterGroup) db.getGroup(id);
	}

	/**
	 * Gets {@link java.util.List} of {@link ome.model.meta.ExperimenterGroup}.
	 * 
	 * @return {@link java.util.List}<{@link ome.model.meta.ExperimenterGroup}>.
	 */
	public List<ExperimenterGroup> getGroups() {
		this.groups = db.lookupGroups();
		return this.groups;
	}

	/**
	 * Adds new {@link ome.model.meta.ExperimenterGroup}.
	 * 
	 * @param group
	 *            {@link ome.model.meta.ExperimenterGroup}.
	 */
	public void addGroup(ExperimenterGroup group) {
		db.createGroup(group);
	}

	/**
	 * {@link java.util.List}<{@link ome.model.meta.ExperimenterGroup}>.
	 * 
	 * @param sortItem
	 *            {@link java.lang.String}.
	 * @param sort
	 *            {@link java.lang.String}.
	 * @return {@link java.util.List}<{@link ome.model.meta.ExperimenterGroup}>.
	 */
	public List<ExperimenterGroup> sortItems(String sortItem, String sort) {
		// this.groups = getGroups();
		sortByProperty = sortItem;
		if (sort.equals("asc"))
			sort(propertyAscendingComparator);
		else if (sort.equals("dsc"))
			sort(propertyDescendingComparator);
		return groups;
	}

	/**
	 * {@link java.util.List}<{@link ome.model.meta.ExperimenterGroup}>.
	 * 
	 * @param sortItem
	 *            {@link java.lang.String}.
	 * @param sort
	 *            {@link java.lang.String}.
	 * @return {@link java.util.List}<{@link ome.model.meta.ExperimenterGroup}>.
	 */
	public List<ExperimenterGroup> getAndSortItems(String sortItem, String sort) {
		this.groups = getGroups();
		sortByProperty = sortItem;
		if (sort.equals("asc"))
			sort(propertyAscendingComparator);
		else if (sort.equals("dsc"))
			sort(propertyDescendingComparator);
		return groups;
	}

	/**
	 * Update {@link ome.model.meta.ExperimenterGroup}.
	 * 
	 * @param group
	 *            {@link ome.model.meta.ExperimenterGroup}.
	 */
	public void updateGroup(ExperimenterGroup group) {
		db.updateGroup(group);
	}

	/**
	 * Delete {@link ome.model.meta.ExperimenterGroup}.
	 * 
	 * @param id
	 *            {@link ome.model.meta.ExperimenterGroup#getId()}.
	 */
	public void deleteGroup(Long id) {
		db.deleteGroup(id);
	}

	/**
	 * Sort {@link ome.model.meta.ExperimenterGroup} by
	 * {@link java.util.Comparator}
	 * 
	 * @param comparator
	 *            {@link java.util.Comparator}.
	 */
	private void sort(Comparator comparator) {
		Collections.sort(groups, comparator);
	}
	
	/**
	 * Checks existing {@link ome.model.meta.ExperimenterGroup#getName()} on the
	 * database.
	 * 
	 * @param name
	 *            {@link ome.model.meta.ExperimenterGroup#getName()}
	 * @return boolean
	 */
	public boolean checkExperimenterGroup(String name) {
		return db.checkExperimenterGroup(name);
	}

}
