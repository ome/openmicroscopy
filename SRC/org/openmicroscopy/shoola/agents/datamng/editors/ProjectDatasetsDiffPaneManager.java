/*
 * org.openmicroscopy.shoola.agents.datamng.editors.ProjectDatasetsDiffPaneManager
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JButton;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.DatasetSummary;

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
class ProjectDatasetsDiffPaneManager
	implements ActionListener
{
	
	private static final int			ALL = 100;
	private static final int			CANCEL = 101;
	private static final int			SAVE = 102;
	
	/** List of datasets to be added. */
	private List						datasetsToAdd;
	
	/** Reference to the {@link ProjectDatasetsDiffPane view}. */
	private ProjectDatasetsDiffPane 	view;
	
	private ProjectEditorManager		control;
	
	private JButton						cancelButton, selectButton, saveButton;
	
	private List						datasetsDiff;
	
	ProjectDatasetsDiffPaneManager(ProjectDatasetsDiffPane view, 
									ProjectEditorManager control, 
									List datasetsDiff)
	{
			this.view = view;
			this.control = control;	
			this.datasetsDiff = datasetsDiff;	
			datasetsToAdd = new ArrayList();
			attachListeners();					
	}
	
	private void attachListeners()
	{
		selectButton = view.getSelectButton();
		selectButton.addActionListener(this);
		selectButton.setActionCommand(""+ALL);
		cancelButton = view.getCancelButton();
		cancelButton.addActionListener(this);
		cancelButton.setActionCommand(""+CANCEL);
		saveButton = view.getSaveButton();
		saveButton.addActionListener(this);
		saveButton.setActionCommand(""+SAVE);
	}

	/** Handle events fired by the buttons. */
	
	public void actionPerformed(ActionEvent e)
	{
		String s = (String) e.getActionCommand();
		try {
			int index = Integer.parseInt(s);
			switch (index) { 
				case SAVE:
					saveSelection();
					break;
				case ALL:
					selectAll();
					break;
				case CANCEL:
					cancelSelection();
					break;
			}// end switch  
		} catch(NumberFormatException nfe) {
		   throw nfe;  //just to be on the safe side...
		} 
	}
	
	void setSelected(boolean value, DatasetSummary ds)
	{
		if (value) datasetsDiff.add(ds);
		else datasetsDiff.remove(ds);
		if (datasetsDiff.size() == 0) {
			selectButton.setEnabled(false);
			cancelButton.setEnabled(false);
			saveButton.setEnabled(false);
		} else {
			selectButton.setEnabled(true);
			cancelButton.setEnabled(true);
			saveButton.setEnabled(true);
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
		if (value)	{
			if (!datasetsToAdd.contains(ds)) datasetsToAdd.add(ds);
		} else 	datasetsToAdd.remove(ds);
	}
	
	/** Add the selection to the datasets. */
	private void saveSelection()
	{
		if (datasetsToAdd.size() != 0) {
			Iterator i = datasetsToAdd.iterator();
			while (i.hasNext())
				datasetsDiff.remove((DatasetSummary) i.next());
		
			if (datasetsDiff.size() == 0) {
				selectButton.setEnabled(false);
				cancelButton.setEnabled(false);
				saveButton.setEnabled(false);
			} else {
				selectButton.setEnabled(true);
				cancelButton.setEnabled(true);
				saveButton.setEnabled(true);
			}
			control.addDatasetsSelection(datasetsToAdd);
		}
		view.setVisible(false);
	}
	
	/** Select All datasets.*/
	private void selectAll()
	{
		selectButton.setEnabled(false);
		view.setSelection(new Boolean(true));
	}
	
	private void cancelSelection()
	{
		selectButton.setEnabled(true);
		view.setSelection(new Boolean(false));
	}
	
}
