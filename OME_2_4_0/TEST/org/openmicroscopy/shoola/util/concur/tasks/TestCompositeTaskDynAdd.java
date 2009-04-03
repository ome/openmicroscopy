/*
 * org.openmicroscopy.shoola.util.concur.tasks.TestCompositeTaskDynAdd
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
 * Verify the execution algorithm of <code>CompositeTask</code> when new nodes
 * are added while in the <i>Iterating</i> state.
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
public class TestCompositeTaskDynAdd
    extends TestCase
{
    
    //Leaf node.  When run, it updates the execIndex and then checks that the 
    //expected execution order is the same as the new execIndex value. 
    private class XOrderChecker
        implements MultiStepTask
    {
        int expected;
        boolean done = false;
        
        XOrderChecker(int expected) { this.expected = expected; }
        public Object doStep() { 
            assertEquals("Wrong execution order.", expected, ++execIndex);
            done = true;
            return null;
        }
        public boolean isDone() { return done; }
    }
    
    //Leaf node.  Behaves just like its XOrderChecker but adds a new child to
    //a given parent at the end of the call.
    private class DynAdder
        extends XOrderChecker
    {
        CompositeTask parent;
        MultiStepTask child;
        
        DynAdder(int expected, CompositeTask parent, MultiStepTask child) {
            super(expected);
            this.parent = parent;
            this.child = child;
        }
        public Object doStep() { 
            super.doStep();
            parent.add(child);
            return null;
        }
    }
    
    //The object under test.  Root of the tree.
    private CompositeTask       target;
    
    //Keeps track of how many leaf nodes have been executed.
    //Updated by every XOrderChecker when it gets executed.
    private int                 execIndex;
    
    
    protected void setUp()
    {
        target = new CompositeTask();
        execIndex = 0;
    }
    
    //Simple flat tree.  One task adds a new one to the root.
    public void test1() 
        throws Exception
    {
        target.add(new DynAdder(1, target, new XOrderChecker(2)));
        while (!target.isDone()) target.doStep();
    }
    
    //Simple flat tree.  Tree tasks, second one adds a new one to the root.
    public void test2() 
        throws Exception
    {
        target.add(new XOrderChecker(1));
        target.add(new DynAdder(2, target, new XOrderChecker(4)));
        target.add(new XOrderChecker(3));
        while (!target.isDone()) target.doStep();
    }
    
    /* Following tree:
     *     R
     *     |--- I
     *     |    |--- L2
     *     |    |--- L3
     *     |    |--- II
     *     |          |--- L4
     *     |--- L1
     * 
     * R is target.  An empty I is added, then L1 and then L2, L3 to I.
     * II is added by L2 during execution.
     */
    public void test3() 
        throws Exception
    {
        CompositeTask I = new CompositeTask(), II = new CompositeTask();
        target.add(I);
        target.add(new XOrderChecker(4));  //L1
        I.add(new DynAdder(1, I, II));  //L2
        I.add(new XOrderChecker(2));  //L3
        II.add(new XOrderChecker(3));  //L4
        while (!target.isDone()) target.doStep();
    }
    
    /* Following tree:
     *     R
     *     |--- I
     *     |    |--- L2
     *     |    |--- L3
     *     | 
     *     |--- L1
     * 
     * R is target.  An empty I is added, then L1 and then L2, L3 to I.
     * L1 attempts to add (and fails) a new node to I during execution.
     */
    public void test4() 
        throws Exception
    {
        CompositeTask I = new CompositeTask(), II = new CompositeTask();
        target.add(I);
        target.add(new DynAdder(3, I, II));  //L1
        I.add(new XOrderChecker(1));  //L2
        I.add(new XOrderChecker(2));  //L3
        II.add(new XOrderChecker(3));  //L4
        try {
            while (!target.isDone()) target.doStep();
            fail("Shouldn't allow to add a child to an already executed node.");
        } catch (IllegalStateException ise) {
            //Ok, expected.
        }
    }

}
