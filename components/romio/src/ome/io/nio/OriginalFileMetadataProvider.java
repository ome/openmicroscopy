/*
 *   $Id$
 *
 *   Copyright 2008 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.io.nio;

import ome.model.core.OriginalFile;
import ome.model.core.Pixels;


/**
 * Denotes an object which can provide metadata about original files and
 * their formats.
 * @author <br>
 *         Chris Allan&nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:callan@blackcat.ca">callan@blackcat.ca</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision$ $Date$) </small>
 * @since OMERO-Beta3.1
 */
public interface OriginalFileMetadataProvider
{
	/**
	 * Finds the first original file for a pixels set whose format starts 
	 * with a given string.
	 * @param pixels Pixels set whose original files we're checking.
	 * @param s String to check each of the original file formats
	 * starts with.
	 * @return First original file object to start with <code>s</code>.
	 */
	OriginalFile getOriginalFileWhereFormatStartsWith(Pixels pixels,
			                                          String s);
}