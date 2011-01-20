/*
 * org.openmicroscopy.shoola.util.ui.border.TitledLineBorder 
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
package org.openmicroscopy.shoola.util.ui.border;


//Java imports
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

//Third-party libraries

//Application-internal dependencies

/** 
 * Extends title border but only draws the top (resp. bottom) of the border if
 * the title position is at the top (resp. bottom).
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
public class TitledLineBorder 
	extends TitledBorder
{

	/** The default color of the line. */
	private static final Color 	DEFAULT_COLOR = Color.LIGHT_GRAY;
	
	/** Extra space added to the bounds of the close button. */
	private static final int	EDGE_GAP = 2;
	
	/** Location of the text. */
	protected Point			textLoc;
	
	/** The color of the line. */
	private Color			lineColor;

	/** Collection of images to add, Images correspond to button. */
	private List<Image>		images;
	
	/** Bounds of the images. */
	private List<Rectangle>	imageBounds;
	
	/**
	 * Returns <code>true</code> if the passed rectangle intersects with 
	 * the passed parameters defining the rectangle of reference, 
	 * <code>false</code> otherwise.
	 * 
	 * @param dest 	The rectangle to handle.
	 * @param rx	The x-coordinate of the top-left corner of the rectangle 
	 * 				of reference.
	 * @param ry	The y-coordinate of the top-left corner of the rectangle 
	 * 				of reference.
	 * @param rw	The width of the rectangle of reference.
	 * @param rh	The height of the rectangle of reference.
	 * @return See above.
	 */
	private static boolean computeIntersection(Rectangle dest, int rx, int ry, 
												int rw, int rh) 
	{
		int x1 = Math.max(rx, dest.x);
		int x2 = Math.min(rx+rw, dest.x+dest.width);
		int y1 = Math.max(ry, dest.y);
		int y2 = Math.min(ry+rh, dest.y+dest.height);
		dest.x = x1;
		dest.y = y1;
		dest.width = x2-x1;
		dest.height = y2-y1;
		if (dest.width <= 0 || dest.height <= 0) return false;
		return true;
	}  
	
	/**
     * Creates a new instance.
     * 
     * @param title  	The title the border should display.
     * @param lineColor	The color of the line.
     */
	public TitledLineBorder(String title, Color lineColor)
	{
		super(title);
		textLoc = new Point();
		setLineColor(lineColor);
	}
	
    /**
     * Creates a new instance.
     * 
     * @param title  The title the border should display.
     */
	public TitledLineBorder(String title)
	{
		this(title, null);
	}
	
	/**
	 * Sets the color of the line.
	 * 
	 * @param lineColor The color of the line.
	 */
	public void setLineColor(Color lineColor)
	{
		if (lineColor == null) this.lineColor = DEFAULT_COLOR;
		else this.lineColor = lineColor;
	}
	
	/**
	 * Sets the collections of images.
	 * 
	 * @param images The collection of images to set.
	 */
	public void setImages(List<Image> images) { this.images = images; }
	
	/**
	 * Returns the collection of images' bounds.
	 * 
	 * @return See above.
	 */
	public List<Rectangle> getImagesBounds() { return imageBounds; }
	
	/**
     * Overridden to remove one segment of the border, either the top or 
     * bottom part.
     * @see TitledBorder#paintBorder(Component, Graphics, int, int, int, int)
     */
    public void paintBorder(Component c, Graphics g, int x, int y, int width, 
    						int height) 
    {
    	 g.setColor(Color.RED);
    	 Border border = getBorder();

         if (getTitle() == null || getTitle().equals("")) {
             if (border != null)
                 border.paintBorder(c, g, x, y, width, height);
             return;
         }

         Rectangle grooveRect = new Rectangle(x+EDGE_SPACING, y+EDGE_SPACING,
                                              width-(EDGE_SPACING*2),
                                              height-(EDGE_SPACING*2));
         Font font = g.getFont();
         Color color = g.getColor();
         Font fontc = getFont(c);
         if (fontc != null) g.setFont(fontc.deriveFont(Font.BOLD));
         
         FontMetrics fm = g.getFontMetrics(g.getFont());
         int fontHeight = fm.getHeight();
         int descent = fm.getDescent();
         int ascent = fm.getAscent();
         int diff;
         int stringWidth = fm.stringWidth(getTitle());
         if (getTitle().equals("")) stringWidth = 0;
         Insets insets;

         if (border != null) insets = border.getBorderInsets(c);
         else insets = new Insets(0, 0, 0, 0);
         
         int titlePos = getTitlePosition();
         switch (titlePos) {
             case ABOVE_TOP:
                 diff = ascent+descent+Math.max(EDGE_SPACING, TEXT_SPACING*2)-
                 		EDGE_SPACING;
                 grooveRect.y += diff;
                 grooveRect.height -= diff;
                 textLoc.y = grooveRect.y-descent-TEXT_SPACING;
                 break;
             case TOP:
             case DEFAULT_POSITION:
                 diff = Math.max(0, ((ascent/2)+TEXT_SPACING)-EDGE_SPACING);
                 grooveRect.y += diff;
                 grooveRect.height -= diff;
                 textLoc.y = grooveRect.y-descent+(insets.top+ascent+descent)/2;
                 break;
             case BELOW_TOP:
                 textLoc.y = grooveRect.y+insets.top+ascent+TEXT_SPACING;
                 break;
             case ABOVE_BOTTOM:
                 textLoc.y = grooveRect.y+grooveRect.height-
                 			(insets.bottom+descent+TEXT_SPACING);
                 break;
             case BOTTOM:
                 grooveRect.height -= fontHeight/2;
                 textLoc.y = grooveRect.y+grooveRect.height-descent +
                         	(ascent+descent-insets.bottom)/2;
                 break;
             case BELOW_BOTTOM:
                 grooveRect.height -= fontHeight;
                 textLoc.y = grooveRect.y+grooveRect.height+ascent+TEXT_SPACING;
                 break;
         }

         int justification = getTitleJustification();
         if (c.getComponentOrientation().isLeftToRight()) {
	 	    if (justification == LEADING || 
	 	    	justification == DEFAULT_JUSTIFICATION) 
	 	        justification = LEFT;
	 	    
	 	    else if (justification == TRAILING) 
	 	    	justification = RIGHT;
 	    
         } else {
	 	    if (justification == LEADING ||
	 	        justification == DEFAULT_JUSTIFICATION) 
	 	        justification = RIGHT;
	 	    
	 	    else if (justification == TRAILING) 
	 	        justification = LEFT;
         }

         switch (justification) {
             case LEFT:
                 textLoc.x = grooveRect.x+TEXT_INSET_H+insets.left;
                 break;
             case RIGHT:
                 textLoc.x = (grooveRect.x+grooveRect.width)-
                         (stringWidth+TEXT_INSET_H+insets.right);
                 break;
             case CENTER:
                 textLoc.x = grooveRect.x +((grooveRect.width-stringWidth)/2);
                 break;
         }

         // If title is positioned in middle of border AND its fontsize
         // is greater than the border's thickness, we'll need to paint 
         // the border in sections to leave space for the component's background 
         // to show through the title.
         //
         if (border != null) {
             if (((titlePos == TOP || titlePos == DEFAULT_POSITION) &&
            		 (grooveRect.y > textLoc.y-ascent)) ||
            		 (titlePos == BOTTOM && 
            		(grooveRect.y+grooveRect.height < textLoc.y+descent))) {
 		  
                 Rectangle clipRect = new Rectangle();
                 
                 // save original clip
                 Rectangle saveClip = g.getClipBounds();

                 // paint strip left of text
                 clipRect.setBounds(saveClip);
                 if (computeIntersection(clipRect, x, y, textLoc.x-1-x, height)) 
                 {
                     g.setClip(clipRect);
                     g.setColor(lineColor);
                     g.drawLine(grooveRect.x, grooveRect.y,
                			 grooveRect.x+grooveRect.width, grooveRect.y);
                     g.setColor(color);
                 }

                 // paint strip right of text
                 clipRect.setBounds(saveClip);
                 if (computeIntersection(clipRect, textLoc.x+stringWidth+1, y,
                                x+width-(textLoc.x+stringWidth+1), height)) {
                     g.setClip(clipRect);
                     g.setColor(lineColor);
                	 g.drawLine(grooveRect.x, grooveRect.y,
                			 grooveRect.x+grooveRect.width, grooveRect.y);
                	 g.setColor(color);
                 }

                 if (titlePos == TOP || titlePos == DEFAULT_POSITION) {
                     // paint strip below text
                     clipRect.setBounds(saveClip);
                     if (computeIntersection(clipRect, textLoc.x-1, 
                    		 textLoc.y+descent, stringWidth+2, 
                    		 y+height-textLoc.y-descent)) {
                         g.setClip(clipRect);
                         g.setColor(lineColor);
                    	 g.drawLine(grooveRect.x, grooveRect.y,
                    			 grooveRect.x+grooveRect.width, grooveRect.y);
                    	 g.setColor(color);
                     }
                  		
                 } else { // titlePos == BOTTOM
                	 // paint strip above text
                     clipRect.setBounds(saveClip);
                     if (computeIntersection(clipRect, textLoc.x-1, y, 
                           stringWidth+2, textLoc.y - ascent - y)) {
                         g.setClip(clipRect); 
                         g.setColor(lineColor);
                    	 g.drawLine(grooveRect.x, 
                    			 grooveRect.y+grooveRect.height,
                    			 grooveRect.x+grooveRect.width, 
                    			 grooveRect.y+grooveRect.height);
                    	 g.setColor(color);
                     }
                 }

                 // restore clip
                 g.setClip(saveClip);   
             } else {
                 border.paintBorder(c, g, grooveRect.x, grooveRect.y,
                                   grooveRect.width, grooveRect.height);
             }
         }
         
         g.setColor(getTitleColor());
         g.drawString(getTitle(), textLoc.x, textLoc.y);
         g.setFont(font);
         g.setColor(color);
         
         if (images == null || images.size() == 0) return;
         Iterator i = images.iterator();
         Rectangle r;
         Image img;
         x = textLoc.x+fm.stringWidth(getTitle())+2;
         y = textLoc.y;
         int w, h;
         imageBounds = new ArrayList<Rectangle>(images.size());
         while (i.hasNext()) {
        	 r = new Rectangle();
        	 img = (Image) i.next();
        	 w = img.getWidth(null);
        	 h = img.getHeight(null);
        	 r.setBounds(x, y-3*h/4, w, h);
        	 g.drawImage(img, x, y-3*h/4, w, h, null); 
        	 x += w;
        	 x += EDGE_GAP;
        	 imageBounds.add(r);
         }
    }
    
}
