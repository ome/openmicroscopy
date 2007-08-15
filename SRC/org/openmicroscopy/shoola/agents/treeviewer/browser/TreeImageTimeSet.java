/*
 * org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageTimeSet 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.treeviewer.browser;




//Java imports
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;

//Third-party libraries

//Application-internal dependencies

/** 
 * Node used for smart folders.
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
public class TreeImageTimeSet
	extends TreeImageSet
{

	/** Identifies a node hosting the images imported in the last 7 days. */
	public static final int	WEEK = 0;
	
	/** Identifies a node hosting the images imported in the last 2 weeks. */
	public static final int	TWO_WEEK = 1;
	
	/** Identifies a node hosting the images imported in the current year. */
	public static final int	YEAR = 2;
	
	/** 
	 * Identifies a node hosting the images imported during the year 
	 * before the current year. 
	 */
	public static final int	YEAR_BEFORE = 3;
	
	/** Identifies a node hosting the images imported before current year. */
	public static final int	OTHER = 4;
	
    /** 
	 * Text of the dummy TreeImageSet containing the images 
	 * imported less than a week ago.
	 */
	private static final String 	WEEK_OLD = "Last 7 days";
	
	/** 
	 * Text of the dummy TreeImageSet containing the images 
	 * imported less than two weeks ago.
	 */
	private static final String 	TWO_WEEK_OLD = "Last 2 weeks";
	
    /** 
	 * Text of the dummy TreeImageSet containing the images 
	 * imported before the current year
	 * .
	 */
	private static final String 	PRIOR_TO = "Before ";
	
	/** Node tooltip if the index is {@link #WEEK}. */
	private static final String		WEEK_TOOLTIP = "Contains the " +
									"data imported in the last 7 days.";
	
	/** Node tooltip if the index is {@link #TWO_WEEK}. */
	private static final String		TWO_WEEK_TOOLTIP = "Contains the " +
									"data imported in the last 2 weeks.";
	
	/** Node tooltip if the index is {@link #YEAR}. */
	private static final String		YEAR_TOOLTIP = "Contains the " +
									"data imported in this year.";
	
	/** Node tooltip if the index is {@link #OTHER}. */
	private static final String		OTHER_TOOLTIP = "Contains the " +
									"data imported before this year.";
	
	/** Node tooltip if the index is {@link #YEAR_BEFORE}. */
	private static final String		YEAR_BEFORE_TOOLTIP = "Contains the " +
									"data imported during the period ";
	
	/** A day in milliseconds. */
	private static final long		DAY = 86400000;
	
	/** The node's index. One of the constants defined by this class. */
	private int 		index;
	
	/** 
	 * Time corresponding to 01/01 of the current year if the index is 
	 * {@link #YEAR} or 7 days before the actual day if the index is 
	 * {@link #WEEK}.
	 */
	private Timestamp 	time;
	
	/** Value only set if the index is {@link #YEAR_BEFORE}. */
	private Timestamp	lowerTime;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param index One of the constants defined by this class.
	 */
	public TreeImageTimeSet(int index)
	{
		super("");
		this.index = index;
		GregorianCalendar gc = new GregorianCalendar();
		int year;
		switch (index) {
			case WEEK:
				setUserObject(WEEK_OLD);
				setToolTip(WEEK_TOOLTIP);
				gc = new GregorianCalendar(gc.get(Calendar.YEAR), 
						gc.get(Calendar.MONTH), 
						gc.get(Calendar.DAY_OF_MONTH), 23, 59, 0);
				time = new Timestamp(gc.getTime().getTime()-7*DAY);
				break;
			case TWO_WEEK:
				setUserObject(TWO_WEEK_OLD);
				setToolTip(TWO_WEEK_TOOLTIP);
				gc = new GregorianCalendar(gc.get(Calendar.YEAR), 
						gc.get(Calendar.MONTH), 
						gc.get(Calendar.DAY_OF_MONTH), 23, 59, 0);
				time = new Timestamp(gc.getTime().getTime()-14*DAY);
				break;
			case YEAR:
				setToolTip(YEAR_TOOLTIP);
				year = gc.get(Calendar.YEAR);
				setUserObject(""+year);
				gc = new GregorianCalendar(year, 0, 1, 0, 0, 0);
			    time = new Timestamp(gc.getTime().getTime());
				break;
			case YEAR_BEFORE:
				setToolTip(YEAR_BEFORE_TOOLTIP);
				year = gc.get(Calendar.YEAR);
				setUserObject((year-1)+"-"+year);
				gc = new GregorianCalendar(year, 0, 1, 0, 0, 0);
			    time = new Timestamp(gc.getTime().getTime());
			    gc = new GregorianCalendar(year-1, 0, 1, 0, 0, 0);
			    lowerTime = new Timestamp(gc.getTime().getTime());
				break;
			case OTHER:
				setToolTip(OTHER_TOOLTIP);
				year = gc.get(Calendar.YEAR)-1;
				setUserObject(PRIOR_TO+year);
				gc = new GregorianCalendar(year, 0, 1, 0, 0, 0);
			    time = new Timestamp(gc.getTime().getTime());
				break;
			default: 
				throw new IllegalArgumentException("Node index not valid.");
		}
	}

	/**
	 * Returns the index of the node.
	 * 
	 * @return See above.
	 */
	public int getIndex() { return index; }
	
	/**
	 * Returns the time of reference.
	 * 
	 * @return See above.
	 */
	public Timestamp getTime() { return time; }
	
	/**
	 * Returns the time of reference.
	 * 
	 * @return See above.
	 */
	public Timestamp getLowerTime() { return lowerTime; }
	
}
