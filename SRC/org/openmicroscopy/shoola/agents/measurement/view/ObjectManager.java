/*
 * org.openmicroscopy.shoola.agents.measurement.view.ObjectManager 
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;


//Third-party libraries
import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.Figure;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.IconManager;
import org.openmicroscopy.shoola.agents.measurement.MeasurementAgent;
import org.openmicroscopy.shoola.agents.measurement.util.AnnotationDescription;
import org.openmicroscopy.shoola.agents.measurement.util.ROITableCellRenderer;
import org.openmicroscopy.shoola.agents.measurement.util.TabPaneInterface;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys;
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;

/** 
 * UI Component managing a Region of Interest.
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
class ObjectManager 
	extends JPanel
	implements TabPaneInterface
{
	/** Index to identify tab */
	public final static int		INDEX = MeasurementViewerUI.MANAGER_INDEX;

	/** The minimum width of the columns. */
	private static int					COLUMNWIDTH = 32; 
	
	/** ROI ID Column no for the wizard. */
	private static final int				ROIID_COLUMN = 0;

	/** Time point Column no for the wizard. */
	private static final int				TIME_COLUMN = 1;
	
	/** Z-Section Column no for the wizard. */
	private static final int				Z_COLUMN = 2;

	/** Type Column no for the wizard. */
	private static final int				SHAPE_COLUMN = 3;

	/** Annotation Column no for the wizard. */
	private static final int				ANNOTATION_COLUMN = 4;

	/** Visible Column no for the wizard. */
	private static final int				VISIBLE_COLUMN = 5;

	/** Collection of column names. */
	private static List<String>			columnNames;
	
	/** The name of the panel. */
	private static final String			NAME = "Manager";
	
	/** The table hosting the ROI objects. */
	private ObjectTable					objectsTable;

	/** Reference to the View. */
	private MeasurementViewerUI			view;
	
	/** Reference to the Model. */
	private MeasurementViewerModel		model;
	
	/** 
	 * The table selection listener attached to the table displaying the 
	 * objects.
	 */
	private ListSelectionListener		listener;
	
	static {
		columnNames = new ArrayList<String>(6);
		columnNames.add(AnnotationDescription.ROIID_STRING);
		columnNames.add(AnnotationDescription.TIME_STRING);
		columnNames.add(AnnotationDescription.ZSECTION_STRING);
		columnNames.add(AnnotationDescription.SHAPE_STRING);
		columnNames.add(AnnotationDescription.annotationDescription.get(
			AnnotationKeys.BASIC_TEXT));
		columnNames.add("Visible");
	}

	/**
	 * overridden version of {@line TabPaneInterface#getIndex()}
	 */
	public int getIndex() {return INDEX; }
	
	/** Initializes the components composing the display. */
	private void initComponents()
	{
		objectsTable = new ObjectTable(new ROIFigureTableModel(columnNames));
		objectsTable.setSelectionMode(
						ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		objectsTable.setRowSelectionAllowed(true);
		objectsTable.getTableHeader().setReorderingAllowed(false);
		//		Add selection listener
		
	//	resizeTableColumns();
		listener = new ListSelectionListener() {
		
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) return;

		        ListSelectionModel lsm =
		            (ListSelectionModel) e.getSource();
		        if (lsm.isSelectionEmpty()) {
		        } else {
		        	int index = lsm.getMinSelectionIndex();
		        	if (index < 0) return;
		        	ROIFigureTableModel m = 
	        			(ROIFigureTableModel) objectsTable.getModel();
	        		ROIFigure figure = (ROIFigure) m.getFigureAt(index);
	        		view.selectFigure(figure);
		        }
			}
		
		};
		

		objectsTable.addMouseListener(new java.awt.event.MouseAdapter() 
		{
			public void mouseClicked(java.awt.event.MouseEvent e) 
			{
				int col = objectsTable.getSelectedColumn();
				int row = objectsTable.getSelectedRow();
				Object value = objectsTable.getValueAt(row, col);
				if (value instanceof Boolean) toggleValue();
			}
		});
		objectsTable.getSelectionModel().addListSelectionListener(listener);
	}
	

	/** Toggles the value of the boolean under the current selection. */
	private void toggleValue()
	{
		int col = objectsTable.getSelectedColumn();
		int row = objectsTable.getSelectedRow();
		TableModel tm = objectsTable.getModel();
		Boolean value = (Boolean) tm.getValueAt(row, col);
		boolean newValue = !(value.booleanValue());
		tm.setValueAt(new Boolean(newValue), row, col);
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		setLayout(new BorderLayout());
		add(new JScrollPane(objectsTable), BorderLayout.CENTER);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param view 	Reference to the control. Mustn't be <code>null</code>.
	 * @param model	Reference to the Model. Mustn't be <code>null</code>.
	 */
	ObjectManager(MeasurementViewerUI	view, MeasurementViewerModel model)
	{
		if (view == null) throw new IllegalArgumentException("No view.");
		if (model == null) throw new IllegalArgumentException("No model.");
		this.view = view;
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
		return icons.getIcon(IconManager.MANAGER);
	}
	
	/**
	 * Adds the collection of figures to the display.
	 * 
	 * @param l The collection of objects to add.
	 */
	void addFigures(Collection l)
	{
		Iterator i = l.iterator();
		ROI roi;
		ROIFigureTableModel tm = (ROIFigureTableModel) objectsTable.getModel();
		try {
			while (i.hasNext()) {
				roi = (ROI) i.next();
				tm.addFigure(roi.getFigure(model.getCurrentView()));
			}
			objectsTable.setModel(tm);
			objectsTable.repaint();
		} catch (Exception e) {
			MeasurementAgent.getRegistry().getLogger().info(this, 
					"Figures addition "+e);;
		}
	}
	
	/**
	 * Selects the collection of figures.
	 * 
	 * @param l 	The collection of objects to select.
	 * @param clear	Pass <code>true</code> to clear the selection
	 * 				<code>false</code> otherwise.
	 */
	void setSelectedFigures(Collection l, boolean clear)
	{
		Iterator i = l.iterator();
		ROI roi;
		ROIFigure figure;
		ROIFigureTableModel tm = (ROIFigureTableModel) objectsTable.getModel();
		int row;
		ListSelectionModel lsm = objectsTable.getSelectionModel();
		if (clear) lsm.clearSelection();
		lsm.removeListSelectionListener(listener);
		try {
			while (i.hasNext()) {
				roi = (ROI) i.next();
				figure = roi.getFigure(model.getCurrentView());
				row = tm.getRowFromFigure(figure);
				objectsTable.addRowSelectionInterval(row, row);
			}
			objectsTable.repaint();
		} catch (Exception e) {
			MeasurementAgent.getRegistry().getLogger().info(this, 
					"Figure selection "+e);
		}
		lsm.addListSelectionListener(listener);
	}
	
	/**
	 * Removes the passed figure from the table.
	 * 
	 * @param figure The figure to remove.
	 */
	void removeFigure(ROIFigure figure)
	{
		if (figure == null) return;
		ROIFigureTableModel m = (ROIFigureTableModel) objectsTable.getModel();
		m.removeFigure(figure);
		objectsTable.setModel(m);
		objectsTable.repaint();
	}
	
	/** Repaints the table. */
	void update() { objectsTable.repaint(); }
	
	/** Rebuilds the table,and repaints. */
	void rebuildTable()
	{
		ROIFigureTableModel tm = (ROIFigureTableModel) objectsTable.getModel();
		tm.clear();
		TreeMap<Long, ROI> roiList = model.getROI();
		Iterator<ROI> iterator = roiList.values().iterator();
		ROI roi;
		TreeMap<Coord3D, ROIShape> shapeList;
		Iterator<ROIShape> shapeIterator;
		while(iterator.hasNext())
		{
			roi = iterator.next();
			shapeList = roi.getShapes();
			shapeIterator = shapeList.values().iterator();
			while (shapeIterator.hasNext())
				tm.addFigure(shapeIterator.next().getFigure());		
		}
	}
	
	/** Basic inner class use to set the cell renderer. */
	class ObjectTable
		extends JTable
	{
		
		/**
		 * Creates a new instance.
		 * 
		 * @param model The model used by this table.
		 */
		ObjectTable(ROIFigureTableModel model)
		{
			super(model);
		}
		
		/**
		 * Overridden to return a customized cell renderer.
		 * @see JTable#getCellRenderer(int, int)
		 */
		public TableCellRenderer getCellRenderer(int row, int column) 
		{
	        return new ROITableCellRenderer();
	    }

	}
	
	/** 
	 * Inner class used to display stringified version of 
	 * the {@link ROIFigure}. 
	 */
	class ROIFigureTableModel 
		extends AbstractTableModel
	{
	
		/** The collection of column's names. */
		private List<String>			columnNames;
		
		/** Collection of {@link ROIFigure} hosted by this model. */
		private List<ROIFigure>			data;
		
		/**
		 * Remove all data from the table .
		 */
		public void clear()
		{
			int size = data.size();
			data.clear();
			this.fireTableRowsDeleted(0, size);
		}
		
		/**
		 * Converts and returns a stringified version of the {@link ROIFigure}
		 * hosted in the specified row.
		 * 
		 * @param row	The selected row.
		 * @param col	The selected column.
		 * @return See above.
		 */
		private Object mapFigureToValue(int row, int col)
		{
			if (row < 0) return null;
			ROIFigure fig = data.get(row);
			switch (col) {
				case ROIID_COLUMN: return fig.getROI().getID();
				case Z_COLUMN: return fig.getROIShape().getCoord3D().getZSection()+1;
				case TIME_COLUMN: return fig.getROIShape().getCoord3D().getTimePoint()+1;
				case SHAPE_COLUMN: return fig.getType();
	        	case ANNOTATION_COLUMN: return AttributeKeys.TEXT.get(fig);
	        	case VISIBLE_COLUMN: return fig.isVisible();
	        	default:
					return null;
			}
		}
		
		/**
		 * Converts the passed object to its corresponding value in the 
		 * {@link ROIFigure}.
		 * 
		 * @param value	The selected value.
		 * @param row	The selected row.
		 * @param col	The selected column.
		 */
		private void mapValueToFigure(Object value, int row, int col)
		{
			Figure fig = data.get(row);
	    	switch (col) {
	    		case 1:
		    	case 0:
		    		break;
		    	case ANNOTATION_COLUMN:
		    		AttributeKeys.TEXT.set(fig, (String) value);
		    		break;
		    	case VISIBLE_COLUMN:
		    		fig.setVisible((Boolean) value);
		    		break;
	    	}
		}
		
		/**
		 * Creates a new instance.
		 * 
		 * @param columnNames	The collection of column's names.
		 * 						Mustn't be <code>null</code>.
		 */
		ROIFigureTableModel(List<String> columnNames)
		{
			if (columnNames == null)
				throw new IllegalArgumentException("No column's names " +
											"specified.");
			this.columnNames = columnNames;
			data = new ArrayList<ROIFigure>();
		}
		
		/**
		 * Adds a new element to the model.
		 * 
		 * @param figure The figure to add.
		 */
		void addFigure(ROIFigure figure)
		{
			if (figure == null) return;
			data.add(figure);
	    	fireTableDataChanged();
		}
		
		/**
		 * Removes the passed element from the model.
		 * 
		 * @param figure The figure to remove.
		 */
		void removeFigure(ROIFigure figure) { data.remove(figure); }
		
		/**
		 * Returns the {@link Figure} hosted by the specified row index.
		 * 
		 * @param row The selected row.
		 * @return See above.
		 */
		Figure getFigureAt(int row)
		{
			if (row < 0 || row >= getRowCount()) return null;
			return data.get(row); 
		}
		
		/**
		 * Returns the index of the row hosting the passed {@link ROIFigure}.
		 * 
		 * @param figure The passed figure.
		 * @return See above.
		 */
		int getRowFromFigure(ROIFigure figure)
		{
			if (figure == null) return -1;
			for (int i = 0 ; i < data.size(); i++) 
				if (figure == data.get(i)) return i;
			
			return -1;
		}
		
		/**
		 * Sets the specifed {@link ROIFigure} and updates the model
		 * 
		 * @param figure The figure to set.
		 */
		void updateModel(ROIFigure figure)
		{
			int i = getRowFromFigure(figure);
			for (int j = 0 ; j < getColumnCount(); j++)
				setValueAt(mapFigureToValue(i,j), i, j);
			fireTableDataChanged();
		}
		
		/** Updates the model. */
		void update()
		{
			for (int i = 0 ; i < data.size(); i++)
				for (int j = 0 ; j < getColumnCount(); j++)
					setValueAt(mapFigureToValue(i,j), i, j);
			fireTableDataChanged();
		}
		
		/**
		 * Sets the specified value.
		 * @see AbstractTableModel#setValueAt(Object, int, int)
		 */
		public void setValueAt(Object value, int row, int col) 
	    {
	    	mapValueToFigure(value, row, col);
	        fireTableCellUpdated(row, col);
	    }
		
		/**
		 * Returns the value of the specified cell.
		 * @see AbstractTableModel#getValueAt(int, int)
		 */
		public Object getValueAt(int row, int column)
		{
			return mapFigureToValue(row, column);
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
		public int getRowCount() { return data.size(); }
		
		/**
		 * Returns <code>true</code> if the selected column equals 
		 * <code>4</code>.
		 * @see AbstractTableModel#isCellEditable(int, int)
		 */
		public boolean isCellEditable(int row, int col)  { return (col == 4); }
		
	}

}
