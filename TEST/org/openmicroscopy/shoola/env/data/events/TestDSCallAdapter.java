/*
 * org.openmicroscopy.shoola.env.data.events.TestDSCallAdapter
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
import junit.framework.TestCase;

//Application-internal dependencies

/** 
 * Routine unit test for {@link DSCallAdapter}.
 * We verify that events are correctly relied. 
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
public class TestDSCallAdapter
    extends TestCase
{

    //Object under test and mock at the same time.
    //This works just great b/c eventFired() is final in DSCallAdapter. 
    MockDSCallAdapter target;  
    
    
    protected void setUp()
    {
        target = new MockDSCallAdapter();
    }

    public void testUpdate()
    {
        //Set up expected calls.
        DSCallFeedbackEvent fe = new DSCallFeedbackEvent(0, null, null);
        target.update(fe);
        
        //Transition mock to verification mode.
        target.activate();
        
        //Test.
        target.eventFired(fe);
        
        //Make sure all expected calls were performed.
        target.verify();
    }

    public void testResult()
    {
        //Set up expected calls.
        Object result = new Object();
        DSCallOutcomeEvent outcome = new DSCallOutcomeEvent(result);
        target.onEnd();
        target.handleResult(result);
        
        //Transition mock to verification mode.
        target.activate();
        
        //Test.
        target.eventFired(outcome);
        
        //Make sure all expected calls were performed.
        target.verify();
    }

    public void testNullResult()
    {
        //Set up expected calls.
        Object result = null;
        DSCallOutcomeEvent outcome = new DSCallOutcomeEvent(result);
        target.onEnd();
        target.handleNullResult();
        
        //Transition mock to verification mode.
        target.activate();
        
        //Test.
        target.eventFired(outcome);
        
        //Make sure all expected calls were performed.
        target.verify();
    }

    public void testHandleCancellation()
    {
        //Set up expected calls.
        DSCallOutcomeEvent outcome = new DSCallOutcomeEvent();
        target.onEnd();
        target.handleCancellation();
        
        //Transition mock to verification mode.
        target.activate();
        
        //Test.
        target.eventFired(outcome);
        
        //Make sure all expected calls were performed.
        target.verify();
    }

    public void testHandleException()
    {
        //Set up expected calls.
        Exception e = new Exception();
        DSCallOutcomeEvent outcome = new DSCallOutcomeEvent(e);
        target.onEnd();
        target.handleException(e);
        
        //Transition mock to verification mode.
        target.activate();
        
        //Test.
        target.eventFired(outcome);
        
        //Make sure all expected calls were performed.
        target.verify();
    }

}
