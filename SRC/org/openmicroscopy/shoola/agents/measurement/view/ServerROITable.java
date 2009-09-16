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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIMap;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.drawingtools.canvas.DrawingCanvasView;

import pojos.FileAnnotationData;

/**
 * Displays the measurement related to the ROIs.
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
	implements ListSelectionListener
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
		if (tr == null) return;
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
		
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		table.setRowSelectionAllowed(true);
		table.getSelectionModel().addListSelectionListener(this);
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		setLayout(new BorderLayout(0, 0));
		if (table != null) add(new JScrollPane(table), BorderLayout.CENTER);
	}
	
	/** 
	 * Shows or hides the ROI depending the passed value.
	 * 
	 * @param row   The row in the table.
	 * @param value <code>true</code> to show the ROI, 
	 * 				<code>false</code> to hide.
	 */
	private void handleVisibility(int row, boolean value)
	{
		DefaultTableModel dtm = (DefaultTableModel) table.getModel();
		Iterator i = rowIDs.entrySet().iterator();
		Entry entry;
		long id = -1;
		while (i.hasNext()) {
			entry = (Entry) i.next();
			if (((Integer) entry.getValue()) == row)
				id = (Long) entry.getKey();
		}
		if (id != -1) {
			try {
				TreeMap<Coord3D, ROIShape> shapes;
				Iterator<ROIShape> j;
				ROIShape shape;
				ROI roi = model.getROI(id);
				shapes = roi.getShapes();
				j = shapes.values().iterator();
				while (j.hasNext()) {
					shape = j.next();
					shape.getFigure().setVisible(value);
				}
			} catch (Exception e) {}
		}
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
	 * Selects the row corresponding to the passed ROI's id.
	 * 
	 * @param roiIDs The collection of ROI's ids.
	 */
	void selectROI(List<Long> roiIDs)
	{
		/*
		if (roiIDs == null || roiIDs.size() == 0) return;
		Iterator<Long> i = roiIDs.iterator();
		int index;
		Long id;
		table.getSelectionModel().removeListSelectionListener(this);
		while (i.hasNext()) {
			id = i.next();
			if (rowIDs.containsKey(id)) {
				index = rowIDs.get(id);
				table.setRowSelectionInterval(index, index);
			}
		}
		table.getSelectionModel().addListSelectionListener(this);
		*/
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
		if (fa == null) return "";
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
	
	/**
	 * Listens to selection in table. Selects the ROIs in the display.
	 * @see ListSelectionListener#valueChanged(ListSelectionEvent)
	 */
	public void valueChanged(ListSelectionEvent e)
	{
		if (e.getValueIsAdjusting()) return;
		ListSelectionModel lsm = (ListSelectionModel) e.getSource();
		List<Integer> indexes = new ArrayList<Integer>();
		if (!lsm.isSelectionEmpty()) {
			int minIndex = lsm.getMinSelectionIndex();
            int maxIndex = lsm.getMaxSelectionIndex();
            for (int i = minIndex; i <= maxIndex; i++) {
                if (lsm.isSelectedIndex(i)) {
                    indexes.add(i);
                }
            }
		}
		Entry entry;
		ROI roi;
		TreeMap<Coord3D, ROIShape> shapes;
		Iterator<ROIShape> j;
		ROIShape shape;
		Iterator i = rowIDs.entrySet().iterator();
		try {
			List<ROIFigure> list = new ArrayList<ROIFigure>();
			while (i.hasNext()) {
				entry = (Entry) i.next();
				if (indexes.contains(entry.getValue())) {
					roi = model.getROI((Long) entry.getKey());
					shapes = roi.getShapes();
					j = shapes.values().iterator();
					while (j.hasNext()) {
						shape = j.next();
						list.add(shape.getFigure());
					}
				}
			}
			DrawingCanvasView dv = model.getDrawingView();
	    	dv.clearSelection();
	    	Iterator<ROIFigure> k = list.iterator();
	    	while (k.hasNext()) {
	    		dv.addToSelection(k.next());
			}
			dv.grabFocus();
	    	
		} catch (Exception ex) {
			// TODO: handle exception
		}
	}
	
	/**
	 * Returns the ID of the file this component is hosting.
	 * 
	 * @return See above.
	 */
	long getFileID()
	{
		if (result == null) return -1;
		return result.getFileID();
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
		
		/**
		 * Overridden to set the name of the image to save.
		 * @see DefaultTableModel#setValueAt(Object, int, int)
		 */
		public void setValueAt(Object value, int row, int col)
		{   
			super.setValueAt(value, row, col);
			if (col == VISIBILITY_INDEX) handleVisibility(row, (Boolean) value);	
			fireTableCellUpdated(row, col);
		}
		
	}
	
}
