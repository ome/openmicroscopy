/*
 * org.openmicroscopy.shoola.util.concur.tasks.TestPlainAssembler
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

import junit.framework.TestCase;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Routine unit test for {@link PlainAssembler}.
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
public class TestPlainAssembler
    extends TestCase
{

    private PlainAssembler  target;  //The object under test.
    
    
    public void setUp()
    {
        target = new PlainAssembler();
    }
    
    public void testPlainAssembler()
    {
        assertNull("A new object should have a null result.", 
                target.assemble());
    }

    public void testSetAndGet()
    {
        Object result = new Object();
        target.add(result);
        assertEquals("Should always return the last object set by add.", 
                result, target.assemble());
        
        result = new Object();
        target.add(result);
        assertEquals("Should always return the last object set by add.", 
                result, target.assemble());
    }

}
