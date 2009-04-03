/*
 * org.openmicroscopy.shoola.util.concur.tasks.MockFuture
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
public class MockFuture
    extends Future
    implements IMock
{
    
    private static final MethodSignature getResult = 
        new MethodSignature(MethodSignature.PUBLIC, Object.class, "getResult");
    private static final MethodSignature getResultLong = 
        new MethodSignature(MethodSignature.PUBLIC, Object.class, "getResult",
                new Class[] {long.class});
    private static final MethodSignature cancelExecution = 
        new MethodSignature(MethodSignature.PUBLIC, void.class, 
                "cancelExecution");
    
    
    private MockSupport     mockSupport;
    
    
    public MockFuture()
    {
        mockSupport = new MockSupport();
    }
    
    //Used in set up mode.  If e != null then it'll be wrapped by an 
    //ExecException and thrown in verification mode. 
    public void getResult(Object retVal, Exception e)
    {
        MockedCall mc = new MockedCall(getResult, retVal);
        if (e != null) mc.setException(new ExecException(e));
        mockSupport.add(mc);
    }
    
    //Used in verification mode.
    public Object getResult()
        throws ExecException
    {
        MockedCall mc = new MockedCall(getResult, new Object());
        mc = mockSupport.verifyCall(mc);
        if (mc.hasException()) {
            ExecException ee = (ExecException) mc.getException();
            throw ee;
        }
        return mc.getResult();
    }
    
    //Used in set up mode.  If e != null then it'll be wrapped by an 
    //ExecException and thrown in verification mode. 
    public void getResult(long timeout, Object retVal, Exception e)
    {
        MockedCall mc = new MockedCall(getResultLong,
                                    new Object[] {new Long(timeout)},
                                    retVal);
        if (e != null) mc.setException(new ExecException(e));
        mockSupport.add(mc);
    }
    
    //Used in verification mode.
    public Object getResult(long timeout)
        throws ExecException
    {
        MockedCall mc = new MockedCall(getResultLong, 
                                        new Object[] {new Long(timeout)},
                                        null);
        mc = mockSupport.verifyCall(mc);
        if (mc.hasException()) {
            ExecException ee = (ExecException) mc.getException();
            throw ee;
        }
        return mc.getResult();
    }
    
    //Used both in set up and verification mode.
    public void cancelExecution()
    {
        MockedCall mc = new MockedCall(cancelExecution);
        if (mockSupport.isSetUpMode()) mockSupport.add(mc);
        if (mockSupport.isVerificationMode()) mockSupport.verifyCall(mc);
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
