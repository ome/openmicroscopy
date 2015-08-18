/*
 * org.openmicroscopy.shoola.agents.measurement.view.GraphPane 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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


//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.IconManager;
import org.openmicroscopy.shoola.agents.measurement.MeasurementAgent;
import org.openmicroscopy.shoola.agents.measurement.util.TabPaneInterface;
import org.openmicroscopy.shoola.agents.measurement.util.model.AnalysisStatsWrapper;
import org.openmicroscopy.shoola.agents.measurement.util.model.AnalysisStatsWrapper.StatsType;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import omero.log.Logger;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.roi.figures.MeasureBezierFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureLineFigure;
import org.openmicroscopy.shoola.util.roi.figures.MeasureTextFigure;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;
import org.openmicroscopy.shoola.util.roi.model.util.MeasurementUnits;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.graphutils.HistogramPlot;
import org.openmicroscopy.shoola.util.ui.graphutils.LinePlot;
import org.openmicroscopy.shoola.util.ui.slider.OneKnobSlider;
import pojos.ChannelData;

/** 
 * Displays the intensities as a graph. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class GraphPane
	extends JPanel 
	implements TabPaneInterface, PropertyChangeListener, ChangeListener
{
	
	/** Ready state. */
	final static int READY = 1;
	
	/** Analyzing state. */
	final static int ANALYSING = 0;
	
	/** Index to identify tab */
	public final static int INDEX = MeasurementViewerUI.GRAPH_INDEX;
	
	/** The name of the panel. */
	private static final String NAME = "Graph Pane";
	
	/** The default color for a line.*/
	private static final Color DEFAULT_COLOR = Color.LIGHT_GRAY;
	
	/** Reference to the model. */
	private MeasurementViewerModel model;
	
	/** Reference to the controller. */
	private MeasurementViewerControl controller;

	/** The map of <ROIShape, ROIStats> .*/
	private Map ROIStats;

	/** The slider controlling the movement of the analysis through Z. */
	private OneKnobSlider zSlider;

	/** The slider controlling the movement of the analysis through T. */
	private OneKnobSlider tSlider;
	
	/** The main panel holding the graphs. */
	private JPanel mainPanel;
			
	/** The map of the shape statistics to coordinates. */
	private Map<Coord3D, Map<StatsType, Map>> shapeStatsList;
	
	/** Map of the pixel intensity values to coordinates. */
	private Map<Coord3D, Map<Integer, double[]>> pixelStats;
	
	/** Map of the coordinates to a shape. */
	private Map<Coord3D, ROIShape> shapeMap;
	
	/** List of channel Names. */
	private List<String> channelName;
	
	/** List of channel colors. */
	private List<Color> channelColour;
	
	/** The current coordinates of the ROI being depicted in the slider. */
	private Coord3D coord;
		
	/** The line profile charts. */
	private LinePlot lineProfileChart;
	
	/** The histogram chart. */
	private HistogramPlot histogramChart;
	
	/** The state of the Graph pane. */
	private int state = READY;
	
	/** Reference to the view.*/
	private MeasurementViewerUI view;
	
	/** Current shape. */
	private ROIShape shape;

	/** Button to save the graph as JPEG or PNG.*/
	private JButton export;
	
	/**
	 * Implemented as specified by the I/F {@link TabPaneInterface}
	 * @see TabPaneInterface#getIndex()
	 */
	public int getIndex() { return INDEX; }
		
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
		Entry entry;
		Iterator i = channels.entrySet().iterator();
		double value = Double.MAX_VALUE;
		int channel;
		while (i.hasNext())
		{
			entry = (Entry) i.next();
			channel = (Integer) entry.getKey();
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
		Entry entry;
		Iterator i = channels.entrySet().iterator();
		double value = Double.MIN_VALUE;
		int channel;
		while (i.hasNext())
		{
			entry = (Entry) i.next();
			channel = (Integer) entry.getKey();
			value = Math.max(value, model.getMetadata(channel).getGlobalMax());
		}
		return value;
	}
	
	/** The slider has changed value and the mouse button released. */
	private void handleSliderReleased()
	{
		if (zSlider == null || tSlider == null) return;
		if (coord == null) return;
		if (state == ANALYSING) return;
		Coord3D thisCoord = new Coord3D(zSlider.getValue()-1, 
				tSlider.getValue()-1);
		if (coord.equals(thisCoord)) return;
		if (!pixelStats.containsKey(thisCoord)) return;
		state = ANALYSING;
		buildGraphsAndDisplay();
		formatPlane();
		if (shape != null)
			view.selectFigure(shape.getFigure());
		state = READY;
	}

	/**
	 * Saves the graph as JPEG or PNG.
	 *
	 * @param file The file where to save the graph
	 * @param type The format to save into.
	 */
	public void saveGraph(File file, int type)
	{
	    try {
	        if (lineProfileChart != null) {
	            lineProfileChart.saveAs(file, type);
	        } else {
	            histogramChart.saveAs(file, type);
	        }
	    } catch (Exception e) {
	        Logger logger = MeasurementAgent.getRegistry().getLogger();
	        logger.error(this, "Cannot save the graph: "+e.toString());
	        UserNotifier un = MeasurementAgent.getRegistry().getUserNotifier();
	        un.notifyInfo("Save Results", "An error occurred while saving " +
	                "the graph.\nPlease try again.");
	    }
	}

	/** Initializes the component composing the display. */
	private void initComponents()
	{
	        export = new JButton(controller.getAction(MeasurementViewerControl.EXPORT_GRAPH));

		zSlider = new OneKnobSlider();
		zSlider.setOrientation(JSlider.VERTICAL);
		zSlider.setPaintTicks(false);
		zSlider.setPaintLabels(false);
		zSlider.setMajorTickSpacing(1);
		zSlider.setShowArrows(true);
		zSlider.setVisible(false);
		zSlider.setEndLabel("Z");		
		zSlider.setShowEndLabel(true);


		tSlider = new OneKnobSlider();
		tSlider.setPaintTicks(false);
		tSlider.setPaintLabels(false);
		tSlider.setMajorTickSpacing(1);
		tSlider.setSnapToTicks(true);
		tSlider.setShowArrows(true);
		tSlider.setVisible(false);
		tSlider.setEndLabel("T");
		tSlider.setShowEndLabel(true);
		zSlider.addPropertyChangeListener(this);
		tSlider.addPropertyChangeListener(this);
		zSlider.addChangeListener(this);
		tSlider.addChangeListener(this);
		mainPanel = new JPanel();
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		buildHistogramNoSelection();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		JPanel centrePanel = new JPanel();
		centrePanel.setLayout(new BoxLayout(centrePanel, BoxLayout.X_AXIS));
		
		centrePanel.add(zSlider);
		centrePanel.add(Box.createHorizontalStrut(5));
		centrePanel.add(mainPanel);
		centrePanel.add(export);
		add(centrePanel);
		add(tSlider);
	}
	
	/** 
	 * Builds the default histogram when no channels are selected.
	 * 
	 */
	private void buildHistogramNoSelection()
	{
		mainPanel.removeAll();
		histogramChart = drawHistogram("Histogram", new ArrayList<String>(),
				new ArrayList<double[]>(), new ArrayList<Color>(), 1001);
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(histogramChart.getChart(Collections.singletonList((AbstractAction)controller.getAction(MeasurementViewerControl.EXPORT_GRAPH))), BorderLayout.CENTER);
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
	private LinePlot drawLineplot(String title,  List<String> channelNames, 
			List<double[][]> data, List<Color> channelColours,
			Map<Integer, List<String>> locations)
	{
		if (channelNames.size() == 0 || data.size() == 0 || 
			channelColours.size() == 0)
			return null;
		if (channelNames.size() != channelColours.size() || 
				channelNames.size() != data.size())
			return null;
		LinePlot plot = new LinePlot(title, channelNames, data, 
			channelColours, channelMinValue(), channelMaxValue());
		plot.addLocations(locations);
		plot.setYAxisName("Intensity");
		plot.setXAxisName("Points");
		return plot;
	}
	
	/**
	 * Draws the current data as a histogram in the graph.
	 * 
	 * @param title The graph title.
	 * @param data The data to render.
	 * @param channelNames The channel names.
	 * @param channelColours The channel colours.
	 * @param bins The number of bins in the histogram.
	 * @return See above.
	 */
	private HistogramPlot drawHistogram(String title,  List<String> channelNames,
			List<double[]> data, List<Color> channelColours, int bins)
	{
        HistogramPlot plot;
        if (!data.isEmpty())
            plot = new HistogramPlot(title, channelNames, data, channelColours,
                    bins, channelMinValue(), channelMaxValue());
        else
            plot = new HistogramPlot(title, Collections.EMPTY_LIST,
                    Collections.EMPTY_LIST, Collections.EMPTY_LIST, bins, 0, 1);
		plot.setXAxisName("Intensity");
		plot.setYAxisName("Frequency");
		return plot;
	}

	/**
	 * The method builds the graphs from the data that was constructed in the
	 * display analysis method. This method should be called from either the 
	 * display analysis method or the changelistener which uses the same ROI 
	 * data generated in the displayAnalysis method.
	 */
	private void buildGraphsAndDisplay()
	{
		coord = new Coord3D(zSlider.getValue()-1, tSlider.getValue()-1);
		Map<Integer, double[]> data = pixelStats.get(coord);
		if (data == null) return;
		shape = shapeMap.get(coord);
		double[][] dataXY;
		Color c;
		int channel;
		List<double[]> channelData = new ArrayList<double[]>();
		List<double[][]> channelXYData = new ArrayList<double[][]>();
		channelName.clear();
		channelColour.clear();
		channelData.clear();

		ChannelData cData;
		List<ChannelData> metadata = model.getMetadata();
		Iterator<ChannelData> j = metadata.iterator();
		double[] values;
		Map<Integer, List<String>> locations = new HashMap<Integer, List<String>>();
		List<String> points = formatPoints(shape.getFigure().getPoints());
		while (j.hasNext()) {
			cData = j.next();
			channel = cData.getIndex();
			if (model.isChannelActive(channel)) 
			{
				cData = model.getMetadata(channel);
				if (cData != null)
				channelName.add(cData.getChannelLabeling());
				c = model.getActiveChannelColor(channel);
				if (UIUtilities.isSameColors(c, Color.white, false))
					c = DEFAULT_COLOR;
				channelColour.add(c);
				values = data.get(channel);
				if (values != null && values.length != 0) {
					channelData.add(values);
					
					if (lineProfileFigure(shape)) {
					    locations.put(channel, points);
						dataXY = new double[2][values.length];
						for (int i = 0 ; i < values.length ; i++)
						{
							dataXY[0][i] = i;
							dataXY[1][i] = values[i];
						}
						channelXYData.add(dataXY);
					}
				}
			}
		}
		mainPanel.removeAll();
		if (channelData.size() == 0) {
			buildHistogramNoSelection();
			return;
		}
		lineProfileChart = null;
		histogramChart = null;
		if (lineProfileFigure(shape))
			lineProfileChart = drawLineplot("Line Profile", 
					channelName, channelXYData, channelColour, locations);
		histogramChart = drawHistogram("Histogram", channelName, 
				channelData, channelColour, 1001);
			
		if (lineProfileChart == null && histogramChart !=null)
		{
			mainPanel.setLayout(new BorderLayout());
			mainPanel.add(histogramChart.getChart(Collections.singletonList((AbstractAction)controller.getAction(MeasurementViewerControl.EXPORT_GRAPH))), BorderLayout.CENTER);
		}
		
		if (lineProfileChart != null && histogramChart !=null)
		{
			mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
			mainPanel.add(lineProfileChart.getChart(Collections.singletonList((AbstractAction)controller.getAction(MeasurementViewerControl.EXPORT_GRAPH))));
			mainPanel.add(histogramChart.getChart(Collections.singletonList((AbstractAction)controller.getAction(MeasurementViewerControl.EXPORT_GRAPH))));
		}
		mainPanel.validate();
		mainPanel.repaint();
	}

	/**
	 * Formats the text associated to the specified points.
	 *
	 * @param points
	 * @return See above.
	 */
	private List<String> formatPoints(List<Point> points)
	{
	    List<String> values = new ArrayList<String>();
	    Iterator<Point> i = points.iterator();
	    Point p;
	    StringBuilder b;
	    MeasurementUnits units = model.getMeasurementUnits();
	    double sx = units.getPixelSizeX().getValue();
	    double sy = units.getPixelSizeX().getValue();
	    while (i.hasNext()) {
            p = i.next();
            b = new StringBuilder();
            b.append("("+p.x+", "+p.y+")"+UIUtilities.PIXELS_SYMBOL);
            b.append("\n");
            b.append("("+UIUtilities.twoDecimalPlaces(p.x*sx)+", "+
            UIUtilities.twoDecimalPlaces(p.y*sy)+ ")"+EditorUtil.MICRONS_NO_BRACKET);
            values.add(b.toString());
        }
	    return values;
	}

	/** Indicates the selected plane.*/
	private void formatPlane()
	{
		if (!zSlider.isVisible() && !tSlider.isVisible()) {
			view.setPlaneStatus("");
			return;
		}
		StringBuffer buffer = new StringBuffer();
		if (zSlider.isVisible())
			buffer.append("Z="+zSlider.getValue()+" ");
		if (tSlider.isVisible())
			buffer.append("T="+tSlider.getValue());
		view.setPlaneStatus(buffer.toString());
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param view Reference to the View. Mustn't be <code>null</code>.
	 * @param controller Reference to the Control. Mustn't be <code>null</code>.
	 * @param model Reference to the Model. Mustn't be <code>null</code>.
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
		this.controller = controller;
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

	/** Clears the data. */
	void clearData()
	{
		mainPanel.removeAll();
		if (zSlider != null) zSlider.setEnabled(false);
		if (tSlider != null) tSlider.setEnabled(false);
	}
	
	/**
	 * Returns the analysis results from the model and converts to the 
	 * necessary array. data types using the ROIStats wrapper then
	 * creates the graph and plot.  
	 */
	void displayAnalysisResults()
	{
		this.ROIStats = model.getAnalysisResults();
		if (ROIStats == null || ROIStats.size() == 0) {
			buildHistogramNoSelection();
			return;
		}
		shapeStatsList = new HashMap<Coord3D, Map<StatsType, Map>>();
		pixelStats = new HashMap<Coord3D, Map<Integer, double[]>>();
		shapeMap = new HashMap<Coord3D, ROIShape>();
		channelName = new ArrayList<String>();
		channelColour = new ArrayList<Color>();
		Entry entry;
		Iterator i  = ROIStats.entrySet().iterator();
		
	
		int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
		int minT = Integer.MAX_VALUE, maxT = Integer.MIN_VALUE;
		
		Coord3D c3D;
		Map<StatsType, Map> shapeStats;
		Map<Integer, double[]> data;
		int t = model.getDefaultT();
		int z = model.getDefaultZ();
		boolean hasData = false;
		int cT, cZ;
		while (i.hasNext())
		{
			entry = (Entry) i.next();
			shape = (ROIShape) entry.getKey();
			
			c3D = shape.getCoord3D();
			cT = c3D.getTimePoint();
			cZ = c3D.getZSection();
			
			minT = Math.min(minT, cT);
			maxT = Math.max(maxT, cT);
			minZ = Math.min(minZ, cZ);
			maxZ = Math.max(maxZ, cZ);
			
			if (cT == t && cZ == z) hasData = true;
			
			shapeMap.put(c3D, shape);
			if (shape.getFigure() instanceof MeasureTextFigure)
				return;
			shapeStats = AnalysisStatsWrapper.convertStats(
					(Map) entry.getValue());
			if (shapeStats != null) {
				shapeStatsList.put(c3D, shapeStats);
				data = shapeStats.get(StatsType.PIXELDATA);
				pixelStats.put(c3D, data);
			}
		}
		if (!hasData) {
			buildHistogramNoSelection();
			return;
		}
		maxZ = maxZ+1;
		minZ = minZ+1;
		minT = minT+1;
		maxT = maxT+1;
		zSlider.setMaximum(maxZ);
		zSlider.setMinimum(minZ);
		tSlider.setMaximum(maxT);
		tSlider.setMinimum(minT);
		zSlider.setVisible(maxZ != minZ);
		tSlider.setVisible(maxT != minT);
		tSlider.setValue(model.getCurrentView().getTimePoint()+1);
		zSlider.setValue(model.getCurrentView().getZSection()+1);
		formatPlane();
		buildGraphsAndDisplay();
	}
	
 	/**
 	 * Indicates any on-going analysis.
 	 * 
 	 * @param analyse Passes <code>true</code> when analyzing,
 	 * <code>false</code> otherwise.
 	 */
	void onAnalysed(boolean analyse)
	{
		zSlider.setEnabled(!analyse);
		tSlider.setEnabled(!analyse);
	}

	/**
	 * Reacts to changes made by slider.
	 * @see ChangeListener#stateChanged(ChangeEvent)
	 */
	public void stateChanged(ChangeEvent evt) {
		Object src = evt.getSource();
		if (src == zSlider || src == tSlider) {
			formatPlane();
			OneKnobSlider slider = (OneKnobSlider) src;
			if (!slider.isDragging()) {
				handleSliderReleased();
			}
		}
	}

	/**
	 * Listens to property fired by {@link #zSlider} or {@link #tSlider}.
	 * @see ChangeListener#stateChanged(ChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (OneKnobSlider.ONE_KNOB_RELEASED_PROPERTY.equals(name)) {
			handleSliderReleased();
		}
	}

}
