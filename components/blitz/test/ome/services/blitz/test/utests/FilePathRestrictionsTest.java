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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ome.services.blitz.repo.path.FilePathRestrictionInstance;
import ome.services.blitz.repo.path.FilePathRestrictions;
import ome.services.blitz.util.CurrentPlatform;

import nl.javadude.assumeng.Assumption;
import nl.javadude.assumeng.AssumptionListener;

import org.apache.commons.collections.CollectionUtils;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;

/**
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.0
 */

@Test(groups = { "fs" })
@Listeners(AssumptionListener.class)
public class FilePathRestrictionsTest {

    /**
     * Test that an empty rule set cannot be combined.
     */
    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testCombineNoRules() {
        FilePathRestrictions.combineFilePathRestrictions();
    }

    /**
     * Test that one may create rule sets using mostly nulls.
     */
    @Test
    public void testNullSafety() {
        FilePathRestrictions.combineFilePathRestrictions(
                new FilePathRestrictions(null, null, null, null, ImmutableSet.of('A')));
    }

    /**
     * Test that rule sets may be combined if they have safe characters in common.
     */
    @Test
    public void testCombineSafeCharacters() {
        FilePathRestrictions.combineFilePathRestrictions(
                new FilePathRestrictions(null, null, null, null, ImmutableSet.of('A', 'B')),
                new FilePathRestrictions(null, null, null, null, ImmutableSet.of('B', 'C')));
    }

    /**
     * Test that rule sets may not be combined if they do not have safe characters in common.
     */
    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testCombineNoSafeCharacters() {
        FilePathRestrictions.combineFilePathRestrictions(
                new FilePathRestrictions(null, null, null, null, ImmutableSet.of('A', 'B')),
                new FilePathRestrictions(null, null, null, null, ImmutableSet.of('C', 'D')));
    }

    /**
     * Test that rule sets may be combined if their mappings have safe characters in common.
     */
    @Test
    public void testCombineTransformation() {
        final SetMultimap<Integer, Integer> transformationMatrixX = HashMultimap.create();
        transformationMatrixX.put(0, 65);
        transformationMatrixX.put(0, 66);
        final SetMultimap<Integer, Integer> transformationMatrixY = HashMultimap.create();
        transformationMatrixY.put(0, 66);
        transformationMatrixY.put(0, 67);
        FilePathRestrictions.combineFilePathRestrictions(
                new FilePathRestrictions(transformationMatrixX, null, null, null, ImmutableSet.of('A')),
                new FilePathRestrictions(transformationMatrixY, null, null, null, ImmutableSet.of('A')));
    }

    /**
     * Test that rule sets may not be combined if their mappings have only unsafe characters in common.
     */
    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testCombineUnsafeTransformation() {
        final SetMultimap<Integer, Integer> transformationMatrixX = HashMultimap.create();
        transformationMatrixX.put(0, 65);
        transformationMatrixX.put(0, 1);
        final SetMultimap<Integer, Integer> transformationMatrixY = HashMultimap.create();
        transformationMatrixY.put(0, 1);
        transformationMatrixY.put(0, 66);
        FilePathRestrictions.combineFilePathRestrictions(
                new FilePathRestrictions(transformationMatrixX, null, null, null, ImmutableSet.of('A')),
                new FilePathRestrictions(transformationMatrixY, null, null, null, ImmutableSet.of('A')));
    }

    /**
     * Test that rule sets may not be combined if their mappings have no safe characters in common.
     */
    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testCombineNoTransformation() {
        final SetMultimap<Integer, Integer> transformationMatrixX = HashMultimap.create();
        transformationMatrixX.put(0, 65);
        transformationMatrixX.put(0, 66);
        final SetMultimap<Integer, Integer> transformationMatrixY = HashMultimap.create();
        transformationMatrixY.put(0, 67);
        transformationMatrixY.put(0, 68);
        FilePathRestrictions.combineFilePathRestrictions(
                new FilePathRestrictions(transformationMatrixX, null, null, null, ImmutableSet.of('A')),
                new FilePathRestrictions(transformationMatrixY, null, null, null, ImmutableSet.of('A')));
    }

    /**
     * Assert that an actual multimap is as expected regardless of order.
     * @param actual the actual value
     * @param expected the expected value
     */
    private void assertEqualMultimaps(Multimap<Integer, Integer> actual, Multimap<Integer, Integer> expected) {
        Assert.assertTrue(CollectionUtils.isEqualCollection(actual.keySet(), expected.keySet()));
        for (final Integer key : expected.keySet()) {
            Assert.assertTrue(CollectionUtils.isEqualCollection(actual.get(key), expected.get(key)));
        }
    }

