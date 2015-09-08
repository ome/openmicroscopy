/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.agents.util;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.swing.filechooser.FileFilter;

import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;

import omero.gateway.model.AnnotationData;
import omero.gateway.model.ChannelData;
import omero.gateway.model.DataObject;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.FileData;
import omero.gateway.model.GroupData;
import omero.gateway.model.ImageData;
import omero.gateway.model.PlateData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.ScreenData;
import omero.gateway.model.TagAnnotationData;
import omero.gateway.model.WellData;

/** 
 * Sorts the values.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: 3208 $ $Date: 2006-02-27 18:56:26 +0000 (Mon, 27 Feb 2006) $)
 * </small>
 * @since OME2.2
 */
public class ViewerSorter
{

    /** Flag to indicate the order selected. */
    private boolean     ascending;
    
    /** Flag to indicate that the objects are ordered by date. */
    private boolean     byDate;
    
    /** The collection to sort. */
    private Collection  collection;
    
    /** The list containing the ordered values. */
    private List        results;
    
    /** Flag indicating to be case sensitive or not. The default value is
     * <code>false</code>.
     */
    private boolean caseSensitive;
    
    /**
     * Compares two {@link Date}s.
     * 
     * @param d1 The first object to compare.
     * @param d2 The second object to compare.
     * @return See below.
     */
    private int compareDates(Date d1, Date d2)
    {
        long n1 = d1.getTime();
        long n2 = d2.getTime();
        int v = 0;
        if (n1 < n2) v = -1;
        else if (n1 > n2) v = 1;
        return v;
    }
    
    /**
     * Compares two {@link String}s.
     * 
     * @param s1 The first object to compare.
     * @param s2 The second object to compare.
     * @return See below.
     */
    private int compareStrings(String s1, String s2)
    {
        if (s1 == null && s2 == null) return 0; 
        else if (s1 == null) return -1; 
        else if (s2 == null) return 1; 
        int v = 0;
        int result;
        if (!caseSensitive) {
        	result = (s1.toLowerCase()).compareTo(s2.toLowerCase());
        } else result = s1.compareTo(s2);
        if (result < 0) v = -1;
        else if (result > 0) v = 1;
        return v;
    }
    
    /**
     * Compares two {@link Object}s.
     * 
     * @param o1 The first object to compare.
     * @param o2 The second object to compare.
     * @return See below.
     */
    private int compareObjects(Object o1, Object o2)
    {
        return compareStrings(o1.toString(), o2.toString());
    }
    
    /**
     * Compares two {@link Boolean}s.
     * 
     * @param bool1 The first object to compare.
     * @param bool2 The second object to compare.
     * @return See below.
     */
    private int compareBooleans(Boolean bool1, Boolean bool2)
    {
        boolean b1 = bool1.booleanValue();
        boolean b2 = bool2.booleanValue();
        int v = 0;
        if (b1 == b2) v = 0;
        else if (b1) v =  -1; //1
        else v = 1;//-1
        return v;
    }
    
    /**
     * Compares two {@link Number}s.
     * 
     * @param n1 The first object to compare.
     * @param n2 The second object to compare.
     * @return See below.
     */
    private int compareNumbers(Number n1, Number n2)
    {
        double d1 = n1.doubleValue();
        double d2 = n2.doubleValue();
        int v = 0;
        if (d1 < d2) v = -1;
        else if (d1 > d2)   v = 1;
        return v;
    }
    
    /**
     * Compares two <code>Long</code>.
     * 
     * @param d1 The first object to compare.
     * @param d2 The second object to compare.
     * @return See below.
     */
    private int compareLongs(long d1, long d2)
    {
         int v = 0;
         if (d1 < d2) v = -1;
         else if (d1 > d2)   v = 1;
         return v;
    }
    
    /**
     *
     * Compares two {@link Timestamp}s.
     * 
     * @param t1 The first object to compare.
     * @param t2 The second object to compare.
     * @return See below.
     */
    private int compareTimestamps(Timestamp t1, Timestamp t2)
    {
    	int v = 0;
        int r = t1.compareTo(t2);
        if (r < 0) v = -1;
        else if (r > 0) v = 1;
        return v;
    }
    
