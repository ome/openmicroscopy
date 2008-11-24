/*
 * org.openmicroscopy.shoola.agents.measurement.view.NewIntensityResultsView 
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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;

//Third-party libraries

//Application-internal dependencies
import org.jhotdraw.draw.Figure;
import org.openmicroscopy.shoola.agents.measurement.IconManager;
import org.openmicroscopy.shoola.agents.measurement.MeasurementAgent;
import org.openmicroscopy.shoola.agents.measurement.util.TabPaneInterface;
import org.openmicroscopy.shoola.agents.measurement.util.model.AnalysisStatsWrapper;
import org.openmicroscopy.shoola.agents.measurement.util.model.AnnotationDescription;
import org.openmicroscopy.shoola.agents.measurement.util.model.AnalysisStatsWrapper.StatsType;
import org.openmicroscopy.shoola.env.log.Logger;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.file.ExcelWriter;
import org.openmicroscopy.shoola.util.filter.file.CSVFilter;
import org.openmicroscopy.shoola.util.math.geom2D.PlanePoint2D;
import org.openmicroscopy.shoola.util.roi.figures.MeasureTextFigure;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys;
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.filechooser.FileChooser;

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
class IntensityResultsView
extends JPanel 
implements TabPaneInterface
{
	
	/** Index to identify tab */
	public final static int		INDEX = MeasurementViewerUI.INTENSITYRESULTVIEW_INDEX;
	
	/** The add button name. */
	private final static String ADDNAME = "Add";
	
	/** Tooltip for the add button. */
	private final static String ADDDESCRIPTION = "Add Intensities for selected ROI to results table.";
	
	/** The remove button name. */
	private final static String REMOVENAME = "Remove";
	
	/** Tooltip for the remove button. */
	private final static String REMOVEDESCRIPTION = "Remove Results in selected row from table.";
	
	/** The save button name. */
	private final static String SAVENAME = "Save";
	
	/** Tooltip for the save button. */
	private final static String SAVEDESCRIPTION = "Save Intensities to CSV File.";
	
	/** Reference to the view. */
	private MeasurementViewerUI			view;
	
	/** The results table. */
	JTable results;
	
	/** The results model for the results table. */
	ResultsTableModel resultsModel;
	
	/** The remove button. */
	JButton removeButton;
	
	/** The save button. */
	JButton saveButton;
	
	/** The add button. */
	JButton addButton;
	
	/** The intensity panel. */
	IntensityView intensityView;
	
	/** The state of the Intensity View. */
	static enum State 
	{
		/** Analysing data. */
		ANALYSING,
		/** Ready to analyse. */
		READY
	}
	
	/** 
	 * Intensity view state, if Analysiing we should not all the user to 
	 * change combobox or save. 
	 */
	private State						state = State.READY;
	
	/** The name of the panel. */
	private static final String			NAME = "Intensity Results View";
	
	/** Reference to the model. */
	private MeasurementViewerModel		model;
	
	/** The map of <ROIShape, ROIStats> .*/
	private Map							ROIStats;
	
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
		state = State.READY;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param view		 Reference to the View. Mustn't be <code>null</code>.
	 * @param model		 Reference to the Model. Mustn't be <code>null</code>.
	 */
	IntensityResultsView(MeasurementViewerUI view, MeasurementViewerModel model)
	{
		if (view == null)
			throw new IllegalArgumentException("No view.");
		if (model == null)
			throw new IllegalArgumentException("No model.");
		this.view = view;
		this.model = model;
		initComponents();
		buildGUI();
	}
	
	private void buildGUI()
	{
		resultsModel = new ResultsTableModel();
		resultsModel.addColumn(AnnotationDescription.ROIID_STRING);
		resultsModel.addColumn("Z");
		resultsModel.addColumn("T");
		resultsModel.addColumn("Channel");
		resultsModel.addColumn("Annotation");
		resultsModel.addColumn("Min");
		resultsModel.addColumn("Max");
		resultsModel.addColumn("Sum");
		resultsModel.addColumn("Mean");
		resultsModel.addColumn("stdDev");
		results = new JTable(resultsModel);
		
		createButtons();
		JPanel centrePanel = new JPanel();
		centrePanel.setLayout(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane(results);
		centrePanel.add(scrollPane, BorderLayout.CENTER);
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new FlowLayout());
		bottomPanel.add(addButton);
		bottomPanel.add(removeButton);
		bottomPanel.add(saveButton);
		JPanel containerPanel = new JPanel();
		containerPanel.setLayout(new BorderLayout());
		containerPanel.add(centrePanel, BorderLayout.CENTER);
		containerPanel.add(bottomPanel, BorderLayout.SOUTH);
		
		this.setLayout(new BorderLayout());
		this.add(containerPanel, BorderLayout.CENTER);
	}
	
	/**
	 * Create the buttons to add, remove and save.
	 *
	 */
	private void createButtons()
	{
		addButton = new JButton(new AddAction());
		saveButton = new JButton(new SaveAction());
		removeButton = new JButton(new RemoveAction());
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
		this.ROIStats = model.getAnalysisResults();
		if (ROIStats == null || ROIStats.size() == 0) return;
		
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
		
		while(shapeIterator.hasNext())
		{
			shape = (ROIShape) shapeIterator.next();
			Map<StatsType, Map> shapeStats;
			
			shapeMap.put(shape.getCoord3D(), shape);
			if (shape.getFigure() instanceof MeasureTextFigure)
			{
				state = State.READY;
				return;
			}
			
			shapeStats = AnalysisStatsWrapper.convertStats(
				(Map) ROIStats.get(shape));
				
			minStats.put(shape.getCoord3D(), shapeStats.get(StatsType.MIN));
			maxStats.put(shape.getCoord3D(), shapeStats.get(StatsType.MAX));
			meanStats.put(shape.getCoord3D(), shapeStats.get(StatsType.MEAN));
			sumStats.put(shape.getCoord3D(), shapeStats.get(StatsType.SUM));
			stdDevStats.put(shape.getCoord3D(), shapeStats.get(StatsType.STDDEV));
			
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
			
			if(channelName.size()==0 || nameMap.size() ==0 || 
				channelColour.size() == 0)
			{
				state = State.READY;
				return;
			}
		
			coord = shape.getCoord3D();
			getResults(shape);
		}
		state = State.READY;
	}
	
	/** Populate the table with the data. 
	 * @param shape the shape to de analysed. 
	 */
	private void getResults(ROIShape shape)
	{
		Vector<Vector> rows = new Vector<Vector>();
		
		Iterator<String> channelIterator = channelName.values().iterator();
		channelMin = minStats.get(coord);
		channelMax = maxStats.get(coord);
		channelMean = meanStats.get(coord);
		channelStdDev = stdDevStats.get(coord);
		channelSum = sumStats.get(coord);	
		while(channelIterator.hasNext())
		{
			String cName = channelIterator.next();
			int channel = nameMap.get(cName);
			Vector rowData = new Vector();
			rowData.add(shape.getID());
			rowData.add(shape.getCoord3D().getZSection()+1);
			rowData.add(shape.getCoord3D().getTimePoint()+1);
			rowData.add(cName);
			rowData.add(AnnotationKeys.TEXT.get(shape));
			rowData.add(channelMin.get(channel));
			rowData.add(channelMax.get(channel));
			rowData.add(channelSum.get(channel));
			rowData.add(channelMean.get(channel));
			rowData.add(channelStdDev.get(channel));
			rows.add(rowData);
		}
		for(Vector rowData : rows)
			resultsModel.addRow(rowData);
		results.repaint();
	}
	
	/**
	 * Save the results of the table to a csv file.
	 *
	 */
	private void saveResults()
	{
		ArrayList<FileFilter> filterList=new ArrayList<FileFilter>();
		FileFilter filter=new CSVFilter();
		filterList.add(filter);
		FileChooser chooser=
			new FileChooser(view, FileChooser.SAVE, "Save Results to Excel", "Save the " +
				"Results data to a file which can be loaded by a spreadsheet.",
				filterList);
		File f = UIUtilities.getDefaultFolder();
		if (f != null) chooser.setCurrentDirectory(f);
		int option = chooser.showDialog();
		if (option != JFileChooser.APPROVE_OPTION) return;
		File  file = chooser.getFormattedSelectedFile();
		
		try
		{
			String filename = file.getAbsolutePath();
			ExcelWriter writer = new ExcelWriter(filename);
			writer.openFile();
			writer.createWorkbook();
			writer.createSheet("Intensity Results");
			writer.writeTableToSheet(0, 0, resultsModel);
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
	 * Remove the selected results from the table.
	 */
	private void removeResults()
	{
		int [] rows = results.getSelectedRows();
		for(int i = rows.length-1 ; i >=0 ; i--)
			resultsModel.removeRow(rows[i]);
	}
	
	/**
	 * Add the statistics from the selected ROI to the table.
	 *
	 */
	private void addResults()
	{
		Set<Figure> selectedFigures = view.getDrawingView().getSelectedFigures();
		if(selectedFigures.size()==0)
			return;
		if(state == State.ANALYSING)
			return;
		state = State.ANALYSING;
		ArrayList<ROIShape> shapeList = new ArrayList<ROIShape>();
		Iterator<Figure> iterator =  selectedFigures.iterator();
		while(iterator.hasNext())
		{
			ROIFigure fig = (ROIFigure)iterator.next();
			if(fig instanceof MeasureTextFigure)
				continue;
			shapeList.add(fig.getROIShape());
		}
		view.calculateStats(shapeList);
		state = state.READY;
	}
	
	/**
	 * The save action, attached to the save button. 
	 */
	class SaveAction
	extends AbstractAction
	{
		
		SaveAction()
		{
			putValue(Action.NAME, SAVENAME);
			putValue(Action.SHORT_DESCRIPTION, 
				UIUtilities.formatToolTipText(SAVEDESCRIPTION));
			
		}
		
		/* (non-Javadoc)
		 * 	@see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e)
		{
			saveResults();
		}
		
	}
	
	/**
	 * The add action, attached to the add button. 
	 */
	class AddAction
	extends AbstractAction
	{
		
		AddAction()
		{
			putValue(Action.NAME, ADDNAME);
			putValue(Action.SHORT_DESCRIPTION, UIUtilities
				.formatToolTipText(ADDDESCRIPTION));
			
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e)
		{
			addResults();
		}
		
	}
	
	/**
	 * The remove action, attached to the remove button. 
	 */
	class RemoveAction
	extends AbstractAction
	{
		
		RemoveAction()
		{
			putValue(Action.NAME, REMOVENAME);
			putValue(Action.SHORT_DESCRIPTION, UIUtilities
				.formatToolTipText(REMOVEDESCRIPTION));
			
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e)
		{
			removeResults();
		}
		
	}
	
	/** 
	 * The table model for the results table, only overridden to make it read 
	 * only.
	 */
	class ResultsTableModel
	extends DefaultTableModel
	{
		public boolean isCellEditable(int row, int col)
		{
			return false;
		}
	}
	
}

