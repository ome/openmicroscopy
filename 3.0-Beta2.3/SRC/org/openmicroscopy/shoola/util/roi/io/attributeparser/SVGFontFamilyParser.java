/*
 * org.openmicroscopy.shoola.util.roi.io.attributeparser.SVGFontFamilyParser 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.roi.io.attributeparser;

//Java imports
import java.awt.Font;

//Third-party libraries
import net.n3.nanoxml.IXMLElement;
import static org.jhotdraw.draw.AttributeKeys.FONT_FACE;


//Application-internal dependencies
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class SVGFontFamilyParser 	
	implements SVGAttributeParser
{

	/**
	 * Overridden from the {@link SVGAttributeParser#parse(ROIFigure, 
	 * IXMLElement, String)}
	 * This Method will parse the font family used to display text read from 
	 * the element.
	 */
	public void parse(ROIFigure figure,IXMLElement element,String value) 
	{
        FONT_FACE.set(figure, new Font(value, Font.PLAIN, 12));
    }
	
}


