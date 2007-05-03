/*
 * measurement.ui.objectinspector.ObjectInspector 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.ui.measurement.ui.objectinspector;


//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

//Third-party libraries
import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.FigureEvent;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.colourpicker.*;
import org.openmicroscopy.shoola.util.ui.measurement.model.DrawingAttributes;
import org.openmicroscopy.shoola.util.ui.measurement.model.DrawingEventList;
import org.openmicroscopy.shoola.util.ui.measurement.ui.UIControl;
import org.openmicroscopy.shoola.util.ui.measurement.ui.UIModel;
import org.openmicroscopy.shoola.util.ui.measurement.ui.util.ExceptionHandler;
import org.openmicroscopy.shoola.util.ui.roi.model.ROI;
import org.openmicroscopy.shoola.util.ui.roi.model.ROIShape;


/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ObjectInspector
	extends JFrame	
	implements PropertyChangeListener
{
	private JScrollPane				scrollPane;
	private	FigureModel				figureModel;
	private	FieldTable				fieldTable;
	private ListSelectionModel 		rowSM;
	private ListSelectionListener 	listener;
	private UIControl				control;
	private UIModel					model;
	private ColourRenderer			colourRenderer;
	private int 					selectedRow;
	private int						selectedColumn;

	
	public ObjectInspector(UIModel model, UIControl control)
	{
		this.model = model;
		this.control = control;
		createUI();
	}

	private void createUI()
	{
	this.setTitle("Object Inspector");
	this.setSize(new Dimension(350, 300));
	this.setLayout(new BorderLayout());
	createTableModel();
	createTable();
	scrollPane = new JScrollPane(fieldTable);
	this.getContentPane().add(scrollPane, BorderLayout.CENTER);
	}

	public void createTableModel()
	{
		ArrayList<AttributeField> fieldList = new ArrayList<AttributeField>();
		//fieldList.add(new AttributeField(ROIAttributes.ROIID, "ID", false));
		//fieldList.add(new AttributeField(DrawingAttributes.FIGURETYPE, "Shape Type", false));
		//fieldList.add(new AttributeField(DrawingAttributes.TEXT, "Text", true));
		fieldList.add(new AttributeField(AttributeKeys.TEXT_COLOR, "Text Colour", false));
		fieldList.add(new AttributeField(AttributeKeys.FILL_COLOR, "Fill Colour", false));
		fieldList.add(new AttributeField(AttributeKeys.STROKE_COLOR, "Line Colour", false));
		fieldList.add(new AttributeField(DrawingAttributes.MEASUREMENTTEXT_COLOUR, "Measurement Text Colour", false));
		fieldList.add(new AttributeField(AttributeKeys.STROKE_WIDTH, "Line Width", true));
		
		
		figureModel = new FigureModel(fieldList);
		figureModel.addColumn("Field");
		figureModel.addColumn("Value");
	}
	
	private void createTable()
	{
		fieldTable = new FieldTable(figureModel);
		fieldTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		fieldTable.setCellSelectionEnabled(true);
		fieldTable.setColumnSelectionAllowed(true);
//		Ask to be notified of selection changes.
		
		rowSM = fieldTable.getSelectionModel();
		listener = new ListSelectionListener() {
		    public void valueChanged(ListSelectionEvent e) {
		        //Ignore extra messages.
		        if (e.getValueIsAdjusting()) return;

		        ListSelectionModel lsm =
		            (ListSelectionModel)e.getSource();
		        if (lsm.isSelectionEmpty()) {
		        } else {
		            objectFieldSelected(fieldTable.getSelectedRow(),fieldTable.getSelectedColumn());
		        }
		    }
		};
		rowSM.addListSelectionListener(listener);
	}
	
	public void objectFieldSelected(int row, int col)
	{
		selectedRow = row;
		selectedColumn = col;
		if(fieldTable.getModel().getValueAt(row, col) instanceof Color)
		{
			ColourPicker colourPicker = new ColourPicker(this, (Color)fieldTable.getModel().getValueAt(row, col));
			colourPicker.addPropertyChangeListener(this);
			colourPicker.setVisible(true);
		}
	}
	
	/* (non-Javadoc)
	 * 	@see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent propertyChangeEvent) 
	{
		if(propertyChangeEvent.getPropertyName() == ColourPicker.COLOUR_PROPERTY)
		{
			fieldTable.getModel().setValueAt(propertyChangeEvent.getNewValue(), selectedRow, selectedColumn);
		}
		
		if(propertyChangeEvent.getPropertyName() == DrawingEventList.UIMODEL_FIGUREATTRIBUTECHANGED)
		{
			figureModel.setModelData((Figure)((FigureEvent)propertyChangeEvent.getNewValue()).getFigure());
			fieldTable.setModel(figureModel);
			fieldTable.repaint();
		}
		
		if(propertyChangeEvent.getPropertyName() == 
									DrawingEventList.UIMODEL_FIGURESELECTED)
		{
			Collection<ROI> selectionList = (Collection<ROI>)propertyChangeEvent.getNewValue();
			Iterator roiIterator = selectionList.iterator();
			while(roiIterator.hasNext())
			{
				try
				{
					ROI roi = ((ROI)roiIterator.next());
					ROIShape shape = roi.getShape(model.getCoord3D());
					Figure fig = shape.getFigure();
					figureModel.setModelData(fig);
					fieldTable.setModel(figureModel);
					fieldTable.repaint();
				}
				catch(Exception e)
				{
					ExceptionHandler.get().handleException(e);
				}
			}
		}	
	}
}


