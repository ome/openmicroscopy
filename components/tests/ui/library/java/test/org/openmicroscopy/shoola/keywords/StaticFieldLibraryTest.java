/*
 * Copyright (C) 2013 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.openmicroscopy.shoola.keywords;

import java.awt.Color;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests a Robot Framework SwingLibrary keyword library.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 4.4.9
 */
public class StaticFieldLibraryTest {
    /** test color constant for use by {@link #testExpectedColorConstant()} */
    public static final Color COLOR = new Color(0x11, 0x22, 0x33);

    /**
     * Test that a static field's String value is correctly retrieved.
     * @throws ClassNotFoundException unexpected
     * @throws IllegalAccessException unexpected
     * @throws NoSuchFieldException unexpected
     */
    @Test
    public void testExpectedStringConstant() throws ClassNotFoundException, IllegalAccessException, NoSuchFieldException {
        final String expected = StaticFieldLibrary.PREFIX;
        final String actual = new StaticFieldLibrary().getJavaString("keywords.StaticFieldLibrary.PREFIX");
        Assert.assertEquals(actual, expected);
    }

    /**
     * Test that a missing constant is properly handled.
     * @throws ClassNotFoundException unexpected
     * @throws IllegalAccessException unexpected
     * @throws NoSuchFieldException expected
     */
    @Test(expectedExceptions = NoSuchFieldException.class)
    public void testMissingField() throws ClassNotFoundException, IllegalAccessException, NoSuchFieldException {
        new StaticFieldLibrary().getJavaString("keywords.StaticFieldLibrary.PREFIX_MISSING");
    }

    /**
     * Test that a missing class is properly handled.
     * @throws ClassNotFoundException expected
     * @throws IllegalAccessException unexpected
     * @throws NoSuchFieldException unexpected
     */
    @Test(expectedExceptions = ClassNotFoundException.class)
    public void testMissingClass() throws ClassNotFoundException, IllegalAccessException, NoSuchFieldException {
        new StaticFieldLibrary().getJavaString("keywords.StaticFieldLibraryMissing.PREFIX");
    }

    /**
     * Test that a static field's Color value is correctly retrieved.
     * @throws ClassNotFoundException unexpected
     * @throws IllegalAccessException unexpected
     * @throws NoSuchFieldException unexpected
     */
    @Test
    public void testExpectedColorConstant() throws ClassNotFoundException, IllegalAccessException, NoSuchFieldException {
        final String expected = "ff112233";
        final String actual = new StaticFieldLibrary().getAWTColor("keywords.StaticFieldLibraryTest.COLOR");
        Assert.assertEquals(actual, expected);
    }
}
