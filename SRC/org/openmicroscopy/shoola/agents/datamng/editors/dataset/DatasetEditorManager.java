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
	
	private DatasetData				model;
	private DatasetEditor			view;
	
	/** List of images to remove. */
	private List					imagesToRemove;
	
	/**List of images to add. */
	private List					imagesToAdd;
	
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
	}
	
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
					resetSelection();
			}  
		} catch(NumberFormatException nfe) {
			throw new Error("Invalid Action ID "+index, nfe);
		} 
	}
	
	void addImagesSelection(List l)
	{
		imagesToAdd = l;
		view.setImagesPane(l);
	}
	
	/** 
	 * Add (resp. remove) the image summary of (resp. from) the list of
	 * image summary to be added (resp. removed).
	 * 
	 * @param value		boolean value true if the checkBox is selected
	 * 					false otherwise.
	 * @param is		image summary to add or remove.
	 */
	void updateAddSelection(boolean value, ImageSummary is) 
	{
		if (value)	imagesToAdd.remove(is);
		else {
			 if (!imagesToAdd.contains(is)) imagesToAdd.add(is);
		} 
		//To be on the save side.
		if (dialog != null) dialog.getManager().setSelected(value, is);
		addImagesSelection(imagesToAdd);
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
	
	/** */
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
		view.selectAll();
		removeButton.setEnabled(false);
	}
	
	/** Cancel selection. */
	private void resetSelection()
	{
		removeButton.setEnabled(true);
		view.resetSelection();
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
	
