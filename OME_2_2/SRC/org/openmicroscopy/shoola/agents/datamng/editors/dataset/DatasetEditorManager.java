/*
 * org.openmicroscopy.shoola.agents.datamng.editors.DatasetEditorManager
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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.datamng.DataManagerCtrl;
import org.openmicroscopy.shoola.env.data.model.DatasetData;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
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
class DatasetEditorManager
	implements ActionListener, DocumentListener, MouseListener
{
	
	/** ID used to handle events. */
	private static final int		SAVE = 0;	
	private static final int		REMOVE = 1;
	private static final int		CANCEL = 2;
	private static final int		ADD = 3;
	private static final int		RESET = 4;
	private static final int		REMOVE_ADDED = 5;
	private static final int		RESET_ADDED = 6;
	
	private DatasetData				model;
	private DatasetEditor			view;
	
	/** List of images to remove. */
	private List					imagesToRemove;
	
	/**List of images to add. */
	private List					imagesToAdd;
	
	/** List of selected images to be added that have to be removed. */
	private List					imagesToAddToRemove;
	
	private DataManagerCtrl 		control;
	
	/** Add button displayed in the {@link DatasetEditorBar}. */
	private JButton 				addButton;
	
	/** Cancel button displayed in the {@link DatasetEditorBar}. */
	private JButton 				cancelButton;
		
	/** Save button displayed in the {@link DatasetEditorBar}. */
	private JButton 				saveButton;

	/** Remove button displayed in the {@link DatasetImagesPane}. */
	private JButton 				removeButton;

	/** Reset button displayed in the {@link DatasetImagesPane}. */
	private JButton 				resetButton;
	
	/** Reset button displayed in the {@link DatasetImagesPane}. */
	private JButton 				resetToAddButton;
	
	/**  
	 * Remove button displayed in the {@link DatasetImagesPane}. 
	 * Remove the selected datasets from the list of datasets to add.
	 */
	private JButton 				removeToAddButton;
	
	/** textArea displayed in the {@link DatasetGeneralPane}. */
	private JTextArea				descriptionArea;
	
	/** text field displayed in the {@link DatasetGeneralPane}. */
	private JTextArea				nameField;
	
	private boolean					nameChange, isName;
	
	private DatasetImagesDiffPane	dialog;
	DatasetEditorManager(DatasetEditor view, DataManagerCtrl control,
						DatasetData model)
	{
		this.view = view;
		this.control = control;
		this.model = model;
		nameChange = false;
		isName = false;
		imagesToRemove = new ArrayList();
		imagesToAdd = new ArrayList();
		imagesToAddToRemove = new ArrayList();
	}
	
	List getImages() { return getDatasetData().getImages(); }
	
	List getImagesToAdd() { return imagesToAdd; }
	
	List getImagesToAddToRemove() { return imagesToAddToRemove; }
	
	DatasetEditor getView() { return view; }
	
	DatasetData getDatasetData() { return model; }

	/** Initializes the listeners. */
	void initListeners()
	{
		//buttons
		saveButton = view.getSaveButton();
		saveButton.addActionListener(this);
		saveButton.setActionCommand(""+SAVE);
		addButton = view.getAddButton();
		addButton.addActionListener(this);
		addButton.setActionCommand(""+ADD);	
		
		cancelButton = view.getCancelButton();
		cancelButton.addActionListener(this);
		cancelButton.setActionCommand(""+CANCEL);	
		
		removeButton = view.getRemoveButton();
		removeButton.addActionListener(this);
		removeButton.setActionCommand(""+REMOVE);
		resetButton = view.getResetButton();
		resetButton.addActionListener(this);
		resetButton.setActionCommand(""+RESET);	
		
		removeToAddButton = view.getRemoveToAddButton();
		removeToAddButton.addActionListener(this);
		removeToAddButton.setActionCommand(""+REMOVE_ADDED);
		resetToAddButton = view.getResetToAddButton();
		resetToAddButton.addActionListener(this);
		resetToAddButton.setActionCommand(""+RESET_ADDED);
		
		//textfields
		nameField = view.getNameField();
		nameField.getDocument().addDocumentListener(this);
		nameField.addMouseListener(this);
		descriptionArea = view.getDescriptionArea();
		descriptionArea.getDocument().addDocumentListener(this);
	}
	
	/** Handles event fired by the buttons. */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		try {
			switch (index) {
				case SAVE:
					save(); break;
				case CANCEL:
					cancel(); break;
				case ADD:
					showImagesSelection(); break;
				case REMOVE:
					remove(); break;
				case RESET:
					resetSelection(); break;
				case REMOVE_ADDED:
					removeAdded(); break;
				case RESET_ADDED:
					resetAdded(); 
			}  
		} catch(NumberFormatException nfe) {
			throw new Error("Invalid Action ID "+index, nfe);
		} 
	}
	
	/** Bring up the images selection dialog. */
	private void showImagesSelection()
	{
		if (dialog == null) {
			List imagesDiff = control.getImagesDiff(model);
			dialog = new DatasetImagesDiffPane(this, imagesDiff);
		} else {
			dialog.remove(dialog.getContents());
			dialog.buildGUI();
		}
		UIUtilities.centerAndShow(dialog);
		view.setSelectedPane(DatasetEditor.POS_IMAGE);
		saveButton.setEnabled(true);	
	}
	
	
	/** Add the list of selected images to the {@link ProjectDatasetPane}. */
	void addImagesSelection(List l)
	{
		Iterator i = l.iterator();
		ImageSummary is;
		while (i.hasNext()) {
			is = (ImageSummary) i.next();
			if (!imagesToAdd.contains(is)) imagesToAdd.add(is);
		}
		view.rebuildComponent();
	}
	
	/** 
	 * Add (resp. remove) the image summary of (resp. from) the list of
	 * image summary to be added (resp. removed).
	 * 
	 * @param value		boolean value true if the checkBox is selected
	 * 					false otherwise.
	 * @param is		image summary to add or remove.
	 */
	void setToAddToRemove(boolean value, ImageSummary is) 
	{
		if (value)
				imagesToAddToRemove.add(is); 
		else {
			if (imagesToAddToRemove.contains(is)) {
				imagesToAddToRemove.remove(is);
			}	
		} 
	}
	
	/** 
	 * Add (resp. remove) the image summary of (resp. from) the list of
	 * image summary objects to be removed.
	 * 
	 * @param value		boolean value true if the checkBox is selected
	 * 					false otherwise.
	 * @param is		image summary to add or remove.
	 */
	void selectImage(boolean value, ImageSummary is) 
	{
		if (value) {
			if(!imagesToRemove.contains(is)) imagesToRemove.add(is); 
		}
		else 	imagesToRemove.remove(is);
		saveButton.setEnabled(true);
	}

	/** Close the widget, doesn't save changes. */
	private void cancel()
	{
		view.setVisible(false);
		view.dispose();
	}
	
	/** Save changes in DB. */
	private void save()
	{
		model.setDescription(descriptionArea.getText());
		model.setName(nameField.getText());
		control.updateDataset(model, imagesToRemove, imagesToAdd, nameChange);
		view.dispose();
	}
	
	/** Select All images.*/
	private void remove()
	{
		view.getImagesPane().setSelection(new Boolean(true));
		removeButton.setEnabled(false);
	}
	
	/** Cancel selection. */
	private void resetSelection()
	{
		removeButton.setEnabled(true);
		view.getImagesPane().setSelection(new Boolean(false));
	}

	/** Remove the selected images from the queue of images to add. */
	private void removeAdded()
	{
		Iterator i = imagesToAddToRemove.iterator();
		ImageSummary is;
	
		while (i.hasNext()) {
			is = (ImageSummary) i.next();
			imagesToAdd.remove(is);
			if (dialog != null) dialog.getManager().setSelected(true, is);
		}
		if (imagesToAddToRemove.size() != 0) {
			imagesToAddToRemove.removeAll(imagesToAddToRemove);
			view.rebuildComponent();
		}
	}

	/** Reset the default for the queue of images to add. */
	private void resetAdded()
	{
		imagesToAddToRemove.removeAll(imagesToAddToRemove);
		view.rebuildComponent();
	}

	
	/** Require by I/F. */
	public void changedUpdate(DocumentEvent e) { saveButton.setEnabled(true); }

	/** Require by I/F. */
	public void insertUpdate(DocumentEvent e)
	{
		if (isName) nameChange = true;
		saveButton.setEnabled(true);
	}
	
	/** Require by I/F. */
	public void removeUpdate(DocumentEvent e)
	{
		if (isName) nameChange = true;
		saveButton.setEnabled(true);
	}
	
	/** Indicates that the name has been modified. */
	public void mousePressed(MouseEvent e) { isName = true; }

	/** 
	 * Required by I/F but not actually needed in our case, no op 
	 * implementation.
	 */ 
	public void mouseClicked(MouseEvent e) {}

	/** 
	 * Required by I/F but not actually needed in our case, no op 
	 * implementation.
	 */ 
	public void mouseEntered(MouseEvent e) {}

	/** 
	 * Required by I/F but not actually needed in our case, no op 
	 * implementation.
	 */ 
	public void mouseExited(MouseEvent e) {}

	/** 
	 * Required by I/F but not actually needed in our case, no op 
	 * implementation.
	 */ 
	public void mouseReleased(MouseEvent e){}
	
}	
	
