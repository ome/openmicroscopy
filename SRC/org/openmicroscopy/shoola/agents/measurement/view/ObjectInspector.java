/*
 * org.openmicroscopy.shoola.agents.measurement.view.ObjectInspector 
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
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

//Third-party libraries
import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.Figure;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.IconManager;
import org.openmicroscopy.shoola.agents.measurement.MeasurementAgent;
import org.openmicroscopy.shoola.agents.measurement.util.AttributeField;
import org.openmicroscopy.shoola.agents.measurement.util.ColorCellRenderer;
import org.openmicroscopy.shoola.util.roi.figures.DrawingAttributes;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;


/** 
 * UI Component displaying various drawing information about a 
 * Region of Interest.
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
class ObjectInspector 
	extends JPanel
{

	/** Collection of column names. */
	private static List<String>			columnNames;
	
	/** The name of the panel. */
	private static final String			NAME = "Inspector";
	
	/** The table hosting the various fields. */
	private JTable 						fieldTable;

	/** Reference to the control. */
	private MeasurementViewerControl	controller;
	
	/** Reference to the model. */
	private MeasurementViewerModel		model;

	static {
		columnNames = new ArrayList<String>(2);
		columnNames.add("Field");
		columnNames.add("Value");
	}
	
	/** Initializes the component composing the display. */
	private void initComponents()
	{
		List<AttributeField> l = new ArrayList<AttributeField>();
		l.add(new AttributeField(AttributeKeys.TEXT, "Text", true));
		l.add(new AttributeField(DrawingAttributes.SHOWTEXT, "Show Text", 
				false));
		l.add(new AttributeField(DrawingAttributes.SHOWMEASUREMENT, 
					"Show Measurements", 
				false)); 
	//	l.add(new AttributeField(DrawingAttributes.INMICRONS, "Measurements In Microns", 
	//			false));
		l.add(new AttributeField(AttributeKeys.STROKE_WIDTH, "Line Width", 
				true));
		l.add(new AttributeField(AttributeKeys.FONT_SIZE, "Font Size", 
				true));
		l.add(new AttributeField(AttributeKeys.TEXT_COLOR, "Font Colour", 
				false));
		l.add(new AttributeField(AttributeKeys.FILL_COLOR, "Fill Colour", 
				false));
		l.add(new AttributeField(AttributeKeys.STROKE_COLOR, "Line Colour", 
				false));
		l.add(new AttributeField(DrawingAttributes.MEASUREMENTTEXT_COLOUR, 
				"Measurement Text Colour", false));
		
		//create the table
		fieldTable = new FigureTable(new FigureTableModel(l, columnNames));
		fieldTable.getTableHeader().setReorderingAllowed(false);
		fieldTable.setRowHeight(25);
		fieldTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		fieldTable.setCellSelectionEnabled(true);
		fieldTable.setColumnSelectionAllowed(true);
		
		fieldTable.addMouseListener(new java.awt.event.MouseAdapter() 
		{
			public void mouseClicked(java.awt.event.MouseEvent e) {
				if (e.getClickCount() == 2) {
					e.consume();
					int col = fieldTable.getSelectedColumn();
					int row = fieldTable.getSelectedRow();

					Object value = fieldTable.getValueAt(row, col);
					if (value instanceof Color)
						controller.showColorPicker((Color) value);
					if (value instanceof Boolean)
						toggleValue();
				}
			}
		});

	}
	
	/** Toggles the value of the boolean under the current selection. */
	private void toggleValue()
	{
		int col = fieldTable.getSelectedColumn();
		int row = fieldTable.getSelectedRow();
		Boolean value = (Boolean)fieldTable.getModel().getValueAt(row, col);
		boolean newValue = !(value.booleanValue());
		fieldTable.getModel().setValueAt(new Boolean(newValue), row, col);
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		setLayout(new BorderLayout());
		add(new JScrollPane(fieldTable), BorderLayout.CENTER);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param controller Reference to the Control. Mustn't be <code>null</code>.
	 * @param model		 Reference to the Model. Mustn't be <code>null</code>.
	 */
	ObjectInspector(MeasurementViewerControl controller, 
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
		return icons.getIcon(IconManager.INSPECTOR);
	}
	
	/**
	 * Sets the passed color to the currently selected cell.
	 * 
	 * @param c The color to set.
	 */
	void setCellColor(Color c)
	{
		int col = fieldTable.getSelectedColumn();
		int row = fieldTable.getSelectedRow();
		fieldTable.getModel().setValueAt(c, row, col);
	}

	/**
	 * Sets the data.
	 * 
	 * @param figure The data to set.
	 */
	void setModelData(Figure figure)
	{
		FigureTableModel tableModel = (FigureTableModel) fieldTable.getModel();
		tableModel.setData(figure);
		fieldTable.setModel(tableModel);
		fieldTable.repaint();
	}
	
	/**
	 * Sets the new figure retrieved from the passed collection.
	 * 
	 * @param l The collection to handle.
	 */
	void setSelectedFigures(Collection l)
	{
		FigureTableModel tableModel = (FigureTableModel) fieldTable.getModel();
		Iterator i = l.iterator();
		//Register error and notify user.
		ROI roi;
		ROIShape shape;
		try {
			while (i.hasNext()) {
				roi = (ROI) i.next();
				shape = roi.getShape(model.getCurrentView());
				tableModel.setData(shape.getFigure());
				fieldTable.setModel(tableModel);
				fieldTable.repaint();
			}
		} catch (Exception e) {
			MeasurementAgent.getRegistry().getLogger().info(this, 
													"Figures selection"+e);;
		}
		
	}
	
	/** Basic inner class use to set the cell renderer. */
	class FigureTable
		extends JTable
	{
		
		/**
		 * Creates a new instance.
		 * 
		 * @param model The model used by this table.
		 */
		FigureTable(FigureTableModel model)
		{
			super(model);
		}
		
		/**
		 * Overridden to return a customized cell renderer.
		 * @see JTable#getCellRenderer(int, int)
		 */
		public TableCellRenderer getCellRenderer(int row, int column) 
		{
	        return new ColorCellRenderer();
	    }

	}
	
	/** Inner class used to display the {@link Figure}. */
	class FigureTableModel
		extends AbstractTableModel
	{
	
		/** Identifies the <code>N/A</code> string. */
		private static final String		NA = "N/A";
		
		/** The figure this model is for. */
		private Figure					figure;
		
		/** The collection of column's names. */
		private List<String>			columnNames;	
		
		/** Collection of supported keys. */
		private List<AttributeKey>		keys;
		
		/** Collection of values handled by this model. */
		private List					values;
		
		/** Collection of fields. */
		private List<AttributeField>	fieldList;
		
		/**
		 * Creates a new instance.
		 * 
		 * @param fieldList 	The collection of fields.
		 * 						Mustn't be <code>null</code>.
		 * @param columnNames	The collection of column's names.
		 * 						Mustn't be <code>null</code>.
		 */
		public FigureTableModel(List<AttributeField> fieldList,
								List<String> columnNames)
		{
			if (fieldList == null)
				throw new IllegalArgumentException("No fields specified.");
			if (columnNames == null)
				throw new IllegalArgumentException("No column's names " +
												"specified.");
			this.fieldList = fieldList;
			this.columnNames = columnNames;
			keys = new ArrayList<AttributeKey>();
			values = new ArrayList<Object>();
		}
	
		/**
		 * Sets the figure handled by this model.
		 * 
		 * @param figure The figure data.
		 */
		public void setData(Figure figure)
		{
			if (figure == null)
				throw new IllegalArgumentException("No figure.");
			this.figure = figure;
			keys.clear();
			values.clear();
			boolean found;
			Iterator i;
			AttributeKey key;
			for (AttributeField fieldName : fieldList) {
				found = false;
				i = figure.getAttributes().keySet().iterator();
				while (i.hasNext()) {
					key = (AttributeKey) i.next();
					if (key.equals(fieldName.getKey())) {
						keys.add(key);
						values.add(figure.getAttribute(key));
						found = true;
						break;
					}
				}
				if (!found) {
					keys.add(fieldName.getKey());
					values.add(NA);
				}
			}
			fireTableDataChanged();
		}
		
		/**
		 * Overridden to return the name of the specified column.
		 * @see AbstractTableModel#getColumnName(int)
		 */
		public String getColumnName(int col) { return columnNames.get(col); }
	
		/**
		 * Returns the number of columns.
		 * @see AbstractTableModel#getColumnCount()
		 */
		public int getColumnCount() { return columnNames.size();  }
	
		/**
		 * Returns the number of rows.
		 * @see AbstractTableModel#getRowCount()
		 */
		public int getRowCount() { return keys.size(); }
	
		/**
		 * Returns the value of the specified cell.
		 * @see AbstractTableModel#getValueAt(int, int)
		 */
		public Object getValueAt(int rowIndex, int columnIndex)
		{
			if (columnIndex == 0)
	    		return fieldList.get(rowIndex).getName();
	    	return values.get(rowIndex);
		}
	
		/**
		 * Sets the value depending on the <code>Attribute Key</code>.
		 * @see AbstractTableModel#setValueAt(Object, int, int)
		 */
		public void setValueAt(Object value, int row, int col) 
	    {
			AttributeKey key = keys.get(row);
	    	if (figure.getAttribute(key) instanceof Double)
	    		figure.setAttribute(keys.get(row), new Double((String) value));
	        else if(figure.getAttribute(key) instanceof Boolean)
	        	figure.setAttribute(keys.get(row), value);
	        else
	        	figure.setAttribute(keys.get(row), value);
	    	values.set(row, value);
	    	fireTableCellUpdated(row, col);
	    }
		
		/**
		 * Depending on the selected cell, allow the user to edit.
		 * @see AbstractTableModel#isCellEditable(int, int)
		 */
		public boolean isCellEditable(int row, int col)
	    { 
	    	if (values.get(row) instanceof String)
	    		if (values.get(row).equals(NA)) return false;
	    	return fieldList.get(row).isEditable();
	    }
	}
	
}
