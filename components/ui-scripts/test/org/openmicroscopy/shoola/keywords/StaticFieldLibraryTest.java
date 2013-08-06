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

import org.testng.annotations.Test;

import junit.framework.Assert;

/**
 * Tests a Robot Framework SwingLibrary keyword library.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 4.4.9
 */
public class StaticFieldLibraryTest {
    /**
     * Test that a static field's value is correctly retrieved.
     * @throws ClassNotFoundException unexpected
     * @throws IllegalAccessException unexpected
     * @throws NoSuchFieldException unexpected
     */
    @Test
    public void testExpectedConstant() throws ClassNotFoundException, IllegalAccessException, NoSuchFieldException {
        final String expected = StaticFieldLibrary.PREFIX;
        final String actual = new StaticFieldLibrary().getJavaString("keywords.StaticFieldLibrary.PREFIX");
        Assert.assertEquals(expected, actual);
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
}
