/*
 * Copyright (C) 2012 - 2013 University of Dundee & Open Microscopy Environment.
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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import ome.services.blitz.repo.path.FilePathRestrictionInstance;
import ome.services.blitz.repo.path.FilePathRestrictions;
import ome.services.blitz.repo.path.MakePathComponentSafe;
import ome.services.blitz.util.CurrentPlatform;
import omero.util.TempFileManager;

import nl.javadude.assumeng.Assumption;
import nl.javadude.assumeng.AssumptionListener;

import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.0
 */
@Test(groups = {"fs"})
@Listeners(AssumptionListener.class)
public class MakePathComponentSafeTest extends MakePathComponentSafe {
    private static final Set<Integer> codePointsOfTypeControl;

    private static final TempFileManager tempFileManager =
            new TempFileManager("test-" + MakePathComponentSafeTest.class.getSimpleName());

    static {
        codePointsOfTypeControl = new HashSet<Integer>();
        for (int codePoint = 0; codePoint < 0x100; codePoint++)
            if (Character.getType(codePoint) == Character.CONTROL)
                codePointsOfTypeControl.add(codePoint);
    }

    public MakePathComponentSafeTest() {
        super(FilePathRestrictionInstance.getFilePathRestrictions(FilePathRestrictionInstance.values()));
    }

    /**
     * Test that the transformation matrix does not correct characters to unsafe ones.
     */
    @Test
    public void testTransformationMatrixLegality() {
        final Set<Integer> unsafeCodePoints = this.rules.transformationMatrix.keySet();
        for (final Integer substitute : this.rules.transformationMatrix.values())
            Assert.assertFalse(unsafeCodePoints.contains(substitute), 
                    "character substitutions may not be to unsafe characters");
    }

    /**
     * Test that the safe character is not included in any code points or strings deemed to be unsafe.
     * (This is an unnecessarily strong criterion, but it is easily met.)
     */
    @Test
    public void testUnsafeCharacterAvoidance() {
        final Set<Integer> unsafeCodePoints = this.rules.transformationMatrix.keySet();
        Assert.assertFalse(unsafeCodePoints.contains(this.rules.safeCharacter),
                "the safe character must not be transformed");
        final Set<String> unsafeStrings = new HashSet<String>();
        unsafeStrings.addAll(this.rules.unsafeNames);
        unsafeStrings.addAll(this.rules.unsafePrefixes);
        unsafeStrings.addAll(this.rules.unsafeSuffixes);
        for (final String unsafeString : unsafeStrings)
            Assert.assertEquals(unsafeString.indexOf(this.rules.safeCharacter), -1,
                    "the safe character may not appear in unsafe strings");
    }

    /**
     * Test that the unsafe strings for matching are all in upper case,
     * as the caller's string is upper-cased for matching in {@link MakePathComponentSafe#apply}.
     */
    @Test
    public void testUnsafeStringCase() {
        final Set<String> unsafeStrings = new HashSet<String>();
        unsafeStrings.addAll(this.rules.unsafeNames);
        unsafeStrings.addAll(this.rules.unsafePrefixes);
        unsafeStrings.addAll(this.rules.unsafeSuffixes);
        for (final String unsafeString : unsafeStrings)
            Assert.assertEquals(unsafeString, unsafeString.toUpperCase(),
                    "the unsafe strings should be upper-case");
    }

    /**
     * Test that the parent directory of the given {@link File} contains a file of the given name.
     * This is used to detect strange phenomena such as accidental reference to Windows NTFS file streams.
     * @param file a file
     * @param expectedName a filename
     * @return if the file's parent directory contains the filename
     */
    private boolean isFileNameReallyThere(File file, String expectedName) {
        if (!file.isFile())
            throw new IllegalArgumentException("must supply an argument that is actually a file");
        for (final String fileInParent : file.getParentFile().list())
            if (expectedName.equals(fileInParent))
                return true;
        return false;
    }

