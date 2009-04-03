/*
 * org.openmicroscopy.shoola.util.roi.io.attributeparser.SVGStrokeLineCap 
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
import java.awt.BasicStroke;
import java.util.HashMap;

//Third-party libraries
import static org.jhotdraw.draw.AttributeKeys.STROKE_CAP;

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
public class SVGStrokeLineCapParser 
	implements SVGAttributeParser
{
	private final static String 					BUTT_VALUE = "butt";
	
	private final static HashMap<String,Integer> 	strokeLinecapMap;
	static 
	{
	        strokeLinecapMap = new HashMap<String, Integer>();
	        strokeLinecapMap.put("butt", 	BasicStroke.CAP_BUTT);
	        strokeLinecapMap.put("round", 	BasicStroke.CAP_ROUND);
	        strokeLinecapMap.put("square", 	BasicStroke.CAP_SQUARE);
	}
		
	/* (non-Javadoc)
	 * @see org.openmicroscopy.shoola.util.roi.io.attributeparser.SVGAttributeParser#parse(org.openmicroscopy.shoola.util.roi.figures.ROIFigure, net.n3.nanoxml.IXMLElement, java.lang.String)
	 */
	public void parse(ROIFigure figure, IXMLElement element, String value) 
	{
		if(strokeLinecapMap.containsKey(value))
			STROKE_CAP.set(figure, strokeLinecapMap.get(value));
		else
			STROKE_CAP.set(figure, strokeLinecapMap.get(BUTT_VALUE));
	}

}


