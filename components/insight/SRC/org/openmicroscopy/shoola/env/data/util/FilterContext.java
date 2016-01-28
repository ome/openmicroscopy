/*
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
package org.openmicroscopy.shoola.env.data.util;


import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import omero.gateway.model.TagAnnotationData;
import omero.gateway.model.TextualAnnotationData;

/** 
 * Helper class storing the filtering parameters.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class FilterContext
{

	/** Indicates that the rating context only is selected. */
	public static final int RATE = 0;
	
	/** Indicates that the tag context only is selected. */
	public static final int TAG = 1;
	
	/** Indicates that the comment context only is selected. */
	public static final int COMMENT = 2;
	
	/** Indicates that several context are selected. */
	public static final int MULTI = 3;
	
	/** Indicates that no contexts selected. */
	public static final int NONE = 4;
	
	/** Indicates that the name context selected. */
	public static final int NAME = 5;
	
	/** Indicates that the ROI context selected. */
	public static final int ROI = 6;
	
	/** Indicate to retrieve objects with a value greater than the passed level. */
	public static final int GREATER_EQUAL = 0;
	
	/** Indicate to retrieve objects with a value lower than the passed level. */
	public static final int LOWER_EQUAL = 1;
	
	/** Indicate to retrieve objects rated with passed level. */
	public static final int EQUAL = 2;
	
	/** Indicate to retrieve the union of the all parameters. */
	public static final int UNION = 100;
	
	/** Indicate to retrieve the union of the all parameters. */
	public static final int INTERSECTION = 101;
	
	/** The selected rating level. */
	private int 						rate;
	
	/** The selected number of ROIs */
	private int                                            rois;
	
	/** One of the constants defined by this class. */
	private int							rateIndex;
	
	/** One of the constants defined by this class. */
	private int                                                     roiIndex;
	
	/** The collection of annotation type. */
	private Map<Class, List<String>> 	annotationType;
	
	/** The lower bound of the time interval. */
	private Timestamp					fromDate;
	
	/** The upper bound of the time interval. */
	private Timestamp					toDate;
	
	/** The type of result either: {@link #UNION} or {@link #INTERSECTION}. */
	private int							resultType;
	
	/** The type of filter. */
	private List<Integer>				type;
	
	/** The collection of name to filter by. */
	private List<String>				names;

	/** The time interval to check.*/
	private int timeType;

	/** Creates a new instance. */
	public FilterContext()
	{
		annotationType = new HashMap<Class, List<String>>();
		rate = -1;
		rateIndex = -1;
		rois = -1;
		roiIndex = GREATER_EQUAL;
		resultType = INTERSECTION;
		type = new ArrayList<Integer>();
	}
	
	/**
	 * Returns <code>true</code> if it is only filtered by name, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isNameOnly()
	{
		int size = type.size();
		if (size > 1) return false;
		return type.contains(NAME);
	}
	
	/**
	 * Returns <code>true</code> if it is only filtered by name, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isTagsOnly()
	{
		int size = type.size();
		if (size > 1) return false;
		return type.contains(TAG);
	}
	
	
	/**
	 * Returns one of the following constants: {@link #RATE}, {@link #TAG} or
	 * {@link #COMMENT}, {@link #NAME}, {@link #ROI} or 
	 * <code>-1</code> when more than one type is selected.
	 * 
	 * @return See above.
	 */
	public int getContext()
	{
		int size = type.size();
		if (size == 0) return NONE;
		if (size > 1) return MULTI;
		return type.get(0);
	}
	
	/**
	 * Returns the type of filter used if any.
	 * 
	 * @return See above.
	 */
	public List<Integer> getContextList() { return type; }
	
	/**
	 * Sets the result type.
	 * 
	 * @param type The value to set.
	 */
	public void setResultType(int type)
	{
		switch (type) {
			case UNION:
			case INTERSECTION:
				resultType = type;
				break;
			default:
				resultType = INTERSECTION;
		}
	}
	
	/**
	 * Returns the result type.
	 * 
	 * @return See above.
	 */
	public int getResultType() { return resultType; }
	
	/**
	 * Sets the rating index and the selected rate.
	 * 
	 * @param index	One of the constants defined by this class.
	 * @param rate	The selected rating index. A value between 0 and 5.
	 */
	public void setRate(int index, int rate)
	{
		switch (index) {
			case LOWER_EQUAL:
			case GREATER_EQUAL:
			case EQUAL:
				this.rateIndex = index;
				break;
			default:
				this.rateIndex = GREATER_EQUAL;
		}
		this.rate = rate;
		type.add(RATE);
	}
	
	/**
	 * Sets the ROI index and the selected number of ROIs.
	 * 
	 * @param index One of the constants defined by this class.
	 * @param rois The selected number of ROIs.
	 */
	public void setRois(int index, int rois) {
	    this.roiIndex = index;
	    this.rois = rois;
	    type.add(ROI);
	}
	
	/**
	 * Sets the time interval.
	 * 
	 * @param fromDate	The lower bound of the time interval.
	 * @param toDate	The upper bound of the time interval.
	 */
	public void setTimeInterval(Timestamp fromDate, Timestamp toDate)
	{
		if (fromDate != null && toDate != null && fromDate.after(toDate)) {
			this.fromDate = toDate;
			this.toDate = fromDate;
		} else {
			this.fromDate = fromDate;
			this.toDate = toDate;
		}
	}

	/**
	 * Sets the time interval to check.
	 *
	 * @param type The value to set.
	 */
	public void setTimeType(int type)
	{
	    timeType = type;
	}

	/**
	 * Return the time interval to check.
	 *
	 * @return See above.
	 */
	public int getTimeType() { return timeType; }

	/**
	 * Returns the lower bound of the time interval.
	 * 
	 * @return See above
	 */
	public Timestamp getFromDate() { return fromDate; }
	
	/**
	 * Returns the upper bound of the time interval.
	 * 
	 * @return See above
	 */
	public Timestamp getToDate() { return toDate; }
	
	/**
	 * Returns the selected rate.
	 * 
	 * @return See above.
	 */
	public int getRate() { return rate; }
	
	/**
	 * Returns the selected number of ROIs.
	 * 
	 * @return See above.
	 */
	public int getROIs() { return rois; }
	
	/**
	 * Returns one of rating filtering constants defined by this class.
	 * 
	 * @return See above.
	 */
	public int getRateIndex() { return rateIndex; }
	
	/**
	 * Returns one of rating filtering constants defined by this class.
	 * 
	 * @return See above.
	 */
	public int getRoiIndex() { return roiIndex; }
	
	/**
	 * Returns the collection of annotations contained text to filter
	 * by.
	 * 
	 * @return See above.
	 */
	public Map<Class, List<String>> getAnnotationType()
	{ 
		return annotationType; 
	}
	
	/**
	 * Returns the collection of terms corresponding to the passed type
	 * of annotation.
	 * 
	 * @param klass The type of annotation.
	 * @return See above.
	 */
	public List<String> getAnnotation(Class klass)
	{
		return annotationType.get(klass);
	}
	
	/**
	 * Adds the specified type to the collection of types.
	 * 
	 * @param klass The type to add.
	 * @param terms The terms related to the annotation type.
	 */
	public void addAnnotationType(Class klass, List<String> terms)
	{
		if (klass != null && terms != null && terms.size() > 0) {
			if (klass.equals(TagAnnotationData.class))
				type.add(TAG);
			else if (klass.equals(TextualAnnotationData.class))
				type.add(COMMENT);
			annotationType.put(klass, terms);
		}
	}
	
	/**
	 * Adds the name to filter by.
	 * 
	 * @param terms The collection of name.
	 */
	public void addName(List<String> terms)
	{
		if (terms == null || terms.size() == 0) return;
		type.add(NAME);
		names = terms;
	}
	
	/**
	 * Returns the collection of names to filter by.
	 * 
	 * @return See above.
	 */
	public List<String> getNames() { return names; }

}
