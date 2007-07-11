/*
 * org.openmicroscopy.shoola.agents.measurement.view.GraphPane 
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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.measurement.IconManager;
import org.openmicroscopy.shoola.agents.measurement.util.AnalysisStatsWrapper;
import org.openmicroscopy.shoola.agents.measurement.util.AnalysisStatsWrapper.StatsType;
import org.openmicroscopy.shoola.env.rnd.roi.ROIShapeStats;
import org.openmicroscopy.shoola.util.math.geom2D.PlanePoint2D;
import org.openmicroscopy.shoola.util.roi.figures.LineAnnotationFigure;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.graphutils.HistogramPlot;
import org.openmicroscopy.shoola.util.ui.graphutils.LinePlot;
import org.openmicroscopy.shoola.util.ui.graphutils.ScatterPlot;

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
class IntensityView
	extends JPanel 
	implements ActionListener
{
	
	/** The name of the panel. */
	private static final String			NAME = "Intensity View";
	
		/** Reference to the control. */
	private MeasurementViewerControl	controller;
	
	/** Reference to the model. */
	private MeasurementViewerModel		model;

	/** The map of <ROIShape, ROIStats> .*/
	private Map							ROIStats;
	
	/** Table Model. */
	private IntensityModel			tableModel;
	
	/** Table view. */
	private IntensityTable 				table;
	
	/** Min intensity label. */
	private JLabel						minLabel;
	
	/** Max intensity label. */
	private JLabel						maxLabel;
	
	/** Mean intensity label. */
	private JLabel						meanLabel;
	
	/** stdDev. intensity label. */
	private JLabel						stdDevLabel;
	
	/** Min intensity textfield. */
	private JTextField					minValue;
	
	/** Max intensity textfield. */
	private JTextField					maxValue;
	
	/** Mean intensity textfield. */
	private JTextField					meanValue;
	
	/** stdDev. intensity textfield. */
	private JTextField					stdDevValue;
	
	/** Select to choose the channel to show values for . */
	private JComboBox 					channelSelection;
	
	/** list of the channel names. */
	private List<String> channelName = new ArrayList<String>();
	
	/** List of the channel colours. */
	private 	List<Color> channelColour = new ArrayList<Color>();
	
	/** Map of the channel mins, for each selected channel. */
	private Map<Integer, Double> channelMin = new HashMap<Integer, Double>();
	
	/** Map of the channel Max, for each selected channel. */
	private Map<Integer, Double> channelMax = new HashMap<Integer, Double>();
	
	/** Map of the channel Mean, for each selected channel. */
	private Map<Integer, Double> channelMean = new HashMap<Integer, Double>();
	
	/** Map of the channel std. dev., for each selected channel. */
	private Map<Integer, Double> channelStdDev = new HashMap<Integer, Double>();
	
	/** Map of the channel Intensities, for each selected channel. */
	private Map<Integer, Map<PlanePoint2D, Double>> planePixels = 
						new HashMap<Integer, Map<PlanePoint2D, Double>>();

	/** Current ROIShape. */
	private 	ROIShape shape;
	
	/** Initializes the component composing the display. */
	private void initComponents()
	{
		Double data[][] = new Double[1][1];
		tableModel = new IntensityModel(data);
		table = new IntensityTable(tableModel);
		minLabel = new JLabel("Min");
		maxLabel = new JLabel("Max");
		meanLabel = new JLabel("Mean");
		stdDevLabel = new JLabel("Std Dev.");
		minValue = new JTextField();
		maxValue = new JTextField();
		meanValue = new JTextField();
		stdDevValue = new JTextField();
		channelSelection = new JComboBox();
		channelSelection.addActionListener(this);
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		JPanel tPanel = tablePanel();
		tPanel.setAlignmentY(JPanel.TOP_ALIGNMENT);
		add(tPanel);
		JPanel fPanel = fieldPanel();
		fPanel.setAlignmentY(JPanel.TOP_ALIGNMENT);
		add(fPanel);
	}
	
	private JPanel tablePanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setVerticalScrollBar(scrollPane.createVerticalScrollBar());
		scrollPane.setHorizontalScrollBar(
				scrollPane.createHorizontalScrollBar());
		panel.add(scrollPane);
		return panel;
	}
	
	private JPanel fieldPanel()
	{
		JPanel panel = new JPanel();
		JPanel fields;
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(channelSelection);
		panel.add(Box.createVerticalStrut(5));
		fields = createLabelText(minLabel, minValue);
		panel.add(fields);
		panel.add(Box.createVerticalStrut(5));
		fields = createLabelText(maxLabel, maxValue);
		panel.add(fields);
		panel.add(Box.createVerticalStrut(5));
		fields = createLabelText(meanLabel, meanValue);
		panel.add(fields);
		panel.add(Box.createVerticalStrut(5));
		fields = createLabelText(stdDevLabel, stdDevValue);
		panel.add(fields);
		Dimension minSize = new Dimension(5, 1000);
		Dimension prefSize = new Dimension(5, 1000);
		Dimension maxSize = new Dimension(100,Short.MAX_VALUE);
		panel.add(new Box.Filler(minSize, prefSize, maxSize));
		return panel;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param controller Reference to the Control. Mustn't be <code>null</code>.
	 * @param model		 Reference to the Model. Mustn't be <code>null</code>.
	 */
	IntensityView(MeasurementViewerControl controller, 
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
		return icons.getIcon(IconManager.INTENSITYVIEW);
	}

	/**
	 * Get the analysis results from the model and convert to the 
	 * necessary array. data types using the ROIStats wrapper then
	 * create the approriate graph and plot.  
	 */
	public void displayAnalysisResults()
	{
		this.ROIStats = model.getAnalysisResults();
		if(ROIStats==null)
			return;
		Iterator shapeIterator  = ROIStats.keySet().iterator();
		channelName = new ArrayList<String>();
		channelColour = new ArrayList<Color>();
		channelMin = new HashMap<Integer, Double>();
		channelMax = new HashMap<Integer, Double>();
		channelMean = new HashMap<Integer, Double>();
		channelStdDev = new HashMap<Integer, Double>();
		planePixels = 
							new HashMap<Integer, Map<PlanePoint2D, Double>>();
		int channel;
		ROIShapeStats stats;
		while(shapeIterator.hasNext())
		{
			shape = (ROIShape) shapeIterator.next();
			Map shapeStats = (Map) ROIStats.get(shape);
			Iterator channelIterator = shapeStats.keySet().iterator();
			clearAllVariables();
			clearAllValues();
			while (channelIterator.hasNext())
			{
				channel = (Integer) channelIterator.next();
				channelName.add(model.getMetadata(channel).getEmissionWavelength()+"");
				channelColour.add((Color)model.getActiveChannels().get(channel));
				stats = (ROIShapeStats) shapeStats.get(channel);
				channelMin.put(channel, stats.getMin());
				channelMax.put(channel, stats.getMax());
				channelMean.put(channel, stats.getMean());
				channelStdDev.put(channel, stats.getStandardDeviation());
				planePixels.put(channel, stats.getPixelsValue());
			}
		}
		populateData();
		createComboBox();
		
	}
	
	private void clearAllVariables()
	{
		channelName.clear();
		channelColour.clear();
		channelMin.clear();
		channelMax.clear();
		channelMean.clear();
		channelStdDev.clear();
		planePixels.clear();
	}
	
	private void clearAllValues()
	{
		channelSelection.removeAllItems();
	}
	
	
	private void createComboBox()
	{
		for(int i = 0; i < channelName.size(); i++)
		{
			channelSelection.addItem(channelName.get(i));
		}
	}
	
	private void populateData()
	{
		populateTable();
		populateFields();
	}

	private void populateTable()
	{
		int channel = channelSelection.getSelectedIndex();
		if(channel == -1)
			return;
		Map<PlanePoint2D, Double> pixels = 
							planePixels.get(channel);
	
		Iterator<PlanePoint2D> pixelIterator = pixels.keySet().iterator();
		double minX, maxX, minY, maxY;
		if(!pixelIterator.hasNext())
			return;
		PlanePoint2D point = pixelIterator.next();
		minX = (point.getX());
		maxX = (point.getX());
		minY = (point.getY());
		maxY = (point.getY());
		while(pixelIterator.hasNext())
		{
			point = pixelIterator.next();
			minX = Math.min(minX, point.getX());
			maxX = Math.max(maxX, point.getX());
			minY = Math.min(minY, point.getY());
			maxY = Math.max(maxY, point.getY());
		}
		Double[][] data = new Double[(int)(maxX-minX)+1]
			                            [(int)(maxY-minY)+1];
		System.err.println("Data.size():"+((maxX-minX)+1)+ ", "+((maxY-minY)+1));
		pixelIterator = pixels.keySet().iterator();
		int x, y;
		while(pixelIterator.hasNext())
		{
			point = pixelIterator.next();
			x = (int)(point.getX()-minX);
			y = (int)(point.getY()-minY);
			System.err.println("X : " + x + " , Y : " + y);
			data[x][y]=pixels.get(point);
		}
		tableModel = new IntensityModel(data);
		table.setModel(tableModel);
	}
	
	private void populateFields()
	{
		int channel = channelSelection.getSelectedIndex();
		if(channel == -1)
			return;
		minValue.setText(FormatString(channelMin.get(channel)));
		maxValue.setText(FormatString(channelMax.get(channel)));
		meanValue.setText(FormatString(channelMean.get(channel)));
		stdDevValue.setText(FormatString(channelStdDev.get(channel)));
	}
	
	
	/**
	 * Create jpanel with the label assigned the value str and
	 * the textbox the value value.
	 * @param str see above.
	 * @param value see above.
	 * @return the jpanel with the label and textfield.
	 */
	private JPanel createLabelText(String str, String value)
	{
		JLabel label = new JLabel(str);
		JTextField text = new JTextField(value);
		UIUtilities.setDefaultSize(label, new Dimension(80, 26));
		UIUtilities.setDefaultSize(text, new Dimension(80, 26));
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(label);
		panel.add(Box.createHorizontalStrut(10));
		panel.add(text);
		return panel;
	}
		
	/**
	 * Create jpanel with Jlabel and JTextField.
	 * @param label see above.
	 * @param text see above.
	 * @return the jpanel with the label and textfield.
	 */
	private JPanel createLabelText(JLabel label, JTextField text)
	{
		UIUtilities.setDefaultSize(label, new Dimension(80, 26));
		UIUtilities.setDefaultSize(text, new Dimension(80, 26));
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(label);
		panel.add(Box.createHorizontalStrut(10));
		panel.add(text);
		return panel;
	}
	
	private String FormatString(double value)
	{
		return String.format("%.2f",value);
	}
	
	 public void actionPerformed(ActionEvent e) 
	 {
		 JComboBox cb = (JComboBox)e.getSource();
	     int channel = cb.getSelectedIndex();
	     if(channel!=-1)
	     {
	    	populateData();
	    	populateFields();
    		repaint();
	     }
	 }
}



