/*
 * org.openmicroscopy.shoola.util.concur.tasks.TestCompositeTaskDone
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
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
 * <i>Done</i> state.
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
public class TestCompositeTaskDone
    extends TestCase
{

    //Object under test.  Will be in DONE state with no children.
    protected CompositeTask   target; 
    
    
    protected void setUp()
    {
        target = new CompositeTask();  //In ADDING state.
        target.isDone();  //Transitions to DONE b/c there are no children.
    }

    public void testDoStep() 
        throws Exception
    {
        try {
            target.doStep();
            fail("Shouldn't allow to call doStep after the node is executed.");
        } catch (IllegalStateException ise) {
            //Ok, expected.
        }
    }

    public void testIsDone()
    {
        assertTrue("Should always return true when state is DONE.", 
                target.isDone());
    }

    public void testAddMultiStepTask()
    {
        try {
            target.add(new NullMultiStepTask());
            fail("Shouldn't allow to add children after the node is executed.");
        } catch (IllegalStateException ise) {
            //Ok, expected.
        }
    }

    public void testAddRunnable()
    {
        try {
            target.add(new NullRunnable());
            fail("Shouldn't allow to add children after the node is executed.");
        } catch (IllegalStateException ise) {
            //Ok, expected.
        }
    }

    public void testAddInvocation()
    {
        try {
            target.add(new MockInvocation());
            fail("Shouldn't allow to add children after the node is executed.");
        } catch (IllegalStateException ise) {
            //Ok, expected.
        }
    }

    public void testGetCurChild()
    {
        assertNull("Should always return null when state is DONE.", 
                target.getCurChild());
    }

}
