/*
 * org.openmicroscopy.shoola.util.concur.tasks.MockMultiStepTask
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
public class MockMultiStepTask
    implements IMock, MultiStepTask
{

    private static final MethodSignature doStep = 
        new MethodSignature(MethodSignature.PUBLIC, Object.class, "doStep");
    private static final MethodSignature isDone = 
        new MethodSignature(MethodSignature.PUBLIC, Boolean.class, "isDone");
    
    private MockSupport     mockSupport;
    
    
    MockMultiStepTask()
    {
        mockSupport = new MockSupport();
    }
    
    //Used in set up mode.  If e != null, then it will be
    //thrown in verification mode.
    public void doStep(Object retVal, Exception e)
    {
        MockedCall mc = new MockedCall(doStep, retVal);
        if (e != null) mc.setException(e);
        mockSupport.add(mc);
    }
    
    //Used in verification mode.
    public Object doStep()
        throws Exception
    {
        MockedCall mc = new MockedCall(doStep, (Object) null);
        mc = mockSupport.verifyCall(mc);
        if (mc.hasException())
            throw (Exception) mc.getException();
        return mc.getResult();
    }


    //Used in set up mode.  If re != null, then it will be
    //thrown in verification mode.
    public void isDone(boolean retVal, RuntimeException re)
    {
        MockedCall mc = new MockedCall(isDone, new Boolean(retVal));
        if (re != null) mc.setException(re);
        mockSupport.add(mc);
    }
    
    //Used in verification mode.
    public boolean isDone()
    {
        MockedCall mc = new MockedCall(isDone, (Boolean) null);
        mc = mockSupport.verifyCall(mc);
        if (mc.hasException())
            throw (RuntimeException) mc.getException();
        Boolean r = (Boolean) mc.getResult();
        return r.booleanValue();
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
