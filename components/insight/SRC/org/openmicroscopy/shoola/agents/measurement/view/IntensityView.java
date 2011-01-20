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
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

//Third-party libraries

//Application-internal dependencies
import org.jhotdraw.draw.Figure;
import org.openmicroscopy.shoola.agents.measurement.IconManager;
import org.openmicroscopy.shoola.agents.measurement.MeasurementAgent;
import org.openmicroscopy.shoola.util.file.ExcelWriter;
import org.openmicroscopy.shoola.util.filter.file.ExcelFilter;
import org.openmicroscopy.shoola.util.image.geom.Factory;
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
import org.openmicroscopy.shoola.util.roi.model.annotation.MeasurementAttributes;
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;
import org.openmicroscopy.shoola.util.ui.ColorListRenderer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.filechooser.FileChooser;
import org.openmicroscopy.shoola.util.ui.slider.OneKnobSlider;
import org.openmicroscopy.shoola.agents.measurement.util.ChannelSummaryModel;
import org.openmicroscopy.shoola.agents.measurement.util.ChannelSummaryTable;
import org.openmicroscopy.shoola.agents.measurement.util.TabPaneInterface;
import org.openmicroscopy.shoola.agents.measurement.util.model.AnalysisStatsWrapper;
import org.openmicroscopy.shoola.agents.measurement.util.model.AnalysisStatsWrapper.StatsType;
import org.openmicroscopy.shoola.env.log.Logger;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import pojos.ChannelData;

