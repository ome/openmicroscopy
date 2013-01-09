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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Transform client-local {@link java.io.File} to repository {@link FsFile} path,
 * and between repository {@link FsFile} path and server-local {@link java.io.File}.
 * The directory topology of the repository {@link FsFile} paths is matched in the
 * server-local {@link java.io.File}s even though the path components may look different.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 4.5
 */
public class FilePathTransformer {
	/* Default field and method visibility is used for access by unit tests. */
	
	/* the OMERO data directory */
	private String omeroDataDir;
	/* where the repository should store its files relative to the OMERO data directory */
	private String fsSubDir;
	/* where the repository should store its files */
	File baseDirFile;
	/* the parent path components that should be omitted from repository paths */
	private FsFile baseDirFsFile;
	/* a function to make file-path components safe across platforms */
	private StringTransformer pathSanitizer;
	
	/* -- SERVER-SIDE METHODS -- */
	
	/**
	 * Given a repository path, returns the corresponding server-local {@link java.io.File}.
	 * Must be executed server-side.
	 * @param fsFile a repository path
	 * @return the corresponding server-local {@link java.io.File}
	 */
	public File getServerFileFromFsFile(FsFile fsFile) {
		return fsFile.toFile(this.baseDirFile);
	}
	
	/**
	 * Given a server-local {@link java.io.File}, returns the corresponding repository path.
	 * Must be executed server-side.
	 * @param serverFile a server-local {@link java.io.File} within the repository
	 * @return the corresponding repository path
	 * @throws IOException if the absolute path of the {@link java.io.File} could not be found
	 */
	public FsFile getFsFileFromServerFile(File serverFile) throws IOException {
		final FsFile fullFsFile = new FsFile(serverFile);
		final FsFile childFsFile = fullFsFile.getPathFrom(this.baseDirFsFile);
		if (childFsFile == null)
			throw new IllegalArgumentException("server files must be within the repository");
		return childFsFile;
	}
	
	/* -- CLIENT-SIDE METHODS -- */

	/**
	 * Given a client-local {@link java.io.File}, and a path component depth,
	 * returns the corresponding repository path. Must be executed client-side.
	 * @param clientFile a client-local {@link java.io.File}
	 * @param depth the path component depth (including filename)
	 * @return the corresponding repository path, intended to be safe cross-platform
	 * @throws IOException if the absolute path of the {@link java.io.File} could not be found
	 */
	public FsFile getFsFileFromClientFile(File clientFile, int depth) throws IOException {
		if (depth < 1) 
			throw new IllegalArgumentException("path depth must be strictly positive");
		return new FsFile(new FsFile(clientFile), depth).transform(this.pathSanitizer);
	}
	
	/**
	 * Test if the given path component depth suffices for disambiguating the given set of
	 * {@link java.io.File}s. Must be executed client-side.
	 * @param files a set of {@link java.io.File}s
	 * @param depth a path component depth
	 * @return if the depth allows the {@link java.io.File}s to each be named distinctly
	 * @throws IOException if the absolute path of any of the {@link java.io.File}s could not be found
	 */
	private boolean isDepthSufficient(Collection<File> files, int depth) throws IOException {
		final Set<FsFile> fsPaths = new HashSet<FsFile>();
		for (final File file : files)
			if (!fsPaths.add(getFsFileFromClientFile(file, depth)))
				return false;
		return true;
	}
	
	/**
	 * Get the smallest path component depth that allows the given set of {@link java.io.File}s
	 * to be disambiguated. Must be executed client-side.
	 * @param files a set of {@link java.io.File}s
	 * @return the minimum depth for disambiguating the {@link java.io.File}s, no less than 1
	 * @throws IOException if the absolute path of any of the {@link java.io.File}s could not be found
	 */
	public int getMinimumDepth(Collection<File> files) throws IOException {
		if (files.size() < 2)
			/* must be a strictly positive integer */
			return 1;
		if (!isDepthSufficient(files, Integer.MAX_VALUE))
			throw new IllegalArgumentException("file set is not unique, so no depth can fix it");
		int depth;
		for (depth = 1; !isDepthSufficient(files, depth); depth++);
		return depth;
	}
	
	/**
	 * Get the files that are too similarly named.
	 * @param files a set of files
	 * @return the files grouped by those to which they are too similar,
	 * or <code>null</code> if all the files are named sufficiently distinctly
	 * @throws IOException if the absolute path of any of the {@link java.io.File}s could not be found
	 */
	public Set<Set<File>> getTooSimilarFiles(Set<File> files) throws IOException {
		final Map<String, Set<File>> filesByFsFile = new HashMap<String, Set<File>>();
		for (final File file : files) {
			final String path = getFsFileFromClientFile(file, Integer.MAX_VALUE).toString().toLowerCase();
			Set<File> similarFiles = filesByFsFile.get(path);
			if (similarFiles == null) {
				similarFiles = new HashSet<File>();
				filesByFsFile.put(path, similarFiles);
			}
			similarFiles.add(file);
		}
		final Set<Set<File>> tooSimilarFiles = new HashSet<Set<File>>();
		for (final Set<File> similarFiles : filesByFsFile.values())
			if (similarFiles.size() > 1)
				tooSimilarFiles.add(similarFiles);
		return tooSimilarFiles.isEmpty() ? null : tooSimilarFiles;
	}
	
	/* -- SPRING-BASED SETUP -- */
	
	/**
	 * Set the string transformer that is used to make file-path components safe across platforms.
	 * This is not required to be an injective function; two different components may transform to the same.
	 * @param pathSanitizer the file-path component string transformer
	 */
	public void setPathSanitizer(StringTransformer pathSanitizer) {
		this.pathSanitizer = pathSanitizer;
	}
	
	/**
	 * Set the OMERO data directory. Expected to be set as part of Spring bean initialization.
	 * @param omeroDataDir the OMERO data directory
	 */
	public void setOmeroDataDir(String omeroDataDir) {
		this.omeroDataDir = omeroDataDir;
	}
	
	/**
	 * Set the repository subdirectory in the OMERO data directory.
	 * Expected to be set as part of Spring bean initialization.
	 * @param fsSubDir the subdirectory in which the repository stores files
	 */
	public void setFsSubDir(String fsSubDir) {
		this.fsSubDir = fsSubDir;
	}
	
	/**
	 * Set the repository base directory.
	 * Expected to be called through Spring as the bean's <code>init-method</code>.
	 * @throws IOException if the absolute path of the base directory could not be found
	 */
	public void calculateBaseDir() throws IOException {
		this.baseDirFile = new File(this.omeroDataDir, this.fsSubDir);
		if (!this.baseDirFile.isDirectory())
			throw new IllegalArgumentException(this.baseDirFile.getPath() + " must specify an existing FS repository directory");
		this.baseDirFsFile = new FsFile(this.baseDirFile);
	}
}
