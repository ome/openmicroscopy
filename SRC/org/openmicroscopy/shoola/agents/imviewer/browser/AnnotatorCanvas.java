/*
 * org.openmicroscopy.shoola.agents.imviewer.browser.AnnotatorCanvas 
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
package org.openmicroscopy.shoola.agents.imviewer.browser;


//Java imports
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.util.ImagePaintingFactory;

/** 
 * UI component where a smaller (40%) version of the rendered image is painted.
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
class AnnotatorCanvas 
	extends JPanel
{

	/** The background color of the text area. */
	private static final Color	BACKGROUND = Color.BLACK;
	
	/** Reference to the Model. */
    private BrowserModel    model;
    
    /** The string to paint on top of the image. */
    private String			paintedString;
    
    /** The font's height. */
    private int 			height;
    
	/**
     * Creates a new instance.
     *
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
	AnnotatorCanvas(BrowserModel model)
    {
        if (model == null) throw new NullPointerException("No model.");
        this.model = model;
        //setLayout(null);
        setDoubleBuffered(true);
        setFont(getFont().deriveFont(10f));
        FontMetrics fm = getFontMetrics(getFont());
        height = fm.getHeight();
        paintedString = null;
    }
    
	/**
	 * Sets the value of the selected z-section and timepoint.
	 * 
	 * @param pressedZ	The selected z-section.
	 * @param pressedT	The selected timepoint.
	 */
	void setPaintedString(int pressedZ, int pressedT)
	{
		if (pressedZ < 0 || pressedT < 0)  paintedString = null;
		else paintedString = "z="+pressedZ+", t="+pressedT;
		repaint();
	}
	
	/**
     * Overridden to paint the image.
     * @see javax.swing.JComponent#paintComponent(Graphics)
     */
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        
        BufferedImage img = model.getAnnotateImage();
        if (img == null) return;
        Graphics2D g2D = (Graphics2D) g;
        ImagePaintingFactory.setGraphicRenderingSettings(g2D);
        g2D.drawImage(img, null, 0, 0); 
        if (paintedString != null) {
        	FontMetrics fm = getFontMetrics(getFont());
        	g2D.setColor(BACKGROUND);
        	int w = fm.stringWidth(paintedString);
        	g2D.fillRect(0, 0, w+4, 3*height/2);
        	g2D.setColor(getBackground());
        	g2D.drawString(paintedString, 2, height);
        }
        	
    }
    
}
