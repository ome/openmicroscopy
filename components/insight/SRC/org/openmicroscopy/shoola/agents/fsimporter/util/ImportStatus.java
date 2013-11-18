/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2013 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.fsimporter.util;

/** 
 * Enumeration values for the status of an import.
 *
 * @author Scott Littlewood, <a href="mailto:sylittlewood@dundee.ac.uk">sylittlewood@dundee.ac.uk</a>
 * @since 4.4
 */
public enum ImportStatus {

	/** Indicates that the import has not started */
	QUEUED,
	
	/** Indicates that the import has started */
	STARTED,
	
	/** Indicates that upload failed */
	UPLOAD_FAILURE,
	
	/** The value indicating that the import was partially successful. */
	PARTIAL,
	
	/** The value indicating that the import was successful. */
	SUCCESS,
	
	/** The value indicating that the import was not successful. */
	FAILURE,

	/** The value indicating that a missing library import was not successful. */
    FAILURE_LIBRARY,
	
	/** The value indicating that the file has been ignored. */
	IGNORED
}
