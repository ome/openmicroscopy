/*
 * org.openmicroscopy.shoola.env.rnd.data.PlaneFetcher
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
import org.openmicroscopy.is.ImageServerException;
import org.openmicroscopy.shoola.env.data.PixelsService;
import org.openmicroscopy.shoola.util.concur.tasks.Invocation;
import org.openmicroscopy.shoola.util.mem.ReadOnlyByteArray;

/** 
 * Retrieves a XY plane within a stack at a given wavelength and timepoint.
 * Implements the {@link Invocation} interface so that it can be run as a
 * service to retrieve data asynchronously.
 *
 * @see org.openmicroscopy.shoola.util.concur.tasks.Invocation
 * @see org.openmicroscopy.shoola.util.concur.tasks.CmdProcessor
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
class PlaneFetcher
    implements Invocation
{

    /** Proxy to <i>OMEIS</i>. */
    private final PixelsService    omeis;
    
    /** Identifies the pixels set. */
    private final Pixels  pixelsID;
    
    /** The section index within the wavelength stack. */
    private final int     z;
    
    /** The wavelength index. */
    private final int     w;
    
    /** The timepoint of the stack we want to retrieve. */
    private final int     t;
    
    /** Size of a wavelength plane within the pixels set. */
    private final int     planeSize;
    
    /** Endianness of the data we're going to retrieve. */
    private final boolean bigEndian;
    
    
    /**
     * Craetes a new instance to an retrieve XY plane within a given wavelength
     * stack.
     * 
     * @param source Proxy to <i>OMEIS</i>.  Mustn't be <code>null</code>.
     * @param pixelsID Identifies the pixels set.  Mustn't be <code>null</code>. 
     * @param z The index of the plane within the stack.
     * @param w The wavelength index.
     * @param t The timepoint of the stack we want to retrieve.
     * @param planeSize The size of a wavelength plane within the pixels set.
     * @param bigEndian Endianness of the data we're going to retrieve.
     */
    PlaneFetcher(PixelsService source, Pixels pixelsID, 
            int z, int w, int t, int planeSize, boolean bigEndian)
    {
        if (source == null) throw new NullPointerException("No source.");
        if (pixelsID == null) throw new NullPointerException("No pixelsID.");
        omeis = source;
        this.pixelsID = pixelsID;
        this.z = z;
        this.w = w;
        this.t = t;
        this.planeSize = planeSize;
        this.bigEndian = bigEndian;
    }
    
    /**
     * Fetches the plane data.
     * 
     * @return A byte array containing the plane data.
     * @throws DataSourceException If an error occurs during the retrieval
     *                              of the data. 
     */
    byte[] retrieveXYPlane()
        throws DataSourceException
    {
        byte[] buf = null;
        try {
            buf = omeis.getPlane(pixelsID, z, w, t, bigEndian);
            if (buf.length != planeSize)  //Should never happen.
                throw new DataSourceException("The nominal size of the plane ("+
                    planeSize+" bytes) at (z="+z+", w="+w+", t="+t+
                    ") differs from the actual size ("+buf.length+").");
        } catch (ImageServerException ise) {
            throw new DataSourceException("Can't retrieve pixels data.", ise);
        }
        return buf;
    }
    
    /**
     * Fetches the plane data and wraps it into a {@link ReadOnlyByteArray}.
     * 
     * @return A {@link ReadOnlyByteArray} object that wraps the actual plane
     *          data.
     * @throws DataSourceException If an error occurs during the retrieval
     *                              of the data. 
     * @see Invocation#call() 
     */
    public Object call() 
        throws Exception
    {
        byte[] buf = retrieveXYPlane();
        return new ReadOnlyByteArray(buf, 0, planeSize);
    }
    
}
