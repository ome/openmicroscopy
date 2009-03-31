/*
 * org.openmicroscopy.shoola.agents.treeviewer.finder.FinderModel
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

//Third-party libraries

//Application-internal dependencies
import java.util.ArrayList;
import java.util.List;

import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;

/** 
 * The Model component in the <code>Finder</code> MVC triad. 
 * This component keeps track of the parameters necessary to retrieve a phrase
 * in the currently selected browser.
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$Date: )
 * </small>
 * @since OME2.2
 */
class FinderModel
{

    /** The phrase to find. */
    private String		findText;
    
    /**
     * Flag to indicate if the name is selected for the find action.
     * <code>true</code> if the name field is selected, <code>false</code>
     * otherwise.
     */
    private boolean		nameSelected;
    
    /**
     * Flag to indicate if the description is selected for the find action.
     * <code>true</code> if the description field is selected,
     * <code>false</code> otherwise.
     */
    private boolean		descriptionSelected;
    
    /**
     * Flag to indicate if the annotation is selected for the find action.
     * <code>true</code> if the annotation field is selected, <code>false</code>
     * otherwise.
     */
    private boolean		annotationSelected;
    
    /** Flag to control is the {@link Finder} is displayed on screen. */
    private boolean 	display;
    
    /** 
     * Set to <code>false</code> if not case sensitive, set to 
     * <code>true</code> otherwise.
     */
    private boolean 	caseSensitive;
    
    /** Collection of pattern searched. */
    private List        history;
    
    /** Back pointer to the parent's model. */
    private TreeViewer	parentComponent;
    
    /**
     * Creates a new instance. 
     * 
     * @param parentComponent 	Back pointer to the parent's model.
     */
    FinderModel(TreeViewer parentComponent)
    {
        this.parentComponent = parentComponent;
        nameSelected = true;
        display = false;
        history = new ArrayList();
    }
    
    /** 
     * Returns the text currently selected. 
     * 
     * @return See above.
     */
    String getFindText() { return findText; }
    
    /**
     * Returns <code>true</code> if the find action applies to the annotation
     * field, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean isAnnotationSelected() { return annotationSelected;}
    
    /**
     * Sets to <code>true</code> if the find action applies to the annotation
     * field, <code>false</code> otherwise.
     * 
     * @param annotationSelected The value to set.
     */
    void setAnnotationSelected(boolean annotationSelected)
    {
        this.annotationSelected = annotationSelected;
    }
    
    /**
     * Returns <code>true</code> if the find action applies to the description
     * field, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean isDescriptionSelected() { return descriptionSelected; }
    
    /**
     * Sets to <code>true</code> if the find action applies to the annotation
     * field, <code>false</code> otherwise.
     * 
     * @param descriptionSelected The value to set.
     */
    void setDescriptionSelected(boolean descriptionSelected)
    {
        this.descriptionSelected = descriptionSelected;
    }
    
    /**
     * Returns <code>true</code> if the find action applies to the name
     * field, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean isNameSelected() { return nameSelected; }
    
    /**
     * Sets to <code>true</code> if the find action applies to the annotation
     * field, <code>false</code> otherwise.
     * 
     * @param nameSelected The value to set.
     */
    void setNameSelected(boolean nameSelected)
    {
        this.nameSelected = nameSelected;
    }
    
    /**
     * Sets the phrase to find.
     * 
     * @param findText The text to set.
     */
    void setFindText(String findText) { this.findText = findText.trim(); }
    
    /**
     * Returns <code>true</code> if the {@link Finder} is visible, 
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean isDisplay() { return display; }
    
    /**
     * Sets the value of the {@link #display} field.
     * 
     * @param display The value to set.
     */
    void setDisplay(boolean display) { this.display = display; }
    
    /**
     * Sets the value of the {@link #caseSensitive} field.
     * 
     * @param b The value to set.
     */
    void setCaseSensitive(boolean b) { caseSensitive = b; }
    
    /**
     * Returns the value of the {@link #caseSensitive} field.
     * 
     * @return See above.
     */
    boolean isCaseSensitive() { return caseSensitive; }
    
    /**
     * Adds the searched text to the history.
     * 
     * @param text The text to add.
     */
    void addToHistory(String text) { history.add(text); }
    
    /**
     * Returns the back pointer to the {@link TreeViewer}.
     * 
     * @return See above.
     */
    TreeViewer getParentComponent() { return parentComponent; }
    
    /**
     * Returns the values already searched for.
     * 
     * @return See above.
     */
    String[] getHistory() 
    { 
        return (String[]) history.toArray(new String[history.size()]);
    }
    
}
