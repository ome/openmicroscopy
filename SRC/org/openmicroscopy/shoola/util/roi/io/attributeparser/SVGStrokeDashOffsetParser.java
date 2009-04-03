/*
 * org.openmicroscopy.shoola.util.roi.io.attributeparser.SCGStrokeDashOffsetParser 
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
import net.n3.nanoxml.IXMLElement;
import static org.jhotdraw.draw.AttributeKeys.IS_STROKE_DASH_FACTOR;
import static org.jhotdraw.draw.AttributeKeys.STROKE_DASH_PHASE;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.io.IOConstants;

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
public class SVGStrokeDashOffsetParser 
	implements SVGAttributeParser
{

	/**
	 * Parses the roi.
	 * @see SVGAttributeParser#parse(ROIFigure, IXMLElement, String)
	 */
	public void parse(ROIFigure figure, IXMLElement element, String value) 
	{
		if (IOConstants.VALUE_NULL.equals(value)) 
		{
			double doubleValue= new Double(value);
			STROKE_DASH_PHASE.set(figure, doubleValue);
			IS_STROKE_DASH_FACTOR.set(figure, false);
		}
	}

}


