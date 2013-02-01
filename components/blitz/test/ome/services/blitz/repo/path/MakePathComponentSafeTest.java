/*
 * Copyright (C) 2012 University of Dundee & Open Microscopy Environment.
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

package ome.services.blitz.repo.path;

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

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

/**
 * A enumeration of operating systems under which tests may be occurring.
 * Some file path tests are platform-specific.
 * @see MakePathComponentSafeTest#requireOperatingSystem
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 4.5
 */
enum OperatingSystem {
	WINDOWS("Microsoft Windows"),
	LINUX("Linux"),
	MAC("Apple Mac OS X");
	
	private final String name;

	OperatingSystem(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return this.name;
	}
}

/**
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 4.5
 */
@Test(groups = {"fs"})
public class MakePathComponentSafeTest {
	private static final MakePathComponentSafe sanitizer = new MakePathComponentSafe();
	
	private static final OperatingSystem os;
	
	static {
		final String osName = System.getProperty("os.name");
		if (osName.startsWith("Windows "))
			os = OperatingSystem.WINDOWS;
		else if (osName.equals("Linux"))
			os = OperatingSystem.LINUX;
		else if (osName.equals("Mac OS X"))
			os = OperatingSystem.MAC;
		else
			os = null;
	}

	/**
	 * If the current operating system differs from the given, then skip the current test.
	 * @param os an operating system
	 */
	private static void requireOperatingSystem(OperatingSystem os) {
		if (os != MakePathComponentSafeTest.os)
			throw new SkipException("test requires " + os + " but this is " + MakePathComponentSafeTest.os);
	}
	
	private static final Set<Integer> codePointsOfTypeControl;
	
	static {
		codePointsOfTypeControl = new HashSet<Integer>();
		for (int codePoint = 0; codePoint < 0x100; codePoint++)
			if (Character.getType(codePoint) == Character.CONTROL)
				codePointsOfTypeControl.add(codePoint);
	}
	
	@Test
	public void testTransformationMatrixLegality() {
		final Set<Integer> unsafeCodePoints = MakePathComponentSafe.transformationMatrix.keySet();
		for (final Integer substitute : MakePathComponentSafe.transformationMatrix.values())
			Assert.assertFalse(unsafeCodePoints.contains(substitute), 
					"character substitutions may not be to unsafe characters");
	}
	
	@Test
	public void testUnsafeCharacterAvoidance() {
		final Set<Integer> unsafeCodePoints = MakePathComponentSafe.transformationMatrix.keySet();
		Assert.assertFalse(unsafeCodePoints.contains(MakePathComponentSafe.safeCharacter),
				"the safe character must not be transformed");
		final Set<String> unsafeStrings = new HashSet<String>();
		unsafeStrings.addAll(MakePathComponentSafe.unsafeNames);
		unsafeStrings.addAll(MakePathComponentSafe.unsafePrefixes);
		unsafeStrings.addAll(MakePathComponentSafe.unsafeSuffixes);
		for (final String unsafeString : unsafeStrings)
			Assert.assertEquals(unsafeString.indexOf(MakePathComponentSafe.safeCharacter), -1,
					"the safe character may not appear in unsafe strings");
	}
	
	@Test
	public void testUnsafeStringCase() {
		final Set<String> unsafeStrings = new HashSet<String>();
		unsafeStrings.addAll(MakePathComponentSafe.unsafeNames);
		unsafeStrings.addAll(MakePathComponentSafe.unsafePrefixes);
		unsafeStrings.addAll(MakePathComponentSafe.unsafeSuffixes);
		for (final String unsafeString : unsafeStrings)
			Assert.assertEquals(unsafeString, unsafeString.toUpperCase(),
					"the unsafe strings should be upper-case");
	}

	/**
	 * Create a temporary directory and return it.
	 * @return a temporary directory
	 * @throws IOException if there is a problem in creating the directory
	 */
	private File createTempDir() throws IOException {
		final File tempDir = File.createTempFile("test-" + getClass().getSimpleName(), null);
		tempDir.delete();
		tempDir.mkdir();
		return tempDir;
	}
	
