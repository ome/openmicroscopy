/*
 * org.openmicroscopy.shoola.agents.datamng.editors.DatasetImagesDiffPane
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
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.datamng.DataManager;
import org.openmicroscopy.shoola.agents.datamng.IconManager;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
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
class DatasetImagesDiffPane
	extends JDialog
{
	
	private JButton							selectButton, cancelButton, 
											saveButton;
											
	private ImagesTableModel 				imagesTM;
	
	/** Reference to the control of the main widget. */
	private DatasetEditorManager 			control;
	
	private DatasetImagesDiffPaneManager	manager;
	
	private JPanel							contents;
	
	private IconManager						im;
	
	DatasetImagesDiffPane(DatasetEditorManager control, List imagesDiff)
	{
		super(control.getView(), "List of exiting images", true);
		this.control = control;
		im = IconManager.getInstance(control.getView().getRegistry());
		initButtons(imagesDiff);
		manager = new DatasetImagesDiffPaneManager(this, control, imagesDiff);
		buildGUI();
	}
	
	/** 
	 * Return the {@link DatasetImagesDiffPaneManager manager} of the widget.
	 */
	DatasetImagesDiffPaneManager getManager() { return manager; }
	
	JPanel getContents() { return contents; }
	
	/** Return the select button. */
	JButton getSelectButton() { return selectButton; }

	/** Return the cancel button. */
	JButton getCancelButton() { return cancelButton; }

	/** Return the save button. */
	JButton getSaveButton() { return saveButton; }
	
	/** Select or not all datasets. */
	void setSelection(Object val)
	{
		int countCol = imagesTM.getColumnCount()-1;
		for (int i = 0; i < imagesTM.getRowCount(); i++)
			imagesTM.setValueAt(val, i, countCol);
	}
	
	/** initializes the controls. */
	private void initButtons(List img)
	{
		//remove button
		selectButton = new JButton("Select All");
		selectButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		selectButton.setToolTipText(
			UIUtilities.formatToolTipText("Select all the images."));
		//cancel button
		cancelButton = new JButton("Reset");
		cancelButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		cancelButton.setToolTipText(
			UIUtilities.formatToolTipText("Cancel selection."));
		//save button
		saveButton = new JButton("OK");
		saveButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		saveButton.setToolTipText(
			UIUtilities.formatToolTipText("Add the selection."));
		
		if (img == null ||img.size() == 0) {
			selectButton.setEnabled(false);
			cancelButton.setEnabled(false);
			saveButton.setEnabled(false);
		}
	}
	
	/** Build and lay out the GUI. */
	void buildGUI()
	{
		TitlePanel tp = new TitlePanel("Add images", 
							"Select images to add to the dataset.", 
							im.getIcon(IconManager.IMAGE_BIG));
		contents = buildImagesPanel();
		contents.setSize(DataManager.ADD_WIN_WIDTH, DataManager.ADD_WIN_HEIGHT);
		getContentPane().add(tp, BorderLayout.NORTH);
		getContentPane().add(contents, BorderLayout.CENTER);
		setSize(DataManager.ADD_WIN_WIDTH, DataManager.ADD_WIN_HEIGHT);
	}
	
	/** Build panel with table. */
	JPanel buildImagesPanel()
	{
		JPanel controls = new JPanel(), p = new JPanel();
		controls.setLayout(new BoxLayout(controls, BoxLayout.X_AXIS));
		controls.add(cancelButton);
		controls.add(Box.createRigidArea(DataManager.HBOX));
		controls.add(selectButton);
		controls.add(Box.createRigidArea(DataManager.HBOX));
		controls.add(saveButton);
		controls.setOpaque(false); //make panel transparent
		
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		
		//datasets table
		imagesTM = new ImagesTableModel();
		JTable t = new JTable(imagesTM);
		t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		t.setPreferredScrollableViewportSize(DataManager.VP_DIM);
		//wrap table in a scroll pane and add it to the panel
		JScrollPane sp = new JScrollPane(t);
		p.add(sp);
		p.add(Box.createRigidArea(DataManager.VBOX));
		p.add(controls);
		p.add(Box.createRigidArea(DataManager.VBOX));
		return p;
	}
	
	/** 
	 * A <code>3</code>-column table model to view the summary of 
	 * datasets contained in the project.
	 * The first column contains the datasets ID and the 
	 * second column the names. Cells are not editable. 
	 */
	private class ImagesTableModel
		extends AbstractTableModel
	{
		
		private final String[]	columnNames = {"Name", "Add"};
		private final Object[]	images = manager.getImagesDiff().toArray();
		private Object[][] 		data = new Object[images.length][2];
		
		private ImagesTableModel()
		{
			for (int i = 0; i < images.length; i++) {
				data[i][0] = ((ImageSummary) images[i]).getName();
				data[i][1] = new Boolean(false);
			}
		}
	
		public int getColumnCount() { return 2; }
	
		public int getRowCount() { return images.length; }
	
		public String getColumnName(int col) { return columnNames[col]; }
		
		public Class getColumnClass(int c)
		{
			return getValueAt(0, c).getClass();
		}

		public Object getValueAt(int row, int col) { return data[row][col]; }

		public boolean isCellEditable(int row, int col)
		{ 
			boolean isEditable = false;
			if (col == 1) isEditable = true;
			return isEditable;
		}
		
		public void setValueAt(Object value, int row, int col)
		{
			data[row][col] = value;
			fireTableCellUpdated(row, col);
			manager.addImage(((Boolean) value).booleanValue(), 
							(ImageSummary) images[row]);
		}
	}
	
}
