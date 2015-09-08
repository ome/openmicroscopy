/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.util.browser;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.openmicroscopy.shoola.env.data.model.TimeRefObject;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;

/** 
 * Node used for smart folders.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class TreeImageTimeSet
	extends TreeImageSet
{

	/** Identifies a node hosting the images imported today. */
	public static final int TODAY = 99;
	
	/** Identifies a node hosting the images imported in the last 7 days. */
	public static final int WEEK = 100;
	
	/** Identifies a node hosting the images imported in the last 2 weeks. */
	public static final int TWO_WEEK = 101;

	/** Identifies a node hosting the images imported before current year. */
	public static final int OTHER = 102;
	
	/** Identifies a node hosting the images imported in the current year. */
	public static final int YEAR = 103;
	
	/** 
	 * Identifies a node hosting the images imported during the year 
	 * before the current year. 
	 */
	public static final int YEAR_BEFORE = 104;
	
	/** 
	 * Identifies a node hosting the images imported during the year 
	 * before the current year. 
	 */
	public static final int MONTH = 105;
	
	/** Identifies the month of January. */
	static final int JANUARY = Calendar.JANUARY;
	
	/** Identifies the month of February. */
	static final int FEBRUARY = Calendar.FEBRUARY;
	
	/** Identifies the month of March. */
	static final int MARCH = Calendar.MARCH;
	
	/** Identifies the month of April. */
	static final int APRIL = Calendar.APRIL;
	
	/** Identifies the month of May. */
	static final int MAY = Calendar.MAY;
	
	/** Identifies the month of June. */
	static final int JUNE = Calendar.JUNE;
	
	/** Identifies the month of July. */
	static final int JULY = Calendar.JULY;
	
	/** Identifies the month of August. */
	static final int AUGUST = Calendar.AUGUST;
	
	/** Identifies the month of September. */
	static final int SEPTEMBER = Calendar.SEPTEMBER;
	
	/** Identifies the month of October. */
	static final int OCTOBER = Calendar.OCTOBER;
	
	/** Identifies the month of November. */
	static final int NOVEMBER = Calendar.NOVEMBER;
	
	/** Identifies the month of December. */
	static final int DECEMBER = Calendar.DECEMBER;
	
	 /** 
	 * Text of the dummy TreeImageSet containing the images
	 * imported today.
	 */
	private static final String TODAY_OLD = "Today";
	
    /** 
	 * Text of the dummy TreeImageSet containing the images
	 * imported less than a week ago.
	 */
	private static final String WEEK_OLD = "Last 7 days";
	
	/** 
	 * Text of the dummy TreeImageSet containing the images
	 * imported less than two weeks ago.
	 */
	private static final String TWO_WEEK_OLD = "Last 2 weeks";
	
    /** 
	 * Text of the dummy TreeImageSet containing the images
	 * imported before the current year
	 * .
	 */
	private static final String PRIOR_TO = "Before ";
	
	/** Node tooltip if the index is {@link #TODAY}. */
	private static final String TODAY_TOOLTIP = "Contains the " +
			"data imported today.";
	
	/** Node tooltip if the index is {@link #WEEK}. */
	private static final String WEEK_TOOLTIP = "Contains the " +
									"data imported in the last 7 days.";
	
	/** Node tooltip if the index is {@link #TWO_WEEK}. */
	private static final String TWO_WEEK_TOOLTIP = "Contains the " +
									"data imported in the last 2 weeks.";
	
	/** Node tooltip if the index is {@link #YEAR}. */
	private static final String YEAR_TOOLTIP = "Contains the " +
									"data imported in this year.";
	
	/** Node tooltip if the index is {@link #OTHER}. */
	private static final String OTHER_TOOLTIP = "Contains the " +
									"data imported before this year.";
	
	/** Node tooltip if the index is {@link #YEAR_BEFORE}. */
	private static final String YEAR_BEFORE_TOOLTIP = "Contains the " +
									"data imported during the period ";
	
	/** Node tooltip if the index is a month. */
	private static final String MONTH_TOOLTIP = "Contains the " +
									"data imported in selected month.";
	
	/** A day in milliseconds. */
	private static final long DAY = UIUtilities.DAY;
	
	/** The default text. */ 
	private static final String TEXT = "_";
	
	
	/** The node's index. One of the constants defined by this class. */
	private int type;
	
	/** 
	 * Time corresponding to 01/01 of the current year if the index is 
	 * {@link #YEAR} or 7 days before the actual day if the index is 
	 * {@link #WEEK}.
	 */
	private Timestamp endTime;
	
	/** Value only set if the index is {@link #YEAR_BEFORE}. */
	private Timestamp startTime;
	
	/** The index. */
	private int index;
	
	/** The ref object hosting the time interval. */
	private TimeRefObject ref;
	
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
	 * @param year The selected year.
	 * @return See above
	 */
	private int getLastDayOfMonth(int month, int year)
	{
		switch (month) {
			case JANUARY: return 31;
			case FEBRUARY:
				if (year%4 == 0) return 29;
				if (year%100 == 0) return 28;
				if (year%400 == 0) return 29;
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
	 * Returns <code>true</code> if the passed time is contained in the time
	 * interval defined by {@link #startTime} and {@link #endTime},
	 * <code>false</code> otherwise.
	 * 
	 * @param t The value to handle.
	 * @return See above.
	 */
	private boolean containTime(Timestamp t)
	{
		if (t == null) return false;
		if (startTime == null && endTime == null) return false;
		if (startTime == null && endTime != null)
			return t.before(endTime);
		if (startTime != null && endTime == null)
			return t.after(startTime);
		if (startTime != null && endTime != null)
			return (t.after(startTime) && t.before(endTime));
		return false;
	}
	
	/**
	 * Returns the current month.
	 * 
	 * @return See above.
	 */
	public static int getCurrentMonth() 
	{ 
		GregorianCalendar gc = new GregorianCalendar();
		return gc.get(Calendar.MONTH);
	}
	
	/**
     * Returns the node hosting the experimenter passing a child node.
     * 
     * @param node The child node.
     * @param path The path to the top node.
     * @return See above.
     */
    public static String createPath(TreeImageDisplay node, String path)
    {
    	if (node == null) return path;
    	TreeImageDisplay parent = node.getParentDisplay();
    	Object ho;
    	ExperimenterData exp;
    	GroupData group;
    	if (parent == null) {
    		ho = node.getUserObject();
    		if (ho instanceof ExperimenterData) {
    			exp = (ExperimenterData) ho;
    			path = "gid"+exp.getGroupId()+TEXT+"eid"+exp.getId()+TEXT+path;
    			return path;
    		}
    		if (ho instanceof GroupData) {
    			group = (GroupData) ho;
    			path = "gid"+group.getId()+TEXT+path;
    			return path;
    		}
    		path = ho.toString()+TEXT+path;
    		return path;
    	}
    	ho = parent.getUserObject();
    	if (ho instanceof ExperimenterData) {
    		exp = (ExperimenterData) ho;
    		//check if we have grandparent
    		TreeImageDisplay gp = parent.getParentDisplay();
    		if (gp != null && gp.getUserObject() instanceof GroupData) {
    			group = (GroupData) gp.getUserObject();
    			path = "gid"+group.getId()+TEXT+"eid"+exp.getId()+TEXT+path;
    		}

    		return path;
    	}
    	if (ho instanceof GroupData) {
    		group = (GroupData) ho;
    		path = "gid"+group.getId()+TEXT+path;
    		return path;
    	}
    	path = ho.toString()+TEXT+path;
    	return createPath(parent, path);
    }
	/**
	 * Creates a new instance.
	 * 
	 * @param type One of the constants defined by this class.
	 */
	public TreeImageTimeSet(int type)
	{
		super("");
		this.type = type;
		index = type;
		GregorianCalendar gc = new GregorianCalendar();
		int year;
		endTime = null;
		switch (type) {
			case TODAY:
				setUserObject(TODAY_OLD);
				setToolTip(TODAY_TOOLTIP);
				gc = new GregorianCalendar(gc.get(Calendar.YEAR),
						gc.get(Calendar.MONTH),
						gc.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
				startTime = new Timestamp(gc.getTime().getTime());
				break;
			case WEEK:
				setUserObject(WEEK_OLD);
				setToolTip(WEEK_TOOLTIP);
				gc = new GregorianCalendar(gc.get(Calendar.YEAR),
						gc.get(Calendar.MONTH),
						gc.get(Calendar.DAY_OF_MONTH), 23, 59, 0);
				startTime = new Timestamp(gc.getTime().getTime()-7*DAY);
				break;
			case TWO_WEEK:
				setUserObject(TWO_WEEK_OLD);
				setToolTip(TWO_WEEK_TOOLTIP);
				gc = new GregorianCalendar(gc.get(Calendar.YEAR),
						gc.get(Calendar.MONTH),
						gc.get(Calendar.DAY_OF_MONTH), 23, 59, 0);
				startTime = new Timestamp(gc.getTime().getTime()-14*DAY);
				break;
			case YEAR:
				setToolTip(YEAR_TOOLTIP);
				year = gc.get(Calendar.YEAR);
				setUserObject(""+year);
				gc = new GregorianCalendar(year, 0, 1, 0, 0, 0);
				startTime = new Timestamp(gc.getTime().getTime());
				endTime = UIUtilities.getDefaultTimestamp();
				break;
			case YEAR_BEFORE:
				setToolTip(YEAR_BEFORE_TOOLTIP);
				year = gc.get(Calendar.YEAR);
				setUserObject(""+(year-1));
				gc = new GregorianCalendar(year, 0, 1, 0, 0, 0);
				endTime = new Timestamp(gc.getTime().getTime());
				gc = new GregorianCalendar(year-1, 0, 1, 0, 0, 0);
				startTime = new Timestamp(gc.getTime().getTime());
				break;
			case OTHER:
				setToolTip(OTHER_TOOLTIP);
				year = gc.get(Calendar.YEAR)-1;
				setUserObject(PRIOR_TO+year);
				gc = new GregorianCalendar(year, 0, 1, 0, 0, 0);
				startTime = null;
				endTime = new Timestamp(gc.getTime().getTime());
				break;
			default:
				throw new IllegalArgumentException("Node index not valid.");
		}
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param type One of the constants defined by this class.
	 * @param monthIndex The index of the month.
	 */
	public TreeImageTimeSet(int type, int monthIndex)
	{
		super("");
		this.type = MONTH;
		index = type+12*(monthIndex+1);
		GregorianCalendar gc = new GregorianCalendar();
		int year;
		int month;
		int lastDay;
		switch (type) {
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
				gc = new GregorianCalendar(year, monthIndex, lastDay, 23, 59,
											0);
				endTime = new Timestamp(gc.getTime().getTime());
				gc = new GregorianCalendar(year, monthIndex, 1, 0, 0, 0);
				startTime = new Timestamp(gc.getTime().getTime());
				break;
			case YEAR_BEFORE:
				setToolTip(MONTH_TOOLTIP);
				year = gc.get(Calendar.YEAR)-1;
				setUserObject(""+getMonth(monthIndex));
				gc = new GregorianCalendar(year, monthIndex,
								getLastDayOfMonth(monthIndex, year), 23, 59, 0);
				endTime = new Timestamp(gc.getTime().getTime());
				gc = new GregorianCalendar(year, monthIndex, 1, 0, 0, 0);
				startTime = new Timestamp(gc.getTime().getTime());
				break;
			default: 
				throw new IllegalArgumentException("Node index not valid.");
		}
	}

	/**
	 * Returns the number of items from the passed list contained in the
	 * time interval defined by this class.
	 * 
	 * @param times The collection to handle.
	 * @return See above.
	 */
	public int countTime(List times)
	{
		if (times == null) return -1;
		Iterator i = times.iterator();
		int number = 0;
		while (i.hasNext()) {
			if (containTime((Timestamp) i.next()))
				number++;
		}
		return number;
	}
	
	/**
	 * Returns the index of the node.
	 * 
	 * @return See above.
	 */
	public int getIndex() { return index; }
	
	/**
	 * Returns the type of the node.
	 * 
	 * @return See above.
	 */
	public int getType() { return type; }
	
	/**
	 * Returns the time of reference.
	 * 
	 * @return See above.
	 */
	public Timestamp getEndTime() { return endTime; }
	
	/**
	 * Returns the time of reference.
	 * 
	 * @return See above.
	 */
	public Timestamp getStartTime() { return startTime; }
	
	/**
	 * Returns the time object corresponding to the node.
	 * 
	 * @return See above.
	 */
	public TimeRefObject getTimeObject(long id)
	{
		if (ref == null) {
			ref = new TimeRefObject(id, TimeRefObject.TIME);
			ref.setTimeInterval(startTime, endTime);
		}
		return ref;
	}

}
