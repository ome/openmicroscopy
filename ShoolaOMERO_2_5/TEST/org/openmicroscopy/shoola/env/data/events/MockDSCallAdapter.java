/*
 * org.openmicroscopy.shoola.env.data.events.MockDSCallAdapter
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

package org.openmicroscopy.shoola.env.data.events;


//Java imports

//Third-party libraries

//Application-internal dependencies
import util.mocks.IMock;
import util.mocks.MethodSignature;
import util.mocks.MockSupport;
import util.mocks.MockedCall;

/** 
 * Mock object for {@link DSCallAdapter}.
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
public class MockDSCallAdapter
    extends DSCallAdapter
    implements IMock
{

    private static final MethodSignature handleCancellation = 
        new MethodSignature(MethodSignature.PUBLIC, void.class, 
                "handleCancellation");
    
    private static final MethodSignature handleException = 
        new MethodSignature(MethodSignature.PUBLIC, void.class, 
                "handleException", new Class[] {Throwable.class});
    
    private static final MethodSignature handleNullResult = 
        new MethodSignature(MethodSignature.PUBLIC, void.class, 
                "handleNullResult");
    
    private static final MethodSignature handleResult = 
        new MethodSignature(MethodSignature.PUBLIC, void.class, 
                "handleResult", new Class[] {Object.class});
    
    private static final MethodSignature onEnd = 
        new MethodSignature(MethodSignature.PUBLIC, void.class, "onEnd");
    
    private static final MethodSignature update = 
        new MethodSignature(MethodSignature.PUBLIC, void.class, 
                "update", new Class[] {DSCallFeedbackEvent.class});
    
    
    private MockSupport     mockSupport;
    
    
    public MockDSCallAdapter()
    {
        mockSupport = new MockSupport();
    }
    
    
    //Used both in set up and verification mode.
    public void update(DSCallFeedbackEvent progress) 
    {
        MockedCall mc = new MockedCall(update, new Object[] {progress});
        if (mockSupport.isSetUpMode()) mockSupport.add(mc);
        else mc = mockSupport.verifyCall(mc);
    }

    //Used both in set up and verification mode.
    public void onEnd() 
    {
        MockedCall mc = new MockedCall(onEnd);
        if (mockSupport.isSetUpMode()) mockSupport.add(mc);
        else mc = mockSupport.verifyCall(mc);
    }
    
    //Used both in set up and verification mode.
    public void handleResult(Object result) 
    {
        MockedCall mc = new MockedCall(handleResult, new Object[] {result});
        if (mockSupport.isSetUpMode()) mockSupport.add(mc);
        else mc = mockSupport.verifyCall(mc);
    }
    
    //Used both in set up and verification mode.
    public void handleNullResult() 
    {
        MockedCall mc = new MockedCall(handleNullResult);
        if (mockSupport.isSetUpMode()) mockSupport.add(mc);
        else mc = mockSupport.verifyCall(mc);
    }
    
    //Used both in set up and verification mode.
    public void handleCancellation() 
    {
        MockedCall mc = new MockedCall(handleCancellation);
        if (mockSupport.isSetUpMode()) mockSupport.add(mc);
        else mc = mockSupport.verifyCall(mc);
    }
    
    //Used both in set up and verification mode.
    public void handleException(Throwable exc) 
    {
        MockedCall mc = new MockedCall(handleException, new Object[] {exc});
        if (mockSupport.isSetUpMode()) mockSupport.add(mc);
        else mc = mockSupport.verifyCall(mc);
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
