/*
 * org.openmicroscopy.shoola.agents.treeviewer.browser.FilterMenu
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

package org.openmicroscopy.shoola.agents.treeviewer.browser;




//Java imports
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


//Third-party libraries

//Application-internal dependencies

/** 
 * A popup menu to display the possible filters for image filtering.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class FilterMenu
    extends JPopupMenu
{

    /** Bounds property indicating that a new filter is selected. */
    static final String             FILTER_SELECTED_PROPERTY = "filterSelected";
    
    /** The Name of the {@link #dataset} item. */
    private static final String NAME_DATASET = "Images in datasets";
    
    /** The description of the {@link #dataset} item. */
    private static final String DESCRIPTION_DATASET = "Retrieve images " +
            "contained in the selected datasets.";
    
    /** The Name of the {@link #category} item. */
    private static final String NAME_CATEGORY = "Images in categories";
    
    /** The Name of the {@link #category} item. */
    private static final String DESCRIPTION_CATEGORY = "Retrieve images " +
                        "contained in the selected categories.";
    
    /** The Name of the {@link #allImages} item. */
    private static final String NAME = "All my Images";
    
    /** The description of the {@link #allImages} item. */
    private static final String DESCRIPTION = "Retrieve all my images."; 
    
    /** Button to retrieve the images in datasets. */
    private JRadioButtonMenuItem    dataset;
    
    /** Button to retrieve the images in categories. */
    private JRadioButtonMenuItem    category;
    
    /** Button to retrieve the all the user's images. */
    private JRadioButtonMenuItem    allImages;
    
    /** Reference to the Model. */
    private BrowserModel            model;
        
    /** Helper method to create the menu items. */
    private void createMenuItems()
    {
        int type = model.getFilterType();
        dataset = new JRadioButtonMenuItem(NAME_DATASET);
        dataset.setToolTipText(DESCRIPTION_DATASET);
        dataset.setSelected(type == Browser.IN_DATASET_FILTER);
        category = new JRadioButtonMenuItem(NAME_CATEGORY);
        category.setToolTipText(DESCRIPTION_CATEGORY);
        category.setSelected(type == Browser.IN_CATEGORY_FILTER);
        allImages = new JRadioButtonMenuItem(NAME);
        allImages.setToolTipText(DESCRIPTION);
        allImages.setSelected(type == Browser.NO_IMAGES_FILTER);
        //Attach listener.
        dataset.addChangeListener(new ChangeListener() {
            /** Sets the selected filter types. */
            public void stateChanged(ChangeEvent ce)
            {
                JRadioButtonMenuItem i = (JRadioButtonMenuItem) ce.getSource();
                if (i.isSelected()) 
                    firePropertyChange(FILTER_SELECTED_PROPERTY, 
                            new Integer(model.getFilterType()), 
                            new Integer(Browser.IN_DATASET_FILTER));
            }
        });
        category.addChangeListener(new ChangeListener() {
            /** Sets the selected filter types. */
            public void stateChanged(ChangeEvent ce)
            {
                JRadioButtonMenuItem i = (JRadioButtonMenuItem) ce.getSource();
                if (i.isSelected()) 
                    firePropertyChange(FILTER_SELECTED_PROPERTY, 
                            new Integer(model.getFilterType()), 
                            new Integer(Browser.IN_CATEGORY_FILTER));
            }
        });
        allImages.addChangeListener(new ChangeListener() {
            /** Sets the selected filter types. */
            public void stateChanged(ChangeEvent ce)
            {
                JRadioButtonMenuItem i = (JRadioButtonMenuItem) ce.getSource();
                if (i.isSelected()) 
                    firePropertyChange(FILTER_SELECTED_PROPERTY, 
                            new Integer(model.getFilterType()), 
                            new Integer(Browser.NO_IMAGES_FILTER));
            }
        });
        
        ButtonGroup group = new ButtonGroup();
        group.add(dataset);
        group.add(category);
        group.add(allImages);
        
    }
    
    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
        setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        add(dataset);
        add(category);
        add(allImages);
    }
    
    /** 
     * Creates a new instance.
     *
     * @param model Reference to the Model. Mustn't be <code>null</code>. 
     */
    FilterMenu(BrowserModel model)
    {
        if (model == null)
            throw new IllegalArgumentException("No model.");
        this.model = model;
        createMenuItems();
        buildGUI() ;
    }
    
}
