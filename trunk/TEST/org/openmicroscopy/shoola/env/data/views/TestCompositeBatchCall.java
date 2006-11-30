/*
 * org.openmicroscopy.shoola.env.data.views.TestCompositeBatchCall
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

package org.openmicroscopy.shoola.env.data.views;


//Java imports

//Third-party libraries
import junit.framework.TestCase;

//Application-internal dependencies

/** 
 * Routine unit test for {@link CompositeBatchCall}.
 * We only concentrate on those methods that don't forward to
 * {@link org.openmicroscopy.shoola.util.concur.tasks.CompositeTask}.
 * In fact, for the rest a {@link CompositeBatchCall} inherits its behavior
 * (through delegation) from <code>CompositeTask</code>, which has already
 * been tested. 
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
public class TestCompositeBatchCall
    extends TestCase
{

    /* Our fixture is the following tree:
     *     R
     *     |--- I1
     *     |    |--- I11
     *     |    |     |--- L1  
     *     |    |    
     *     |    |--- I12
     *     |    |     |--- L2  
     *     |
     *     |--- I2 (empty)
     *     |
     *     |--- I3
     *     |    |--- L3  
     *     |    |--- L4  
     *     |
     *     |--- L5
     * 
     * R (root) is target, Ixx are internal nodes (instances of 
     * CompositeBatchCall), Lx are leaf nodes (instances of
     * FakeLeafBatchCall). 
     */
    
    private CompositeBatchCall  target;
    private FakeLeafBatchCall[] L;  //Use same numbers. Leave [0] = null.
    
    
    private void buildTree()
    {
        CompositeBatchCall I1 = new CompositeBatchCall(), //1st-level nodes.
                              I11 = new CompositeBatchCall(), //2nd-level nodes.
                              I12 = new CompositeBatchCall(),
                           I2 = new CompositeBatchCall(),
                           I3 = new CompositeBatchCall();
        target.add(I1); target.add(I2); target.add(I3);
        I1.add(I11); I1.add(I12);
        I11.add(L[1]); I12.add(L[2]);
        I3.add(L[3]); I3.add(L[4]);
        target.add(L[5]);
    }
    
    protected void setUp()
    {
        target = new CompositeBatchCall();
        L = new FakeLeafBatchCall[6];
        for (int i = 1; i < 6; ++i)
            L[i] = new FakeLeafBatchCall();
        buildTree();
    }
    
    public void testCountCalls()
    {
        assertEquals("Wrong calls count.", 5, target.countCalls());
    }
    
    public void testGetCurCall() 
        throws Exception
    {
        for (int i = 1; i < 6; ++i) {
            assertFalse("Not done, stopped at "+i+".", target.isDone());
            assertSame("Wrong current call, stopped at "+i+".",
                    L[i], target.getCurCall());
            target.doStep();
        }
        assertTrue("Should have been finished.", target.isDone());
        assertNull("Last processed call should be discaded when done.", 
                target.getCurCall());
        
        for (int i = 1; i < 6; ++i) 
            assertEquals("L"+i+" should have been called only once.", 
                    1, L[i].getDoCallCount());
    }

}
