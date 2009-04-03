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
import org.openmicroscopy.shoola.omeis.services.ImageServiceException;
import org.openmicroscopy.shoola.omeis.services.PixelsReader;
import org.openmicroscopy.shoola.util.concur.tasks.Invocation;
import org.openmicroscopy.shoola.util.mem.ByteArray;
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
    private final PixelsReader omeis;
    
    /** Identifies the pixels set under <i>OMEIS</i>. */
    private final long          pixelsID;
    
    /** The section index within the wavelength stack. */
    private final int           z;
    
    /** The wavelength index. */
    private final int           w;
    
    /** The timepoint of the stack we want to retrieve. */
    private final int           t;
    
    /** Size of a wavelength plane within the pixels set. */
    private final int           planeSize;
    
    
    
    /**
     * Craetes a new instance to an retrieve XY plane within a given wavelength
     * stack.
     * 
     * @param omeis     Proxy to <i>OMEIS</i>.  Mustn't be <code>null</code>.
     * @param pixelsID  Identifies the pixels set under <i>OMEIS</i>. 
     * @param z         The index of the plane within the stack.
     * @param w         The wavelength index.
     * @param t         The timepoint of the stack we want to retrieve.
     * @param planeSize The size of a wavelength plane within the pixels set.
     */
    PlaneFetcher(PixelsReader omeis, long pixelsID, 
                    int z, int w, int t, int planeSize)
    {
        if (omeis == null) throw new NullPointerException("No proxy to OMEIS.");
        this.omeis = omeis;
        this.pixelsID = pixelsID;
        this.z = z;
        this.w = w;
        this.t = t;
        this.planeSize = planeSize;
    }
    
    /**
     * Fetches the plane data.
     * 
     * @return A byte array containing the plane data.
     * @throws DataSourceException If an error occurs during the retrieval
     *                              of the data. 
     */
    ReadOnlyByteArray retrieveXYPlane()
        throws DataSourceException
    {
        
        byte[] base = new byte[planeSize];
        ByteArray buffer = new ByteArray(base, 0, base.length);
        try {
            omeis.getPlane(pixelsID, z, w, t, DataSink.BIG_ENDIAN, buffer);
        } catch (ImageServiceException ise) {
            throw new DataSourceException("Can't retrieve pixels data.", ise);
        }
        return buffer;
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
        return retrieveXYPlane();
    }
    
}
