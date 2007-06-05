/*
 * org.openmicroscopy.shoola.agents.imviewer.browser.ImageCanvas 
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

import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies

/** 
 * 
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

	/** The background color of the text area. */
	static final Color		BACKGROUND = Color.BLACK;
	
	/** Reference to the Model. */
	protected BrowserModel	model;
    
    /** The string to paint on top of the image. */
    protected String		paintedString;
    
    /** The font's height. */
    protected int 			height;
    
	/**
     * Creates a new instance.
     *
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
    ImageCanvas(BrowserModel model)
    {
    	if (model == null) throw new NullPointerException("No model.");
        this.model = model;
        setBackground(model.getBackgroundColor());
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
	
}
