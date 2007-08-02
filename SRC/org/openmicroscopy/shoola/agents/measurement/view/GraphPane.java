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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.measurement.IconManager;
import org.openmicroscopy.shoola.agents.measurement.util.AnalysisStatsWrapper;
import org.openmicroscopy.shoola.agents.measurement.util.AnalysisStatsWrapper.StatsType;
import org.openmicroscopy.shoola.util.roi.figures.BezierAnnotationFigure;
import org.openmicroscopy.shoola.util.roi.figures.LineAnnotationFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureTextFigure;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
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
class GraphPane
	extends JPanel
{
	
	/** The name of the panel. */
	private static final String			NAME = "Graph Pane";
	
	/** Reference to the control. */
	private MeasurementViewerControl	controller;
	
	/** Reference to the model. */
	private MeasurementViewerModel		model;

	/** The map of <ROIShape, ROIStats> .*/
	private Map							ROIStats;

	/**
	 * Returns <code>true</code> if the figure contained in the ROIShape
	 * is a line or bezier path, <code>false</code> otherwise.
	 * 
	 * @param shape The ROIShape containing figure.
	 * @return See above.
	 */
	private boolean lineProfileFigure(ROIShape shape)
	{
		ROIFigure f = shape.getFigure();
		if (f instanceof LineAnnotationFigure) return true;
		if (f instanceof BezierAnnotationFigure )
		{
			BezierAnnotationFigure fig = (BezierAnnotationFigure) f;
			if (!fig.isClosed()) return true;
		}
		return false;
	}
	
	/**
	 * Finds the minimum value from the channelMin map.
	 * 
	 * @return See above.
	 */
	private double channelMinValue()
	{
		Map channels = model.getActiveChannels();
		Iterator<Integer> i = channels.keySet().iterator();
		double value = 0;
		int channel;
		while (i.hasNext())
		{
			channel = i.next();
			value = Math.min(value, model.getMetadata(channel).getGlobalMin());
		}
		return value;
	}
	
	/**
	 * Finds the maximum value from the channelMin map.
	 * 
	 * @return See above.
	 */
	private double channelMaxValue()
	{
		Map channels = model.getActiveChannels();
		Iterator<Integer> i = channels.keySet().iterator();
		double value = 0;
		int channel;
		while (i.hasNext())
		{
			channel = i.next();
			value = Math.max(value, model.getMetadata(channel).getGlobalMax());
		}
		return value;
	}
	
	/**
	 * Creates a UI component with the label assigned the value str and
	 * the textbox the value value.
	 * 
	 * @param str 	The value assigned to the label.
	 * @param value The value assigned to the textbox.
	 * @return See above
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
	
	/** Initializes the component composing the display. */
	private void initComponents()
	{
		
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		setLayout(new BorderLayout());
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param controller Reference to the Control. Mustn't be <code>null</code>.
	 * @param model		 Reference to the Model. Mustn't be <code>null</code>.
	 */
	GraphPane(MeasurementViewerControl controller, 
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
		return icons.getIcon(IconManager.GRAPHPANE);
	}

	/**
	 * Returns the analysis results from the model and converts to the 
	 * necessary array. data types using the ROIStats wrapper then
	 * creates the approriate graph and plot.  
	 */
	void displayAnalysisResults()
	{
		this.ROIStats = model.getAnalysisResults();
		if (ROIStats == null) return;
		
		Map<StatsType, Map> shapeStats; 
		Iterator shapeIterator  = ROIStats.keySet().iterator();
		ROIShape shape;
		List<String> channelName = new ArrayList<String>();
		List<Color> channelColour = new ArrayList<Color>();
		List<double[]> channelData = new ArrayList<double[]>();
		List<double[][]> channelXYData = new ArrayList<double[][]>();
		int channel;
		JPanel histogramChart = null;
		JPanel lineProfileChart = null;
		Map<Integer, double[]> data;
		Iterator<Integer> channelIterator;
		Map activeChannels = model.getActiveChannels();
		while(shapeIterator.hasNext())
		{
			shape = (ROIShape) shapeIterator.next();
			if (shape.getFigure() instanceof MeasureTextFigure)
				return;
		
			shapeStats = AnalysisStatsWrapper.convertStats(
											(Map) ROIStats.get(shape));
			channelName.clear();
			channelColour.clear();
			data = shapeStats.get(StatsType.PIXELDATA);
			//channelIterator = data.keySet().iterator();
			channelIterator = activeChannels.keySet().iterator();
			double[] dataY;
			double[][] dataXY;
			Color c;
			while (channelIterator.hasNext())
			{
				channel = channelIterator.next();
				channelName.add(
						model.getMetadata(channel).getEmissionWavelength()+"");
				c = model.getActiveChannelColor(channel);
				//if (c == null) return;
				channelColour.add(c);
				if (data.get(channel).length == 0)
					return;
				channelData.add(data.get(channel));
				
				if (lineProfileFigure(shape))
				{
					dataY = data.get(channel);
					dataXY = new double[2][dataY.length];
					if (dataY.length == 0) return;
					for (int i = 0 ; i < dataY.length ; i++)
					{
						dataXY[0][i] = i;
						dataXY[1][i] = dataY[i];
					}
					channelXYData.add(dataXY);
				}
			}
			
			if (lineProfileFigure(shape))
				lineProfileChart = drawLineplot("Line Profile", 
						channelName, channelXYData, channelColour);
			histogramChart = drawHistogram("Histogram", channelName, 
							channelData, channelColour, 1001);
		}
		
		
		this.removeAll();
		if (histogramChart == null && lineProfileChart == null)
			return;
		if (lineProfileChart == null && histogramChart !=null)
		{
			setLayout(new BorderLayout());
			add(histogramChart, BorderLayout.CENTER);
		}
		if (lineProfileChart != null && histogramChart !=null)
		{
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			add(lineProfileChart);
			add(histogramChart);
		}
	}

	/**
	 * Draws the current data as a scatter plot in the graph.
	 * 
	 * @param title 			The graph title.
	 * @param data 				The data to render.
	 * @param channelNames 		The channel names.
	 * @param channelColours	The channel colours.
	 * @return See above.
	 */
	JPanel drawScatterplot(String title, List<String>	channelNames, 
			List<double[][]> data, List<Color> channelColours)
	{
		ScatterPlot plot = new ScatterPlot(title, channelNames, data, 
			channelColours);
		return plot.getChart();
	}
	
	/**
	 * Draws the current data as a line plot in the graph.
	 * 
	 * @param title 			The graph title.
	 * @param data 				The data to render.
	 * @param channelNames 		The channel names.
	 * @param channelColours	The channel colours.
	 * @return See above.
	 */
	JPanel drawLineplot(String title,  List<String> channelNames, 
			List<double[][]> data, List<Color> channelColours)
	{
		LinePlot plot = new LinePlot(title, channelNames, data, 
			channelColours, channelMinValue(), channelMaxValue());
		return plot.getChart();
	}
	
	/**
	 * Draws the current data as a histogram in the graph.
	 * 
	 * @param title 			The graph title.
	 * @param data 				The data to render.
	 * @param channelNames 		The channel names.
	 * @param channelColours	The channel colours.
	 * @param bins 				The number of bins in the histogram.
	 * @return See above.
	 */
	JPanel drawHistogram(String title,  List<String> channelNames, 
			List<double[]> data, List<Color> channelColours, int bins)
	{
		HistogramPlot plot = new HistogramPlot(title, channelNames, data, 
			channelColours, bins, channelMinValue(), channelMaxValue());
		return plot.getChart();
	}
	
}



