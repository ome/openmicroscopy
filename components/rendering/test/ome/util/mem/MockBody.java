/*
 * ome.util.mem.MockBody
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

package ome.util.mem;


//Java imports

//Third-party libraries

//Application-internal dependencies
import util.mocks.IMock;
import util.mocks.MethodSignature;
import util.mocks.MockSupport;
import util.mocks.MockedCall;

/** 
 * Mock object to simulate a Body object to attach to a {@link Handle}.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: 1.1 $ $Date: 2005/06/25 18:09:08 $)
 * </small>
 * @since OME2.2
 */
class MockBody
    implements Copiable, IMock
{
    
    private static final MethodSignature readState = 
        new MethodSignature(MethodSignature.PUBLIC, void.class, "readState");
    private static final MethodSignature writeState = 
        new MethodSignature(MethodSignature.PUBLIC, void.class, "writeState");
    private static final MethodSignature copy = 
        new MethodSignature(MethodSignature.PUBLIC, Object.class, "copy");
    
    
    private MockSupport     mockSupport;
    
    
    MockBody()
    {
        mockSupport = new MockSupport();
    }
    
    //Used both in set up and verification mode.
    public void readState()
    {
        MockedCall mc = new MockedCall(readState);
        if (mockSupport.isSetUpMode())
            mockSupport.add(mc);
        else
            mockSupport.verifyCall(mc);
    }
    
    //Used both in set up and verification mode.
    public void writeState()
    {
        MockedCall mc = new MockedCall(writeState);
        if (mockSupport.isSetUpMode())
            mockSupport.add(mc);
        else
            mockSupport.verifyCall(mc);
    }
    
    //Used in set up mode.
    public void copy(Object retVal)
    {
        MockedCall mc = new MockedCall(copy, retVal);
        mockSupport.add(mc);
    }
    
    //Used in verification mode.
    public Object copy()
    {
        MockedCall mc = new MockedCall(copy, (Object) null);
        mc = mockSupport.verifyCall(mc);
        return mc.getResult();
    }
    
    /**
     * @see util.mocks.IMock#activate()
     */
    public void activate()
    {
        mockSupport.activate(); 
    }
    
    /**
     * @see util.mocks.IMock#verify()
     */
    public void verify()
    {
        mockSupport.verifyCallSequence();
    }
    
}
