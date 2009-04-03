/*
 * org.openmicroscopy.shoola.env.rnd.data.DataSource
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
import org.openmicroscopy.shoola.omeis.services.PixelsReader;
import org.openmicroscopy.shoola.util.concur.tasks.CmdProcessor;
import org.openmicroscopy.shoola.util.concur.tasks.Future;
import org.openmicroscopy.shoola.util.mem.ReadOnlyByteArray;

/** 
 * Retrieves planes and stacks of a given pixels set.
 * Data retrieval happens asynchronously, so the <code>getXXXData</code>
 * methods returns immediately and the caller can decide when to block
 * to wait for data.
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
public class DataSource
{
    
    /** 
     * The id, under <i>OMEIS</i>, of the pixels set this 
     * <code>DataSource</code> is for. 
     */
    private final long              omeisPixelsID;
    
    /** The 5D dimesions of the pixels set. */
    private final PixelsDimensions  pixDims;
    
    /** 
     * The type used to store pixel values.  
     * One of the constants defined by {@link DataSink}.
     */
    private final int               pixelType;
    
    /** Tells how many bytes are used to store a pixel. */
    private final int               bytesPerPixel;
    
    /** Size of a wavelength stack within the pixels set. */
    private final int               waveStackSize;
    
    /** Size of a XY plane within the pixels set. */
    private final int               planeSize;
    
    /** Proxy to <i>OMEIS</i>. */
    private final PixelsReader      omeis;
    
    /** To perform asynchronous data retieval. */
    private final CmdProcessor      cmdProcessor;
    
    
    /**
     * Helper method to check that the passed arguments are within the right
     * bounds.
     * That is, they're within the sizes specified by {@link #pixDims}.
     * 
     * @param z Index of the stack section.
     * @param w Index of the wavelength.
     * @param t Timepoint.
     * @throws IllegalArgumentException If any of the passed arguments is not 
     *                                  within the right bounds.
     */
    private void checkBounds(int z, int w, int t)
    {
        if (z < 0 || pixDims.sizeZ <= z)
            throw new IllegalArgumentException(
                "Section index out of bounds [0, "+pixDims.sizeZ+"): "+z+".");
        if (w < 0 || pixDims.sizeW <= w)
            throw new IllegalArgumentException(
            "Wavelength index out of bounds [0, "+pixDims.sizeW+"): "+w+".");
        if (t < 0 || pixDims.sizeT <= t)
            throw new IllegalArgumentException(
                "Timepoint index out of bounds [0, "+pixDims.sizeT+"): "+t+".");
    }
    
    /**
     * Creates a new instance.
     * 
     * @param omeisPixelsID Identifies a pixels set under <i>OMEIS</i>.
     * @param pixDims       The dimensions of said pixels set.
     *                      Mustn't be <code>null</code>.
     * @param pixelType     The type used to store pixel values.  Must be one
     *                      of the constants defined by {@link DataSink}.
     * @param omeis         Proxy to <i>OMEIS</i>.  
     *                      Mustn't be <code>null</code>.
     * @param cmdProcessor  To perform asynchronous data retrieval.
     *                      Mustn't be <code>null</code>.
     */
    DataSource(long omeisPixelsID, PixelsDimensions pixDims,
                        int pixelType, PixelsReader omeis, 
                        CmdProcessor cmdProcessor)
    {
        if (pixDims == null)
            throw new NullPointerException("No pixels dimensions.");
        if (omeis == null)
            throw new NullPointerException("No proxy to OMEIS.");
        if (cmdProcessor == null)
            throw new NullPointerException("No command processor.");
        this.omeisPixelsID = omeisPixelsID;
        this.pixDims = pixDims;
        this.omeis = omeis;
        this.cmdProcessor = cmdProcessor;
        
        //Check the pixel type and find out how many bytes per pixels.
        bytesPerPixel = DataSink.getBytesPerPixel(pixelType);
        this.pixelType = pixelType;
        
        //Calculate sizes and cache them.
        planeSize = pixDims.sizeX * pixDims.sizeY * bytesPerPixel;
        waveStackSize = planeSize * pixDims.sizeZ;
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
    ReadOnlyByteArray getPlaneData(int z, int w, int t) 
        throws DataSourceException 
    {   
        //First some sanity checks.
        checkBounds(z, w, t);
        
        //See if data has been cached, but don't wait for data to be fetched.
        //(Just a reminder, NOT IMPLEMENTED YET.)
        
        //Data is not available yet.  Do an asynchronous fetch.
        PlaneFetcher invocation = new PlaneFetcher(omeis, omeisPixelsID, 
                                                    z, w, t, planeSize);
        Future dataFuture = cmdProcessor.exec(invocation);
        return new ReadOnlyByteArrayFuture(dataFuture);
    }
    
    /**
     * <b>NOT IMPLEMENTED YET.<b>
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
    ReadOnlyByteArray getStackData(int w, int t)
        throws DataSourceException
    {
        throw new DataSourceException(
                "Stack data retrieval capabilities not available.");
    }
    
    /**
     * Returns the dimensions of the pixels set this object is working for.
     * 
     * @return See above.
     */
    PixelsDimensions getPixDims() { return pixDims; }
    
    /** 
     * Returns the type used to store pixel values within the pixels set this 
     * object is working for.  
     * 
     * @return One of the constants defined by {@link DataSink}.
     */
    int getPixelType() { return pixelType; }
    
    /** 
     * Tells how many bytes are used to store a pixel within the pixels set
     * this object is working for.
     * 
     * @return See above. 
     */
    int getBytesPerPixel() { return bytesPerPixel; }
    
}