    /**
     * Compares two {@link DataObject}s.
     * 
     * @param o1 The first object to compare.
     * @param o2 The second object to compare.
     * @return See below.
     */
    private int compareDataObjects(DataObject o1, DataObject o2)
    {
    	if (o1 instanceof ChannelData) {
    		ChannelData c1 = (ChannelData) o1;
    		ChannelData c2 = (ChannelData) o2;
    		return compareNumbers(c1.getIndex(), c2.getIndex());
    	}
    	if (o1 instanceof WellData) {
    		return compareNumbers(((WellData) o1).getRow(), 
    				((WellData) o2).getRow());
    	}
    	return compareObjects(o1, o2, true);
    }

    /**
     * Compares the passed objects.
     * 
     * @param o1 			The first object to compare.
     * @param o2 			The second object to compare.
     * @param dataObject 	Pass <code>true</code> to compare data object
     * 						<code>false</code> otherwise.
     * @return See above.
     */
    private int compareObjects(Object o1, Object o2, boolean dataObject)
    {
    	if (!byDate) {
        	int r = compareStrings(getNameFor(o1), getNameFor(o2));
        	if (r == 0) {
        		if (dataObject)
        			return compareLongs(((DataObject) o1).getId(),
        					((DataObject) o2).getId());
        	}
        	return r;
        }
        return compareTimestamps(getTimeFor(o1), getTimeFor(o2));
    }
    
    /**
     * Compares two {@link TreeImageDisplay}s.
     * 
     * @param o1 The first object to compare.
     * @param o2 The second object to compare.
     * @return See below.
     */
    private int compareTreeImageDisplays(TreeImageDisplay o1, 
            TreeImageDisplay o2)
    {
        Object ob1 = o1.getUserObject();
        Object ob2 = o2.getUserObject();
        if ((ob1 instanceof DataObject) && (ob2 instanceof DataObject)) {
        	return compareDataObjects((DataObject) ob1, (DataObject) ob2);
        }
        /*
        if ((ob1 instanceof DataObject) && (ob2 instanceof File)) {
        	return compareObjects(o1, o2);
        } else if ((ob2 instanceof DataObject) && (ob1 instanceof File)) {
        	return compareObjects(o1, o2);
        } else if ((ob1 instanceof File) && (ob2 instanceof File)) {
        	return compareObjects(o1, o2);
        }
        */
        return -1;
    }
    
    /**
     * Compares two {@link ImageDisplay}s.
     * 
     * @param o1 The first object to compare.
     * @param o2 The second object to compare.
     * @return See below.
     */
    private int compareImageDisplays(ImageDisplay o1, ImageDisplay o2)
    {
        Object ob1 = o1.getHierarchyObject();
        Object ob2 = o2.getHierarchyObject();
        if (!(ob1 instanceof DataObject)) return -1;
        if (!(ob2 instanceof DataObject)) return 1;
        return compareDataObjects((DataObject) ob1, (DataObject) ob2);
    }
    
    /** 
     * Returns the insertion time for the specified data object.
     * 
     * @param o The data object to control.
     * @return See above.
     */
    private Timestamp getTimeFor(Object o)
    {
        Timestamp t = null;
        if (o instanceof ImageData) {
            try {
                t = ((ImageData) o).getAcquisitionDate();
            } catch (Exception e) {}
        } else {
        	try {
        		if (o instanceof DataObject) {
        			t = ((DataObject) o).getCreated();
        		} else if (o instanceof File) {
        			t = new Timestamp(((File) o).lastModified());
        		}
                
            } catch (Exception e) {} 
        }
        if (t == null) t = new Timestamp(new Date().getTime());
        return t;
    }
    
    /**
     * Returns the name of the node.
     * 
     * @param obj The object to handle
     * @return See above.
     */
    public String getNameFor(Object obj)
    { 
        if (obj instanceof ProjectData) return ((ProjectData) obj).getName();
        else if (obj instanceof DatasetData) 
            return ((DatasetData) obj).getName();
        else if (obj instanceof ImageData) 
            return ((ImageData) obj).getName();
        else if (obj instanceof ExperimenterData) {
        	ExperimenterData exp = (ExperimenterData) obj;
        	String s = exp.getLastName();
        	if (s != null && s.trim().length() != 0) return s;
        	 s = exp.getFirstName();
         	if (s != null && s.trim().length() != 0) return s;
        	return exp.getUserName();
        }  else if (obj instanceof GroupData) 
            return ((GroupData) obj).getName();
        else if (obj instanceof AnnotationData) 
        	return ((AnnotationData) obj).getContentAsString();
        else if (obj instanceof ScreenData)
        	return ((ScreenData) obj).getName();
        else if (obj instanceof PlateData)
        	return ((PlateData) obj).getName();
        else if (obj instanceof TagAnnotationData)
        	return ((TagAnnotationData) obj).getTagValue();
        else if (obj instanceof FileData)
        	return ((FileData) obj).getName();
        else if (obj instanceof GroupData)
        	return ((GroupData) obj).getName();
        else if (obj instanceof String) return (String) obj;
        return "";
    }

