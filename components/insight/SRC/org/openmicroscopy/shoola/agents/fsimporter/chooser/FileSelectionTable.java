/*
 * org.openmicroscopy.shoola.fsimporter.chooser.FileSelectionTable
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.fsimporter.chooser;


//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

//Third-party libraries
import info.clearthought.layout.TableLayout;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.fsimporter.ImporterAgent;
import org.openmicroscopy.shoola.env.data.model.ImportableFile;
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.treetable.renderers.StringCellRenderer;

/**
 * Component displaying the files to import.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class FileSelectionTable 
	extends JPanel
	implements ActionListener
{

	/** Bound property indicating to add files to the queue. */
	static final String ADD_PROPERTY = "add";
	
	/** Bound property indicating to remove files to the queue. */
	static final String REMOVE_PROPERTY = "remove";
	
	/** Action command ID to add a field to the result table. */
	private static final int 		ADD = 0;
	
	/** Action command ID to remove a field from the result table. */
	private static final int 		REMOVE = 1;
	
	/** Action command ID to remove all fields from the result table. */
	private static final int 		REMOVE_ALL = 2;
	
	/** The index of the file's name column. */
	private static final int		FILE_INDEX = 0;
	
	/** 
	 * The index of the column indicating the container where files will
	 * be imported
	 */
	private static final int		CONTAINER_INDEX = 1;
	
	/** 
	 * The index of the column indicating to use the folder
	 * as a dataset. 
	 */
	private static final int		FOLDER_AS_CONTAINER_INDEX = 2;
	
	/** The index of the column indicating to archive the file. */
	private static final int		ARCHIVED_INDEX = 3;
	
	/** The columns of the table. */
	private static final Vector<String> COLUMNS;
	
	/** The columns of the table. */
	private static final Vector<String> COLUMNS_NO_FOLDER_AS_CONTAINER;
	
	/** The width of the column. */
	private static final int COLUMN_WIDTH = 20;
	
	static {
		COLUMNS = new Vector<String>(4);
		COLUMNS.add("File or Folder");
		COLUMNS.add("Container");
		COLUMNS.add(ImportDialog.FAD_ABBREVIATION);
		COLUMNS.add(ImportDialog.ARCHIVED_ABBREVIATION);
		
		COLUMNS_NO_FOLDER_AS_CONTAINER = new Vector<String>(3);
		COLUMNS_NO_FOLDER_AS_CONTAINER.add("File or Folder");
		COLUMNS_NO_FOLDER_AS_CONTAINER.add("Container");
		COLUMNS_NO_FOLDER_AS_CONTAINER.add(ImportDialog.ARCHIVED_ABBREVIATION);
	}
	
	/** The button to move an item from the remaining items to current items. */
	private JButton 			addButton;
	
	/** The button to move an item from the current items to remaining items. */
	private JButton 			removeButton;
	
	/** The button to move all items to the remaining items. */
	private JButton 			removeAllButton;

	/** The table displaying the collection to files to import. */
	private JXTable				table;
	
	/** Reference to the model. */
	private ImportDialog 		model;
	
	/** The default value of the archived file. */
	private boolean archived;
	
	/** The default value of the archived file. */
	private boolean archivedTunable;
	
	/** Formats the table model. */
	private void formatTableModel()
	{
		TableColumnModel tcm = table.getColumnModel();
		TableColumn tc = tcm.getColumn(FOLDER_AS_CONTAINER_INDEX);
		tc.setCellEditor(table.getDefaultEditor(Boolean.class));  
		tc.setCellRenderer(table.getDefaultRenderer(Boolean.class));  
		tc.setPreferredWidth(COLUMN_WIDTH);
		tc.setResizable(false);
		
		if (table.getColumnCount() == COLUMNS.size()) {
			tc = tcm.getColumn(ARCHIVED_INDEX);
			tc.setCellEditor(table.getDefaultEditor(Boolean.class));  
			tc.setCellRenderer(table.getDefaultRenderer(Boolean.class));
			tc.setPreferredWidth(COLUMN_WIDTH);
			tc.setResizable(false);
		}
		TableCellRenderer renderer = new StringCellRenderer();
		for (int i = 0; i < table.getColumnCount(); i++) 
			tcm.getColumn(i).setHeaderRenderer(renderer);
	}
	
	/** Initializes the components composing the display. */
	private void initComponents()
	{
		IconManager icons = IconManager.getInstance();
		addButton = new JButton(icons.getIcon(IconManager.RIGHT_ARROW));
		addButton.setToolTipText("Add the selected files to the queue.");
		addButton.setEnabled(false);
		removeButton = new JButton(icons.getIcon(IconManager.LEFT_ARROW));
		removeButton.setToolTipText("Remove the selected files " +
				"from the queue.");
		removeButton.setEnabled(false);
		removeAllButton = new JButton(
				icons.getIcon(IconManager.DOUBLE_LEFT_ARROW));
		removeAllButton.setToolTipText("Remove all files from the queue.");
		removeAllButton.setEnabled(false);
		addButton.setActionCommand(""+ADD);
		addButton.addActionListener(this);
		removeButton.setActionCommand(""+REMOVE);
		removeButton.addActionListener(this);
		removeAllButton.setActionCommand(""+REMOVE_ALL);
		removeAllButton.addActionListener(this);
		Boolean b = (Boolean) ImporterAgent.getRegistry().lookup(
				ImportDialog.ARCHIVED);
		if (b != null) archived = b.booleanValue();
		b = (Boolean) ImporterAgent.getRegistry().lookup(
				ImportDialog.ARCHIVED_AVAILABLE);
		if (b != null) archivedTunable = b.booleanValue();
		if (model.useFolderAsContainer()) {
			table = new JXTable(new FileTableModel(COLUMNS));
		} else {
			table = new JXTable(new FileTableModel(
					COLUMNS_NO_FOLDER_AS_CONTAINER));
		}
		table.getTableHeader().setReorderingAllowed(false);
		table.addKeyListener(new KeyAdapter() {
			
			/**
			 * Adds the files to the import queue.
			 * @see KeyListener#keyPressed(KeyEvent)
			 */
			public void keyPressed(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					if (table.isFocusOwner())
						removeSelectedFiles();
				}
			}
		});
		formatTableModel();
		Highlighter h = HighlighterFactory.createAlternateStriping(
				UIUtilities.BACKGROUND_COLOUR_EVEN, 
				UIUtilities.BACKGROUND_COLOUR_ODD);
		table.addHighlighter(h);
	}
	
	/**
	 * Builds and lays out the controls.
	 * 
	 * @return See above.
	 */
	JPanel buildControls()
	{
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
		p.add(addButton);
		p.add(Box.createVerticalStrut(5));
		p.add(removeButton);
		p.add(Box.createVerticalStrut(5));
		p.add(removeAllButton);
		return p;
	}
	
	/**
	 * Returns the component hosting the collection of files to import.
	 * 
	 * @return See above.
	 */
	private JPanel buildTablePane()
	{
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
		p.add(Box.createVerticalStrut(5));
		p.add(new JScrollPane(table));
		return p;
	}
	
	/** Builds and lays out the UI. */
	private void builGUI()
	{
		double[][] size = {{TableLayout.FILL}, {TableLayout.FILL}};
		setLayout(new TableLayout(size));
		add(buildTablePane(), "0, 0");
	}

	/** Removes the selected files from the queue. */
	private void removeSelectedFiles()
	{
		int[] rows = table.getSelectedRows();
		if (rows == null || rows.length == 0) return;
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		Vector v = model.getDataVector();
		for (int i = 0; i < rows.length; i++) 
			v.remove(rows[i]);
		
		table.repaint();
		int n = table.getRowCount();
		firePropertyChange(REMOVE_PROPERTY, n-1, n);
		enabledControl(table.getRowCount() > 0);
	}
	
	/**
	 * Sets the enabled flag of the buttons.
	 * 
	 * @param value The value to set.
	 */
	private void enabledControl(boolean value)
	{
		removeButton.setEnabled(value);
		removeAllButton.setEnabled(value);
		if (value) {
			//int[] rows = table.getSelectedRows();
			//removeButton.setEnabled(rows != null && rows.length > 0);
		}
	}
	
	/** 
	 * Creates a new instance. 
	 * 
	 * @param model The model.
	 */
	FileSelectionTable(ImportDialog model)
	{
		if (model == null)
			throw new IllegalArgumentException("No model.");
		this.model = model;
		initComponents();
		builGUI();
	}

	/**
	 * Returns <code>true</code> if there are files to import,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasFilesToImport() { return table.getRowCount() > 0; }
	
	/** 
	 * Returns the collection of files to import. 
	 * 
	 * @return See above.
	 */
	List<ImportableFile> getFilesToImport()
	{
		List<ImportableFile> files = new ArrayList<ImportableFile>();
		int n = table.getRowCount();
		DefaultTableModel dtm = (DefaultTableModel) table.getModel();
		FileElement element;
		File file;
		ImportableFile importable;
		int columns = table.getColumnCount();
		boolean b;
		DataNodeElement dne;
		for (int i = 0; i < n; i++) {
			element = (FileElement) dtm.getValueAt(i, FILE_INDEX);
			dne = (DataNodeElement) dtm.getValueAt(i, CONTAINER_INDEX);
			file = element.getFile();
			if (columns == COLUMNS_NO_FOLDER_AS_CONTAINER.size()) {
				importable = new ImportableFile(file, 
						Boolean.valueOf((Boolean) dtm.getValueAt(i, 
								FOLDER_AS_CONTAINER_INDEX)));
			} else {
				if (file.isFile()) b = false;
				else b = Boolean.valueOf((Boolean) dtm.getValueAt(i, 
						FOLDER_AS_CONTAINER_INDEX));
				importable = new ImportableFile(file, 
						Boolean.valueOf((Boolean) dtm.getValueAt(i, 
					ARCHIVED_INDEX)), b);
			}
			importable.setLocation(dne.getParent(), dne.getLocation());
			importable.setRefNode(dne.getRefNode());
			files.add(importable);
		}
		return files;
	}
	
	/**
	 * Resets the components.
	 * 
	 * @param value The value to set.
	 */
	void reset(boolean value)
	{ 
		allowAddition(value); 
		if (model.useFolderAsContainer())
			table.setModel(new FileTableModel(COLUMNS));
		else 
			table.setModel(new FileTableModel(COLUMNS_NO_FOLDER_AS_CONTAINER));
		formatTableModel();
	}
	
	/**
	 * Sets the enable flag of the {@link #addButton}.
	 * 
	 * @param value The value to set.
	 */
	void allowAddition(boolean value)
	{
		addButton.setEnabled(value); 
	}
	
	/** Removes all the files from the queue. */
	void removeAllFiles()
	{
		int n = table.getRowCount();
		if (n == 0) return;
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		model.getDataVector().clear();
		table.repaint();
		firePropertyChange(REMOVE_PROPERTY, -1, 0);
		enabledControl(false);
	}
	
	/**
	 * Adds the collection of files to the queue.
	 * 
	 * @param files The files to add.
	 */
	void addFiles(List<File> files)
	{
		if (files == null || files.size() == 0) return;
		addButton.setEnabled(false);
		enabledControl(true);
		File f;
		DefaultTableModel dtm = (DefaultTableModel) table.getModel();
		//Check if the file has already 
		List<String> inQueue = new ArrayList<String>();
		FileElement element;
		for (int i = 0; i < table.getRowCount(); i++) {
			element = (FileElement) dtm.getValueAt(i, FILE_INDEX);
			inQueue.add(element.getFile().getAbsolutePath());
		}
		Iterator<File> i = files.iterator();
		int n = table.getModel().getColumnCount();
		boolean b = n == COLUMNS.size();
		DataNode node = model.getImportLocation();
		String value = null;
		boolean fad = model.isFolderAsDataset();
		boolean v = false;
		boolean a = model.isToArchive();
		while (i.hasNext()) {
			f = i.next();
			if (!inQueue.contains(f.getAbsolutePath())) {
				value = null;
				element = new FileElement(f);
				element.setName(f.getName());
				if (b) {
					if (f.isDirectory()) {
						value = f.getName();
						v = fad; 
					}
					dtm.addRow(new Object[] {element, 
							new DataNodeElement(node, value),
							Boolean.valueOf(v), Boolean.valueOf(a)});
				} else dtm.addRow(new Object[] {element, 
						new DataNodeElement(node, null),
						Boolean.valueOf(a)});
			}
		}
	}

	/**
	 * Marks the folder as a dataset.
	 * 
	 * @param fad Pass <code>true</code> to mark the folder as a dataset,
	 * 			  <code>false</code> otherwise.
	 */
	void markFolderAsDataset(boolean fad)
	{
		int n = table.getRowCount();
		if (n == 0) return;
		int m = table.getColumnCount();
		if (COLUMNS_NO_FOLDER_AS_CONTAINER.size() == m) return;
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		FileElement element;
		for (int i = 0; i < n; i++) {
			element = (FileElement) model.getValueAt(i, FILE_INDEX);
			if (element.isDirectory())
				model.setValueAt(fad, i, FOLDER_AS_CONTAINER_INDEX);
		}
	}
	
	/**
	 * Marks to archive the images.
	 * 
	 * @param archive Pass <code>true</code> to archive the images,
	 * 			      <code>false</code> otherwise.
	 */
	void markFileToArchive(boolean archive)
	{
		int n = table.getRowCount();
		if (n == 0) return;
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		int m = table.getColumnCount();
		int index = ARCHIVED_INDEX;
		if (COLUMNS_NO_FOLDER_AS_CONTAINER.size() == m)
			index = index-1;
		for (int i = 0; i < n; i++) {
			model.setValueAt(archive, i, index);
		}
	}
	
	/** Resets the names of all selected files. */
	void resetFilesName()
	{
		int n = table.getRowCount();
		if (n == 0) return;
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		FileElement element;
		for (int i = 0; i < n; i++) {
			element = (FileElement) model.getValueAt(i, FILE_INDEX);
			element.setName(element.getFile().getAbsolutePath());
		}
		table.repaint();
	}
	
	/** Applies the partial names to all the files. */
	void applyToAll()
	{
		int n = table.getRowCount();
		DefaultTableModel dtm = (DefaultTableModel) table.getModel();
		FileElement element;
		for (int i = 0; i < n; i++) {
			element = (FileElement) dtm.getValueAt(i, FILE_INDEX);
			if (!element.isDirectory()) {
				element.setName(model.getDisplayedFileName(
						element.getFile().getAbsolutePath()));
			}
		}
		table.repaint();
	}
	
	/**
	 * Adds or removes files from the import queue.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt)
	{
		int index = Integer.parseInt(evt.getActionCommand());
		switch (index) {
			case ADD:
				firePropertyChange(ADD_PROPERTY, Boolean.valueOf(false),
						Boolean.valueOf(true));
				break;
			case REMOVE:
				removeSelectedFiles();
				break;
			case REMOVE_ALL:
				removeAllFiles();
		}
	}

	/** Inner class so that some cells cannot be edited. */
	class FileTableModel 
		extends DefaultTableModel
	{
		
		/**
		 * Creates a new instance.
		 * 
		 * @param columns	The columns to display.
		 */
		FileTableModel(Vector<String> columns)
		{
			super(null, columns);
		}
		
		/**
		 * Overridden so that some cells cannot be edited.
		 * @see DefaultTableModel#isCellEditable(int, int)
		 */
		public boolean isCellEditable(int row, int column)
		{ 
			switch (column) {
				case FILE_INDEX: 
				case CONTAINER_INDEX:
					return false;
				case FOLDER_AS_CONTAINER_INDEX:
					if (getColumnCount() == 
						COLUMNS_NO_FOLDER_AS_CONTAINER.size())
						return archivedTunable;
					FileElement f = (FileElement) getValueAt(row, FILE_INDEX);
					return f.isDirectory();
				case ARCHIVED_INDEX: return archivedTunable;
			}
			return false; 
		}

		/**
		 * Overridden to set the name of the image to save.
		 * @see DefaultTableModel#setValueAt(Object, int, int)
		 */
		public void setValueAt(Object value, int row, int col)
		{   
			//FileElement f = (FileElement) getValueAt(row, FILE_INDEX);
			//if (value instanceof String) f.setName((String) value);
			if (value instanceof Boolean) {
				if (col == FOLDER_AS_CONTAINER_INDEX) {
					DataNodeElement element = (DataNodeElement) getValueAt(row, 
							CONTAINER_INDEX);
					FileElement f = (FileElement) getValueAt(row, FILE_INDEX);
					if (f.isDirectory()) {
						boolean b = ((Boolean) value).booleanValue();
						if (b) element.setName(f.getName());
						else element.setName(null);
					}
				}
				super.setValueAt(value, row, col);
			}
			fireTableCellUpdated(row, col);
		}
	}

}
