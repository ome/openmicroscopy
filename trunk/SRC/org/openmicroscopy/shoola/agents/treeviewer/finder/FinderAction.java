/*
 * org.openmicroscopy.shoola.agents.treeviewer.finder.actions.FinderAction
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

package org.openmicroscopy.shoola.agents.treeviewer.finder;



//Java imports
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.finder.Finder;

/** 
 * Top class that each action should extend.
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$Date: )
 * </small>
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
