/*
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.measurement.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.openmicroscopy.shoola.agents.measurement.IconManager;
import org.openmicroscopy.shoola.agents.measurement.MeasurementAgent;
import omero.gateway.model.ROIResult;
import omero.gateway.model.TableResult;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.file.ExcelWriter;
import org.openmicroscopy.shoola.util.filter.file.ExcelFilter;
import org.openmicroscopy.shoola.util.image.geom.Factory;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.annotation.MeasurementAttributes;
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.filechooser.FileChooser;
import org.openmicroscopy.shoola.util.ui.treetable.renderers.StringCellRenderer;

import omero.gateway.model.FileAnnotationData;

/**
 * Displays the measurement related to the ROIs.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
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
	private JTable						table;
	
	/** Map whose key is the id of the ROI and value its row. */
	private Map<Long, Integer>			rowIDs;
	
	/** Button to export the data to excel. */
	private JButton						export;
	
	/**
	 * Rounds the specified value if it is a double or float.
	 * 
	 * @param value The value to handle.
	 * @return See above.
	 */
	private Object roundValue(Object value)
	{
		if (value instanceof Double) {
			return UIUtilities.roundTwoDecimals((Double) value);
		} else if (value instanceof Float) {
			return UIUtilities.roundTwoDecimals((Float) value);
		}
		return value;
	}
	
	/** Initializes the components. */
	private void initialize()
	{
		TableResult tr = (TableResult) result.getResult();
		if (tr == null) return;
		String[] headers = tr.getHeaders();
		Object[][] data = tr.getData();
		Object[][] rows = new Object[data.length][headers.length];
		String[] columns = new String[headers.length];
		int roiIndex = tr.getColumnIndex(TableResult.ROI_COLUMN_INDEX);
		if (roiIndex < 0) roiIndex = 0;
		for (int i = 0; i < headers.length; i++) {
			if (i == roiIndex) columns[i] = "Visible";
			else columns[i] = headers[i];
		}
		
		for (int i = 0; i < columns.length; i++) {
			for (int j = 0; j < data.length; j++) {
				if (i == roiIndex) {
					rowIDs.put((Long) data[j][i], j);
					rows[j][i] = Boolean.valueOf(true);
				} else rows[j][i] = roundValue(data[j][i]);	
			}
		}
		table = new JTable(new ServerROITableModel(rows, columns));
		TableColumnModel tcm = table.getColumnModel();
		
		TableColumn tc = tcm.getColumn(VISIBILITY_INDEX);
		tc.setCellEditor(table.getDefaultEditor(Boolean.class));  
		tc.setCellRenderer(table.getDefaultRenderer(Boolean.class));  
		
		table.setShowGrid(true);
		table.setGridColor(Color.LIGHT_GRAY);
		table.setSelectionBackground(UIUtilities.SELECTED_BACKGROUND_COLOUR);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		table.setRowSelectionAllowed(true);
		table.getSelectionModel().addListSelectionListener(this);
		TableCellRenderer renderer = new StringCellRenderer();
		for (int i = 0; i < table.getColumnCount(); i++) {
			tcm.getColumn(i).setHeaderRenderer(renderer);
		}
		export = new JButton("Save To Excel");
		export.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				saveTable();
			}
		});
	}

	/** Saves the table. */
	private void saveTable()
	{
		List<FileFilter> filterList = new ArrayList<FileFilter>();
		FileFilter filter = new ExcelFilter();
		filterList.add(filter);
		FileChooser chooser=
				new FileChooser(
					view, FileChooser.SAVE, "Save the Results",
					"Save the Results data to a file which can be loaded " +
					"by a spreadsheet.",
					filterList);
		try {
			File f = UIUtilities.getDefaultFolder();
			if (f != null) chooser.setCurrentDirectory(f);
		} catch (Exception ex) {}
		int choice = chooser.showDialog();
		if (choice != JFileChooser.APPROVE_OPTION) return;
		File file = chooser.getSelectedFile();
		if (!file.getAbsolutePath().endsWith(ExcelFilter.EXCEL))
		{
			String fileName = file.getAbsolutePath()+"."+ExcelFilter.EXCEL;
			file = new File(fileName);
		}
		String filename = file.getAbsolutePath();
		ExcelWriter writer = new ExcelWriter(filename);
		try {
			writer.openFile();
			writer.createSheet("Measurement");
			writer.writeTableToSheet(0, 0, table.getModel());
			try {
				BufferedImage originalImage = model.getRenderedImage();
				BufferedImage image =  Factory.copyBufferedImage(originalImage);
				model.setAttributes(MeasurementAttributes.SHOWID, true);
				model.getDrawingView().print(image.getGraphics());
				model.setAttributes(MeasurementAttributes.SHOWID, false);
				String imageName = "ROIImage";
				writer.addImageToWorkbook(imageName, image); 
				int col = writer.getMaxColumn(0);
				writer.writeImage(0, col+1, 256, 256,	imageName);
			} catch (Exception e) {
				//opengGL
			}
			
			writer.close();
		} catch (Exception e) {
			UserNotifier un = MeasurementAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Save Measurements", 
					"Unable to save the measurements");
		}
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		setLayout(new BorderLayout(0, 0));
		if (table != null) {
			add(new JScrollPane(table), BorderLayout.CENTER);
			add(UIUtilities.buildComponentPanelRight(export), 
					BorderLayout.SOUTH);
		}
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
	 * Makes the table scroll to the passed row.
	 * 
	 * @param row The row to handle.
	 */
	private void scrollToRow(int row)
	{
		Rectangle r = table.getCellRect(row, 0, true);
		table.scrollRectToVisible(r);
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
	 * @param roiIDs The collection of ROI's identifiers.
	 */
	void selectROI(List<Long> roiIDs)
	{
		if (roiIDs == null || roiIDs.size() == 0) return;
		if (table == null) return;
		Iterator<Long> i = roiIDs.iterator();
		int index;
		Long id;
		table.getSelectionModel().removeListSelectionListener(this);
		int[] array = table.getSelectedRows();
		List<Integer> l = new ArrayList<Integer>();
		if (array != null) {
			for (int j = 0; j < array.length; j++) 
				l.add(array[j]);
		}
		if (roiIDs.size() <= l.size()) table.clearSelection();
		while (i.hasNext()) {
			id = i.next();
			if (rowIDs.containsKey(id)) {
				index = rowIDs.get(id);
				if (!l.contains(index)) {
					table.addRowSelectionInterval(index, index);
					scrollToRow(index);
				}
			}
		}
		table.repaint();
		table.getSelectionModel().addListSelectionListener(this);
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
		//if (e.getValueIsAdjusting()) return;
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
			view.setTableSelectedFigure(list);
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
