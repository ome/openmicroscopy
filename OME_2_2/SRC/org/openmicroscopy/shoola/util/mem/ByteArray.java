/*
 * org.openmicroscopy.shoola.util.mem.ByteArray
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.util.mem;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * A read-write slice of a given array.
 * This class extends {@link ReadOnlyByteArray} to allow for elements to be
 * written to the array slice.  This class works just like its parent; so you
 * get relative indexing and any changes to the original array will be visible
 * in the corresponding <code>ByteArray</code> object, and vice-versa, any
 * invocation of the {@link #set(int, byte) set} method will be reflected into
 * the original array.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class ByteArray 
	extends ReadOnlyByteArray
{

	/**
	 * Creates a read-write slice of <code>base</code>.
	 * The <code>offset</code> argument marks the start of the slice, at
	 * <code>base[offset]</code>.  The <code>length</code> argument defines
	 * the length of the slice, the last element being 
	 * <code>base[offset+length-1]</code>.  Obviously enough, these two
	 * arguments must define an interval  
	 * <code>[offset, offset+length]</code> in <code>[0, base.length]</code>.
	 * 
	 * @param base	The original array.
	 * @param offset The start of the slice.
	 * @param length	The length of the slice.
	 */
	public ByteArray(byte[] base, int offset, int length) 
	{
		super(base, offset, length);
	}
	
	/**
	 * Writes the element at the <code>index</code> position within this
	 * slice.
	 * 
 	 * @param index	The index.
	 * @param value	The value to write.
	 */
	public void set(int index, byte value)
	{
		checkIndex(index);
		base[offset+index] = value;
	}

}
