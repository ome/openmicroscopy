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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeSelectionModel;




import org.apache.commons.collections.CollectionUtils;
//Third-party libraries
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.ColumnFactory;
import org.jdesktop.swingx.table.TableColumnExt;
import org.jhotdraw.draw.Figure;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.IconManager;
import org.openmicroscopy.shoola.agents.measurement.MeasurementAgent;
import org.openmicroscopy.shoola.agents.measurement.util.TabPaneInterface;
import org.openmicroscopy.shoola.agents.measurement.util.model.AnnotationDescription;
import org.openmicroscopy.shoola.agents.measurement.util.roitable.ROINode;
import org.openmicroscopy.shoola.agents.measurement.util.roitable.ROITableModel;
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
	
	/** 
	 * List of default column names.
	 */
	private static Vector<String>			COLUMN_NAMES;
	
	static {
		COLUMN_NAMES = new Vector<String>(6);
		COLUMN_NAMES.add("ROI");
		COLUMN_NAMES.add(AnnotationDescription.ROIID_STRING);
		COLUMN_NAMES.add(AnnotationDescription.ZSECTION_STRING);
		COLUMN_NAMES.add(AnnotationDescription.TIME_STRING);
		COLUMN_NAMES.add(AnnotationDescription.SHAPE_STRING);
		//columnNames.add(AnnotationDescription.annotationDescription.get(
		//		AnnotationKeys.NAMESPACE));
		//columnNames.add(AnnotationDescription.annotationDescription.get(
		//		AnnotationKeys.KEYWORDS));
		COLUMN_NAMES.add(AnnotationDescription.annotationDescription.get(
			AnnotationKeys.TEXT));
		COLUMN_NAMES.add("Visible");
	}
	
	/**
	 * List of default column sizes. 
	 */
	private static Map<String, Integer> COLUMN_WIDTHS;
	
	static{
		COLUMN_WIDTHS = new HashMap<String, Integer>();
        COLUMN_WIDTHS.put(COLUMN_NAMES.get(0), 80);
        COLUMN_WIDTHS.put(COLUMN_NAMES.get(1), 36);
        COLUMN_WIDTHS.put(COLUMN_NAMES.get(2), 36);
        COLUMN_WIDTHS.put(COLUMN_NAMES.get(3), 36);
        COLUMN_WIDTHS.put(COLUMN_NAMES.get(4), 36);
        //columnWidths.put(columnNames.get(5), 36);
        //columnWidths.put(columnNames.get(6), 96);
        COLUMN_WIDTHS.put(COLUMN_NAMES.get(5), 96);
        COLUMN_WIDTHS.put(COLUMN_NAMES.get(6), 36);
	}
	
	/** Index to identify tab */
	private final static int		INDEX = MeasurementViewerUI.MANAGER_INDEX;

	/** The name of the panel. */
	private static final String			NAME = "Manager";
	
	/** The table hosting the ROI objects. */
	private ROITable					objectsTable;

	/** Reference to the Model. */
	private MeasurementViewerModel		model;
	
	/** Reference to the View. */
	private MeasurementViewerUI 		view;

	/** Reference to the Model. */
    private MeasurementViewerControl control;

	/** 
	 * The table selection listener attached to the table displaying the 
	 * objects.
	 */
	private TreeSelectionListener		treeSelectionListener;

	/** Initializes the components composing the display. */
	private void initComponents()
	{
		ROINode root = new ROINode("root");
	    objectsTable = new ROITable(new ROITableModel(root, COLUMN_NAMES), 
	    		COLUMN_NAMES, this);
	    objectsTable.setRootVisible(false);
	    objectsTable.setColumnSelectionAllowed(true);
	    objectsTable.setRowSelectionAllowed(true);
	    treeSelectionListener = new TreeSelectionListener()
	    {
			
			public void valueChanged(TreeSelectionEvent e)
			{
				TreeSelectionModel tsm = objectsTable.getTreeSelectionModel();
				if (tsm.isSelectionEmpty()) return;
				int[] index = tsm.getSelectionRows();
				if (index.length == 0) return;
				if (index.length == 1)
				{
					ROINode node = (ROINode) objectsTable.getNodeAtRow(
							objectsTable.getSelectedRow());
					if (node == null) return;
					Object nodeValue = node.getUserObject();
					if (nodeValue instanceof ROIShape) 
						view.selectFigure(((ROIShape) nodeValue).getFigure());
					int col = objectsTable.getSelectedColumn();
					int row = objectsTable.getSelectedRow();
					
					if (row < 0 || col < 0) return;
				}
				else
				{
					ROIShape shape;
					for (int i = 0; i < index.length; i++)
					{
						shape = objectsTable.getROIShapeAtRow(index[i]);
						if (shape != null)
						{
							view.selectFigure(shape.getFigure());
							requestFocus();
						}
					}
				}
			}
		};
	    
	    objectsTable.addTreeSelectionListener(treeSelectionListener);

	    ColumnFactory columnFactory = new ColumnFactory() {


	    	public void configureColumnWidths(JXTable table, 
	    			TableColumnExt columnExt) 
	    	{
	    		columnExt.setPreferredWidth(
	    				COLUMN_WIDTHS.get(columnExt.getHeaderValue()));
	    	}
	    };
    	objectsTable.setHorizontalScrollEnabled(true);
	    objectsTable.setColumnControlVisible(true);
	    objectsTable.setColumnFactory(columnFactory);
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
	 * @param view Reference to the view. Mustn't be <code>null</code>.
	 * @param controller Reference to the control. Mustn't be <code>null</code>.
	 * @param model Reference to the Model. Mustn't be <code>null</code>.
	 */
	ObjectManager(MeasurementViewerUI view, MeasurementViewerControl control,
	        MeasurementViewerModel model)
	{
		if (view == null) throw new IllegalArgumentException("No view.");
		if (model == null) throw new IllegalArgumentException("No model.");
		if (control == null) throw new IllegalArgumentException("No control.");
		this.view = view;
		this.model = model;
		this.control = control;
		initComponents();
		buildGUI();
	}
	
    /** 
     * Displays the menu at the specified location if not already visible.
     * 
     * @param x The x-coordinate of the mouse click.
     * @param y The y-coordinate of the mouse click.
     */
    void showROIManagementMenu(int x, int y)
    {
    	objectsTable.showROIManagementMenu(view.getDrawingView(), x, y);
    }
    
	/** Rebuilds Tree */
	void rebuildTable()
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
		objectsTable.collapseAll();
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
		Iterator<ROIShape> j;
		while (i.hasNext())
		{
			roi = (ROI) i.next();
			j = roi.getShapes().values().iterator();
			while (j.hasNext())
				objectsTable.addROIShape(j.next());
		}
	}

	/**
	 * Adds the collection of ROIShapes to the display.
	 * 
	 * @param shapeList The collection of ROIShapes to add.
	 */
	void addROIShapes(List<ROIShape> shapeList)
	{
		objectsTable.addROIShapeList(shapeList);
	}
	
	/**
	 * Selects the collection of figures.
	 * 
	 * @param l The collection of objects to select.
	 * @param clear Pass <code>true</code> to clear the selection
	 *            <code>false</code> otherwise.
	 */
	void setSelectedFigures(List<ROIShape> l, boolean clear)
	{
		Iterator<ROIShape> i = l.iterator();
		TreeSelectionModel tsm = objectsTable.getTreeSelectionModel();
		ROIFigure figure = null;
		ROIShape shape;
		if (clear) tsm.clearSelection();
		objectsTable.removeTreeSelectionListener(treeSelectionListener);
	
		try 
		{
			while (i.hasNext()) 
			{
				shape = i.next();
				figure = shape.getFigure();
				objectsTable.selectROIShape(figure.getROIShape());
			}
			objectsTable.repaint();
			if (figure != null)
				objectsTable.scrollToROIShape(figure.getROIShape());
		} 
		catch (Exception e) {
			MeasurementAgent.getRegistry().getLogger().info(this, 
					"Figure selection "+e);
		}
		
		objectsTable.addTreeSelectionListener(treeSelectionListener);
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
	
	/**
	 * Removes the passed figures from the table.
	 * 
	 * @param figures The figures to handle.
	 */
	void removeFigures(List<ROIFigure> figures)
	{
		if (figures == null || figures.size() == 0) return;
		Iterator<ROIFigure> i = figures.iterator();
		while (i.hasNext()) {
			objectsTable.removeROIShape(i.next().getROIShape());
		}
		objectsTable.repaint();
	}
	
	/**
	 * Deletes the ROI shapes in the list.
	 * 
	 * @param shapeList see above.
	 */
	void deleteROIShapes(List<ROIShape> shapeList)
	{
		view.deleteROIShapes(shapeList);
		this.rebuildTable();
	}
	
	/** Resets the component.*/
	void reset() { model.getROIComponent().reset(); }
	
	/**
	 * Duplicates the ROI shapes in the list and belonging to the ROI with
	 * id.
	 * @param id see above.
	 * @param shapeList see above.
	 */
	void duplicateROI(long id, List<ROIShape> shapeList)
	{
		view.duplicateROI(id, shapeList);
		this.rebuildTable();
	}
	
	/**
	 * Calculates the statistics for the roi in the list.
	 * 
	 * @param shapeList The collection of shapes.
	 */
	void calculateStats(List<ROIShape> shapeList)
	{
		view.calculateStats(shapeList);
	}
	
	/**
	 * Merges the ROI shapes in the list and belonging to the ROI with
	 * id in idList into a single new ROI. The ROI in the shape list should 
	 * all be on separate planes.
	 * 
	 * @param idList see above. see above.
	 * @param shapeList see above.
	 */
	void mergeROI(List<Long> idList, List<ROIShape> shapeList)
	{
		view.mergeROI(idList, shapeList);
		this.rebuildTable();
	}
	
	/**
	 * Split the ROI shapes in the list and belonging to the ROI with
	 * id into a single new ROI. The ROI in the shape list should 
	 * all be on separate planes.
	 * 
	 * @param id see above. see above.
	 * @param shapeList see above.
	 */
	void splitROI(long id, List<ROIShape> shapeList)
	{
		view.splitROI(id, shapeList);
		this.rebuildTable();
	}
	
	/** Repaints the table. */
	void update() 
	{ 
		objectsTable.refresh();
		objectsTable.invalidate(); 
		objectsTable.repaint();
	}

	/**
	 * Shows the roi assistant for the roi.
	 * 
	 * @param roi see above.
	 */
	void propagateROI(ROI roi)
	{
		view.showROIAssistant(roi);
	}
	
	/**
	 * Display message in status bar. 
	 * @param messageString see above.
	 */
	void showMessage(String messageString)
	{
		view.setStatus(messageString);
	}
	
	/**
	 * Display Ready message in status bar. 
	 */
	void showReadyMessage()
	{
		view.setReadyStatus();
	}
	
	/** Invokes when new figures are selected. */
	void onSelectedFigures()
	{
		objectsTable.onSelectedFigures(model.getSelectedFigures());
	}
	
	/** 
	 * Returns the selected figures.
	 * 
	 * @return See above.
	 */
	Collection<Figure> getSelectedFigures()
	{
		return model.getSelectedFigures();
	}
	
	/**
	 * Implemented as specified by the I/F {@link TabPaneInterface}
	 * @see TabPaneInterface#getIndex()
	 */
	public int getIndex() { return INDEX; }

	/** Loads the tags.*/
	void loadTags()
	{
	    control.loadTags();
	}
	
	/**
	 * Returns a collection of all figures selected
	 * If a ROI is selected, all the figures hosted will that ROI will be
	 * returned.
	 * @return See above.
	 */
	Collection<Figure> getSelectedFiguresFromTables()
	{
	    List l = objectsTable.getSelectedObjects();
        if (CollectionUtils.isEmpty(l)) return null;
        Iterator i = l.iterator();
        Object o;
        ROI roi;
        ROIShape shape;
        List<Figure> list = new ArrayList<Figure>();
        while (i.hasNext()) {
            o =  i.next();
            if (o instanceof ROI) {
                roi = (ROI) o;
                list.addAll(roi.getAllFigures());
            } else if (o instanceof ROIShape) {
                shape = (ROIShape) o;
                list.add(shape.getFigure());
            }
        }
        return list;
	}
}

