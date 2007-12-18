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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.JTableHeader;

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
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.filechooser.FileChooser;
import org.openmicroscopy.shoola.util.ui.slider.OneKnobSlider;
import org.openmicroscopy.shoola.agents.measurement.util.ChannelSummaryModel;
import org.openmicroscopy.shoola.agents.measurement.util.ChannelSummaryTable;
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
	
	/** The intial size of the intensity table dialog. */
	private Dimension intensityTableSize = new Dimension(300,300);
	
	
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
	
	/** The show table action button. */
	private static final String			SHOWTABLEACTION = "SHOWTABLEACTION";
	
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
	
	/** Table for summary of measurement values. */
	private ChannelSummaryTable			channelSummaryTable;

	/** Model for summary of measurement values. */
	private ChannelSummaryModel			channelSummaryModel;
	
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
	Map<String, Integer> nameMap = new LinkedHashMap<String, Integer>();
	
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
	
	/** Button for the calling of the intensity table. */
	JButton showIntensityTable;
	
	/** Current ROIShape. */
	private 	ROIShape shape;
	
	/** Dialog showing the intensity values for the selected channel. */
	private JDialog intensityDialog;
	
	/** The scroll pane for the intensityDialog. */
	private JScrollPane intensityTableScrollPane;
	
	/** The Row header for the intensityTableScrollPane. */
	private JList intensityTableRowHeader;
	
	/**
	 * overridden version of {@line TabPaneInterface#getIndex()}
	 * @return the index.
	 */
	public int getIndex() {return INDEX; }
	
	/** Initializes the component composing the display. */
	private void initComponents()
	{
		Double data[][] = new Double[1][1];
		Double summaryData[][] = new Double[1][1];
		ArrayList<String> rowNames = new ArrayList<String>();
		ArrayList<String> columnNames = new ArrayList<String>();
		
		rowNames.add("");
		columnNames.add("");
		tableModel = new IntensityModel(data);
		table = new IntensityTable(tableModel);
		channelSummaryModel = new ChannelSummaryModel(rowNames, columnNames, 
														summaryData);
		channelSummaryTable = new ChannelSummaryTable(channelSummaryModel);
	
		showIntensityTable = new JButton("Show Intensity Values");
		showIntensityTable.addActionListener(this);
		showIntensityTable.setActionCommand(SHOWTABLEACTION);
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

 		intensityDialog = new JDialog(view,"Intensity Values");
		table.setVisible(true);
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		JPanel scrollPanel = new JPanel();
		JPanel containerPanel = new JPanel();
		containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.X_AXIS));
		JPanel tPanel = tablePanel();
		containerPanel.add(zSlider);
		containerPanel.add(tPanel);
		JPanel cPanel = new JPanel();
		cPanel.setLayout(new BoxLayout(cPanel, BoxLayout.Y_AXIS));
		cPanel.add(containerPanel);
		cPanel.add(tSlider);
		JPanel buttonPanel = createButtonPanel();
		
		scrollPanel.setLayout(new BoxLayout(scrollPanel, BoxLayout.X_AXIS));
		scrollPanel.add(cPanel);
		scrollPanel.add(buttonPanel);
		scrollPanel.add(Box.createGlue());
		
		this.setLayout(new BorderLayout());
		this.add(scrollPanel, BorderLayout.CENTER);
		
		intensityDialog.getContentPane().setLayout(new BorderLayout());
		intensityDialog.getContentPane().add(createInfoPanel(), BorderLayout.NORTH);

		table.setColumnSelectionAllowed(true);
		table.setRowSelectionAllowed(true);
		table.getTableHeader().setReorderingAllowed(false);
		table.setShowGrid(true);
		intensityTableScrollPane = new JScrollPane(table);
		intensityTableScrollPane.setVerticalScrollBar(intensityTableScrollPane.createVerticalScrollBar());
		intensityTableScrollPane.setHorizontalScrollBar(
			intensityTableScrollPane.createHorizontalScrollBar());
		intensityTableRowHeader = new JList(new HeaderListModel(table.getRowCount()));
		intensityTableRowHeader.setFixedCellHeight(table.getRowHeight());
		intensityTableRowHeader.setFixedCellWidth(table.getColumnWidth());
		intensityTableRowHeader.setCellRenderer(new RowHeaderRenderer(table));
	    intensityTableScrollPane.setRowHeaderView(intensityTableRowHeader);
	    intensityTableScrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, new JPanel());
		intensityDialog.getContentPane().add(intensityTableScrollPane, BorderLayout.CENTER);
		JViewport viewPort = intensityTableScrollPane.getViewport();
  		viewPort.setViewPosition(new Point(1,1)); 
		}
	
	/**
	 * Create the button panel to the right of the summary table.
	 * @return see above.
	 */
	private JPanel createButtonPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(Box.createRigidArea(new Dimension(0,10)));
		JPanel channelPanel = UIUtilities.buildComponentPanel(channelSelection);
		UIUtilities.setDefaultSize(channelPanel, new Dimension(175, 32));
		panel.add(channelPanel);
		panel.add(Box.createRigidArea(new Dimension(0,10)));
		JPanel intensityPanel = UIUtilities.buildComponentPanel(showIntensityTable);
		UIUtilities.setDefaultSize(intensityPanel, new Dimension(175, 32));
		panel.add(intensityPanel);
		panel.add(Box.createRigidArea(new Dimension(0,10)));
		JPanel savePanel = UIUtilities.buildComponentPanel(saveButton);
		UIUtilities.setDefaultSize(savePanel, new Dimension(175, 32));
		panel.add(savePanel);
		panel.add(Box.createVerticalGlue());
		return panel;
	}
	
	/**
	 * Show the intensity values table. 
	 */
	private void showIntensityTable()
	{
		if(!intensityDialog.isVisible())
			UIUtilities.setLocationRelativeToAndShow(this, intensityDialog);
	}
	
	/**
	 * Creates the info panel at the top the the dialog, 
	 * showing a little text about the Intensity Pane. 
	 * 
	 * @return See above.
	 */
	private JPanel createInfoPanel()
	{
		JPanel infoPanel = new TitlePanel("Intensity Values", 
				"This table shows the Intensity values for the selected channel" +
				"of the selected ROI.",
				IconManager.getInstance().getIcon(IconManager.WIZARD));
		return infoPanel;
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
		JScrollPane scrollPane = new JScrollPane(channelSummaryTable);
		scrollPane.setVerticalScrollBar(scrollPane.createVerticalScrollBar());
		scrollPane.setHorizontalScrollBar(
				scrollPane.createHorizontalScrollBar());
		panel.add(scrollPane);
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
		this.ROIStats = model.getAnalysisResults();
		if (ROIStats == null || ROIStats.size() == 0) 
			return;
		state = State.ANALYSING;
		
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
		
		if(channelName.size()!=channelColour.size())
		{
			state = State.READY;
			return;
		}
		createComboBox();
		if(selectedChannel >= channelSelection.getItemCount())
		{
			state = State.READY;
			return;
		}

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
		if(channelCols.length==0)
			return;
		
		channelSelection.setModel(new DefaultComboBoxModel(channelCols));	
		ColourListRenderer renderer =  new ColourListRenderer();
		channelSelection.setRenderer(renderer);
		if(nameMap.containsKey(selectedChannelName))
			selectedChannel = nameMap.get(selectedChannelName);
		else
			selectedChannel = 0;
		Object[] nameColour = (Object[])channelSelection.getSelectedItem();
		selectedChannelName = (String)nameColour[1];
		if(selectedChannel >= channelSelection.getItemCount() || selectedChannel < 0)
			return;
		channelSelection.setSelectedIndex(selectedChannel);
	}
	
	/** Populate the table and fields with the data. 
	 * @param coord the coordinate of the shape being analysed.
	 * @param channel the channel to be analysed. 
	 */
	private void populateData(Coord3D coord, int channel)
	{
		interpretResults(coord, channel);
		populateChannelSummaryTable(coord);
	}

	/**
	 * Populate the summary table with the list of values for the ROI at 
	 * coord.
	 * @param coord see above.
	 */
	private void populateChannelSummaryTable(Coord3D coord)
	{
		ArrayList<String> statNames = new ArrayList<String>();
		ArrayList<String> channelNames = new ArrayList<String>();
		ROIFigure fig = shape.getFigure();
		int count = 0;
		statNames.add("Min");
		statNames.add("Max");
		statNames.add("Sum");
		statNames.add("Mean");
		statNames.add("Std Dev.");
		if(areaFigure(fig))
			addAreaStats(statNames);
		else if(lineFigure(fig))
			addLineStats(statNames);
		else if(pointFigure(fig))
			addPointStats(statNames);
		Iterator<Integer> channelIterator = channelName.keySet().iterator();
		while(channelIterator.hasNext())
			channelNames.add(channelName.get(channelIterator.next()));
		Double data[][] = new Double[channelName.size()][statNames.size()];
		
		channelIterator = channelName.keySet().iterator();
		int channel;
		count = 0;
		while(channelIterator.hasNext())
		{
			channel = channelIterator.next();
			populateSummaryColumn(fig, data, channel, count);
			count++;
		}
		
		channelSummaryModel = new ChannelSummaryModel(statNames, channelNames, data);
		channelSummaryTable.setModel(channelSummaryModel);
	}
	
	/**
	 * Populate the data for use in the summary table for the figure fig,
	 * and for channel. 
	 * @param fig see above.
	 * @param data see above.
	 * @param channel see above.
	 * @param count the column the data is being placed in.
	 */
	private void populateSummaryColumn(ROIFigure fig, Double data[][], int channel, int count)
	{
		data[count][0] = channelMin.get(channel);
		data[count][1] = channelMax.get(channel);
		data[count][2] = channelSum.get(channel);
		data[count][3] = channelMean.get(channel);
		data[count][4] = channelStdDev.get(channel);
		
		if(areaFigure(fig))
			addValuesForAreaFigure(fig, data, channel, count);
		else if(lineFigure(fig))
			addValuesForLineFigure(fig, data, channel, count);
		else if (pointFigure(fig))
			addValuesForPointFigure(fig, data, channel, count);
	}
	
	/**
	 * Add stats in the column for area figures.
	 * @param fig the figure where the stats come from. 
	 * @param data the data being populated.
	 * @param channel the channel where the stats come from/.
	 * @param count the column in the table being populated.
	 */
	private void addValuesForAreaFigure(ROIFigure fig, Double data[][], int channel, int count)
	{
		data[count][5] = fig.getBounds().getX();
		data[count][6] = fig.getBounds().getY();
		data[count][7] = AnnotationKeys.WIDTH.get(shape);
		data[count][8] = AnnotationKeys.HEIGHT.get(shape);
		data[count][9] = AnnotationKeys.CENTREX.get(shape);
		data[count][10] = AnnotationKeys.CENTREY.get(shape);
	}
	
	/**
	 * Add stats in the column for line figures.
	 * @param fig the figure where the stats come from. 
	 * @param data the data being populated.
	 * @param channel the channel where the stats come from/.
	 * @param count the column in the table being populated.
	 */
	private void addValuesForLineFigure(ROIFigure fig, Double data[][], int channel, int count)
	{
		data[count][5] = AnnotationKeys.STARTPOINTX.get(shape);
		data[count][6] = AnnotationKeys.STARTPOINTY.get(shape);
		data[count][7] = AnnotationKeys.ENDPOINTX.get(shape);
		data[count][8] = AnnotationKeys.ENDPOINTY.get(shape);
		data[count][9] = AnnotationKeys.CENTREX.get(shape);
		data[count][10] = AnnotationKeys.CENTREY.get(shape);
	}
	

	/**
	 * Add stats in the column for point figures.
	 * @param fig the figure where the stats come from. 
	 * @param data the data being populated.
	 * @param channel the channel where the stats come from/.
	 * @param count the column in the table being populated.
	 */
	private void addValuesForPointFigure(ROIFigure fig, Double data[][], int channel, int count)
	{
		data[count][5] = AnnotationKeys.CENTREX.get(shape);
		data[count][6] = AnnotationKeys.CENTREY.get(shape);
	}
	
	/**
	 * Sets the rows describing the stats being displayed in the summary table. 
	 * @param statNames The list of stats being displayed in the summary table.
	 */
	private void addAreaStats(ArrayList<String> statNames)
	{
		statNames.add("X Coord");
		statNames.add("Y Coord");
		statNames.add("Width");
		statNames.add("Height");
		statNames.add("X Centre");
		statNames.add("Y Centre");
	}

	/**
	 * Sets the rows describing the stats being displayed in the summary table. 
	 * @param statNames The list of stats being displayed in the summary table.
	 */
	private void addLineStats(ArrayList<String> statNames)
	{
		statNames.add("X1 Coord");
		statNames.add("Y1 Coord");
		statNames.add("X2 Coord");
		statNames.add("Y2 Coord");
		statNames.add("X Centre");
		statNames.add("Y Centre");
	}
	
	/**
	 * Sets the rows describing the stats being displayed in the summary table. 
	 * @param statNames The list of stats being displayed in the summary table.
	 */
	private void addPointStats(ArrayList<String> statNames)
	{

		statNames.add("X Centre");
		statNames.add("Y Centre");
	}

	/**
	 * Called by the display analysis results method to build the results into
	 * datastructures used by the intensityView.
	 * @param coord
	 * @param channel
	 */
	private void interpretResults(Coord3D coord, int channel)
	{
		Map<PlanePoint2D, Double> pixels=pixelStats.get(coord).get(channel);
		if (pixels==null) return;
		Iterator<PlanePoint2D> pixelIterator=pixels.keySet().iterator();
		double minX, maxX, minY, maxY;
		if (!pixelIterator.hasNext()) return;
		PlanePoint2D point=pixelIterator.next();
		minX=(point.getX());
		maxX=(point.getX());
		minY=(point.getY());
		maxY=(point.getY());
		while (pixelIterator.hasNext())
		{
			point=pixelIterator.next();
			minX=Math.min(minX, point.getX());
			maxX=Math.max(maxX, point.getX());
			minY=Math.min(minY, point.getY());
			maxY=Math.max(maxY, point.getY());
		}
		int sizeX, sizeY;
		sizeX=(int) (maxX-minX)+1;
		sizeY=(int) ((maxY-minY)+1);
		Double[][] data=new Double[sizeX][sizeY];
		pixelIterator=pixels.keySet().iterator();
		int x, y;
		while (pixelIterator.hasNext())
		{
			point=pixelIterator.next();
			x=(int) (point.getX()-minX);
			y=(int) (point.getY()-minY);
			if (x>=sizeX||y>=sizeY) continue;
			Double value;
			if (pixels.containsKey(point)) value=pixels.get(point);
			else value=new Double(0);
			data[x][y]=value;
		}
		channelMin=minStats.get(coord);
		channelMax=maxStats.get(coord);
		channelMean=meanStats.get(coord);
		channelStdDev=stdDevStats.get(coord);
		channelSum=sumStats.get(coord);
		tableModel=new IntensityModel(data);
		shape=shapeMap.get(coord);
		table.setModel(tableModel); 
		Vector<Integer> newData = new Vector<Integer>();
		for(int i = 1 ; i < table.getRowCount()+1 ; i++)
			newData.add(i);
		intensityTableRowHeader.setListData(newData);
		
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
					if(channel==ChannelSelectionForm.SUMMARYVALUE)
					{
						addSummaryTable(out);
						continue;
					}
					if (!nameMap.containsKey(channelName.get(channel)))
						continue;
					writeTitle(out, 
							"Channel Number : "+channelName.get(channel));
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
		out.write("Image , "+model.getImageName());
		out.newLine();
		out.write("Z ,"  + currentCoord.getZSection()+1);
		out.newLine();
		out.write("T ,"  + currentCoord.getTimePoint()+1);
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
	 * Add the any remaining fields (min, max, mean, stdDev) to the file being
	 * saved. 
	 * 
	 * @param out The output stream
	 * @throws IOException Any io error.
	 */
	private void addSummaryTable(BufferedWriter out) 
		throws IOException
	{
		for(int x = 0 ; x < channelSummaryTable.getColumnCount(); x++)
				out.write(channelSummaryTable.getColumnName(x)+",");
		out.newLine();
		for(int y = 0 ; y < channelSummaryTable.getRowCount() ; y++)
		{
			for(int x = 0 ; x < channelSummaryTable.getColumnCount(); x++)
				out.write(channelSummaryTable.getValueAt(y, x)+",");
			out.newLine();
		}
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
		 if(e.getActionCommand().equals(SHOWTABLEACTION))
		 {
			 showIntensityResults();
		 }
	
	 }
	 
	 /**
	  * Show the intensity results dialog.
	  */
	 private void showIntensityResults()
	 {
		 UIUtilities.setLocationRelativeToAndSizeToWindow(this, intensityDialog, intensityTableSize);
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
	
	/**
	 * Class to define the row header data, this is the Z section 
	 * count in the ROIAssistant.
	 */
	class HeaderListModel
		extends AbstractListModel
	{

		/** The header values. */
		private String[] headers;
    
		/**
		 * Instantiate the header values with a count from n to 1. 
		 * @param n see above.
		 */
		public HeaderListModel(int n)
		{
			headers = new String[n];
			for (int i = 0; i<n; i++) 
				headers[i] = ""+(n-i);
		}
    
		/** 
		 * Get the size of the header. 
		 * @return see above.
		 */
		public int getSize(){ return headers.length; }
    
		/** 
		 * Get the header object at index.
		 * @param index see above. 
		 * @return see above.
		 */
		public Object getElementAt(int index) { return headers[index]; }
    
	}

	/**
	 * The renderer for the row header. 
	 */
	class RowHeaderRenderer
    	extends JLabel 
    	implements ListCellRenderer
    {
    
		/** 
		 * Instantiate row renderer for table.
		 * @param table see above.
		 */
		public RowHeaderRenderer(JTable table)
		{
			if (table != null) 
			{
				JTableHeader header = table.getTableHeader();
				setOpaque(true);
				setBorder(UIManager.getBorder("TableHeader.cellBorder"));
				setHorizontalAlignment(CENTER);
				setHorizontalTextPosition(CENTER);
				setForeground(header.getForeground());
				setBackground(header.getBackground());
				setFont(header.getFont());
			}
		}
    
		/**
		 * Return the component for the renderer.
		 * @param list the list containing the headers render context.
		 * @param value the value to be rendered.
		 * @param index the index of the rendered object. 
		 * @param isSelected is the  current header selected.
		 * @param cellHasFocus has the cell focus.
		 * @return the render component. 
		 */
		public Component getListCellRendererComponent(JList list, Object value, 
            int index, boolean isSelected, boolean cellHasFocus)
		{
			setText((value == null) ? "" : value.toString());
			System.err.println("Set Text : " + this.getText());
			return this;
		}
    
    }
}
