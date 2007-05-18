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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;



//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.IconManager;
import org.openmicroscopy.shoola.agents.measurement.util.AnnotationField;
import org.openmicroscopy.shoola.agents.measurement.util.MeasurementObject;
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
	private static List<String>				columnNames;
	
	/** Collection of column names. */
	private static List<AnnotationField>	fields;
	
	/** Button to save locally the results. */
	private JButton							saveButton;
	
	/** The table displaying the results. */
	private JTable							results;
	
	/** Reference to the control. */
	private MeasurementViewerControl		controller;
	
	/** Reference to the model. */
	private MeasurementViewerModel			model;
	
	static {
		fields = new ArrayList<AnnotationField>(11);
		fields.add(new AnnotationField(AnnotationKeys.ROIID,"ROI ID", false)); 
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
		fields.add(new AnnotationField(AnnotationKeys.LENGTH,"Length", false)); 
		fields.add(new AnnotationField(AnnotationKeys.ANGLE,"Angle", false)); 
		fields.add(new AnnotationField(AnnotationKeys.STARTPOINT,"Start Point", 
										false)); 
		fields.add(new AnnotationField(AnnotationKeys.ENDPOINT,"End Point", 
										false)); 
		columnNames = new ArrayList<String>(fields.size()+2);
		columnNames.add("Time Point");
		columnNames.add("Z Section");
		for (int i = 0 ; i < fields.size(); i++)
			columnNames.add(fields.get(i).getName());
	}
	
	/** Initializes the components composing the display. */
	private void initComponents()
	{
		saveButton = new JButton("Save");
		saveButton.addActionListener(new ActionListener() {
		
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
		
			}
		
		});
		
		//Create table model.
		results = new JTable();
		results.getTableHeader().setReorderingAllowed(false);
		MeasurementTableModel tm = new MeasurementTableModel(columnNames);
		results.setModel(tm);
		
	}
	
	/** Builds and lays out the GUI. */
	private void buildGUI()
	{
		setLayout(new BorderLayout());
		add(new JScrollPane(results), BorderLayout.CENTER);
		add(UIUtilities.buildComponentPanelRight(saveButton), 
			BorderLayout.SOUTH);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param controller Reference to the Control. Mustn't be <code>null</code>.
	 * @param model		 Reference to the Model. Mustn't be <code>null</code>.
	 */
	MeasurementResults(MeasurementViewerControl	controller, 
					MeasurementViewerModel model)
	{
		if (controller == null)
			throw new IllegalArgumentException("No control.");
		if (model == null)
			throw new IllegalArgumentException("No model.");
		this.controller = controller;
		this.model = model;
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
		MeasurementTableModel tm = (MeasurementTableModel) results.getModel();
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
				for (int k = 0; k < fields.size(); k++) {
					key = fields.get(k).getKey();
					row.addElement(key.get(shape));
				}
				tm.addRow(row);
			}
		}
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
