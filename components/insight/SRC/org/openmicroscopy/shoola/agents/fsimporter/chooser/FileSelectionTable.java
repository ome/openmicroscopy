/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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

import ij.IJ;
import ij.ImagePlus;
import info.clearthought.layout.TableLayout;

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
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.commons.collections.CollectionUtils;
import org.openmicroscopy.shoola.agents.fsimporter.ImporterAgent;
import org.openmicroscopy.shoola.agents.fsimporter.view.Importer;
import org.openmicroscopy.shoola.agents.util.browser.DataNode;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.data.model.FileObject;
import org.openmicroscopy.shoola.env.data.model.ImportableFile;
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.MultilineHeaderSelectionRenderer;
import org.openmicroscopy.shoola.util.ui.TooltipTableHeader;

import omero.gateway.model.DatasetData;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;

/**
 * Component displaying the files to import.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
class FileSelectionTable 
	extends JPanel
	implements ActionListener
{

    /** Description of the <code>Remove All</code> action.*/
	private static final String TOOLTIP_BUTTON_REMOVE_ALL = "Remove all files" +
			" from the queue.";

	/** Description of the <code>Removel</code> action.*/
	private static final String TOOLTIP_BUTTON_REMOVE = "Remove the selected" +
			" files from the queue.";

	/** Description of the <code>Add All</code> action.*/
	private static final String TOOLTIP_BUTTON_ADD = "Add the selected files"+
	        "to the queue.";

	/** Tooltip text for group column */
	private static final String TOOLTIP_GROUP = 
			"The group where to import data.";

	/** Tooltip text for owner column */
	private static final String TOOLTIP_OWNER = "The owner of imported data";

	/** Tooltip text for folder as dataset column */
	private static final String TOOLTIP_FAD = "Convert the folder as dataset.";

	/** Tooltip text for container column */
	private static final String TOOLTIP_CONTAINER = 
			"The container where to import the data.";

	/** Tooltip text for size column */
	private static final String TOOLTIP_SIZE = "Size of File or Folder.";

	/** Tooltip text for file column */
	private static final String TOOLTIP_FILE = "File or Folder to import.";

	/** Bound property indicating to add files to the queue. */
	static final String ADD_PROPERTY = "add";

	/** Bound property indicating to remove files to the queue. */
	static final String REMOVE_PROPERTY = "remove";

	/** Action command ID to add a field to the result table. */
	private static final int ADD = 0;

	/** Action command ID to remove a field from the result table. */
	private static final int REMOVE = 1;

	/** Action command ID to remove all fields from the result table. */
	private static final int REMOVE_ALL = 2;

	/** The text displayed to use the folder as container. */
	private static final String FAD_TEXT = "Folder as\nDataset";

	/** Indicate to select the files. */
	private static final String FILE_TEXT = "File or\nFolder";

	/** The text indicating the size of the file or folder. */
	private static final String SIZE_TEXT = "Size";

	/** 
	 * The text displaying where to import the data to if importing
	 * to Project/Dataset or Screen.
	 */
	private static final String CONTAINER_PROJECT_TEXT =
		"Project/Dataset\nor Screen";

	/** The group where the files will be imported.*/
	private static final String GROUP_TEXT = "Group";

	/** The owner of the imported data. */
	private static final String OWNER_TEXT = "Owner";

	/** The button to move an item from the remaining items to current items. */
	private JButton addButton;

	/** The button to move an item from the current items to remaining items. */
	private JButton removeButton;

	/** The button to move all items to the remaining items. */
	private JButton removeAllButton;

	/** The table displaying the collection to files to import. */
	private JTable table;

	/** Reference to the model. */
	private ImportDialog model;

	/** The key listener added to the queue. */
	private KeyAdapter keyListener;

    /* the index of each column, null if the column is not shown */
    private Integer fileIndex, groupIndex, ownerIndex, containerIndex, folderAsDatasetIndex, sizeIndex;

    /* the headings of the columns selected for the display */
    private final Vector<String> columnHeadings = new Vector<String>();

    /* the tooltips for the columns selected for the display */
    private final Vector<String> columnTooltips = new Vector<String>();

    /** Formats the table model. */
    private void formatTableModel()
    {
        final TableColumnModel tcm = table.getColumnModel();

        int index = 0;
        this.columnHeadings.clear();
        this.columnTooltips.clear();

        this.fileIndex = index++;
        this.columnHeadings.add(FILE_TEXT);
        this.columnTooltips.add(TOOLTIP_FILE);
        tcm.getColumn(this.fileIndex).setCellRenderer(new FileTableRendererFileColumn());

        if (model.isSingleGroup()) {
            this.groupIndex = null;
        } else {
            this.groupIndex = index++;
            this.columnHeadings.add(GROUP_TEXT);
            this.columnTooltips.add(TOOLTIP_GROUP);
        }

        if (model.canImportAs()) {
            this.ownerIndex = index++;
            this.columnHeadings.add(OWNER_TEXT);
            this.columnTooltips.add(TOOLTIP_OWNER);
        } else {
            this.ownerIndex = null;
        }

        this.containerIndex = index++;
        this.columnHeadings.add(CONTAINER_PROJECT_TEXT);
        this.columnTooltips.add(TOOLTIP_CONTAINER);
        tcm.getColumn(this.containerIndex).setCellRenderer(new FileTableRendererContainerColumn());

        this.folderAsDatasetIndex = index++;
        this.columnHeadings.add(FAD_TEXT);
        this.columnTooltips.add(TOOLTIP_FAD);
        setColumnAsBoolean(tcm.getColumn(this.folderAsDatasetIndex));

        this.sizeIndex = index++;
        this.columnHeadings.add(SIZE_TEXT);
        this.columnTooltips.add(TOOLTIP_SIZE);
 
        table.setTableHeader(new TooltipTableHeader(tcm, columnTooltips));

        final TableCellRenderer renderer = new MultilineHeaderSelectionRenderer();
        while (--index >= 0) {
            setHeaderRenderer(tcm, index, renderer);
        }

		table.getTableHeader().resizeAndRepaint();
		table.getTableHeader().setReorderingAllowed(false);
	}

	/**
	 * Helper method to setup the column as a boolean column
	 * @param column the column to set
	 */
	private void setColumnAsBoolean(TableColumn column) {
	    column.setCellEditor(table.getDefaultEditor(Boolean.class));
	    column.setCellRenderer(table.getDefaultRenderer(Boolean.class));
	    column.setResizable(false);
	}

	/**
	 * Helper method to sets the table header with the specified index to the 
	 * renderer provided.
	 * @param columnModel The <see>TableColumnModel</see> to use
	 * @param columnIndex The index of the column to set
	 * @param renderer The renderer to set
	 */
	private void setHeaderRenderer(TableColumnModel columnModel,
			int columnIndex, TableCellRenderer renderer) {
		TableColumn column = columnModel.getColumn(columnIndex);
		column.setHeaderRenderer(renderer);
	}

	/* initialize the column headings, tool-tips, and indices thereof */
    private void initColumns() {
        int index = 0;
        this.columnHeadings.clear();
        this.columnTooltips.clear();

        this.fileIndex = index++;
        this.columnHeadings.add(FILE_TEXT);
        this.columnTooltips.add(TOOLTIP_FILE);

        if (model.isSingleGroup()) {
            this.groupIndex = null;
        } else {
            this.groupIndex = index++;
            this.columnHeadings.add(GROUP_TEXT);
            this.columnTooltips.add(TOOLTIP_GROUP);
        }

        if (model.canImportAs()) {
            this.ownerIndex = index++;
            this.columnHeadings.add(OWNER_TEXT);
            this.columnTooltips.add(TOOLTIP_OWNER);
        } else {
            this.ownerIndex = null;
        }

        this.containerIndex = index++;
        this.columnHeadings.add(CONTAINER_PROJECT_TEXT);
        this.columnTooltips.add(TOOLTIP_CONTAINER);

        this.folderAsDatasetIndex = index++;
        this.columnHeadings.add(FAD_TEXT);
        this.columnTooltips.add(TOOLTIP_FAD);

        this.sizeIndex = index++;
        this.columnHeadings.add(SIZE_TEXT);
        this.columnTooltips.add(TOOLTIP_SIZE);
    }

	/** Initializes the components composing the display. */
	private void initComponents()
	{
	    IconManager icons = IconManager.getInstance();
	    addButton = new JButton(icons.getIcon(IconManager.RIGHT_ARROW));
	    addButton.setToolTipText(TOOLTIP_BUTTON_ADD);
	    addButton.setEnabled(false);
	    removeButton = new JButton(icons.getIcon(IconManager.LEFT_ARROW));
	    removeButton.setToolTipText(TOOLTIP_BUTTON_REMOVE);
	    removeButton.setEnabled(false);
	    removeAllButton = new JButton(
	            icons.getIcon(IconManager.DOUBLE_LEFT_ARROW));
	    removeAllButton.setToolTipText(TOOLTIP_BUTTON_REMOVE_ALL);
	    removeAllButton.setEnabled(false);
	    addButton.setActionCommand(""+ADD);
	    addButton.addActionListener(this);
	    removeButton.setActionCommand(""+REMOVE);
	    removeButton.addActionListener(this);
	    removeAllButton.setActionCommand(""+REMOVE_ALL);
	    removeAllButton.addActionListener(this);

	    table = new JTable(new FileTableModel());
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
	private void buildGUI()
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
	    Vector<?> v = dtm.getDataVector();
	    List<Object> indexes = new ArrayList<Object>();
	    for (int i = 0; i < table.getRowCount(); i++) {
	        if (table.isRowSelected(i))
	            indexes.add(v.get(i));
	    }
	    v.removeAll(indexes);
	    dtm.setDataVector(v, this.columnHeadings);
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
	 * Returns <code>true</code> if the file can be added to the queue again,
	 * <code>false</code> otherwise.
	 * 
	 * @param queue The list of files already in the queue.
	 * @param f The file to check.
	 * @param gID The id of the group to import the image into.
	 * @param userID The id of the user.
	 * @return See above.
	 */
	private boolean allowAddToQueue(List<FileElement> queue, FileObject f, long gID,
	        long userID)
	{
	    if (f == null) return false;
	    if (queue == null) return true;
	    Object o = f.getFile();
	    if (f.isNewImage()) {
	        return true;
	    }
	    File file = f.getTrueFile();
	    //check if file is null
	    if (file == null) return false;
	    Iterator<FileElement> i = queue.iterator();
	    FileElement fe;
	    String name = file.getAbsolutePath();
	    while (i.hasNext()) {
	        fe = i.next();
	        if (fe.getFile().getAbsolutePath().equals(name) &&
	                fe.getGroup().getId() == gID &&
	                fe.getUser().getId() == userID) {
	            o = fe.getFile().getFile();
	            if (o instanceof ImagePlus && f.getFile() instanceof ImagePlus) {
	                fe.getFile().addAssociatedFile(new FileObject(f.getFile()));
	            }
	            return false;
	        }
	    }
	    return true;
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
	    initColumns();
	    initComponents();
	    buildGUI();
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
	    if (ImporterAgent.runAsPlugin() != LookupNames.IMAGE_J_IMPORT) {
	        p.add(addButton);
	        p.add(Box.createVerticalStrut(5));
	    }
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
	    FileObject file;
	    ImportableFile importable;
	    boolean isFolderDataset;
	    DataNodeElement dne;
	    DatasetData dataset;
	    for (int i = 0; i < n; i++) {
	        element = (FileElement) dtm.getValueAt(i, this.fileIndex);
	        file = element.getFile();

	        dne = (DataNodeElement) dtm.getValueAt(i, this.containerIndex);
	        isFolderDataset = (Boolean) dtm.getValueAt(i, this.folderAsDatasetIndex);
	        importable = new ImportableFile(file, isFolderDataset);

	        dataset = dne.getLocation();

	        if (isFolderDataset) dataset = null;
	        importable.setLocation(dne.getParent(), dataset);
	        importable.setRefNode(dne.getRefNode());
	        importable.setGroup(element.getGroup());
	        importable.setUser(element.getUser());
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
	    initColumns();
	    table.setModel(new FileTableModel());
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
	 * @param settings The import settings.
	 */
	void addFiles(List<FileObject> files, ImportLocationSettings settings)
	{
	    if (CollectionUtils.isEmpty(files)) return;
	    boolean fad = settings.isParentFolderAsDataset();
	    GroupData group = settings.getImportGroup();
	    ExperimenterData user = settings.getImportUser();
	    enabledControl(true);
	    
	    DefaultTableModel dtm = (DefaultTableModel) table.getModel();
	    //Check if the file has already 
	    List<FileElement> inQueue = new ArrayList<FileElement>();
	    FileElement element;
	    for (int i = 0; i < table.getRowCount(); i++) {
	        element = (FileElement) dtm.getValueAt(i, this.fileIndex);
	        inQueue.add(element);
	    }
	    Iterator<FileObject> i = files.iterator();
	    DataNode node = settings.getImportLocation();
	    if (model.getType() != Importer.SCREEN_TYPE)
	        node.setParent(settings.getParentImportLocation());
	    String value = null;
	    boolean v;
	    long gID = group.getId();
	    FileObject f;
	    while (i.hasNext()) {
	        f = i.next();
	        if (allowAddToQueue(inQueue, f, gID, user.getId())) {
	            element = new FileElement(f, model.getType(), group, user);
	            element.setName(f.getName());
	            inQueue.add(element);
	            value = null;
	            v = false;
	            value = f.getFolderAsContainerName();
	            if (f.isDirectory()) {
	                v = fad;
	                if (model.getType() == Importer.SCREEN_TYPE) {
	                    value = null;
	                }
	            } else {
	                if (fad) {
	                    v = true;
	                    element.setToggleContainer(v);
	                }
	            }

	            final Vector<Object> row = new Vector<Object>();
	            row.setSize(this.columnHeadings.size());
	
	            if (this.fileIndex != null) {
	                row.set(this.fileIndex, element);
	            }
	            if (this.groupIndex != null) {
	                row.set(this.groupIndex, group.getName());
	            }
	            if (this.ownerIndex != null) {
	                row.set(this.ownerIndex, user.getUserName());
	            }
	            if (this.containerIndex != null) {
	                row.set(this.containerIndex, new DataNodeElement(node, value));
	            }
	            if (this.folderAsDatasetIndex != null) {
	                row.set(this.folderAsDatasetIndex, v);
	            }
	            if (this.sizeIndex != null) {
	                row.set(this.sizeIndex, element.getFileLengthAsString());
	            }
	            dtm.addRow(row);
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
	        element = (FileElement) dtm.getValueAt(i, this.fileIndex);
	        size += element.getFileLength();
	    }
	    return size;
	}

	/**
	 * Marks the folder as a dataset.
	 * 
	 * @param fad Pass <code>true</code> to mark the folder as a dataset,
	 *            <code>false</code> otherwise.
	 */
	void markFolderAsDataset(boolean fad)
	{
	    int n = table.getRowCount();
	    if (n == 0) return;
	    DefaultTableModel dtm = (DefaultTableModel) table.getModel();
	    FileElement element;

	    for (int i = 0; i < n; i++) {
	        element = (FileElement) dtm.getValueAt(i, this.fileIndex);
	        if (element.isDirectory())
	            dtm.setValueAt(fad, i, this.folderAsDatasetIndex);
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
	        element = (FileElement) model.getValueAt(i, this.fileIndex);
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
	        element = (FileElement) dtm.getValueAt(i, this.fileIndex);
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
	        firePropertyChange(ADD_PROPERTY, false, true);
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
	     */
	    FileTableModel()
	    {
	        super(null, columnHeadings);
	    }

	    /**
	     * Overridden so that some cells cannot be edited.
	     * @see DefaultTableModel#isCellEditable(int, int)
	     */
	    public boolean isCellEditable(int row, int column)
	    { 
	        return false; 
	    }

	    /**
	     * Overridden to set the name of the image to save.
	     * @see DefaultTableModel#setValueAt(Object, int, int)
	     */
	    public void setValueAt(Object value, int row, int col)
	    {   
	        if (value instanceof Boolean) {
	            if (col == folderAsDatasetIndex) {
	                DataNodeElement element = (DataNodeElement) getValueAt(row,
	                        containerIndex);
	                FileElement f = (FileElement) getValueAt(row, fileIndex);
	                if (f.isDirectory() || (!f.isDirectory() && 
	                        f.isToggleContainer())) {
	                    boolean b = (Boolean) value;
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
