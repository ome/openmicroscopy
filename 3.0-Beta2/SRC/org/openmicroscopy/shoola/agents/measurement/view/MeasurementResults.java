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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;



//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.IconManager;
import org.openmicroscopy.shoola.agents.measurement.actions.RefreshResultsTableAction;
import org.openmicroscopy.shoola.agents.measurement.actions.SaveResultsAction;
import org.openmicroscopy.shoola.agents.measurement.util.AnnotationField;
import org.openmicroscopy.shoola.agents.measurement.util.MeasurementObject;
import org.openmicroscopy.shoola.agents.measurement.util.ResultsCellRenderer;
import org.openmicroscopy.shoola.agents.measurement.view.ObjectManager.ROIFigureTableModel;
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
	private static final String			NAME = "Results";
	
	/** Collection of column names. */
	private List<String>				columnNames;
	
	/** Collection of column names. */
	private List<AnnotationField>	fields;
	
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
	private MeasurementViewerUI			view;
	
	/** 
	 * The table selection listener attached to the table displaying the 
	 * objects.
	 */
	private ListSelectionListener		listener;
	

	/** Initializes the components composing the display. */
	private void initComponents()
	{
		saveButton = new JButton("Save");
		saveButton.addActionListener(new SaveResultsAction(model.getMeasurementComponent()));
		
		resultsWizardButton = new JButton("Results Wizard..");
		resultsWizardButton.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent arg0) 
			{
				showResultsWizard();
			}
			
		});
		
		
		refreshButton = new JButton("Refresh Results");
		refreshButton.addActionListener(new RefreshResultsTableAction(model.getMeasurementComponent()));
		
		
		//Create table model.
		createDefaultFields();
		results = new ResultsTable();
		results.getTableHeader().setReorderingAllowed(false);
		MeasurementTableModel tm = new MeasurementTableModel(columnNames);
		results.setModel(tm);
		results.setSelectionMode(
				ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		results.setRowSelectionAllowed(true);

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
	        		long ROIID = (Long)m.getValueAt(index, 2);
	        		int T = (Integer)m.getValueAt(index, 0);
	        		int Z = (Integer)m.getValueAt(index, 1);
	        		view.selectFigure(ROIID, T, Z);
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
	 * Create the default fields to show results of in the measurement tool.
	 *
	 */
	private void createDefaultFields()
	{
		fields = new ArrayList<AnnotationField>();
		fields.add(new AnnotationField(AnnotationKeys.FIGURETYPE,"Figure Type", 
										false)); 
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
		for (int i = 0 ; i < fields.size(); i++)
			columnNames.add(fields.get(i).getName());
	}
	
	/**
	 * Show the results wizard and update the fields based on the users 
	 * selection.
	 */
	private void showResultsWizard()
	{
		ResultsWizard resultsWizard = new ResultsWizard(fields);
		UIUtilities.setLocationRelativeToAndShow(this, resultsWizard);
		columnNames.clear();
		columnNames = new ArrayList<String>();
		columnNames.add("Time Point");
		columnNames.add("Z Section");
		columnNames.add("ROI ID");
		for (int i = 0 ; i < fields.size(); i++)
			columnNames.add(fields.get(i).getName());
		populate();
		results.repaint();
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param controller Reference to the Control. Mustn't be <code>null</code>.
	 * @param model		 Reference to the Model. Mustn't be <code>null</code>.
	 */
	MeasurementResults(MeasurementViewerControl	controller, 
					MeasurementViewerModel model, MeasurementViewerUI view)
	{
		if (controller == null)
			throw new IllegalArgumentException("No control.");
		if (model == null)
			throw new IllegalArgumentException("No model.");
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
				row.addElement(shape.getCoord3D().getTimePoint());
				row.addElement(shape.getCoord3D().getZSection());
				row.addElement(shape.getROI().getID());
				for (int k = 0; k < fields.size(); k++) {
					key = fields.get(k).getKey();
					row.addElement(key.get(shape));
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
	
	public void saveResults()
	{
		JFileChooser chooser;

		chooser = new JFileChooser();
		int results = chooser.showSaveDialog(this.getParent());
		if(results!=JFileChooser.APPROVE_OPTION)
			return;
		File file = chooser.getSelectedFile();

		try
		{
			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			writeColumns(out);
			writeData(out);
			out.close();
		}
		catch(IOException e)
		{

		}
	}

	private void writeColumns(BufferedWriter out) throws IOException
	{
		for(int i = 0 ; i < results.getColumnCount() ; i++)
		{
			out.write(results.getColumnName(i));
			if(i<results.getColumnCount()-1)
				out.write(",");
		}
		out.newLine();
	}
	
	private void writeData(BufferedWriter out) throws IOException
	{
		MeasurementTableModel model = (MeasurementTableModel) results.getModel();
		for(int i = 0 ; i < results.getRowCount() ; i++)
		{
			MeasurementObject row = model.getRow(i);
			writeRow(out, row);
		}
	}
	
	private void writeRow(BufferedWriter out, MeasurementObject row) throws IOException
	{
		int height=1;
		int width = row.getSize();
		for(int i = 0 ; i < row.getSize(); i++)
		{
			Object element = row.getElement(i);
			if(element instanceof ArrayList)
			{
				ArrayList list = (ArrayList)element;
				if(list.size()>height)
					height = list.size();
			}
		}
		for(int j = 0 ; j < height ; j++)
		{
			for(int i = 0 ; i < width ; i++)
			{
				Object element = row.getElement(i);
				out.write(writeElement(element, j));
				if(i<width-1)
					out.write(",");
			}
			out.newLine();
		}
	}
	
	private String writeElement(Object element, int j)
	{
		if(element instanceof Double || element instanceof Integer ||
				element instanceof Float || element instanceof String ||
				element instanceof Boolean || element instanceof Long)
			if(j==0)
			{
				return convertElement(element);
			}
			else
				return "";
		else if(element instanceof ArrayList)
		{
			ArrayList list = (ArrayList)element;
			if(j<list.size())
				return convertElement(list.get(j));
			else
				return "";
		}
		return "";
	}
	
	private String convertElement(Object element)
	{
		if(element instanceof Double)
			return ((Double) element)+"";
		if(element instanceof Boolean)
			return ((Boolean) element).toString();
		if(element instanceof Long)
			return ((Long) element)+"";
		if(element instanceof Integer)
			return ((Integer) element)+"";
		if(element instanceof Float)
			return ((Float) element)+"";
		if(element instanceof String)
			return (String) element;
		else
			return "";
	}
	
	public void refreshResults()
	{
		populate();
	}
	
	/** Basic inner class use to set the cell renderer. */
	class ResultsTable
		extends JTable
	{
		
		/**
		 * Creates a new instance.
		 * 
		 */
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
	
	
	/** 
	 * Inner class used to display the results of measurement.
	 */
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
	    	if (row < 0 || row > values.size()) return;
	    	MeasurementObject rowData = values.get(row);
	    	rowData.setElement(value, col);
	    	fireTableCellUpdated(row, col);
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
		public boolean isCellEditable(int row, int col) { return false; }
		
	}

}