    /**
     * Create the given file, check that data can be stored in it and retrieved from it,
     * check that a file with the given name exists in the file's directory, then delete it.
     * @param file a file
     * @param name a filename
     * @throws IOException if there was a problem in writing and reading the file
     */
    private void testDataStorage(File file, String name) throws IOException {
        final long testContentsOut = System.nanoTime();
        final long testContentsIn;
        final DataOutputStream out = new DataOutputStream(new FileOutputStream(file));
        out.writeLong(testContentsOut);
        out.close();
        final boolean isFileCreated = isFileNameReallyThere(file, name);
        final DataInputStream in = new DataInputStream(new FileInputStream(file));
        testContentsIn = in.readLong();
        in.close();
        file.delete();
        Assert.assertEquals(testContentsIn, testContentsOut,
                "failed to be able to store data in file named " + name);
        Assert.assertTrue(isFileCreated, 
                "ought to be able to create files named " + name);
    }

    /**
     * Test that data cannot be stored in files named using unsafe characters.
     * @param ruleId the rules for the platform to be tested
     * @throws IOException unexpected
     */
    private void testUnsafeCharacterUnsafety(FilePathRestrictionInstance ruleId) throws IOException {
        final FilePathRestrictions rules = FilePathRestrictionInstance.getFilePathRestrictions(ruleId);
        final File tempDir = tempFileManager.createPath("testUnsafeCharacterUnsafetyWindows", null, true);
        for (final int unsafeCodePoint : rules.transformationMatrix.keySet()) {
            if (codePointsOfTypeControl.contains(unsafeCodePoint))
                /* no point testing, one wants to avoid control characters in filenames whatever the operating system permits */
                continue;
            final String unsafeString = new String(new int[] {unsafeCodePoint}, 0, 1);
            /* don't use the unsafe code point as a prefix or suffix */
            final String unsafeName = "unsafe" + this.rules.safeCharacter + unsafeString +
                    this.rules.safeCharacter + "unsafe";
            final File unsafeFile = new File(tempDir, unsafeName);
            try {
                final OutputStream out = new FileOutputStream(unsafeFile);
                out.close();
                final boolean isFileCreated = isFileNameReallyThere(unsafeFile, unsafeName);
                unsafeFile.delete();
                Assert.assertFalse(isFileCreated, 
                        "ought not to be able to create files whose name contains code point " + unsafeCodePoint);
            } catch (FileNotFoundException e) {
                // expected
            }
        }
        tempFileManager.removePath(tempDir);
    }

    /**
     * On Microsoft Windows, test that data cannot be stored in files named using unsafe characters.
     * @throws IOException unexpected
     */
    @Test
    @Assumption(methods = {"isWindows"}, methodClass = CurrentPlatform.class)
    public void testUnsafeCharacterUnsafetyWindows() throws IOException {
        testUnsafeCharacterUnsafety(FilePathRestrictionInstance.WINDOWS_REQUIRED);
    }

    /**
     * On Linux, test that data cannot be stored in files named using unsafe characters.
     * @throws IOException unexpected
     */
    @Test
    @Assumption(methods = {"isLinux"}, methodClass = CurrentPlatform.class)
    public void testUnsafeCharacterUnsafetyLinux() throws IOException {
        testUnsafeCharacterUnsafety(FilePathRestrictionInstance.UNIX_REQUIRED);
    }

    /**
     * On Apple Mac OS X, test that data cannot be stored in files named using unsafe characters.
     * @throws IOException unexpected
     */
    @Test
    @Assumption(methods = {"isMacOSX"}, methodClass = CurrentPlatform.class)
    public void testUnsafeCharacterUnsafetyMacOSX() throws IOException {
        testUnsafeCharacterUnsafety(FilePathRestrictionInstance.UNIX_REQUIRED);
    }

    /**
     * Test that one of the operating-system-specific tests for the unsafety of unsafe characters
     * did actually execute because the current operating system was actually recognized.
     */
    @Test
    @Assumption(methods = {"isUnknown"}, methodClass = CurrentPlatform.class)
    public void testPlatformTestExecuted() {
        Assert.fail("one of the operating-system-specific tests should have executed");
    }

