/*
 * org.openmicroscopy.shoola.env.rnd.defs.TestPlaneDef
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

package org.openmicroscopy.shoola.env.rnd.defs;


//Java imports
import java.util.HashSet;
import java.util.Set;

//Third-party libraries
import junit.framework.TestCase;

//Application-internal dependencies

/** 
 * Unit test for {@link PlaneDef}.
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
public class TestPlaneDef
    extends TestCase
{
    
    public void testPlaneDef()
    {
        try {
            new PlaneDef(-1, 0);
            fail("Should only accept slice identifiers defined by the static "+
                    "constants.");
        } catch (IllegalArgumentException iae) {
            //Ok, expected.
        }
        try {
            new PlaneDef(PlaneDef.XY, -1);
            fail("Shouldn't accept negative timepoints.");
        } catch (IllegalArgumentException iae) {
            //Ok, expected.
        }
    }
    
    public void testSetX()
    {
        PlaneDef pd = new PlaneDef(PlaneDef.ZY, 0);
        try {
            pd.setX(-1);
            fail("Shouldn't accept negative index.");
        } catch (IllegalArgumentException iae) {
            //Ok, expected.
        }
        assertEquals("Shouldn't have set a negative index.", 0, pd.getX());
        
        pd = new PlaneDef(PlaneDef.XY, 0);
        try {
            pd.setX(1);
            fail("Should refuse to set x index if not ZY plane.");
        } catch (IllegalArgumentException iae) {
            //Ok, expected.
        }
        assertEquals("Shouldn't have set the index.", 0, pd.getX());
    }
    
    public void testSetY()
    {
        PlaneDef pd = new PlaneDef(PlaneDef.XZ, 0);
        try {
            pd.setY(-1);
            fail("Shouldn't accept negative index.");
        } catch (IllegalArgumentException iae) {
            //Ok, expected.
        }
        assertEquals("Shouldn't have set a negative index.", 0, pd.getY());
        
        pd = new PlaneDef(PlaneDef.XY, 0);
        try {
            pd.setY(1);
            fail("Should refuse to set y index if not XZ plane.");
        } catch (IllegalArgumentException iae) {
            //Ok, expected.
        }
        assertEquals("Shouldn't have set the index.", 0, pd.getY());
    }
    
    public void testSetZ()
    {
        PlaneDef pd = new PlaneDef(PlaneDef.XY, 0);
        try {
            pd.setZ(-1);
            fail("Shouldn't accept negative index.");
        } catch (IllegalArgumentException iae) {
            //Ok, expected.
        }
        assertEquals("Shouldn't have set a negative index.", 0, pd.getZ());
        
        pd = new PlaneDef(PlaneDef.XZ, 0);
        try {
            pd.setZ(1);
            fail("Should refuse to set z index if not XY plane.");
        } catch (IllegalArgumentException iae) {
            //Ok, expected.
        }
        assertEquals("Shouldn't have set the index.", 0, pd.getZ());
    }
    
    public void testEqualsXY()
    {
        PlaneDef pd = new PlaneDef(PlaneDef.XY, 0);
        assertFalse("Should never be equal to null.", pd.equals(null));
        assertFalse("Should never be equal to a different type.", 
                pd.equals(new Object()));
        PlaneDef pd2 = new PlaneDef(PlaneDef.XZ, 0);
        assertFalse("Should never be equal if different slice.", 
                pd.equals(pd2));
        pd2 = new PlaneDef(PlaneDef.ZY, 0);
        assertFalse("Should never be equal if different slice.", 
                pd.equals(pd2));
        pd2 = new PlaneDef(PlaneDef.XY, 1);
        assertFalse("Should never be equal if different timepoint.", 
                pd.equals(pd2));
        pd2 = new PlaneDef(PlaneDef.XY, 0);
        pd2.setZ(1);
        assertFalse("Should never be equal if different z-index.", 
                pd.equals(pd2));
        pd2.setZ(0);
        assertTrue("Object identity should never matter.", 
                pd.equals(pd2));
    }
    
    public void testEqualsXZ()
    {
        PlaneDef pd = new PlaneDef(PlaneDef.XZ, 0);
        assertFalse("Should never be equal to null.", pd.equals(null));
        assertFalse("Should never be equal to a different type.", 
                pd.equals(new Object()));
        PlaneDef pd2 = new PlaneDef(PlaneDef.XY, 0);
        assertFalse("Should never be equal if different slice.", 
                pd.equals(pd2));
        pd2 = new PlaneDef(PlaneDef.ZY, 0);
        assertFalse("Should never be equal if different slice.", 
                pd.equals(pd2));
        pd2 = new PlaneDef(PlaneDef.XZ, 1);
        assertFalse("Should never be equal if different timepoint.", 
                pd.equals(pd2));
        pd2 = new PlaneDef(PlaneDef.XZ, 0);
        pd2.setY(1);
        assertFalse("Should never be equal if different y-index.", 
                pd.equals(pd2));
        pd2.setY(0);
        assertTrue("Object identity should never matter.", 
                pd.equals(pd2));
    }
    
    public void testEqualsZY()
    {
        PlaneDef pd = new PlaneDef(PlaneDef.ZY, 0);
        assertFalse("Should never be equal to null.", pd.equals(null));
        assertFalse("Should never be equal to a different type.", 
                pd.equals(new Object()));
        PlaneDef pd2 = new PlaneDef(PlaneDef.XY, 0);
        assertFalse("Should never be equal if different slice.", 
                pd.equals(pd2));
        pd2 = new PlaneDef(PlaneDef.XZ, 0);
        assertFalse("Should never be equal if different slice.", 
                pd.equals(pd2));
        pd2 = new PlaneDef(PlaneDef.ZY, 1);
        assertFalse("Should never be equal if different timepoint.", 
                pd.equals(pd2));
        pd2 = new PlaneDef(PlaneDef.ZY, 0);
        pd2.setX(1);
        assertFalse("Should never be equal if different x-index.", 
                pd.equals(pd2));
        pd2.setX(0);
        assertTrue("Object identity should never matter.", 
                pd.equals(pd2));
    }
    
    public void testHashCode()
    {
        //Hash function is guaranteed not to have collisions if 
        //u, t in [0, 2^15).  We make this interval smaller to
        //avoid out of memory errors, but keep it big enough for
        //all pratical purposes.
        PlaneDef pd;
        Integer hValue;
        Set hashValues = new HashSet(500*100);
        for (int t = 0; t < 500; ++t) {
            for (int z = 0; z < 100; ++z) {
                pd = new PlaneDef(PlaneDef.XY, t);
                pd.setZ(z);
                hValue = new Integer(pd.hashCode());
                assertTrue("Duplicated hash value [z="+z+" , t="+t+"].", 
                        hashValues.add(hValue));
            }
        }
    }
    
}
