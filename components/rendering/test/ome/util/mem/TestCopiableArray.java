/*
 * ome.util.mem.TestCopiableArray
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
 * Unit test for {@link CopiableArray}.
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
public class TestCopiableArray
    extends TestCase
{
    
    private int                     SIZE = 10000;
    
    private SimpleCopiableArray     copiableArray;
    
    private MockBody                element;  //Mock to play the element role.
    
    protected void setUp()
    {
        copiableArray = new SimpleCopiableArray(SIZE);
        element = new MockBody();
    }
    
    public void testMakeNew()
    {
        try {
            new SimpleCopiableArray(0);
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage(), true);
        }
    }
    
    public void testSize() 
    {
        assertEquals("Should setthe size to argument passed to constructor.", 
                SIZE, copiableArray.getSize(), 0);
        
    }
    
    public void testSet()
    {
        try {
            copiableArray.set(null, SIZE);
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage(), true);
        }
        try {
            copiableArray.set(null, -1);
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage(), true);
        }
    }
    
    public void testGet()
    {
        try {
            copiableArray.get(SIZE);
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage(), true);
        }
        try {
            copiableArray.get(-1);
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage(), true);
        }
    }
    
    public void testSetAndGet()
    {
        for (int i = 0; i < copiableArray.getSize(); i++)
            copiableArray.set(element, i);
        for (int i = 0; i < copiableArray.getSize(); i++) {
            assertEquals("Wrong Copiable", element, copiableArray.get(i));
            assertNotNull(copiableArray.get(i));
        } 
    }
    
    public void testSetAndGet1()
    {
        SimpleCopiableArray 
            array = new SimpleCopiableArray(copiableArray.getSize());
        MockBody mb;
        for (int i = 0; i < copiableArray.getSize(); i++) {
            mb = new MockBody();
            copiableArray.set(mb, i);
            array.set(mb, i);
        }
        for (int i = 0; i < copiableArray.getSize(); i++) 
            assertSame("Wrong Copiable", array.get(i), copiableArray.get(i));
    }

    public void testCopy()
    {
        try {
            copiableArray.copy(-1, 0);
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage(), true);
        }
        try {
            copiableArray.copy(SIZE, SIZE);
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage(), true);
        }
        try {
            copiableArray.copy(0, SIZE);
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage(), true);
        }
        try {
            copiableArray.copy(1, 0);
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage(), true);
        }
    }
    
    public void testCopy1()
    {
        SimpleCopiableArray array = new SimpleCopiableArray(2);
        array.set(element, 0);
        assertEquals("Wrong Copiable", element, array.get(0));
        assertNull("Element should be null", array.get(1));
        element.copy(element); 
        element.activate();
        array.copy(0, 1);
        element.verify();
        assertNotNull("Element shouldn't be null", array.get(1));
        assertEquals("Wrong Copiable", element, array.get(1));
    }
    
    public void testCopyNull()
    {
        SimpleCopiableArray array = new SimpleCopiableArray(2);
        assertNull("Element should be null", array.get(0));
        assertNull("Element should be null", array.get(1));
        array.copy(0, 1);
        assertNull("Element should be null", array.get(1));
    }
    
}
