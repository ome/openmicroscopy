 /*
 * xmlMVC.ConfigConstants 
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
 */
package xmlMVC;

import java.io.File;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * This is a place to hold variables for configuration of the application.
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ConfigConstants {

	/**
	 * The folder that should be used to store all temp. files, config files, logs etc. 
	 */
	public static final String OMERO_EDITOR_FILE = System.getProperty("user.home") + File.separator +
		"omero" + File.separator + "Editor";
	/**
	 * This string is used to add a "version" attribute to the XML documents saved by this application.
	 */
	public static final String VERSION = "version";
	/**
	 * Prior to OMERO 3.0-Beta-3.0, all documents have been assigned "version=1.0".
	 * The version number will contain both the milestone eg "3.0" and the version, eg "3.1.2"
	 * separated by a dash. So, 3.0-Beta-3.0 will be "3.0-3.0"
	 * 
	 * NB. There is also a reference to the current version in the 
	 * jar "client-3.0-Beta3.jar" etc. This is used at startup, right before the
	 * "splash-screen" is displayed, to check that this is the "current" version.
	 * 
	 */
	public static final String EDITOR_VERSION_NUMBER = "3.0-3.0.1";
	/**
	 * This is an identifier, eg. for Exception handler's bug reporter.
	 * It consists of the EDITOR_VERSION_NUMBER, as well as any release candidate id,
	 * So, 3.0-Beta-3.0, release candidate 2 will be "3.0-3.0rc2".
	 * For the Milestone releases, the editor release Id will be the same as 
	 * the EDITOR_VERSION_NUMBER.
	 */
	public static final String EDITOR_RELEASE_ID = EDITOR_VERSION_NUMBER + "";

}
