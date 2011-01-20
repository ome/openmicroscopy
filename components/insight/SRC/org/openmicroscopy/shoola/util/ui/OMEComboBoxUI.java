/*
 * org.openmicroscopy.shoola.util.ui.OMEComboBoxUI 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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

import javax.swing.JButton;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;

//Third-party libraries

//Application-internal dependencies

/** 
 * Extends the Ui to repaint the arrow button.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class OMEComboBoxUI 
	extends BasicComboBoxUI
{

	/** The background color of the arrow button. */
	private Color backgroundColor;
	
	/**
     * Creates an button which will be used as the control to show or hide
     * the popup portion of the combo box.
     *
     * @return a button which represents the popup control
     */
    protected JButton createArrowButton()
    {
    	if (backgroundColor == null)
    		backgroundColor = UIManager.getColor("ComboBox.buttonBackground");
        JButton button = new OMEBasicArrowButton(BasicArrowButton.SOUTH,
        		backgroundColor, UIManager.getColor("ComboBox.buttonShadow"),
        		UIManager.getColor("ComboBox.buttonDarkShadow"),
        		UIManager.getColor("ComboBox.buttonHighlight"));
        button.setName("ComboBox.arrowButton");
        return button;
    }
    
    /**
     * Sets the background color of the button.
     * 
     * @param backgroundColor The color to set.
     */
    public void setBackgroundColor(Color backgroundColor)
    {
    	this.backgroundColor = backgroundColor;
    	UIManager.put("ComboBox.buttonBackground", backgroundColor);
    }
    
}
