/*
 * org.openmicroscopy.shoola.util.concur.tasks.MockExecCommand
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
public class MockExecCommand
    extends ExecCommand
    implements IMock
{

    private static final MethodSignature cancel = 
        new MethodSignature(MethodSignature.PACKAGE, void.class, "cancel");
    private static final MethodSignature run = 
        new MethodSignature(MethodSignature.PUBLIC, void.class, "run");
    
    private MockSupport     mockSupport;
    
    
    MockExecCommand() 
    {
        super(new NullMultiStepTask(), new NullResultAssembler(), 
                new NullFuture(), new NullExecMonitor());
        mockSupport = new MockSupport();
    }
    
    //Used both in set up and verification mode 
    //(possible b/c of void return type).
    void cancel()
    {
        MockedCall mc = new MockedCall(cancel);
        if (mockSupport.isSetUpMode()) mockSupport.add(mc);
        else mockSupport.verifyCall(mc);
    }
    
    //Used both in set up and verification mode 
    //(possible b/c of void return type).
    public void run()
    {
        MockedCall mc = new MockedCall(run);
        if (mockSupport.isSetUpMode()) mockSupport.add(mc);
        else mockSupport.verifyCall(mc);
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
