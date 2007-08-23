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
	public static final int	WEEK = 100;
	
	/** Identifies a node hosting the images imported in the last 2 weeks. */
	public static final int	TWO_WEEK = 101;

	/** Identifies a node hosting the images imported before current year. */
	public static final int	OTHER = 102;
	
	/** Identifies a node hosting the images imported in the current year. */
	public static final int	YEAR = 103;
	
	/** 
	 * Identifies a node hosting the images imported during the year 
	 * before the current year. 
	 */
	public static final int	YEAR_BEFORE = 104;
	
	/** 
	 * Identifies a node hosting the images imported during the year 
	 * before the current year. 
	 */
	public static final int	MONTH = 105;
	
	/** Identifies the month of january. */
	static final int JANUARY = Calendar.JANUARY;
	
	/** Identifies the month of february. */
	static final int FEBRUARY = Calendar.FEBRUARY;
	
	/** Identifies the month of march. */
	static final int MARCH = Calendar.MARCH;
	
	/** Identifies the month of april. */
	static final int APRIL = Calendar.APRIL;
	
	/** Identifies the month of may. */
	static final int MAY = Calendar.MAY;
	
	/** Identifies the month of june. */
	static final int JUNE = Calendar.JUNE;
	
	/** Identifies the month of july. */
	static final int JULY = Calendar.JULY;
	
	/** Identifies the month of august. */
	static final int AUGUST = Calendar.AUGUST;
	
	/** Identifies the month of september. */
	static final int SEPTEMBER = Calendar.SEPTEMBER;
	
	/** Identifies the month of october. */
	static final int OCTOBER = Calendar.OCTOBER;
	
	/** Identifies the month of november. */
	static final int NOVEMBER = Calendar.NOVEMBER;
	
	/** Identifies the month of december. */
	static final int DECEMBER = Calendar.DECEMBER;
	
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
	
	/** Node tooltip if the index is a month. */
	private static final String		MONTH_TOOLTIP = "Contains the " +
									"data imported in selected month.";
	
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
	 * Returns the month corresponding to the passed index.
	 * 
	 * @param month One of the values identifying the months.
	 * @return See above.
	 */
	private String getMonth(int month) 
	{
		switch (month) {
			case JANUARY: return "January";
			case FEBRUARY: return "February";
			case MARCH: return "March";
			case APRIL: return "April";
			case MAY: return "May";
			case JUNE: return "June";
			case JULY: return "July";
			case AUGUST: return "August";
			case SEPTEMBER: return "September";
			case OCTOBER: return "October";
			case NOVEMBER: return "November";
			case DECEMBER: return "December";
			default:
				throw new IllegalArgumentException("Month not valid.");
		}
	}
	
	/** 
	 * Returns the last day of the month.
	 * 
	 * @param month The selected month.
	 * @param year	The selected year.
	 * @return See above
	 */
	private int getLastDayOfMonth(int month, int year)
	{
		switch (month) {
			case JANUARY: return 31;
			case FEBRUARY: 
				if (year % 4 == 0) return 29;
				if (year % 100 == 0) return 28;
				if (year % 400 == 0) return 29;
				return 28;
			case MARCH: return 31;
			case APRIL: return 30;
			case MAY: return 31;
			case JUNE: return 30;
			case JULY: return 31;
			case AUGUST: return 31;
			case SEPTEMBER: return 30;
			case OCTOBER: return 31;
			case NOVEMBER: return 30;
			case DECEMBER: return 31;
			default:
				
			throw new IllegalArgumentException("Month not valid.");
		}
	}
	
	/**
	 * Returns the current month.
	 * 
	 * @return See above.
	 */
	static int getCurrentMonth() 
	{ 
		GregorianCalendar gc = new GregorianCalendar();
		return gc.get(Calendar.MONTH);
	}
	
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
				setUserObject(""+(year-1));
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
	 * Creates a new instance.
	 * 
	 * @param index 		One of the constants defined by this class.
	 * @param monthIndex 	The index of the month.
	 */
	public TreeImageTimeSet(int index, int monthIndex)
	{
		super("");
		this.index = MONTH;
		GregorianCalendar gc = new GregorianCalendar();
		int year;
		int month;
		int lastDay;
		switch (index) {
			case YEAR:
				setToolTip(MONTH_TOOLTIP);
				year = gc.get(Calendar.YEAR);
				month = gc.get(Calendar.MONTH);
				setUserObject(""+getMonth(monthIndex));
				if (monthIndex == month) { // i.e. current month
					lastDay = gc.get(Calendar.DAY_OF_MONTH);
				} else {
					lastDay = getLastDayOfMonth(monthIndex, year);
				}
				gc = new GregorianCalendar(year, monthIndex, lastDay, 23, 59, 0);
				time = new Timestamp(gc.getTime().getTime());
				gc = new GregorianCalendar(year, monthIndex, 1, 0, 0, 0);
				lowerTime = new Timestamp(gc.getTime().getTime());
				break;
			case YEAR_BEFORE:
				setToolTip(MONTH_TOOLTIP);
				year = gc.get(Calendar.YEAR)-1;
				setUserObject(""+getMonth(monthIndex));
				gc = new GregorianCalendar(year, monthIndex, 
								getLastDayOfMonth(monthIndex, year), 23, 59, 0);
				time = new Timestamp(gc.getTime().getTime());
				gc = new GregorianCalendar(year, monthIndex, 1, 0, 0, 0);
				lowerTime = new Timestamp(gc.getTime().getTime());
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
