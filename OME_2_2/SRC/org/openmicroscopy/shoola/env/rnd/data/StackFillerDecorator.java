/*
 * org.openmicroscopy.shoola.env.rnd.data.StackFillerDecorator
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

package org.openmicroscopy.shoola.env.rnd.data;

//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.ds.st.Pixels;
import org.openmicroscopy.shoola.env.data.PixelsService;
import org.openmicroscopy.shoola.env.rnd.metadata.PixelsDimensions;
import org.openmicroscopy.shoola.util.mem.ReadOnlyByteArray;

/** 
 * 
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
class StackFillerDecorator 
	implements StackFiller
{

	//State flags.
	private static final int		STOPPED = 0;
	private static final int		READY = 1;
	private static final int		FILLING = 2;
	private static final int		FILLED = 3;
	
	
	//Implements the actual functionality.
	StackFillerImpl delegate;
	
	//Tracks the current state of the delegate.
	private int		state;
	
	
	
	StackFillerDecorator(StackFillerImpl delegate) 
	{
		this.delegate = delegate;
		this.state = STOPPED;
	}

	public void configure(Pixels pixelsID, PixelsDimensions pixDims,
							int bytesPerPixel, PixelsService source)
		throws DataSourceException
	{
		if (state != STOPPED || state != FILLED) return;
		try {
			delegate.configure(pixelsID, pixDims, bytesPerPixel, source);
		} catch (DataSourceException dse) {
			state = STOPPED;
			throw dse;
		}
		state = READY;
	}

	public void fill(int t, boolean bigEndian) 
		throws DataSourceException
	{
		if (state != READY) return;
		state = FILLING;
		try {
			delegate.fill(t, bigEndian);
		} finally {
			if (state == FILLING) state = STOPPED;  //Exception kicked in.
			state = FILLED;
		}
	}

	public void stop()
	{
	}

	public boolean isPlaneDataAvailable(int z, int w)
	{
		return false;
	}

	public boolean isStackDataAvailable(int w)
	{
		return false;
	}

	public ReadOnlyByteArray getPlaneData(int z, int w)
	{
		return null;
	}

	public ReadOnlyByteArray getStackData(int w)
	{
		return null;
	}

}
