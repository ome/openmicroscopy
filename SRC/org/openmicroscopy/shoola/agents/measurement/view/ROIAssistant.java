/*
 * org.openmicroscopy.shoola.agents.measurement.view.ROIAssistant 
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
import java.awt.Dimension;
import java.awt.Point;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.IconManager;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys;
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

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
 * @param <MeasurementViewerModel>
 * @since OME3.0
 */
public class ROIAssistant
	extends JDialog
{	
	/** 
	 * The table showing the ROI and allowing the user to propagate the selected
	 * ROI through time and Z-section. 
	 */	
	private 	ROIAssistantTable		table;
	
	/**
	 * The model which will define the ROI's displayed in the table.
	 */
	private 	ROIAssistantModel 		model;
	
	/** Text field showing the current type of the selected shape. */
	private 	JTextField				shapeType;
	
	/** Text field showing the x coord of the selected shape. */
	private 	JTextField				xCoord;
	
	/** Text field showing the y coord of the selected shape. */
	private 	JTextField				yCoord;
	
	/** Text field showing the width of the selected shape. */
	private 	JTextField				width;
	
	/** Text field showing the height of the selected shape. */
	private 	JTextField				height;
	
	/** Text field showing the description of the selected shape. */
	private 	JTextField 				description;
	
	/** Checkbox which is selected if the user has selected to add an ROI. */
	private 	JCheckBox 				addButton;
	
	/** Checkbox which is selected if the user has selected to remove an ROI. */
	private 	JCheckBox				removeButton;
	
	/** Listener for the selection of cells in the table. */
	private 	ListSelectionListener	listener;
	
	/** The scroll pane of the Table. */
	private 	JScrollPane				scrollPane;
	
	/** Model for the measyrement tool. */
	private 	MeasurementViewerUI		view;
	private 	Coord3D					currentPlane;
	private 	ROIShape 				initialShape;
	/**
	 * Constructor for the ROIAssistant Dialog.
	 * @param numRow The number of z sections in the image. 
	 * @param numCol The numer of time points in the image. 
	 * @param selectedROI The ROI which will be propagated.
	 */
	public ROIAssistant(int numRow, int numCol, Coord3D currentPlane, 
						ROI selectedROI, MeasurementViewerUI view)
	{
		this.view = view;
		initialShape = null;
		this.currentPlane = currentPlane;
		this.setAlwaysOnTop(true);
		this.setModal(true);
		createTable(numRow, numCol,currentPlane, selectedROI);
		buildUI();

		JViewport viewPort = scrollPane.getViewport();
		Point point = mapCoordToCell(currentPlane);
		int x = (int)Math.max((point.getX()-6*ROIAssistantTable.COLUMNWIDTH), 0);
		int y = (int)Math.max((point.getY()-6*ROIAssistantTable.COLUMNWIDTH),0);
		viewPort.setViewPosition(new Point(x, y));
	}
	
	public void setVisible(boolean isVisible)
	{
		super.setVisible(isVisible);
	}
	
	
	private Point mapCoordToCell(Coord3D coord)
	{
		int x = coord.getTimePoint()*ROIAssistantTable.COLUMNWIDTH+
		ROIAssistantTable.LEADERCOLUMN_WIDTH; 
		int y = coord.getZSection()*ROIAssistantTable.COLUMNWIDTH;
		Point pt = new Point(x, y);
		return pt;
	}
	
	/** Create the UI for the Assistant. */
	private void buildUI()
	{
		layoutUI();
	}
	
	/**
	 * Create the table and model.
	 *  
	 * @param numRow The number of z sections in the image. 
	 * @param numCol The numer of time points in the image. 
	 * @param selectedROI The ROI which will be propagated.
	 */
	private void createTable(int numRow, int numCol, Coord3D currentPlane, ROI selectedROI)
	{
		model = new ROIAssistantModel(numRow, numCol, currentPlane, selectedROI);
		table = new ROIAssistantTable(model);
		listener = new ListSelectionListener() {
			
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) return;

		        ListSelectionModel lsm =
		            (ListSelectionModel) e.getSource();
		        if (lsm.isSelectionEmpty()) {
		        } else {
		        	int index = lsm.getMinSelectionIndex();
		        	ROIAssistantModel m = 
	        			(ROIAssistantModel) table.getModel();
		        	//TODO: something
	        	}
			}
		
		};
		

		table.addMouseListener(new java.awt.event.MouseAdapter() 
		{
			public void mousePressed(java.awt.event.MouseEvent e) 
			{
				int col = table.getSelectedColumn();
				int row = table.getSelectedRow();
				Object value = table.getShapeAt(row, col);
				if(value instanceof ROIShape)
				{
					ROIShape shape = (ROIShape)value;
					initialShape = shape;
					shapeType.setText(shape.getFigure().getType());
					description.setText((String)shape.getAnnotation(AnnotationKeys.BASIC_TEXT));
					xCoord.setText(shape.getFigure().getStartPoint().getX()+"");
					yCoord.setText(shape.getFigure().getStartPoint().getY()+"");
					width.setText(Math.abs(shape.getFigure().getEndPoint().getX()-shape.getFigure().getStartPoint().getX())+"");
					height.setText(Math.abs(shape.getFigure().getEndPoint().getY()-shape.getFigure().getStartPoint().getY())+"");
				}
				else if(value == null)
				{
					
				}
					
			}
			
			public void mouseReleased(java.awt.event.MouseEvent e) 
			{
				if(initialShape==null)
					return;
				int[] col = table.getSelectedColumns();
				int[] row = table.getSelectedRows();
				for(int i = 0 ; i < row.length ; i++)
					row[i] = (table.getRowCount()-row[i])-1;
				
				int mincol = col[0];
				int maxcol = col[0];
				int minrow = row[0];
				int maxrow = row[0];
								
				for( int i = 0 ; i < col.length; i++)
				{
					mincol = Math.min(mincol, col[i]);
					maxcol = Math.max(maxcol, col[i]);
				}
				for( int i = 0 ; i < row.length; i++)
				{
					minrow = Math.min(minrow, row[i]);
					maxrow = Math.max(maxrow, row[i]);
				}
				maxcol = maxcol-1;
				mincol = mincol-1;
				int boundrow;
				int boundcol;
				if(maxcol!=initialShape.getCoord3D().getTimePoint())
					boundcol = maxcol;
				else
					boundcol = mincol;
				if(maxrow!=initialShape.getCoord3D().getZSection())
					boundrow = maxrow;
				else
					boundrow = minrow;
				try
				{
					if(initialShape != null)
					{
						if(addButton.isSelected())
							view.propagateShape(initialShape, boundcol, boundrow);
						if(removeButton.isSelected())
							view.deleteShape(initialShape, boundcol, boundrow);
						initialShape=null;
						table.repaint();
					}
				}
				catch(Exception exception)
				{
					exception.printStackTrace();
				}
					
			}
		});
	}
	
	/**
	 * The action panel is the panel which holds the buttons to choose the 
	 * action to perform on the ROI. 
	 * 
	 * @return the action panel.
	 */
	private JPanel createActionPanel()
	{
		JPanel actionPanel = new JPanel();
		addButton = new JCheckBox("Add ROI");
		removeButton = new JCheckBox("Remove ROI");
		ButtonGroup group = new ButtonGroup();
		addButton.setSelected(true);
		group.add(addButton);
		group.add(removeButton);
		actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
		actionPanel.add(addButton);
		actionPanel.add(removeButton);
		return actionPanel;
	}
	
	/**
	 * The info panel at the top the the dialog, showing a little text about the
	 * ROI Assistant. 
	 * @return the info panel.
	 */
	private JPanel createInfoPanel()
	{
		JPanel infoPanel = new TitlePanel("ROI Assistant", "<html><body>This is " +
				"the ROI Assistant. It allows you to create an ROI which extends<p> " +
				"through time and z-sections.</body></html>", 
				IconManager.getInstance().getIcon(IconManager.WIZARD));
		return infoPanel;
	}
	
	/** 
	 * Create the shape panel which shows the parameters of the initial shape. 
	 * @return the shapePanel. 
	 */
	private JPanel createShapePanel()
	{
		JPanel shapePanel = new JPanel();
		shapeType = new JTextField();
		description = new JTextField();
		xCoord = new JTextField();
		yCoord = new JTextField();
		width = new JTextField();
		height = new JTextField();
		JLabel shapeTypeLabel = new JLabel("Shape Type ");
		JLabel xCoordLabel = new JLabel("X Coord");
		JLabel yCoordLabel = new JLabel("Y Coord");
		JLabel widthLabel = new JLabel("Width");
		JLabel heightLabel = new JLabel("Height");
		JLabel descriptionLabel = new JLabel("Description");
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(createLabelText(shapeTypeLabel, shapeType));
		panel.add(Box.createVerticalStrut(5));
		panel.add(createLabelText(descriptionLabel, description));
		
		JPanel panel2 = new JPanel();
		panel2.setLayout(new BoxLayout(panel2, BoxLayout.Y_AXIS));
		panel2.add(createLabelText(xCoordLabel, xCoord));
		panel2.add(Box.createVerticalStrut(5));
		panel2.add(createLabelText(yCoordLabel, yCoord));
		
		JPanel panel3 = new JPanel();
		panel3.setLayout(new BoxLayout(panel3, BoxLayout.Y_AXIS));
		panel3.add(createLabelText(widthLabel, width));
		panel3.add(Box.createVerticalStrut(5));
		panel3.add(createLabelText(heightLabel, height));
		
		shapePanel.setLayout(new BoxLayout(shapePanel, BoxLayout.X_AXIS));
		shapePanel.add(panel);
		shapePanel.add(Box.createHorizontalStrut(10));
		shapePanel.add(panel2);
		shapePanel.add(Box.createHorizontalStrut(10));
		shapePanel.add(panel3);
		
		return shapePanel;
	}
	
	/** 
	 * Create a panel with label and textfield.
	 * @param l label
	 * @param t textfield
	 * @return see above.
	 */
	JPanel createLabelText(JLabel l, JTextField t)
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(l);
		panel.add(t);
		UIUtilities.setDefaultSize(l, new Dimension(80,22));
		UIUtilities.setDefaultSize(t, new Dimension(80,22));
		return panel;
	}
	
	/** Layout the UI, adding panels to the form. */
	private void layoutUI()
	{
		this.setSize(550,530);
		JPanel panel = new JPanel();
		JPanel infoPanel = createInfoPanel();
		JPanel shapePanel = createShapePanel();

		scrollPane = new JScrollPane(table);
		scrollPane.setVerticalScrollBar(scrollPane.createVerticalScrollBar());
		scrollPane.setHorizontalScrollBar(scrollPane.createHorizontalScrollBar());
		
		JPanel scrollPanel = new JPanel();
		scrollPanel.setLayout(new BoxLayout(scrollPanel, BoxLayout.X_AXIS));
		scrollPanel.add(Box.createHorizontalStrut(10));
		scrollPanel.add(scrollPane);
		scrollPanel.add(Box.createHorizontalStrut(10));
		scrollPanel.add(createActionPanel());
		scrollPanel.add(Box.createHorizontalStrut(10));
		
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		panel.add(infoPanel);
		panel.add(Box.createVerticalStrut(10));
		panel.add(scrollPanel);
		panel.add(Box.createVerticalStrut(10));
		panel.add(shapePanel);
		panel.add(Box.createVerticalStrut(10));
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(panel, BorderLayout.CENTER);
	}


}


