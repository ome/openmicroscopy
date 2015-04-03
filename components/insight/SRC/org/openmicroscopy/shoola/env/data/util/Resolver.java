/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.env.data.util;

import java.io.InputStream;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.openmicroscopy.shoola.util.ui.UIUtilities;


/**
 * Use
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.1
 */
public class Resolver
    implements URIResolver
{

    /** The sheet to the units conversion.*/
    private static final String UNITS = "transforms/units-conversion.xsl";

    /** The stream.*/
    private InputStream stream;

    /** Close the input stream if not <code>null</code>.*/
    public void close()
        throws Exception
    {
        if (stream != null) stream.close();
    }

    @Override
    public Source resolve(String href, String base)
            throws TransformerException {
        if (UIUtilities.isWindowsOS()) {
            stream = this.getClass().getClassLoader().getResourceAsStream(UNITS);
        } else {
            stream = this.getClass().getResourceAsStream("/"+UNITS);
        }
        return new StreamSource(stream);
    }

}
