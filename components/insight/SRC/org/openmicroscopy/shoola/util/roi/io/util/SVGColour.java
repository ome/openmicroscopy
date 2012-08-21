/*
 * org.openmicroscopy.shoola.util.roi.io.util.SVGColour 
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
package org.openmicroscopy.shoola.util.roi.io.util;




//Java imports
import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;
import javax.swing.UIManager;

//Third-party libraries

//Application-internal dependencies

/** 
 * Default SVG Color.
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
public class SVGColour 
{
	
	/** The default value. */
	public final static String 				VALUE_NULL = "";
	
	/** The supported colors. */
	public final static Map<String,Color> 	SVG_COLORS;
	
	/** The default color. */
	private static final Color				DEFAULT_COLOR = Color.black;
	
	static {
		LinkedHashMap<String,Color> map = new LinkedHashMap<String,Color>();

		// SVG 1.2 Tiny colors
		map.put("black", new Color(0, 0, 0));
		map.put("green", new Color(0, 128, 0));
		map.put("silver", new Color(192, 192, 192));
		map.put("lime", new Color(0, 255, 0));
		map.put("gray", new Color(128, 128, 128));
		map.put("olive", new Color(128, 128, 0));
		map.put("white", new Color(255, 255, 255));
		map.put("yellow", new Color(255, 255, 0));
		map.put("maroon", new Color(128, 0, 0));
		map.put("navy", new Color(0, 0, 128));
		map.put("red", new Color(255, 0, 0));
		map.put("blue", new Color(0, 0, 255));
		map.put("purple", new Color(128, 0, 128));
		map.put("teal", new Color(0, 128, 128));
		map.put("fuchsia", new Color(255, 0, 255));
		map.put("aqua", new Color(0, 255, 255));

		// SVG 1.1 colors
		map.put("aliceblue", new Color(240, 248, 255));
		map.put("antiquewhite", new Color(250, 235, 215));
		map.put("aqua", new Color( 0, 255, 255));
		map.put("aquamarine", new Color(127, 255, 212));
		map.put("azure", new Color(240, 255, 255));
		map.put("beige", new Color(245, 245, 220));
		map.put("bisque", new Color(255, 228, 196));
		map.put("black", new Color( 0, 0, 0));
		map.put("blanchedalmond", new Color(255, 235, 205));
		map.put("blue", new Color( 0, 0, 255));
		map.put("blueviolet", new Color(138, 43, 226));
		map.put("brown", new Color(165, 42, 42));
		map.put("burlywood", new Color(222, 184, 135));
		map.put("cadetblue", new Color( 95, 158, 160));
		map.put("chartreuse", new Color(127, 255, 0));
		map.put("chocolate", new Color(210, 105, 30));
		map.put("coral", new Color(255, 127, 80));
		map.put("cornflowerblue", new Color(100, 149, 237));
		map.put("cornsilk", new Color(255, 248, 220));
		map.put("crimson", new Color(220, 20, 60));
		map.put("cyan", new Color( 0, 255, 255));
		map.put("darkblue", new Color( 0, 0, 139));
		map.put("darkcyan", new Color( 0, 139, 139));
		map.put("darkgoldenrod", new Color(184, 134, 11));
		map.put("darkgray", new Color(169, 169, 169));
		map.put("darkgreen", new Color( 0, 100, 0));
		map.put("darkgrey", new Color(169, 169, 169));
		map.put("darkkhaki", new Color(189, 183, 107));
		map.put("darkmagenta", new Color(139, 0, 139));
		map.put("darkolivegreen", new Color( 85, 107, 47));
		map.put("darkorange", new Color(255, 140, 0));
		map.put("darkorchid", new Color(153, 50, 204));
		map.put("darkred", new Color(139, 0, 0));
		map.put("darksalmon", new Color(233, 150, 122));
		map.put("darkseagreen", new Color(143, 188, 143));
		map.put("darkslateblue", new Color( 72, 61, 139));
		map.put("darkslategray", new Color( 47, 79, 79));
		map.put("darkslategrey", new Color( 47, 79, 79));
		map.put("darkturquoise", new Color( 0, 206, 209));
		map.put("darkviolet", new Color(148, 0, 211));
		map.put("deeppink", new Color(255, 20, 147));
		map.put("deepskyblue", new Color( 0, 191, 255));
		map.put("dimgray", new Color(105, 105, 105));
		map.put("dimgrey", new Color(105, 105, 105));
		map.put("dodgerblue", new Color( 30, 144, 255));
		map.put("firebrick", new Color(178, 34, 34));
		map.put("floralwhite", new Color(255, 250, 240));
		map.put("forestgreen", new Color( 34, 139, 34));
		map.put("fuchsia", new Color(255, 0, 255));
		map.put("gainsboro", new Color(220, 220, 220));
		map.put("ghostwhite", new Color(248, 248, 255));
		map.put("gold", new Color(255, 215, 0));
		map.put("goldenrod", new Color(218, 165, 32));
		map.put("gray", new Color(128, 128, 128));
		map.put("grey", new Color(128, 128, 128));
		map.put("green", new Color( 0, 128, 0));
		map.put("greenyellow", new Color(173, 255, 47));
		map.put("honeydew", new Color(240, 255, 240));
		map.put("hotpink", new Color(255, 105, 180));
		map.put("indianred", new Color(205, 92, 92));
		map.put("indigo", new Color( 75, 0, 130));
		map.put("ivory", new Color(255, 255, 240));
		map.put("khaki", new Color(240, 230, 140));
		map.put("lavender", new Color(230, 230, 250));
		map.put("lavenderblush", new Color(255, 240, 245));
		map.put("lawngreen", new Color(124, 252, 0));
		map.put("lemonchiffon", new Color(255, 250, 205));
		map.put("lightblue", new Color(173, 216, 230));
		map.put("lightcoral", new Color(240, 128, 128));
		map.put("lightcyan", new Color(224, 255, 255));
		map.put("lightgoldenrodyellow", new Color(250, 250, 210));
		map.put("lightgray", new Color(211, 211, 211));
		map.put("lightgreen", new Color(144, 238, 144));
		map.put("lightgrey", new Color(211, 211, 211));
		map.put("lightpink", new Color(255, 182, 193));
		map.put("lightsalmon", new Color(255, 160, 122));
		map.put("lightseagreen", new Color( 32, 178, 170));
		map.put("lightskyblue", new Color(135, 206, 250));
		map.put("lightslategray", new Color(119, 136, 153));
		map.put("lightslategrey", new Color(119, 136, 153));
		map.put("lightsteelblue", new Color(176, 196, 222));
		map.put("lightyellow", new Color(255, 255, 224));
		map.put("lime", new Color( 0, 255, 0));
		map.put("limegreen", new Color( 50, 205, 50));
		map.put("linen", new Color(250, 240, 230));
		map.put("magenta", new Color(255, 0, 255));
		map.put("maroon", new Color(128, 0, 0));
		map.put("mediumaquamarine", new Color(102, 205, 170));
		map.put("mediumblue", new Color( 0, 0, 205));
		map.put("mediumorchid", new Color(186, 85, 211));
		map.put("mediumpurple", new Color(147, 112, 219));
		map.put("mediumseagreen", new Color( 60, 179, 113));
		map.put("mediumslateblue", new Color(123, 104, 238));
		map.put("mediumspringgreen", new Color( 0, 250, 154));
		map.put("mediumturquoise", new Color( 72, 209, 204));
		map.put("mediumvioletred", new Color(199, 21, 133));
		map.put("midnightblue", new Color( 25, 25, 112));
		map.put("mintcream", new Color(245, 255, 250));
		map.put("mistyrose", new Color(255, 228, 225));
		map.put("moccasin", new Color(255, 228, 181));
		map.put("navajowhite", new Color(255, 222, 173));
		map.put("navy", new Color( 0, 0, 128));
		map.put("oldlace", new Color(253, 245, 230));
		map.put("olive", new Color(128, 128, 0));
		map.put("olivedrab", new Color(107, 142, 35));
		map.put("orange", new Color(255, 165, 0));
		map.put("orangered", new Color(255, 69, 0));
		map.put("orchid", new Color(218, 112, 214));
		map.put("palegoldenrod", new Color(238, 232, 170));
		map.put("palegreen", new Color(152, 251, 152));
		map.put("paleturquoise", new Color(175, 238, 238));
		map.put("palevioletred", new Color(219, 112, 147));
		map.put("papayawhip", new Color(255, 239, 213));
		map.put("peachpuff", new Color(255, 218, 185));
		map.put("peru", new Color(205, 133, 63));
		map.put("pink", new Color(255, 192, 203));
		map.put("plum", new Color(221, 160, 221));
		map.put("powderblue", new Color(176, 224, 230));
		map.put("purple", new Color(128, 0, 128));
		map.put("red", new Color(255, 0, 0));
		map.put("rosybrown", new Color(188, 143, 143));
		map.put("royalblue", new Color( 65, 105, 225));
		map.put("saddlebrown", new Color(139, 69, 19));
		map.put("salmon", new Color(250, 128, 114));
		map.put("sandybrown", new Color(244, 164, 96));
		map.put("seagreen", new Color( 46, 139, 87));
		map.put("seashell", new Color(255, 245, 238));
		map.put("sienna", new Color(160, 82, 45));
		map.put("silver", new Color(192, 192, 192));
		map.put("skyblue", new Color(135, 206, 235));
		map.put("slateblue", new Color(106, 90, 205));
		map.put("slategray", new Color(112, 128, 144));
		map.put("slategrey", new Color(112, 128, 144));
		map.put("snow", new Color(255, 250, 250));
		map.put("springgreen", new Color( 0, 255, 127));
		map.put("steelblue", new Color( 70, 130, 180));
		map.put("tan", new Color(210, 180, 140));
		map.put("teal", new Color( 0, 128, 128));
		map.put("thistle", new Color(216, 191, 216));
		map.put("tomato", new Color(255, 99, 71));
		map.put("turquoise", new Color( 64, 224, 208));
		map.put("violet", new Color(238, 130, 238));
		map.put("wheat", new Color(245, 222, 179));
		map.put("white", new Color(255, 255, 255));
		map.put("whitesmoke", new Color(245, 245, 245));
		map.put("yellow", new Color(255, 255, 0));
		map.put("yellowgreen", new Color(154, 205, 50));

		// SVG 1.2 Tiny system colors
		map.put("activeborder", UIManager.getColor("activeCaptionBorder"));
		map.put("activecaption", UIManager.getColor("activeCaption"));
		map.put("appworkspace", UIManager.getColor("window"));
		map.put("background", UIManager.getColor("desktop"));
		map.put("buttonface", UIManager.getColor("control"));
		map.put("buttonhighlight", UIManager.getColor("controlHighlight"));
		map.put("buttonshadow", UIManager.getColor("controlShadow"));
		map.put("buttontext", UIManager.getColor("controlText"));
		map.put("captiontext", UIManager.getColor("activeCaptionText"));
		map.put("graytext", UIManager.getColor("textInactiveText"));
		map.put("highlight", UIManager.getColor("textHighlight"));
		map.put("highlighttext", UIManager.getColor("textHighlightText"));
		map.put("inactiveborder", UIManager.getColor("inactiveCaptionBorder"));
		map.put("inactivecaption", UIManager.getColor("inactiveCaption"));
		map.put("inactivecaptiontext", UIManager.getColor("inactiveCaptionText"));
		map.put("infobackground", UIManager.getColor("info"));
		map.put("infotext", UIManager.getColor("infoText"));
		map.put("menu", UIManager.getColor("menu"));
		map.put("menutext", UIManager.getColor("menuText"));
		map.put("scrollbar", UIManager.getColor("scrollbar"));
		map.put("threeddarkshadow", UIManager.getColor("controlDkShadow"));
		map.put("threedface", UIManager.getColor("control"));
		map.put("threedhighlight", UIManager.getColor("controlHighlight"));
		map.put("threedlightshadow", UIManager.getColor("controlLtHighlight"));
		map.put("threedshadow", UIManager.getColor("controlShadow"));
		map.put("window", UIManager.getColor("window"));
		map.put("windowframe", UIManager.getColor("windowBorder"));
		map.put("windowtext", UIManager.getColor("windowText"));

		SVG_COLORS = map;
	}
	    
	/**
	 * Returns the color corresponding to the passed string.
	 * 
	 * @param value The string identifying the color.
	 * @return See above.
	 */
	public Color toColor(String value)
	{
		if (value == null) return DEFAULT_COLOR;
		String colourString = value.trim().toLowerCase();
        if (SVG_COLORS.containsKey(colourString))
			return SVG_COLORS.get(colourString);
        else if (colourString.equals(VALUE_NULL))
        	return DEFAULT_COLOR;
        else if (colourString.charAt(0) == '#' && colourString.length() == 7)
           	return new Color(Integer.decode(colourString));
        else if (colourString.charAt(0) == '#' && colourString.length() == 4)
        {
        	 int th = Integer.decode(colourString);
	         return new Color(
	                    (th & 0xf) | ((th & 0xf) << 4) |
	                    ((th & 0xf0) << 4) | ((th & 0xf0) << 8) |
	                    ((th & 0xf00) << 8) | ((th & 0xf00) << 12)
	                    );
        } else if(colourString.startsWith("rgb")) {
        	 StringTokenizer tt = new StringTokenizer(colourString,"() ,");
	         tt.nextToken();
	         Color c = new Color(
	                    Integer.decode(tt.nextToken()),
	                    Integer.decode(tt.nextToken()),
	                    Integer.decode(tt.nextToken())
	                    );
	            return c;
        }
        return DEFAULT_COLOR;
	}
	
}


