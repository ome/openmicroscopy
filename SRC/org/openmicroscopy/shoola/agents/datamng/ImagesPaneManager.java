/*
 * org.openmicroscopy.shoola.agents.datamng.ExplorerImagesPaneManager
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

package org.openmicroscopy.shoola.agents.datamng;



//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTable;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.datamng.util.DatasetsSelector;
import org.openmicroscopy.shoola.agents.datamng.util.Filter;
import org.openmicroscopy.shoola.agents.datamng.util.ISelector;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class ImagesPaneManager
	implements ActionListener, ISelector
{

	/** Action command ID. */
	private static final int				LOAD = 0, SELECTION = 1, FILTER = 2;

	/** This UI component's view. */
	private ImagesPane 						view;
	
	/** The agent's control component. */
	private DataManagerCtrl 				agentCtrl;

	private boolean							loaded;

    private int                             index;
    
    private Map                             filters, complexFilters;
    
    private List                            selectedDatasets;
    
	ImagesPaneManager(ImagesPane view, DataManagerCtrl agentCtrl)
	{
		this.view = view;
		this.agentCtrl = agentCtrl;
		loaded = false;
        index = -1;
	}

    /** Implemented as specified by {@link ISelector} I/F. */
    public void setSelectedDatasets(List l)
    { 
        selectedDatasets = l;
        loaded = false;
    }
    
    /** Implemented as specified by {@link ISelector} I/F. */
    public void setFilters(Map filters)
    {
        this.filters = filters;
        loaded = false;
    }
    
    /** Implemented as specified by {@link ISelector} I/F. */
    public void setComplexFilters(Map complexFilters)
    { 
        this.complexFilters = complexFilters;
        loaded = false; 
    }
    
	/** update the view when an image's name has been modified. */
	void updateImageInTable(ImageSummary is)
	{
		if (loaded) {
			int rows = view.imagesSplitPane.sorter.getRowCount();
			ImageSummary summary;
			for (int i = 0; i < rows; i++) {
				summary = (ImageSummary) 
                    view.imagesSplitPane.sorter.getValueAt(i, ImagesPane.NAME);
				if (summary.getID() == is.getID()) {
                    view.imagesSplitPane.tableModel.setValueAt(is, i);
					break;
				}		
			}
		}
	}
	
    /** Attach a mouseListener to the specified table. */
    void attachTableListener(JTable table)
    {
        table.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { onClick(e); }
            public void mouseReleased(MouseEvent e) { onClick(e); }
        });
    }
    
	/** Initializes the listeners. */
	void initListeners()
	{
        attachButtonListeners(view.bar.load, LOAD);
        attachButtonListeners(view.bar.filter, FILTER);
        attachBoxListeners(view.bar.selections, SELECTION);
	}

    /** Attach an {@link ActionListener} to an {@link AbstractButton}. */
    private void attachButtonListeners(JButton button, int id)
    {
        button.addActionListener(this);
        button.setActionCommand(""+id);
    }
    
    /** Attach an {@link ActionListener} to an {@link AbstractButton}. */
    private void attachBoxListeners(JComboBox box, int id)
    {
        box.addActionListener(this);
        box.setActionCommand(""+id);
    }
    
	/** Handles event fired by the buttons. */
	public void actionPerformed(ActionEvent e)
	{
		try {
            int index = Integer.parseInt(e.getActionCommand());
			switch (index) { 
				case LOAD:
					loadImages(); break;
                case SELECTION:
                    bringSelector(e); break;
                case FILTER:
                    bringFilter(); break;
			}
		} catch(NumberFormatException nfe) {
			throw new Error("Invalid Action ID "+e.getActionCommand(), nfe);
		} 
	}

    /** Bring up the filter. */
    private void bringFilter()
    {
        UIUtilities.centerAndShow(new Filter(agentCtrl, this));
    }
    
    /** Bring up the datasetSelector. */
    private void bringSelector(ActionEvent e)
    {
        int selectedIndex = ((JComboBox) e.getSource()).getSelectedIndex();
        if (selectedIndex == ImagesPaneBar.IMAGES_USED) {
            index = selectedIndex;
            //retrieve the datasets used by the current user.
            List d = agentCtrl.getUsedDatasets();
            if (d != null && d.size() > 0)
                UIUtilities.centerAndShow(new DatasetsSelector(agentCtrl, this, 
                                            d));
            else {
                UserNotifier un = agentCtrl.getRegistry().getUserNotifier();
                un.notifyInfo("Used datasets", "no dataset used ");
            }
        }
    }
    
	/** Load all the images. */
	private void loadImages()
	{
        int selectedIndex = view.bar.selections.getSelectedIndex();
        if (selectedIndex != index || !loaded) {
            index = selectedIndex;
            List images = null;
            switch (selectedIndex) {
                case ImagesPaneBar.IMAGES_IMPORTED:
                    images = agentCtrl.getImportedImages(filters, 
                                                        complexFilters);
                    break;
                case ImagesPaneBar.IMAGES_GROUP:
                    images = agentCtrl.getGroupImages(filters, complexFilters);
                    break;
                case ImagesPaneBar.IMAGES_SYSTEM:
                    images = agentCtrl.getSystemImages(filters, complexFilters);
                    break;
                case ImagesPaneBar.IMAGES_USED:
                    images = agentCtrl.loadImagesInDatasets(selectedDatasets, 
                            DataManagerCtrl.FOR_HIERARCHY, null, filters, 
                            complexFilters);
            }
            displayListImages(images);
        }
	}	
    
	/** 
	 * Handles mouse clicks within the tree component in the view.
	 * If the mouse event is the platform popup trigger event, then the context 
	 * popup menu is brought up. Otherwise, double-clicking on a project, 
	 * dataset node brings up the corresponding property sheet dialog.
	 *
	 * @param e   The mouse event.
	 */
	private void onClick(MouseEvent e)
	{
		int selRow = view.imagesSplitPane.table.getSelectedRow();
		if (selRow != -1) {
			if (e.isPopupTrigger()) {
				ImageSummary 
					target = (ImageSummary) 
                        view.imagesSplitPane.sorter.getValueAt(selRow, 
                            ImagesPane.NAME);
				DataManagerUIF presentation = agentCtrl.getReferenceFrame();
				TreePopupMenu popup = presentation.getPopupMenu();
				popup.setTarget(target);  
                popup.setIndex(DataManagerCtrl.FOR_IMAGES);
				popup.show(view.imagesSplitPane.table, e.getX(), e.getY());
			} 
		}	
	}
    
    /** Display the images. */
    private void displayListImages(List images)
    {
        if (images == null || images.size() == 0) {
            UserNotifier un = agentCtrl.getRegistry().getUserNotifier();
            un.notifyInfo("Image retrieval", "No image matching your criteria");
            return;
        }
        view.displayImages(images.toArray());
        loaded = true;
    }
	
}
