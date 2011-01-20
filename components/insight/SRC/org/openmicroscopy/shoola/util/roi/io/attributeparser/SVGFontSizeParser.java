/*
 * org.openmicroscopy.shoola.util.roi.io.attributeparser.SVGFontSizeParser 
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
import java.util.HashMap;

//Third-party libraries
import static org.jhotdraw.draw.AttributeKeys.FONT_SIZE;
import net.n3.nanoxml.IXMLElement;

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
public class SVGFontSizeParser 
	implements SVGAttributeParser
{
	/**
	 * The absolute font size is a string mapping to a font size. 
	 */
	 private final static HashMap<String,Double> absoluteFontSizeMap;
	    static 
	    {
	        absoluteFontSizeMap = new HashMap<String,Double>();
	        absoluteFontSizeMap.put("xx-small",6.944444);
	        absoluteFontSizeMap.put("x-small",8.3333333);
	        absoluteFontSizeMap.put("small", 10d);
	        absoluteFontSizeMap.put("medium", 12d);
	        absoluteFontSizeMap.put("large", 14.4);
	        absoluteFontSizeMap.put("x-large", 17.28);
	        absoluteFontSizeMap.put("xx-large",20.736);
	    }	
	    

	/**
	* Overridden from the {@link SVGAttributeParser#parse(ROIFigure, 
	* IXMLElement, String)}
	* This Method will parse the absolute font size from an element to set the
	* font size of a text object in the figure.
	*/
	public void parse(ROIFigure figure,IXMLElement element, String value) 
	{
		if (absoluteFontSizeMap.containsKey(value)) 
			FONT_SIZE.set(figure, absoluteFontSizeMap.get(value));
		else
			FONT_SIZE.set(figure, new Double(value));
	}
}