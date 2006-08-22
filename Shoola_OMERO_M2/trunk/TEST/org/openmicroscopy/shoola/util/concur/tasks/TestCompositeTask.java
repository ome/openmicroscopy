/*
 * org.openmicroscopy.shoola.util.concur.tasks.TestCompositeTask
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//Third-party libraries
import junit.framework.TestCase;

//Application-internal dependencies

/** 
 * Basic tests to verify the execution algorithm of <code>CompositeTask</code>.
 * We first use flat trees with a root node and only leaf nodes below it and 
 * verify how each tree behaves when in the <i>Iterating</i> state.  We then
 * switch to more elaborate trees.
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
public class TestCompositeTask
    extends TestCase
{

    //Helper class to configure a MockMultiStepTask to run within doTest().
    //Pass the number of steps the task is supposed to execute and get a
    //mock task with all calls already set up for running within doTest().
    //The mock will be in verification mode.
    private class TaskConfig
    {
        final int steps;
        final MockMultiStepTask task;
        
        TaskConfig(int steps)
        {
            if (steps <= 0) 
                throw new IllegalArgumentException(
                        "Number of steps must be positive.");
            this.steps = steps;
            task = new MockMultiStepTask();
            
            //Set up expected calls -- see doTest().
            for (int i = 0; i < steps; ++i) { 
                task.isDone(false, null);  //Triggered by target.isDone().
                task.isDone(false, null);  //Triggered by target.getCurChild().
                task.doStep(null, null);   //Triggered by target.doStep().
            }
            task.isDone(true, null);  //Triggered by target.isDone() w/in the
                                       //loop of the next task or by the last
                                       //call to target.isDone if this is the
                                       //last task.
            
            //Transition the mock to verification mode.
            task.activate();
        }
    }
    
    //Object under test, root of the tree.
    private CompositeTask   target;   
    
    //Mirrors the sub-list of target.children composed of all not-done tasks.
    //That is, all tasks whose isDone method returns false.  These are all of
    //the tasks that were added by addTask().  Each element is a TaskConfig
    //which wraps the original mock task and keeps track of how many steps the
    //task is supposed to carry out.
    //For example, if target.children is the list <d1, d2, t1, t2, d3, t3>
    //(d stands for done task added by addDoneTask, t for mock task added by
    //addTask), then actualTasks would be <tc1, tc2, tc3> (tc is a TaskConfig)
    //and tc(i).task == t(i).
    private List            actualTasks;
    
    
    protected void setUp()
    {
        target = new CompositeTask();
        actualTasks = new ArrayList();
    }
    
    //Adds a task leaf node to the root.
    //Pass the number of steps the task is supposed to carry out.
    protected void addTask(int steps) 
    { 
        TaskConfig tc = new TaskConfig(steps);
        target.add(tc.task);
        actualTasks.add(tc);
    }
    
    //Adds a done task leaf node to the root.
    protected void addDoneTask() { target.add(new NullMultiStepTask()); }
    
    //Runs the test.
    //The test consists in simulating the execution loop run by a CmdProcessor.
    //We verify that all tasks added by addTask() are executed in the same
    //order as they were added.
    //Call this method after you've built the tree (add methods).
    protected void doTest() 
        throws Exception
    {
        TaskConfig tc;
        Iterator tasksIter = actualTasks.iterator();
        while (tasksIter.hasNext()) {
            
            //Get next TaskConfig.  Call expectations on its mock task have 
            //already been set up and and the mock is in verification mode.
            tc = (TaskConfig) tasksIter.next();
            
            //Execute every task's step.
            for (int i = 0; i < tc.steps; ++i) {
                
                //First call isDone().  This has to result in the target's 
                //execution algo advancing to tc.task (i=0) or staying on it
                //until it's done.  Advancing to tc.task involves a call to
                //tc.task.isDone which returns false.  Also, if tc is not the
                //first element of activationList, then its predecessor's task
                //will receive a call to isDone, which has to return true.
                assertFalse("Task wasn't done yet <"+tc.task+">.", 
                        target.isDone());
                
                //Verify overall execution order and execute step.
                assertSame("Wrong execution order; execution algo didn't "+
                        "rispect the order in which the tasks were added.", 
                        tc.task, target.getCurChild());
                target.doStep();
            }
        }
        
        //Last call to target.isDone.  This will result in a call to ltc.isDone
        //which has to return true -- ltc is the last tc in activationList.
        assertTrue("Should have been finished.", target.isDone());
        
        //Verify that all expected calls where performed.
        tasksIter = actualTasks.iterator();
        while (tasksIter.hasNext()) {
            tc = (TaskConfig) tasksIter.next();
            tc.task.verify();
        }
    }
    
    public void test1() 
        throws Exception
    {
        //Build the tree.
        addTask(1);     //t1 - [steps=1]
        
        //Run the test.
        doTest();
    }
    
    public void test2() 
        throws Exception
    {
        //Build the tree.
        addDoneTask();  //d1
        addTask(1);     //t1 - [steps=1]
        
        //Run the test.
        doTest();
    }
    
    public void test3() 
        throws Exception
    {
        //Build the tree.
        addTask(3);     //t1 - [steps=1]
        addDoneTask();  //d1
        
        //Run the test.
        doTest();
    }
    
    public void test4() 
        throws Exception
    {
        //Build the tree.
        addDoneTask();  //d1
        addDoneTask();  //d2
        addTask(1);     //t1 - [steps=1]
        addTask(2);     //t2 - [steps=2]
        addDoneTask();  //d3
        addTask(2);     //t3 - [steps=1]
        
        //Run the test.
        doTest();
    }
    
    public void test5() 
        throws Exception
    {
        //Build the tree.
        addDoneTask();  //d1
        addDoneTask();  //d2
        addTask(5);     //t1 - [steps=5]
        addTask(2);     //t2 - [steps=2]
        addDoneTask();  //d3
        addTask(4);     //t3 - [steps=1]
        addDoneTask();  //d4
        
        //Run the test.
        doTest();
    }
    
    /* Following tree:
     *     R
     *     |--- I1
     *     |    |--- I11
     *     |    |     |--- L1  (2 steps)
     *     |    |    
     *     |    |--- I12
     *     |    |     |--- L2  (1 step)
     *     |
     *     |--- I2 (empty)
     *     |
     *     |--- I3
     *          |--- L3  (1 step)
     *          |--- L4  (3 steps)
     */
    public void test6() throws Exception
    {
        //---------------- BUILD TREE ------------------------------------------
        
        //Create root and internal nodes.
        CompositeTask R = new CompositeTask();  //Root.
        CompositeTask   I1 = new CompositeTask(),  //1st-level internal nodes. 
                           I11 = new CompositeTask(),  //2nd-level nodes.
                           I12 = new CompositeTask(),
                        I2 = new CompositeTask(), 
                        I3 = new CompositeTask();
        
        //Link internal nodes.
        R.add(I1); R.add(I2); R.add(I3);
        I1.add(I11); I1.add(I12);
        
        //Set leaves.  Share convenience mock.  (Avoids one mock for each leaf.)
        MockMultiStepTask task = new MockMultiStepTask();
        
        I11.add(task);  //L1 - [steps=2]
        task.isDone(false, null);
        task.doStep(null, null);
        task.isDone(false, null);
        task.doStep(null, null);
        task.isDone(true, null);
        
        I12.add(task);  //L2 - [steps=1]
        task.isDone(false, null);
        task.doStep(null, null);
        task.isDone(true, null);
        
        I3.add(task);  //L3 - [steps=1]
        task.isDone(false, null);
        task.doStep(null, null);
        task.isDone(true, null);
        
        I3.add(task);  //L4 - [steps=3]
        task.isDone(false, null);
        task.doStep(null, null);
        task.isDone(false, null);
        task.doStep(null, null);
        task.isDone(false, null);
        task.doStep(null, null);
        task.isDone(true, null);
        
        
        //------------- TEST ---------------------------------------------------
        
        task.activate();
        while (!R.isDone()) R.doStep();
        task.verify();
    }

}
