/*
 * org.openmicroscopy.shoola.util.ui.ColouredButton
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2016 University of Dundee. All rights reserved.
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
import java.awt.image.BufferedImage;
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
     * Set the background image (takes precedence over color!)
     */
    private void setButtonImage(BufferedImage img) {
        colourButtonUI.setImage(img);
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
		setModel(new DefaultButtonModel());
		init(text, null);
		colourButtonUI = new ColouredButtonUI(this, color);
		setUI(colourButtonUI);
		setRolloverEnabled(false);
		setBorder(BorderFactory.createBevelBorder(3));
		setBorderPainted(true);
		this.color = color;
		setButtonColour(color);
	}

	/**
     * Creates a new instance.
     * 
     * @param text      The text of the button. 
     * @param img     The background image of the button. Corresponds to the
     *                  LUT associated to the channel.
     */
    public ColouredButton(String text, BufferedImage img)
    {
        this.color = Color.GRAY;
        setModel(new DefaultButtonModel());
        init(text, null);
        colourButtonUI = new ColouredButtonUI(this, color);
        setUI(colourButtonUI);
        setRolloverEnabled(false);
        setBorder(BorderFactory.createBevelBorder(3));
        setBorderPainted(true);
        setButtonImage(img);
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
		if (greyedOut) 
		    setButtonColour(Color.LIGHT_GRAY);
		else
		    setButtonColour(color);
		colourButtonUI.setGrayedOut(greyedOut);
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
		this.color = color;
		setButtonColour(color);
		repaint();
	}

    /**
     * Set the background image (takes precedence over color!)
     * 
     * @param img
     *            The image
     */
    public void setImage(BufferedImage img) {
        setButtonImage(img);
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
	
}
