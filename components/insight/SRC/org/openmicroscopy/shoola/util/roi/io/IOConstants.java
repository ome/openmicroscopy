/*
 * org.openmicroscopy.shoola.util.roi.io.InputOutputConstants 
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
package org.openmicroscopy.shoola.util.roi.io;


//Java imports
import java.awt.Color;

//Third-party libraries

//Application-internal dependencies

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
public class IOConstants 
{
	/** The file which maps the pixelIDs to the file saved by the user. */
	//public static final String FILEMAP_XML = "fileMap.xml";
	/** The tag in the filemap listing all the server saved in the fileMap. */
	public static final String SERVER_TAG = "server";
	/** The attribute for the name of the server. */
	public static final String SERVER_NAME_ATTRIBUTE = "name";
	/** The attribute for the name of the user. */
	public static final String USER_NAME_ATTRIBUTE = "name";
	/** The tag in the filemap listing all the users saved in the fileMap. */
	public static final String USER_TAG = "user";
	/** The tag in the filemap listing all the pixels saved in the fileMap. */
	public static final String PIXELSID_TAG = "pixelsid";
	/** The tag in the pixelsidtag listing all the files saved under pixels id. */
	public static final String FILE_TAG = "file";
	/** Name space for the fileMap object. */
	public final static String FILEMAP_XML_NAMESPACE = "https://www.openmicroscopy.org";
	/** Version number for the filemap object. */
	public final static String FILEMAP_XML_VERSION = "1.0";
	/** Version tag for fileMap object. */
	public final static String FILEMAP_XML_VERSION_TAG = "version";
	/** Attribute with value of pixel for pixels id. */
	public final static String PIXELSID_ATTRIBUTE = "id";
	/** Attribute with name of file.*/
	public final static String FILENAME_ATTRIBUTE = "filename";
	/** The pixels tag contains the set of pixels used in the system. */
	public final static String PIXELSSET_TAG = "pixelset";

	public final static String SVG_NAMESPACE = "http://www.w3.org/2000/svg";
	public final static String ROI_NAMESPACE = "https://www.openmicroscopy.org";
	public final static String VERSION_TAG = "version";
	public final static String SVG_VERSION = "1.2";
	public final static String SVG_XLINK_VALUE = "http://www.w3.org/1999/xlink";
	public final static String XLINK_ATTRIBUTE = "xmlns:xlink";
	
	public final static String ROI_VERSION = "1.0";
	public final static String ROISET_TAG = "roiset";
	public final static String ROI_TAG = "roi";
	public final static String ROI_ID_ATTRIBUTE = "id";
	public final static String ROISHAPE_TAG = "roishape";
	public final static String ANNOTATION_TAG = "annotation";
	public final static String DEFS_TAG = "defs";
	public final static String SVG_TAG = "svg";
	public final static String VALUE_TAG = "value";
	public final static String RECT_TAG = "rect";
	public final static String ELLIPSE_TAG = "ellipse";
	public final static String LINE_TAG = "line";
	public final static String TEXT_TAG = "text";
	public final static String POLYLINE_TAG = "polyline";
	public final static String POLYGON_TAG = "polygon";
	public final static String POINT_TAG = "point";

	public final static String DATATYPE_ATTRIBUTE = "type";
	public final static String SIZE_ATTRIBUTE = "size"; 
	public final static String VALUE_ATTRIBUTE = "value";
	public final static String POINTS_MASK_ATTRIBUTE = "mask";
	public final static String POINTS_ATTRIBUTE = "points";
	public final static String POINTS_CONTROL1_ATTRIBUTE = "points-c1";
	public final static String POINTS_CONTROL2_ATTRIBUTE = "points-c2";
	public final static String VALUE_NULL = "";
	
	public final static String CONNECTION_TO_ATTRIBUTE = "to";
	public final static String CONNECTION_FROM_ATTRIBUTE = "from";
	public final static String X_ATTRIBUTE = "x";
	public final static String X1_ATTRIBUTE = "x1";
	public final static String X2_ATTRIBUTE = "x2";
	public final static String Y_ATTRIBUTE = "y";
	public final static String Y1_ATTRIBUTE = "y1";
	public final static String Y2_ATTRIBUTE = "y2";
	public final static String CX_ATTRIBUTE = "cx";
	public final static String CY_ATTRIBUTE = "cy";
	public final static String RX_ATTRIBUTE = "rx";
	public final static String RY_ATTRIBUTE = "ry";
	public final static String Z_ATTRIBUTE = "z";
	public final static String C_ATTRIBUTE = "c";
	public final static String T_ATTRIBUTE = "t";
	public final static String WIDTH_ATTRIBUTE = "width";
	public final static String HEIGHT_ATTRIBUTE = "height";
	public final static String RED_ATTRIBUTE = "r";
	public final static String BLUE_ATTRIBUTE = "b";
	public final static String GREEN_ATTRIBUTE = "g";
	public final static String ALPHA_ATTRIBUTE = "a";
		
	public final static String ATTRIBUTE_DATATYPE_STRING = "String";
	public final static String ATTRIBUTE_DATATYPE_DOUBLE = "Double";
	public final static String ATTRIBUTE_DATATYPE_LONG = "Long";
	public final static String ATTRIBUTE_DATATYPE_INTEGER = "Integer";
	public final static String ATTRIBUTE_DATATYPE_BOOLEAN = "Boolean";
	public final static String ATTRIBUTE_DATATYPE_FLOAT = "Float";
	public final static String ATTRIBUTE_DATATYPE_POINT2D = "Point2D";
	public final static String ATTRIBUTE_DATATYPE_ELLIPSE2D = "Ellipse2D";
	public final static String ATTRIBUTE_DATATYPE_RECTANGLE2D = "Rectangle2D";
	public final static String ATTRIBUTE_DATATYPE_COLOUR = "Color";
	public final static String ATTRIBUTE_DATATYPE_COORD3D = "Coord3D";
	public final static String ATTRIBUTE_DATATYPE_ARRAYLIST = "ArrayList";
	
	public final static String ATTRIBUTE_SHOWTEXT = "ShowBasicTextAnnotation";
	public final static String ATTRIBUTE_SHOWMEASUREMENT = "ShowMeasurement";
	
	public final static String SVG_FILL_ATTRIBUTE = "fill";
	public final static String SVG_FILL_OPACITY_ATTRIBUTE = "fill-opacity";
	public final static String SVG_FILL_RULE_ATTRIBUTE = "fill-rule";
	public final static String SVG_STROKE_ATTRIBUTE = "stroke";
	public final static String SVG_STROKE_OPACITY_ATTRIBUTE = "stroke-opacity";
	public final static String SVG_STROKE_WIDTH_ATTRIBUTE = "stroke-width";
	public final static String SVG_STROKE_DASHOFFSET_ATTRIBUTE = "stroke-dashoffset";
	public final static String SVG_STROKE_DASHARRAY_ATTRIBUTE = "stroke-dasharray";
	public final static String SVG_STROKE_LINECAP_ATTRIBUTE = "stroke-linecap";
	public final static String SVG_STROKE_LINEJOIN_ATTRIBUTE = "stroke-linejoin";
	public final static String SVG_STROKE_MITERLIMIT_ATTRIBUTE = "stroke-miterlimit";
	public final static String SVG_COLOR_INTERPOLATION_ATTRIBUTE = "color-interpolation";
	public final static String SVG_COLOR_RENDERING_ATTRIBUTE = "color-rendering";
	public final static String SVG_OPACITY_ATTRIBUTE = "opacity";
	public final static String SVG_MARKER_END_ATTRIBUTE = "marker-end";
	public final static String SVG_MARKER_MID_ATTRIBUTE = "color-rendering";
	public final static String SVG_MARKER_START_ATTRIBUTE = "color-rendering";
	public final static String SVG_FONT_FAMILY_ATTRIBUTE = "font-family";
	public final static String SVG_FONT_SIZE_ATTRIBUTE = "font-size";
	public final static String SVG_FONT_SIZE_ADJUST_ATTRIBUTE = "font-adjust";
	public final static String SVG_FONT_STRETCH_ATTRIBUTE = "font-strech";
	public final static String SVG_FONT_STYLE_ATTRIBUTE = "font-style";
	public final static String SVG_FONT_VARIANT_ATTRIBUTE = "font-variant";
	public final static String SVG_FONT_WEIGHT_ATTRIBUTE = "font-weight";
	public final static String SVG_ALIGNMENT_BASELINE_ATTRIBUTE = "alignment-baseline";
	public final static String SVG_BASELINE_SHIFT_ATTRIBUTE = "baseline-shift";
	public final static String SVG_DIRECTION_ATTRIBUTE = "direction";
	public final static String SVG_DOMINANT_BASELINE_ATTRIBUTE = "dominant-baseline";
	public final static String SVG_GLYPH_ORIENTATION_HORIZONTAL_ATTRIBUTE = "glyph-orientation-horizontal";
	public final static String SVG_GLYPH_ORIENTATION_VERTICAL_ATTRIBUTE = "glyph-orientation-vertical";
	public final static String SVG_KERNING_ATTRIBUTE = "kerning";
	public final static String SVG_LETTER_SPACING_ATTRIBUTE = "letter-spacing";
	public final static String SVG_TEXT_ANCHOR_ATTRIBUTE = "text-anchor";
	public final static String SVG_TEXT_DECORATION_ATTRIBUTE = "text-decoration";
	public final static String SVG_UNICODE_BIDI_ATTRIBUTE = "unicode-bidi";
	public final static String SVG_WORD_SPACING_ATTRIBUTE = "word-spacing";
	public final static String SVG_ROTATE_ATTRIBUTE = "rotate";
	public final static String SVG_TRANSFORM_ATTRIBUTE = "transform";

	public final static Color DEFAULT_TEXT_COLOUR = new Color(196, 196, 196, 196);
	public final static Color DEFAULT_FILL_COLOUR = new Color(0, 0, 0, 64);
	public final static Color DEFAULT_STROKE_COLOUR = new Color(196, 196, 196, 196);
	public final static Color DEFAULT_FILL_COLOUR_ALPHA = new Color(0, 0, 0, 32);
	public final static Color DEFAULT_STROKE_COLOUR_ALPHA = new Color(196, 196, 196, 196);
	public final static Color DEFAULT_MEASUREMENT_TEXT_COLOUR =  
												new Color(255, 255, 255, 220);
	public final static double DEFAULT_FONT_SIZE = 12.0f;
	public final static double DEFAULT_STROKE_WIDTH =  1.0f;
}


