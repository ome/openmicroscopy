/*
 * ome.util.mem.ByteArray
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

package ome.util.mem;

//Java imports
import java.io.IOException;
import java.io.InputStream;

//Third-party libraries

//Application-internal dependencies

/** 
 * A read-write slice of a given array.
 * This class extends {@link ReadOnlyByteArray} to allow for elements to be
 * written to the array slice.  This class works just like its parent; so you
 * get relative indexing and any changes to the original array will be visible
 * in the corresponding <code>ByteArray</code> object, and vice-versa, any
 * invocation of the <code>set</code> methods will be reflected into the 
 * original array.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: 1.1 $ $Date: 2005/06/09 15:01:57 $)
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
	 * Writes <code>value</code> into the element at the <code>index</code>
     * position within this slice.
	 * 
 	 * @param index	The index, within this slice, at which to write.  Must be
     *              in the <code>[0, {@link ReadOnlyByteArray#length})</code>
     *              interval.
	 * @param value	The value to write.
	 */
	public void set(int index, byte value)
	{
		checkIndex(index);
		base[offset+index] = value;
	}
    
    /**
     * Copies the values in <code>buf</code> into this slice, starting from
     * <code>index</code>.
     * This method first checks to see whether <code>buf.length</code> bytes
     * can be written into this slice starting from <code>index</code>.  If
     * the check fails an {@link ArrayIndexOutOfBoundsException} is thrown
     * and no data is copied at all.
     * 
     * @param index The index, within this slice, from which to start writing.
     *              Must be in the <code>[0, {@link ReadOnlyByteArray#length})
     *              </code> interval. 
     * @param buf A buffer containing the values to write.  Mustn't be 
     *              <code>null</code> and must fit into this slice.  If
     *              the length is <code>0</code>, then this method does
     *              nothing.
     */
    public void set(int index, byte[] buf)
    {
        if (buf == null) throw new NullPointerException("No buffer.");
        if (buf.length == 0) return;
        checkIndex(index);
        checkIndex(index+buf.length-1);
        System.arraycopy(buf, 0, base, offset+index, buf.length);
    }
    
    /**
     * Writes up to <code>maxLength</code> bytes from the supplied stream into
     * this slice, starting from <code>index</code>.
     * To be precise, this method will attempt to write <code>m</code> bytes
     * into this slice (starting from the element at <code>index</code>),
     * <code>m</code> being the minimum of the following numbers:
     * <code>maxLength</code>, 
     * <code>this.{@link ReadOnlyByteArray#length length} - index</code>
     * (that is, bytes from <code>index</code> to the end of the slice),
     * and the number of bytes to the end of the supplied stream.
     * 
     * @param index The index, within this slice, from which to start writing.
     *              Must be in the <code>[0, {@link ReadOnlyByteArray#length})
     *              </code> interval.
     * @param maxLength The maximum amount of bytes to write.  If not positive,
     *                  this method does nothing and returns <code>0</code>.
     * @param in    The stream from which to read data.  Mustn't be 
     *              <code>null</code>.
     * @return The amount of bytes actually written or <code>-1</code> if the
     *          end of the stream has been reached.
     * @throws IOException  If an I/O error occurred while reading data from
     *                      the stream.
     */
    public int set(int index, int maxLength, InputStream in) 
        throws IOException
    {
        if (in == null) throw new NullPointerException("No stream.");
        if (maxLength <= 0) return 0;
        checkIndex(index);
        maxLength = (index+maxLength <= length ? maxLength : length-index);
        return in.read(base, offset+index, maxLength);
    }

}
