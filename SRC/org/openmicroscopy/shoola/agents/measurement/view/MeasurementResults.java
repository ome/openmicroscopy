/*
 * org.openmicroscopy.shoola.agents.measurement.view.MeasurementResults 
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
package org.openmicroscopy.shoola.agents.measurement.view;


//Java imports
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.IconManager;
import org.openmicroscopy.shoola.agents.measurement.util.AnnotationField;
import org.openmicroscopy.shoola.agents.measurement.util.MeasurementObject;
import org.openmicroscopy.shoola.agents.measurement.util.ResultsCellRenderer;
import org.openmicroscopy.shoola.util.filter.file.CSVFilter;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKey;
import org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * UI component displaying various value computed on a Region of Interest.
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
class MeasurementResults 
	extends JPanel
{

	/** The name of the panel. */
	private static final String				NAME = "Results";
	
	/** Collection of column names. */
	private List<String>					columnNames;
	
	/** Collection of column names. */
	private List<AnnotationField>			fields;
	
	/** Collection of column names for all possible fields. */
	private List<AnnotationField>			allFields;
	
	/** Button to save locally the results. */
	private JButton							saveButton;

	/** Button to save locally the results. */
	private JButton							refreshButton;
	
	/** Button to launch the results wizard. */
	private JButton 						resultsWizardButton;
	
	/** The table displaying the results. */
	private ResultsTable					results;
	
	/** Reference to the control. */
	private MeasurementViewerControl		controller;
	
	/** Reference to the model. */
	private MeasurementViewerModel			model;
	
	/** Reference to the View. */
	private MeasurementViewerUI				view;
	
	/** 
	 * The table selection listener attached to the table displaying the 
	 * objects.
	 */
	private ListSelectionListener			listener;

	/** Initializes the components composing the display. */
	private void initComponents()
	{
		saveButton = new JButton(
				controller.getAction(MeasurementViewerControl.SAVE_RESULTS));
		resultsWizardButton = new JButton(
				controller.getAction(MeasurementViewerControl.RESULTS_WIZARD));
		
		refreshButton = new JButton(
				controller.getAction(MeasurementViewerControl.REFRESH_RESULTS));
		//Create table model.
		createAllFields();
		createDefaultFields();
		results = new ResultsTable();
		results.getTableHeader().setReorderingAllowed(false);
		MeasurementTableModel tm = new MeasurementTableModel(columnNames);
		results.setModel(tm);
		results.setSelectionMode(
				ListSelectionModel.SINGLE_SELECTION);
		results.setRowSelectionAllowed(true);
		results.setColumnSelectionAllowed(false);
		listener = new ListSelectionListener() {
			
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) return;

		        ListSelectionModel lsm =
		            (ListSelectionModel) e.getSource();
		        if (lsm.isSelectionEmpty()) {
		        } else {
		        	int index = lsm.getMinSelectionIndex();
		        	MeasurementTableModel m = 
	        			(MeasurementTableModel) results.getModel();
	        		long ROIID = (Long) m.getValueAt(index, 2);
	        		int t = (Integer) m.getValueAt(index, 0)-1;
	        		int z = (Integer) m.getValueAt(index, 1)-1;
	        		view.selectFigure(ROIID, t, z);
		        }
			}
		
		};

		results.getSelectionModel().addListSelectionListener(listener);
	}
	
	/** Builds and lays out the GUI. */
	private void buildGUI()
	{
		setLayout(new BorderLayout());
		add(new JScrollPane(results), BorderLayout.CENTER);
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());
		panel.add(resultsWizardButton);
		panel.add(refreshButton);
		panel.add(saveButton);
		this.add(panel, BorderLayout.SOUTH);
	}

	/**
	 * Create the fields which can be selected from. 
	 *
	 */
	private void createAllFields()
	{
		allFields = new ArrayList<AnnotationField>();
		allFields.add(new AnnotationField(AnnotationKeys.BASIC_TEXT,
							"Description", false)); 
		allFields.add(new AnnotationField(AnnotationKeys.CENTREX,"Centre X", 
							false)); 
		allFields.add(new AnnotationField(AnnotationKeys.CENTREY,"Centre Y", 
						false)); 
		allFields.add(new AnnotationField(AnnotationKeys.AREA,"Area", false)); 
		allFields.add(new AnnotationField(AnnotationKeys.PERIMETER,"Perimeter", 
						false)); 
		allFields.add(new AnnotationField(AnnotationKeys.LENGTH, "Length", 
						false)); 
		allFields.add(new AnnotationField(AnnotationKeys.WIDTH, "Width", 
						false)); 
		allFields.add(new AnnotationField(AnnotationKeys.HEIGHT, "Height", 
						false)); 
		allFields.add(new AnnotationField(AnnotationKeys.ANGLE, "Angle", 
						false)); 
		allFields.add(new AnnotationField(AnnotationKeys.POINTARRAYX, 
						"Points X Coord", false)); 
		allFields.add(new AnnotationField(AnnotationKeys.POINTARRAYY, 
						"Points Y Coord", false)); 
		allFields.add(new AnnotationField(AnnotationKeys.STARTPOINTX, 
						"Start Point X Coord", false)); 
		allFields.add(new AnnotationField(AnnotationKeys.STARTPOINTY, 
						"Start Point Y Coord", false)); 
		allFields.add(new AnnotationField(AnnotationKeys.ENDPOINTX,
						"End Point X Coord", false)); 
		allFields.add(new AnnotationField(AnnotationKeys.ENDPOINTY,
						"End Point Y Coord", false)); 
	}
	
	/**
	 * Creates the default fields to show results of in the measurement tool.
	 */
	private void createDefaultFields()
	{
		fields = new ArrayList<AnnotationField>();
		fields.add(new AnnotationField(AnnotationKeys.BASIC_TEXT,"Description", 
										false)); 
		fields.add(new AnnotationField(AnnotationKeys.CENTREX,"Centre X", 
										false)); 
		fields.add(new AnnotationField(AnnotationKeys.CENTREY,"Centre Y", 
										false)); 
		fields.add(new AnnotationField(AnnotationKeys.AREA,"Area", false)); 
		fields.add(new AnnotationField(AnnotationKeys.PERIMETER,"Perimeter", 
										false)); 
		fields.add(new AnnotationField(AnnotationKeys.LENGTH, "Length", false)); 
		fields.add(new AnnotationField(AnnotationKeys.ANGLE, "Angle", false)); 
		columnNames = new ArrayList<String>();
		columnNames.add("Time Point");
		columnNames.add("Z Section");
		columnNames.add("ROI ID");
		columnNames.add("Figure Type");
		for (int i = 0 ; i < fields.size(); i++)
			columnNames.add(fields.get(i).getName());
	}
	
	/**
	 * Writes the contain of the columns into the passed buffer.
	 * 
	 * @param out The buffer to write data into.
	 * @throws IOException Thrown if the data cannot be written.
	 */
	private void writeColumns(BufferedWriter out) 
		throws IOException
	{
		int n = results.getColumnCount()-1;
		for (int i = 0 ; i < n+1 ; i++) {
			out.write(results.getColumnName(i));
			if (i < n) out.write(",");
		}
		out.newLine();
	}
	
	/**
	 * Writes the data to the passed buffer.
	 * 
	 * @param out	The buffer to write data into.
	 * @throws IOException Thrown if the data cannot be written.
	 */
	private void writeData(BufferedWriter out) 
		throws IOException
	{
		MeasurementTableModel tm = (MeasurementTableModel) results.getModel();
		for (int i = 0 ; i < results.getRowCount() ; i++)
			writeRow(out, tm.getRow(i));
	}
	
	/**
	 * Writes the contain for the passed row to the passed buffer.
	 * 
	 * @param out	The buffer to write data into.
	 * @param row	The row to get data from.
	 * @throws IOException Thrown if the data cannot be written.
	 */
	private void writeRow(BufferedWriter out, MeasurementObject row) 
		throws IOException
	{
		int height = 1;
		int width = row.getSize();
		Object element;
		ArrayList list;
		for (int i = 0 ; i < row.getSize(); i++)
		{
			element = row.getElement(i);
			if (element instanceof ArrayList) {
				list = (ArrayList) element;
				if (list.size() > height) height = list.size();
			}
		}
		for (int j = 0 ; j < height ; j++)
		{
			for (int i = 0 ; i < width ; i++)
			{
				out.write(writeElement(row.getElement(i), j));
				if (i < width-1) out.write(",");
			}
			out.newLine();
		}
	}
	
	/**
	 * Converts the passed element into a String depending on the specified
	 * index.
	 * 
	 * @param element 	The element to convert.
	 * @param j			The index.
	 * @return See above.
	 */
	private String writeElement(Object element, int j)
	{
		if (element instanceof Double || element instanceof Integer ||
				element instanceof Float || element instanceof String ||
				element instanceof Boolean || element instanceof Long)
			if (j == 0) return convertElement(element);
			else return "";
		else if (element instanceof ArrayList)
		{
			ArrayList list = (ArrayList)element;
			if (j < list.size()) return convertElement(list.get(j));
			else return "";
		}
		return "";
	}
	
	/**
	 * Stringifies the passed object.
	 * 
	 * @param element The object to stringify.
	 * @return See above.
	 */
	private String convertElement(Object element)
	{
		if (element instanceof Double) 
			return ((Double) element).doubleValue()+"";
		if (element instanceof Boolean) return ((Boolean) element).toString();
		if (element instanceof Long) 
			return ((Long) element).longValue()+"";
		if (element instanceof Integer)
			return ((Integer) element).intValue()+"";
		if (element instanceof Float)
			return ((Float) element).floatValue()+"";
		if (element instanceof String)
			return (String) element;
		return "";
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param controller Reference to the Control. Mustn't be <code>null</code>.
	 * @param model		 Reference to the Model. Mustn't be <code>null</code>.
	 * @param view		 Reference to the View. Mustn't be <code>null</code>.
	 */
	MeasurementResults(MeasurementViewerControl	controller, 
					MeasurementViewerModel model, MeasurementViewerUI view)
	{
		if (controller == null)
			throw new IllegalArgumentException("No control.");
		if (model == null)
			throw new IllegalArgumentException("No model.");
		if (view == null)
			throw new IllegalArgumentException("No view.");
		this.controller = controller;
		this.model = model;
		this.view = view;
		initComponents();
		buildGUI();
	}
	
	/** Populate the table. */
	void populate()
	{
		TreeMap map = model.getROI();
		Iterator i = map.keySet().iterator();
		ROI roi;
		TreeMap shapes;
		Iterator j;
		ROIShape shape;
		ROIFigure figure;
		MeasurementObject row;
		AnnotationKey key;
		MeasurementTableModel tm = new MeasurementTableModel(columnNames);
		while (i.hasNext()) {
			roi = (ROI) map.get(i.next());
			shapes = roi.getShapes();
			j = shapes.keySet().iterator();
			while (j.hasNext()) {
				shape = (ROIShape) shapes.get(j.next());
				figure = shape.getFigure();
				figure.calculateMeasurements();
				row = new MeasurementObject();
				row.addElement(shape.getCoord3D().getTimePoint()+1);
				row.addElement(shape.getCoord3D().getZSection()+1);
				row.addElement(shape.getROI().getID());
				row.addElement(shape.getFigure().getType());
				for (int k = 0; k < fields.size(); k++) {
					key = fields.get(k).getKey();
					Object value = key.get(shape);
					if (value instanceof ArrayList)
					{
						ArrayList valueArray = (ArrayList) value;
						ArrayList arrayList = new ArrayList(valueArray);
						row.addElement(arrayList);
					}
					else
						row.addElement(value);
				}
				tm.addRow(row);
			}
		}
		results.setModel(tm);
	}

	/**
	 * Returns the name of the component.
	 * 
	 * @return See above.
	 */
	String getComponentName() { return NAME; }
	
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
	 * Save the results.
	 * 
	 * @throws IOException Thrown if the data cannot be written.
	 * @return true if results saved, false if users cancels save.
	 */
	boolean saveResults()
		throws IOException
	{
		JFileChooser chooser = new JFileChooser();
		FileFilter filter = new CSVFilter();
		chooser.addChoosableFileFilter(filter);
		chooser.setFileFilter(filter);

		File f = UIUtilities.getDefaultFolder();
	    if(f != null) chooser.setCurrentDirectory(f);
		int results = chooser.showSaveDialog(this.getParent());
		if(results != JFileChooser.APPROVE_OPTION) return false;
		File file = chooser.getSelectedFile();
		if (!file.getAbsolutePath().endsWith(CSVFilter.CSV))
		{
			String fileName = file.getAbsolutePath()+"."+CSVFilter.CSV;
			file = new File(fileName);
		}
		if (file.exists()) 
		{
			int response = JOptionPane.showConfirmDialog (null,
						"Overwrite existing file?","Confirm Overwrite",
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE);
	        if (response == JOptionPane.CANCEL_OPTION) return false;
	    }
		BufferedWriter out = new BufferedWriter(new FileWriter(file));
		writeColumns(out);
		writeData(out);
		out.close();
		return true;
	}

	/** Refreshes the result table. */
	void refreshResults() { populate(); }
	
	/**
	 * Shows the results wizard and updates the fields based on the users 
	 * selection.
	 */
	void showResultsWizard()
	{
		ResultsWizard resultsWizard = new ResultsWizard(fields, allFields);
		resultsWizard.pack();
		UIUtilities.setLocationRelativeToAndShow(this, resultsWizard);
		columnNames.clear();
		columnNames = new ArrayList<String>();
		columnNames.add("Time Point");
		columnNames.add("Z Section");
		columnNames.add("ROI ID");
		columnNames.add("Figure Type");
		for (int i = 0 ; i < fields.size(); i++)
			columnNames.add(fields.get(i).getName());
		populate();
		results.repaint();
	}
	
	/** Basic inner class use to set the cell renderer. */
	class ResultsTable
		extends JTable
	{
		
		/** Creates a new instance. */
		ResultsTable()
		{
			super();
		}
		
		/**
		 * Overridden to return a customized cell renderer.
		 * @see JTable#getCellRenderer(int, int)
		 */
		public TableCellRenderer getCellRenderer(int row, int column) 
		{
	        return new ResultsCellRenderer();
	    }
	}
	
	/** Inner class used to display the results of measurement. */
	class MeasurementTableModel 
		extends AbstractTableModel
	{
	
		/** The collection of column's names. */
		private List<String>			columnNames;
		
		/** Collection of <code>Object</code>s hosted by this model. */
		private List<MeasurementObject>	values;
		
		/**
		 * Creates a new instance.
		 * 
		 * @param columnNames	The collection of column's names.
		 * 						Mustn't be <code>null</code>.
		 */
		MeasurementTableModel(List<String> columnNames)
		{
			if (columnNames == null)
				throw new IllegalArgumentException("No column's names " +
													"specified.");
			this.columnNames = columnNames;
			this.values = new ArrayList<MeasurementObject>();
		}
		
		/** 
		 * Adds a new row to the model.
		 * 
		 * @param row The value to add.
		 */
		void addRow(MeasurementObject row)
		{
			values.add(row);
			fireTableStructureChanged();
		}
		
		/** 
		 * Get a row from the model.
		 * 
		 * @param index The row to return
		 * 
		 * @return MeasurementObject the row.
		 */
		MeasurementObject getRow(int index)
		{
			if(index < values.size())
				return values.get(index);
			return null;
		}
		
		/**
		 * Returns the value of the specified cell.
		 * @see AbstractTableModel#getValueAt(int, int)
		 */
		public Object getValueAt(int row, int col) 
	    {
			if (row < 0 || row > values.size()) return null;
			MeasurementObject rowData = values.get(row);
	    	return rowData.getElement(col);
		}
	    
		/**
		 * Sets the specified value.
		 * @see AbstractTableModel#setValueAt(Object, int, int)
		 */
	    public void setValueAt(Object value, int row, int col) 
	    {

	    }
	    
		/**
		 * Overridden to return the name of the specified column.
		 * @see AbstractTableModel#getColumnName(int)
		 */
		public String getColumnName(int col) { return columnNames.get(col); }
	    
	    /**
		 * Overridden to return the number of columns.
		 * @see AbstractTableModel#getColumnCount()
		 */
		public int getColumnCount() { return columnNames.size();  }
	
		/**
		 * Overridden to return the number of rows.
		 * @see AbstractTableModel#getRowCount()
		 */
		public int getRowCount() { return values.size(); }
		
		/**
		 * Overridden so that the cell is not editable.
		 * @see AbstractTableModel#isCellEditable(int, int)
		 */
		public boolean isCellEditable(int row, int col) 
		{ 
			return false;
		}
		
	}

}
