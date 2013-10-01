/*
 * org.openmicroscopy.shoola.util.ui.TestUIUtilities
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2013 Glencoe Software, Inc.
 *  All rights reserved.
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

package org.openmicroscopy.shoola.util.ui;


//Java imports

//Third-party libraries
import junit.framework.TestCase;

//Application-internal dependencies

/**
 * Verifies that {@link UIUtilities} methods operate correctly.
 * @author  Chris Allan &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:callan@glencoesoftware.com">
 * 				    callan@glencoesoftware.com</a>
 * @version 4.4.8
 * @since 4.4.8
 */
public class TestUIUtilities extends TestCase
{

    public void testFindDecimalNegative()
    {
        assertEquals(3, UIUtilities.findDecimal(-0.004, 1));
        assertEquals(2, UIUtilities.findDecimal(-0.005, 1));
        assertEquals(2, UIUtilities.findDecimal(-0.04, 1));
        assertEquals(1, UIUtilities.findDecimal(-0.05, 1));
        assertEquals(1, UIUtilities.findDecimal(-0.5, 1));
        assertEquals(1, UIUtilities.findDecimal(-0.4, 1));
        assertEquals(1, UIUtilities.findDecimal(-1.0, 1));
    }

    public void testFindDecimalPositive()
    {
        assertEquals(3, UIUtilities.findDecimal(0.004, 1));
        assertEquals(2, UIUtilities.findDecimal(0.005, 1));
        assertEquals(2, UIUtilities.findDecimal(0.04, 1));
        assertEquals(1, UIUtilities.findDecimal(0.05, 1));
        assertEquals(1, UIUtilities.findDecimal(0.5, 1));
        assertEquals(1, UIUtilities.findDecimal(0.4, 1));
        assertEquals(1, UIUtilities.findDecimal(1.0, 1));
    }
}
