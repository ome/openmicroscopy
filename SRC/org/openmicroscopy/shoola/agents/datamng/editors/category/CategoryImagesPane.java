/*
 * org.openmicroscopy.shoola.agents.datamng.editors.category.CategoryImagesPane
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

package org.openmicroscopy.shoola.agents.datamng.editors.category;


//Java imports
import java.awt.Cursor;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.datamng.DataManagerUIF;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.util.ui.table.TableComponent;
import org.openmicroscopy.shoola.util.ui.table.TableComponentCellEditor;
import org.openmicroscopy.shoola.util.ui.table.TableComponentCellRenderer;
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
class CategoryImagesPane
	extends JPanel
{
						
	/** Reference to the manager. */
	private CategoryEditorManager              manager;
	
	JButton                                    removeButton, resetButton, 
											    removeToAddButton, resetToAddButton;
	
	private JPanel                             tablePanel, buttonsPanel,
											    buttonsToAddPanel;
	
	private ImagesTableModel 				  imagesTM;

	
	CategoryImagesPane(CategoryEditorManager manager)
	{
		this.manager = manager;
		buildGUI();
	}
	
	/** Select or not all images. */
	void setSelection(Object val)
	{
		int countCol = imagesTM.getColumnCount()-1;
		for (int i = 0; i < imagesTM.getRowCount(); i++)
			imagesTM.setValueAt(val, i, countCol);
	}
	
	/** Rebuild the component if some images are marked to be added. */ 
	void rebuildComponent()
	{
		removeAll();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(tablePanel);
		add(Box.createRigidArea(DataManagerUIF.VBOX));
		add(buttonsPanel);
		if (manager.getImagesToAdd().size() != 0) {
			add(buildTableToAddPanel());
			add(Box.createRigidArea(DataManagerUIF.VBOX));
			add(buttonsToAddPanel);
		}
		
		Border b = BorderFactory.createEmptyBorder(0, 0, 10, 10);
		setBorder(b);
	}
	
	/** Build and lay out the GUI. */
	private void buildGUI()
	{
		tablePanel = buildTablePanel();
		buttonsPanel = buildButtonsPanel();
		buttonsToAddPanel = buildButtonsToAddPanel();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(tablePanel);
		add(Box.createRigidArea(DataManagerUIF.VBOX));
		add(buttonsPanel);
		add(Box.createRigidArea(DataManagerUIF.VBOX));
		Border b = BorderFactory.createEmptyBorder(0, 0, 10, 10);
		setBorder(b);
	}
	
	/** 
	 * Build a panel with buttons used to remove or not 
	 * the selected datatsets.
	 */
	private JPanel buildButtonsToAddPanel()
	{
		JPanel controls = new JPanel();
		//remove button
		removeToAddButton = new JButton("Remove added");
		removeToAddButton.setCursor(
					Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		removeToAddButton.setToolTipText(
			UIUtilities.formatToolTipText("Remove the images " +
									"from the list."));

		//cancel button
		resetToAddButton = new JButton("Reset");
		resetToAddButton.setCursor(
						Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		resetToAddButton.setToolTipText(
			UIUtilities.formatToolTipText("Cancel selection."));

		controls.setLayout(new BoxLayout(controls, BoxLayout.X_AXIS));
		controls.add(resetToAddButton);
		controls.add(Box.createRigidArea(DataManagerUIF.HBOX));
		controls.add(removeToAddButton);
		controls.setOpaque(false); //make panel transparent
		return controls;
	}

	/** Initializes and build panel containing the buttons. */
	private JPanel buildButtonsPanel()
	{
		JPanel controls = new JPanel();
		//remove button
		removeButton = new JButton("Select All");
		removeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		removeButton.setToolTipText(
			UIUtilities.formatToolTipText("Remove all categories."));
		//cancel button
		resetButton = new JButton("Reset");
		resetButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		resetButton.setToolTipText(
			UIUtilities.formatToolTipText("Cancel selection."));
		controls.setLayout(new BoxLayout(controls, BoxLayout.X_AXIS));
		controls.add(resetButton);
		controls.add(Box.createRigidArea(DataManagerUIF.HBOX));
		controls.add(removeButton);
		controls.setOpaque(false); //make panel transparent
	
		if (manager.getImages() == null || manager.getImages().size() == 0) {
			removeButton.setEnabled(false);
			resetButton.setEnabled(false);
		}
		return controls;
	}

	/** Build panel with table containing the images to add. */
	private JPanel buildTableToAddPanel()
	{
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.add(buildLabelTable());
		ImagesAddTableModel tm = new ImagesAddTableModel();
		JTable table = new JTable(tm);
		table.setBackground(DataManagerUIF.STEELBLUE);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setPreferredScrollableViewportSize(DataManagerUIF.VP_DIM);
		//wrap table in a scroll pane and add it to the panel
		JScrollPane spAdd = new JScrollPane(table);
		p.add(spAdd);
		return p;
	}

	/** Build panel with table containing existing datasets. */
	private JPanel buildTablePanel()
	{
  		JPanel  p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		//images table
		imagesTM = new ImagesTableModel();
		JTable t = new JTable(imagesTM);
		t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		t.setPreferredScrollableViewportSize(DataManagerUIF.VP_DIM);
		//wrap table in a scroll pane and add it to the panel
		JScrollPane sp = new JScrollPane(t);
		p.add(sp);
		return p;
	}
	
	private TableComponent buildLabelTable()
	{
		TableComponent table = new TableComponent(1, 2);
		setTableLayout(table);
		//First row.
		JLabel label = UIUtilities.setTextFont(" Images to add");
		table.setValueAt(label, 0, 0);
		label = new JLabel("");
		table.setValueAt(label, 0, 1);
		return table;
	}

	/** Set the table layout. */
	private void setTableLayout(TableComponent table)
	{
		table.setTableHeader(null);
		table.setOpaque(false);
		table.setShowGrid(false);
		table.setRowHeight(DataManagerUIF.ROW_NAME_FIELD);
		table.setDefaultRenderer(JComponent.class, 
								new TableComponentCellRenderer());
		table.setDefaultEditor(JComponent.class, 
								new TableComponentCellEditor());
	}
	
	/** 
	 * A <code>3</code>-column table model to view the summary of 
	 * images contained in the dataset.
	 * The first column contains the datasets ID and the 
	 * second column the names. Cells are not editable. 
	 */
	private class ImagesTableModel
		extends AbstractTableModel
	{
		
		private final String[]	columnNames = {"Name", "Declassify"};
		private final Object[]	images = manager.getImages().toArray();
		private Object[][]		data = new Object[images.length][2];
		
		private ImagesTableModel()
		{
			for (int i = 0; i < images.length; i++) {
				data[i][0] = ((ImageSummary) images[i]).getName();
				data[i][1] = Boolean.FALSE;
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
			manager.selectImage(((Boolean) value).booleanValue(), 
								(ImageSummary) images[row]);
		}
	}
	
	/** 
	 * A <code>3</code>-column table model to view the summary of 
	 * datasets to be added to the project.
	 * The first column contains the datasets ID and the 
	 * second column the names. Cells are not editable. 
	 */
	private class ImagesAddTableModel
		extends AbstractTableModel
	{
		
		private final String[]	columnNames = {"Name", "Classify"};
		private final Object[]	images = manager.getImagesToAdd().toArray();
		private Object[][] 		data = new Object[images.length][2];
		
		private List			imgs = manager.getImagesToAddToRemove();
		
		private ImagesAddTableModel()
		{
			ImageSummary is;
		
			for (int i = 0; i < images.length; i++) {
				is = (ImageSummary) images[i];
				data[i][0] = is.getName();
				data[i][1] = new Boolean(imgs.contains(is));
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

		public boolean isCellEditable(int row, int col) { return (col == 1); }

		public void setValueAt(Object value, int row, int col)
		{
			data[row][col] = value;
			fireTableCellUpdated(row, col);
			manager.setToAddToRemove(((Boolean) value).booleanValue(),
									(ImageSummary) images[row]);
		}
	}
	
}
