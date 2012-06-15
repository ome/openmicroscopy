/*
 * org.openmicroscopy.shoola.util.ui.ColouredButton
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

package org.openmicroscopy.shoola.util.ui;


//Java imports
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.DefaultButtonModel;
import javax.swing.JButton;


//Third-party libraries

//Application-internal dependencies

/** 
 * Customized button used to select the rendered channel.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class ColouredButton
	extends JButton
{
	
	/** UI for the colour button. */
	private ColouredButtonUI colourButtonUI;
	
	/** The background color. */
	private Color	color;

	/** Flag indicating if the button is greyed out or not. */
	private boolean greyedOut;
	
	/**
	 * Sets the color of the button.
	 * 
	 * @param color The background color of the button. Corresponds to the
	 *                  color associated to the channel.
	 */
	private void setButtonColour(Color color)
	{
		colourButtonUI.setColor(color);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param text      The text of the button. 
	 * @param color     The background color of the button. Corresponds to the
	 *                  color associated to the channel.
	 */
	public ColouredButton(String text, Color color)
	{
		if (color == null) color = Color.GRAY;
		setModel(new DefaultButtonModel());
		init(text, null);
		colourButtonUI = new ColouredButtonUI(this, color);
		setUI(colourButtonUI);
		setRolloverEnabled(false);
		setBorder(BorderFactory.createBevelBorder(3));
		setBorderPainted(true);
		this.color = color;
		greyedOut = false;
		setButtonColour(color);
	}

	/**
	 * Changes to size need to be reflected in changes in painters. 
	 * @see JButton#setSize(Dimension)
	 */
	public void setSize(Dimension d)
	{
		super.setSize(d);
		setColor(color);
	}
	
	/**
	 * Sets the button to be greyed out.
	 *  
	 * @param greyedOut Pass <code>true</code> to gray out the button,
	 *                  <code>false</code> otherwise.
	 */
	public void setGrayedOut(boolean greyedOut)
	{
		this.greyedOut = greyedOut;
		if (greyedOut) setButtonColour(Color.LIGHT_GRAY);
		else setButtonColour(color);
		repaint();
	}

	/**
	 * Sets the Background colour of the button, this will 
	 * be used as the base colour to generate the gradient fill of the 
	 * buttons. 
	 * 
	 * @param color The color to set.
	 */
	public void setColor(Color color) 
	{ 
		if (color == null) return;
		this.color = color;
		setGrayedOut(greyedOut);
		//setButtonColour(color);
		repaint();
	}

	/**
	 * Returns the color.
	 * 
	 * @return See above.
	 */
	public Color getColor() { return color; }
	
	/**
	 * Sets the index of the derived font used to paint the text.
	 * 
	 * @param index The font index. 
	 */
	public void setFontIndex(int index)
	{
		getFont().deriveFont(index);
	}

	/**
	 * Overridden. Does nothing as it's overwritten by L&F and breaks the 
	 * colored button.
	 * @see javax.swing.JComponent#setBackground(Color)
	 */
	public void setBackground(Color c) {}

	/** 
	 * Overridden.
	 * Needed for the MacOS Layout Managers to work correctly.
	 * @see  javax.swing.JComponent#getMinimumSize()
	 */
	public Dimension getMinimumSize() { return getPreferredSize(); }

	/** 
	 * Overridden.
	 * Needed for the MacOS Layout Managers to work correctly.
	 * @see  javax.swing.JComponent#getMaximumSize()
	 */
	public Dimension getMaximumSize() { return getPreferredSize(); }

	/**
	 * Changes to size need to be reflected in changes in painters. 
	 * @see JButton#setSize(int, int)
	 */
	public void setSize(int x, int y)
	{
		super.setSize(x,y);
		setColor(color);
	}
	
	/**
	 * Changes to size need to be reflected in changes in painters. 
	 * @see JButton#setPreferredSize(Dimension)
	 */
	public void setPreferredSize(Dimension preferredSize)
	{
		super.setPreferredSize(preferredSize);
		setColor(color);
	}
	
}
