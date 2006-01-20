/*
 * org.openmicroscopy.shoola.agents.treeviewer.finder.PopupMenu
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
import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies

/** 
 * Pop-up menu to filter the find action.
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$Date: )
 * </small>
 * @since OME2.2
 */
class PopupMenu
	extends JPopupMenu
{

    /** The text displayed by the {@link #inNameItem}. */
    private static final String	NAME_IN_NAME = "Find in name";
    
    /** The description of the {@link #inNameItem}. */
    private static final String	DESCRIPTION_IN_NAME = "Finds the occurence of" +
    											" the phrase in the name.";
    
    /** The text displayed by the {@link #inDescriptionItem}. */
    private static final String	NAME_IN_DESCRIPTION = "Find in description";
    
    /** The description of the {@link #inDescriptionItem}. */
    private static final String	DESCRIPTION_IN_DESCRIPTION = 
        			"Finds the occurence of the phrase in the description.";
    
    /** The text displayed by the {@link #inAnnotationItem}. */
    private static final String	NAME_IN_ANNOTATION = "Find in annotation";
    
    /** The description of the {@link #inAnnotationItem}. */
    private static final String	DESCRIPTION_IN_ANNOTATION = 
        				"Finds the occurence of the phrase in the annotation.";
    
    /** Reference to the {@link Finder}. */
    private Finder				model;
    
    /** Item to find the phrase in the name. */
    private JCheckBoxMenuItem	inNameItem; 
    
    /** Item to find the phrase in the description. */
    private JCheckBoxMenuItem	inDescriptionItem; 
    
    /** Item to find the phrase in the annotation. */
    private JCheckBoxMenuItem	inAnnotationItem; 
    
    /** Helper method to create the items. */
    private void createMenuItems()
    {
        inNameItem = new JCheckBoxMenuItem(NAME_IN_NAME);
        inNameItem.setToolTipText(DESCRIPTION_IN_NAME);
        inNameItem.setSelected(model.isNameSelected());
        inNameItem.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
                model.setNameSelected(item.isSelected());
            }
        });
        inDescriptionItem = new JCheckBoxMenuItem(NAME_IN_DESCRIPTION);
        inDescriptionItem.setToolTipText(DESCRIPTION_IN_DESCRIPTION);
        inDescriptionItem.setSelected(model.isDescriptionSelected());
        inDescriptionItem.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
                model.setDescriptionSelected(item.isSelected());
            }
        });
        inAnnotationItem = new JCheckBoxMenuItem(NAME_IN_ANNOTATION);
        inAnnotationItem.setToolTipText(DESCRIPTION_IN_ANNOTATION);
        inAnnotationItem.setSelected(model.isAnnotationSelected());
        inAnnotationItem.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
                model.setAnnotationSelected(item.isSelected());
            }
        });
    }
    
    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
        setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        add(inNameItem);
        add(inDescriptionItem);
        add(inAnnotationItem);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the {@link Finder}.
     */
    PopupMenu(Finder model)
    {
        if (model == null) throw new NullPointerException("No model.");
        this.model = model;
        createMenuItems();
        buildGUI();
    }
    
}
