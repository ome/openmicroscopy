/*
 * org.openmicroscopy.shoola.omeis.proxy.GetStackRequest
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

//Third-party libraries
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;

//Application-internal dependencies

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
class GetStackRequest
    extends Request
{
    
    private static final String   PIXELS_ID_FIELD = "PixelsID";
    private static final String   THE_C_FIELD = "theC";
    private static final String   THE_T_FIELD = "theT";
    private static final String   BIG_ENDIAN_FIELD = "BigEndian";
    
    private static final String   METHOD = "GetStack";
    
    
    private long    pixelsID;
    private int     theC;
    private int     theT;
    private int     bigEndian;
    
    
    protected GetStackRequest(String sessionKey, String method)
    {
        super(sessionKey, method);
    }
    
    /**
     * 
     * @param sessionKey
     */
    GetStackRequest(String sessionKey)
    {
        super(sessionKey, METHOD);
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.omeis.proxy.Request#marshal()
     */
    public HttpMethod marshal()
    {
        //Create request.
        PostMethod request = new PostMethod();
        
        //Marshal.
        request.addParameter(SESSION_KEY_FIELD, sessionKey);
        request.addParameter(METHOD_FIELD, method);
        request.addParameter(PIXELS_ID_FIELD, Long.toString(pixelsID));
        request.addParameter(THE_C_FIELD, Integer.toString(theC));
        request.addParameter(THE_T_FIELD, Integer.toString(theT));
        request.addParameter(BIG_ENDIAN_FIELD, Integer.toString(bigEndian));
        
        return request;
    }
    
    boolean getBigEndian() { return (bigEndian == 1); }
    
    void setBigEndian(boolean bigEndian) 
    { 
        this.bigEndian = (bigEndian ? 1 : 0);
    }
    
    long getPixelsID() { return pixelsID; }
    
    void setPixelsID(long pixelsID) { this.pixelsID = pixelsID; }
    
    int getTheC() { return theC; }
    
    void setTheC(int theC) { this.theC = theC; }
    
    int getTheT() { return theT; }
    
    void setTheT(int theT) { this.theT = theT; }

}
