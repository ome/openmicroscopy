/*
 * org.openmicroscopy.shoola.agents.metadata.editor.AttachmentsTable 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.metadata.editor;

//Java imports
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.List;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

//Third-party libraries
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.table.ColumnHeaderRenderer;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.FileAnnotationData;

/** 
 * Lays out the files attached to the data object in a table.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class AttachmentsTable
	extends JXTable
{

	/** The columns of the table. */
	private static final String[]	COLUMNS;

	/** Identified the column displaying the file's path. */
	private static final int  		NAME_COL = 0;
	
	/** Identified the column displaying the added date. */
	private static final int  		DATE_COL = 1;
	
	/** Identified the column displaying the size of the file. */
	private static final int  		SIZE_COL = 2;
	
	/** Identified the column displaying the type of file. */
	private static final int  		KIND_COL = 3;
	
	/** Identified the column displaying the owner of file. */
	private static final int  		OWNER_COL = 4;
	
	/** The texf of the {@link #NAME_COL}. */
	static final String				NAME =  "Name";
	
	/** The texf of the {@link #DATE_COL}. */
	static final String				DATE =  "Date Added";
	
	/** The texf of the {@link #SIZE_COL}. */
	static final String				SIZE =  "Size";
	
	/** The texf of the {@link #KIND_COL}. */
	static final String				KIND =  "Kind";
	
	/** The texf of the {@link #DATE_COL}. */
	static final String				OWNER =  "Owner";
	
	static {
		COLUMNS = new String[4];
		COLUMNS[NAME_COL] = NAME;
		COLUMNS[DATE_COL] = DATE;
		COLUMNS[SIZE_COL] = SIZE;
		COLUMNS[KIND_COL] = KIND;
	}
	
	/** The annotation to lay out. */
	private FileAnnotationData[] 	data;
	
	/** Reference to the view. */
	private AttachmentsUI			uiDelegate;
	
	/** The model's listener. */
	private ListSelectionListener	listener;
	
	/**
	 * Selects the annotation and brings up the management menu at
	 * the specified location.
	 * 
	 * @param x	The x-coordinate of the mouse pressed.
	 * @param y	The y-coordinate of the mouse pressed.
	 */
	private void showMenu(int x, int y)
	{
		int row = getSelectedRow();
		if (row >= 0 && row < data.length) {
			FileAnnotationData annotation = data[row];
			uiDelegate.manageAnnotation(annotation, this, x, y);
		}
	}
	
	/** Sets the properties of the table. */
	private void setProperties()
	{
		ColumnHeaderRenderer l = 
			(ColumnHeaderRenderer) getTableHeader().getDefaultRenderer();
		l.setHorizontalAlignment(SwingConstants.CENTER);
		Highlighter[] highlighters = new Highlighter[1];
		highlighters[0] = HighlighterFactory.createAlternateStriping();
		setHighlighters(highlighters);
		setModel(new AttachmentTableModel());
		getTableHeader().setReorderingAllowed(false);
		
		addMouseListener(new MouseAdapter() {
			
			/**
			 * Brings up the management menu.
			 */
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger())
					showMenu(e.getX(), e.getY());
			}
		
			/**
			 * Brings up the management menu.
			 */
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger())
					showMenu(e.getX(), e.getY());
			}
		
		});
		listener = new ListSelectionListener() {
		
			public void valueChanged(ListSelectionEvent e) {
				if (e.getSource() == getSelectionModel()
	              && getRowSelectionAllowed() && e.getValueIsAdjusting()) {
					int row = getSelectedRow();
					if (row != -1) {
						FileAnnotationData f = data[row];
						if (f != null)
							uiDelegate.setSelectedFileAnnotation(f);
					}
	            }
			}
		
		};
		getSelectionModel().addListSelectionListener(listener);
	}
	
	/** Sets the selected node. */
	private void setSelected()
	{
		getSelectionModel().removeListSelectionListener(listener);
		long id = -1;
		FileAnnotationData selected = uiDelegate.getSelectedAnnotation();
		if (selected != null) id = selected.getId();
		int row = -1;
		for (int i = 0; i < data.length; i++) {
			if (data[i].getId() == id) {
				row = i;
				break;
			}
		}
		if (row != -1) getSelectionModel().setSelectionInterval(row, row);
		getSelectionModel().addListSelectionListener(listener);
		
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param uiDelegate	Reference to the UI delegate. 
	 * 						Mustn't be <code>null</code>.
	 * @param annotations 	The collection of annotations.
	 */
	AttachmentsTable(AttachmentsUI uiDelegate, 
					List<FileAnnotationData> annotations)
	{
		if (uiDelegate == null)
			throw new IllegalArgumentException("No view.");
		this.uiDelegate = uiDelegate;
		data = new FileAnnotationData[annotations.size()];
		Iterator<FileAnnotationData> i = annotations.iterator();
		int index = 0;
		while (i.hasNext()) {
			data[index] = i.next();
			index++;
		}
		setProperties();
		setSelected();
	}
	
	/** Helper class used to format the {@link FileAnnotation}s. */
	class AttachmentTableModel 
		extends AbstractTableModel
	{

		/** 
		 * Returns the number of columns.
		 * @see AbstractTableModel#getColumnCount()
		 */
		public int getColumnCount() { return COLUMNS.length; }

		/** 
		 * Returns the number of rows.
		 * @see AbstractTableModel#getRowCount()
		 */
		public int getRowCount() { return data.length; }

		/**
		 * Overridden to return the name of the column.
		 * @see AbstractTableModel#getColumnName(int)
		 */
		public String getColumnName(int column) { return COLUMNS[column]; }
		
		/** 
		 * Returns the value at the specified cell.
		 * @see AbstractTableModel#getValueAt(int, int)
		 */
		public Object getValueAt(int rowIndex, int columnIndex)
		{
			FileAnnotationData f = data[rowIndex];
			if (f == null) return "";
			switch (columnIndex) {
				case NAME_COL:
					return f.getFileName();
				case DATE_COL:
					return UIUtilities.formatWDMYDate(f.getLastModified());
				case SIZE_COL:
					return UIUtilities.formatFileSize(f.getFileSize());
				case KIND_COL:
					return f.getFileKind();
			}
			return null;
		}
	}
	
}
