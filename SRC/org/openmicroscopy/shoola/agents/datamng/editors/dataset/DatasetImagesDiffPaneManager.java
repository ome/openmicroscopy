/*
 * org.openmicroscopy.shoola.agents.datamng.editors.dataset.DatasetImagesDiffPaneManager
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

package org.openmicroscopy.shoola.agents.datamng.editors.dataset;

//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JButton;

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
class DatasetImagesDiffPaneManager
	implements ActionListener
{
	
	/** ID to handle event fired by the buttons. */
	private static final int			ALL = 100;
	private static final int			CANCEL = 101;
	private static final int			SAVE = 102;
	
	/** List of images to be added. */
	private List						imagesToAdd;
	
	/** Reference to the {@link DatasetImagesDiffPane view}. */
	private DatasetImagesDiffPane 		view;
	
	private DatasetEditorManager		control;
	
	private List						imagesDiff;
	
	DatasetImagesDiffPaneManager(DatasetImagesDiffPane view, 
									DatasetEditorManager control, 
									List imagesDiff)
	{
			this.view = view;
			this.control = control;	
			this.imagesDiff = imagesDiff;
			imagesToAdd = new ArrayList();
			attachListeners();					
	}
	
	List getImagesDiff() { return imagesDiff; }
	
	/** Attach listeners. */
	private void attachListeners()
	{
        attachButtonListener(view.selectButton, ALL);
        attachButtonListener(view.cancelButton, CANCEL);
        attachButtonListener(view.saveButton, SAVE);
	}
    
    private void attachButtonListener(JButton button, int id)
    {
        button.addActionListener(this);
        button.setActionCommand(""+id);
    }
    
	/** Handle events fired by the buttons. */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		try {
			switch (index) { 
				case SAVE:
					saveSelection(); break;
				case ALL:
					selectAll(); break;
				case CANCEL:
					cancelSelection();
			}
		} catch(NumberFormatException nfe) {
			throw new Error("Invalid Action ID "+index, nfe);
		} 
	}
	
	void setSelected(boolean value, ImageSummary is)
	{
		if (value) imagesDiff.add(is);
		else imagesDiff.remove(is);
		buttonsEnabled(imagesDiff.size() != 0);
	}
	
	/** Set the buttons enabled. */
	void buttonsEnabled(boolean b)
	{
		view.selectButton.setEnabled(b);
		view.cancelButton.setEnabled(b);
		view.saveButton.setEnabled(b);
	}
	
	/** 
	 * Add (resp. remove) the image to (resp. from) the list of
	 * image summary objects to be added to the project.
	 * 
	 * @param value		boolean value true if the checkBox is selected
	 * 					false otherwise.
	 * @param is		image summary to add or remove
	 */
	void addImage(boolean value, ImageSummary is) 
	{
		if (value) {
			if (!imagesToAdd.contains(is))	imagesToAdd.add(is); 
		} else 	imagesToAdd.remove(is);
	}
	
	/** Add the selection to the dataset. */
	private void saveSelection()
	{
		if (imagesToAdd.size() != 0) {
			Iterator i = imagesToAdd.iterator();
			while (i.hasNext())
				imagesDiff.remove(i.next());
			buttonsEnabled(imagesDiff.size() != 0);
			control.addImagesSelection(imagesToAdd);
			imagesToAdd.removeAll(imagesToAdd);
		}
		view.setVisible(false);
	}
	
	/** Select All datasets.*/
	private void selectAll()
	{
        view.selectButton.setEnabled(false);
		view.setSelection(Boolean.TRUE);
	}
	
	/** Cancel the selection. */
	private void cancelSelection()
	{
        view.selectButton.setEnabled(true);
		view.setSelection(Boolean.FALSE);
	}
	
}
