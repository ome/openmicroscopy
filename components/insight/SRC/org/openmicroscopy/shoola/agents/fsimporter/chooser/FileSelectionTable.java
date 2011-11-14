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
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

//Third-party libraries
import info.clearthought.layout.TableLayout;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.fsimporter.ImporterAgent;
import org.openmicroscopy.shoola.agents.fsimporter.view.Importer;
import org.openmicroscopy.shoola.agents.util.browser.DataNode;
import org.openmicroscopy.shoola.env.data.model.ImportableFile;
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.MultilineHeaderSelectionRenderer;
import org.openmicroscopy.shoola.util.ui.TooltipTableHeader;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.DatasetData;

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
	static final int		FILE_INDEX = 0;
	
	/** The index of the file's length column. */
	static final int		SIZE_INDEX = 1;
	
	/** 
	 * The index of the column indicating the container where files will
	 * be imported
	 */
	static final int		CONTAINER_INDEX = 2;
	
	/** 
	 * The index of the column indicating to use the folder
	 * as a dataset. 
	 */
	static final int		FOLDER_AS_CONTAINER_INDEX = 3;
	
	/** The index of the column indicating to archive the file. */
	private static final int		ARCHIVED_INDEX = 4;
	
	/** The columns of the table. */
	private static final Vector<String> COLUMNS;
	
	/** The columns of the table. */
	private static final Vector<String> COLUMNS_NO_FOLDER_AS_CONTAINER;
	
	/** The tool-tip of the columns. */
	private static final String[] COLUMNS_TOOLTIP;
	
	/** The tool-tip of the columns if no folder specified. */
	private static final String[] COLUMNS_NO_FOLDER_AS_CONTAINER_TOOLTIP;
	
	/** String used to retrieve if the value of the archived flag. */
	private static final String ARCHIVED = "/options/Archived";
	
	/** String used to retrieve if the archived option is displayed. */
	private static final String ARCHIVED_AVAILABLE = "/options/ArchivedTunable";
	
	/** The text displayed to use the folder as container. */
	private static final String FAD_TEXT = "Folder as\nDataset";
	
	/** The text displayed to archived the files. */
	private static final String ARCHIVED_TEXT = "Archive";
	
	/** The text displayed to select the files. */
	private static final String FILE_TEXT = "File or\nFolder";
	
	/** The text displayed to indicate the size of the file or folder. */
	private static final String SIZE_TEXT = "Size";
	
	/** 
	 * The text displayed where to import the data to if importing
	 * to Project/Dataset or Screen.
	 */
	private static final String CONTAINER_PROJECT_TEXT = 
		"Project/Dataset\nor Screen";
	
	/** 
	 * The text displayed where to import the data to if importing
	 * to Screen.
	 */
	private static final String CONTAINER_SCREEN_TEXT = "Screen";
	
	static {
		int n = 5;
		COLUMNS = new Vector<String>(n);
		COLUMNS.add(FILE_TEXT);
		COLUMNS.add(SIZE_TEXT);
		COLUMNS.add(CONTAINER_PROJECT_TEXT);
		COLUMNS.add(FAD_TEXT);
		COLUMNS.add(ARCHIVED_TEXT);
		COLUMNS_TOOLTIP = new String[n];
		COLUMNS_TOOLTIP[FILE_INDEX] = "File or Folder to import.";
		COLUMNS_TOOLTIP[SIZE_INDEX] = "Size of File or Folder.";
		COLUMNS_TOOLTIP[CONTAINER_INDEX] = 
			"The container where to import the data.";
		COLUMNS_TOOLTIP[FOLDER_AS_CONTAINER_INDEX] = 
			"Convert the folder as dataset.";
		COLUMNS_TOOLTIP[ARCHIVED_INDEX] = "Archive the data.";
		
		n = 4;
		COLUMNS_NO_FOLDER_AS_CONTAINER = new Vector<String>(n);
		COLUMNS_NO_FOLDER_AS_CONTAINER.add(FILE_TEXT);
		COLUMNS_NO_FOLDER_AS_CONTAINER.add(SIZE_TEXT);
		COLUMNS_NO_FOLDER_AS_CONTAINER.add(CONTAINER_PROJECT_TEXT);
		COLUMNS_NO_FOLDER_AS_CONTAINER.add(ARCHIVED_TEXT);
		
		COLUMNS_NO_FOLDER_AS_CONTAINER_TOOLTIP = new String[n];
		COLUMNS_NO_FOLDER_AS_CONTAINER_TOOLTIP[FILE_INDEX] = 
			COLUMNS_TOOLTIP[FILE_INDEX];
		COLUMNS_NO_FOLDER_AS_CONTAINER_TOOLTIP[SIZE_INDEX] = 
			COLUMNS_TOOLTIP[SIZE_INDEX];
		COLUMNS_NO_FOLDER_AS_CONTAINER_TOOLTIP[CONTAINER_INDEX] = 
			COLUMNS_TOOLTIP[CONTAINER_INDEX];
		COLUMNS_NO_FOLDER_AS_CONTAINER_TOOLTIP[ARCHIVED_INDEX-1] = 
			COLUMNS_TOOLTIP[ARCHIVED_INDEX];
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
	
	/** Indicates to archive the files. */
	private JCheckBox			archivedBox;
	
	/** The key listener added to the queue. */
	private KeyAdapter			keyListener;
	
	/** The columns selected for the display.*/
	private Vector<String>		selectedColumns;
	
	/** Formats the table model. */
	private void formatTableModel()
	{
		TableColumnModel tcm = table.getColumnModel();
		TableColumn tc = tcm.getColumn(FILE_INDEX);
		tc.setCellRenderer(new FileTableRenderer()); 
		
		tc = tcm.getColumn(CONTAINER_INDEX);
		tc.setCellRenderer(new FileTableRenderer()); 
		
		tc = tcm.getColumn(FOLDER_AS_CONTAINER_INDEX);
		tc.setCellEditor(table.getDefaultEditor(Boolean.class));  
		tc.setCellRenderer(table.getDefaultRenderer(Boolean.class));  
		tc.setCellRenderer(new FileTableRenderer());
		tc.setResizable(false);

		String[] tips;
		int n = table.getColumnCount();
		if (n == COLUMNS.size()) {
			tc = tcm.getColumn(ARCHIVED_INDEX);
			tc.setCellEditor(table.getDefaultEditor(Boolean.class));  
			tc.setCellRenderer(table.getDefaultRenderer(Boolean.class));
			tc.setResizable(false);
			tips = COLUMNS_TOOLTIP;
		} else 
			tips = COLUMNS_NO_FOLDER_AS_CONTAINER_TOOLTIP;

		TooltipTableHeader header = new TooltipTableHeader(tcm, tips);
		table.setTableHeader(header);
		
		//renderer = new MultiLineHeader();
		tcm.getColumn(SIZE_INDEX).setHeaderRenderer(
				new MultilineHeaderSelectionRenderer());

		tc = tcm.getColumn(FILE_INDEX);
		tc.setHeaderRenderer(new MultilineHeaderSelectionRenderer());
		tc = tcm.getColumn(CONTAINER_INDEX);
		tc.setHeaderRenderer(new MultilineHeaderSelectionRenderer());
		if (n == COLUMNS.size()) {
			tc = tcm.getColumn(FOLDER_AS_CONTAINER_INDEX);
			tc.setCellRenderer(new FileTableRenderer());
			tc.setHeaderRenderer(new MultilineHeaderSelectionRenderer());
			tcm.getColumn(ARCHIVED_INDEX).setHeaderRenderer(
					new MultilineHeaderSelectionRenderer(table, archivedBox));
		} else {
			tcm.getColumn(FOLDER_AS_CONTAINER_INDEX).setHeaderRenderer(
					new MultilineHeaderSelectionRenderer(table, archivedBox));
		}
		/*
		String text = CONTAINER_PROJECT_TEXT;
		if (model.getType() == Importer.SCREEN_TYPE)
			text = CONTAINER_SCREEN_TEXT;
		tc = tcm.getColumn(CONTAINER_INDEX);
		tc.setHeaderValue(text);
		*/
		table.getTableHeader().resizeAndRepaint();
		table.getTableHeader().setReorderingAllowed(false);
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
		Boolean b = (Boolean) ImporterAgent.getRegistry().lookup(ARCHIVED);
		if (b != null) archived = b.booleanValue();
		b = (Boolean) ImporterAgent.getRegistry().lookup(ARCHIVED_AVAILABLE);
		if (b != null) archivedTunable = b.booleanValue();
		//if (model.useFolderAsContainer()) {
			selectedColumns = COLUMNS;
		//} else {
		//	selectedColumns = COLUMNS_NO_FOLDER_AS_CONTAINER;
		//}
		table = new JXTable(new FileTableModel(selectedColumns));
		table.getTableHeader().setReorderingAllowed(false);
		keyListener = new KeyAdapter() {
			
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
		};
		table.addKeyListener(keyListener);
		
		Highlighter h = HighlighterFactory.createAlternateStriping(
				UIUtilities.BACKGROUND_COLOUR_EVEN, 
				UIUtilities.BACKGROUND_COLOUR_ODD);
		table.addHighlighter(h);
		

		archivedBox = new JCheckBox();
		archivedBox.setBackground(UIUtilities.BACKGROUND);
		//archivedBox.setHorizontalTextPosition(SwingConstants.LEFT);
		archivedBox.setSelected(archived);
		archivedBox.setEnabled(archivedTunable);
    	if (archivedTunable) {
    		archivedBox.addChangeListener(new ChangeListener() {
				
				public void stateChanged(ChangeEvent e) {
					markFileToArchive(archivedBox.isSelected());
				}
			});
    	}
    	
    	formatTableModel();
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
		table.removeKeyListener(keyListener);
		int[] rows = table.getSelectedRows();
		if (rows == null || rows.length == 0) return;
		DefaultTableModel dtm = (DefaultTableModel) table.getModel();
		int rowCount = dtm.getRowCount();
		Vector v = dtm.getDataVector();
		List<Object> indexes = new ArrayList<Object>();
		for (int i = 0; i < table.getRowCount(); i++) {
			if (table.isRowSelected(i))
				indexes.add(v.get(i));
		}
		v.removeAll(indexes);
		dtm.setDataVector(v, selectedColumns);
		table.clearSelection();
		formatTableModel();
		table.repaint();
		table.addKeyListener(keyListener);
		int n = table.getRowCount();
		firePropertyChange(REMOVE_PROPERTY, n-1, n);
		enabledControl(table.getRowCount() > 0);
		model.onSelectionChanged();
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
	 * Builds and lays out the controls.
	 * 
	 * @return See above.
	 */
	JPanel buildControls()
	{
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.add(addButton);
		p.add(Box.createVerticalStrut(5));
		p.add(removeButton);
		p.add(Box.createVerticalStrut(5));
		p.add(removeAllButton);
		return p;
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
		DatasetData dataset;
		for (int i = 0; i < n; i++) {
			element = (FileElement) dtm.getValueAt(i, FILE_INDEX);
			dne = (DataNodeElement) dtm.getValueAt(i, CONTAINER_INDEX);
			file = element.getFile();
			dataset = dne.getLocation();
			if (columns == COLUMNS_NO_FOLDER_AS_CONTAINER.size()) {
				importable = new ImportableFile(file, 
						Boolean.valueOf((Boolean) dtm.getValueAt(i, 
								FOLDER_AS_CONTAINER_INDEX)));
			} else {
				/*
				if (file.isFile()) b = false;
				else b = Boolean.valueOf((Boolean) dtm.getValueAt(i, 
						FOLDER_AS_CONTAINER_INDEX));
						*/
				b = Boolean.valueOf((Boolean) dtm.getValueAt(i, 
						FOLDER_AS_CONTAINER_INDEX));
				importable = new ImportableFile(file, 
						Boolean.valueOf((Boolean) dtm.getValueAt(i, 
					ARCHIVED_INDEX)), b);
				if (b) dataset = null;
			}
			importable.setLocation(dne.getParent(), dataset);
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
		if (model.useFolderAsContainer()) selectedColumns = COLUMNS;
		else selectedColumns = COLUMNS_NO_FOLDER_AS_CONTAINER;
		table.setModel(new FileTableModel(selectedColumns));
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
		DefaultTableModel dtm = (DefaultTableModel) table.getModel();
		dtm.getDataVector().clear();
		table.clearSelection();
		formatTableModel();
		table.repaint();
		firePropertyChange(REMOVE_PROPERTY, -1, 0);
		enabledControl(false);
		model.onSelectionChanged();
	}
	
	/**
	 * Adds the collection of files to the queue.
	 * 
	 * @param files The files to add.
	 * @param fad 	Pass <code>true</code> to indicate to mark the folder as
	 * 				a dataset, <code>false</code> otherwise.
	 */
	void addFiles(List<File> files, boolean fad)
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
		int n = dtm.getColumnCount();
		boolean b = n == COLUMNS.size();
		DataNode node = model.getImportLocation();
		if (node.isDefaultNode() && model.getType() != Importer.SCREEN_TYPE)
			node.setParent(model.getParentImportLocation());
		String value = null;
		boolean v = false;
		boolean a = archivedBox.isSelected();
		while (i.hasNext()) {
			f = i.next();
			if (!inQueue.contains(f.getAbsolutePath())) {
				element = new FileElement(f, model.getType());
				element.setName(f.getName());
				value = null;
				v = false;
				if (b) {
					if (f.isDirectory()) {
						value = f.getName();
						v = fad;
						if (model.getType() == Importer.SCREEN_TYPE) {
							//v = false;
							value = null;
						}
					} else {
						if (model.isParentFolderAsDataset()) {
							value = f.getParentFile().getName();
							v = true;
							element.setToggleContainer(v);
						}
					}
					dtm.addRow(new Object[] {element, 
							element.getFileLengthAsString(),
							new DataNodeElement(node, value),
							Boolean.valueOf(v), Boolean.valueOf(a)});
				} else dtm.addRow(new Object[] {element, 
						element.getFileLengthAsString(),
						new DataNodeElement(node, null),
						Boolean.valueOf(a)});
			}
		}
		model.onSelectionChanged();
	}

	/**
	 * Returns the size of the files to import.
	 * 
	 * @return See above.
	 */
	long getSizeFilesInQueue()
	{
		DefaultTableModel dtm = (DefaultTableModel) table.getModel();
		FileElement element;
		long size = 0;
		for (int i = 0; i < table.getRowCount(); i++) {
			element = (FileElement) dtm.getValueAt(i, FILE_INDEX);
			size += element.getFileLength();
		}
		return size;
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
				case SIZE_INDEX:
					return false;
				case FOLDER_AS_CONTAINER_INDEX:	
					if (getColumnCount() == 
						COLUMNS_NO_FOLDER_AS_CONTAINER.size())
						return archivedTunable;
					FileElement f = (FileElement) getValueAt(row, FILE_INDEX);
					if (f.getType() == Importer.SCREEN_TYPE)
						return false;
					return false;//f.isDirectory();
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
			if (value instanceof Boolean) {
				if (col == FOLDER_AS_CONTAINER_INDEX) {
					DataNodeElement element = (DataNodeElement) getValueAt(row, 
							CONTAINER_INDEX);
					FileElement f = (FileElement) getValueAt(row, FILE_INDEX);
					if (f.isDirectory() || (!f.isDirectory() && 
							f.isToggleContainer())) {
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
