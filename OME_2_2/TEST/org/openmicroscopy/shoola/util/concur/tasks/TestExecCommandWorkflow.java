/*
 * org.openmicroscopy.shoola.util.concur.tasks.TestExecCommandWorkflow
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
import junit.framework.TestCase;

//Application-internal dependencies

/** 
 * Verifies that the main flow within the execution workflow is correctly
 * performed.
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
public class TestExecCommandWorkflow
    extends TestCase
{

    private ExecCommand         target;  //Object under test.
    private MockMultiStepTask   task;  //Mock object.
    private MockResultAssembler assembler;  //Mock object.
    private MockExecMonitor     observer;  //Mock object.
    private Future              future;
    private Object[]            partialResult;  //Results of each step.
    private Object              result;  //Result of the computation.
    
    
    public void makeMocks()
    {
        task = new MockMultiStepTask();
        assembler = new MockResultAssembler();
        observer = new MockExecMonitor();
        future = new Future();  //Not in a legal state yet (two-step init).
        target = new ExecCommand(task, assembler, future, observer);
        future.setCommand(target);  //OK, init completed now.
        result = new Object();
    }
    
    private void setUpExpectedCalls(int nbrOfSteps)
    {
        partialResult = new Object[nbrOfSteps];
        observer.onStart(null);
        for (int i = 0; i < nbrOfSteps; ++i) {
            partialResult[i] = new Object();
            task.isDone(false, null);
            task.doStep(partialResult[i], null);
            assembler.add(partialResult[i], null);
            observer.update(i+1, null);
        }
        task.isDone(true, null);
        assembler.assemble(result, null);
        observer.onEnd(result, null);
    }
    
    private void transitionMocksToVerificationMode()
    {
        task.activate();
        assembler.activate();
        observer.activate();
    }
    
    private void ensureAllExpectedCallsWerePerformed()
    {
        task.verify();
        assembler.verify();
        observer.verify();
    }
    
    private void doTestMainFlow(int nbrOfSteps) 
        throws ExecException, InterruptedException
    {
        makeMocks();
        setUpExpectedCalls(nbrOfSteps);
        transitionMocksToVerificationMode();
        
        //Test.
        target.run();
        assertEquals("Wrong result set into the future.", 
                result, future.getResult());
        
        ensureAllExpectedCallsWerePerformed();
    }
    
    public void setUp()
    {
        task = null;
        assembler = null;
        observer = null;
        future = null;
        target = null;
        result = null;
    }
    
    public void testMainFlow()
        throws ExecException, InterruptedException
    {
        for (int nbrOfStep = 0; nbrOfStep < 10; ++nbrOfStep)
            doTestMainFlow(nbrOfStep);
    }
    
}
