/*
 * org.openmicroscopy.shoola.env.rnd.data.StackFillerImpl
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
import java.io.IOException;
import java.io.InputStream;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.ds.st.Pixels;
import org.openmicroscopy.is.ImageServerException;
import org.openmicroscopy.shoola.env.data.PixelsService;
import org.openmicroscopy.shoola.env.data.PixelsServiceAdapter;
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
class StackFillerImpl
	implements StackFiller
{
	
	//Proxy.
	private PixelsServiceAdapter	omeis;  //TODO: just tmp hack, breaks I/F.
	
	//Area of memory to contain the various waveleght stacks.
	private byte[] 	memArea;
	//NOTE: why not use java.nio.ByteBuffer? B/c fillStack() operates on
	//InputStream, which doesn't have a getChannel() method. (If we could only
	//get that damn socket from the http lib...)
	
	//Size of the blocks that are read from the network stream at a time.
	private int		blockSize;
	
	//Current amount of bytes read into memArea.
	private int		bytesRetrieved;
	
	//Currently hanlded pixels set.
	private Pixels	pixelsID;
	
	//Dimensions of the currently hanlded pixels set.
	private PixelsDimensions pixDims;
	
	//Size of a wavelength stack within the currently handled pixels set.
	private int		waveStackSize;
	
	//Size of a XY plane within the currently handled pixels set.
	private int		planeSize;
	
	//Allows to stop the filling loop.
	private boolean shouldFill;
	
	
	//Makes sure that the arguments are within the right bounds.
	private void checkBounds(int z, int w, int t)
	{
		if (z < 0 || pixDims.sizeZ <= z)
			throw new IllegalArgumentException(
				"Section index out of bounds [0, "+pixDims.sizeZ+"): "+z);
		if (w < 0 || pixDims.sizeW <= w)
			throw new IllegalArgumentException(
				"Wavelength index out of bounds [0, "+pixDims.sizeW+"): "+w);
		if (t < 0 || pixDims.sizeT <= t)
			throw new IllegalArgumentException(
				"Timepoint index out of bounds [0, "+pixDims.sizeT+"): "+t);
	}
	
	//Calculate plane offset in memArea.
	//Return -1 if plane data hasn't been retrieved yet.
	private int getPlaneOffset(int z, int w)
	{
		checkBounds(z, w, 0);
		int offset = w*waveStackSize + z*planeSize;
		return (offset+planeSize <= bytesRetrieved) ? offset : -1;
	}
	
	//Calculate wavelength stack offset in memArea.
	//Return -1 if wavelength stack data hasn't been retrieved yet.
	private int getStackOffset(int w)
	{
		checkBounds(0, w, 0);
		int offset = w*waveStackSize;
		return (offset+waveStackSize <= bytesRetrieved) ? offset : -1;
	}
	
	//Allocates the memory area and sets the block size.	
	StackFillerImpl(int memSize, int blockSize) 
	{
		this.memArea = new byte[memSize];
		this.blockSize = blockSize;
	}
	
	//Configure this object to work with the specified pixels set.
	//Invalidate if stack too big.
	public void configure(Pixels pixelsID, PixelsDimensions pixDims, 
							int bytesPerPixel, PixelsService source)
		throws DataSourceException
	{
		if (pixelsID == null) throw new NullPointerException("No pixelsID.");
		if (pixDims == null) throw new NullPointerException("No pixDims.");
		//TODO: check bytesPerPixel.
		omeis = (PixelsServiceAdapter) source;  //TODO: just tmp hack.
		
		//Calculate sizes and cache them.
		planeSize = pixDims.sizeX * pixDims.sizeY * bytesPerPixel;
		waveStackSize = planeSize * pixDims.sizeZ;
		
		//Make sure we can hanlde this stack.
		if (memArea.length < waveStackSize*pixDims.sizeW)
			throw new DataSourceException(
				"The stack exceeds the memory capability.");
		
		this.pixDims = pixDims;
		this.pixelsID = pixelsID;
	}
	
	public void stop()
	{
		shouldFill = false;
	}
	
	public void fill(int t, boolean bigEndian)
		throws DataSourceException
	{
		checkBounds(0, 0, t);
		InputStream pixelStream = null;
		bytesRetrieved = 0;
		int w = 0, bytesRead = 0; 
		shouldFill = true;   	
		try {
			for (; w < pixDims.sizeW; ++w) {
				pixelStream = omeis.getStackStream(pixelsID, w, t, bigEndian);
				while (shouldFill && 0 < (bytesRead = 
						pixelStream.read(memArea, bytesRetrieved, blockSize))) 
							bytesRetrieved += bytesRead;
			}	
			if (bytesRetrieved != waveStackSize*pixDims.sizeW)  //Should never happen.
				throw new DataSourceException("The nominal size of the stack ("+
					(waveStackSize*pixDims.sizeW)+" bytes) at timepoint "+t+
					" differs from the actual size ("+bytesRetrieved+").");
		} catch (ImageServerException ise) {
			throw new DataSourceException(
				"Can't retrieve wavelength "+w+" stack at timepoint "+
				t+": ", ise);
		} catch (IOException ioe) {
			throw new DataSourceException(
				"Can't retrieve wavelength "+w+" stack at timepoint "+
				t+": ", ioe);
		} catch (ArrayIndexOutOfBoundsException aiobe) {  //Should never happen.
			throw new DataSourceException(
				"The size of the stack stream exceeds the nominal size of the"+
				"stack ("+(waveStackSize*pixDims.sizeW)+" bytes) at timepoint "+
				t+".");				
		} finally {
			try {
				//Release http connection.
				if (pixelStream != null) pixelStream.close();
			} catch (IOException ioe) {}
		}
	}
	
	public ReadOnlyByteArray getPlaneData(int z, int w)
	{
		int offset = getPlaneOffset(z, w);
		if (offset != -1)
			return new ReadOnlyByteArray(memArea, offset, planeSize);
		return null;
	}
	
	public ReadOnlyByteArray getStackData(int w)
	{
		checkBounds(0, w, 0);
		int offset = getStackOffset(w);
		if (offset != -1)
			return new ReadOnlyByteArray(memArea, offset, waveStackSize);
		return null;
	}
	
	public boolean isPlaneDataAvailable(int z, int w) 
	{
		return (getPlaneOffset(z, w) != -1);
	}

	public boolean isStackDataAvailable(int w) 
	{ 
		return (getStackOffset(w) != -1);
	}

}
