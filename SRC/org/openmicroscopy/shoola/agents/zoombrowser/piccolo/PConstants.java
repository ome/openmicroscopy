/*
 * org.openmicroscopy.shoola.agents.zoombrowser.piccolo.PConstants
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */




/*------------------------------------------------------------------------------
 *
 * Written by:    Harry Hochheiser <hsh@nih.gov>
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.zoombrowser.piccolo;

//Java imports
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;

//Third-party libraries

//Application-internal dependencies

/** 
 *
 * Somce constant values used throughout the piccolo code 
 *  
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */
public class PConstants {


	/** default label color   */ 
    public static final Color DEFAULT_COLOR = Color.BLACK;
	public static final Color DEFAULT_TEXT_COLOR = DEFAULT_COLOR;
	public static final Color DEFAULT_FILL = Color.LIGHT_GRAY;
    
   
   	/** default label font */
	public static final Font PROJECT_LABEL_FONT = 
			new Font("Helvetica",Font.BOLD,10);

	/** font for module names */	
	public static final Font NAME_FONT = new Font("Helvetica",Font.BOLD,14);	

	/** font for tooltips */		
	public static final Font TOOLTIP_FONT = new Font("Helvetica",Font.BOLD,12);
	
	/** The {@link Font} used for the name of a Formal Parameter */
	public static final Font THUMBNAIL_NAME_FONT = 
			new Font("Helvetica",Font.BOLD,6);
			
	/** a selected label's color */
	public static final Color PROJECT_SELECTED_COLOR = new Color(0,0,255);
	
	/** active label color */
	public static final Color PROJECT_ACTIVE_COLOR = new Color(0,0,220);
	
	/** color of rollover label */
	public static final Color PROJECT_ROLLOVER_COLOR = new Color(0,0,200);
	
	/** The standard background color for Piccolo Canvases */
	public static final Color CANVAS_BACKGROUND_COLOR = Color.WHITE;
	
	/** Highlighting of a link  between modules */
	public static final Color LINK_HIGHLIGHT_COLOR=Color.WHITE;
    
	
	/** Highlighting of generic boxes*/
    
	public static final Color HIGHLIGHT_COLOR_OUTER = new Color(215,140,47);
	public static final Color HIGHLIGHT_COLOR_MIDDLE = new Color(223,163,89);
	public static final Color HIGHLIGHT_COLOR_INNER = new Color(231,186,130);
  
	 /** Borders of generic boxes */
	public static final Color BORDER_OUTER = new Color(191,191,191);
	public static final Color BORDER_MIDDLE = new Color(212,212,212);
	public static final Color BORDER_INNER =  new Color(233,233,233);

	/**
	 * The color used to identify items that can be linked to the current item.
	 */
	public static final Color HIGHLIGHT_COLOR = new Color(154,51,155); 
	
	/** 
	 * The color that can be used to identify items that are the same type 
	 * (ie., {@link ModuleView}s corresponding to the same module) as the current
	 * selection 
	 */
	public static final Color SELECTED_HIGHLIGHT_COLOR = new Color(51,204,255);

	/** Tooltip border color */
	public static Color TOOLTIP_BORDER_COLOR = new Color(102,102,153);
	
	/** Tooltip fill color */
	public static Color TOOLTIP_FILL_COLOR = new Color(153,153,204);

	/** color for zooming halo around thumbnails */
	public static final Color HALO_COLOR = Color.RED;

	
	/** Dataset browser side width */
	public static final int BROWSER_SIDE=400;
	
	/** small border around some nodes */
	public static final int SMALL_BORDER=20;	
	
	/** larger border */
	public static final int BORDER=80;
	

	/** strokes and size for borders of boxes */
	public static final float STROKE_WIDTH=4.0f;
	public static final BasicStroke BORDER_STROKE = new BasicStroke(STROKE_WIDTH);
	
	/** and for module nodes */
	public static final BasicStroke MODULE_STROKE = new BasicStroke(5);
		
	/** gap between images in dataset browser */
	public static final int DATASET_IMAGE_GAP=8;
	
	/**
	 * Animation delay when scaling a node to center in the canvas. 
	 */
	public static final int ANIMATION_DELAY=500;
	
	/** a large font */
	public static final Font LARGE_NAME_FONT = 
			new Font("Helvtical",Font.BOLD,24);
	
    
	/** 
	 * Positional offsets for a category name in a {@link CategoryBox} in a 
	 * {@link ModulePaletteCanvas}
	 *
	 */
	public static final double CATEGORY_LABEL_OFFSET_X=40;
	public static final double CATEGORY_LABEL_OFFSET_Y=20;
	
	/**
	 * Size parameters for the circle at the end of a {@link Link}
	 */
	public static final float LINK_BULB_SIZE=8;
	public static final float LINK_BULB_RADIUS = LINK_BULB_SIZE/2;
	public static final float LINK_TARGET_SIZE=10;
	public static final float LINK_TARGET_HALF_SIZE=LINK_TARGET_SIZE/2;
	public static final float  LINK_TARGET_BUFFER=3;
	
	public static final BasicStroke LINK_STROKE=
			new BasicStroke(1,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND);
	
	
	/**
	 * Magnification threshold for sematic zooming. When magnification decreases
	 * past this point, zoom-out to lower-level of detail, and vice-versa.
	 */ 
	public static final double SCALE_THRESHOLD=0.5;
	
	
}