/*
 * org.openmicroscopy.shoola.util.roi.model.util.StackIterator 
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
package org.openmicroscopy.shoola.util.roi.model.util;


//Java imports
import java.util.ArrayList;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.roi.model.ROIShape;

/** 
 * 
 *
 * upon passing a 4D stack to the iterator it will iterate throught the 3D stack
 *  passing back a list of all the ROI in the stack.
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
public class StackIterator
	implements Iterable<ArrayList<ROIShape>>, Iterator<ArrayList<ROIShape>>
{	
	/**  The 4D stack to iterate. */
	private TreeMap<Coord3D, ROIShape> stack;
	
	/** The current timepoint in the 4D stack. */
	private int currentT;
	
	/** The start of the time point in the stack. */
	private int minT;
	
	/** The end of the time point in the stack. */
	private int maxT;
	
	/** The start of the zsection in the stack. */
	private int minZ;
	
	/** The end of the zsection in the stack. */
	private int maxZ;
	
	
	/**
	 * Instance of the stack iterator, upon passing a 4D stack to the 
	 * iterator it will iterate throught the 3D stack passing back a list 
	 * of all the ROI in the stack.
	 * @param stack The stack to iterate. 
	 */
	public StackIterator(TreeMap<Coord3D, ROIShape> stack)
	{
		this.stack = stack;
		Coord3D minCoord = stack.firstKey();
		Coord3D maxCoord = stack.lastKey();
		minZ = minCoord.getZSection();
		maxZ = maxCoord.getZSection();
		minT = minCoord.getTimePoint();
		maxT = maxCoord.getTimePoint();
		currentT = minT;
	}
	
	/** 
	 * Overridden, this returns the iterator for the zStack, used in for(..) 
	 * construct.
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<ArrayList<ROIShape>> iterator()
	{
		return this;
	}

	/** 
	 * Overridden, this returns true if there is another zStack in the iterator.
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext()
	{
		if(currentT<=maxT)
			return true;
		return false;
	}

	/** 
	 * Overridden, this passes back the next zSection stack in the iterator.
	 * @see java.util.Iterator#next()
	 */
	public ArrayList<ROIShape> next()
	{
		Coord3D start = new Coord3D(minZ, currentT);
		Coord3D end = new Coord3D(maxZ, currentT);
		ArrayList<ROIShape> zStack = new ArrayList<ROIShape>();
		if(start.equals(end))
			zStack.add(stack.get(start));
		else
		{
			SortedMap<Coord3D, ROIShape> subStack = stack.subMap(start, end);
			for(Iterator<ROIShape> s = subStack.values().iterator() ; s.hasNext() ;)
				zStack.add(s.next());
		}
		currentT = currentT+1;
		return zStack;
	}

	/** 
	 * Overridden.
	 * @see java.util.Iterator#remove()
	 */
	public void remove()
	{
		// TODO Auto-generated method stub
		
	}
}


