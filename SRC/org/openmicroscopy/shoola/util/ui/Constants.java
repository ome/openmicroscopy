/*
 * org.openmicroscopy.shoola.agents.zoombrowser.piccolo.Constants
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

package org.openmicroscopy.shoola.util.ui;

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
public class Constants {


	/** default label color   */ 
    public static final Color DEFAULT_COLOR = Color.BLACK;
	public static final Color DEFAULT_TEXT_COLOR = DEFAULT_COLOR;
	public static final Color EXECUTION_SELECTED_FILL = new Color(255,124,124);
	public static final Color SELECTED_FILL = new Color(120,154,255);


    public static final Color DEFAULT_FILL = new Color(182,209,255);
	
   	/** default label font */
	public static final Font PROJECT_LABEL_FONT = 
			new Font("Helvetica",Font.BOLD,10);

	/** font for module names */	
	public static final Font NAME_FONT = new Font("Helvetica",Font.BOLD,14);	

	/** font for STs */
	public static final Font ST_FONT = new Font("Helvetica",Font.BOLD,7);	

	/** font for tooltips */		
	public static final Font TOOLTIP_FONT = new Font("Helvetica",Font.BOLD,12);
	
	/** small tooltips */
	public static final Font SMALL_TOOLTIP_FONT = new Font("Helvetica",Font.BOLD,8);
	
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
	public static final Color CANVAS_BACKGROUND_COLOR = new Color(227,227,227);
	
	/** an alternative background */
	public static final Color ALT_BACKGROUND_COLOR = new Color(215,215,215);
	
	/** Highlighting of a link  between modules */
	public static final Color LINK_HIGHLIGHT_COLOR=Color.WHITE;
    
	/** basic and highlight colors for NEXes */
	public static final Color NEX_COLOR = new Color(128,144,183);
	public static final Color NEX_HIGHLIGHT_COLOR = new Color(251,24,251);
	public static final Color NEX_COUNT_COLOR = new Color(64,80,119);
	
	/* NEX size */
	public static final float NEX_SIDE=4.0f;
	
	/** Highlighting of generic boxes*/
	public static final Color HIGHLIGHT_COLORS[] = {
			new Color(199,94,0),
			new Color(207,117,23),
			new Color(215,140,46),
			new Color(223,163,69),
			new Color(231,186,92),
	};
	
	 /** Borders of generic boxes */
	public static final Color BORDER_COLORS[] = {
			new Color(149,149,149),
			new Color(170,170,170),
			new Color(191,191,191),
			new Color(212,212,212),
			new Color(233,233,233)
	};
	
	public static final Color SINGLE_HIGHLIGHT_COLOR=HIGHLIGHT_COLORS[0];
	
	/** color for a locked chain */
	public static final Color LOCKED_COLOR = Color.RED;
	/**
	 * The color used to identify items that can be linked to the current item.
	 */
	public static final Color HIGHLIGHT_COLOR = new Color(154,51,155); 
	
	/** 
	 * The color that can be used to link thumbnails based on being the same color
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
	
	/** stroke for thumbnail highlight */
	public static final BasicStroke THUMBNAIL_HIGHLIGHT_STROKE=new BasicStroke(2);
	/** and for module nodes */
	public static final BasicStroke MODULE_STROKE = new BasicStroke(12);
		
	/** gap between images in dataset browser */
	public static final int DATASET_IMAGE_GAP=2;
	
	/**
	 * Animation delay when scaling a node to center in the canvas. 
	 */
	public static final int ANIMATION_DELAY=500;
	
	/** Transparency delay for changing transparency in place */
	public static final int TRANSPARENCY_DELAY=1500;
	
	/** a large font */
	public static final Font LARGE_NAME_FONT = 
			new Font("Helvtical",Font.BOLD,24);
	
	public static final Font LABEL_FONT  = new Font("Helvetica",Font.BOLD,18);
	   
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
	
	public static final float MODULE_LINK_TARGET_SIZE=14;
	
	
	public static final BasicStroke LINK_STROKE=
			new BasicStroke(1,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND);
	
	
	/**
	 * Magnification threshold for sematic zooming. When magnification decreases
	 * past this point, zoom-out to lower-level of detail, and vice-versa.
	 */ 
	public static final double SCALE_THRESHOLD=0.5;
	
	/** how much to move in or out when scaling chain creation */
	public static final double SCALE_FACTOR=1.2;
	
	
	/** how much to move in or out when scaling chain palette */
	public static final double LARGE_SCALE_FACTOR=1.6;
	
	/** parameter label transparency */
	public static final float MODULE_TRANSPARENT=0.5f;
	public static final float MODULE_OPAQUE=1.0f;
}