/*
 * org.openmicroscopy.shoola.env.data.views.TestCallHandle
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

import org.openmicroscopy.shoola.util.concur.tasks.MockExecHandle;

import junit.framework.TestCase;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Routine unit test for {@link CallHandle}.
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
public class TestCallHandle
    extends TestCase
{

    public void testCallHandle()
    {
        try {
            new CallHandle(null);
            fail("Shouldn't allow null.");
        } catch (NullPointerException npe) {
            //Ok, expected.
        }
    }

    //Verify delegation.
    public void testCancel()
    {
        //Create mock and set up expectations, then transition to 
        //verification mode.
        MockExecHandle delegate = new MockExecHandle();
        delegate.cancelExecution();
        delegate.activate();
        
        //Test.
        CallHandle target = new CallHandle(delegate);
        target.cancel();
        
        //Make sure all expected calls were performed.
        delegate.verify();
    }

}
