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
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.openmicroscopy.shoola.util.ui.PaintPot;

//Third-party libraries

//Application-internal dependencies

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
	public static final String COLOURCHANGEEVENT = "ColourChangeEvent"; 
	
	/** Reference to the control. */
	private JComponent				control;
	
	/**
	 * Creates the UI and attach the component to the control.
	 * 
	 * @param c Reference to the control. Mustn't be <code>null</code>.
	 */
	PaintPotUI(Color col, JComponent c)
	{
		super(col);
        if (c == null) throw new NullPointerException("No control.");
		control = c;
		control.addPropertyChangeListener(this);
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

	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event)
	{
		if(event.getPropertyName()==COLOURCHANGEEVENT)
		{
			colour = (Color)event.getNewValue();
			invalidate();
			repaint();
		}
	}
    
}
