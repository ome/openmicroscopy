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
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JTable;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.ImageSummary;

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
	implements ActionListener
{

	/** Action id. */
	private static final int				LOAD = 0;

	/** This UI component's view. */
	private ImagesPane 						view;
	
	/** The agent's control component. */
	private DataManagerCtrl 				agentCtrl;

	private boolean							loaded;

    private int                             index;
    
	ImagesPaneManager(ImagesPane view, DataManagerCtrl agentCtrl)
	{
		this.view = view;
		this.agentCtrl = agentCtrl;
		loaded = false;
        index = -1;
		initListeners();
	}

	/** update the view when an image's name has been modified. */
	void updateImageInTable(ImageSummary is)
	{
		if (loaded) {
			int rows = view.sorter.getRowCount();
			ImageSummary summary;
			for (int i = 0; i < rows; i++) {
				summary = (ImageSummary) view.sorter.getValueAt(i, 
                                                ImagesPane.NAME);
				if (summary.getID() == is.getID()) {
					view.tableModel.setValueAt(is, i);
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
	private void initListeners()
	{
        attachButtonListeners(view.bar.load, LOAD);
	}

    /** Attach an {@link ActionListener} to an {@link AbstractButton}. */
    private void attachButtonListeners(JButton button, int id)
    {
        button.addActionListener(this);
        button.setActionCommand(""+id);
    }
    
	/** Handles event fired by the buttons. */
	public void actionPerformed(ActionEvent e)
	{
		try {
            int index = Integer.parseInt(e.getActionCommand());
			switch (index) { 
				case LOAD:
					loadImages(); break;
			}
		} catch(NumberFormatException nfe) {
			throw new Error("Invalid Action ID "+e.getActionCommand(), nfe);
		} 
	}

	/** Load all the images. */
	private void loadImages()
	{
        int selectedIndex = view.bar.selections.getSelectedIndex();
        if (selectedIndex != index) {
            index = selectedIndex;
            Object[] images = null;
            switch (selectedIndex) {
                case ImagesPaneBar.IMAGES_IMPORTED:
                    images = agentCtrl.getImportedImages().toArray(); break;
                case ImagesPaneBar.IMAGES_USED:
                    images = agentCtrl.getUsedImages().toArray(); break;
                case ImagesPaneBar.IMAGES_GROUP:
                    images = agentCtrl.getGroupImages().toArray(); break;
                case ImagesPaneBar.IMAGES_SYSTEM:
                    images = agentCtrl.getSystemImages().toArray(); break;
            }
            if (images == null) return;
            view.displayImages(images);
            loaded = true;
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
		int selRow = view.table.getSelectedRow();
		if (selRow != -1) {
			if (e.isPopupTrigger()) {
				ImageSummary 
					target = (ImageSummary) view.sorter.getValueAt(selRow, 
                            ImagesPane.NAME);
				DataManagerUIF presentation = agentCtrl.getReferenceFrame();
				TreePopupMenu popup = presentation.getPopupMenu();
				popup.setTarget(target);  
				popup.show(view.table, e.getX(), e.getY());
			} 
		}	
	}
	
}
