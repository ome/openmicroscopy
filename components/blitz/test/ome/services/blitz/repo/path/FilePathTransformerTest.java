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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ome.services.blitz.repo.path.FilePathTransformer;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 4.5
 */
@Test(groups = {"fs"})
public class FilePathTransformerTest {
	private FilePathTransformer fpt;
	
	/* TODO: use Spring to create a proper mock test application context, and increase code coverage */
	@BeforeClass
	public void mockSpring() throws IOException {
		final File tempFile = File.createTempFile("test-" + getClass().getSimpleName(), null);
		final File tempDir = tempFile.getParentFile();
		tempFile.delete();
		tempFile.mkdir();
		this.fpt = new FilePathTransformer();
		this.fpt.setPathSanitizer(new MakePathComponentSafe());
		this.fpt.setOmeroDataDir(tempDir.getAbsolutePath());
		this.fpt.setFsSubDir(tempFile.getName());
		this.fpt.calculateBaseDir();
	}
	
	@Test
	public void testLegalityOfEncodedStrings() throws UnsupportedEncodingException {
		final String testPhrase = "The red kipper flies at midnight.";
		final FsFile originalFile = new FsFile(testPhrase);
		final FsFile recreatedFile = new FsFile(originalFile.getComponents());
		Assert.assertEquals(recreatedFile, originalFile,
					"server-local path components may contain only legal bytes");
	}
	
	@Test
	public void testServerPathConversion() throws IOException {
		final FsFile repositoryPath = new FsFile("wibble/wobble/|]{~`\u00B1\u00A7/\u00F3\u00DF\u20AC\u00C5\u00E6");
		final File serverPath = fpt.getServerFileFromFsFile(repositoryPath);
		Assert.assertEquals(fpt.getFsFileFromServerFile(serverPath), repositoryPath,
				"conversion from legal repository paths to server-local paths and back must return the original");
	}
	
	/**
	 * Get the absolute path of the root directory above the current directory.
	 * Assumes that the current directory is named <q><code>.</code></q>.
	 * @return the root directory
	 */
	private static File getRootDir() {
		File dir = new File(".").getAbsoluteFile();
		do {
			final File parent = dir.getParentFile();
			if (parent == null)
				return dir;
			dir = parent;
		} while (true);
	}
	
	/**
	 * Converts the path components to a corresponding absolute File.
	 * @param components path components
	 * @return the corresponding File
	 */
	private File componentsToFile(String... components) {
		File file = getRootDir();
		for (final String component : components)
			file = new File(file, component);
		return file;
	}
	
	@Test
	public void testConstructionFromStringPath() {
		final FsFile file = new FsFile("a/b/c");
		final List<String> expected = new ArrayList<String>();
		expected.add("a");
		expected.add("b");
		expected.add("c");
		Assert.assertEquals(file.getComponents(), expected,
				"construction from delimited path components should work");
	};
	
	@Test
	public void testClientPathToRepository() throws IOException {
		final File clientPath = componentsToFile("Batteries", "not", "included");
		Assert.assertEquals(fpt.getFsFileFromClientFile(clientPath, 3), new FsFile("Batteries/not/included"),
				"unexpected result in converting from client path to repository path");
		Assert.assertEquals(fpt.getFsFileFromClientFile(clientPath, 2), new FsFile("not/included"),
				"unexpected result in converting from client path to repository path");
		Assert.assertEquals(fpt.getFsFileFromClientFile(clientPath, 1), new FsFile("included"),
				"unexpected result in converting from client path to repository path");
	}
	
	@Test
	public void testGetMinimumDepthImpossible() throws IOException {
		final File clientPath1 = componentsToFile("Batteries", "not", "included");
		final File clientPath2 = componentsToFile("Batteries", "not", "included");
		final File clientPath3 = componentsToFile("Batteries", "are", "included");
		fpt.getMinimumDepth(Arrays.asList(clientPath1, clientPath3));
		try {
		 fpt.getMinimumDepth(Arrays.asList(clientPath1, clientPath2, clientPath3));
		 Assert.fail("getMinimumDepth must not give a result for file sets whose elements cannot be made distinguishable");
		} catch (IllegalArgumentException e) { }
	}
	
