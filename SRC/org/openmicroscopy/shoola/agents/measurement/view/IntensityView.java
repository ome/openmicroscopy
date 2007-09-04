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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.measurement.IconManager;
import org.openmicroscopy.shoola.env.rnd.roi.ROIShapeStats;
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
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.agents.measurement.util.ColourListRenderer;
import org.openmicroscopy.shoola.agents.measurement.util.TabPaneInterface;

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
	implements ActionListener, TabPaneInterface
{
	/** Index to identify tab */
	public final static int		INDEX = MeasurementViewerUI.INTENSITY_INDEX;
	
	/** The state of the Intensity View. */
	enum State 
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
	
	/** The name of the panel. */
	private static final String			CHANNELSELECTION = "CHANNELSELECTION";
	
	/** Reference to the control. */
	private MeasurementViewerControl	controller;
	
	/** Reference to the model. */
	private MeasurementViewerModel		model;

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

	
	/** Select to choose the channel to show values for . */
	private JComboBox 					channelSelection;
	
	/** The save button. */
	private JButton 					saveButton; 
	
	/** list of the channel names. */
	private Map<Integer, String> channelName = new TreeMap<Integer, String>();
	
	/** List of the channel colours. */
	private Map<Integer, Color> channelColour = new TreeMap<Integer, Color>();
	
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
	
	
	/** Current ROIShape. */
	private 	ROIShape shape;
	
	/**
	 * overridden version of {@line TabPaneInterface#getIndex()}
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
		if(state==State.ANALYSING)
			return;
		state = state.ANALYSING;
		this.ROIStats = model.getAnalysisResults();
		if(ROIStats==null || ROIStats.size() == 0)
			return;
		Iterator shapeIterator  = ROIStats.keySet().iterator();
		channelName =  new TreeMap<Integer, String>();
		nameMap = new HashMap<String, Integer>();
		channelColour =  new TreeMap<Integer, Color>();
		channelMin = new TreeMap<Integer, Double>();
		channelMax = new TreeMap<Integer, Double>();
		channelMean = new TreeMap<Integer, Double>();
		channelStdDev = new TreeMap<Integer, Double>();
		planePixels = new TreeMap<Integer, Map<PlanePoint2D, Double>>();
		int channel;
		ROIShapeStats stats;
		while(shapeIterator.hasNext())
		{
			shape = (ROIShape) shapeIterator.next();
			if(shape.getFigure() instanceof MeasureTextFigure)
				return;
			Map shapeStats = (Map) ROIStats.get(shape);
			Iterator channelIterator = shapeStats.keySet().iterator();
			clearAllVariables();
			clearAllValues();
			while (channelIterator.hasNext())
			{
				channel = (Integer) channelIterator.next();
				stats = (ROIShapeStats) shapeStats.get(channel);
				channelMin.put(channel, stats.getMin());
				channelMax.put(channel, stats.getMax());
				channelMean.put(channel, stats.getMean());
				channelStdDev.put(channel, stats.getStandardDeviation());
				planePixels.put(channel, stats.getPixelsValue());
				channelName.put(channel,
					model.getMetadata(channel).getEmissionWavelength()+"");
				nameMap.put(channelName.get(channel), channel);
				channelColour.put(channel, 
					(Color)model.getActiveChannels().get(channel));
			}
		}
		
		createComboBox();
		Object[] nameColour = (Object[])channelSelection.getSelectedItem();
		String string = (String)nameColour[1];
		int selectedChannel = nameMap.get(string);
		populateData(selectedChannel);
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
		channelSelection.setSelectedIndex(0);
	}
	
	/** Populate the table and fields with the data. */
	private void populateData(int channel)
	{
		populateTable(channel);
		populateFields(channel);
		state=State.READY;
	}

	/** Populate the table with the data. */
	private void populateTable(int channel)
	{
		Map<PlanePoint2D, Double> pixels = 
							planePixels.get(channel);
	
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
		tableModel = new IntensityModel(data);
		table.setModel(tableModel);
	}
	
	/** Populate the fields with the data.
	 * @param channel The channel for the stats.
	 */
	private void populateFields(int channel)
	{
		minValue.setText(FormatString(channelMin.get(channel)));
		maxValue.setText(FormatString(channelMax.get(channel)));
		meanValue.setText(FormatString(channelMean.get(channel)));
		stdDevValue.setText(FormatString(channelStdDev.get(channel)));
		ROIFigure fig = shape.getFigure();
		if(areaFigure(fig))
		{
			setValuesForAreaFigure(fig);
		}
		else if(lineFigure(fig))
		{
			setValuesForLineFigure(fig);
		}
		else if (pointFigure(fig))
		{
			setValuesForPointFigure(fig);
		}
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
		XCentreValue.setText(FormatString(AnnotationKeys.CENTREX.get(shape)));
		YCentreValue.setText(FormatString(AnnotationKeys.CENTREY.get(shape)));
	}

	/**
	 * Set the values of the labels and textfield for line figures. 
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
		XCentreValue.setText(FormatString(AnnotationKeys.CENTREX.get(shape)));
		YCentreValue.setText(FormatString(AnnotationKeys.CENTREY.get(shape)));
	}
	
	/**
	 * Set the values of the labels and textfield for point figures. 
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
		XCentreValue.setText(FormatString(AnnotationKeys.CENTREX.get(shape)));
		YCentreValue.setText(FormatString(AnnotationKeys.CENTREY.get(shape)));
	}
	
	/**
	 * Is the param an area figure. 
	 * @param fig The param. 
	 * @return See above.
	 */
	private boolean areaFigure(ROIFigure fig)
	{
		if(fig instanceof MeasureEllipseFigure ||
			fig instanceof MeasureRectangleFigure)
			return true;
		if(	fig instanceof MeasureBezierFigure)
		{
			MeasureBezierFigure bFig = (MeasureBezierFigure)fig;
			if(bFig.isClosed())
				return true;
		}
		return false;
	}
	
	/**
	 * Is the param an line figure. 
	 * @param fig The param. 
	 * @return See above.
	 */
	private boolean lineFigure(ROIFigure fig)
	{
		if(fig instanceof MeasureLineFigure ||
			fig instanceof MeasureLineConnectionFigure)
			return true;
		if(	fig instanceof MeasureBezierFigure)
		{
			MeasureBezierFigure bFig = (MeasureBezierFigure)fig;
			if(!bFig.isClosed())
				return true;
		}
		return false;
	}
	
	/**
	 * Is the param an point figure. 
	 * @param fig The param. 
	 * @return See above.
	 */
	private boolean pointFigure(ROIFigure fig)
	{
		if(fig instanceof MeasurePointFigure)
			return true;
		return false;
	}
	
	/**
	 * Create jpanel with Jlabel and JTextField.
	 * @param label See above.
	 * @param text See above.
	 * @return The jpanel with the label and textfield.
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
	
	/** Format the string to be 2 decimal places. 
	 * @param value the vale to be formatted. 
	 * @return formatted string.
	 */
	private String FormatString(double value)
	{
		try {
			return String.format("%.2f",value);
		} catch (Exception e) {
			return "";
		}
	}
	
	/** Save the results to a csv File. */
	private void saveResults() 
	{
		JFileChooser chooser = new JFileChooser();
		FileFilter filter = new CSVFilter();
		chooser.addChoosableFileFilter(filter);
		chooser.setFileFilter(filter);

		File f = UIUtilities.getDefaultFolder();
	    if(f != null) chooser.setCurrentDirectory(f);
		int results = chooser.showSaveDialog(this.getParent());
		if(results != JFileChooser.APPROVE_OPTION) return;
		File file = chooser.getSelectedFile();
		if (!file.getAbsolutePath().endsWith(CSVFilter.CSV))
		{
			String fileName = file.getAbsolutePath()+"."+CSVFilter.CSV;
			file = new File(fileName);
		}
		if (file.exists()) 
		{
			int response = JOptionPane.showConfirmDialog (null,
						"Overwrite existing file?","Confirm Overwrite",
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE);
	        if (response == JOptionPane.CANCEL_OPTION) return;
	    }
		channelsSelectionForm = new ChannelSelectionForm(channelName);
		UIUtilities.setLocationRelativeToAndShow(this, channelsSelectionForm);
		if(channelsSelectionForm.getState()!= 
											ChannelSelectionForm.State.ACCEPTED)
			return;
		List<Integer> userChannelSelection = channelsSelectionForm.
															getUserSelection();
		BufferedWriter out;
		try
		{
			out=new BufferedWriter(new FileWriter(file));
			for( int i = 0 ; i < userChannelSelection.size() ; i++)
			{
				writeTitle(out, "Channel Number : " + 
					channelName.get(userChannelSelection.get(i)));
				if(!nameMap.containsKey(
					channelName.get(userChannelSelection.get(i))))
					continue;
				int channel = nameMap.get(
					channelName.get(userChannelSelection.get(i)));
		
				writeData(out, channel);
			}
			out.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
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
	 * @param channel The channel to output.
	 * @throws IOException Any IO Error.
	 */
	private void writeData(BufferedWriter out, int channel) throws IOException
	{
		populateData(channel);
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
		if(state==State.ANALYSING)
			return;
		if(e.getActionCommand().equals(CHANNELSELECTION))
		{
			JComboBox cb = (JComboBox)e.getSource();
			Object[] nameColour = (Object[])cb.getSelectedItem();
			String string = (String)nameColour[1];
			if(!nameMap.containsKey(string))
				return;
			int channel = nameMap.get(string);
			if(channel!=-1)
			{
				populateData(channel);
				populateFields(channel);
				repaint();
			}
		 }
		 if(e.getActionCommand().equals(SAVEACTION))
		 {
			 saveResults();
		 }
	 }
	 
}