/** 
 * Displays statistics computed on the pixels intensity value of a given 
 * ROI shape.
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

	/** Width of the text field components. */
	public final static int		TEXT_WIDTH = 100;

	/** Height of the text field components. */
	public final static int		TEXT_HEIGHT = 26;

	/** Width of label components. */
	public final static int		LABEL_WIDTH = 60;

	/** Height of label components. */
	public final static int		LABEL_HEIGHT = 26;
	
	/** The initial size of the intensity table dialog. */
	private Dimension intensityTableSize = new Dimension(300, 300);

	/** The state of the Intensity View. */
	static enum State 
	{
		/** Analysing data. */
		ANALYSING,
		
		/** Ready to analyse. */
		READY
	}
	
	/** 
	 * Intensity view state, if Analysing we should not all the user to 
	 * change combobox or save. 
	 */
	private State						state = State.READY;
	
	/** The name of the panel. */
	private static final String			NAME = "Intensity View";

	/** The save button action command. */
	private static final int			SAVE_ACTION = 0;
	
	/** The show table action button. */
	private static final int			SHOW_TABLE_ACTION = 1;
	
	/** The channel selection action command. */
	private static final int			CHANNEL_SELECTION = 2;
	
	/** Reference to the model. */
	private MeasurementViewerModel		model;
	
	/** Reference to the view. */
	private MeasurementViewerUI			view;

	/** 
	 * SelectChannelsForm the form to select the channels to output to the 
	 * file. 
	 */
	private	ChannelSelectionForm		channelsSelectionForm;
	
	/** The map of <ROIShape, ROIStats> .*/
	private Map							ROIStats;
	
	/** Table for summary of measurement values. */
	private ChannelSummaryTable			channelSummaryTable;

	/** Model for summary of measurement values. */
	private ChannelSummaryModel			channelSummaryModel;
	
	/** The channel selected in the Combo box. */
	private int 						selectedChannel=-1;

	/** The name of the channel selected in the Combo box. */
	private String 						selectedChannelName;
	
	/** Select to choose the channel to show values for . */
	private JComboBox 					channelSelection;
	
	/** The save button. */
	private JButton 					saveButton; 

	/** The slider controlling the movement of the analysis through Z. */
	private OneKnobSlider 				zSlider;

	/** The slider controlling the movement of the analysis through T. */
	private OneKnobSlider 				tSlider;
	
	/** List of the channel names. */
	private Map<Integer, String> channelName = new TreeMap<Integer, String>();
	
	/** List of the channel colours. */
	private Map<Integer, Color> channelColour = new TreeMap<Integer, Color>();
	
	/** Map of the channel sums, for each selected channel. */
	private Map<Integer, Double> channelSum = new TreeMap<Integer, Double>();
	
	/** Map of the channel minimum, for each selected channel. */
	private Map<Integer, Double> channelMin = new TreeMap<Integer, Double>();
	
	/** Map of the channel Max, for each selected channel. */
	private Map<Integer, Double> channelMax = new TreeMap<Integer, Double>();
	
	/** Map of the channel Mean, for each selected channel. */
	private Map<Integer, Double> channelMean = new TreeMap<Integer, Double>();
	
	/** Map of the channel std. dev., for each selected channel. */
	private Map<Integer, Double> channelStdDev = new TreeMap<Integer, Double>();
	
	/** Map of the channel name to channel number .*/
	private Map<String, Integer> nameMap = new LinkedHashMap<String, Integer>();
	
	/** The map of the shape stats to coord. */
	private TreeMap<Coord3D, Map<StatsType, Map>> shapeStatsList;

	/** Map of the pixel intensity values to coordinates. */
	private TreeMap<Coord3D, Map<Integer, Map<Point, Double>>> pixelStats;
	
	/** Map of the minimum channel intensity values to coordinates. */
	private TreeMap<Coord3D, Map<Integer, Double>> minStats;

	/** Map of the max channel intensity values to coordinates. */
	private TreeMap<Coord3D, Map<Integer, Double>> maxStats;

	/** Map of the mean channel intensity values to coordinates. */
	private TreeMap<Coord3D, Map<Integer, Double>> meanStats;
	
	/** Map of the std dev channel intensity values to coordinates. */
	private TreeMap<Coord3D, Map<Integer, Double>> stdDevStats;
	
	/** Map of the sum channel intensity values to coordinates. */
	private TreeMap<Coord3D, Map<Integer, Double>> sumStats;
	
	/** Map of the coordinates to a shape. */
	private TreeMap<Coord3D, ROIShape> shapeMap;
	
	/** The current coordinates of the ROI being depicted in the slider. */
	private Coord3D 					coord;
	
	/** Button for the calling of the intensity table. */
	private JButton 					showIntensityTable;
	
	/** Current ROIShape. */
	private ROIShape 					shape;
	
	/** Dialog showing the intensity values for the selected channel. */
	private IntensityValuesDialog 		intensityDialog;
	
	/** Table Model. */
	private IntensityModel				tableModel;
	
	/** The slider has changed value and the mouse button released. */
	private void handleSliderReleased()
	{
		stateChanged(null);
	}
	
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
		channelSummaryModel = new ChannelSummaryModel(rowNames, columnNames, 
														summaryData);
		channelSummaryTable = new ChannelSummaryTable(channelSummaryModel);
	
		showIntensityTable = new JButton("Intensity Values");
		showIntensityTable.setEnabled(false);
		showIntensityTable.addActionListener(this);
		showIntensityTable.setActionCommand(""+SHOW_TABLE_ACTION);
		channelSelection = new JComboBox();
		channelSelection.setEnabled(false);
		channelSelection.addActionListener(this);
		channelSelection.setActionCommand(""+CHANNEL_SELECTION);
		saveButton = new JButton("Export to Excel");
		saveButton.addActionListener(this);
		saveButton.setActionCommand(""+SAVE_ACTION);
		saveButton.setEnabled(false);
		state = State.READY;

		zSlider = new OneKnobSlider();
		zSlider.setOrientation(JSlider.VERTICAL);
		zSlider.setPaintTicks(false);
		zSlider.setPaintLabels(false);
		zSlider.setMajorTickSpacing(1);
		zSlider.addMouseListener(new MouseAdapter()
		{
			public void mouseReleased(MouseEvent e)
			{
				handleSliderReleased();
			}
		});
		zSlider.setShowArrows(true);
		zSlider.setVisible(false);
		zSlider.setEndLabel("Z");
		zSlider.setShowEndLabel(true);

		tSlider = new OneKnobSlider();
		tSlider.setPaintTicks(false);
		tSlider.setPaintLabels(false);
		tSlider.setMajorTickSpacing(1);
		tSlider.setSnapToTicks(true);
		tSlider.addMouseListener(new MouseAdapter()
		{
			public void mouseReleased(MouseEvent e)
			{
				handleSliderReleased();
			}
		});
		tSlider.setShowArrows(true);
		tSlider.setVisible(false);	
		tSlider.setEndLabel("T");
		tSlider.setShowEndLabel(true);
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		JPanel scrollPanel = new JPanel();
		JPanel containerPanel = new JPanel();
		containerPanel.setLayout(new BoxLayout(containerPanel, 
				BoxLayout.X_AXIS));
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
		intensityDialog = new IntensityValuesDialog(tableModel);	
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
		JPanel channelPanel = 
			UIUtilities.buildComponentPanel(channelSelection);
		UIUtilities.setDefaultSize(channelPanel, new Dimension(175, 32));
		panel.add(channelPanel);
		panel.add(Box.createRigidArea(new Dimension(0,10)));
		JPanel intensityPanel = 
			UIUtilities.buildComponentPanel(showIntensityTable);
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
	
	/** Clears the maps just incase the data is not being reassigned. */
	private void clearMaps()
	{
		if (shapeStatsList != null)
			shapeStatsList.clear();
		shapeStatsList = null;
		if (pixelStats != null)
			pixelStats.clear();
		pixelStats = null;
		if (shapeMap != null)
			shapeMap.clear();
		shapeMap = null;
		if (maxStats != null)
			maxStats.clear();
		maxStats = null;
		if (meanStats != null)
			meanStats.clear();
		meanStats = null;
		if (minStats != null)
			minStats.clear();
		minStats = null;
		if (sumStats != null)
			sumStats.clear();
		sumStats = null;
		if (stdDevStats != null)
			stdDevStats.clear();	
		stdDevStats = null;
	}



	/** Clear the combo box. */
	private void clearAllValues()
	{
		channelSelection.removeAllItems();
	}
	
	/** Creates the combobox holding the channel list. */
	private void createComboBox()
	{
		Object[][] channelCols = new Object[channelName.size()][2];
		List<ChannelData> metadata = model.getMetadata();
		Iterator<ChannelData> i = metadata.iterator();
		ChannelData channelData;
		int channel;
		int index = 0;
		while (i.hasNext()) {
			channelData = i.next();
			channel = channelData.getIndex();
			if (channelName.containsKey(channel)) 
			{
				channelCols[index] = new Object[]{ channelColour.get(channel), 
						channelName.get(channel) };
				index++;
			}
		}
		//if (channelCols.length == 0)
			//return;
		
		channelSelection.setModel(new DefaultComboBoxModel(channelCols));	
		channelSelection.setRenderer(new ColorListRenderer());
		if (selectedChannelName != null)
			if (nameMap.containsKey(selectedChannelName))
				selectedChannel = nameMap.get(selectedChannelName);
			else
				selectedChannel = 0;
		else
			selectedChannel = 0;
		if (selectedChannel >= channelSelection.getItemCount() 
				|| selectedChannel < 0)
			return;
		channelSelection.setSelectedIndex(selectedChannel);
		channelSelection.setEnabled(true);
	}
	
	/** 
	 * Populates the table and fields with the data.
	 *  
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
		List<String> statNames = new ArrayList<String>();
		List<String> channelNames = new ArrayList<String>();
		ROIFigure fig = shape.getFigure();
		int count = 0;
		statNames.add("Min");
		statNames.add("Max");
		statNames.add("Sum");
		statNames.add("Mean");
		statNames.add("Std Dev.");
		statNames.add("NumPixels");
		if (areaFigure(fig))
			addAreaStats(statNames);
		else if (lineFigure(fig))
			addLineStats(statNames);
		else if (pointFigure(fig))
			addPointStats(statNames);
		Iterator<Integer> channelIterator = channelName.keySet().iterator();
		while (channelIterator.hasNext())
			channelNames.add(channelName.get(channelIterator.next()));
		Double data[][] = new Double[channelName.size()][statNames.size()];
		
		channelIterator = channelName.keySet().iterator();
		int channel;
		count = 0;
		while (channelIterator.hasNext())
		{
			channel = channelIterator.next();
			populateSummaryColumn(fig, data, channel, count);
			count++;
		}
		
		channelSummaryModel = new ChannelSummaryModel(statNames, channelNames, 
				data);
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
	private void populateSummaryColumn(ROIFigure fig, Double data[][], 
			int channel, int count)
	{
		data[count][0] = channelMin.get(channel);
		data[count][1] = channelMax.get(channel);
		data[count][2] = channelSum.get(channel);
		data[count][3] = channelMean.get(channel);
		data[count][4] = channelStdDev.get(channel);
		data[count][5] = (double)fig.getPoints().size();
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
	 * @param count the column in the table being populated.
	 */
	private void addValuesForAreaFigure(ROIFigure fig, Double data[][], 
			int channel, int count)
	{
		data[count][6] = AnnotationKeys.AREA.get(fig.getROIShape());
		data[count][7] = fig.getBounds().getX();
		data[count][8] = fig.getBounds().getY();
		data[count][9] = AnnotationKeys.WIDTH.get(fig.getROIShape());
		data[count][10] = AnnotationKeys.HEIGHT.get(fig.getROIShape());
		data[count][11] = AnnotationKeys.CENTREX.get(fig.getROIShape());
		data[count][12] = AnnotationKeys.CENTREY.get(fig.getROIShape());
	}
	
	/**
	 * Add stats in the column for line figures.
	 * @param fig the figure where the stats come from. 
	 * @param data the data being populated.
	 * @param channel the channel where the stats come from/.
	 * @param count the column in the table being populated.
	 */
	private void addValuesForLineFigure(ROIFigure fig, Double data[][], 
			int channel, int count)
	{
		data[count][6] = AnnotationKeys.STARTPOINTX.get(shape);
		data[count][7] = AnnotationKeys.STARTPOINTY.get(shape);
		data[count][8] = AnnotationKeys.ENDPOINTX.get(shape);
		data[count][9] = AnnotationKeys.ENDPOINTY.get(shape);
		data[count][10] = AnnotationKeys.CENTREX.get(shape);
		data[count][11] = AnnotationKeys.CENTREY.get(shape);
	}
	

	/**
	 * Add stats in the column for point figures.
	 * @param fig the figure where the stats come from. 
	 * @param data the data being populated.
	 * @param channel the channel where the stats come from/.
	 * @param count the column in the table being populated.
	 */
	private void addValuesForPointFigure(ROIFigure fig, Double data[][], 
			int channel, int count)
	{
		data[count][6] = AnnotationKeys.CENTREX.get(shape);
		data[count][7] = AnnotationKeys.CENTREY.get(shape);
	}
	
	/**
	 * Sets the rows describing the stats being displayed in the summary table. 
	 * @param statNames The list of stats being displayed in the summary table.
	 */
	private void addAreaStats(List<String> statNames)
	{
		statNames.add("Area");
		statNames.add("X Coord");
		statNames.add("Y Coord");
		statNames.add("Width");
		statNames.add("Height");
		statNames.add("X Centre");
		statNames.add("Y Centre");
	}

	/**
	 * Sets the rows describing the stats being displayed in the summary table. 
	 * 
	 * @param statNames The list of stats being displayed in the summary table.
	 */
	private void addLineStats(List<String> statNames)
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
	 *  
	 * @param statNames The list of stats being displayed in the summary table.
	 */
	private void addPointStats(List<String> statNames)
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
		Map<Point, Double> pixels = pixelStats.get(coord).get(channel);
		if (pixels == null) return;
		Iterator<Point> pixelIterator = pixels.keySet().iterator();
		double minX, maxX, minY, maxY;
		if (!pixelIterator.hasNext()) return;
		Point point = pixelIterator.next();
		minX = (point.getX());
		maxX = (point.getX());
		minY = (point.getY());
		maxY = (point.getY());
		while (pixelIterator.hasNext())
		{
			point = pixelIterator.next();
			minX = Math.min(minX, point.getX());
			maxX = Math.max(maxX, point.getX());
			minY = Math.min(minY, point.getY());
			maxY = Math.max(maxY, point.getY());
		}
		int sizeX, sizeY;
		sizeX = (int) (maxX-minX)+1;
		sizeY = (int) ((maxY-minY)+1);
		Double[][] data = new Double[sizeX][sizeY];
		pixelIterator = pixels.keySet().iterator();
		int x, y;
		Double value;
		while (pixelIterator.hasNext())
		{
			point = pixelIterator.next();
			x = (int) (point.getX()-minX);
			y = (int) (point.getY()-minY);
			if (x >= sizeX || y >= sizeY) continue;
			
			if (pixels.containsKey(point)) value = pixels.get(point);
			else value = new Double(0);
			data[x][y] = value;
		}
		channelMin = minStats.get(coord);
		channelMax = maxStats.get(coord);
		channelMean = meanStats.get(coord);
		channelStdDev = stdDevStats.get(coord);
		channelSum = sumStats.get(coord);
		tableModel = new IntensityModel(data);
		shape = shapeMap.get(coord);
		intensityDialog.setModel(tableModel);
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
	
	/** Save the results to an Excel File. */
	private void saveResults() 
	{
		channelsSelectionForm = new ChannelSelectionForm(channelName);
		ArrayList<FileFilter> filterList=new ArrayList<FileFilter>();
		FileFilter filter=new ExcelFilter();
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
		
		if (!file.getAbsolutePath().endsWith(ExcelFilter.EXCEL))
		{
			String fileName = file.getAbsolutePath()+"."+ExcelFilter.EXCEL;
			file = new File(fileName);
		}
		
		List<Integer> channels = channelsSelectionForm.getUserSelection();
		if (channels == null || channels.size() == 0) {
			UserNotifier un = MeasurementAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Save Results", " Please select at least a channel.");
			view.setStatus("No Channel selected to output.");
			
			return;
		}
				
		try
		{
			ExcelWriter writer = new ExcelWriter(file.getAbsolutePath());
			writer.openFile();
			writer.createSheet("Channel Summary");
			Iterator<Coord3D> coordMapIterator = shapeMap.keySet().iterator();
			Coord3D currentCoord;
			int n = channels.size();
			Integer channel;
			if (channelSummarySelected(channels))
				outputSummary(writer, shapeMap);
			BufferedImage originalImage = model.getRenderedImage();
			BufferedImage image = Factory.copyBufferedImage(originalImage);
			
			// Add the ROI for the current plane to the image.
			//TODO: Need to check that.
			model.setAttributes(MeasurementAttributes.SHOWID, true);
			model.getDrawingView().print(image.getGraphics());
			model.setAttributes(MeasurementAttributes.SHOWID, false);
			try {
				writer.addImageToWorkbook("ThumbnailImage", image); 
			} catch (Exception e) {
				//TODO
			}
			int col = writer.getMaxColumn(0);
			writer.writeImage(0, col+1, 256, 256, "ThumbnailImage");

			if (channelSummarySelected(channels) && channels.size() != 1)
				while (coordMapIterator.hasNext())
				{
					currentCoord = coordMapIterator.next();
					for (int i = 0 ; i < n ; i++)
					{
						channel = channels.get(i);
						if (channel == ChannelSelectionForm.SUMMARYVALUE)
							continue;
						if (!nameMap.containsKey(channelName.get(channel)))
							continue;
						int rowIndex = 0;
						
						writer.createSheet("Channel Number "+
								channelName.get(channel));
						writeHeader(writer, rowIndex, currentCoord);
						channel = nameMap.get(channelName.get(channel));
						writeData(writer, rowIndex, currentCoord, 
								channel.intValue());
					}
				}
			writer.close();
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
	 * Create summary table with horizontal columns.
	 * 
	 * @param writer 	The Excel writer.
	 * @param rowIndex 	The selected row.
	 */
	private void printSummaryHeader(ExcelWriter writer, int rowIndex) 
	{
		writer.writeElement(rowIndex, 0, "channel");
		writer.writeElement(rowIndex, 1, "zsection");
		writer.writeElement(rowIndex, 2, "time");
		for (int y = 0 ; y < channelSummaryTable.getRowCount() ; y++)
				writer.writeElement(rowIndex, 3+y, 
						channelSummaryTable.getValueAt(y, 0));
	}
	
	/**
	 * Outputs the summary information from the shape map.
	 * 
	 * @param writer 	The Excel writer.
	 * @param shapeMap see above.
	 * @throws IOException
	 */
	private void outputSummary(ExcelWriter writer, TreeMap<Coord3D, 
			ROIShape> shapeMap) 
	{
		int rowIndex = 0;
		printSummaryHeader(writer, rowIndex);
		rowIndex++;
		Coord3D start = shapeMap.firstKey();
		Coord3D end = shapeMap.lastKey();
		Coord3D coord;
		List<Integer> channels = new ArrayList<Integer>(channelName.keySet());
		for (Integer c : channels)
		{
			for (int z = start.getZSection() ; z<= end.getZSection(); z++)
				for (int t = start.getTimePoint() ; t <= end.getTimePoint(); 
					t++)
				{
					coord = new Coord3D(z, t);
					populateData(coord, c);
					outputSummaryRow(writer,rowIndex, c, z, t);
					rowIndex++;
				}
		}
	}

	
	/**
	 * Adds the any remaining fields (min, max, mean, stdDev) to the file being
	 * saved. 
	 * 
	 * @param writer 	The Excel writer.
	 * @param rowIndex 	The selected row.
	 * @param channel 	The channel to output. 
	 * @param z z-section to output.
	 * @param t timepoint to output.
	 */
	private void outputSummaryRow(ExcelWriter writer, int rowIndex, 
			Integer channel, int z, int t) 
	{
		writer.writeElement(rowIndex, 0, channelName.get(channel));
		writer.writeElement(rowIndex, 1, z+"");
		writer.writeElement(rowIndex, 2, t+"");
		int col;
		for(int y = 0 ; y < channelSummaryTable.getRowCount() ; y++)
		{
			col = getColumn(channelName.get(channel));
			if (col == -1)
				continue;
			writer.writeElement(rowIndex, 3+y, 
					new Double((String)channelSummaryTable.getValueAt(y, col)));
		}
	}
	
	/** 
	 * Returns the column for the name equal to the string.
	 * 
	 * @param name see above.
	 * @return see above.
	 */
	private int getColumn(String name)
	{
		for (int i = 0 ; i < channelSummaryModel.getColumnCount(); i++)
			if (channelSummaryModel.getColumnName(i).equals(name))
				return i;
		return -1;
	}

	/**
	 * Returns <code>true</code> if the user has selected the summary channel,
	 * <code>false</code> otherwise.
	 * 
	 * @param selection see above.
	 * @return see above.
	 */
	private boolean channelSummarySelected(List<Integer> selection)
	{
		for (Integer value: selection)
			if (value == ChannelSelectionForm.SUMMARYVALUE)
				return true;
		return false;
	}
	
	/**
	 * Writes the header information for the file, image, projects, dataset.
	 * 
	 * @param writer 	The Excel writer.
	 * @param rowIndex 	The selected row.
	 * @param currentCoord  The coord of the shape being written.
	 * @throws IOException Thrown if the data cannot be written.
	 */
	private void writeHeader(ExcelWriter writer, int rowIndex, 
			Coord3D currentCoord) 
	{
		writer.writeElement(rowIndex, 0 , "Image ");
		writer.writeElement(rowIndex, 1 , model.getImageName());
		rowIndex++;
		writer.writeElement(rowIndex, 0 , "Z ");
		writer.writeElement(rowIndex, 1 , (currentCoord.getZSection()+1));
		rowIndex++;
		writer.writeElement(rowIndex, 0 , "T ");
		writer.writeElement(rowIndex, 1 , (currentCoord.getTimePoint()+1));
		rowIndex++;
	}
	
	/** 
	 * Writes the channel intensities and stats to the files.
	 * 
	 * @param writer 	The Excel writer.
	 * @param rowIndex 	The selected row.
	 * @param coord		The specified coordinate.
	 * @param channel	The channel to output.
	 */
	private void writeData(ExcelWriter writer, int rowIndex, Coord3D coord, 
			int channel) 
	{
		populateData(coord, channel);
		writer.writeTableToSheet(rowIndex, 0, tableModel);
	}

	 /** Shows the intensity results dialog. */
	 private void showIntensityResults()
	 {
		 UIUtilities.setLocationRelativeToAndSizeToWindow(this, intensityDialog, 
				 intensityTableSize);
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
	 * create the appropriate table data and summary statistics.  
	 */
	void displayAnalysisResults()
	{
		if (state == State.ANALYSING)
			return;
		this.ROIStats = model.getAnalysisResults();
		if (ROIStats == null || ROIStats.size() == 0) 
			return;
		state = State.ANALYSING;
		
		clearMaps();
		shapeStatsList = new TreeMap<Coord3D, Map<StatsType, Map>>(new Coord3D());
		pixelStats = 
			new TreeMap<Coord3D, Map<Integer, Map<Point, Double>>>(new Coord3D());
		shapeMap = new TreeMap<Coord3D, ROIShape>(new Coord3D());
		minStats = new TreeMap<Coord3D, Map<Integer, Double>>(new Coord3D());
		maxStats = new TreeMap<Coord3D, Map<Integer, Double>>(new Coord3D());
		meanStats = new TreeMap<Coord3D, Map<Integer, Double>>(new Coord3D());
		sumStats = new TreeMap<Coord3D, Map<Integer, Double>>(new Coord3D());
		stdDevStats = new TreeMap<Coord3D, Map<Integer, Double>>(new Coord3D());
		
		Entry entry;
		Iterator j  = ROIStats.entrySet().iterator();
		channelName =  new TreeMap<Integer, String>();
		nameMap = new LinkedHashMap<String, Integer>();

		int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
		int minT = Integer.MAX_VALUE, maxT = Integer.MIN_VALUE;
		clearAllValues();
		Coord3D c3D;
		Map<StatsType, Map> shapeStats;
		ChannelData channelData;
		int channel;
		Iterator<ChannelData> i;
		List<ChannelData> metadata = model.getMetadata();
		while (j.hasNext())
		{
			entry = (Entry) j.next();
			shape = (ROIShape) entry.getKey();
			c3D = shape.getCoord3D();
			minT = Math.min(minT, c3D.getTimePoint());
			maxT = Math.max(maxT, c3D.getTimePoint());
			minZ = Math.min(minZ, c3D.getZSection());
			maxZ = Math.max(maxZ, c3D.getZSection());
			
			shapeMap.put(c3D, shape);
			if (shape.getFigure() instanceof MeasureTextFigure)
			{
				state = State.READY;
				return;
			}
	
			shapeStats = AnalysisStatsWrapper.convertStats(
										(Map) entry.getValue());
			shapeStatsList.put(c3D, shapeStats);

			minStats.put(c3D, shapeStats.get(StatsType.MIN));
			maxStats.put(c3D, shapeStats.get(StatsType.MAX));
			meanStats.put(c3D, shapeStats.get(StatsType.MEAN));
			sumStats.put(c3D, shapeStats.get(StatsType.SUM));
			stdDevStats.put(c3D, shapeStats.get(StatsType.STDDEV));
			pixelStats.put(c3D, shapeStats.get(StatsType.PIXEL_PLANEPOINT2D));
			
			/* really inefficient but hey.... quick hack just now till refactor */
			channelName.clear();
			nameMap.clear();
			channelColour.clear();
			
			
			i = metadata.iterator();
			List<String> names = new ArrayList<String>();
			String name;
			while (i.hasNext()) {
				channelData = i.next();
				channel = channelData.getIndex();
				if (model.isChannelActive(channel)) 
				{
					name = channelData.getChannelLabeling();
					if (names.contains(name)) name += " "+channel;
					channelName.put(channel, name);
					nameMap.put(channelName.get(channel), channel);
					channelColour.put(channel, 
						(Color) model.getActiveChannels().get(channel));
				}
			}
		}
		if (channelName.size() != channelColour.size() || nameMap.size() == 0)
		{
			createComboBox();
			List<String> names = channelSummaryModel.getRowNames();
			List<String> channelNames = new ArrayList<String>();
			Double data[][] = new Double[channelName.size()][names.size()];
			channelSummaryModel = new ChannelSummaryModel(names, channelNames, 
					data);
			channelSummaryTable.setModel(channelSummaryModel);
			saveButton.setEnabled(false);
			showIntensityTable.setEnabled(false);
			if (intensityDialog != null) intensityDialog.setVisible(false);
			state = State.READY;
			return;
		}
		saveButton.setEnabled(true);
		showIntensityTable.setEnabled(true);
		maxZ = maxZ+1;
		minZ = minZ+1;
		maxT = maxT+1;
		minT = minT+1;
		
		createComboBox();
		Object[] nameColour = (Object[]) channelSelection.getSelectedItem();
		String string = (String) nameColour[1];
		selectedChannel = nameMap.get(string);
		zSlider.setMaximum(maxZ);
		zSlider.setMinimum(minZ);
		tSlider.setMaximum(maxT);
		tSlider.setMinimum(minT);
		zSlider.setVisible((maxZ != minZ));
		tSlider.setVisible((maxT != minT));
		tSlider.setValue(model.getCurrentView().getTimePoint()+1);
		zSlider.setValue(model.getCurrentView().getZSection()+1);
		coord = new Coord3D(zSlider.getValue()-1, tSlider.getValue()-1);
		shape = shapeMap.get(coord);
		populateData(coord, selectedChannel);	
		state = State.READY;
		int row = tableModel.getRowCount();
		saveButton.setEnabled(row > 0);
	}

	/** Invokes when figures are selected. */
	void onFigureSelected()
	{
		Set<Figure> selectedFigures = 
			view.getDrawingView().getSelectedFigures();
		if (selectedFigures == null || selectedFigures.size() == 0) {
			int row = tableModel.getRowCount();
			showIntensityTable.setEnabled(row > 0);
			channelSelection.setEnabled(row > 0);
		} else {
			int size = 0;
			size = channelSelection.getModel().getSize();
			channelSelection.setEnabled(size > 0);
			showIntensityTable.setEnabled(size > 0);
		}
	}
	
	/** 
	 * Reacts to the controls.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	 public void actionPerformed(ActionEvent e) 
	 {
		if (state == State.ANALYSING)
			return;
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case CHANNEL_SELECTION:
				JComboBox cb = (JComboBox)e.getSource();
				Object[] nameColour = (Object[])cb.getSelectedItem();
				String string = (String)nameColour[1];
				if (!nameMap.containsKey(string))
					return;
				selectedChannelName = string;
				int channel = nameMap.get(string);
				if (channel != -1)
				{
					Coord3D newCoord = new Coord3D(zSlider.getValue()-1, 
							tSlider.getValue()-1);
					populateData(newCoord, channel);
					repaint();
				}
				break;
	
			case SAVE_ACTION:
				saveResults();
				break;
			case SHOW_TABLE_ACTION:
				showIntensityResults();
		}
	 }

	/**
	 * Reacts to slider moves.
	 * @see ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
	{
		if (zSlider == null || tSlider == null || coord == null || 
				state != State.READY)
			return;
		Coord3D thisCoord = new Coord3D(zSlider.getValue()-1, 
				tSlider.getValue()-1);
		if (coord.equals(thisCoord)) return;
		if (!pixelStats.containsKey(thisCoord)) return;
	
		Object[] nameColour = (Object[]) channelSelection.getSelectedItem();
		String string = (String)nameColour[1];
		if (!nameMap.containsKey(string))
		{
			state=State.READY;
			return;
		}
		selectedChannelName = string;
		int channel = nameMap.get(string);
		if (channel == -1)
			return;
		if (!pixelStats.get(thisCoord).containsKey(channel))
			return;
		
		state = State.ANALYSING;
		populateData(thisCoord, channel);
		repaint();
		if (shape!=null)
			view.selectFigure(shape.getFigure());
		state = State.READY;
	}
	
	/**
	 * Overridden version of {@line TabPaneInterface#getIndex()}
	 * @return the index.
	 */
	public int getIndex() {return INDEX; }
}