	/**
	 * Test that the parent directory of the given {@link java.io.File} contains a file of the given name.
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

	@Test
	public void testUnsafeCharacterUnsafetyWindows() throws IOException {
		requireOperatingSystem(OperatingSystem.WINDOWS);
		final File tempDir = createTempDir();
		for (final int unsafeCodePoint : MakePathComponentSafe.transformationMatrix.keySet()) {
			if (codePointsOfTypeControl.contains(unsafeCodePoint))
				/* no point testing, one wants to avoid control characters in filenames whatever the operating system permits */
				continue;
			final String unsafeString = new String(new int[] {unsafeCodePoint}, 0, 1);
			/* don't use the unsafe code point as a prefix or suffix */
			final String unsafeName = "unsafe" + MakePathComponentSafe.safeCharacter + unsafeString +
					MakePathComponentSafe.safeCharacter + "unsafe";
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
		tempDir.delete();
	}
	
	@Test
	public void testSanitizedUnsafeCharacterSafety() throws IOException {
		final File tempDir = createTempDir();
		for (final int safeCodePoint : MakePathComponentSafe.transformationMatrix.keySet()) {
			final String unsafeString = new String(new int[] {safeCodePoint}, 0, 1);
			final String unsafeName = "safe" + MakePathComponentSafe.safeCharacter + unsafeString +
					MakePathComponentSafe.safeCharacter + "safe";
			final String safeName = sanitizer.apply(unsafeName);
			Assert.assertEquals(sanitizer.apply(safeName), safeName,
					"sanitization should not change already-sanitized names");
			final File safeFile = new File(tempDir, safeName);
			testDataStorage(safeFile, safeName);
		}
		tempDir.delete();
	}

	@Test
	public void testSafeCharacterSafety() throws IOException {
		final File tempDir = createTempDir();
		final Set<Integer> safeCodePoints = new HashSet<Integer>();
		safeCodePoints.add(Character.codePointAt(new char[] {MakePathComponentSafe.safeCharacter}, 0));
		safeCodePoints.addAll(MakePathComponentSafe.transformationMatrix.values());
		for (final int safeCodePoint : safeCodePoints) {
			final String safeString = new String(new int[] {safeCodePoint}, 0, 1);
			final String safeName = "safe" + MakePathComponentSafe.safeCharacter + safeString +
					MakePathComponentSafe.safeCharacter + "safe";
			final File safeFile = new File(tempDir, safeName);
			testDataStorage(safeFile, safeName);
		}
		tempDir.delete();
	}

	@Test
	public void testSanitizedUnsafeNameSafety() throws IOException {
		final File tempDir = createTempDir();
		for (final String unsafeName : MakePathComponentSafe.unsafeNames) {
			final String safeName = sanitizer.apply(unsafeName);
			final File safeFile = new File(tempDir, safeName);
			testDataStorage(safeFile, safeName);
		}
		tempDir.delete();
	}
	
	@Test
	public void testSanitizedUnsafePrefixSafety() throws IOException {
		final File tempDir = createTempDir();
		for (final String unsafePrefix : MakePathComponentSafe.unsafePrefixes) {
			final String unsafeName = unsafePrefix + MakePathComponentSafe.safeCharacter + "unsafe";
			final String safeName = sanitizer.apply(unsafeName);
			Assert.assertEquals(sanitizer.apply(safeName), safeName,
					"sanitization should not change already-sanitized names");
			Assert.assertTrue(safeName.endsWith("unsafe"),
					"file path sanitization should preserve safe suffixes");
			final File safeFile = new File(tempDir, safeName);
			testDataStorage(safeFile, safeName);
		}
		tempDir.delete();
	}
	
	@Test
	public void testSanitizedUnsafeSuffixSafety() throws IOException {
		final File tempDir = createTempDir();
		for (final String unsafeSuffix : MakePathComponentSafe.unsafeSuffixes) {
			final String unsafeName = "unsafe" + MakePathComponentSafe.safeCharacter + unsafeSuffix;
			final String safeName = sanitizer.apply(unsafeName);
			Assert.assertEquals(sanitizer.apply(safeName), safeName,
					"sanitization should not change already-sanitized names");
			Assert.assertTrue(safeName.startsWith("unsafe"),
					"file path sanitization should preserve safe prefixes");
			final File safeFile = new File(tempDir, safeName);
			testDataStorage(safeFile, safeName);
		}
		tempDir.delete();
	}
	
	@Test
	public void testSensitiveNameSafety() {
		// Leica OME
		final String sensitiveName = "{Group}GroupData.xml";
		Assert.assertEquals(sanitizer.apply(sensitiveName), sensitiveName,
				"sensitive names should not be changed by sanitization");
	}
}
