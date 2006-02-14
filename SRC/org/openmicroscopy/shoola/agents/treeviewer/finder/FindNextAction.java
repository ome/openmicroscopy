/*
 * org.openmicroscopy.shoola.agents.treeviewer.finder.actions.FindNextAction
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
import javax.swing.Action;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.finder.Finder;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Action to find the next occurence of the phrase.
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$Date: )
 * </small>
 * @since OME2.2
 */
public class FindNextAction
	extends FinderAction
{
    
    /** The name of the action. */
    private static final String NAME = "Find Next";
    
    /** The description of the action. */
    private static final String DESCRIPTION = "Finds the next occurence of " +
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
                && !model.isAnnotationSelected()) setEnabled(false);
        else  setEnabled(true);  
    }
    
    /** 
     * Sets the action enabled depending on the retrieved data.
     * @see FinderAction#onRetrievedChanged(int)
     */
    protected void onRetrievedChanged(int n) { setEnabled(n != 0); }
    
    /**
     * Creates a new instance. 
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
    public FindNextAction(Finder model)
    {
        super(model);
        putValue(Action.NAME, NAME);
        putValue(Action.SHORT_DESCRIPTION, 
                UIUtilities.formatToolTipText(DESCRIPTION));
        IconManager im = IconManager.getInstance();
        putValue(Action.SMALL_ICON, im.getIcon(IconManager.FIND_NEXT));
        //register for property change.
        model.addPropertyChangeListener(Finder.TEXT_ENTERED_PROPERTY, this);
        model.addPropertyChangeListener(Finder.LEVEL_PROPERTY, this);
        model.addPropertyChangeListener(Finder.RETRIEVED_PROPERTY, this);
    }

    /**
     * Finds the next occurence of the phrase.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e) { model.findNext(); }
    
}
