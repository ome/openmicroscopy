/*
 * org.openmicroscopy.shoola.util.mem.SimpleHandle
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

package org.openmicroscopy.shoola.util.mem;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Supports unit tests for the {@link Handle} class.
 * Takes on the Handle role, the Body is {@link MockBody}.
 * Also, clearly exemplifies how to write a well behaved Handle. 
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
class SimpleHandle
    extends Handle
{
    
    SimpleHandle()
    {
        super(new MockBody());  //Always create a new Body object.
        
        //Hold no state, as our state is hold by the Body. 
    }
    
    //WARNING:  A well behaved Handle mustn't leak out a reference to its
    //Body.  The only purpose of this method is to allow test cases to set
    //expectations on the Mock object.
    MockBody getInitialBody() 
    {
        return (MockBody) getBody();
    }
    
    //Replicate Body's class interface to forward calls.
    
    public void readState()
    {
        //This method only read the Body's state, just forward the call.
        MockBody body = (MockBody) getBody();
        body.readState();
        
        //Now just discard the reference to the Body.  No caching, no leakage.  
    }
    
    public void writeState()
    {
        //This method writes the Body's state, we must notify Handle first.
        breakSharing();
        
        //Then we can forward the call.
        MockBody body = (MockBody) getBody();
        body.writeState();
        
        //Now just discard the reference to the Body.  No caching, no leakage.
    }
    
}
