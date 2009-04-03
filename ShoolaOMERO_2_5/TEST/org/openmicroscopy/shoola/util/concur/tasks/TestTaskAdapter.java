/*
 * org.openmicroscopy.shoola.util.concur.tasks.TestTaskAdapter
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
 * Routine unit test for {@link TaskAdapter}.
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
public class TestTaskAdapter
    extends TestCase
{

    private TaskAdapter     target;  //Object under test.
    private MockExecCommand task;  //Convenience, we only use it as a Runnable.
    
    
    public void setUp()
    {
        task = new MockExecCommand();
        target = new TaskAdapter(task);
    }
    
    public void testTaskAdapter()
    {
        try {
            new TaskAdapter(null);
            fail("TaskAdapter shouldn't accept null.");
        } catch (NullPointerException npe) {
            //OK, expected.
        }
    }
    
    public void testIsDone() 
        throws Exception
    {
        //Set up expected calls.
        task.run();
        
        //Transition mock to verification mode.
        task.activate();
        
        //Test.
        assertFalse("Before doStep we're not done!", target.isDone());
        target.doStep();
        assertTrue("After doStep we're done!", target.isDone());
        target.doStep();
        assertTrue("After the fisrt doStep we're done!", target.isDone());
        
        //Make sure all expected calls were performed.
        task.verify();
    }
    
    public void testDoStep() 
        throws Exception
    {
        //Set up expected calls.
        task.run();
        
        //Transition mock to verification mode.
        task.activate();
        
        //Test.
        target.doStep();
        target.doStep();  //Shouldn't be executed.
        
        //Make sure all expected calls were performed.
        task.verify();
    }
    
}
