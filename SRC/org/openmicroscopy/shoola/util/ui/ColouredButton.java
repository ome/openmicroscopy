/*
 * org.openmicroscopy.shoola.util.ui.ColouredButton
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
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

package org.openmicroscopy.shoola.util.ui;


//Java imports
import java.awt.Color;
import java.awt.Graphics;
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
    
    /** The UI for this button. */
    private ColouredButtonUI uiDelegate;
    
    /**
     * Creates a new instance.
     * 
     * @param text      The text of the button. The text should correspond to
     *                  the emission wavelength.
     * @param color     The background color of the button. Corresponds to the
     *                  color associated to the channel.
     */
    public ColouredButton(String text, Color color)
    {
        super(text);
        if (color == null) 
            throw new IllegalArgumentException("No color.");
        setBackground(color);
        uiDelegate = new ColouredButtonUI(this, color);
        //setUI(uiDelegate);
        setRolloverEnabled(false);
    }
    
    /**
     * Sets the button to be greyed out.
     *  
     * @param greyedOut Pass <code>true</code> to gray out the button,
     *                  <code>false</code> otherwise.
     */
    public void setGrayedOut(boolean greyedOut)
    {
        if (uiDelegate == null) return;
        uiDelegate.setGrayedOut(greyedOut);
        repaint();
    }
    
    /**
     * Sets the Background colour of the button, this will 
     * be used as the base colour to generate the gradient fill of the 
     * buttons. 
     * 
     * @param c The color to set.
     */
    public void setColor(Color c) { setBackground(c); }
    
    /**
     * Sets the index of the derived font used to paint the text.
     * 
     * @param index The font index. 
     */
    public void setFontIndex(int index)
    {
        if (uiDelegate != null) uiDelegate.setDeriveFont(index);
    }
    
    /**
     * Overridden. Sets the Background colour of the button, this will 
     * be used as the base colour to generate the gradient fill of the 
     * buttons. 
     * @see javax.swing.JComponent#setBackground(Color)
     */
    public void setBackground(Color c)
    {
        if (uiDelegate != null) uiDelegate.setColor(c);
        super.setBackground(c);
    }
    
    /**
     * Overridden to paint the button.
     * @see JButton#paintComponent(Graphics)
     */
    public void paintComponent(Graphics g)
    {
        if (uiDelegate != null) uiDelegate.paint(g, this);
    }
    
}
