/*
 * org.openmicroscopy.shoola.util.concur.tasks.TestPlainAssembler
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
import junit.framework.TestCase;

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
