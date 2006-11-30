/*
 * org.openmicroscopy.shoola.util.concur.tasks.TestListAssembler
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
import java.util.Iterator;
import java.util.List;

//Third-party libraries
import junit.framework.TestCase;

//Application-internal dependencies

/** 
 * Routine unit test for {@link ListAssembler}.
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
public class TestListAssembler
    extends TestCase
{

    private ListAssembler   target;
    
    
    public void setUp() 
    {
        target = new ListAssembler();
    }
    
    public void testResult()
    {
        Object result = target.assemble();
        assertNotNull("Should never return null.", result);
        assertTrue("Should return a List.", result instanceof List);
    }

    public void testAdd()
    {
        target.add(null);  //OK, accepts null.
    }

    public void testAssemble()
    {
        Object[] partialResults = {new Object(), null, new Object()};
        target.add(partialResults[0]);
        target.add(null);  //partialResults[1].
        target.add(partialResults[2]);
        List result = (List) target.assemble();
        assertEquals("Results not added correctly.", 3, result.size());
        Iterator i = result.iterator();
        for(int k = 0; k < 3; ++k)
            assertTrue("Partial result out of order: "+k+".", 
                    partialResults[k] == i.next());
    }

}
