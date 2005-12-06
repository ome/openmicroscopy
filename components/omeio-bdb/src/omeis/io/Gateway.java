/*
 * omeis.io.Gateway
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

package omeis.io;


//Java imports
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//Third-party libraries

//Application-internal dependencies

/** 
 * Gateway to the native I/O subsystem that allows you to interface with the
 * <i>OMEIS</i> repository.
 * The methods in this class operate on a given repository file, which contains
 * both the 5D pixels array and some metadata about this pixels set that was
 * associated to an <i>OME</i> Image.  Note that an Image can be associated to
 * more than one pixels set, so the ids used by the methods in this class are
 * the ids by which <i>OMEIS</i> looks up a repository file &#151; that is, 
 * an <i>OMEIS</i> id is <i>not</i> the same as the id of the Image in which
 * the pixels set belongs. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: 1.6 $ $Date: 2005/06/17 16:17:19 $)
 * </small>
 * @since OME2.2
 */
public class Gateway
{

    private static Log log = LogFactory.getLog(Gateway.class);
    
    static {
        String oldPath = System.getProperty("java.library.path");
        System.setProperty("java.library.path","/Local/build/lib:/usr/lib:/lib:/lib/tls/i686/cmov:"+oldPath);
        String name = "gateway";
        System.loadLibrary(name);
    }
    
    /**
     * Tells whether a given pixels set exists in the repository.
     * 
     * @param pixelsID The id under which the pixels set is known to the server.
     * @return <code>true</code> if the pixels set exists; <code>false</code>
     *         otherwise.
     * @throws IOException If an I/O error occurs while accessing the 
     *                     repository.
     */
    public static native boolean exists(long pixelsID) throws IOException;
    
    /**
     * Retrieves an <i>XY</i> plane from the pixels set pointed by 
     * <code>pixelsID</code>.
     * The 5D array is assumed to be kept in the <i>XYZCT</i> format and the
     * returned data to be in <i>big</i> endian order.
     * 
     * @param pixelsID The id under which the pixels set is known to the server.
     * @param z The index of the stack in which the plane belongs.
     * @param c The index of the wavelength (channel) at which to fetch the 
     *          plane.
     * @param t The timepoint at which to fetch the plane.
     * @return The plane data.  Note that the caller is responsible to 
     *         interpret the data correctly &#151; INT8, UINT8, INT16, etc.
     * @throws IOException If an I/O error occurs while accessing the repository
     *                     file containing the specified pixels set.
     * @throws IllegalArgumentException If <code>pixelsID</code> is not a valid
     *                     id or if any of the <code>z, w, t</code> coords are
     *                     out of range. 
     */
    public static native byte[] getPlane(long pixelsID, int z, int c, int t)
        throws IOException;
    
//    /**
//     * Retrieves the metadata associated to the pixels set pointed by 
//     * <code>pixelsID</code>.
//     * 
//     * @param pixelsID The id under which the pixels set is known to the server.
//     * @return An object holding the metadata extracted from the repository
//     *         file containing the specified pixels set.
//     * @throws IOException If an I/O error occurs while accessing the repository
//     *                     file containing the specified pixels set.
//     */
//    public static native PixelsHeader getPixelsHeader(long pixelsID)
//        throws IOException;
//    
//    /**
//     * Builds an object that contains some statistics for each stack in the
//     * specified pixels set.
//     * 
//     * @param pixelsID The id under which the pixels set is known to the server.
//     * @return An object containing the stats.
//     * @throws IOException If an I/O error occurs while accessing the repository
//     *                     file containing the specified pixels set.
//     * @throws IllegalArgumentException If <code>pixelsID</code> is not a valid
//     *                     id.
//     */
//    public static native StackStatistics getStackStatistics(long pixelsID)
//        throws IOException;
//    
//    /**
//     * Tells the endianness of the platform.
//     * 
//     * @return Returns <code>true</code> if big endian, <code>false</code> 
//     *         if little endian.
//     */
//    public static native boolean isBigEndian();
//    
}
