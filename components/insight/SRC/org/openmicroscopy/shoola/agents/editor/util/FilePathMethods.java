/*
 * org.openmicroscopy.shoola.agents.editor.util.FilePathMethods
 *  
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
 *	author Will Moore will@lifesci.dundee.ac.uk
 */

package org.openmicroscopy.shoola.agents.editor.util;

import java.io.File;
import java.util.StringTokenizer;


/** 
 * This class contains some useful static methods for manipulating file paths. 
 * Eg. converting from a relative to an absolute file path (or vice versa).
 * 
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class FilePathMethods {

	/**
	 * This method returns a file path that is relative to the location of 
	 * the editorFile. 
	 * This relative path will link from the location of editorFile TO the 
	 * location specified by absolutePath.
	 * 
	 * @param absolutePath
	 * @return
	 */
	public static String getRelativePathFromAbsolutePath(File linkFromFile, 
			String absolutePath) {
		return getRelativePathFromAbsolutePath(linkFromFile.getParent(), 
				absolutePath);
	}
	
	/**
	 * This method returns a file path that is relative to the location of 
	 * the given directory.  
	 * This relative path will link from the directory TO the 
	 * location specified by absolutePath.
	 * 
	 * @param linkFromDir		The directory from which to link FROM
	 * @param absolutePath		The file to link TO
	 * @return		The path from the directory to the absolutePath. 
	 */
	public static String getRelativePathFromAbsolutePath(String linkFromDir, 
			String absolutePath) 
	{	
		if (linkFromDir == null) {
			throw new IllegalArgumentException("File must have valid file path");
		}
		
		String fileSeparator = File.separator;
		
		// Need to split both file paths into arrays of directories in order to see
		// how many directories are similar (starting at the root).
		// Can't use string.split(File.separator) because the backslash of Windows 
		// File.separator causes regex errors. Need to use StringTokenizer...

		StringTokenizer st = new StringTokenizer(linkFromDir, fileSeparator);
		int tokens = st.countTokens();
		
		String[] editorFileDirectories = new String[tokens];
		for (int i=0; i<editorFileDirectories.length; i++) {
			editorFileDirectories[i] = st.nextToken();
		}
		
		st = new StringTokenizer(absolutePath, fileSeparator);
		tokens = st.countTokens();
		
		String[] imageFileDirectories = new String[tokens];
		for (int i=0; i<imageFileDirectories.length; i++) {
			imageFileDirectories[i] = st.nextToken();
		}
	
		
		// Count the root directories that are common to both file paths. 
		int commonDirsCount = 0;
		while ((commonDirsCount < editorFileDirectories.length) && 
				(commonDirsCount < imageFileDirectories.length) && 
				(editorFileDirectories[commonDirsCount].equals(imageFileDirectories[commonDirsCount]))) {
			commonDirsCount++;
		}
		
		// The relative image file path needs to be built from the remaining directories of 
		// the image file path.
		// This is the path from the last common directory to the image. 
		String relativeFilePath = "";
		for (int i=commonDirsCount; i<imageFileDirectories.length; i++) {
			// don't add fileSeparator at start. 
			if (i > commonDirsCount)
				relativeFilePath = relativeFilePath + File.separator;
			
			relativeFilePath = relativeFilePath + imageFileDirectories[i];
		}
		
		// If the editor File has additional directories after the last common directory...
		// See how many...
		int editorFileDirsRemaining = editorFileDirectories.length - commonDirsCount;
		
		// Add  ../ for every directory level.
		for (int i=0; i<editorFileDirsRemaining; i++) {
			relativeFilePath = ".." + File.separator + relativeFilePath;
		}
		
		// windows troubleshooting!!
		//JOptionPane.showMessageDialog(null, "FormFieldImage getRelativePath  relativeFilePath = " + relativeFilePath);
		
		return relativeFilePath;
	}
	
	

	/**
	 * This method returns an absolute file path that is constructed from the 
	 * relative file path, and the absolute file path of the 
	 * editorFile.
	 * 
	 * @param	the starting point of the relative path.  
	 * @param 	relativePath is the path RELATIVE to the editorFile. 
	 * @return	the absolute path to the location specified by relativePath.
	 */
	public static String getAbsolutePathFromRelativePath(
			File editorFile, String relativePath) 
	{
		if (editorFile == null)		return null;
		
		String editorDirectory = editorFile.getParent();
		
		if (editorDirectory == null) {
			throw new IllegalArgumentException("File must have valid file path");
		}

		// Need to turn the absolute file path of the Editor file 
		// into an array of directories...
		StringTokenizer st = new StringTokenizer(editorDirectory, File.separator);
		int tokens = st.countTokens();
		
		String[] editorFileDirectories = new String[tokens];
		for (int i=0; i<editorFileDirectories.length; i++) {
			editorFileDirectories[i] = st.nextToken();
		}
		
		// Count the number of directories that the Editor file path has after 
		// the last common directory (that is shared with the image file path).
		// This is denoted in the relative file path by the number of "../" 
		//at the start
		int filePathExtraDirs = 0;
		// look for ".." because you don't know what the file separator is...
		while (relativePath.startsWith("..")) {
			
			filePathExtraDirs++;

			// remove the first 3 characters "../" from the relative image path.
			// Assume that File.separator is only a single character!! 
			relativePath = relativePath.substring(3, relativePath.length());
		}
		
		String absoluteImagePath = "";
		
		// Need to build up the root file path that is common to both the Editor file
		// and the Image. 
		int commonDirs = editorFileDirectories.length - filePathExtraDirs;
		for(int i=0; i<commonDirs; i++) {
			
			absoluteImagePath = absoluteImagePath.concat(File.separator + editorFileDirectories[i]);
		}
		
		// Now make the absolute image path by adding the relative file path
		// to the common directory path.
		absoluteImagePath = absoluteImagePath + File.separator + relativePath;
		
		return absoluteImagePath;
	}
}
