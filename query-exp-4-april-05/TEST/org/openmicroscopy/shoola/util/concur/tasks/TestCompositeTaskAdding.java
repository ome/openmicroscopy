/*
 * org.openmicroscopy.shoola.util.concur.tasks.TestCompositeTaskAdding
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
import org.openmicroscopy.shoola.util.tests.common.NullRunnable;

/** 
 * Verifies the behavior of a <code>CompositeTask</code> object when in the
 * <i>Adding</i> state.
 * All transitions going out of the <i>Adding</i> state are verified. 
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
public class TestCompositeTaskAdding
    extends TestCase
{

    //Object under test.  Will be in ADDING state.
    private CompositeTask   target;  
    
    
    protected void setUp()
    {
        target = new CompositeTask();
    }

    public void testCompositeTask()
    {
        assertEquals("Should be in the ADDING state after creation.", 
                CompositeTask.ADDING, target.getState());
        assertEquals("Should have no children after creation.",
                0, target.getChildren().size());
    }
    
    public void testIsDoneWhenNoChildren()
    {
        assertTrue("Should return true if there are no children.", 
                target.isDone());
        assertEquals("Should transition to DONE if there are no children.", 
                CompositeTask.DONE, target.getState());
    }
    
    public void testIsDoneWhenChildrenDone1()
    {
        target.add(new NullMultiStepTask());  //Its isDone always returns TRUE.
        assertTrue("Should return true if there are children, but they're "+
                "all done.", target.isDone());
        assertEquals("If there are children, but they're all done, then it "+
                "should transition to DONE.", 
                CompositeTask.DONE, target.getState());
    }
    
    public void testIsDoneWhenChildrenDone2()
    {
        target.add(new NullMultiStepTask());  //Its isDone always returns TRUE.
        target.add(new NullMultiStepTask());
        assertTrue("Should return true if there are children, but they're "+
                "all done.", target.isDone());
        assertEquals("If there are children, but they're all done, then it "+
                "should transition to DONE.", 
                CompositeTask.DONE, target.getState());
    }
    
    public void testIsDoneWhenChildrenNotDone1()
    {
        //A Runnable is wrapped into an adapter whose isDone method always
        //returns FALSE until doStep is called.
        target.add(new NullRunnable());  
        assertFalse("Should return false if there are children and they're "+
                "not all done.", target.isDone());
        assertEquals("If there are children and they're not all done, then it "+
                "should transition to ITERATING.", 
                CompositeTask.ITERATING, target.getState());
    }
    
    public void testIsDoneWhenChildrenNotDone2()
    {
        //A Runnable is wrapped into an adapter whose isDone method always
        //returns FALSE until doStep is called.
        target.add(new NullRunnable());  
        target.add(new NullMultiStepTask());  //Its isDone always returns TRUE.
        assertFalse("Should return false if there are children and they're "+
                "not all done.", target.isDone());
        assertEquals("If there are children and they're not all done, then it "+
                "should transition to ITERATING.", 
                CompositeTask.ITERATING, target.getState());
    }
    
    public void testIsDoneWhenChildrenNotDone3()
    {
        target.add(new NullMultiStepTask());  //Its isDone always returns TRUE.
        //A Runnable is wrapped into an adapter whose isDone method always
        //returns FALSE until doStep is called.
        target.add(new NullRunnable());  
        target.add(new NullMultiStepTask());  //Its isDone always returns TRUE.
        assertFalse("Should return false if there are children and they're "+
                "not all done.", target.isDone());
        assertEquals("If there are children and they're not all done, then it "+
                "should transition to ITERATING.", 
                CompositeTask.ITERATING, target.getState());
    }

    public void testDoStep() 
        throws Exception
    {
        try {
            target.doStep();
            fail("Shouldn't allow to call doStep while the node is built.");
        } catch (IllegalStateException ise) {
            //Ok, expected.
        }
    }
    
    public void testAddMultiStepTask()
    {
        MultiStepTask task = null;
        try {
            target.add(task);
            fail("Shouldn't allow to add a null child.");
        } catch (NullPointerException npe) {
            //Ok, expected.
        }
        task = new NullMultiStepTask();
        target.add(task);
        assertEquals("Didn't add.", 1, target.getChildren().size());
        assertEquals("Should have kept the original MultiStepTask node.", 
                task, target.getChildren().get(0));
        assertEquals("Should have remained in the ADDING state.", 
                CompositeTask.ADDING, target.getState());
    }

    public void testAddRunnable()
    {
        Runnable task = null;
        try {
            target.add(task);
            fail("Shouldn't allow to add a null child.");
        } catch (NullPointerException npe) {
            //Ok, expected.
        }
        task = new NullRunnable();
        target.add(task);
        assertEquals("Didn't add.", 1, target.getChildren().size());
        assertEquals("Should have remained in the ADDING state.", 
                CompositeTask.ADDING, target.getState());
    }

    public void testAddInvocation()
    {
        Invocation task = null;
        try {
            target.add(task);
            fail("Shouldn't allow to add a null child.");
        } catch (NullPointerException npe) {
            //Ok, expected.
        }
        task = new MockInvocation();
        target.add(task);
        assertEquals("Didn't add.", 1, target.getChildren().size());
        assertEquals("Should have remained in the ADDING state.", 
                CompositeTask.ADDING, target.getState());
    }
    
    public void testGetCurChildWhenNoChildren()
    {
        assertNull("Should return null if there are no children.", 
                target.getCurChild());
        assertEquals("Should transition to DONE if there are no children.", 
                CompositeTask.DONE, target.getState());
    }
    
    public void testGetCurChildWhenChildrenDone()
    {
        target.add(new NullMultiStepTask());  //Its isDone always returns TRUE.
        assertNull("Should return null if there are children, but they're "+
                "all done.", 
                target.getCurChild());
        assertEquals("Should transition to DONE if all children are done.", 
                CompositeTask.DONE, target.getState());
    }
    
    public void testGetCurChildWhenChildrenNotDone1()
    {
        //A Runnable is wrapped into an adapter whose isDone method always
        //returns FALSE until doStep is called.
        target.add(new NullRunnable()); 
        assertNotNull("Shouldn't return null if there are children and "+
                "they're not all done.", 
                target.getCurChild());
        assertEquals("Should transition to ITERATING if not all children are "+
                "done.", 
                CompositeTask.ITERATING, target.getState());
    }
    
    public void testGetCurChildWhenChildrenNotDone2()
    {
        MultiStepTask task = new NullMultiStepTask();
        target.add(task);  //task.isDone() always returns TRUE.
        //A Runnable is wrapped into an adapter whose isDone method always
        //returns FALSE until doStep is called.
        target.add(new NullRunnable()); 
        assertNotNull("Shouldn't return null if there are children and "+
                "they're not all done.", 
                target.getCurChild());
        assertNotSame("Returned wrong child.", task, target.getCurChild());
        assertEquals("Should transition to ITERATING if not all children are "+
                "done.", 
                CompositeTask.ITERATING, target.getState());
    }

}