    /**
     * Test that two complex sets of rules combined as expected.
     * (On a rainy day this test could be broken up into several smaller tests.)
     */
    @Test
    public void testCombineRules() {
        /* these variables define the X set of rules to combine */

        final SetMultimap<Integer, Integer> transformationMatrixX = HashMultimap.create();
        final Set<String> unsafePrefixesX = new HashSet<String>();
        final Set<String> unsafeSuffixesX = new HashSet<String>();
        final Set<String> unsafeNamesX = new HashSet<String>();
        final Set<Character> safeCharactersX = new HashSet<Character>();

        /* these variables define the Y set of rules to combine */

        final SetMultimap<Integer, Integer> transformationMatrixY = HashMultimap.create();
        final Set<String> unsafePrefixesY = new HashSet<String>();
        final Set<String> unsafeSuffixesY = new HashSet<String>();
        final Set<String> unsafeNamesY = new HashSet<String>();
        final Set<Character> safeCharactersY = new HashSet<Character>();

        /* these variables define the expected result of combining X and Y */

        final SetMultimap<Integer, Integer> transformationMatrixXY = HashMultimap.create();
        final Set<String> unsafePrefixesXY = new HashSet<String>();
        final Set<String> unsafeSuffixesXY = new HashSet<String>();
        final Set<String> unsafeNamesXY = new HashSet<String>();
        final Set<Character> safeCharactersXY = new HashSet<Character>();

        /* automatically map control characters to the safe characters;
         * we will remove and replace any that are to be tested specially */

        for (int codePoint = 0; codePoint < 0x100; codePoint++) {
            if (Character.getType(codePoint) == Character.CONTROL) {
                transformationMatrixXY.put(codePoint, 65);
            }
        }

        /* choose four control characters and remove them from the transformation matrix */

        final Iterator<Integer> controlCodePointIterator = transformationMatrixXY.keySet().iterator();
        final int controlCharacterP = controlCodePointIterator.next();
        final int controlCharacterQ = controlCodePointIterator.next();
        final int controlCharacterR = controlCodePointIterator.next();
        final int controlCharacterS = controlCodePointIterator.next();

        transformationMatrixXY.removeAll(controlCharacterP);
        transformationMatrixXY.removeAll(controlCharacterQ);
        transformationMatrixXY.removeAll(controlCharacterR);
        transformationMatrixXY.removeAll(controlCharacterS);

        /* set up test case for combining control character mappings */

        transformationMatrixX.put(controlCharacterP, 65);
        transformationMatrixX.put(controlCharacterP, 67);
        transformationMatrixX.put(controlCharacterQ, 65);
        transformationMatrixX.put(controlCharacterQ, 66);
        transformationMatrixX.put(controlCharacterR, 66);

        transformationMatrixY.put(controlCharacterQ, 66);
        transformationMatrixY.put(controlCharacterR, 66);
        transformationMatrixY.put(controlCharacterS, 68);

        transformationMatrixXY.put(controlCharacterP, 65);
        transformationMatrixXY.put(controlCharacterP, 67);
        transformationMatrixXY.put(controlCharacterQ, 66);
        transformationMatrixXY.put(controlCharacterR, 66);
        transformationMatrixXY.put(controlCharacterS, 68);

        /* choose four non-control characters and remove them from the transformation matrix */

        int[] normalCodePoints = new int[4];
        int index = 0;
        int codePoint = 0;
        while (index < normalCodePoints.length) {
            if (Character.getType(codePoint) != Character.CONTROL) {
                normalCodePoints[index++] = codePoint;
                transformationMatrixXY.removeAll(codePoint);
            }
            codePoint++;
        }
        int normalCharacterP = normalCodePoints[0];
        int normalCharacterQ = normalCodePoints[1];
        int normalCharacterR = normalCodePoints[2];
        int normalCharacterS = normalCodePoints[3];

        /* set up test case for combining non-control character mappings */

        transformationMatrixX.put(normalCharacterP, 65);
        transformationMatrixX.put(normalCharacterP, 67);
        transformationMatrixX.put(normalCharacterQ, 65);
        transformationMatrixX.put(normalCharacterQ, 66);
        transformationMatrixX.put(normalCharacterR, 66);

        transformationMatrixY.put(normalCharacterQ, 66);
        transformationMatrixY.put(normalCharacterR, 66);
        transformationMatrixY.put(normalCharacterS, 68);

        transformationMatrixXY.put(normalCharacterP, 65);
        transformationMatrixXY.put(normalCharacterP, 67);
        transformationMatrixXY.put(normalCharacterQ, 66);
        transformationMatrixXY.put(normalCharacterR, 66);
        transformationMatrixXY.put(normalCharacterS, 68);

        /* set up test cases for combining proscribed strings */

        unsafePrefixesX.add("XP");
        unsafePrefixesX.add("YP");

        unsafePrefixesY.add("YP");
        unsafePrefixesY.add("ZP");

        unsafePrefixesXY.add("XP");
        unsafePrefixesXY.add("YP");
        unsafePrefixesXY.add("ZP");

        unsafeSuffixesX.add("XS");
        unsafeSuffixesX.add("YS");

        unsafeSuffixesY.add("YS");
        unsafeSuffixesY.add("ZS");

        unsafeSuffixesXY.add("XS");
        unsafeSuffixesXY.add("YS");
        unsafeSuffixesXY.add("ZS");

        unsafeNamesX.add("XN");
        unsafeNamesX.add("YN");

        unsafeNamesY.add("YN");
        unsafeNamesY.add("ZN");

        unsafeNamesXY.add("XN");
        unsafeNamesXY.add("YN");
        unsafeNamesXY.add("ZN");

        /* set up test case for combining safe characters */

        safeCharactersX.add('A');
        safeCharactersX.add('B');

        safeCharactersY.add('A');

        safeCharactersXY.add('A');

        /* perform the combination */

        final FilePathRestrictions rulesX =
                new FilePathRestrictions(transformationMatrixX, unsafePrefixesX, unsafeSuffixesX, unsafeNamesX, safeCharactersX);
        final FilePathRestrictions rulesY =
                new FilePathRestrictions(transformationMatrixY, unsafePrefixesY, unsafeSuffixesY, unsafeNamesY, safeCharactersY);
        final FilePathRestrictions rulesXY =
                FilePathRestrictions.combineFilePathRestrictions(rulesX, rulesY);

        /* test that the combination is as expected in all respects */

        Assert.assertTrue(CollectionUtils.isEqualCollection(rulesXY.safeCharacters, safeCharactersXY));
        Assert.assertTrue(CollectionUtils.isEqualCollection(rulesXY.unsafePrefixes, unsafePrefixesXY));
        Assert.assertTrue(CollectionUtils.isEqualCollection(rulesXY.unsafeSuffixes, unsafeSuffixesXY));
        Assert.assertTrue(CollectionUtils.isEqualCollection(rulesXY.unsafeNames, unsafeNamesXY));
        assertEqualMultimaps(rulesXY.transformationMatrix, transformationMatrixXY);

        /* given a mapping choice, prefer the safe character */

        Assert.assertEquals((int) rulesXY.transformationMap.get(controlCharacterP), 65);
        Assert.assertEquals((int) rulesXY.transformationMap.get(normalCharacterP), 65);
    }

