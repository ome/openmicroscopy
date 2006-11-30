/*
 * org.openmicroscopy.shoola.omeis.proxy.PixelsReaderProxy
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

package org.openmicroscopy.shoola.omeis.proxy;


//Java imports
import java.io.IOException;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.omeis.services.ImageServiceException;
import org.openmicroscopy.shoola.omeis.services.PixelsReader;
import org.openmicroscopy.shoola.omeis.transport.HttpChannel;
import org.openmicroscopy.shoola.util.mem.ByteArray;

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
public class PixelsReaderProxy
    extends AbstractProxy
    implements PixelsReader
{

    /**
     * @param sessionKey
     * @param channel
     */
    public PixelsReaderProxy(String sessionKey, HttpChannel channel)
    {
        super(sessionKey, channel);
    }

    /* (non-Javadoc)
     * @see PixelsReader#getPlane(long, int, int, int, boolean, ByteArray)
     */
    public void getPlane(long pixelsID, int theZ, int theC, int theT,
                            boolean bigEndian, ByteArray buf)
        throws ImageServiceException
    {
        GetPlaneRequest out = new GetPlaneRequest(sessionKey);
        out.setPixelsID(pixelsID);
        out.setTheZ(theZ);
        out.setTheC(theC);
        out.setTheT(theT);
        out.setBigEndian(bigEndian);
        
        GetPlaneReply in = new GetPlaneReply(buf);
        
        try {
            channel.exchange(out, in);
        } catch (IOException ioe) {
            throw new ImageServiceException(
                    "Couldn't communicate with OMEIS (I/O error).", ioe);
        }
    }

    /* (non-Javadoc)
     * @see PixelsReader#getStack(long, int, int, boolean, ByteArray)
     */
    public void getStack(long pixelsID, int theC, int theT, boolean bigEndian,
                            ByteArray buf)
        throws ImageServiceException
    {
        GetStackRequest out = new GetStackRequest(sessionKey);
        out.setPixelsID(pixelsID);
        out.setTheC(theC);
        out.setTheT(theT);
        out.setBigEndian(bigEndian);
        
        GetStackReply in = new GetStackReply(buf);
        
        try {
            channel.exchange(out, in);
        } catch (IOException ioe) {
            throw new ImageServiceException(
                    "Couldn't communicate with OMEIS (I/O error).", ioe);
        }
    }

}
