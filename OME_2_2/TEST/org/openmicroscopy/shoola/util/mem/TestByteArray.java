/*
 * org.openmicroscopy.shoola.util.mem.TestByteArray
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

package org.openmicroscopy.shoola.util.mem;


//Java imports

//Third-party libraries
import junit.framework.TestCase;

//Application-internal dependencies

/** 
 * Tests the normal operation of <code>ByteArray</code> and possible exceptions.
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
public class TestByteArray
    extends TestCase
{

    public void testByteArray()
    {
        try {
            new ByteArray(null, 0, 0);
            fail("Shouldn't accept null base.");
        } catch (NullPointerException npe) {}
        
        byte[] base = new byte[0];
        try {
            new ByteArray(base, -1, 0);
            fail("Shouldn't accept negative offset.");
        } catch (IllegalArgumentException iae) {}
        try {
            new ByteArray(base, 0, -1);
            fail("Shouldn't accept negative length.");
        } catch (IllegalArgumentException iae) {}
        try {
            new ByteArray(base, 1, 0);
            fail("Shouldn't accept inconsistent [offset, offset+length]."); 
        } catch (IllegalArgumentException iae) {}
    }

    public void testSetEmptyArray()
    {
        byte value = (byte) 25;
        byte[] base = new byte[] {0};
        ByteArray ba = new ByteArray(base, 1, 0);
        try {
            ba.set(0, value);
            fail("Shouldn't accept index 0 if length is 0.");
        } catch (ArrayIndexOutOfBoundsException aiobe) {}
        try {
            ba.set(-1, value);
            fail("Shouldn't accept negative index.");
        } catch (ArrayIndexOutOfBoundsException aiobe) {}
        try {
            ba.set(1, value);
            fail("Shouldn't accept index greater than length-1.");
        } catch (ArrayIndexOutOfBoundsException aiobe) {}
    }
    
    public void testSet1LengthArray()
    {
        byte value = (byte) 25;
        byte[] base = new byte[] {0, 1};
        ByteArray ba = new ByteArray(base, 1, 1);
        ba.set(0, value);
        assertEquals("Set wrong value.", value, ba.get(0));
        assertEquals("Didn't set value in original base array.",
                value, base[1]);
        try {
            ba.get(-1);
            fail("Shouldn't accept negative index.");
        } catch (ArrayIndexOutOfBoundsException aiobe) {}
        try {
            ba.get(1);
            fail("Shouldn't accept index greater than length-1.");
        } catch (ArrayIndexOutOfBoundsException aiobe) {}
    }
    
    public void testSet2LengthArray()
    {
        byte value_0 = (byte) 25, value_1 = (byte) -1;
        byte[] base = new byte[] {0, 1};
        ByteArray ba = new ByteArray(base, 0, 2);
        ba.set(0, value_0);
        assertEquals("Set wrong value.", value_0, ba.get(0));
        assertEquals("Didn't set value in original base array.",
                value_0, base[0]);
        ba.set(1, value_1);
        assertEquals("Set wrong value.", value_1, ba.get(1));
        assertEquals("Didn't set value in original base array.",
                value_1, base[1]);
        try {
            ba.get(-1);
            fail("Shouldn't accept negative index.");
        } catch (ArrayIndexOutOfBoundsException aiobe) {}
        try {
            ba.get(2);
            fail("Shouldn't accept index greater than length-1.");
        } catch (ArrayIndexOutOfBoundsException aiobe) {}
    }
    
    public void testSet3LengthArray()
    {
        byte value_0 = (byte) 25, value_1 = (byte) -1, value_2 = (byte) 7;
        byte[] base = new byte[] {0, 1, 2, 3};
        ByteArray ba = new ByteArray(base, 1, 3);
        ba.set(0, value_0);
        assertEquals("Set wrong value.", value_0, ba.get(0));
        assertEquals("Didn't set value in original base array.",
                value_0, base[1]);
        ba.set(1, value_1);
        assertEquals("Set wrong value.", value_1, ba.get(1));
        assertEquals("Didn't set value in original base array.",
                value_1, base[2]);
        ba.set(2, value_2);
        assertEquals("Set wrong value.", value_2, ba.get(2));
        assertEquals("Didn't set value in original base array.",
                value_2, base[3]);
        try {
            ba.get(-1);
            fail("Shouldn't accept negative index.");
        } catch (ArrayIndexOutOfBoundsException aiobe) {}
        try {
            ba.get(3);
            fail("Shouldn't accept index greater than length-1.");
        } catch (ArrayIndexOutOfBoundsException aiobe) {}
    }

}
