/*
 * org.openmicroscopy.shoola.agents.measurement.view.ObjectInspector 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;

import org.jhotdraw.draw.AttributeKey;
import org.openmicroscopy.shoola.agents.measurement.IconManager;
import org.openmicroscopy.shoola.agents.measurement.MeasurementAgent;
import org.openmicroscopy.shoola.agents.measurement.util.model.AnnotationDescription;
import org.openmicroscopy.shoola.agents.measurement.util.model.AttributeField;
import org.openmicroscopy.shoola.agents.measurement.util.model.FigureTableModel;
import org.openmicroscopy.shoola.agents.measurement.util.ui.FigureTable;
import org.openmicroscopy.shoola.util.roi.figures.MeasureBezierFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureEllipseFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureLineConnectionFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureLineFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasurePointFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureRectangleFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureTextFigure;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys;
import org.openmicroscopy.shoola.util.roi.model.annotation.MeasurementAttributes;


/** 
 * UI Component displaying various drawing information about a 
 * Region of Interest.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
class ObjectInspector
	extends JPanel
{
	
	/** Index to identify tab */
	public final static int		INDEX = MeasurementViewerUI.INSPECTOR_INDEX;

	/** Collection of column names. */
	private static final List<String>			COLUMN_NAMES;
	
	/** The row indicating to show the text or not. */
	private static final int TEXT_ROW = 0;
	
	/** The row indicating if to update the figure width and height together. */
	private static final int SCALE_PROPORTIONALLY_ROW = 1;
	
	/** The row hosting the figure width. */
	private static final int WIDTH_ROW = 2;
	
	/** The row hosting the figure height. */
	private static final int HEIGHT_ROW = 3;
	
	/** The row indicating to show the text or not. */
	private static final int SHOW_TEXT_ROW = 4;
	
	/** The row indicating to show the measurement or not. */
	private static final int SHOW_MEASUREMENT_ROW = 5;
	
	/** The row hosting the fill color. */
	static final int FILL_COLOR_ROW = 6;
	
	/** The row hosting the line color. */
	static final int LINE_COLOR_ROW = 7;
	
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
				AnnotationKeys.TEXT), true));
		l.add(new AttributeField(MeasurementAttributes.SCALE_PROPORTIONALLY,
				AnnotationDescription.annotationDescription.get(
						MeasurementAttributes.SCALE_PROPORTIONALLY),
						false));
		l.add(new AttributeField(MeasurementAttributes.WIDTH, 
				AnnotationDescription.annotationDescription.get(
				AnnotationKeys.WIDTH), true));
		l.add(new AttributeField(MeasurementAttributes.HEIGHT, 
				AnnotationDescription.annotationDescription.get(
				AnnotationKeys.HEIGHT), true));
		l.add(new AttributeField(MeasurementAttributes.SHOWTEXT, "Show Comment", 
				false));
		l.add(new AttributeField(MeasurementAttributes.SHOWMEASUREMENT, 
				AnnotationDescription.annotationDescription.get(
						MeasurementAttributes.SHOWMEASUREMENT), 
						false));
		l.add(new AttributeField(MeasurementAttributes.FILL_COLOR, 
				AnnotationDescription.annotationDescription.get(
						MeasurementAttributes.FILL_COLOR), 
						false));
		l.add(new AttributeField(MeasurementAttributes.STROKE_COLOR, 
				AnnotationDescription.annotationDescription.get(
						MeasurementAttributes.STROKE_COLOR), 
						false));
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
							if (figure.canEdit())
								controller.showColorPicker((Color) value);
					} else if (value instanceof Boolean) {
						toggleValue();
					}
				}
			}
		});
		fieldTable.addPropertyChangeListener(new PropertyChangeListener() {
			
			public void propertyChange(PropertyChangeEvent evt) {
				String name = evt.getPropertyName();
				if (FigureTable.VALUE_CHANGED_PROPERTY.equals(name)) {
					handleValueChanged((String) evt.getNewValue());
				}
			}
		});
	}
	
	private void handleValueChanged(String text)
	{
		int row = fieldTable.getEditingRow();
		FigureTableModel ftm = (FigureTableModel) 
		fieldTable.getModel();
		ROIFigure figure = ftm.getFigure();
		switch (row) {
			case TEXT_ROW:
				if (figure instanceof MeasureLineConnectionFigure){
					((MeasureLineConnectionFigure) figure).setText(text);
				} else if (figure instanceof MeasureLineFigure){
					((MeasureLineFigure) figure).setText(text);
				} else if (figure instanceof MeasureEllipseFigure){
					((MeasureEllipseFigure) figure).setText(text);
				} else if (figure instanceof MeasureRectangleFigure){
					((MeasureRectangleFigure) figure).setText(text);
				} else if (figure instanceof MeasureBezierFigure){
					((MeasureBezierFigure) figure).setText(text);
				} else if (figure instanceof MeasurePointFigure){
					((MeasurePointFigure) figure).setText(text);
				} else if (figure instanceof MeasureTextFigure){
					((MeasureTextFigure) figure).setText(text);
				}
				break;
			case WIDTH_ROW:
				try {
					double d = Double.parseDouble(text);
					setFigureDimension(figure, MeasurementAttributes.WIDTH, d);
					if (isScaleProportionally()) {
						setFigureDimension(figure, MeasurementAttributes.HEIGHT,
								d);
					}
				} catch (Exception e) {
				}
				break;
			case HEIGHT_ROW:
				try {
					double d = Double.parseDouble(text);
					setFigureDimension(figure, MeasurementAttributes.HEIGHT, d);
					if (isScaleProportionally()) {
						setFigureDimension(figure, MeasurementAttributes.WIDTH,
								d);
					}
				} catch (Exception e) {
				}
				break;
		}
		model.getDrawingView().repaint();
	}
	
	private void setFigureDimension(ROIFigure figure, AttributeKey key,
			double dimension) {
		if (figure instanceof MeasureEllipseFigure ||
			figure instanceof MeasureRectangleFigure ||
			figure instanceof MeasureBezierFigure ||
			figure instanceof MeasureTextFigure ||
			figure instanceof MeasureLineConnectionFigure ||
			figure instanceof MeasureLineFigure) {
			figure.setAttribute(key, dimension);
		}
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
		if (n > 4) {
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
		if (n > 4) {
			FigureTableModel ftm = (FigureTableModel) 
			fieldTable.getModel();
			ROIFigure f = ftm.getFigure();
			if (f != null && f == figure)
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
		if (n > 5) {
			Object v = fieldTable.getModel().getValueAt(SHOW_MEASUREMENT_ROW, 
					1);
			if (v == null) return false;
			return (Boolean) v;
		}
		return false;
	}
	
	/**
	 * Returns <code>true</code> if the figure dimensions are to be scaled
	 * proportionally, <code>false</code> otherwise.
	 *
	 * @return See above.
	 */
	boolean isScaleProportionally() {
		if (fieldTable == null) return false;
		int n = fieldTable.getRowCount();
		if (n > 1) {
			Object v = fieldTable.getModel().getValueAt(SCALE_PROPORTIONALLY_ROW,
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
				fieldTable.repaint();
			}
		} catch (Exception e) {
			MeasurementAgent.getRegistry().getLogger().info(this, 
													"Figures selection"+e);
		}
	}
	
}
