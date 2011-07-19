/*
 * org.openmicroscopy.shoola.util.ui.tpane.CloseButton 
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
package org.openmicroscopy.shoola.util.ui.tpane;



//Java imports
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;

import org.openmicroscopy.shoola.util.ui.IconManager;

//Third-party libraries

//Application-internal dependencies

/** 
 * Close the component.
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
public class CloseButton    
	extends JButton
    implements TinyObserver, PropertyChangeListener, ActionListener
{
 
    /** Tooltip text when the button represents the close action. */
    static final String CLOSE_TOOLTIP = "Close";
    
    /** The Model this button is working with. */
    private TinyPane   model;
    
    /**
     * Creates a new instance.
     * 
     * @param model The Model this button will be working with.
     *              Mustn't be <code>null</code>.
     */
    CloseButton(TinyPane model) 
    {
        if (model == null) throw new NullPointerException("No model.");
        setBorder(BorderFactory.createEmptyBorder());  //No border around icon.
        setMargin(new Insets(0, 0, 0, 0));//Just to make sure button sz=icon sz.
        setOpaque(false);  //B/c button=icon.
        setFocusPainted(false);  //Don't paint focus box on top of icon.
        setRolloverEnabled(true);
        this.model = model;
    }

    /**
     * Registers this button with the Model. 
     * @see TinyObserver#attach()
     */
    public void attach() 
    { 
        addActionListener(this);
        model.addPropertyChangeListener(TinyPane.CLOSED_PROPERTY, this);
        propertyChange(null);  //Synch button w/ current state.
    }
    
    /**
     * Detaches this button from the Model's change notification registry.
     * @see TinyObserver#detach()
     */
    public void detach() 
    { 
        model.removePropertyChangeListener(TinyPane.CLOSED_PROPERTY, this); 
    }
    
    /**
     * Sets the button appearence according to the collapsed state of the Model.
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent pce)
    {
        //NOTE: We can only receive COLLAPSED_PROPERTY changes, see attach().
        if (!model.isClosed()) {
        	IconManager icons = IconManager.getInstance();
            setIcon(icons.getIcon(IconManager.CROSS));
            setRolloverIcon(icons.getIcon(IconManager.CROSS_OVER));
            setToolTipText(CLOSE_TOOLTIP);
        }
    }
    
    /**
     * Expands or collapses the frame depending on the current state of the
     * frame.
     * @see ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent ae)
    {
        boolean b = !model.isClosed();
        model.setClosed(b);
    }
    
    /** 
     * Overridden to make sure no focus is painted on top of the icon. 
     * @see JButton#isFocusable()
     */
    public boolean isFocusable() { return false; }
    
    /**
     * Overridden to make sure no focus is painted on top of the icon. 
     * @see JButton#requestFocus()
     */
    public void requestFocus() {}
    
}
