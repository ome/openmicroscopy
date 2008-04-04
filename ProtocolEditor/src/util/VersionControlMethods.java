
/*
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

package util;

import java.util.StringTokenizer;

import xmlMVC.XMLModel;

/**
 * This class contains a method for checking the current version number of this software
 * with the version of a newly opened file. 
 * 
 * @author will
 *
 */
public class VersionControlMethods {

	/**
	 * This method compares two version numbers made up from integers,
	 * separated by "." and "-". Eg "3.0-3.1.2"
	 * The integers are compared, starting at the beginning, and if 
	 * the fileVersion number is higher than the software version, true is returned. 
	 * Returns false if either are null.
	 * 
	 * @param softwareVersion	The version of the current software. 
	 * @param fileVersion		The version of a file, being checked for future version.
	 * @return		True if the file version number is higher than the software version number. 
	 */
	public static boolean isFileVersionFromFuture(String softwareVersion, String fileVersion) {
		
		if ((softwareVersion == null) || (fileVersion == null))
			return false;
		
		/*
		 * Tokenize the version numbers, by replacing "-" with "." then splitting..
		 */
		softwareVersion = softwareVersion.replace("-", ".");
		fileVersion = fileVersion.replace("-", ".");
		
		StringTokenizer softwareTokenizer = new StringTokenizer(softwareVersion, ".");
		StringTokenizer fileTokenizer = new StringTokenizer(fileVersion, ".");
		
		/*
		 * If the file version number is greater than the software version number, return true.
		 * Also return true if the software version has fewer numbers (has no more tokens).
		 */
		while (fileTokenizer.hasMoreTokens()) {
			
			if (! softwareTokenizer.hasMoreTokens())
				return true;
			
			int softwareNo = Integer.parseInt(softwareTokenizer.nextToken());
			int fileNo = Integer.parseInt(fileTokenizer.nextToken());
			
			if (fileNo > softwareNo)
				return true;
		}
		
		return false;
	}
	
	/**
	 * This method compares the file version number with the current software version
	 * number, as provided by getSoftwareVersionNumber().
	 * If the fileVersion number is higher than the software version, true is returned. 
	 * Returns false if either are null.
	 * 
	 * @param fileVersion		The version of a file, being checked for future version.
	 * @return		True if the file version number is higher than the software version number. 
	 */
	public static boolean isFileVersionFromFuture(String fileVersion) {
		return isFileVersionFromFuture(getSoftwareVersionNumber(), fileVersion);
	}
	
	/**
	 * A simple method that returns the current version number of this software. 
	 * This includes the Milestone eg "3.0" and the version eg"3.0.1" separated by 
	 * a "-"
	 * eg "3.0-3.0.1"
	 * @return
	 */
	public static String getSoftwareVersionNumber() {
		return XMLModel.EDITOR_VERSION_NUMBER;
	}
	
	/**
	 * For testing.
	 * @param args
	 */
	public static void main (String[] args) {
		
		String softwareVersion = "3.0-3.2";
		String fileVersion = "3.0-3.2";
		
		System.out.println("softwareVersion " + softwareVersion + ", fileVersion " + fileVersion);
		System.out.println("isFileVersionFromFuture: " + isFileVersionFromFuture(softwareVersion, fileVersion));
	}
}