    /**
     * Compares two {@link Object}s.
     * 
     * @param o1 The first object to compare.
     * @param o2 The second object to compare.
     * @return See below.
     */
    private int compare(Object o1, Object o2)
    {
        // If both values are null, return 0.
        if (o1 == null && o2 == null) return 0; 
        else if (o1 == null) return -1; 
        else if (o2 == null) return 1; 
        int result = 0;

        if (o1 instanceof Number || o1 instanceof Integer || 
                o1 instanceof Double || o1 instanceof Float)
            result = compareNumbers((Number) o1, (Number) o2);
        else if ((o1 instanceof Date) && (o2 instanceof Date))
            result = compareDates((Date) o1, (Date) o2);
        else if ((o1 instanceof String) && (o2 instanceof String))
            result = compareStrings((String) o1, (String) o2);
        else if ((o1 instanceof Boolean) && (o2 instanceof Boolean))
            result = compareBooleans((Boolean) o1, (Boolean) o2);    
        else if ((o1 instanceof DataObject) && (o2 instanceof DataObject))
            result = compareDataObjects((DataObject) o1, (DataObject) o2);
        else if ((o1 instanceof TreeImageDisplay) && 
        		(o2 instanceof TreeImageDisplay))
            result = compareTreeImageDisplays((TreeImageDisplay) o1, 
                    (TreeImageDisplay) o2);
        else if ((o1 instanceof ImageDisplay) && (o2 instanceof ImageDisplay))
            result = compareImageDisplays((ImageDisplay) o1, (ImageDisplay) o2);
        else if ((o1 instanceof Timestamp) && (o2 instanceof Timestamp))
        	result = compareTimestamps((Timestamp) o1, (Timestamp) o2);
        else if ((o1 instanceof FileFilter) && (o2 instanceof FileFilter))
        	result = compareStrings(((FileFilter) o1).getDescription(), 
        			((FileFilter) o2).getDescription());
        else if (o1 instanceof ROIShape && o2 instanceof ROIShape) {
        	result = compareLongs(((ROIShape) o1).getID(),
        			((ROIShape) o2).getID());
        } else result = compareObjects(o1, o2);
           
        if (result != 0) return ascending ? result : -result;
        return result;
    }

    /**
     * Sorts the objects.
     * 
     * @param from The source.
     * @param to   The destination.
     * @param low  The lowest value.
     * @param high The highest value
     */
    private void shuttlesort(Object[] from, Object[] to, int low, int high)
    {
        if (high-low < 2) return;
        int middle = (low+high)/2;
        shuttlesort(to, from, low, middle);
        shuttlesort(to, from, middle, high);

        int p = low, q = middle;

        /* This is an optional short-cut; at each recursive call,
        check to see if the elements in this subset are already
        ordered.  If so, no further comparisons are needed; the
        sub-array can just be copied.  The array must be copied rather
        than assigned otherwise sister calls in the recursion might
        get out of sinc.  When the number of elements is three they
        are partitioned so that the first set, [low, mid), has one
        element and and the second, [mid, high), has two. We skip the
        optimisation when the number of elements is three or less as
        the first compare in the normal merge will produce the same
        sequence of steps. This optimisation seems to be worthwhile
        for partially ordered lists but some analysis is needed to
        find out how the performance drops to Nlog(N) as the initial
        order diminishes - it may drop very quickly.  */

        if (high-low >= 4 && compare(from[middle-1], from[middle]) <= 0) {
            for (int i = low; i < high; i++)
                to[i] = from[i];
            return;
        }

        for (int i = low; i < high; i++) {
            if (q >= high || (p < middle && compare(from[p], from[q]) <= 0)) 
                to[i] = from[p++];
            else to[i] = from[q++]; 
        }
    }
    
