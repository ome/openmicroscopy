/*
 * org.openmicroscopy.shoola.env.rnd.data.StackFiller
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
interface StackFiller
{

	void configure(Pixels pixelsID, PixelsDimensions pixDims, 
					int bytesPerPixel, PixelsService source)
		throws DataSourceException;
	
	void fill(int t, boolean bigEndian) throws DataSourceException;
	
	void stop();
	
	boolean isPlaneDataAvailable(int z, int w);
	
	boolean isStackDataAvailable(int w);
	
	ReadOnlyByteArray getPlaneData(int z, int w);
	
	ReadOnlyByteArray getStackData(int w);
			
}
