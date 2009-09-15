/*
 * org.openmicroscopy.shoola.agents.measurement.view.ServerROITable
 *
 *------------------------------------------------------------------------------
 * Copyright (C) 2006-2009 University of Dundee. All rights reserved.
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.measurement.view;


//Java imports
import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;


//Third-party libraries
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;


//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.IconManager;
import org.openmicroscopy.shoola.env.data.model.ROIResult;
import org.openmicroscopy.shoola.env.data.model.TableResult;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.FileAnnotationData;

/**
 *
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class ServerROITable 
	extends JPanel
{

	/** The index of the visible flag. */
	private static final int			VISIBILITY_INDEX = 0;
	
	/** Reference to the Model. */
	private MeasurementViewerModel		model;
	
	/** Reference to the View. */
	private MeasurementViewerUI 		view;
	
	/** The ROI result. */
	private ROIResult 					result;
	
	/** The table displaying the collection to files to import. */
	private JXTable						table;
	
	/* Map whose key is the id of the ROI and value its row. */
	private Map<Long, Integer>			rowIDs;
	
	/** Initializes the components. */
	private void initialize()
	{
		TableResult tr = (TableResult) result.getResult();
		String[] headers = tr.getHeaders();
		Object[][] data = tr.getData();
		Object[][] rows = new Object[data.length][headers.length];
		String[] columns = new String[headers.length];
		for (int i = 0; i < headers.length; i++) {
			if (i == 0) columns[i] = "Visible";
			else columns[i] = headers[i];
		}
		for (int i = 0; i < columns.length; i++) {
			for (int j = 0; j < data.length; j++) {
				if (i == 0) rows[j][i] = Boolean.valueOf(true);
				else rows[j][i] = data[j][i];
				if (i == 1) 
					rowIDs.put((Long) rows[j][i], j);
			}
		}
		table = new JXTable(new ServerROITableModel(rows, columns));
		TableColumn tc = table.getColumnModel().getColumn(VISIBILITY_INDEX);
		tc.setCellEditor(table.getDefaultEditor(Boolean.class));  
		tc.setCellRenderer(table.getDefaultRenderer(Boolean.class));  
		Highlighter h = HighlighterFactory.createAlternateStriping(
				UIUtilities.BACKGROUND_COLOUR_EVEN, 
				UIUtilities.BACKGROUND_COLOUR_ODD);
		table.addHighlighter(h);
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		setLayout(new BorderLayout(0, 0));
		add(new JScrollPane(table), BorderLayout.CENTER);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param view 	Reference to the control. Mustn't be <code>null</code>.
	 * @param model	Reference to the Model. Mustn't be <code>null</code>.
	 */
	ServerROITable(MeasurementViewerUI view, MeasurementViewerModel model)
	{
		if (view == null) throw new IllegalArgumentException("No view.");
		if (model == null) throw new IllegalArgumentException("No model.");
		this.view = view;
		this.model = model;
		rowIDs = new HashMap<Long, Integer>();
	}

	/**
	 * Sets the result.
	 * 
	 * @param result The value to set.
	 */
	void setResult(ROIResult result)
	{
		if (result == null)
			throw new IllegalArgumentException("No result to display");
		this.result = result;
		initialize();
		buildGUI();
	}
	
	/**
	 * Returns the name associated to the component.
	 * 
	 * @return See above.
	 */
	String getComponentName()
	{
		if (result == null) return "";
		FileAnnotationData fa = model.getMeasurement(result.getFileID());
		return fa.getDescription();
	}
	
	/**
	 * Returns the icon of the component.
	 * 
	 * @return See above.
	 */
	Icon getComponentIcon()
	{
		IconManager icons = IconManager.getInstance();
		return icons.getIcon(IconManager.RESULTS);
	}
	
	/** Inner class so that some cells cannot be edited. */
	class ServerROITableModel 
		extends DefaultTableModel
	{
		
		/**
		 * Creates a new instance.
		 * 
		 * @param rows		The rows to display.
		 * @param columns	The columns to display.
		 */
		ServerROITableModel(Object[][] rows, String[] columns)
		{
			super(rows, columns);
		}
		
		/**
		 * Overridden so that some cells cannot be edited.
		 * @see DefaultTableModel#isCellEditable(int, int)
		 */
		public boolean isCellEditable(int row, int column)
		{ 
			return (column == VISIBILITY_INDEX);
		}
	}
	
}
