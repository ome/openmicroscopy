/*
 * org.openmicroscopy.shoola.agents.measurement.view.IntensityView 
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.measurement.IconManager;
import org.openmicroscopy.shoola.agents.measurement.MeasurementAgent;
import org.openmicroscopy.shoola.util.filter.file.CSVFilter;
import org.openmicroscopy.shoola.util.math.geom2D.PlanePoint2D;
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
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.filechooser.FileChooser;
import org.openmicroscopy.shoola.util.ui.slider.OneKnobSlider;
import org.openmicroscopy.shoola.agents.measurement.util.TabPaneInterface;
import org.openmicroscopy.shoola.agents.measurement.util.model.AnalysisStatsWrapper;
import org.openmicroscopy.shoola.agents.measurement.util.model.AnalysisStatsWrapper.StatsType;
import org.openmicroscopy.shoola.agents.measurement.util.ui.ColourListRenderer;
import org.openmicroscopy.shoola.env.log.Logger;
import org.openmicroscopy.shoola.env.ui.UserNotifier;

/** 
 * Displays stats computed on the pixels intensity value of a given ROI shape.
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
	implements ActionListener, TabPaneInterface, ChangeListener
{
	/** Index to identify tab */
	public final static int		INDEX = MeasurementViewerUI.INTENSITY_INDEX;

	/** width of textfield components. */
	public final static int		TEXTWIDTH = 100;

	/** width of textfield components. */
	public final static int		TEXTHEIGHT = 26;

	/** width of textfield components. */
	public final static int		LABELWIDTH = 60;

	/** width of textfield components. */
	public final static int		LABELHEIGHT = 26;
	
	/** The state of the Intensity View. */
	static enum State 
	{
		/**
		 * Analysing data.
		 */
		ANALYSING,
		
		/** 
		 * Ready to analyse.
		 */
		READY
	}
	
	/** 
	 * Intensity view state, if Analysiing we should not all the user to 
	 * change combobox or save. 
	 */
	private State						state = State.READY;
	
	/** The name of the panel. */
	private static final String			NAME = "Intensity View";

	/** The save button action command. */
	private static final String			SAVEACTION = "SAVEACTION";
	
	/** The cannel selection action command. */
	private static final String			CHANNELSELECTION = "CHANNELSELECTION";
	
	/** Reference to the model. */
	private MeasurementViewerModel		model;
	
	/** Reference to the view. */
	private MeasurementViewerUI			view;

	/** SelectChannelsForm the form to select the channels to output to the 
	 * file. 
	 */
	private	ChannelSelectionForm		channelsSelectionForm;
	
	/** The map of <ROIShape, ROIStats> .*/
	private Map							ROIStats;
	
	/** Table Model. */
	private IntensityModel				tableModel;
	
	/** Table view. */
	private IntensityTable 				table;
	
	/** Min intensity label. */
	private JLabel						minLabel;
	
	/** Max intensity label. */
	private JLabel						maxLabel;
	
	/** Sum  intensity label. */
	private JLabel						sumLabel;
	
	/** Mean intensity label. */
	private JLabel						meanLabel;
	
	/** stdDev. intensity label. */
	private JLabel						stdDevLabel;
	
	/** XCoord label. */
	private JLabel						XCoordLabel;
	
	/** YCoord label. */
	private JLabel						YCoordLabel;
	
	/** Width label. */
	private JLabel						widthLabel;
	
	/** Height label. */
	private JLabel						heightLabel;

	/** XCentre label. */
	private JLabel						XCentreLabel;
	
	/** YCentre label. */
	private JLabel						YCentreLabel;
	
	/** Min intensity textfield. */
	private JTextField					minValue;
	
	/** Max intensity textfield. */
	private JTextField					maxValue;
	
	/** Sum intensity textfield. */
	private JTextField					sumValue;
	
	/** Mean intensity textfield. */
	private JTextField					meanValue;
	
	/** stdDev. intensity textfield. */
	private JTextField					stdDevValue;
	
	/** XCoord textfield. */
	private JTextField					XCoordValue;
	
	/** YCoord textfield. */
	private JTextField					YCoordValue;
	
	/** Width textfield. */
	private JTextField					widthValue;
	
	/** Height textfield. */
	private JTextField					heightValue;

	/** CentreX textfield. */
	private JTextField					XCentreValue;
	
	/** CentreY textfield. */
	private JTextField					YCentreValue;

	/** The channel selected in the combo box. */
	private int 						selectedChannel;

	/** The name of the channel selected in the combo box. */
	private String 						selectedChannelName;
	
	/** Select to choose the channel to show values for . */
	private JComboBox 					channelSelection;
	
	/** The save button. */
	private JButton 					saveButton; 

	/** The slider controlling the movement of the analysis through Z. */
	private OneKnobSlider 				zSlider;

	/** The slider controlling the movement of the analysis through T. */
	private OneKnobSlider 				tSlider;
	
	
	/** list of the channel names. */
	private Map<Integer, String> channelName = new TreeMap<Integer, String>();
	
	/** List of the channel colours. */
	private Map<Integer, Color> channelColour = new TreeMap<Integer, Color>();
	
	/** Map of the channel sums, for each selected channel. */
	private Map<Integer, Double> channelSum = new TreeMap<Integer, Double>();
	
	/** Map of the channel mins, for each selected channel. */
	private Map<Integer, Double> channelMin = new TreeMap<Integer, Double>();
	
	/** Map of the channel Max, for each selected channel. */
	private Map<Integer, Double> channelMax = new TreeMap<Integer, Double>();
	
	/** Map of the channel Mean, for each selected channel. */
	private Map<Integer, Double> channelMean = new TreeMap<Integer, Double>();
	
	/** Map of the channel std. dev., for each selected channel. */
	private Map<Integer, Double> channelStdDev = new TreeMap<Integer, Double>();
	
	/** Map of the channel Intensities, for each selected channel. */
	private Map<Integer, Map<PlanePoint2D, Double>> planePixels = 
						new TreeMap<Integer, Map<PlanePoint2D, Double>>();

	/** Map of the channel name to channel number .*/
	Map<String, Integer> nameMap = new HashMap<String, Integer>();
	
	/** The map of the shape stats to coord. */
	private HashMap<Coord3D, Map<StatsType, Map>> shapeStatsList;

	/** Map of the pixel intensity values to coord. */
	HashMap<Coord3D, Map<Integer, Map<PlanePoint2D, Double>>> pixelStats;
	
	/** Map of the min channel intensity values to coord. */
	HashMap<Coord3D, Map<Integer, Double>> minStats;

	/** Map of the max channel intensity values to coord. */
	HashMap<Coord3D, Map<Integer, Double>> maxStats;

	/** Map of the mean channel intensity values to coord. */
	HashMap<Coord3D, Map<Integer, Double>> meanStats;
	
	/** Map of the std dev channel intensity values to coord. */
	HashMap<Coord3D, Map<Integer, Double>> stdDevStats;
	
	/** Map of the sum channel intensity values to coord. */
	HashMap<Coord3D, Map<Integer, Double>> sumStats;
	
	/** Map of the coord to a shape. */
	HashMap<Coord3D, ROIShape> shapeMap;
	
	/** The current coord of the ROI being depicted in the slider. */
	Coord3D coord;
	
	/** Current ROIShape. */
	private 	ROIShape shape;
	
	/**
	 * overridden version of {@line TabPaneInterface#getIndex()}
	 * @return the index.
	 */
	public int getIndex() {return INDEX; }
	
	/** Initializes the component composing the display. */
	private void initComponents()
	{
		Double data[][] = new Double[1][1];
		tableModel = new IntensityModel(data);
		table = new IntensityTable(tableModel);
		minLabel = new JLabel("Min");
		maxLabel = new JLabel("Max");
		sumLabel = new JLabel("Sum");
		meanLabel = new JLabel("Mean");
		stdDevLabel = new JLabel("Std Dev.");
		XCoordLabel = new JLabel("X Coord");
		YCoordLabel = new JLabel("Y Coord");
		widthLabel = new JLabel("Width");
		heightLabel = new JLabel("Height");
		XCentreLabel = new JLabel("X Centre");
		YCentreLabel = new JLabel("Y Centre");
		minValue = new JTextField();
		maxValue = new JTextField();
		meanValue = new JTextField();
		sumValue = new JTextField();
		sumValue.setHorizontalAlignment(JTextField.LEFT);
		stdDevValue = new JTextField();
		XCoordValue = new JTextField();
		YCoordValue = new JTextField();
		widthValue = new JTextField();
		heightValue = new JTextField();
		XCentreValue = new JTextField();
		YCentreValue = new JTextField();
		channelSelection = new JComboBox();
		channelSelection.addActionListener(this);
		channelSelection.setActionCommand(CHANNELSELECTION);
		saveButton = new JButton("Save Results");
		saveButton.addActionListener(this);
		saveButton.setActionCommand(SAVEACTION);
		state = State.READY;

		zSlider = new OneKnobSlider();
		zSlider.setOrientation(JSlider.VERTICAL);
		zSlider.setPaintTicks(true);
		zSlider.setPaintLabels(true);
		zSlider.setMajorTickSpacing(1);
		zSlider.addChangeListener(this);
		zSlider.setShowArrows(true);
		zSlider.setVisible(false);
		zSlider.setEndLabel("Z");
		zSlider.setShowEndLabel(true);


		tSlider = new OneKnobSlider();
		tSlider.setPaintTicks(true);
		tSlider.setPaintLabels(true);
		tSlider.setMajorTickSpacing(1);
		tSlider.setSnapToTicks(true);
		tSlider.addChangeListener(this);
		tSlider.setShowArrows(true);
		tSlider.setVisible(false);	
		tSlider.setEndLabel("T");
		tSlider.setShowEndLabel(true);

		table.setVisible(false);
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		JPanel scrollPanel = new JPanel();
		JPanel containerPanel = new JPanel();
		containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.X_AXIS));
		JPanel tPanel = tablePanel();
		tPanel.setAlignmentY(JPanel.TOP_ALIGNMENT);
		containerPanel.add(zSlider);
		containerPanel.add(tPanel);
		JPanel fPanel = fieldPanel();
		fPanel.setAlignmentY(JPanel.TOP_ALIGNMENT);
		containerPanel.add(fPanel);
		scrollPanel.setLayout(new BoxLayout(scrollPanel, BoxLayout.Y_AXIS));
		scrollPanel.add(containerPanel);
		scrollPanel.add(tSlider);
		JScrollPane scrollPane = new JScrollPane(scrollPanel);
		scrollPane.setVerticalScrollBar(scrollPane.createVerticalScrollBar());
		scrollPane.setHorizontalScrollBar(
				scrollPane.createHorizontalScrollBar());
		this.setLayout(new BorderLayout());
		this.add(scrollPane, BorderLayout.CENTER);
	}
	
	/**
	 * Create the table panel which holds all the intensities for the selected
	 * channel in the table.
	 * @return See Above.
	 */
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
	
	/**
	 * Create the field panel which holds all stats fields, min, mean..
	 * @return The fields panel. 
	 */
	private JPanel fieldPanel()
	{
		JPanel panel = new JPanel();
		JPanel fields;
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		UIUtilities.setDefaultSize(channelSelection, new Dimension(150, 32));
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
		fields = createLabelText(sumLabel, sumValue);
		panel.add(fields);
		panel.add(Box.createVerticalStrut(5));
		fields = createLabelText(stdDevLabel, stdDevValue);
		panel.add(fields);
		panel.add(Box.createVerticalStrut(5));
		fields = createLabelText(XCoordLabel, XCoordValue);
		panel.add(fields);
		panel.add(Box.createVerticalStrut(5));
		fields = createLabelText(YCoordLabel, YCoordValue);
		panel.add(fields);
		panel.add(Box.createVerticalStrut(5));
		fields = createLabelText(widthLabel, widthValue);
		panel.add(fields);
		panel.add(Box.createVerticalStrut(5));
		fields = createLabelText(heightLabel, heightValue);
		panel.add(fields);
		panel.add(Box.createVerticalStrut(5));
		fields = createLabelText(XCentreLabel, XCentreValue);
		panel.add(fields);
		panel.add(Box.createVerticalStrut(5));
		fields = createLabelText(YCentreLabel, YCentreValue);
		panel.add(fields);
		panel.add(saveButton);
		Dimension minSize = new Dimension(5, 200);
		Dimension prefSize = new Dimension(5, 200);
		Dimension maxSize = new Dimension(100,Short.MAX_VALUE);
		panel.add(new Box.Filler(minSize, prefSize, maxSize));
			return panel;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param view		 Reference to the View. Mustn't be <code>null</code>.
	 * @param model		 Reference to the Model. Mustn't be <code>null</code>.
	 */
	IntensityView(MeasurementViewerUI view, MeasurementViewerModel model)
	{
		if (view == null)
			throw new IllegalArgumentException("No view.");
		if (model == null)
			throw new IllegalArgumentException("No model.");
		this.view = view;
		this.model = model;
		selectedChannelName = "";
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
	 * create the approriate table data and summary statistics.  
	 */
	public void displayAnalysisResults()
	{
		if(state==State.ANALYSING)
			return;
		state = State.ANALYSING;
		this.ROIStats = model.getAnalysisResults();
		if (ROIStats == null || ROIStats.size() == 0) return;
		
		shapeStatsList = new HashMap<Coord3D, Map<StatsType, Map>>();
		pixelStats = new HashMap<Coord3D, Map<Integer, Map<PlanePoint2D, Double>>>();
		shapeMap = new HashMap<Coord3D, ROIShape>();
		minStats = new HashMap<Coord3D, Map<Integer, Double>>();
		maxStats = new HashMap<Coord3D, Map<Integer, Double>>();
		meanStats = new HashMap<Coord3D, Map<Integer, Double>>();
		sumStats = new HashMap<Coord3D, Map<Integer, Double>>();
		stdDevStats = new HashMap<Coord3D, Map<Integer, Double>>();
		
		
		Iterator<ROIShape> shapeIterator  = ROIStats.keySet().iterator();
		channelName =  new TreeMap<Integer, String>();
		nameMap = new HashMap<String, Integer>();

		int minZ=Integer.MAX_VALUE, maxZ=Integer.MIN_VALUE;
		int minT=Integer.MAX_VALUE, maxT=Integer.MIN_VALUE;
		clearAllValues();
		while(shapeIterator.hasNext())
		{
			shape = (ROIShape) shapeIterator.next();
			minT = Math.min(minT, shape.getCoord3D().getTimePoint());
			maxT = Math.max(maxT, shape.getCoord3D().getTimePoint());
			minZ = Math.min(minZ, shape.getCoord3D().getZSection());
			maxZ = Math.max(maxZ, shape.getCoord3D().getZSection());
			Map<StatsType, Map> shapeStats;
		
			shapeMap.put(shape.getCoord3D(), shape);
			if (shape.getFigure() instanceof MeasureTextFigure)
			{
				state = State.READY;
				return;
			}
	
			shapeStats = AnalysisStatsWrapper.convertStats(
										(Map) ROIStats.get(shape));
			shapeStatsList.put(shape.getCoord3D(), shapeStats);

			minStats.put(shape.getCoord3D(), shapeStats.get(StatsType.MIN));
			maxStats.put(shape.getCoord3D(), shapeStats.get(StatsType.MAX));
			meanStats.put(shape.getCoord3D(), shapeStats.get(StatsType.MEAN));
			sumStats.put(shape.getCoord3D(), shapeStats.get(StatsType.SUM));
			stdDevStats.put(shape.getCoord3D(), shapeStats.get(StatsType.STDDEV));
			pixelStats.put(shape.getCoord3D(), shapeStats.get(StatsType.PIXEL_PLANEPOINT2D));
			
			/* really inefficient but hey.... quick hack just now till refactor */
			Iterator<Integer> channelIterator = shapeStats.get(StatsType.MIN).keySet().iterator();
			channelName.clear();
			nameMap.clear();
			channelColour.clear();
			while(channelIterator.hasNext())
			{
				int channel = channelIterator.next();
				if (model.isChannelActive(channel)) 
				{
					channelName.put(channel,
						model.getMetadata(channel).getEmissionWavelength()+"");
					nameMap.put(channelName.get(channel), channel);
					channelColour.put(channel, 
						(Color)model.getActiveChannels().get(channel));
				}
			}
			
		}
		if(channelName.size()==0 || nameMap.size() ==0 || 
				channelColour.size() == 0)
		{
			state = State.READY;
			return;
		}
		maxZ = maxZ+1;
		minZ = minZ+1;
		
		createComboBox();
		Object[] nameColour = (Object[])channelSelection.getSelectedItem();
		String string = (String)nameColour[1];
		int selectedChannel = nameMap.get(string);
			
		zSlider.setMaximum(maxZ);
		zSlider.setMinimum(minZ);
		tSlider.setMaximum(maxT);
		tSlider.setMinimum(minT);
		zSlider.setVisible((maxZ!=minZ));
		tSlider.setVisible((maxT!=minT));
		tSlider.setValue(model.getCurrentView().getTimePoint());
		zSlider.setValue(model.getCurrentView().getZSection()+1);
		coord = new Coord3D(zSlider.getValue()-1, tSlider.getValue());
		shape = shapeMap.get(coord);
		populateData(coord, selectedChannel);	
		state = State.READY;
	}

	/**
	 * Clear all the variables to start a new analysis.
	 *
	 */
	private void clearAllVariables()
	{
		channelName.clear();
		channelColour.clear();
		channelMin.clear();
		channelSum.clear();
		channelMax.clear();
		channelMean.clear();
		channelStdDev.clear();
		planePixels.clear();
		nameMap.clear();
	}

	/** Clear the combo box. */
	private void clearAllValues()
	{
		channelSelection.removeAllItems();
	}
	
	/**
	 * Create the combobox holding the channel list.
	 *
	 */
	private void createComboBox()
	{
		
		Object[][] channelCols = new Object[channelName.size()][2]; 
		Iterator<Integer> iterator = channelName.keySet().iterator();
		int i = 0;
		while(iterator.hasNext())
		{
			int channel = iterator.next();
			channelCols[i] = new Object[]{ channelColour.get(channel), 
											channelName.get(channel)};
			i++;
		}
	
		channelSelection.setModel(new DefaultComboBoxModel(channelCols));	
		ColourListRenderer renderer =  new ColourListRenderer();
		channelSelection.setRenderer(renderer);
		if(nameMap.containsKey(selectedChannelName))
			selectedChannel = nameMap.get(selectedChannelName);
		else
			selectedChannel = 0;
		channelSelection.setSelectedIndex(selectedChannel);
	}
	
	/** Populate the table and fields with the data. 
	 * @param coord the coordinate of the shape being analysed.
	 * @param channel the channel to be analysed. 
	 */
	private void populateData(Coord3D coord, int channel)
	{
		populateTable(coord, channel);
		populateFields(channel);
	}

	/** Populate the table with the data. 
	 * @param coord the coordinate of the shape being analysed.
	 * @param channel the channel to be analysed. 
	 */
	private void populateTable(Coord3D coord, int channel)
	{
		Map<PlanePoint2D, Double> pixels = 
							pixelStats.get(coord).get(channel);
	
		if(pixels==null)
			return;
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
		int sizeX, sizeY;
		sizeX = (int)(maxX-minX)+1;
		sizeY = (int)((maxY-minY)+1);
		Double[][] data = new Double[sizeX][sizeY];
		pixelIterator = pixels.keySet().iterator();
		int x, y;
		while(pixelIterator.hasNext())
		{
			point = pixelIterator.next();
			x = (int)(point.getX()-minX);
			y = (int)(point.getY()-minY);
			if(x>=sizeX || y>=sizeY)
				continue;
			Double value;
			if(pixels.containsKey(point))
				value = pixels.get(point);
			else
				value = new Double(0);
			data[x][y] = value;
		}
		channelMin = minStats.get(coord);
		channelMax = maxStats.get(coord);
		channelMean = meanStats.get(coord);
		channelStdDev = stdDevStats.get(coord);
		channelSum = sumStats.get(coord);
		tableModel = new IntensityModel(data);
		shape = shapeMap.get(coord);
		table.setModel(tableModel);
		table.setVisible(true);
	}
	
	/** 
	 * Populates the fields with the data.
	 * 
	 * @param channel The channel for the stats.
	 */
	private void populateFields(int channel)
	{
		minValue.setText(UIUtilities.FormatToDecimal(channelMin.get(channel)));
		maxValue.setText(UIUtilities.FormatToDecimal(channelMax.get(channel)));
		sumValue.setText(UIUtilities.FormatToDecimal(channelSum.get(channel)));
		meanValue.setText(UIUtilities.FormatToDecimal(channelMean.get(channel)));
		stdDevValue.setText(UIUtilities.FormatToDecimal(
				channelStdDev.get(channel)));
		ROIFigure fig = shape.getFigure();
		if(areaFigure(fig))
			setValuesForAreaFigure(fig);
		else if(lineFigure(fig))
			setValuesForLineFigure(fig);
		else if (pointFigure(fig))
			setValuesForPointFigure(fig);
	}

	/**
	 * Set the values of the labels and textfield for area figures. 
	 * @param fig The figure.
	 */
	private void setValuesForAreaFigure(ROIFigure fig)
	{
		XCoordValue.setText(fig.getBounds().getX()+"");
		YCoordValue.setText(fig.getBounds().getY()+"");
		widthLabel.setText("Width");
		heightLabel.setText("Height");
		widthValue.setText(AnnotationKeys.WIDTH.get(shape)+"");
		heightValue.setText(AnnotationKeys.HEIGHT.get(shape)+"");
		XCentreValue.setText(UIUtilities.FormatToDecimal
				(AnnotationKeys.CENTREX.get(shape)));
		YCentreValue.setText(UIUtilities.FormatToDecimal(
				AnnotationKeys.CENTREY.get(shape)));
	}

	/**
	 * Sets the values of the labels and textfield for line figures. 
	 * 
	 * @param fig The figure.
	 */
	private void setValuesForLineFigure(ROIFigure fig)
	{
		XCoordValue.setText(AnnotationKeys.STARTPOINTX.get(shape)+"");
		YCoordValue.setText(AnnotationKeys.STARTPOINTY.get(shape)+"");
		widthLabel.setText("End X");
		heightLabel.setText("End Y");
		widthValue.setText(AnnotationKeys.ENDPOINTX.get(shape)+"");
		heightValue.setText(AnnotationKeys.ENDPOINTY.get(shape)+"");
		XCentreValue.setText(UIUtilities.FormatToDecimal(
				AnnotationKeys.CENTREX.get(shape)));
		YCentreValue.setText(UIUtilities.FormatToDecimal(
				AnnotationKeys.CENTREY.get(shape)));
	}
	
	/**
	 * Sets the values of the labels and textfield for point figures. 
	 * 
	 * @param fig The figure.
	 */
	private void setValuesForPointFigure(ROIFigure fig)
	{
		XCoordValue.setText(AnnotationKeys.CENTREX.get(shape)+"");
		YCoordValue.setText(AnnotationKeys.CENTREY.get(shape)+"");
		widthLabel.setText("Width");
		heightLabel.setText("Height");
		widthValue.setText("1");
		heightValue.setText("1");
		XCentreValue.setText(UIUtilities.FormatToDecimal(
				AnnotationKeys.CENTREX.get(shape)));
		YCentreValue.setText(UIUtilities.FormatToDecimal(
				AnnotationKeys.CENTREY.get(shape)));
	}
	
	/**
	 * Returns <code>true</code> if the passed figure is an area figure,
	 * <code>false</code> otherwise.
	 * 
	 * @param fig The param. 
	 * @return See above.
	 */
	private boolean areaFigure(ROIFigure fig)
	{
		if (fig instanceof MeasureEllipseFigure ||
			fig instanceof MeasureRectangleFigure)
			return true;
		if (fig instanceof MeasureBezierFigure)
		{
			MeasureBezierFigure bFig = (MeasureBezierFigure) fig;
			if (bFig.isClosed())
				return true;
		}
		return false;
	}
	
	/**
	 * Returns <code>true</code> if the passed figure is a line figure,
	 * <code>false</code> otherwise.
	 * 
	 * @param fig The param. 
	 * @return See above.
	 */
	private boolean lineFigure(ROIFigure fig)
	{
		if (fig instanceof MeasureLineFigure ||
			fig instanceof MeasureLineConnectionFigure)
			return true;
		if (fig instanceof MeasureBezierFigure)
		{
			MeasureBezierFigure bFig = (MeasureBezierFigure)fig;
			if (!bFig.isClosed())
				return true;
		}
		return false;
	}
	
	/**
	 * Returns <code>true</code> if the passed figure is a Point figure,
	 * <code>false</code> otherwise.
	 *  
	 * @param fig The param. 
	 * @return See above.
	 */
	private boolean pointFigure(ROIFigure fig)
	{
		return (fig instanceof MeasurePointFigure);
	}
	
	/**
	 * Creates jpanel with Jlabel and JTextField.
	 * 
	 * @param label See above.
	 * @param text See above.
	 * @return The jpanel with the label and textfield.
	 */
	private JPanel createLabelText(JLabel label, JTextField text)
	{
		UIUtilities.setDefaultSize(label, 
					new Dimension(LABELWIDTH, LABELHEIGHT));
		UIUtilities.setDefaultSize(text, 
				new Dimension(TEXTWIDTH, TEXTHEIGHT));
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(label);
		panel.add(Box.createHorizontalStrut(10));
		panel.add(text);
		return panel;
	}

	/** Save the results to a csv File. */
	private void saveResults() 
	{
		channelsSelectionForm = new ChannelSelectionForm(channelName);
		ArrayList<FileFilter> filterList=new ArrayList<FileFilter>();
		FileFilter filter=new CSVFilter();
		filterList.add(filter);
		FileChooser chooser=
				new FileChooser(
					 view, FileChooser.SAVE, "Save the Results", "Save the " +
				"Results data to a file which can be loaded by a spreadsheet.",
				filterList);
		chooser.addComponentToControls(channelsSelectionForm);
		File f = UIUtilities.getDefaultFolder();
	    if (f != null) chooser.setCurrentDirectory(f);
		int results = chooser.showDialog();
		if (results != JFileChooser.APPROVE_OPTION) return;
		File  file = chooser.getFormattedSelectedFile();
		//TODO: Modify that code when we have various writer.
		/*
		if (!file.getAbsolutePath().endsWith(CSVFilter.CSV))
		{
			String fileName = file.getAbsolutePath()+"."+CSVFilter.CSV;
			file = new File(fileName);
		}
		*/
		List<Integer> channels = channelsSelectionForm.getUserSelection();
		if (channels == null || channels.size() == 0) {
			UserNotifier un = MeasurementAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Save Results", " Please select at least a channel.");
			view.setStatus("No Channel selected to output.");
			
			return;
		}
		
		BufferedWriter out;
		try
		{
			out = new BufferedWriter(new FileWriter(file));
			Iterator<Coord3D> coordMapIterator = shapeMap.keySet().iterator();
			Coord3D currentCoord;
			int n = channels.size();
			Integer channel;
			while (coordMapIterator.hasNext())
			{
				currentCoord = coordMapIterator.next();
				writeHeader(out, currentCoord);
				for (int i = 0 ; i < n ; i++)
				{
					channel = channels.get(i);
					writeTitle(out, 
							"Channel Number : "+channelName.get(channel));
					if (!nameMap.containsKey(channelName.get(channel)))
						continue;
					channel = nameMap.get(channelName.get(channel));
					writeData(out, currentCoord, channel.intValue());
				}
			}
			out.close();
		}
		catch (IOException e)
		{
			Logger logger = MeasurementAgent.getRegistry().getLogger();
			logger.error(this, "Cannot save ROI results: "+e.toString());
			
			UserNotifier un = MeasurementAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Save Results", "An error occured while trying to" +
					" save the data.\n" +
					"Please try again.");
		}
	}

	/**
	 * Writes the header information for the file, image, projects, dataset.
	 * 
	 * @param out	The buffer to write data into.
	 * @param currentCoord the coord of the shape being written.
	 * @throws IOException Thrown if the data cannot be written.
	 */
	private void writeHeader(BufferedWriter out, Coord3D currentCoord) 
		throws IOException
	{
		//out.write("Project , "+model.getProjectName());
		//out.newLine();
		//out.write("Dataset , "+model.getDatasetName());
		//out.newLine();
		out.write("Image , "+model.getImageName());
		out.newLine();
		out.write("Z ,"  + currentCoord.getZSection()+1);
		out.newLine();
		out.write("T ,"  + currentCoord.getTimePoint());
		out.newLine();
	}
	
	/** 
	 * Write the title for the current channel. 
	 * @param out The output stream.
	 * @param string The title.
	 * @throws IOException 
	 */
	private void writeTitle(BufferedWriter out, String string) throws IOException
	{
		out.write(string);
		out.newLine();
	}
	
	
	/** 
	 * Write the channel intensities and stats to the files.
	 * @param out The output stream.
	 * @param coord the coord of the channel being written.
	 * @param channel The channel to output.
	 * @throws IOException Any IO Error.
	 */
	private void writeData(BufferedWriter out, Coord3D coord, int channel) 
		throws IOException
	{
		populateData(coord, channel);
		Double value;
		addFields(out, channel);
		for(int y = 0 ; y < tableModel.getRowCount() ; y++)
		{
			for(int x = 0 ; x < tableModel.getColumnCount()-1; x++)
			{
				value = (Double) tableModel.getValueAt(y, x); 
				if(value == null)
					value = new Double(0);
				out.write(String.format("%.2f",value));	
				out.write(",");
			}
			value = (Double) tableModel.getValueAt(y, 
												tableModel.getColumnCount()-1); 
			if(value == null)
				value = new Double(0);
			out.write(String.format("%.2f", value)); 
			out.newLine();
		}
	}
	
	/**
	 * Write the min stat to file.
	 * @param out The output stream.
	 * @param channel The channel.
	 * @throws IOException Any io error. 
	 */
	private void writeMinStat(BufferedWriter out, int channel)
		throws IOException
	{
		out.write("Minimum Intensity, ");
		out.write(channelMin.get(channel)+"");
	}
	
	/**
	 * Write the max stat to file.
	 * @param out The output stream.
	 * @param channel Tthe channel.
	 * @throws IOException Any io error. 
	 */
	private void writeMaxStat(BufferedWriter out, int channel)
		throws IOException
	{
		out.write("Maximum Intensity, ");
		out.write(channelMax.get(channel)+"");
	}
	
	/**
	 * Write the mean stat to file.
	 * @param out The output stream.
	 * @param channel The channel.
	 * @throws IOException Any io error. 
	 */
	private void writeMeanStat(BufferedWriter out, int channel)
		throws IOException
	{
		out.write("Mean Intensity, ");
		out.write(channelMean.get(channel)+"");
	}

	/**
	 * Write the sum stat to file.
	 * @param out The output stream.
	 * @param channel The channel.
	 * @throws IOException Any io error. 
	 */
	private void writeSumStat(BufferedWriter out, int channel)
		throws IOException
	{
		out.write("Sum Intensity, ");
		out.write(channelSum.get(channel)+"");
	}
	
	/**
	 * Write the stdDev stat to file.
	 * @param out The output stream.
	 * @param channel The channel.
	 * @throws IOException Any io error. 
	 */
	private void writeStdDevStat(BufferedWriter out, int channel)
		throws IOException
	{
		out.write("StdDev , ");
		out.write(channelStdDev.get(channel)+"");
	}
	
	/**
	 * Write the XCoord stat to file.
	 * @param out The output stream.
	 * @param channel The channel.
	 * @throws IOException Any io error. 
	 */
	private void writeXCoordStat(BufferedWriter out, int channel)
		throws IOException
	{
		out.write(XCoordLabel.getText()+",");
		out.write(XCoordValue.getText());
	}

	/**
	 * Write the YCoord stat to file.
	 * @param out The output stream.
	 * @param channel The channel.
	 * @throws IOException Any io error. 
	 */
	private void writeYCoordStat(BufferedWriter out, int channel)
		throws IOException
	{
		out.write(YCoordLabel.getText()+",");
		out.write(YCoordValue.getText());
	}

	/**
	 * Write the width stat to file.
	 * @param out The output stream.
	 * @param channel The channel.
	 * @throws IOException Any io error. 
	 */
	private void writeWidthStat(BufferedWriter out, int channel)
		throws IOException
	{
		out.write(widthLabel.getText()+",");
		out.write(widthValue.getText());
	}
	
	/**
	 * Write the height stat to file.
	 * @param out The output stream.
	 * @param channel The channel.
	 * @throws IOException Any io error. 
	 */
	private void writeHeightStat(BufferedWriter out, int channel)
		throws IOException
	{
		out.write(heightLabel.getText()+",");
		out.write(heightValue.getText());
	}
	
	/**
	 * Write the XCentre stat to file.
	 * @param out The output stream.
	 * @param channel The channel.
	 * @throws IOException Any io error. 
	 */
	private void writeXCentreStat(BufferedWriter out, int channel)
		throws IOException
	{
		out.write(XCentreLabel.getText()+",");
		out.write(XCentreValue.getText());
	}
	
	/**
	 * Write the YCentre stat to file.
	 * @param out The output stream.
	 * @param channel The channel.
	 * @throws IOException Any io error. 
	 */
	private void writeYCentreStat(BufferedWriter out, int channel)
		throws IOException
	{
		out.write(YCentreLabel.getText()+",");
		out.write(YCentreValue.getText());
	}
	
	/**
	 * Add the any remaining fields (min, max, mean, stdDev) to the file being
	 * saved. 
	 * 
	 * @param out The output stream
	 * @param channel The channel
	 * @throws IOException Any io error.
	 */
	private void addFields(BufferedWriter out, int channel) 
		throws IOException
	{
		writeMinStat(out, channel);
		out.newLine();
		writeMaxStat(out, channel);
		out.newLine();
		writeSumStat(out, channel);
		out.newLine();
		writeMeanStat(out, channel);
		out.newLine();
		writeStdDevStat(out, channel);
		out.newLine();
		writeXCoordStat(out, channel);
		out.newLine();
		writeYCoordStat(out, channel);
		out.newLine();
		writeWidthStat(out, channel);
		out.newLine();
		writeHeightStat(out, channel);
		out.newLine();
		writeXCentreStat(out, channel);
		out.newLine();
		writeYCentreStat(out, channel);
		out.newLine();
	}
	
	/** 
	 * 	Action called when the combo box changed. 
	 *  @param e The event.
	 **/
	 public void actionPerformed(ActionEvent e) 
	 {
		if (state==State.ANALYSING)
			return;
		if (e.getActionCommand().equals(CHANNELSELECTION))
		{
			JComboBox cb = (JComboBox)e.getSource();
			Object[] nameColour = (Object[])cb.getSelectedItem();
			String string = (String)nameColour[1];
			if(!nameMap.containsKey(string))
				return;
			selectedChannelName = string;
			int channel = nameMap.get(string);
			if (channel!=-1)
			{
				Coord3D newCoord = new Coord3D(zSlider.getValue()-1, 
						tSlider.getValue());
				populateData(newCoord, channel);
				repaint();
			}
		 }
		 if (e.getActionCommand().equals(SAVEACTION))
		 {
			 saveResults();
		 }
	
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
		if(state!=State.READY)
			return;
		Coord3D thisCoord = new Coord3D(zSlider.getValue()-1, tSlider.getValue());
		if(coord.equals(thisCoord))
			return;
		Object[] nameColour = (Object[])channelSelection.getSelectedItem();
		String string = (String)nameColour[1];
		if(!nameMap.containsKey(string))
			return;
		selectedChannelName = string;
		int channel = nameMap.get(string);
		
		if(channel!=-1)
		{
			state = State.ANALYSING;
			populateData(thisCoord, channel);
			repaint();
			if(shape!=null)
				view.selectFigure(shape.getFigure());
			state=State.READY;
		}
	}
	 
}
