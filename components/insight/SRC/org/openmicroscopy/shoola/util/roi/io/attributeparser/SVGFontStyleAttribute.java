/*
 * org.openmicroscopy.shoola.util.roi.io.attributeparser.SVGFontStyleAttribute 
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

//Third-party libraries
import static org.jhotdraw.draw.AttributeKeys.FONT_ITALIC;
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
public class SVGFontStyleAttribute
	implements SVGAttributeParser
{

	/** Text string for normal text. */
	private final static String FONT_NORMAL_VALUE = "normal";
	
	/** Text string for italic text. */
	@SuppressWarnings("unused")
	private final static String FONT_ITALIC_VALUE = "italic";
	
	/**
	 * Overridden from the {@link SVGAttributeParser#parse(ROIFigure, 
	 * IXMLElement, String)}
	 * This Method will parse the font text style for the element 
	 * and set the font style of the figure to that value.
	 */
	public void parse(ROIFigure figure,IXMLElement element, String value) 
	{
		if(value.equals(FONT_NORMAL_VALUE))
			FONT_ITALIC.set(figure, false);
		else
			FONT_ITALIC.set(figure, true);
	}
}


