/*
 * org.openmicroscopy.shoola.agents.treeviewer.finder.FinderControl
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


//Java imports
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Action;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.util.ui.HistoryDialog;

/** 
 * The {@link Finder}'s Controller.
 * 
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$Date: )
 * </small>
 * @since OME2.2
 */
class FinderControl
	implements PropertyChangeListener
{

    /** Identifies the <code>Close</code> action in the Actions menu. */
    static final Integer     CLOSE = Integer.valueOf(0);
    
    /** Identifies the <code>Find</code> action in the Actions menu. */
    static final Integer     FIND = Integer.valueOf(1);
    
    /** Identifies the <code>Find Next</code> action in the Actions menu. */
    static final Integer     FIND_NEXT = Integer.valueOf(2);
    
    /** Identifies the <code>Find Previous</code> action in the Actions menu. */
    static final Integer     FIND_PREVIOUS = Integer.valueOf(3);
    
    /** Identifies the <code>Highlight</code> action in the Actions menu. */
    static final Integer     HIGHLIGHT = Integer.valueOf(4);
    
    /** Identifies the <code>Filter</code> action in the Actions menu. */
    static final Integer     FILTER_MENU = Integer.valueOf(5);
    
    /** Reference to the {@link Finder}, viewed as the Model. */
    private Finder						model;
    
    /** Reference to the {@link FinderUI view}. */
    private FinderUI					view;
    
    /** Maps actions ids onto actual <code>Action</code> object. */
    private Map<Integer, FinderAction>	actionsMap;
    
    /** Helper method to create all the UI actions. */
    private void createActions()
    {
        actionsMap.put(CLOSE, new CloseAction(model));
        actionsMap.put(FIND, new FindAction(model));
        actionsMap.put(FIND_NEXT, new FindNextAction(model));
        actionsMap.put(FIND_PREVIOUS, new FindPreviousAction(model));
        actionsMap.put(HIGHLIGHT, new HighlightAction(model));
        actionsMap.put(FILTER_MENU, new FilterMenuAction(model));
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the {@link Finder}.
     * 				Mustn't be <code>null</code>.
     */
    FinderControl(Finder model)
    {
        if (model == null) throw new NullPointerException("No model.");
        this.model = model;
        model.addPropertyChangeListener(Finder.RETRIEVED_PROPERTY, this);
        actionsMap = new HashMap<Integer, FinderAction>();
        createActions();
    }
    
    /**
     * Links the Controller with the View.
     * 
     * @param view The View.
     */
    void initialize(FinderUI view) 
    {
        this.view = view;
    }
    
    /**
     * Returns the action corresponding to the specified id.
     * 
     * @param id One of the flags defined by this class.
     * @return The specified action.
     */
    Action getAction(Integer id) { return actionsMap.get(id); }

    /**
     * Reacts to the {@link Finder#RETRIEVED_PROPERTY} and
     * {@link TreeViewer#FINDER_VISIBLE_PROPERTY} property changes.
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent pce)
    {
        String name = pce.getPropertyName();
        if (name.equals(TreeViewer.FINDER_VISIBLE_PROPERTY))
            model.setDisplay(((Boolean) pce.getNewValue()).booleanValue());
        else if (name.equals(Finder.RETRIEVED_PROPERTY)) 
            view.setMessage(((Integer) pce.getNewValue()).intValue());
        else if (name.equals(HistoryDialog.SELECTION_PROPERTY))
            view.setTextToFind((String) pce.getNewValue());
    }
    
}
