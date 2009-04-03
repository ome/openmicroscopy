/*
 * org.openmicroscopy.shoola.util.concur.tasks.MockExecMonitor
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
public class MockExecMonitor
    implements IMock, ExecMonitor
{

    private static final MethodSignature onStart = 
        new MethodSignature(MethodSignature.PUBLIC, void.class, "onStart");
    private static final MethodSignature update = 
        new MethodSignature(MethodSignature.PUBLIC, void.class, "update",
                new Class[] {int.class});
    private static final MethodSignature onCancel = 
        new MethodSignature(MethodSignature.PUBLIC, void.class, "onCancel");
    private static final MethodSignature onAbort = 
        new MethodSignature(MethodSignature.PUBLIC, void.class, "onAbort",
                new Class[] {Throwable.class});
    private static final MethodSignature onEnd = 
        new MethodSignature(MethodSignature.PUBLIC, void.class, "onEnd",
                new Class[] {Object.class});
    
    
    private MockSupport     mockSupport;
    
    
    MockExecMonitor()
    {
        mockSupport = new MockSupport();
    }
    
    //Used in set up mode.  If re != null, then it will be
    //thrown in verification mode.
    public void onStart(RuntimeException re)
    {
        MockedCall mc = new MockedCall(onStart);
        if (re != null) mc.setException(re);
        mockSupport.add(mc);
    }
    
    //Used in verification mode.
    public void onStart()
    {
        MockedCall mc = new MockedCall(onStart);
        mc = mockSupport.verifyCall(mc);
        if (mc.hasException())
            throw (RuntimeException) mc.getException();
    }

    //Used in set up mode.  If re != null, then it will be
    //thrown in verification mode.
    public void update(int step, RuntimeException re)
    {
        Integer stepArg = new Integer(step);
        MockedCall mc = new MockedCall(update, new Object[] {stepArg});
        if (re != null) mc.setException(re);
        mockSupport.add(mc);
    }
 
    //Used in verification mode.
    public void update(int step)
    {
        Integer stepArg = new Integer(step);
        MockedCall mc = new MockedCall(update, new Object[] {stepArg});
        mc = mockSupport.verifyCall(mc);
        if (mc.hasException())
            throw (RuntimeException) mc.getException();
    }

    //Used in set up mode.  If re != null, then it will be
    //thrown in verification mode.
    public void onCancel(RuntimeException re)
    {
        MockedCall mc = new MockedCall(onCancel);
        if (re != null) mc.setException(re);
        mockSupport.add(mc);
    }
    
    //Used in verification mode.
    public void onCancel()
    {
        MockedCall mc = new MockedCall(onCancel);
        mc = mockSupport.verifyCall(mc);
        if (mc.hasException())
            throw (RuntimeException) mc.getException();
    }

    //Used in set up mode.  If re != null, then it will be
    //thrown in verification mode.
    public void onAbort(Throwable cause, RuntimeException re)
    {
        MockedCall mc = new MockedCall(onAbort, new Object[] {cause});
        if (re != null) mc.setException(re);
        mockSupport.add(mc);
    }

    //Used in verification mode.
    public void onAbort(Throwable cause)
    {
        MockedCall mc = new MockedCall(onAbort, new Object[] {cause});
        mc = mockSupport.verifyCall(mc);
        if (mc.hasException())
            throw (RuntimeException) mc.getException();
    }
    
    //Used in set up mode.  If re != null, then it will be
    //thrown in verification mode.
    public void onEnd(Object result, RuntimeException re)
    {
        MockedCall mc = new MockedCall(onEnd, new Object[] {result});
        if (re != null) mc.setException(re);
        mockSupport.add(mc);
    }
    
    //Used in verification mode.
    public void onEnd(Object result)
    {
        MockedCall mc = new MockedCall(onEnd, new Object[] {result});
        mc = mockSupport.verifyCall(mc);
        if (mc.hasException())
            throw (RuntimeException) mc.getException();
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
