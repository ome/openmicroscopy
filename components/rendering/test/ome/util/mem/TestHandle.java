/*
 * ome.util.mem.TestHandle
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

package ome.util.mem;


//Java imports

//Third-party libraries
import junit.framework.TestCase;

//Application-internal dependencies

/** 
 * Routine unit test for {@link Handle}.
 * Verifies that we do shallow copy with deep copy semantics.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: 1.1 $ $Date: 2005/06/25 18:09:08 $)
 * </small>
 * @since OME2.2
 */
public class TestHandle
    extends TestCase
{

    private SimpleHandle    handle;  //Object under test.
    private MockBody        body;  //Mock to play the Body role.
    
    
    protected void setUp()
    {
        handle = new SimpleHandle();
        body = handle.getInitialBody();
    }
    
    public void testSharing()
    {
        //Set up expected calls.
        body.readState();
        body.readState();
        body.readState();
        
        //Transition mocks to verification mode.
        body.activate();
        
        //Test.
        //Copy operation will have to rebind body to h1 and h2.
        SimpleHandle h1 = (SimpleHandle) handle.copy(), 
                        h2 = (SimpleHandle) h1.copy();
        handle.readState();  //Has to forward to body.
        h1.readState();  //Has to forward to body.
        h2.readState();  //Has to forward to body.
        
        //Make sure all expected calls were performed.
        body.verify();
    }
    
    public void testWriteStateWhenNoSharing()
    {
        //Set up expected calls.
        body.readState();
        body.writeState();
        body.readState();
        
        //Transition mocks to verification mode.
        body.activate();
        
        //Test.
        //handle is the only Handle linked to body.  No sharing.
        handle.readState();  //Has to forward to body.
        handle.writeState();  //Has to forward to body as there's no sharing.
        handle.readState();  //Has to forward to body.
        
        //Make sure all expected calls were performed.
        body.verify();
    }
    
    public void testBreakSharing()
    {
        //Set up expected calls.
        MockBody newHandleBody = new MockBody();  //Will be linked to handle in
                                                  //the handle.copy() call.  
        body.readState();
        body.writeState();
        body.readState();
        body.copy(newHandleBody);  //Results in newHandleBody being linked to
                                   //handle. 
        newHandleBody.writeState();
        
        //Transition mocks to verification mode.
        body.activate();
        newHandleBody.activate();
        
        //Test.
        handle.readState();  //handle is initially linked to body.
                             //Reading body's state has no effect on the link.
        handle.writeState();  //The link won't be broken when writing the state
                              //if body is referenced by only one Handle.
        handle.copy();  //The Handle h returned by this method will now point
                        //to body too.
        handle.readState();  //Reading body's state has no effect on the link.
        handle.writeState();  //h and handle point to body.  State sharing must
                              //be broken b/c a new state is about to be
                              //written -- hanlde will have to make a copy of
                              //body and forward the write call to this new
                              //object.
        
        //Make sure all expected calls were performed.
        body.verify();
        newHandleBody.verify();
    }
    
}
