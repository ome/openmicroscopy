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

package ome.services.blitz.test.utests;

import java.util.HashSet;
import java.util.Set;

import ome.services.blitz.repo.path.FilePathNamingValidator;
import omero.FilePathNamingException;

import org.apache.commons.collections.CollectionUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.0
 */
@Test(groups = {"fs"})
public class FilePathNamingValidatorTest extends FilePathTransformerTestBase {
    /**
     * Test that file path naming validation accepts legal names.
     * @throws FilePathNamingException unexpected
     */
    @Test
    public void testValidFilePath() throws FilePathNamingException {
        final FilePathNamingValidator validator = new FilePathNamingValidator(this.conservativeRules);
        validator.validateFilePathNaming("C;/Foo1._/_nUl.txt/coM5_/_$bar/_.[]._/óß€Åæ");
    }

    /**
     * Test that file path naming validation flags the correct proscriptions.
     * @throws FilePathNamingException unexpected
     */
    @Test
    public void testInvalidFilePath() throws FilePathNamingException {
        final String illegalFilePath = "C:/Foo1./nUl.txt/coM5/$bar/.<>.";
        final Set<Integer> illegalCodePoints = new HashSet<Integer>();
        final Set<String> illegalPrefixes = new HashSet<String>();
        final Set<String> illegalSuffixes = new HashSet<String>();
        final Set<String> illegalNames = new HashSet<String>();

        illegalCodePoints.add(0x3A);  // :
        illegalCodePoints.add(0x3C);  // [
        illegalCodePoints.add(0x3E);  // ]

        illegalPrefixes.add("NUL.");
        illegalPrefixes.add("$");
        illegalPrefixes.add(".");
        illegalSuffixes.add(".");
        illegalNames.add("COM5");

        final FilePathNamingValidator validator = new FilePathNamingValidator(this.conservativeRules);
        try {
            validator.validateFilePathNaming(illegalFilePath);
            Assert.fail("illegal file path passed name validation");
        } catch (FilePathNamingException fpne) {
            Assert.assertEquals(fpne.illegalFilePath, illegalFilePath);
            Assert.assertTrue(CollectionUtils.isEqualCollection(fpne.illegalCodePoints, illegalCodePoints));
            Assert.assertTrue(CollectionUtils.isEqualCollection(fpne.illegalPrefixes, illegalPrefixes));
            Assert.assertTrue(CollectionUtils.isEqualCollection(fpne.illegalSuffixes, illegalSuffixes));
            Assert.assertTrue(CollectionUtils.isEqualCollection(fpne.illegalNames, illegalNames));
        }
    }
}
