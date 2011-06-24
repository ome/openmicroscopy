/*
 * org.openmicroscopy.shoola.util.ui.omeeditpane.Position 
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
package org.openmicroscopy.shoola.util.ui.omeeditpane;


//Java imports
import java.io.Serializable;
import java.util.Comparator;

//Third-party libraries

//Application-internal dependencies

/** 
 * Locates the position of the text.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class Position
	implements Comparable<Position>, Comparator, Cloneable, Serializable
{	
	
	/** The start position of the text object. */
	private int start;
	
	/** The end position of the text object. */
	private int end;
	
	/**
	 * Creates a new instance. 
	 * 
	 * @param start	The start position of the char.
	 * @param end 	The end position of the char.
	 */
	Position(int start, int end)
	{
		this.start = start;
		this.end = end;
	}
	
	/**
	 * Returns the start position of the text object.
	 * 
	 * @return See above.
	 */
	public int getStart() { return start; }
	
	/**
	 * Returns the end position of the text object.
	 * 
	 * @return See above.
	 */
	public int getEnd() { return end; }
	
	/**
	 * Returns <code>true</code> if the position contains position 
	 * with start(p0) and end(p1), <code>false</code> otherwise.
	 * 
	 * @param p0 See above.
	 * @param p1 See above.
	 * @return See above.
	 */
	public boolean contains(int p0, int p1)
	{ 
		return (start <= p0 && end >= p1);
	}

	/**
	 * Returns <code>true</code> if the position contains position
	 * <code>false</code> otherwise.
	 * 
	 * @param p See above.
	 * @return See above.
	 */
	boolean contains(Position p) { return contains(p.start, p.end); }
	
	/**
	 * Returns <code>true</code> if the position is within position p
	 * <code>false</code> otherwise.
	 * 
	 * @param p See above.
	 * @return See above.
	 */
	boolean within(Position p) { return within(p.start, p.end); }

	/**
	 * Returns <code>true</code> if the position is within position with 
	 * start(p0) and end(p1), <code>false</code> otherwise.
	 * 
	 * @param p0 See above.
	 * @param p1 See above.
	 * @return See above.
	 */
	boolean within(int p0, int p1)
	{
		return (p0 <= start && p1 >= end);
	}
	
	/**
	 * Returns <code>true</code> the position p overlaps this position,
	 * <code>false</code> otherwise.
	 * 
	 * @param p as above.
	 * @return see above.
	 */
	boolean overlaps(Position p)
	{
		if (p.within(this)) return true;
		if (within(p)) return true;
		if (p.start > start && p.start < end) return true;
		if (p.end > start && p.end < end) return true;
		return false;
	}
	
	/**
	 * Returns the length of the object; where position is the characters
	 * [start, end] 
	 * 
	 * @return See above.
	 */
	int length() { return end-start+1; }

	/**
	 * Controls if the passed object is <,> or = the current one.
	 * @see Comparator#compare(Object, Object)
	 */
	public int compare(Object o1, Object o2)
	{
		if (!(o1 instanceof Position) || !(o2 instanceof Position))
			return -1;
		Position a = (Position) o1;
		Position b = (Position) o2;
		if (a.start < b.start) return -1;
		else if (a.start > b.start) return 1;
		else if (a.end < b.end) return -1;
		else if (a.end > b.end) return 1;
		else return 0;
	}
	
	/**
	 * Calculate the hashCode for the data, will have collisions on same start
	 * but should not be that important.  
	 * @return see above.
	 */
	public int hashCode()
	{
		int value = start;
		return value;
	}
	
	/**
	 * Clones the object.
	 * @see java.lang.Object#clone()
	 */
	protected Position clone() { return new Position(this.start, this.end); }

	/**
	 * Returns the string of the position.
	 * @see java.lang.Object#toString()
	 */
	public String toString() { return "["+start+","+end+"]"; }

	/**
	 * Controls if the passed object is <,> or = the current one.
	 * @see Comparable#compareTo(Object)
	 */
	public int compareTo(Position o)
	{
		return compare(this, o);
	}
	
	/**
	 * Overridden to control if the passed object equals the current one.
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object obj)
	{
		if (!(obj instanceof Position)) return false;
		Position comp = (Position) obj;
		return (comp.start == this.start && comp.end == this.end);
	}
	
}


