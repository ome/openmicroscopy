/*
 * org.openmicroscopy.shoola.util.roi.figures.MeasureLineTextFigure 
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
package org.openmicroscopy.shoola.util.roi.figures;



//Java imports
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;

//Third-party libraries
import static org.jhotdraw.draw.AttributeKeys.FILL_COLOR;
import static org.jhotdraw.draw.AttributeKeys.FONT_FACE;
import static org.jhotdraw.draw.AttributeKeys.FONT_SIZE;
import static org.jhotdraw.draw.AttributeKeys.FONT_UNDERLINED;
import static org.jhotdraw.draw.AttributeKeys.TEXT;
import static org.jhotdraw.draw.AttributeKeys.TEXT_COLOR;

import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.TextHolderFigure;
import org.jhotdraw.draw.Tool;
import org.jhotdraw.geom.Insets2D;

//Application-internal dependencies
import static org.openmicroscopy.shoola.util.ui.drawingtools.attributes.DrawingAttributes.SHOWTEXT;

import org.openmicroscopy.shoola.util.ui.drawingtools.texttools.DrawingTextTool;


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
public 	class LineAnnotationFigure 	
		extends MeasureLineFigure
{
	
	public LineAnnotationFigure() 
	{
		this("Text");
	}

	public LineAnnotationFigure(String text) 
	{
		super(text);
	}


}