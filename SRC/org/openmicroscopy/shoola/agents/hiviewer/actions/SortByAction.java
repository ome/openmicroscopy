/*
 * org.openmicroscopy.shoola.agents.hiviewer.actions.SortByAction
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

package org.openmicroscopy.shoola.agents.hiviewer.actions;



//Java imports
import java.awt.event.ActionEvent;

import javax.swing.Action;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.IconManager;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;

/** 
 * Sorts by date or by name depending on the index.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class SortByAction
    extends HiViewerAction
{

    /** Indicates to sort the nodes by date. */
    public static final int     BY_DATE = 0;
    
    /** Indicates to sort the nodes by name. */
    public static final int     BY_NAME = 1;
    
    /** The name of the action. */
    private static final String DESCRIPTION_NAME = "Sort the nodes by name";
    
    /** The name of the action. */
    private static final String DESCRIPTION_DATE = "Sort the nodes by date.";
    
    /** One of the constants defined by this class. */
    private int index;
    
    /**
     * Controls if the specified index is valid.
     * 
     * @param i The value to check.
     */
    private void checkIndex(int i)
    {
        IconManager im = IconManager.getInstance();
        switch (i) {
            case BY_DATE:
                putValue(Action.SMALL_ICON, 
                            im.getIcon(IconManager.SORT_BY_DATE));
                putValue(Action.SHORT_DESCRIPTION, DESCRIPTION_DATE);
                break;
            case BY_NAME:
                putValue(Action.SMALL_ICON, 
                        im.getIcon(IconManager.SORT_BY_NAME));
                putValue(Action.SHORT_DESCRIPTION, DESCRIPTION_NAME);
                break;
            default:
                new IllegalArgumentException("Index not valid.");
        }
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     * @param index The action index. One of the constants defined by this 
     *              class.
     */
    public SortByAction(HiViewer model, int index)
    {
        super(model);
        checkIndex(index);
        setEnabled(true);
        this.index = index;
    }
    
    /**
     * Sorts the nodes by date or name depending on the index.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e) { model.sortBy(index); }
    
}
