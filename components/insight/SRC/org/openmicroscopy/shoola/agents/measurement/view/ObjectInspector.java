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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.IconManager;
import org.openmicroscopy.shoola.agents.measurement.MeasurementAgent;
import org.openmicroscopy.shoola.agents.measurement.util.model.AnnotationDescription;
import org.openmicroscopy.shoola.agents.measurement.util.model.AttributeField;
import org.openmicroscopy.shoola.agents.measurement.util.model.FigureTableModel;
import org.openmicroscopy.shoola.agents.measurement.util.ui.FigureTable;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys;
import org.openmicroscopy.shoola.util.roi.model.annotation.MeasurementAttributes;


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
	
	/** Index to identify tab */
	public final static int		INDEX = MeasurementViewerUI.INSPECTOR_INDEX;

	/** The row hosting the fill color. */
	static final int FILL_COLOR_ROW = 5;
	
	/** The row hosting the fill color. */
	static final int LINE_COLOR_ROW = 6;
	
	/** Collection of column names. */
	private static final List<String>			COLUMN_NAMES;
	
	/** The row indicating to show the text or not. */
	private static final int SHOW_TEXT_ROW = 3;
	
	/** The row indicating to show the measurement or not. */
	private static final int SHOW_MEASUREMENT_ROW = 4;
	
	/** The name of the panel. */
	private static final String			NAME = "Inspector";
	
	/** The table hosting the various fields. */
	private FigureTable					fieldTable;

	/** Reference to the control. */
	private MeasurementViewerControl	controller;
	
	/** Reference to the model. */
	private MeasurementViewerModel		model;

	static {
		COLUMN_NAMES = new ArrayList<String>(2);
		COLUMN_NAMES.add("Field");
		COLUMN_NAMES.add("Value");
	}
	
	/** Initializes the component composing the display. */
	private void initComponents()
	{
		List<AttributeField> l = new ArrayList<AttributeField>();
		l.add(new AttributeField(MeasurementAttributes.TEXT, 
				AnnotationDescription.annotationDescription.get(
				AnnotationKeys.TEXT), Boolean.valueOf(true)));
		l.add(new AttributeField(MeasurementAttributes.WIDTH, 
				AnnotationDescription.annotationDescription.get(
				AnnotationKeys.WIDTH), Boolean.valueOf(true)));
		l.add(new AttributeField(MeasurementAttributes.HEIGHT, 
				AnnotationDescription.annotationDescription.get(
				AnnotationKeys.HEIGHT), Boolean.valueOf(true)));
		//l.add(new AttributeField(AnnotationKeys.NAMESPACE, "Workflow", false));
		//l.add(new AttributeField(AnnotationKeys.KEYWORDS, "Keywords", false));
		l.add(new AttributeField(MeasurementAttributes.SHOWTEXT, "Show Text", 
				Boolean.valueOf(false)));
		l.add(new AttributeField(MeasurementAttributes.SHOWMEASUREMENT, 
				AnnotationDescription.annotationDescription.get(
						MeasurementAttributes.SHOWMEASUREMENT), 
						Boolean.valueOf(false))); 
		//l.add(new AttributeField(MeasurementAttributes.SHOWID, 
			//"Show ID", false)); 
		l.add(new AttributeField(MeasurementAttributes.FILL_COLOR, 
				AnnotationDescription.annotationDescription.get(
						MeasurementAttributes.FILL_COLOR), 
						Boolean.valueOf(false)));
		l.add(new AttributeField(MeasurementAttributes.STROKE_COLOR, 
				AnnotationDescription.annotationDescription.get(
						MeasurementAttributes.STROKE_COLOR), 
						Boolean.valueOf(false)));
		//create the table
		fieldTable = new FigureTable(new FigureTableModel(l, COLUMN_NAMES));
		fieldTable.getTableHeader().setReorderingAllowed(false);
		fieldTable.setRowHeight(26);
		fieldTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		fieldTable.setCellSelectionEnabled(true);
		fieldTable.setColumnSelectionAllowed(true);
		
		fieldTable.addMouseListener(new MouseAdapter() 
		{
			public void mouseClicked(MouseEvent e) {
				
				int col = fieldTable.getSelectedColumn();
				int row = fieldTable.getSelectedRow();
				Object value = fieldTable.getValueAt(row, col);
				if (e.getClickCount() == 1) {
					if (value instanceof Boolean) {
						toggleValue();
					}
				} else if (e.getClickCount() > 1) {
					e.consume();
					if (value instanceof Color) {
						//Only if the figure is not read only.
						FigureTableModel ftm = (FigureTableModel) 
							fieldTable.getModel();
						ROIFigure figure = ftm.getFigure();
						if (figure != null && !figure.isReadOnly())
							controller.showColorPicker((Color) value);
					} else if (value instanceof Boolean) {
						toggleValue();
					}
				}
			}
		});
	}
	
	/**
	 * Sets the range of values the stroke attribute may take. 
	 * 
	 * @return see above.
	 */
	private List<Double> strokeRange()
	{
		List<Double> sRange = new ArrayList<Double>();
		sRange.add(new Double(0.5));
		sRange.add(new Double(0.75));
		sRange.add(new Double(1));
		sRange.add(new Double(2));
		sRange.add(new Double(3));
		sRange.add(new Double(4));
		return sRange;
	}
	
	/**
	 * Set the range of values the font attribute may take. 
	 * 
	 * @return see above.
	 */
	private List<Double> fontRange()
	{
		List<Double> fRange = new ArrayList<Double>();
		fRange.add(new Double(4));
		fRange.add(new Double(8));
		fRange.add(new Double(10));
		fRange.add(new Double(12));
		fRange.add(new Double(16));
		fRange.add(new Double(24));
		return fRange;
	}
	
	/** Toggles the value of the boolean under the current selection. */
	private void toggleValue()
	{
		int col = fieldTable.getSelectedColumn();
		int row = fieldTable.getSelectedRow();
		Object v = (Boolean) fieldTable.getModel().getValueAt(row, col);
		Boolean value = Boolean.valueOf(false);
		if (v != null) value = (Boolean) v;
		boolean newValue = !(value.booleanValue()); 
		fieldTable.getModel().setValueAt(Boolean.valueOf(newValue), row, col);
		model.getDrawingView().repaint();
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
	 * Returns the selected row.
	 * 
	 * @param c The color to set.
	 */
	int setCellColor(Color c)
	{
		int col = fieldTable.getSelectedColumn();
		int row = fieldTable.getSelectedRow();
		fieldTable.getModel().setValueAt(c, row, col);
		return row;
	}

	/**
	 * Returns <code>true</code> if the text has to be shown, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isShowText()
	{
		if (fieldTable == null) return false;
		int n = fieldTable.getRowCount();
		if (n > 3) {
			Object v = fieldTable.getModel().getValueAt(SHOW_TEXT_ROW, 1);
			if (v == null) return false;
			return (Boolean) v;
		}
		return false;
	}

	/**
	 * Shows or hides the text for the currently selected figure.
	 * 
	 * @param show  Pass <code>true</code> to show the text, <code>false</code>
	 * 				otherwise. 
	 * @param figure The selected figure.
	 */
	void showText(boolean show, ROIFigure figure)
	{
		if (fieldTable == null) return;
		int n = fieldTable.getRowCount();
		if (n > 3) {
			FigureTableModel ftm = (FigureTableModel) 
			fieldTable.getModel();
			ROIFigure f = ftm.getFigure();
			if (f != null && !f.isReadOnly() && f == figure)
				fieldTable.getModel().setValueAt(show, SHOW_TEXT_ROW, 1);
		}
	}
	
	/**
	 * Returns <code>true</code> if the measurement has to be shown, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isShowMeasurement()
	{
		if (fieldTable == null) return false;
		int n = fieldTable.getRowCount();
		if (n > 4) {
			Object v = fieldTable.getModel().getValueAt(SHOW_MEASUREMENT_ROW, 
					1);
			if (v == null) return false;
			return (Boolean) v;
		}
		return false;
	}
	
	
	/**
	 * Sets the data.
	 * 
	 * @param figure The data to set.
	 */
	void setModelData(ROIFigure figure)
	{
		FigureTableModel tableModel = (FigureTableModel) fieldTable.getModel();
		tableModel.setData(figure);
		//fieldTable.setModel(tableModel);
		fieldTable.repaint();
	}
	
	/**
	 * Removes the ROI figure.
	 * 
	 * @param figure The figure to remove.
	 */
	void removeROIFigure(ROIFigure figure)
	{
		if (figure == null) return;
		FigureTableModel tm = (FigureTableModel) fieldTable.getModel();
		ROIFigure value = tm.getFigure();
		if (value == null) return;
		if (value.getROI().getID() == figure.getROI().getID())
			tm.clearData();
	}
	
	/**
	 * Removes the ROI figures.
	 * 
	 * @param figures The figures to remove.
	 */
	void removeROIFigures(List<ROIFigure> figures)
	{
		if (figures == null || figures.size() == 0) return;
		FigureTableModel tm = (FigureTableModel) fieldTable.getModel();
		ROIFigure value = tm.getFigure();
		if (value == null) return;
		Iterator<ROIFigure> i = figures.iterator();
		ROIFigure figure;
		while (i.hasNext()) {
			figure = i.next();
			if (value.getROI().getID() == figure.getROI().getID())
				tm.clearData();
		}
		fieldTable.repaint();
	}
	
	/**
	 * Sets the new figure retrieved from the passed collection.
	 * 
	 * @param l The collection to handle.
	 */
	void setSelectedFigures(List<ROIShape> l)
	{
		FigureTableModel tableModel = (FigureTableModel) fieldTable.getModel();
		Iterator<ROIShape> i = l.iterator();
		//Register error and notify user.
		ROIShape shape;
		try {
			TableCellEditor editor = fieldTable.getCellEditor();
			if (editor != null) editor.stopCellEditing();
			while (i.hasNext()) {
				shape = i.next();
				tableModel.setData(shape.getFigure());
				//fieldTable.setModel(tableModel);
				fieldTable.repaint();
			}
		} catch (Exception e) {
			MeasurementAgent.getRegistry().getLogger().info(this, 
													"Figures selection"+e);
		}
	}
	
}
