/*
 * org.openmicroscopy.shoola.util.ui.drawingtools.figures.FigureUtil 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2016 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.ui.drawingtools.figures;


//Java imports
import java.awt.Color;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.text.AttributedString;
import java.util.HashMap;
import java.util.Map;

import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.Figure;


//Third-party libraries

//Application-internal dependencies

/** 
 * Helper class to handle layout.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class FigureUtil
{

	/** Identifies the <code>Rectangle</code> type. */
	public static final String RECTANGLE_TYPE = "Rectangle";
	
	/** Identifies the <code>Ellipse</code> type. */
	public static final String ELLIPSE_TYPE = "Ellipse";
	
	/** Identifies the <code>Point</code> type. */
	public static final String POINT_TYPE = "Point";
	
	/** Identifies the <code>Line</code> type. */
	public static final String LINE_TYPE = "Line";
	
	/** Identifies the <code>Line</code> type. */
    public static final String ARROW_TYPE = "Arrow";
	
	/** Identifies the <code>LineConnection</code> type. */
	public static final String LINE_CONNECTION_TYPE = "LineConnection";
	
	/** Identifies the <code>Polygon</code> type. */
	public static final String POLYGON_TYPE = "Polygon";
	
	/** Identifies the <code>Text</code> type. */
	public static final String TEXT_TYPE = "Text";
	
	/** Identifies the <code>Scribble</code> type. */
	public static final String SCRIBBLE_TYPE = "Scribble";

	/** Identifies the <code>Mask</code> type. */
	public static final String MASK_TYPE = "Mask";
	
	/** The default number of columns for text. */
	static final int 			TEXT_COLUMNS = 4;
	
	/** The default tab size. */
	static final int 			TAB_SIZE = 8;

	/** The default number of columns for text. */
	static final int 			TEXT_WIDTH = 100;
	
	/** The default background color.*/
	static final Color TEXT_COLOR = Color.BLACK;
	
	/**
	 * Creates a layout for the text.
	 * 
	 * @param text			The text to display.
	 * @param frc			The font context.
	 * @param f				The font.
	 * @param underlined	Passed <code>true</code> if the text is underlined,
	 * 						<code>false</code> otherwise.
	 * @return See above.
	 */
	static TextLayout createLayout(String text, FontRenderContext frc, Font f, 
									boolean underlined)
	{
		if (text == null || text.trim().length() == 0) text = " ";

		Map<TextAttribute, Object> 
			textAttributes = new HashMap<TextAttribute, Object>();
		textAttributes.put(TextAttribute.FONT, f);
		textAttributes.put(TextAttribute.SIZE, 40);
		if (underlined) 
			textAttributes.put(TextAttribute.UNDERLINE,
					TextAttribute.UNDERLINE_LOW_ONE_PIXEL);
		TextLayout layout = new TextLayout(text, textAttributes, frc);
		return layout;
	}
	
	/**
	 * Formats the layout parameters.
	 * 
	 * @param f The font.
	 * @param styledText Host the parameters to set.
	 * @param figure The figure to handle.
	 */
	static void formatLayout(Font f, AttributedString styledText, Figure figure)
	{
		if (styledText == null) return;
		if (f != null) styledText.addAttribute(TextAttribute.FONT, f);
		if (figure != null && AttributeKeys.FONT_UNDERLINE.get(figure)) 
			styledText.addAttribute(TextAttribute.UNDERLINE,
					TextAttribute.UNDERLINE_LOW_ONE_PIXEL);
	}
	
}
