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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.TableModel;
import javax.swing.tree.TreeSelectionModel;


//Third-party libraries
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.ColumnFactory;
import org.jdesktop.swingx.table.TableColumnExt;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.IconManager;
import org.openmicroscopy.shoola.agents.measurement.MeasurementAgent;
import org.openmicroscopy.shoola.agents.measurement.util.AnnotationDescription;
import org.openmicroscopy.shoola.agents.measurement.util.ROINode;
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
	
	
	/** Collection of column names. */
	private static List<String>			columnNames;
	
	/** The name of the panel. */
	private static final String			NAME = "Manager";
	
	/** The table hosting the ROI objects. */
	private ROITable					objectsTable;

	/** Reference to the Model. */
	private MeasurementViewerModel		model;
	
	/** Reference to the View. */
	private MeasurementViewerUI 		view;
	
	/** 
	 * The table selection listener attached to the table displaying the 
	 * objects.
	 */
	private TreeSelectionListener		treeSelectionListener;
	
	/** 
	 * List of default column names.
	 */
	static {
		columnNames = new Vector<String>(6);
		columnNames.add("ROI");
		columnNames.add(AnnotationDescription.ROIID_STRING);
		columnNames.add(AnnotationDescription.TIME_STRING);
		columnNames.add(AnnotationDescription.ZSECTION_STRING);
		columnNames.add(AnnotationDescription.SHAPE_STRING);
		columnNames.add(AnnotationDescription.annotationDescription.get(
			AnnotationKeys.BASIC_TEXT));
		columnNames.add("Visible");
	}
	
	/**
	 * List of default column sizes. 
	 */
	static HashMap<String, Integer> columnWidths;
	static{
		columnWidths= new HashMap<String, Integer>();
        columnWidths.put(columnNames.get(0), 80);
        columnWidths.put(columnNames.get(1),36);
        columnWidths.put(columnNames.get(2),36);
        columnWidths.put(columnNames.get(3),36);
        columnWidths.put(columnNames.get(4),36);
        columnWidths.put(columnNames.get(5),128);
        columnWidths.put(columnNames.get(6),48);
	}
        
	/**
	 * overridden version of {@line TabPaneInterface#getIndex()}
	 */
	public int getIndex() {return INDEX; }
	

	/** Initializes the components composing the display. */
	private void initComponents()
	{
		ROINode root = new ROINode("root");
        Vector cName = (Vector) columnNames;
        ROITableModel tableModel = new ROITableModel(root, cName);
        	   
	    objectsTable = new ROITable(tableModel, cName);
	    objectsTable.setRootVisible(false);
	    objectsTable.setColumnSelectionAllowed(true);
	    objectsTable.setRowSelectionAllowed(true);
	    treeSelectionListener = new TreeSelectionListener()
	    {

			public void valueChanged(TreeSelectionEvent e)
			{
				{
					TreeSelectionModel tsm =
			            objectsTable.getTreeSelectionModel();
			        if (tsm.isSelectionEmpty()) 
			        {
			        } 
			        else 
			        {
			        	int []index = tsm.getSelectionRows();
			        	if (index.length == 0) return;
			        	if (index.length == 1)
			        	{
			        		ROIShape shape = objectsTable.
			        		getROIShapeAtRow(objectsTable.getSelectedRow());
			        		if(shape == null) return;
			        		view.selectFigure(shape.getFigure());
			        		int col = objectsTable.getSelectedColumn();
							int row = objectsTable.getSelectedRow();
							if(row < 0 || col < 0)
								return;
							Object value = objectsTable.getValueAt(row, col);
							if (value instanceof Boolean) toggleValue();
				    	}
	
			        	for(int i = 0 ; i  < index.length ; i++)
			        	{
			        		ROIShape shape = objectsTable.
			        						getROIShapeAtRow(index[i]);
			        		if(shape != null)
			        		{
			        			view.selectFigure(shape.getFigure());
			        			requestFocus();
			        		}
			        	}
			        }
				}
			}	    	
	    };
	    
	    objectsTable.addTreeSelectionListener(treeSelectionListener);
		objectsTable.addMouseListener(new java.awt.event.MouseAdapter() 
		{
			public void mouseClicked(java.awt.event.MouseEvent e) 
			{
				int col = objectsTable.getSelectedColumn();
				int row = objectsTable.getSelectedRow();
				if(row < 0 || col < 0)
					return;
				Object value = objectsTable.getValueAt(row, col);
				if (value instanceof Boolean) toggleValue();
			}
		});
		
	     ColumnFactory columnFactory = new ColumnFactory() {
            @Override
            public void configureTableColumn(TableModel model, TableColumnExt columnExt) {
                super.configureTableColumn(model, columnExt);
                if (columnExt.getModelIndex() == 1) {
                    //columnExt.setVisible(false);
                }
            }
 
            public void configureColumnWidths(JXTable table, TableColumnExt columnExt) 
            {
            	columnExt.setPreferredWidth(columnWidths.get(columnExt.getHeaderValue()));
            }
        };
    	objectsTable.setHorizontalScrollEnabled(true);
	    objectsTable.setColumnControlVisible(true);
	    objectsTable.setColumnFactory(columnFactory);
	}


	/** Toggles the value of the boolean under the current selection. */
	private void toggleValue()
	{
		int col = objectsTable.getSelectedColumn();
		int row = objectsTable.getSelectedRow();
		Boolean value = (Boolean) objectsTable.getValueAt(row, col);
		ROIShape roiShape = objectsTable.getROIShapeAtRow(row);
		boolean newValue = !(value.booleanValue());
		objectsTable.setValueAt(new Boolean(newValue), row, col);
		if(roiShape!=null)
			objectsTable.selectROIShape(roiShape);
		else
			objectsTable.expandROIRow(objectsTable.getROIAtRow(row));		

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
	 * Rebuild Tree
	 * 
	 */
	public void rebuildTable()
	{
		TreeMap<Long, ROI> roiList = model.getROI();
		Iterator<ROI> iterator = roiList.values().iterator();
		ROI roi;
		TreeMap<Coord3D, ROIShape> shapeList;
		Iterator<ROIShape> shapeIterator;
		objectsTable.clear();
		while(iterator.hasNext())
		{
			roi = iterator.next();
			shapeList = roi.getShapes();
			shapeIterator = shapeList.values().iterator();
			while (shapeIterator.hasNext())
				objectsTable.addROIShape(shapeIterator.next());		
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
		return icons.getIcon(IconManager.MANAGER);
	}
	
	/**
	 * Adds the collection of figures to the display.
	 * 
	 * @param l The collection of objects to add.
	 */
	void addFigures(Collection l)
	{
		Iterator i=l.iterator();
		ROI roi;
		while (i.hasNext())
		{
			roi=(ROI) i.next();
			Iterator<ROIShape> j=roi.getShapes().values().iterator();
			while (j.hasNext())
			{
				objectsTable.addROIShape(j.next());
			}
		}
	}
	
	/**
	 * Selects the collection of figures.
	 * 
	 * @param l
	 *            The collection of objects to select.
	 * @param clear
	 *            Pass <code>true</code> to clear the selection
	 *            <code>false</code> otherwise.
	 */
	void setSelectedFigures(Collection l, boolean clear)
	{
		Iterator i = l.iterator();
		ROI roi;
		ROIFigure figure;
		objectsTable.clearSelection();
		TreeSelectionModel tsm = objectsTable.getTreeSelectionModel();
		if (clear) tsm.clearSelection();
		tsm.removeTreeSelectionListener(treeSelectionListener);
	
		try {
			while (i.hasNext()) {
				roi = (ROI) i.next();
				figure = roi.getFigure(model.getCurrentView());
				objectsTable.selectROIShape(figure.getROIShape());
			}
			objectsTable.repaint();
		} catch (Exception e) {
			MeasurementAgent.getRegistry().getLogger().info(this, 
					"Figure selection "+e);
		}
		tsm.addTreeSelectionListener(treeSelectionListener);
	}
	
	/**
	 * Removes the passed figure from the table.
	 * 
	 * @param figure The figure to remove.
	 */
	void removeFigure(ROIFigure figure)
	{
		if (figure == null) return;
		objectsTable.removeROIShape(figure.getROIShape());
		objectsTable.repaint();
	}
	
	/** Repaints the table. */
	void update() 
	{ 
		objectsTable.refresh();
		objectsTable.invalidate(); 
		objectsTable.repaint();
	}
	
}
	
