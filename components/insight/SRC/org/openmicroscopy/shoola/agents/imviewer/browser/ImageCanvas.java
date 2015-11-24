/*
 * org.openmicroscopy.shoola.agents.imviewer.browser.ImageCanvas 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.imviewer.browser;


//Java imports
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JPanel;
import javax.swing.JViewport;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.util.ImagePaintingFactory;

/** 
 * Components where the image to display is painted.
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
class ImageCanvas
	extends JPanel
{

	/** Indicate to display the bird eye in <code>Top left</code>.*/
	static final int TOP_LEFT = 0;
	
	/** Indicate to display the bird eye in <code>Bottom right</code>.*/
	static final int BOTTOM_RIGHT = 1;
	
	/** The background color of the text area. */
	static final Color		BACKGROUND = Color.BLACK;
	
	/** Reference to the Model. */
	protected BrowserModel	model;
    
	/** Reference to the Model. */
	protected BrowserUI 	view;
	
    /** The string to paint on top of the image. */
    protected String		paintedString;
    
    /** The font's height. */
    protected int 			height;
    
	/**
     * Creates a new instance.
     *
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
    ImageCanvas(BrowserModel model, BrowserUI view)
    {
    	if (model == null) throw new NullPointerException("No model.");
    	if (view == null) throw new NullPointerException("No view.");
        this.model = model;
        this.view = view;
        setBackground(model.getBackgroundColor());
        setDoubleBuffered(true);
        setFont(getFont().deriveFont(10f));
        FontMetrics fm = getFontMetrics(getFont());
        height = fm.getHeight();
        paintedString = null;
    }
    
    /**
	 * Sets the value of the selected z-section and time-point.
	 * 
	 * @param pressedZ	The selected z-section.
	 * @param pressedT	The selected time-point.
	 */
	void setPaintedString(int pressedZ, int pressedT)
	{
		if (pressedZ < 0 || pressedT < 0)  paintedString = null;
		else paintedString = "z="+pressedZ+", t="+pressedT;
		repaint();
	}
	
	/** 
	 * Paints the scale bar on the specified graphics context.
	 * 
	 * @param g2D 		The graphics context.
	 * @param width 	The image's width.
	 * @param height 	The image's height.
	 * @param viewPort 	The viewport hosting the canvas.
	 */
	void paintScaleBar(Graphics2D g2D, int width, int height,
					JViewport viewPort)
	{
		if (!model.isUnitBar()) return;

		String value = model.getUnitBarValue();
		if (value == null) 
		    return;

		value += " "+model.getUnitBarUnit();
		
		int size = (int) (model.getUnitBarSize());
		
		// Position scalebar in the bottom left of the viewport or
		// the image which ever is viewable. 
		Rectangle imgRect = new Rectangle(0, 0, width, height);
		Rectangle viewRect = viewPort.getBounds();
		Point p = viewPort.getViewPosition();
		int x = (int) p.getX();
		int y = (int) p.getY();
		int w = Math.min(x+viewRect.width, width);
		int h = Math.min(y+viewRect.height, height);
		if (imgRect.contains(viewRect)) {
			w = x+viewRect.width;
			h = y+viewRect.height;
		}
		if (imgRect.getWidth() < size)
			size = 1;
		if (viewRect.width >= size && size > 1) {
			switch (view.getBirdEyeViewLocationIndex()) {
				case ImageCanvas.BOTTOM_RIGHT:
					ImagePaintingFactory.paintScaleBar(g2D, x+10, h-10,
							size, value, model.getUnitBarColor());
					break;
				case ImageCanvas.TOP_LEFT:
				default:
					ImagePaintingFactory.paintScaleBar(g2D, w-size-10, h-10,
							size, value, model.getUnitBarColor());
			}
		}
	}
	
}