    /**
     * Test that transformation matrices are transitively closed upon combination.
     */
    @Test
    public void testTransitiveTransformationClosure() {
        final SetMultimap<Integer, Integer> transformationMatrixX = HashMultimap.create();
        final SetMultimap<Integer, Integer> transformationMatrixY = HashMultimap.create();
        final SetMultimap<Integer, Integer> transformationMatrixZ = HashMultimap.create();
        final SetMultimap<Integer, Integer> transformationMatrixXYZ = HashMultimap.create();

        for (int codePoint = 0; codePoint < 0x100; codePoint++) {
            if (Character.getType(codePoint) == Character.CONTROL) {
                transformationMatrixXYZ.put(codePoint, 90);
            }
        }

        /*
         * 65 → 66
         *    ↘ ↓  ↘
         *      67   68
         *      ↓    ↓
         *      70   69
         */

        transformationMatrixX.put(65, 66);
        transformationMatrixX.put(65, 67);

        transformationMatrixY.put(66, 67);
        transformationMatrixY.put(66, 68);

        transformationMatrixZ.put(67, 70);
        transformationMatrixZ.put(68, 69);

        transformationMatrixXYZ.put(65, 69);
        transformationMatrixXYZ.put(65, 70);
        transformationMatrixXYZ.put(66, 69);
        transformationMatrixXYZ.put(66, 70);
        transformationMatrixXYZ.put(67, 70);
        transformationMatrixXYZ.put(68, 69);

        final Set<Character> safeCharacters = ImmutableSet.of('Z');

        final FilePathRestrictions rulesX =
                new FilePathRestrictions(transformationMatrixX, null, null, null, safeCharacters);
        final FilePathRestrictions rulesY =
                new FilePathRestrictions(transformationMatrixY, null, null, null, safeCharacters);
        final FilePathRestrictions rulesZ =
                new FilePathRestrictions(transformationMatrixZ, null, null, null, safeCharacters);

        final FilePathRestrictions rulesXYZ =
                FilePathRestrictions.combineFilePathRestrictions(rulesX, rulesY, rulesZ);
        assertEqualMultimaps(rulesXYZ.transformationMatrix, transformationMatrixXYZ);

        final FilePathRestrictions rulesZYX =
                FilePathRestrictions.combineFilePathRestrictions(rulesZ, rulesY, rulesX);
        assertEqualMultimaps(rulesZYX.transformationMatrix, transformationMatrixXYZ);
    }