    /**
     * Test that data can be stored in files named using sanitized unsafe characters.
     * @throws IOException unexpected
     */
    @Test
    public void testSanitizedUnsafeCharacterSafety() throws IOException {
        final File tempDir = tempFileManager.createPath("testSanitizedUnsafeCharacterSafety", null, true);
        for (final int safeCodePoint : this.rules.transformationMatrix.keySet()) {
            final String unsafeString = new String(new int[] {safeCodePoint}, 0, 1);
            final String unsafeName = "safe" + this.rules.safeCharacter + unsafeString +
                    this.rules.safeCharacter + "safe";
            final String safeName = apply(unsafeName);
            Assert.assertEquals(apply(safeName), safeName,
                    "sanitization should not change already-sanitized names");
            final File safeFile = new File(tempDir, safeName);
            testDataStorage(safeFile, safeName);
        }
        tempFileManager.removePath(tempDir);
    }

    /**
     * Test that data can be stored in files named using unsanitized safe characters.
     * @throws IOException unexpected
     */
    @Test
    public void testSafeCharacterSafety() throws IOException {
        final File tempDir = tempFileManager.createPath("testSafeCharacterSafety", null, true);
        final Set<Integer> safeCodePoints = new HashSet<Integer>();
        safeCodePoints.add(Character.codePointAt(new char[] {this.rules.safeCharacter}, 0));
        safeCodePoints.addAll(this.rules.transformationMatrix.values());
        for (final int safeCodePoint : safeCodePoints) {
            final String safeString = new String(new int[] {safeCodePoint}, 0, 1);
            final String safeName = "safe" + this.rules.safeCharacter + safeString +
                    this.rules.safeCharacter + "safe";
            final File safeFile = new File(tempDir, safeName);
            testDataStorage(safeFile, safeName);
        }
        tempFileManager.removePath(tempDir);
    }

    /**
     * Test that data can be stored in files named using sanitized unsafe names.
     * @throws IOException unexpected
     */
    @Test
    public void testSanitizedUnsafeNameSafety() throws IOException {
        final File tempDir = tempFileManager.createPath("testSanitizedUnsafeNameSafety", null, true);
        for (final String unsafeName : this.rules.unsafeNames) {
            final String safeName = apply(unsafeName);
            final File safeFile = new File(tempDir, safeName);
            testDataStorage(safeFile, safeName);
        }
        tempFileManager.removePath(tempDir);
    }

    /**
     * Test that data can be stored in files named using sanitized unsafe prefixes.
     * @throws IOException unexpected
     */
    @Test
    public void testSanitizedUnsafePrefixSafety() throws IOException {
        final File tempDir = tempFileManager.createPath("testSanitizedUnsafePrefixSafety", null, true);
        for (final String unsafePrefix : this.rules.unsafePrefixes) {
            final String unsafeName = unsafePrefix + this.rules.safeCharacter + "unsafe";
            final String safeName = apply(unsafeName);
            Assert.assertEquals(apply(safeName), safeName,
                    "sanitization should not change already-sanitized names");
            Assert.assertTrue(safeName.endsWith("unsafe"),
                    "file path sanitization should preserve safe suffixes");
            final File safeFile = new File(tempDir, safeName);
            testDataStorage(safeFile, safeName);
        }
        tempFileManager.removePath(tempDir);
    }

    /**
     * Test that data can be stored in files named using sanitized unsafe suffixes.
     * @throws IOException unexpected
     */
    @Test
    public void testSanitizedUnsafeSuffixSafety() throws IOException {
        final File tempDir = tempFileManager.createPath("testSanitizedUnsafeSuffixSafety", null, true);
        for (final String unsafeSuffix : this.rules.unsafeSuffixes) {
            final String unsafeName = "unsafe" + this.rules.safeCharacter + unsafeSuffix;
            final String safeName = apply(unsafeName);
            Assert.assertEquals(apply(safeName), safeName,
                    "sanitization should not change already-sanitized names");
            Assert.assertTrue(safeName.startsWith("unsafe"),
                    "file path sanitization should preserve safe prefixes");
            final File safeFile = new File(tempDir, safeName);
            testDataStorage(safeFile, safeName);
        }
        tempFileManager.removePath(tempDir);
    }

    /**
     * Test that safely named files are not renamed in name sanitization.
     * Checks that those names are preserved upon which BioFormats depends.
     * @throws IOException unexpected
     */
    @Test
    public void testSensitiveNameSafety() {
        // Leica OME
        final String sensitiveName = "{Group}GroupData.xml";
        Assert.assertEquals(apply(sensitiveName), sensitiveName,
                "sensitive names should not be changed by sanitization");
    }
}
