/*
 * org.openmicroscopy.shoola.env.data.ImportException 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.env.data;



//Java imports
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import ome.conditions.ResourceError;
import omero.ChecksumValidationException;

//Third-party libraries
import loci.formats.FormatException;
import loci.formats.UnsupportedCompressionException;

//Application-internal dependencies

/** 
 * Reports an error occurred while importing an image.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @author Blazej Pindelski, bpindelski at dundee.ac.uk
 * @version 3.0
 * @since 3.0-Beta4
 */
public class ImportException
    extends omero.gateway.exception.ImportException
{

    public ImportException(String message, Throwable cause) {
        super(message, cause);
    }

    public ImportException(String message) {
        super(message);
    }

    public ImportException(Throwable cause) {
        super(cause);
    }


}