    /**
     * Test that cyclic transformation matrices do not cause an infinite loop.
     */
    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testCyclicTransformationCombination() {
        final SetMultimap<Integer, Integer> transformationMatrix = HashMultimap.create();
        transformationMatrix.put(0, 0);

        final FilePathRestrictions rules =
                new FilePathRestrictions(transformationMatrix, null, null, null, ImmutableSet.of('A'));
        FilePathRestrictions.combineFilePathRestrictions(rules); 
    }

    /**
     * Assert that collections contain exactly the same elements, regardless of ordering.
     * @param actual the actual elements
     * @param expected the expected elements
     */
    private static <X> void assertEqualsNoOrder(Collection<X> actual, Collection<X> expected) {
        final HashSet<X> remaining = new HashSet<X>(actual);
        for (final X expectedItem : expected) {
            Assert.assertTrue(remaining.remove(expectedItem), "collections must contain the same elements");
        }
        Assert.assertTrue(remaining.isEmpty(), "collections must contain the same elements");
    }

    /**
     * Assert that file path restriction rules are identical in effect.
     * @param actual the actual rules
     * @param expected the expected rules
     */
    private static void assertSameRules(FilePathRestrictions actual, FilePathRestrictions expected) {
        assertEqualsNoOrder(actual.transformationMatrix.entries(), expected.transformationMatrix.entries());
        assertEqualsNoOrder(actual.unsafePrefixes, expected.unsafePrefixes);
        assertEqualsNoOrder(actual.unsafeSuffixes, expected.unsafeSuffixes);
        assertEqualsNoOrder(actual.unsafeNames, expected.unsafeNames);
        assertEqualsNoOrder(actual.safeCharacters, expected.safeCharacters);
        Assert.assertEquals(actual.safeCharacter, expected.safeCharacter);
        assertEqualsNoOrder(actual.transformationMap.entrySet(), expected.transformationMap.entrySet());
    }

    /**
     * On Microsoft Windows, test that the applicable rules are those for Microsoft Windows.
     */
    @Test
    @Assumption(methods = {"isWindows"}, methodClass = CurrentPlatform.class)
    public void testUnsafeCharacterUnsafetyWindows() {
        assertSameRules(FilePathRestrictionInstance.getFilePathRestrictions(FilePathRestrictionInstance.LOCAL_REQUIRED),
                        FilePathRestrictionInstance.getFilePathRestrictions(FilePathRestrictionInstance.WINDOWS_REQUIRED));
        assertSameRules(FilePathRestrictionInstance.getFilePathRestrictions(FilePathRestrictionInstance.LOCAL_OPTIONAL),
                        FilePathRestrictionInstance.getFilePathRestrictions(FilePathRestrictionInstance.WINDOWS_OPTIONAL));
    }

    /**
     * On Linux, test that the applicable rules are those for UNIX-like platforms.
     */
    @Test
    @Assumption(methods = {"isLinux"}, methodClass = CurrentPlatform.class)
    public void testUnsafeCharacterUnsafetyLinux() {
        assertSameRules(FilePathRestrictionInstance.getFilePathRestrictions(FilePathRestrictionInstance.LOCAL_REQUIRED),
                        FilePathRestrictionInstance.getFilePathRestrictions(FilePathRestrictionInstance.UNIX_REQUIRED));
        assertSameRules(FilePathRestrictionInstance.getFilePathRestrictions(FilePathRestrictionInstance.LOCAL_OPTIONAL),
                        FilePathRestrictionInstance.getFilePathRestrictions(FilePathRestrictionInstance.UNIX_OPTIONAL));
    }

    /**
     * On Apple Mac OS X, test that the applicable rules are those for UNIX-like platforms.
     */
    @Test
    @Assumption(methods = {"isMacOSX"}, methodClass = CurrentPlatform.class)
    public void testUnsafeCharacterUnsafetyMacOSX() {
        assertSameRules(FilePathRestrictionInstance.getFilePathRestrictions(FilePathRestrictionInstance.LOCAL_REQUIRED),
                        FilePathRestrictionInstance.getFilePathRestrictions(FilePathRestrictionInstance.UNIX_REQUIRED));
        assertSameRules(FilePathRestrictionInstance.getFilePathRestrictions(FilePathRestrictionInstance.LOCAL_OPTIONAL),
                        FilePathRestrictionInstance.getFilePathRestrictions(FilePathRestrictionInstance.UNIX_OPTIONAL));
    }
}
