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
import java.awt.GradientPaint;
import java.awt.geom.Point2D;
import javax.swing.DefaultButtonModel;


//Third-party libraries
import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.painter.CompoundPainter;
import org.jdesktop.swingx.painter.GlossPainter;
import org.jdesktop.swingx.painter.MattePainter;
import org.jdesktop.swingx.painter.Painter;
import org.jdesktop.swingx.painter.RectanglePainter;

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
//    extends AbstractButton
	extends JXButton
{
     
	/** The foreground color when the color of the button is dark. */
	private static final Color	FOREGROUND_DARK = Color.BLACK.brighter();
	
	/** The foreground color when the color of the button is light. */
	private static final Color	FOREGROUND_LIGHT = Color.DARK_GRAY.darker();//Color.GRAY.darker();
	
	/** The default color. */
	private static final Color	DEFAULT = new Color(255, 255, 255, 
											(int) (255*0.5));
	
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
    	Color newColor = color.darker();
        Color translucent = new Color(newColor.getRed(), newColor.getGreen(), 
        	newColor.getBlue(), 0);
        
        //Maybe use foregroundPainter
        if (UIUtilities.isDarkColor(newColor)) 
        	setForeground(FOREGROUND_LIGHT);
        else setForeground(FOREGROUND_DARK);
        
        GradientPaint bgToTranslucent = new GradientPaint(
			new Point2D.Double(.4, 0), newColor,
			new Point2D.Double(1, 0), translucent);
        MattePainter veil = new MattePainter(bgToTranslucent);
        veil.setPaintStretched(true);
        Painter backgroundPainter = new RectanglePainter(color, null);
        setBackgroundPainter(new CompoundPainter(new GlossPainter(), veil, 
   			 backgroundPainter));
    }
    
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
    	if (color == null) 
            throw new IllegalArgumentException("No color.");
        setModel(new DefaultButtonModel());
    	init(text, null);
       // uiDelegate = new ColouredButtonUI(this, color);
      //  setUI(uiDelegate);
        setRolloverEnabled(false);
        setBorder(null);
    	this.color = color;
        setButtonColour(color);
    }
    
     /**
     * Sets the button to be greyed out.
     *  
     * @param greyedOut Pass <code>true</code> to gray out the button,
     *                  <code>false</code> otherwise.
     */
    public void setGrayedOut(boolean greyedOut)
    {
    	//if (uiDelegate == null) return;
       	//	uiDelegate.setGrayedOut(greyedOut);
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
    	//if (uiDelegate != null && c != null)
    	//	uiDelegate.setColor(c); 
        setButtonColour(color);
        repaint();
    }
    
    /**
     * Sets the index of the derived font used to paint the text.
     * 
     * @param index The font index. 
     */
    public void setFontIndex(int index)
    {
        //if (uiDelegate != null) uiDelegate.setDeriveFont(index);
    	getFont().deriveFont(index);
    }
    
    /**
     * Overridden. Does nothing as it's overwritten by laf and breaks the 
     * coloured button.
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
