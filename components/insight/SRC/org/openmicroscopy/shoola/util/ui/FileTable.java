/*
 * org.openmicroscopy.shoola.util.ui.FileTable
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.ui;


//Java imports
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.openmicroscopy.shoola.util.file.ImportErrorObject;


//Third-party libraries
import info.clearthought.layout.TableLayout;

//Application-internal dependencies

/**
 * Table displaying the file to send post to the server. 
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
class FileTable 
	extends JScrollPane
{
	
	/** The columns for the layout of the {@link #entries}. */
	private static final double[] COLUMNS = {TableLayout.FILL};
	
	/** Component hosting the entries. */
	private JPanel	entries;
	
	/** The files to send. */
	private List<FileTableNode> nodes;
	
	/**
	 * Adds a new row.
	 * 
	 * @param layout The layout.
	 * @param index	 The index of the row.
	 * @param c		 The component to add.
	 */
	private void addRow(TableLayout layout, int index, JComponent c)
	{
		layout.insertRow(index, TableLayout.PREFERRED);
		if (index%2 == 0)
			c.setBackground(UIUtilities.BACKGROUND_COLOUR_EVEN);
		else 
			c.setBackground(UIUtilities.BACKGROUND_COLOUR_ODD);
		entries.add(c, "0, "+index+"");
	}
	
	/**
	 * Builds the table.
	 * 
	 * @param files The collection of objects to submit to the development team.
	 */
	private void initialize(List<ImportErrorObject> files)
	{
		Iterator<ImportErrorObject> i = files.iterator();
		nodes = new ArrayList<FileTableNode>();
		while (i.hasNext()) {
		    nodes.add(new FileTableNode(i.next()));
		}
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		entries = new JPanel();
		entries.setBackground(UIUtilities.BACKGROUND);
		TableLayout layout = new TableLayout();
		layout.setColumn(COLUMNS);
		entries.setLayout(layout);
		Iterator<FileTableNode> i = nodes.iterator();
		int index = 0;
		while (i.hasNext()) {
			addRow(layout, index, i.next());
			index++;
		}
		setOpaque(false);
		getViewport().add(entries);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param files The collection of objects to submit to the development team.
	 */
	FileTable(List<ImportErrorObject> files)
	{
		initialize(files);
		buildGUI();
	}

	/**
	 * Returns the files to send.
	 * 
	 * @return See above.
	 */
	List<FileTableNode> getSelectedFiles() { return nodes; }
	
}
