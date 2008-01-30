/*
 * org.openmicroscopy.shoola.util.ui.OutBarComponent 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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

//Third-party libraries

//Application-internal dependencies
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

/** 
 * Helper class used to store information about individual bars.
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
class OutlookBarComponent
{

	/** The name of the bar component. */
    private String 		name;

    /** Implements the bar component. */
    private JButton 	button;

    /** The component composing the core of the bar component. */
    private JComponent	component;

    /** The creation index of the component. */
    private int			index;
    
    /** Flag inidicating if the component is the current one. */
    private boolean		current;
    
    /**
     * Creates a new instance.
     * 
     * @param name		The name of the bar.
     * @param component	The main component of the bar.
     * @param icon		The icon add to the bar.
     * @param index		The index of the component.
     */
    OutlookBarComponent(String name, JComponent component, Icon icon, int index)
    {
    	this.name = name;
    	this.component = component;
    	button = new JButton(name, icon);
    	this.index = index;
    	Border emptyBorder = BorderFactory.createEmptyBorder(0, 0, 3, 0);
    	Border etchedBorder = BorderFactory.createEtchedBorder(
    						EtchedBorder.LOWERED);
    	button.setBorder(BorderFactory.createCompoundBorder(
				etchedBorder, emptyBorder));
    }
    
    /**
     * Sets to <code>true</code> if the component is current,
     * <code>false</code> otherwise.
     * 
     * @param current	The value to set.
     */
    void setCurrent(boolean current) { this.current = current; }
    
    /**
     * Returns <code>true</code> if the component is current,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean isCurrent() { return current; }
    
    /**
     * Returns the component that implements the body of the bar.
     * 
     * @return See above.
     */
    JComponent getComponent() { return component; }
    
    /**
     * Returns the name of the bar.
     * 
     * @return See above.
     */
    String getName() { return name; }
    
    /** 
     * Returns the index of the selected bar.
     * 
     * @return See above.
     */
    int getIndex() { return index; }
    
    /**
     * Returns the button used to select the component.
     * 
     * @return See above.
     */
    JButton getButton() { return button; }
    
}
