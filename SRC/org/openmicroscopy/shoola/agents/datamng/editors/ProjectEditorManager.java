/*
 * org.openmicroscopy.shoola.agents.datamng.editors.ProjectEditorManager
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

package org.openmicroscopy.shoola.agents.datamng.editors;

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
import org.openmicroscopy.shoola.env.data.model.DatasetSummary;
import org.openmicroscopy.shoola.env.data.model.ProjectData;

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
class ProjectEditorManager
	implements ActionListener, DocumentListener, MouseListener
{
	/** ID used to handle events. */
	private static final int	SAVE = 0;	
	private static final int	RELOAD = 1;
	private static final int	REMOVE = 2;
	private static final int	CANCEL_SELECTION = 3;
	
	/** Reference to the model. */
	private ProjectData			model;
	
	/** Reference to the view. */
	private ProjectEditor		view;
	
	/** Reference to the control. */
	private DataManagerCtrl 	control;
	
	/** List of datasets to be removed. */
	private List				datasetsToRemove;
	
	/** Save button displayed in the {@link ProjectGeneralPane}. */
	private JButton 			saveButton;
	
	/** Reload button displayed in the {@link ProjectGeneralPane}. */
	private JButton 			reloadButton;
	
	/** Remove button displayed in the {@link ProjectDatasetsPane}. */
	private JButton 			removeButton;
	
	/** Cancel button displayed in the {@link ProjectDatasetsPane}. */
	private JButton 			cancelButton;
	
	/** textArea displayed in the {@link ProjectGeneralPane}. */
	private JTextArea			descriptionArea;
	
	/** text field displayed in the {@link ProjectGeneralPane}. */
	private JTextArea			nameField;
	
	private boolean				nameChange, isName;
	
	ProjectEditorManager(ProjectEditor view, DataManagerCtrl control,
						ProjectData model)
	{
		this.view = view;
		this.control = control;
		this.model = model;
		nameChange = false;
		isName = false;
	}
	
	ProjectData getProjectData()
	{
		return model;
	}

	/** Initializes the listeners. */
	void initListeners()
	{
		saveButton = view.getSaveButton();
		reloadButton = view.getReloadButton();
		saveButton.addActionListener(this);
		saveButton.setActionCommand(""+SAVE);
		reloadButton.addActionListener(this);
		reloadButton.setActionCommand(""+RELOAD);
		removeButton = view.getRemoveButton();
		removeButton.addActionListener(this);
		removeButton.setActionCommand(""+REMOVE);
		cancelButton = view.getCancelButton();
		cancelButton.addActionListener(this);
		cancelButton.setActionCommand(""+CANCEL_SELECTION);
		nameField = view.getNameField();
		nameField.getDocument().addDocumentListener(this);
		nameField.addMouseListener(this);
		descriptionArea = view.getDescriptionArea();
		descriptionArea.getDocument().addDocumentListener(this);
	}
	
	/** Handles event fired by the buttons. */
	public void actionPerformed(ActionEvent e)
	{
		String s = (String) e.getActionCommand();
		try {
			int     index = Integer.parseInt(s);
			switch (index) { 
				case SAVE:
					save();
					break;
				case RELOAD:
					reload();
					break;
				case REMOVE:
					remove();
					break;
				case CANCEL_SELECTION:
					cancelSelection();
					break;
			}// end switch  
		} catch(NumberFormatException nfe) {
		   throw nfe;  //just to be on the safe side...
		} 
	}
	
	/** 
	 * Add (resp. remove) the dataset summary to (resp. from) the list of
	 * dataset summary objects to add to the new project.
	 * 
	 * @param value		boolean value true if the checkBox is selected
	 * 					false otherwise.
	 * @param ds		dataset summary to add or remove
	 */
	void addDataset(boolean value, DatasetSummary ds) 
	{
		if (datasetsToRemove == null) datasetsToRemove = new ArrayList();
		if (value == true) datasetsToRemove.add(ds);
		else 	datasetsToRemove.remove(ds);
	}
	
	/** Save in DB. */
	private void save()
	{
		model.setDescription(descriptionArea.getText());
		model.setName(nameField.getText());
		if (datasetsToRemove != null) setModelDatasets();
		control.updateProject(model, nameChange);
		view.dispose();
	}
	
	/** Remove the selected datasets from the datasets list of the model. */
	private void setModelDatasets()
	{
		Iterator i = datasetsToRemove.iterator();
		List datasets = model.getDatasets();
		while (i.hasNext())
			datasets.remove((DatasetSummary) i.next());
		model.setDatasets(datasets);
	}
	
	/** Select All datasets.*/
	private void remove()
	{
		datasetsToRemove = model.getDatasets();
		view.removeAll();
		removeButton.setEnabled(false);
	}
	
	/** Cancel selection. */
	private void cancelSelection()
	{
		datasetsToRemove = null;
		removeButton.setEnabled(true);
		view.cancelSelection();
	}
	
	/** */
	private void reload()
	{
		//TODO: implement method.
	}

	/** Require by I/F. */
	public void changedUpdate(DocumentEvent e)
	{
		saveButton.setEnabled(true);
	}

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

	/** Tells that the name has been modified. */
	public void mousePressed(MouseEvent e)
	{ 
		isName = true;
	}
	
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
	