    /** Sets the default values. */
    private void initialize()
    {
        ascending = true;
        byDate = false;
        caseSensitive = false;
    }
    
    /** Creates a new instance. */
    public ViewerSorter()
    {
        initialize();
    }
    
    /**
     * Creates a new instance. 
     * 
     * @param collection The collection to sort. Mustn't be <code>null</code>.
     */
    public ViewerSorter(Collection collection)
    {
        if (collection == null) throw new NullPointerException("No collection");
        this.collection = collection;
        initialize();
    }
    
    /**
     * Returns the value of the {@link #ascending} flag.
     * 
     * @return See above.
     */
    public boolean isAscending() { return ascending; } 
    
    /**
     * Sets the ascending flag.
     * 
     * @param b Pass <code>true</code> to order the values in the ascending
     *          order, <code>false</code> otherwise.
     */
    public void setAscending(boolean b)
    {
        if (b == ascending) return;
        ascending = b;
    }
 
    /**
     * Sets the case sensitive flag
     * 
     * @param b Pass <code>true</code> to be case sensitive
     * 		<code>false</code> otherwise.
     */
    public void setCaseSensitive(boolean b)
    {
    	caseSensitive = b;
    }
    
    /**
     * Returns <code>true</code> if case sensitive, <code>false</code>
     * otherwise.
     * 
     * @return See above.
     */
    public boolean isCaseSensitive() { return caseSensitive; }
    
    /**
     * Returns <code>true</code> if the collection is ordered by date,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isByDate() { return byDate; }

    /**
     * Passes <code>true</code> to order the collection by date, 
     * <code>false</code> otherwise.
     * 
     * @param b The passed value.
     */
    public void setByDate(boolean b) { byDate = b; }

    /**
     * Sorts the passed array.
     * 
     * @param array The array to sort.
     * @return A list of ordered values.
     */
    public List sort(Object[] array)
    {
    	if (array == null) return null;
    	List<Object> l = new ArrayList<Object>();
    	for (int i = 0; i < array.length; i++) {
			l.add(array[i]);
		} 
    	this.collection = l;
        return sort();
    }
    
    /**
     * Sorts the specified collection.
     * 
     * @param collection The collection to sort.
     * @return A list of ordered values.
     */
    public List sort(Collection collection)
    {
        if (collection == null) return null;
        this.collection = collection;
        return sort();
    }
    
    /**
     * Sorts the collection previously set.
     * 
     * @return A list of ordered values.
     */
    public List sort()
    {
        if (collection == null) return null;
        Iterator i = collection.iterator();
        Object[]  array = new Object[collection.size()];
        Object[]  clone = new Object[collection.size()];
        int index = 0;
        Object obj;
        while (i.hasNext()) {
            obj = i.next();
            array[index] = obj;
            clone[index] = obj;
            index++;
        }
        shuttlesort(clone, array, 0, array.length);
        results = new ArrayList();
        for (int j = 0; j < array.length; j++)  
            results.add(array[j]);
        return results;
    }

    /**
     * Sorts the specified collection.
     * 
     * @param collection The collection to sort.
     * @return An array of ordered values.
     */
    public Object[] sortAsArray(Collection collection)
    {
    	if (collection == null) return null;
        this.collection = collection;
        return sortAsArray();
    }
    
    /**
     * Sorts the passed array.
     * 
     * @param array The array to sort.
     * @return An array of ordered values.
     */
    public Object[] sortAsArray(Object[] array)
    {
    	if (array == null) return null;
    	List<Object> l = new ArrayList<Object>();
    	for (int i = 0; i < array.length; i++) {
			l.add(array[i]);
		} 
    	this.collection = l;
    	return sortAsArray();
    }
    
    /**
     * Sorts the collection previously set.
     * 
     * @return An array of ordered values.
     */
    public Object[] sortAsArray()
    {
    	if (collection == null) return null;
        Iterator i = collection.iterator();
        Object[]  array = new Object[collection.size()];
        Object[]  clone = new Object[collection.size()];
        int index = 0;
        Object obj;
        while (i.hasNext()) {
            obj = i.next();
            array[index] = obj;
            clone[index] = obj;
            index++;
        }
        shuttlesort(clone, array, 0, array.length);
        return array;
    }
    
}
