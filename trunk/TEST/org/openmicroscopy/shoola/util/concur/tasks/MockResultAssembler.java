/*
 * org.openmicroscopy.shoola.util.concur.tasks.MockResultAssembler
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

package org.openmicroscopy.shoola.util.concur.tasks;


//Java imports

//Third-party libraries

//Application-internal dependencies
import util.mocks.IMock;
import util.mocks.MethodSignature;
import util.mocks.MockSupport;
import util.mocks.MockedCall;

/** 
 * Mock object.
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
public class MockResultAssembler
    implements IMock, ResultAssembler
{

    private static final MethodSignature add = 
        new MethodSignature(MethodSignature.PUBLIC, void.class, "add", 
                new Class[] {Object.class});
    private static final MethodSignature assemble = 
        new MethodSignature(MethodSignature.PUBLIC, Object.class, "assemble");
    
    private MockSupport     mockSupport;
    
    
    MockResultAssembler()
    {
        mockSupport = new MockSupport();
    }
    
    //Used in set up mode.  If re != null, then it will be
    //thrown in verification mode.
    public void add(Object partialResult, RuntimeException re)
    {
        MockedCall mc = new MockedCall(add, new Object[] {partialResult});
        if (re != null) mc.setException(re);
        mockSupport.add(mc);
    }
    
    //Used in verification mode.
    public void add(Object partialResult)
    {
        MockedCall mc = new MockedCall(add, new Object[] {partialResult});
        mc = mockSupport.verifyCall(mc);
        if (mc.hasException())
            throw (RuntimeException) mc.getException();
    }

    //Used in set up mode.  If re != null, then it will be
    //thrown in verification mode.
    public void assemble(Object retVal, RuntimeException re)
    {
        MockedCall mc = new MockedCall(assemble, retVal);
        if (re != null) mc.setException(re);
        mockSupport.add(mc);
    }
    
    //Used in verification mode.
    public Object assemble()
    {
        MockedCall mc = new MockedCall(assemble, (Object) null);
        mc = mockSupport.verifyCall(mc);
        if (mc.hasException())
            throw (RuntimeException) mc.getException();
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
