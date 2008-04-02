/*
 * org.openmicroscopy.shoola.env.data.util.FilterContext 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.env.data.util;



//Java imports
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies

/** 
 * Helper class storing the filtering parameters.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class FilterContext
{

	/** Indicate to retrieve objects rated higher than the passed level. */
	public static final int HIGHER = 0;
	
	/** Indicate to retrieve objects rated lower than the passed level. */
	public static final int LOWER = 1;
	
	/** Indicate to retrieve objects rated with passed level. */
	public static final int EQUAL = 2;
	
	/** The selected rating level. */
	private int 						rate;
	
	/** One of the constants defined by this class. */
	private int							index;
	
	/** The collection of annotation type. */
	private Map<Class, List<String>> 	annotationType;
	
	/** The lower bound of the time interval. */
	private Timestamp					fromDate;
	
	/** The upper bound of the time interval. */
	private Timestamp					toDate;
	
	/** Creates a new instance. */
	public FilterContext()
	{
		annotationType = new HashMap<Class, List<String>>();
		rate = -1;
		index = -1;
	}
	
	/**
	 * Sets the rating index and the selected rate.
	 * 
	 * @param index	One of the constants defined by this class.
	 * @param rate	The selected rating index. A value between 0 and 5.
	 */
	public void setRate(int index, int rate)
	{
		switch (index) {
			case LOWER:
			case HIGHER:
			case EQUAL:
				this.index = index;
				break;
			default:
				this.index = HIGHER;
		}
		this.rate = rate;
	}
	
	/**
	 * Sets the time interval.
	 * 
	 * @param fromDate	The lower bound of the time interval.
	 * @param toDate	The upper bound of the time interval.
	 */
	public void setTimeInterval(Timestamp fromDate, Timestamp toDate)
	{
		if (fromDate != null && fromDate.after(toDate)) {
			this.fromDate = toDate;
			this.toDate = fromDate;
		} else {
			this.fromDate = fromDate;
			this.toDate = toDate;
		}
	}
	
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
	 * Returns one of rating filtering constants defined by this class.
	 * 
	 * @return See above.
	 */
	public int getIndex() { return index; }
	
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
	 * Adds the specified type to the collection of types.
	 * 
	 * @param klass The type to add.
	 * @param terms The terms related to the annotation type.
	 */
	public void addAnnotationType(Class klass, List<String> terms)
	{
		if (klass != null && terms != null && terms.size() > 0) 
			annotationType.put(klass, terms);
	}
	
}
