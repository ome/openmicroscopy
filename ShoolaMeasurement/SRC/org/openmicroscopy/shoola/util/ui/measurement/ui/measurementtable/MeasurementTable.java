/*
 * org.openmicroscopy.shoola.util.ui.measurement.ui.MeasurementTable.MeasurementTable 
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
package org.openmicroscopy.shoola.util.ui.measurement.ui.measurementtable;

//Java imports
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.measurement.ui.UIModel;
import org.openmicroscopy.shoola.util.ui.measurement.ui.figures.ROIFigure;
import org.openmicroscopy.shoola.util.ui.measurement.ui.util.UIUtils;
import org.openmicroscopy.shoola.util.ui.roi.model.ROI;
import org.openmicroscopy.shoola.util.ui.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.ui.roi.model.annotation.AnnotationKey;
import org.openmicroscopy.shoola.util.ui.roi.model.annotation.AnnotationKeys;
import org.openmicroscopy.shoola.util.ui.roi.model.util.Coord3D;

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
public class MeasurementTable
	extends JFrame implements ActionListener
{
	private UIModel 						model;
	private JTable							table;
	private MeasurementTableModel 			tableModel;
	
	private JButton							saveBtn;
	private ArrayList<AnnotationField> 		annotationList;
	private JScrollPane						tableScrollPane;
	private JPanel							buttonPanel;
	
	public MeasurementTable(UIModel model)
	{
		this.model = model;
		setWindowParams();
		createUI();
		setAnnotations();
		populateTable();
		setVisible(true);
	}
	
	private void setWindowParams()
	{
		setTitle("Measurement Table");
		setSize(800,300);
	}
	
	private void createTable()
	{
		table = new JTable();
		JPanel tablePanel = new JPanel();
		table = new JTable();
		tableScrollPane = new JScrollPane(table);
		tablePanel.add(tableScrollPane, BorderLayout.CENTER);
		
	}
	
	private void createSaveBtn()
	{
		saveBtn = new JButton("Save Results");
		saveBtn.addActionListener(this);
		
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new BorderLayout());
		buttonPanel.add(saveBtn, BorderLayout.EAST);
	}
	
	private void createUI()
	{
		createTable();
		createSaveBtn();
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(tableScrollPane, BorderLayout.CENTER);
		this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
	}
	
	public void setAnnotations()
	{
		AnnotationField field;
		annotationList = new ArrayList<AnnotationField>();
		field = new AnnotationField(AnnotationKeys.ROIID,"ROI ID", false);
		annotationList.add(field);
		field = new AnnotationField(AnnotationKeys.FIGURETYPE,"Figure Type", false);
		annotationList.add(field);
		field = new AnnotationField(AnnotationKeys.BASIC_TEXT,"Description", false);
		annotationList.add(field);
		field = new AnnotationField(AnnotationKeys.CENTRE,"Centre", false);
		annotationList.add(field);
		field = new AnnotationField(AnnotationKeys.AREA,"Area", false);
		annotationList.add(field);
		field = new AnnotationField(AnnotationKeys.PERIMETER,"Perimeter", false);
		annotationList.add(field);
		field = new AnnotationField(AnnotationKeys.LENGTH,"Length", false);
		annotationList.add(field);
		field = new AnnotationField(AnnotationKeys.ANGLE,"Angle", false);
		annotationList.add(field);
		field = new AnnotationField(AnnotationKeys.STARTPOINT,"Start Point", false);
		annotationList.add(field);
		field = new AnnotationField(AnnotationKeys.ENDPOINT,"End Point", false);
		annotationList.add(field);
	}
	
	private void populateTable()
	{
		tableModel = new MeasurementTableModel();
		createHeadings();
		addROIMeasurements();
		table.setModel(tableModel);
	}
	
	private void createHeadings()
	{
		tableModel.addColumn("Time Point");
		tableModel.addColumn("Z Section");
		for(int i = 0 ; i < annotationList.size(); i++)
		{
			tableModel.addColumn(annotationList.get(i).fieldName);
		}
		
	}
	
	private void addROIMeasurements()
	{
		TreeMap<Long, ROI> roiMap = model.getROIMap();
    	Iterator keyIterator = roiMap.keySet().iterator();
    	
    	while(keyIterator.hasNext())
    		addRow(roiMap.get((Long)keyIterator.next()));

	}
	
	private void addRow(ROI roi)
	{
    	TreeMap<Coord3D, ROIShape> shapes = roi.getShapes();
		Iterator roiShapeIterator = shapes.keySet().iterator();
		if(!roiShapeIterator.hasNext())
			return;
		ROIShape shape = shapes.get((Coord3D)roiShapeIterator.next());
		ROIFigure figure = shape.getFigure();
		figure.calculateMeasurements();
		TableRow row = new TableRow();
		row.add(shape.getCoord3D().getTimePoint());
		row.add(shape.getCoord3D().getZSection());
		for( int i = 0 ; i < annotationList.size() ; i++)
		{
			AnnotationKey key = annotationList.get(i).key;
			row.add(key.get(shape));
		}
		tableModel.addRow(row);
	}

	private void saveResults()
	{
		
		JFileChooser fileChooser = new JFileChooser();
		int returnValue = fileChooser.showSaveDialog(this);
		if(returnValue != JFileChooser.APPROVE_OPTION)
			return;
		File file = fileChooser.getSelectedFile();
		PrintWriter outputStream = null;
	    try 
	    {
	    	outputStream = 
	    	new PrintWriter(new FileWriter(file));
	    	for( int col = 0 ; col < tableModel.getColumnCount(); col++)
	    		outputStream.print(tableModel.getColumnName(col)+",");
	    	outputStream.println();
	    	for( int row = 0 ; row < tableModel.getRowCount() ; row++)
	    	{
	    		for(int col = 0; col < tableModel.getColumnCount() ; col++)
	    		{
	    			Object value = tableModel.getValueAt(row, col);
	    			if(value instanceof String)
	    				outputStream.print(tableModel.getValueAt(row, col)+",");
	    			else if( value instanceof Point2D.Double)
	    			{
	    				Point2D pt = (Point2D)value;
	    				outputStream.print("("+pt.getX()+";"+pt.getY()+"),");
	    			} 
	    			else if (value instanceof Double)
	    			{
	    				NumberFormat formatter = new DecimalFormat("###.#");
	    				String formattedValue = formatter.format(value);
	    				outputStream.print(formattedValue+",");
	    			}
	    			else if (value instanceof Integer)
		    		{
		    			outputStream.print(value+",");
		    		}
	    			else if (value instanceof Long)
			    	{
			    		outputStream.print(value+",");
		    		}
	    			else if(value == null)
		    				continue;
	    		}
	    		outputStream.println();
	    	}
	   
	    } catch (IOException exception) 
	    {
				// TODO Auto-generated catch block
				exception.printStackTrace();
		} 
		finally 
		{
			if (outputStream != null) 
				outputStream.close();
		}
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) 
	{
		saveResults();
	}
	
}


