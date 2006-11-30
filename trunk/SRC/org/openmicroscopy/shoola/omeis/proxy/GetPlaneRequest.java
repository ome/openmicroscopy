/*
 * org.openmicroscopy.shoola.omeis.proxy.GetPlaneRequest
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
class GetPlaneRequest
    extends GetStackRequest
{
    
    private static final String   THE_Z_FIELD = "theZ";
    
    private static final String   METHOD = "GetPlane";
    
    
    private int     theZ;
    
    /**
     * 
     * @param sessionKey
     */
    GetPlaneRequest(String sessionKey)
    {
        super(sessionKey, METHOD);
    }

    /* (non-Javadoc)
     * @see org.openmicroscopy.shoola.omeis.proxy.Request#marshal()
     */
    public HttpMethod marshal()
    {
        PostMethod request = (PostMethod) super.marshal();
        request.addParameter(THE_Z_FIELD, Integer.toString(theZ));
        return request;
    }
    
    int getTheZ() { return theZ; }
    
    void setTheZ(int theZ) { this.theZ = theZ; }

}
