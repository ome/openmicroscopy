/*
 * org.openmicroscopy.shoola.util.file.WriterTextCst
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.util.file;




//Java imports
import java.util.HashMap;

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class WriterTextCst
{

    private static final String FC = 
        "https://www.openmicroscopy.org/XMLschemas/OME/FC/ome.xsd";
    
    private static final String STD = 
        "https://www.openmicroscopy.org/XMLschemas/STD/RC2/STD.xsd";
    
    private static final String BIN = 
      "https://www.openmicroscopy.org/XMLschemas/BinaryFile/RC1/BinaryFile.xsd";
    
    private static final String XSI =
        "http://www.w3.org/2001/XMLSchema-instance";
    
    static final String XML_HEADER = "<?xml version=\"1.0\" " +
                                            "encoding=\"UTF-8\"?>";
        
    static final String ROOT = "OME";
    
    static final HashMap ATTRIBUTES_ROOT;
    
    static {
        ATTRIBUTES_ROOT = new HashMap();
        ATTRIBUTES_ROOT.put("xmlns", FC);
        ATTRIBUTES_ROOT.put("xmlns:STD", STD);
        ATTRIBUTES_ROOT.put("xmlns:Bin", BIN);
        ATTRIBUTES_ROOT.put("xmlns:xsi", XSI);
        ATTRIBUTES_ROOT.put("xsi:schemaLocation", FC+" "+STD);
    }
        
}
