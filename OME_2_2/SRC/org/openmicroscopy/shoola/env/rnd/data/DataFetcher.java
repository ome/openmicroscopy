/*
 * org.openmicroscopy.shoola.env.rnd.data.DataFetcher
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
import org.openmicroscopy.shoola.env.rnd.metadata.PixelsDimensions;
import org.openmicroscopy.shoola.util.concur.AsyncByteBuffer;
import org.openmicroscopy.shoola.util.concur.BufferWriteException;
import org.openmicroscopy.shoola.util.concur.tasks.CmdProcessor;
import org.openmicroscopy.shoola.util.concur.tasks.Future;
import org.openmicroscopy.shoola.util.mem.ReadOnlyByteArray;

/** 
 * Retrieves planes and stacks in behalf of its {@link DataSink}.
 * If a whole stack (all wavelengths) fits into the provided 
 * {@link #stackBuffer}, then this class will cache an entire stack.
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
class DataFetcher
{

    /** Used to access pixels-related information. */
    private DataSink         dataSink;
    
    /**
     * Caches an entire stack.
     * If the stack turns out to be too big for the buffer, then no data is
     * cached at all and this buffer is unused.
     */
	private AsyncByteBuffer stackBuffer;
	
    /** To perform asynchronous data retieval. */
	private CmdProcessor     cmdProcessor;
	
	/** Size of a wavelength stack within the pixels set. */
	private int		waveStackSize;
	
	/** Size of a XY plane within the pixels set. */
	private int		planeSize;
	
	/** The timepoint that identifies the current stack we want to retrieve. */
	private int		curT;
    
    /** Tells whether the stack can fit into the {@link #stackBuffer}. */
    private boolean canCacheStack;
	
	
	/**
     * Makes sure that the passed arguments are within the right bounds.
     * That is the sizes specified by the pixels set.
	 * 
     * @param z Index of the stack section.
	 * @param w Index of the wavelength.
	 * @param t Timepoint.
     * @throws IllegalArgumentException If any of the passed arguments is not 
     *                                  within the right bounds.
	 */
	private void checkBounds(int z, int w, int t)
	{
		PixelsDimensions pixDims = dataSink.getPixDims();
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
	
    /**
     * Calculates the plane offset within the whole stack.
     * 
     * @param z Index of the stack section.
     * @param w Index of the wavelength.
     * @return The plane offset.
     * @throws IllegalArgumentException If any of the passed arguments is not 
     *                                  within the right bounds.
     */
	private int getPlaneOffset(int z, int w)
	{
		checkBounds(z, w, 0);
		return w*waveStackSize + z*planeSize;
	}

    /**
     * Calculates the wavelength stack offset within the whole stack.
     * 
     * @param w Index of the wavelength.
     * @return The wavelength stack offset.
     * @throws IllegalArgumentException If any of the passed arguments is not 
     *                                  within the right bounds.
     */
	private int getStackOffset(int w)
	{
		checkBounds(0, w, 0);
		return w*waveStackSize;
	}
	
    /**
     * Sets {@link #curT} to <code>t</code> and starts an asynchronous stack 
     * fetch if it fits into {@link #stackBuffer}.
     * 
     * @param t The new timepoint.
     * @throws DataSourceException If the calling thread is interrupted.
     */
    private void setCurT(int t) 
        throws DataSourceException
    {
        if (t == curT) return;
        curT = t;
        StackFiller producer = null;
        if (canCacheStack) {
            PixelsDimensions d = dataSink.getPixDims();
            producer = new StackFiller(dataSink.getSource(),
                    dataSink.getPixelsID(), d.sizeW, curT, waveStackSize,
                    DataSink.BIG_ENDIAN);
        }
        try {  
            //Start async stack fetch if producer != null.  In any case, get
            //rid of the previous producer (if any).  If we din't do this in
            //the case producer == null, then the next call to getPlaneData
            //or getStackData would be working with the wrong producer. 
            stackBuffer.setProducer(producer);  
        } catch (InterruptedException ie) {
            //Should never happen as we never interrupt Swing thread.
            throw new DataSourceException("Thread interrupted.", ie); 
        }
    }
    
    /**
     * Creates a new instance.
     * 
     * @param dataSink  Links back to the object this <code>DataFetcher</code>
     *                  is working for and which contains all pixels-related
     *                  information.
     * @param stackBuffer   The buffer to be used to cache a whole stack, if
     *                      possible.
     * @param cmdProcessor  To perform asynchronous data retieval.
     */
	DataFetcher(DataSink dataSink, AsyncByteBuffer stackBuffer, 
                CmdProcessor cmdProcessor)
	{
		if (dataSink == null) throw new NullPointerException("No data sink.");
        if (stackBuffer == null)
            throw new NullPointerException("No stack buffer.");
        if (cmdProcessor == null)
            throw new NullPointerException("No command processor.");
		
        this.dataSink = dataSink;
		this.stackBuffer = stackBuffer;
        this.cmdProcessor = cmdProcessor;
		this.curT = -1;
		
		//Calculate sizes and cache them.
        PixelsDimensions pixDims = dataSink.getPixDims();
		planeSize = pixDims.sizeX * pixDims.sizeY * dataSink.getBytesPerPixel();
		waveStackSize = planeSize * pixDims.sizeZ;
        
        canCacheStack = (pixDims.sizeW*waveStackSize <= stackBuffer.getSize());
	}
    
    /**
     * Tells whether an entire stack (all wavelengths) can be cached.
     * 
     * @return <code>true</code> if we can cache, <code>false</code> otherwise.
     */
    boolean canCacheStack() 
    {
        return canCacheStack;
    }
	
    /**
     * Retrieves the specified plane data within the pixels set.
     * If the data has been cached, this method returns immediately an
     * {@link ReadOnlyByteArray} object to access the plane data.  Otherwise,
     * it starts an asynchronous fetch and returns an 
     * {@link ReadOnlyByteArrayFuture} object which has to be used to get the
     * actual data when ready.
     * 
     * @param z Index of the stack section.
     * @param w Index of the wavelength.
     * @param t Timepoint.
     * @return Either a {@link ReadOnlyByteArray} object if the data is already 
     *          available or a {@link ReadOnlyByteArrayFuture} object if the
     *          data is going to be fetched asynchronously. 
     * @throws DataSourceException If the cached data couldn't be retrieved
     *                              because of an error.
     */
	public ReadOnlyByteArray getPlaneData(int z, int w, int t) 
		throws DataSourceException 
	{
		setCurT(t);  //Start async stack fetch if t != curT.
        int offset = getPlaneOffset(z, w);
        
        //See if data has been cached, but don't wait for data to be fetched.
		ReadOnlyByteArray data = null;
        try {
            data = stackBuffer.read(offset, planeSize, 0);  //0 for no wait.
        } catch (BufferWriteException bwe) {
            throw new DataSourceException("Can't retrieve pixels data.", bwe);
        } catch (InterruptedException ie) {
            //Should never happen as we never interrupt Swing thread.
            throw new DataSourceException("Thread interrupted.", ie); 
        }
        //if (data != null) return new ReadOnlyByteArrayFuture(data);
        if (data != null) return data;
        
        //Data is not available yet.  Do an asynchronous fetch.
        PlaneFetcher invocation = new PlaneFetcher(dataSink.getSource(),
                                dataSink.getPixelsID(), z, w, t, planeSize,
                                DataSink.BIG_ENDIAN);
        Future dataFuture = cmdProcessor.exec(invocation);
        return new ReadOnlyByteArrayFuture(dataFuture);
	}
	
    /**
     * Retrieves the specified wavelength stack data within the pixels set.
     * If the data has been cached, this method returns immediately an
     * {@link ReadOnlyByteArray} object to access the plane data.  Otherwise,
     * it waits until the data is asynchronously fetched and then returns it.
     * 
     * @param w Index of the wavelength.
     * @param t Timepoint.
     * @return A {@link ReadOnlyByteArray} object to access the stack data.
     * @throws DataSourceException If the cached data couldn't be retrieved
     *                              because of an error.
     */
	public ReadOnlyByteArray getStackData(int w, int t)
		throws DataSourceException
	{
        setCurT(t);  //Start async stack fetch if t != curT.
        int offset = getStackOffset(w);
        
        //Wait until stack data has been cached and then return it.
		ReadOnlyByteArray stack = null;
		try {
			stack = stackBuffer.read(offset, waveStackSize);  //Unbounded wait.
		} catch (BufferWriteException bwe) {
			throw new DataSourceException("Can't retrieve pixels data.", bwe);
		} catch (InterruptedException ie) {
			throw new DataSourceException("Thread interrupted.", ie);
		}
		return stack;
	}

}
