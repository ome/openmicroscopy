/*
* ome.admin.logic
*
*   Copyright 2007 University of Dundee. All rights reserved.
*   Use is subject to license terms supplied in LICENSE.txt
*/

package ome.admin.logic;

// Java imports
import ome.connection.ConnectionDB;
import ome.model.meta.ExperimenterGroup;

import org.apache.commons.beanutils.BeanUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// Third-party libraries

// Application-internal dependencies

/**
 * Delegate of group mangement.
 * @author Aleksandra Tarkowska &nbsp;&nbsp;&nbsp;&nbsp; <a href="mailto:A.Tarkowska@dundee.ac.uk">A.Tarkowska@dundee.ac.uk</a>
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
     * {@link ome.connection.ConnectionDB}
     */
	ConnectionDB db = new ConnectionDB();
	{
		getGroups();
	}

    /**
     * Gets {@link ome.model.meta.ExperimenterGroup} by {@link ome.model.meta.ExperimenterGroup#getId()}.
     * @param id {@link ome.model.meta.ExperimenterGroup#getId()}.
     * @return {@link ome.model.meta.ExperimenterGroup}.
     */
	public ExperimenterGroup getGroupById(Long id) {
		return (ExperimenterGroup) db.getGroup(id);
	}

    /**
     * Gets {@link java.util.List} of {@link ome.model.meta.ExperimenterGroup}.
     * @return {@link java.util.List}<{@link ome.model.meta.ExperimenterGroup}>.
     */
	public List<ExperimenterGroup> getGroups() {
		this.groups = db.lookupGroups();
		return this.groups;
	}

    /**
     * Adds new {@link ome.model.meta.ExperimenterGroup}.
     * @param group {@link ome.model.meta.ExperimenterGroup}.
     */
	public void addGroup(ExperimenterGroup group) {
		db.createGroup(group);
	}

    /**
     * {@link java.util.List}<{@link ome.model.meta.ExperimenterGroup}>.
     * @param sortItem {@link java.lang.String}.
     * @param sort {@link java.lang.String}.
     * @return {@link java.util.List}<{@link ome.model.meta.ExperimenterGroup}>.
     */
	public List<ExperimenterGroup> sortItems(String sortItem, String sort) {
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
     * @param group {@link ome.model.meta.ExperimenterGroup}.
     */
	public void updateGroup(ExperimenterGroup group) {
		db.updateGroup(group);
	}

    /**
     * Delete {@link ome.model.meta.ExperimenterGroup}.
     * @param id {@link ome.model.meta.ExperimenterGroup#getId()}.
     */
	public void deleteGroup(Long id) {
		db.deleteGroup(id);
	}

    /**
     * Sort {@link ome.model.meta.ExperimenterGroup} by {@link java.util.Comparator}
     * @param comparator {@link java.util.Comparator}.
     */
	private void sort(Comparator comparator) {
		Collections.sort(groups, comparator);
	}

}
