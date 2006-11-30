/*
 * org.openmicroscopy.shoola.util.concur.ByteBufferFiller
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

package org.openmicroscopy.shoola.util.concur;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Defines the contract an {@link AsyncByteBuffer}'s provider has to honour.
 * The {@link AsyncByteBuffer} lets the provider fill up its internal buffer
 * by calling the {@link #write(byte[], int, int) write} method progressively,
 * within a filling loop.  When no more data can be written, the provider must
 * return <code>-1</code> so that the {@link AsyncByteBuffer} may exit its
 * filling loop.
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
public interface ByteBufferFiller 
{

	/**
	 * Writes up to <code>length</code> bytes into <code>buffer</code> starting
	 * from <code>buffer[offset]</code>.
	 * The implementation of this method mustn't write outside of the
	 * <code>[offset, offset+length-1]</code> interval.
	 * 
	 * @param buffer	The buffer to write to.
	 * @param offset	The position at which the first byte will be written.
	 * @param length	The maximum number of bytes to write.
	 * @return	The number of bytes actually written.  This could be less than
	 * 			<code>length</code> if less data is availabe to write at the
	 * 			moment of the invocation.  When no more data can be written,
	 * 			this method has to return <code>-1</code> to indicate the end
	 * 			of the stream.
	 * @throws BufferWriteException	If an error occurs and the data can't be
	 * 								written.
	 */
	public int write(byte[] buffer, int offset, int length)
		throws BufferWriteException;
	
	/**
	 * Returns the total amount of bytes that this filler can ever write into
	 * the buffer.
	 * The filler will make sure that when the filling loop ends (that is when 
	 * the {@link #write(byte[], int, int) write} method returns <code>-1</code>
	 * ), at most <code>n</code> bytes will have been written into the buffer,
	 * <code>n</code> being the return value of this method.
	 * 
	 * @return The total amount of bytes that can be written.
	 */	
	public int getTotalLength();

}
