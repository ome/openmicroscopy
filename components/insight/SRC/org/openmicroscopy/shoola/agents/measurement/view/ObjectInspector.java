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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;

import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.TextHolderFigure;
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
	
	/** The name of the panel. */
	private static final String			NAME = "Inspector";
	
	/** Text indicating the scaling factor.*/
	private static final String MAGNIFICATION = "The scaling Factor";
	
	/** The table hosting the various fields. */
	private FigureTable					fieldTable;

	/** Reference to the control. */
	private MeasurementViewerControl	controller;
	
	/** Reference to the model. */
	private MeasurementViewerModel		model;

	/** Magnification factor label*/
	private JLabel infoLabel;

	static {
		COLUMN_NAMES = new ArrayList<String>(2);
		COLUMN_NAMES.add("Field");
		COLUMN_NAMES.add("Value");
	}
	
	/** Statically initialize the AttributeFields to be shown;
	 *  The order in the list reflects the order they are shown in the table
         */
        private static final List<AttributeField> attributeFields = new ArrayList<AttributeField>();
        static {
            attributeFields.add(new AttributeField(AnnotationKeys.TAG,
                    AnnotationDescription.annotationDescription
                            .get(AnnotationKeys.TAG), false));
            attributeFields.add(new AttributeField(MeasurementAttributes.TEXT,
                    AnnotationDescription.annotationDescription
                            .get(AnnotationKeys.TEXT), true));
            attributeFields.add(new AttributeField(MeasurementAttributes.FONT_SIZE,
                    AnnotationDescription.annotationDescription
                            .get(MeasurementAttributes.FONT_SIZE), true));
            attributeFields.add(new AttributeField(MeasurementAttributes.SCALE_PROPORTIONALLY,
                    AnnotationDescription.annotationDescription
                            .get(MeasurementAttributes.SCALE_PROPORTIONALLY), false));
            attributeFields.add(new AttributeField(MeasurementAttributes.WIDTH,
                    AnnotationDescription.annotationDescription
                            .get(AnnotationKeys.WIDTH), true));
            attributeFields.add(new AttributeField(MeasurementAttributes.HEIGHT,
                    AnnotationDescription.annotationDescription
                            .get(AnnotationKeys.HEIGHT), true));
            attributeFields.add(new AttributeField(MeasurementAttributes.SHOWTEXT,
                    "Show Comment", false));
            attributeFields.add(new AttributeField(MeasurementAttributes.SHOWMEASUREMENT,
                    AnnotationDescription.annotationDescription
                            .get(MeasurementAttributes.SHOWMEASUREMENT), false));
            attributeFields.add(new AttributeField(MeasurementAttributes.FILL_COLOR,
                    AnnotationDescription.annotationDescription
                            .get(MeasurementAttributes.FILL_COLOR), false));
            attributeFields.add(new AttributeField(MeasurementAttributes.STROKE_COLOR,
                    AnnotationDescription.annotationDescription
                            .get(MeasurementAttributes.STROKE_COLOR), false));
            attributeFields.add(new AttributeField(MeasurementAttributes.STROKE_WIDTH,
                    AnnotationDescription.annotationDescription
                            .get(MeasurementAttributes.STROKE_WIDTH), true));
        }
	
	/** Initializes the component composing the display. */
	private void initComponents()
	{
	    infoLabel = new JLabel(MAGNIFICATION+" is "+
	model.getDrawingView().getScaleFactor());
		//create the table
		fieldTable = new FigureTable(new FigureTableModel(attributeFields,
		        COLUMN_NAMES));
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
	
        private void handleValueChanged(String text) {
            int row = fieldTable.getEditingRow();
    
            if (row < 0 || row >= attributeFields.size()) {
                return;
            }
    
            AttributeKey attr = getAttributeKey(row);
            
            FigureTableModel ftm = (FigureTableModel) fieldTable.getModel();
            ROIFigure figure = ftm.getFigure();
    
            if (attr.equals(MeasurementAttributes.TEXT)) {
                if(TextHolderFigure.class.isAssignableFrom(figure.getClass())) {
                    ((TextHolderFigure) figure).setText(text);
                }
            } else if (attr.equals(MeasurementAttributes.FONT_SIZE)) {
                double d = parseDouble(text);
                figure.setAttribute(MeasurementAttributes.FONT_SIZE, d);
               figure.changed();
            }
            else if (attr.equals(MeasurementAttributes.STROKE_WIDTH)) {
                double d = parseDouble(text);
                figure.setAttribute(MeasurementAttributes.STROKE_WIDTH, d);
            }
            else if (attr.equals(MeasurementAttributes.WIDTH)) {
                try {
                    double d = parseDouble(text);
                    setFigureDimension(figure, MeasurementAttributes.WIDTH, d);
                    if (isScaleProportionally()) {
                        setFigureDimension(figure, MeasurementAttributes.HEIGHT, d);
                    }
                } catch (Exception e) {
                }
            } else if (attr.equals(MeasurementAttributes.HEIGHT)) {
                try {
                    double d = parseDouble(text);
                    setFigureDimension(figure, MeasurementAttributes.HEIGHT, d);
                    if (isScaleProportionally()) {
                        setFigureDimension(figure, MeasurementAttributes.WIDTH, d);
                    }
                } catch (Exception e) {
                }
            }
            model.getDrawingView().repaint();
        }
        
        /**
         * Safe method to parse text into double value
         * 
         * @param text
         *            The text to convert to double
         * @return The parsed double value or 1 if input is invalid or < 1
         */
        private double parseDouble(String text) {
            double result = 1;
            if (text != null && text.trim().length() > 0) {
                try {
                    result = Double.parseDouble(text);
                } catch (NumberFormatException e) {
                }
                if (result < 1) {
                    result = 1;
                }
            }
            return result;
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
			Object v = fieldTable.getModel().getValueAt(getRowIndex(MeasurementAttributes.SHOWTEXT), 1);
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
				fieldTable.getModel().setValueAt(show, getRowIndex(MeasurementAttributes.SHOWTEXT), 1);
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
			Object v = fieldTable.getModel().getValueAt(getRowIndex(MeasurementAttributes.SHOWMEASUREMENT), 
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
			Object v = fieldTable.getModel().getValueAt(getRowIndex(MeasurementAttributes.SCALE_PROPORTIONALLY),
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
	
	/** Updates the display when the magnification factor changes.*/
	void onMagnificationChanged()
	{
	    infoLabel.setText(MAGNIFICATION+" is "+
	model.getDrawingView().getScaleFactor());
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

    /** Clear the inspector after saving the data. */
	void clearData()
	{
	    FigureTableModel tm = (FigureTableModel) fieldTable.getModel();
	    tm.clearData();
	}

	/**
	 * Get the AttributeKey which is handled by a certain row
	 * @param row The row which AttributeKey to get 
	 * @return See above
	 */
	AttributeKey getAttributeKey(int row) {
	    return attributeFields.get(row).getKey();
	}
	
	/**
	 * Get the index of the row which handles a certain attribute 
	 * @param attKey The AttributeKey
	 * @return See above
	 */
        int getRowIndex(AttributeKey attKey) {
            for (int i = 0; i < attributeFields.size(); i++) {
                if (attributeFields.get(i).getKey().equals(attKey)) {
                    return i;
                }
            }
            return -1;
        }
}
