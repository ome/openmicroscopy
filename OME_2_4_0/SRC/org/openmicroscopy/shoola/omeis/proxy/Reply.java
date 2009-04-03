/*
 * org.openmicroscopy.shoola.omeis.proxy.Reply
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
import java.io.InputStream;

//Third-party libraries
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;

//Application-internal dependencies
import org.openmicroscopy.shoola.omeis.services.ImageServiceException;
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
public abstract class Reply
{

    protected static void checkStatusCode(HttpMethod response)
        throws ImageServiceException
    {
        int status = response.getStatusCode();
        if (status != HttpStatus.SC_OK)
            throw new ImageServiceException("OMEIS couldn't handle request: "+
                        HttpStatus.getStatusText(status)+".");
    }
    
    protected static void checkContentType(HttpMethod response, String mimeType)
        throws ImageServiceException 
    {
        Header header = response.getResponseHeader("Content-Type");
        if (header == null)
            throw new ImageServiceException(
                    "Unspecified content type (OMEIS internal error).");
        String value = header.getValue();
        if (!mimeType.equals(value)) {
            throw new ImageServiceException(
                    "Wrong mime type: ["+value+ "] (OMEIS internal error).");
        }
    }
    
    protected static void readResponseStream(InputStream in, ByteArray buffer, 
                                                int blockSize)
        throws ImageServiceException, IOException
    {
        int bytesWritten = 0, writeLength = 0;
        try { 
            //Read the strem data into the buffer.
            do {
                writeLength = buffer.set(bytesWritten, blockSize, in);
                bytesWritten += (writeLength < 0 ? 0 : writeLength);
            } while (writeLength != -1 && bytesWritten < buffer.length);
            
            //Make sure we actually reached the end of the stream.
            writeLength = in.read();
            if (writeLength == -1) {
                //Check for underflow.  Possible, for example, if OMEIS crashed
                //while streaming the data or returned the wrong data (this
                //latter one should never happen).
                if (bytesWritten < buffer.length)
                    throw new ImageServiceException(
                            "Stream underflow: expected ["+buffer.length+"], "+
                            "actual ["+bytesWritten+"].");
                //Else bytesWritten == buffer.length, otherwise an
                //ArrayIndexOutOfBoundsException would have been
                //thrown.
            } else {  
                //We have bytesWritten == buffer.length (same as above). 
                //Nonetheless, there's more data in the stream.  Overflow,
                //perhaps OMEIS returned the wrong data (should never happen).
                throw new ImageServiceException(
                        "Stream overflow: expected ["+buffer.length+"].");
            }
        } catch (ArrayIndexOutOfBoundsException aiobe) {
            //Overflow, argument similar to the above applies.
            throw new ImageServiceException(
                    "Stream overflow: expected ["+buffer.length+"].");
        } finally {
            try {
                in.close();  //Required by Http Client.
            } catch (IOException ioe) {}
        }
    }
    
    public abstract void unmarshal(HttpMethod response, HttpChannel context)
        throws ImageServiceException, IOException;
    
}
