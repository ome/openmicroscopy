/*
 * org.openmicroscopy.shoola.agents.datamng.editors.project.ProjectEditorManager
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

package org.openmicroscopy.shoola.agents.datamng.editors.project;

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
class ProjectEditorManager
	implements ActionListener, DocumentListener, MouseListener
{
	
	/** ID used to handle events. */
	private static final int		SAVE = 0;	
	private static final int		REMOVE = 1;
	private static final int		RESET = 2;
	private static final int		ADD = 3;
	private static final int		CANCEL = 4;
	private static final int		REMOVE_ADDED = 5;
	private static final int		RESET_ADDED = 6;
	
	/** Reference to the model. */
	private ProjectData				model;
	
	/** Reference to the view. */
	private ProjectEditor			view;
	
	/** Reference to the control. */
	private DataManagerCtrl 		control;
	
	/** List of datasets to remove. */
	private List					datasetsToRemove;
	
	/** List of datasets to add. */
	private List					datasetsToAdd;
	
	/** List of selected datasets to be added that have to be removed. */
	private List					datasetsToAddToRemove;
	
	private boolean					nameChange, isName;
	
	private ProjectDatasetsDiffPane	dialog;
	
	ProjectEditorManager(ProjectEditor view, DataManagerCtrl control,
						ProjectData model)
	{
		this.view = view;
		this.control = control;
		this.model = model;
		nameChange = false;
		isName = false;
		datasetsToRemove = new ArrayList();
		datasetsToAdd = new ArrayList();
		datasetsToAddToRemove = new ArrayList();
	}
	
	List getDatasetsToAdd() { return datasetsToAdd; }
	
	List getDatasetsToAddToRemove() { return datasetsToAddToRemove; }
	
	ProjectEditor getView() { return view; }
	
	ProjectData getProjectData() { return model; }

	/** Initializes the listeners. */
	void initListeners()
	{
		//buttons
        attachButtonListener(view.getSaveButton(), SAVE);
        attachButtonListener(view.getAddButton(), ADD);
        attachButtonListener(view.getCancelButton(), CANCEL);
        attachButtonListener(view.getRemoveButton(), REMOVE);
        attachButtonListener(view.getResetButton(), RESET);
        attachButtonListener(view.getRemoveToAddButton(), REMOVE_ADDED);
        attachButtonListener(view.getResetToAddButton(), RESET_ADDED);	
        
		//text fields.
        JTextArea nameField = view.getNameField();
		nameField.getDocument().addDocumentListener(this);
		nameField.addMouseListener(this);
		JTextArea descriptionArea = view.getDescriptionArea();
		descriptionArea.getDocument().addDocumentListener(this);
	}
	
    private void attachButtonListener(JButton button, int id)
    {
        button.addActionListener(this);
        button.setActionCommand(""+id);
    }
    
	/** Handles event fired by the buttons. */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		try {
			switch (index) { 
				case SAVE:
					save(); break;
				case ADD:
					showDatasetsSelection(); break;
				case CANCEL:
					cancel(); break;
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
	
	/** Bring up the datasets selection dialog. */
	void showDatasetsSelection()
	{
		if (dialog == null) {
			//tempo solution
			List datasetsDiff = control.getDatasetsDiff(model);
			dialog = new ProjectDatasetsDiffPane(this, datasetsDiff);
		} else {
			dialog.remove(dialog.getContents());
			dialog.buildGUI();
		}
		view.setSelectedPane(ProjectEditor.POS_DATASET);
		UIUtilities.centerAndShow(dialog);
		view.getSaveButton().setEnabled(true);	
	}
	
	/** Add the list of selected datasets to the {@link ProjectDatasetPane}. */
	void addDatasetsSelection(List l)
	{
		Iterator i = l.iterator();
		DatasetSummary ds;
		while (i.hasNext()) {
			ds = (DatasetSummary) i.next();
			if (!datasetsToAdd.contains(ds)) datasetsToAdd.add(ds);
		}
		view.rebuildComponent();
	}
	
	/** 
	 * Add (resp. remove) the dataset summary of (resp. from) the list of
	 * dataset summary to be added.
	 * 
	 * @param value		boolean value true if the checkBox is selected
	 * 					false otherwise.
	 * @param ds		dataset summary to add or remove
	 */
	void setToAddToRemove(boolean value, DatasetSummary ds) 
	{
		if (value)
				datasetsToAddToRemove.add(ds); 
		else {
			if (datasetsToAddToRemove.contains(ds)) 
				datasetsToAddToRemove.remove(ds);
		} 
	}

	/** 
	 * Add (resp. remove) the dataset summary of (resp. from) the list of
	 * dataset summary objects to be removed.
	 * 
	 * @param value		boolean value true if the checkBox is selected
	 * 					false otherwise.
	 * @param ds		dataset summary to add or remove
	 */
	void selectDataset(boolean value, DatasetSummary ds) 
	{
		if (value){
			 if (!datasetsToRemove.contains(ds)) datasetsToRemove.add(ds); 
		} else 	datasetsToRemove.remove(ds);
		view.getSaveButton().setEnabled(true);
	}
	
	/** Close the widget, doesn't save changes. */
	private void cancel()
	{
		view.setVisible(false);
		view.dispose();
	}
	
	/** Save in DB. */
	private void save()
	{
		model.setDescription(view.getDescriptionArea().getText());
		model.setName(view.getNameField().getText());
		control.updateProject(model, datasetsToRemove, datasetsToAdd, 
							nameChange);
		view.dispose();
	}

	/** Select All datasets.*/
	private void remove()
	{
		view.getDatasetsPane().setSelection(Boolean.TRUE);
		view.getRemoveButton().setEnabled(false);
	}
	
	/** Reset the default i.e. no dataset selected. */
	private void resetSelection()
	{
        view.getRemoveButton().setEnabled(true);
		view.getDatasetsPane().setSelection(Boolean.FALSE);
	}

	/** Remove the selected datasets from the queue of datasets to add. */
	private void removeAdded()
	{
		Iterator i = datasetsToAddToRemove.iterator();
		DatasetSummary ds;
		while (i.hasNext()) {
			ds = (DatasetSummary) i.next();
			datasetsToAdd.remove(ds);
			if (dialog != null) dialog.getManager().setSelected(true, ds);
		}
		if (datasetsToAddToRemove.size() != 0) {
			datasetsToAddToRemove.removeAll(datasetsToAddToRemove);
			view.rebuildComponent();
		}
	}

	/** Reset the default for the queue of datasets to add. */
	private void resetAdded()
	{
		datasetsToAddToRemove.removeAll(datasetsToAddToRemove);
		view.rebuildComponent();
	}

	/** Require by I/F. */
	public void changedUpdate(DocumentEvent e)
	{
        view.getSaveButton().setEnabled(true);
	}

	/** Require by I/F. */
	public void insertUpdate(DocumentEvent e)
	{
		view.getSaveButton().setEnabled(isName);
	}
	
	/** Require by I/F. */
	public void removeUpdate(DocumentEvent e)
	{
        view.getSaveButton().setEnabled(isName);
	}

	/** Tells that the name has been modified. */
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
	
