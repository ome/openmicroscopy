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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.IconManager;
import org.openmicroscopy.shoola.agents.measurement.util.TabPaneInterface;
import org.openmicroscopy.shoola.agents.measurement.util.model.AnalysisStatsWrapper;
import org.openmicroscopy.shoola.agents.measurement.util.model.AnalysisStatsWrapper.StatsType;
import org.openmicroscopy.shoola.util.roi.figures.MeasureBezierFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureLineFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureTextFigure;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;
import org.openmicroscopy.shoola.util.ui.graphutils.HistogramPlot;
import org.openmicroscopy.shoola.util.ui.graphutils.LinePlot;
import org.openmicroscopy.shoola.util.ui.graphutils.ScatterPlot;
import org.openmicroscopy.shoola.util.ui.slider.OneKnobSlider;

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
	implements TabPaneInterface, ChangeListener
{
	/** Ready state. */
	final static int 						READY = 1;
	
	/** Analysing state. */
	final static int 						ANALYSING = 0;
	
	/** Index to identify tab */
	public final static int					INDEX = MeasurementViewerUI.GRAPH_INDEX;
	
	/** The name of the panel. */
	private static final String				NAME = "Graph Pane";
	
	/** Reference to the model. */
	private MeasurementViewerModel			model;

	/** The map of <ROIShape, ROIStats> .*/
	private Map								ROIStats;

	/** The slider controlling the movement of the analysis through Z. */
	private OneKnobSlider 					zSlider;

	/** The slider controlling the movement of the analysis through T. */
	private OneKnobSlider 					tSlider;
	
	/** The main panel holding the graphs. */
	private JPanel 							mainPanel;
			
	/** The map of the shape stats to coord. */
	private HashMap<Coord3D, Map<StatsType, Map>> shapeStatsList;
	
	/** Map of the pixel intensity values to coord. */
	HashMap<Coord3D, Map<Integer, double[]>> pixelStats;
	
	/** Map of the coord to a shape. */
	HashMap<Coord3D, ROIShape> 				shapeMap;
	
	/** List of channel Names. */
	List<String> 							channelName ;
	
	/** List of channel colours. */
	List<Color> 							channelColour;
	
	/** The current coord of the ROI being depicted in the slider. */
	Coord3D 								coord;
		
	/** The line profile charts. */
	LinePlot 								lineProfileChart;
	
	/** The histogram chart. */
	HistogramPlot 							histogramChart;
	
	/** The state of the Graph pane. */
	int 									state= READY;
	
	/** Reference to the view.*/
	MeasurementViewerUI 					view;
	
	/** Current shape. */
	ROIShape 								shape;
	
	/**
	 * overridded version of {@line TabPaneInterface#getIndex()}
	 * @return the index of the tab.
	 */
	public int getIndex() {return INDEX; }
		
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
		if (f instanceof MeasureLineFigure) return true;
		if (f instanceof MeasureBezierFigure )
		{
			MeasureBezierFigure fig = (MeasureBezierFigure) f;
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
	
	/** Initializes the component composing the display. */
	private void initComponents()
	{
		zSlider = new OneKnobSlider();
		zSlider.setOrientation(JSlider.VERTICAL);
		zSlider.setPaintTicks(true);
		zSlider.setPaintLabels(true);
		zSlider.setMajorTickSpacing(1);
		zSlider.addChangeListener(this);
		zSlider.setShowArrows(true);
		zSlider.setVisible(false);

		tSlider = new OneKnobSlider();
		tSlider.setPaintTicks(true);
		tSlider.setPaintLabels(true);
		tSlider.setMajorTickSpacing(1);
		tSlider.setSnapToTicks(true);
		tSlider.addChangeListener(this);
		tSlider.setShowArrows(true);
		tSlider.setVisible(false);
		mainPanel = new JPanel();
		
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		JPanel centrePanel = new JPanel();
		centrePanel.setLayout(new BoxLayout(centrePanel, BoxLayout.X_AXIS));
		
		centrePanel.add(zSlider);
		centrePanel.add(mainPanel);
		this.add(centrePanel);
		this.add(tSlider);
		
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param view 		 Reference to the View. Mustn't be <code>null</code>.
	 * @param controller Reference to the Control. Mustn't be <code>null</code>.
	 * @param model		 Reference to the Model. Mustn't be <code>null</code>.
	 */
	GraphPane(MeasurementViewerUI view, MeasurementViewerControl controller, 
		MeasurementViewerModel model)
	{
		if (view == null)
			throw new IllegalArgumentException("No view.");
		if (controller == null)
			throw new IllegalArgumentException("No control.");
		if (model == null)
			throw new IllegalArgumentException("No model.");
		this.model = model;
		this.view = view;
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
		shapeStatsList = new HashMap<Coord3D, Map<StatsType, Map>>();
		pixelStats = new HashMap<Coord3D, Map<Integer, double[]>>();
		Iterator<ROIShape> shapeIterator  = ROIStats.keySet().iterator();
		shapeMap = new HashMap<Coord3D, ROIShape>();
		channelName = new ArrayList<String>();
		channelColour = new ArrayList<Color>();
	
		int minZ=Integer.MAX_VALUE, maxZ=Integer.MIN_VALUE;
		int minT=Integer.MAX_VALUE, maxT=Integer.MIN_VALUE;
		while(shapeIterator.hasNext())
		{
			shape = (ROIShape) shapeIterator.next();
			minT = Math.min(minT, shape.getCoord3D().getTimePoint());
			maxT = Math.max(maxT, shape.getCoord3D().getTimePoint());
			minZ = Math.min(minZ, shape.getCoord3D().getZSection());
			maxZ = Math.max(maxZ, shape.getCoord3D().getZSection());
			Map<StatsType, Map> shapeStats;
			Map<Integer, double[]> data;
			
			shapeMap.put(shape.getCoord3D(), shape);
			if (shape.getFigure() instanceof MeasureTextFigure)
				return;
		
			shapeStats = AnalysisStatsWrapper.convertStats(
											(Map) ROIStats.get(shape));
			shapeStatsList.put(shape.getCoord3D(), shapeStats);

	
			data = shapeStats.get(StatsType.PIXELDATA);
			pixelStats.put(shape.getCoord3D(), data);
		}
		maxZ = maxZ+1;
		minZ = minZ+1;
		zSlider.setMaximum(maxZ);
		zSlider.setMinimum(minZ);
		tSlider.setMaximum(maxT);
		tSlider.setMinimum(minT);
		zSlider.setVisible((maxZ!=minZ));
		tSlider.setVisible((maxT!=minT));
		tSlider.setValue(model.getCurrentView().getTimePoint());
		zSlider.setValue(model.getCurrentView().getZSection()+1);

		buildGraphsAndDisplay();
	}

	/**
	 * The method builds the graphs from the data that was constructed in the
	 * display analysis method. This method should be called from either the 
	 * display analysis method or the changelistener which uses the same ROI 
	 * data generated in the displayAnalysis method.
	 *
	 */
	private void buildGraphsAndDisplay()
	{
		coord = new Coord3D(zSlider.getValue()-1, tSlider.getValue());
		Map<Integer, double[]> data = pixelStats.get(coord);
		if(data==null)
			return;
		shape = shapeMap.get(coord);
		double[] dataY;
		double[][] dataXY;
		Color c;
		int channel;
		List<double[]> channelData = new ArrayList<double[]>();
		List<double[][]> channelXYData = new ArrayList<double[][]>();
		channelName.clear();
		channelColour.clear();
		channelXYData.clear();
		channelData.clear();
		Iterator<Integer>channelIterator = data.keySet().iterator();
		while (channelIterator.hasNext())
		{
			channel = channelIterator.next();
			if (model.isChannelActive(channel)) 
			{
				channelName.add(
					model.getMetadata(channel).getEmissionWavelength()+"");
				c = model.getActiveChannelColor(channel);
				if (c == null) return;
				channelColour.add(c);
				if (data.get(channel).length == 0)
					return;
				channelData.add(data.get(channel));
				
				if (lineProfileFigure(shape))
				{
					dataY = data.get(channel);
					dataXY = new double[2][dataY.length];
					if (dataY.length == 0) 
						return;
					for (int i = 0 ; i < dataY.length ; i++)
					{
						dataXY[0][i] = i;
						dataXY[1][i] = dataY[i];
					}
					channelXYData.add(dataXY);
				}
			}
		}
		
		if (lineProfileFigure(shape))
			lineProfileChart = drawLineplot("Line Profile", 
					channelName, channelXYData, channelColour);
		histogramChart = drawHistogram("Histogram", channelName, 
						channelData, channelColour, 1001);
		mainPanel.removeAll();
			
		if (histogramChart == null && lineProfileChart == null)
			return;

		if (lineProfileChart == null && histogramChart !=null)
		{
			mainPanel.setLayout(new BorderLayout());
			mainPanel.add(histogramChart.getChart(), BorderLayout.CENTER);
		}
		
		if (lineProfileChart != null && histogramChart !=null)
		{
			mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
			mainPanel.add(lineProfileChart.getChart());
			mainPanel.add(histogramChart.getChart());
		}
		mainPanel.validate();
		mainPanel.repaint();
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
	LinePlot drawLineplot(String title,  List<String> channelNames, 
			List<double[][]> data, List<Color> channelColours)
	{
		LinePlot plot = new LinePlot(title, channelNames, data, 
			channelColours, channelMinValue(), channelMaxValue());
		plot.setYAxisName("Intensity");
		plot.setXAxisName("Pixel");
		return plot;
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
	HistogramPlot drawHistogram(String title,  List<String> channelNames, 
			List<double[]> data, List<Color> channelColours, int bins)
	{
		HistogramPlot plot = new HistogramPlot(title, channelNames, data, 
			channelColours, bins, channelMinValue(), channelMaxValue());
		plot.setXAxisName("Intensity");
		plot.setYAxisName("Frequency");
		return plot;
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
	{
		if(zSlider == null || tSlider == null )
			return;
		if(coord==null)
			return;
		if(state==ANALYSING)
			return;
		Coord3D thisCoord = new Coord3D(zSlider.getValue()-1, tSlider.getValue());
		if(coord.equals(thisCoord))
			return;
		state = ANALYSING;
		this.buildGraphsAndDisplay();
		state=READY;
		if(shape!=null)
			view.selectFigure(shape.getFigure());
	}
	
}



