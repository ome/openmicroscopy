/*
 * org.openmicroscopy.shoola.agents.treeviewer.finder.actions.FinderAction
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;

/** 
 * Top class that each action should extend.
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since OME2.2
 */
public class FinderAction
	extends AbstractAction
    implements PropertyChangeListener
{

    /** Reference to the Model. */
    protected Finder	model;
    
    /** 
     * Callback to notify a {@link Finder#TEXT_ENTERED_PROPERTY} property change
     * in the {@link Finder}. 
     * Subclasses override the method.
     */
    protected void onTextChanged() {} ;
    
    /** 
     * Callback to notify a {@link Finder#LEVEL_PROPERTY} property change
     * in the {@link Finder}. 
     * Subclasses override the method.
     */
    protected void onLevelChanged() {};
    
    /** 
     * Callback to notify a {@link Finder#RETRIEVED_PROPERTY} property change
     * in the {@link Finder}. 
     * Subclasses override the method.
     * 
     * @param n The number of children of the container.
     */
    protected void onRetrievedChanged(int n) {};
    
    /**
     * Creates a new instance. 
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
    public FinderAction(Finder model)
    {
        if (model == null) throw new NullPointerException("No model");
        this.model = model;
        setEnabled(false); 
    }
    
    /** 
     * Subclasses override this method.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {}
    
    /** 
     * Reacts to property changes fired by {@link Finder}. 
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent pce) 
    {
        String name = pce.getPropertyName();
        if (name.equals(Finder.TEXT_ENTERED_PROPERTY)) onTextChanged();
        else if (name.equals(Finder.LEVEL_PROPERTY)) onLevelChanged();
        else if (name.equals(Finder.RETRIEVED_PROPERTY)) 
            onRetrievedChanged(((Integer) pce.getNewValue()).intValue()); 
    }

}
