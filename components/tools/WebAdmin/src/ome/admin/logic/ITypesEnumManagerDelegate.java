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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

// Third-party libraries
import org.apache.commons.beanutils.BeanUtils;

// Application-internal dependencies
import ome.admin.data.ConnectionDB;
import ome.admin.model.Enumeration;
import ome.model.IEnum;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;

/**
 * Delegate of enumeration mangement.
 * 
 * @author Aleksandra Tarkowska &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:A.Tarkowska@dundee.ac.uk">A.Tarkowska@dundee.ac.uk</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Revision$Date: $)</small>
 * @since OME3.0
 */
public class ITypesEnumManagerDelegate implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * {@link java.util.List} of {@link ome.admin.model.Enumeration}
	 */
	private List<Enumeration> enums = new ArrayList<Enumeration>();

	/**
	 * {@link java.lang.String} set by "className";
	 */
	private String sortByProperty = "className";

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
						ITypesEnumManagerDelegate.this.sortByProperty);
				String property2 = BeanUtils.getProperty(object2,
						ITypesEnumManagerDelegate.this.sortByProperty);

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
						ITypesEnumManagerDelegate.this.sortByProperty);
				String property2 = BeanUtils.getProperty(object2,
						ITypesEnumManagerDelegate.this.sortByProperty);

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
	 * Creates a new instance of ITypesEnumManagerDelegate.
	 */
	public ITypesEnumManagerDelegate() {
		getEnums();
	}

	/**
	 * Allowes scroller to appear.
	 * @return boolean
	 */
	public boolean setScroller() {

		if (this.enums.size() > scrollerSize)
			return true;
		else
			return false;
	}

	/**
	 * Gets {@link java.util.List} of {@link ome.admin.model.Enumeration}.
	 * 
	 * @return {@link java.util.List}<{@link ome.admin.model.Enumeration}>.
	 */
	public List<Enumeration> getEnums() {
		List<Enumeration> list = new ArrayList<Enumeration>();
		Map map = db.getEnumerations();
		for (Iterator iter = map.keySet().iterator(); iter.hasNext();) {
            Class klass = (Class) iter.next();
            List<IEnum> entries = (List<IEnum>) map.get(klass); 
            
            Enumeration en = new Enumeration();
			en.setClassName(klass.getName());
			en.setEnumList(entries);
			list.add(en);
        }
		this.enums = list;
		return this.enums;
	}

	/**
	 * {@link java.util.List}<{@link ome.admin.model.Enumeration}>.
	 * 
	 * @param sortItem
	 *            {@link java.lang.String}.
	 * @param sort
	 *            {@link java.lang.String}.
	 * @return {@link java.util.List}<{@link ome.admin.model.Enumeration}>.
	 */
	public List<Enumeration> sortItems(String sortItem, String sort) {
		// this.enums = getGroups();
		sortByProperty = sortItem;
		if (sort.equals("asc"))
			sort(propertyAscendingComparator);
		else if (sort.equals("dsc"))
			sort(propertyDescendingComparator);
		return enums;
	}

	/**
	 * {@link java.util.List}<{@link ome.admin.model.Enumeration}>.
	 * 
	 * @param sortItem
	 *            {@link java.lang.String}.
	 * @param sort
	 *            {@link java.lang.String}.
	 * @return {@link java.util.List}<{@link ome.admin.model.Enumeration}>.
	 */
	public List<Enumeration> getAndSortItems(String sortItem, String sort) {
		this.enums = getEnums();
		sortByProperty = sortItem;
		if (sort.equals("asc"))
			sort(propertyAscendingComparator);
		else if (sort.equals("dsc"))
			sort(propertyDescendingComparator);
		return enums;
	}

	/**
	 * Sort {@link ome.admin.model.Enumeration} by
	 * {@link java.util.Comparator}
	 * 
	 * @param comparator
	 *            {@link java.util.Comparator}.
	 */
	private void sort(Comparator comparator) {
		Collections.sort(enums, comparator);
	}

}
