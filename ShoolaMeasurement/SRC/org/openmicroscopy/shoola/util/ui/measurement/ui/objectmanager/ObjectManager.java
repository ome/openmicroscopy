/*
 * measurement.ui.ObjectManager 
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
package org.openmicroscopy.shoola.util.ui.measurement.ui.objectmanager;


//Java imports
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

// Third-party Libraries 
import org.jhotdraw.draw.DrawingEvent;
import org.jhotdraw.draw.Figure;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.measurement.model.DrawingEventList;
import org.openmicroscopy.shoola.util.ui.measurement.ui.UIControl;
import org.openmicroscopy.shoola.util.ui.measurement.ui.UIModel;
import org.openmicroscopy.shoola.util.ui.measurement.ui.util.ExceptionHandler;
import org.openmicroscopy.shoola.util.ui.roi.exception.NoSuchShapeException;
import org.openmicroscopy.shoola.util.ui.roi.model.ROI;

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
public class ObjectManager
	extends JFrame	
	implements PropertyChangeListener
{
	private	JTable					objects;
	private ObjectModel 			tableModel;
	private JScrollPane				scrollPane;
	private ListSelectionModel 		rowSM;
	private ListSelectionListener 	listener;
	private boolean					fireEvent;
	private UIControl				control;
	private UIModel					model;
	
	public ObjectManager(UIModel model, UIControl control)
	{
		this.model = model;
		this.control = control;
		createUI();
	}
	
	private void createUI()
	{
		this.setTitle("Object Manager");
		this.setSize(new Dimension(350, 300));
		this.setLayout(new BorderLayout());
		createTableModel();
		createTable();
		scrollPane = new JScrollPane(objects);
		this.getContentPane().add(scrollPane, BorderLayout.CENTER);
		fireEvent = true;
	}

	private void createTableModel()
	{
		tableModel = new ObjectModel();
		tableModel.addColumn("ID");
		tableModel.addColumn("Shape");
		tableModel.addColumn("Text");
		tableModel.addColumn("Visible");
	}
	
	private void createTable()
	{
		objects = new JTable(tableModel);
		objects.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		objects.setRowSelectionAllowed(true);
		
//		Ask to be notified of selection changes.
		rowSM = objects.getSelectionModel();
		listener = new ListSelectionListener() {
		    public void valueChanged(ListSelectionEvent e) {
		        //Ignore extra messages.
		        if (e.getValueIsAdjusting()) return;

		        ListSelectionModel lsm =
		            (ListSelectionModel)e.getSource();
		        if (lsm.isSelectionEmpty()) {
		        } else {
		            objectIDSelected(lsm.getMinSelectionIndex());
		        }
		    }
		};
		rowSM.addListSelectionListener(listener);
	}
	

	private void objectIDSelected(int index)
	{
		if(fireEvent)
			control.selectFigure((Figure)tableModel.getRow(index));
	}
		
	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public  void propertyChange(PropertyChangeEvent propertyChangeEvent) 
	{
		if(propertyChangeEvent.getPropertyName() == DrawingEventList.UIMODEL_FIGUREATTRIBUTECHANGED)
		{
			objects.repaint();
		}
			
		
		if(propertyChangeEvent.getPropertyName() == DrawingEventList.UIMODEL_FIGUREADDED)
		{
			Collection<ROI> roiList = (Collection<ROI>)propertyChangeEvent.getNewValue();
			Iterator<ROI> roiIterator = roiList.iterator();
			
			while(roiIterator.hasNext())
			{
				ROI roi = roiIterator.next();
				try 
				{
					tableModel.addRow(roi.getFigure(model.getCoord3D()));
				} 
				catch (NoSuchShapeException e) 
				{
					ExceptionHandler.get().handleException(e);
				}
				
			}
			objects.setModel(tableModel);
			objects.repaint();
		}

		if(propertyChangeEvent.getPropertyName() == DrawingEventList.UIMODEL_FIGUREREMOVED)
		{
			DrawingEvent event = (DrawingEvent) propertyChangeEvent.getNewValue();
			tableModel.removeRow(event.getFigure());
			objects.setModel(tableModel);
			objects.repaint();
		}

		if(propertyChangeEvent.getPropertyName() == DrawingEventList.UIMODEL_FIGURESELECTED)
		{
			Collection<ROI> selectionList = (Collection<ROI>) 
											propertyChangeEvent.getNewValue();
			fireEvent = false;
			rowSM.clearSelection();
			Iterator<ROI> roiIterator = selectionList.iterator();
			
			while(roiIterator.hasNext())
			{
				try
				{
					ROI roi = roiIterator.next();
					Figure fig = roi.getFigure(model.getCoord3D());
					objects.addRowSelectionInterval(
						tableModel.getRowFromFigure(fig),
						tableModel.getRowFromFigure(fig));
				}
				catch(Exception e)
				{
					ExceptionHandler.get().handleException(e);
				}
			}
			objects.repaint();
			fireEvent = true;
		}
		
	}
	
}