	/**
	 * Test that the minimum depth for a set of paths is as expected.
	 * @param expectedDepth the expected minimum depth
	 * @param paths a set of paths, each String being a path, each character being a path component
	 * @throws IOException unexpected, test fails
	 */
	private void testMinimumDepth(int expectedDepth, String... paths) throws IOException {
		final Collection<File> files = new ArrayList<File>(paths.length);
		for (final String path : paths) {
			final char[] componentChars = new char[path.length()];
			path.getChars(0, path.length(), componentChars, 0);
			final String[] componentStrings = new String[path.length()];
			for (int i = 0; i < componentChars.length; i++)
				componentStrings[i] = Character.toString(componentChars[i]);
			files.add(componentsToFile(componentStrings));
		}
		Assert.assertEquals(fpt.getMinimumDepth(files), expectedDepth,
				"unexpected result for minimum path depth for files");
	}
	
	/**
	 * Test that the given components, when sanitized, become the given repository path,
	 * and that a path of that name can be used for data storage on the local filesystem.
	 * @param fsPath the expected repository path for the path components
	 * @param components path components to be sanitized into a repository path
	 * @throws IOException unexpected
	 */
	private void testClientPath(String fsPath, String... components) throws IOException {
		final FsFile rootFile = fpt.getFsFileFromClientFile(componentsToFile(), Integer.MAX_VALUE);
		final FsFile fsFile = fpt.getFsFileFromClientFile(componentsToFile(components), Integer.MAX_VALUE);
		Assert.assertEquals(fsFile.getPathFrom(rootFile).toString(), fsPath,
				"client-side file path components do not assemble to form the expected repository path");
		final File serverFile = fpt.getServerFileFromFsFile(fsFile);
		final FleetingDirectory serverDir = new FleetingDirectory(serverFile.getParentFile());
		final long testContentsOut = System.nanoTime();
		final long testContentsIn;
		final DataOutputStream out = new DataOutputStream(new FileOutputStream(serverFile));
		out.writeLong(testContentsOut);
		out.close();
		final DataInputStream in = new DataInputStream(new FileInputStream(serverFile));
		testContentsIn = in.readLong();
		in.close();
		serverFile.delete();
		serverDir.deleteCreated();
		Assert.assertEquals(testContentsIn, testContentsOut,
				"should be able to read and write data with safe file-paths");
	}
	
	@Test
	public void testClientPathSafety() throws IOException {
		testClientPath("C;/Foo1._/_nUl.txt/coM5_/_$bar/_.[]", "C:", "Foo1.", "nUl.txt", "coM5", "$bar", ".<>");
	}
	
	@Test
	public void testGetMinimumDepthPossible() throws IOException {
		testMinimumDepth(1, "abcd", "abce", "abcf");
		testMinimumDepth(2, "abcd", "abdd", "abcf");
		testMinimumDepth(1, "abcd", "abc", "abcf");
		testMinimumDepth(2, "abcd", "abd", "abcf");
		testMinimumDepth(3, "abcd", "accd", "abcf");
		testMinimumDepth(4, "abcd", "bbcd", "abcf");
		testMinimumDepth(4, "aabcd", "bbcd", "d");
	}
	
	@Test
	public void testChildPathLegal() {
		final FsFile parent = new FsFile("a/b/c");
		final FsFile child = new FsFile("a/b/c/d/e");
		Assert.assertEquals(child.getPathFrom(parent).toString(), "d/e",
				"unexpected result for relative path");
	}
	
	@Test
	public void testChildPathSame() {
		final FsFile path = new FsFile("a/b/c");
		Assert.assertEquals(path.getPathFrom(path).toString(), "",
				"relative path to same directory should be empty");
	}
	
	@Test
	public void testChildPathIllegal() {
		final FsFile parent = new FsFile("a/c/c");
		final FsFile child = new FsFile("a/b/c/d/e");
		Assert.assertNull(child.getPathFrom(parent),
				"relative path may only be within parent directory");
	}
	
	@AfterClass
	public void tearDown() {
		fpt.baseDirFile.delete();
		this.fpt = null;
	}
}
