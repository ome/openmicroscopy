/*
 * org.openmicroscopy.shoola.util.ui.colourpicker.PaintPotUI
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.util.ui.colourpicker;

//Java imports
import java.awt.Color;
import java.awt.Graphics;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.PaintPot;


/** 
 * Paintpot is the colour strip along the top of the colourpicker ui which 
 * represents the current colour. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $$Revision: $$Date: $$)
 * </small>
 * @since OME2.2
 */
class PaintPotUI
	extends PaintPot
	implements PropertyChangeListener
{
	
	/** Property indicating that the color has changed. */
	static final String COLOUR_CHANGED_PROPERTY = "colourChanged"; 
	
	/** Reference to the control. */
	private JComponent				control;
	
	/**
	 * Creates the UI and attach the component to the control.
	 * 
	 * @param col The color to paint. Mustn't be <code>null</code>.
	 * @param c Reference to the control. Mustn't be <code>null</code>.
	 */
	PaintPotUI(Color col, JComponent c)
	{
		super(col);
        if (c == null) throw new NullPointerException("No control.");
		control = c;
		control.addPropertyChangeListener(COLOUR_CHANGED_PROPERTY, this);
	}
	  
    /**
     * Overridden to render the paint pot. 
     * @see javax.swing.JComponent#paintComponent(Graphics)
     */
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        render(g);
    }

	/**
	 * Reacts to color change events. Repaints the componet
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event)
	{
		colour = (Color) event.getNewValue();
		invalidate();
		repaint();
	}
    
}
