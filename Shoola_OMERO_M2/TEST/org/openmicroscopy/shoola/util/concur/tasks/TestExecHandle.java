/*
 * org.openmicroscopy.shoola.util.concur.tasks.TestExecHandle
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
 * Routine unit test for {@link ExecHandle}.
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
public class TestExecHandle
    extends TestCase
{
    
    //Mock object.
    private MockExecCommand     command;
    
    //Object under test.
    private ExecHandle          handle;
    
    
    public void setUp() 
    {
        command = new MockExecCommand();
        handle = new ExecHandle();  //Not in a legal state yet (two-step init).
        handle.setCommand(command);  //OK, init completed now.
    }
    
    public void testSetCommand()
    {
        try {
            handle.setCommand(null);
            fail("Shouldn't accept a null command.");
        } catch (NullPointerException npe) {
            //OK, expected.
        }
    }
    
    public void testCancelExecution()
    {
        //Set up expected calls.
        command.cancel();
        
        //Transition mock to verification mode.
        command.activate();
        
        //Test.
        handle.cancelExecution();
        
        //Make sure all expected calls were performed.
        command.verify();
    }

}
