/*
 * org.openmicroscopy.shoola.agents.treeviewer.finder.actions.FindAction
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

package org.openmicroscopy.shoola.agents.treeviewer.finder;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Action to find all occurrences of the phrase.
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since OME2.2
 */
public class FindAction 
	extends FinderAction
{
    
    /** The name of the action. */
    private static final String NAME = "Find";
    
    /** The description of the action. */
    private static final String DESCRIPTION = "Finds all occurrences of " +
	  											"the phrase.";
    
    /** 
     * Sets the action enabled depending on the text entered.
     * @see FinderAction#onTextChanged()
     */
    protected void onTextChanged() { setEnabled(!model.isTextEmpty()); }
    
    /** 
     * Sets the action enabled depending on the level selected.
     * @see FinderAction#onLevelChanged()
     */
    protected void onLevelChanged() 
    {
        if (!model.isNameSelected() && !model.isDescriptionSelected()
                && !model.isAnnotationSelected())
            setEnabled(false);
        else  setEnabled(true);  
    }
    
    /**
     * Creates a new instance. 
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
    public FindAction(Finder model)
    {
        super(model);
        putValue(Action.NAME, NAME);
        putValue(Action.SHORT_DESCRIPTION, 
                UIUtilities.formatToolTipText(DESCRIPTION));
        //register for property change.
        model.addPropertyChangeListener(Finder.TEXT_ENTERED_PROPERTY, this);
        model.addPropertyChangeListener(Finder.LEVEL_PROPERTY, this);
    }

    /**
     * Finds the phrase.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e) { model.find(); }
    
}
