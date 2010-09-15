/*
 * org.openmicroscopy.shoola.env.ui.AnalysisResultsDialog 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.env.ui;


//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.AnalysisResultsHandlingParam;
import org.openmicroscopy.shoola.util.filter.file.CSVFilter;
import org.openmicroscopy.shoola.util.filter.file.CustomizedFileFilter;
import org.openmicroscopy.shoola.util.filter.file.JPEGFilter;
import org.openmicroscopy.shoola.util.filter.file.PNGFilter;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.filechooser.FileChooser;
import org.openmicroscopy.shoola.util.ui.graphutils.ChartObject;
import org.openmicroscopy.shoola.util.ui.graphutils.HistogramPlot;

/** 
 * Displays the results stored in the passed file.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class AnalysisResultsDialog 
	extends JDialog
	implements ActionListener, PropertyChangeListener
{

	/** Bound property indicating that the chart as been saved. */
	static final String SAVED_CHART_PROPERTY = "savedChart";
	
	/** Action ID indicating to close the dialog. */
	private static final int CLOSE = 0;
	
	/** Action ID indicating to save the result. */
	private static final int SAVE = 1;
	
	/** The default color. */
	private static final Color DEFAULT_COLOR = Color.RED;
	
	/** The number of bins. */
	private static final int BINS = 1001;
	
	/** Filters used for the save options. */
	private static List<FileFilter> FILTERS;
	
	static {
		FILTERS = new ArrayList<FileFilter>();
		FILTERS.add(new PNGFilter());
		FILTERS.add(new JPEGFilter());
	}
		
	/** The file to handle. */
	private File file;
	
	/** The parameters indicating how to display the results. */
	private AnalysisResultsHandlingParam parameters;
	
	/** Button to close the dialog. */
	private JButton closeButton;
	
	/** Button to close the dialog. */
	private JButton saveButton;
	
	/** The main component displaying the results. */
	private JComponent body;
	
	/** The object hosting the chart. */
	private ChartObject chartObject;
	
	/** The chooser. */
	private FileChooser chooser;
	
	/**
	 * Returns the legend to link to the display.
	 * 
	 * @return See above.
	 */
	private String getLegend()
	{
		return file.getName();
	}
	
	/** 
	 * Initializes the components composing the display.
	 * 
	 *  @param name The name to display in the legend.
	 */
	private void initComponents(String name)
	{
		closeButton = new JButton("Close");
		closeButton.setActionCommand(""+CLOSE);
		closeButton.addActionListener(this);
		saveButton = new JButton("Save");
		saveButton.setActionCommand(""+SAVE);
		saveButton.addActionListener(this);
		List<Double> results = parseFile();
		if (results == null || results.size() == 0) {
			 JLabel l = new JLabel();
			 l.setText("Cannot display the results");
			 body = l;
		} else {
			if (name == null || name.trim().length() == 0)
				name = getLegend();
			switch (parameters.getIndex()) {
				case AnalysisResultsHandlingParam.HISTOGRAM:
					HistogramPlot hp = new HistogramPlot();
					hp.setXAxisName(parameters.getNameXaxis());
					hp.setYAxisName(parameters.getNameYaxis());
					double[] values = new double[results.size()];
					Iterator<Double> i = results.iterator();
					int index = 0;
					while (i.hasNext()) {
						values[index] = i.next().doubleValue();
						index++;
					}
					hp.addSeries(name, values, DEFAULT_COLOR, BINS);
					body = hp.getChart();
					chartObject = hp;
					break;
	
				default:
					break;
			}
		}
		saveButton.setEnabled(chartObject != null);
	}
	
	/** Closes the dialog. */
	private void close()
	{
		setVisible(false);
		dispose();
		file.delete();
	}
	
	/** Saves the display. */
	private void save()
	{
		chooser = new FileChooser((JFrame) getOwner(), FileChooser.SAVE, 
				"Save Chart", "Saves the chart", FILTERS);
		chooser.setCurrentDirectory(UIUtilities.getDefaultFolder());
		chooser.addPropertyChangeListener(this);
		chooser.centerDialog();
	}
	
	/**
	 * Parses a CSV file.
	 * 
	 * @return See above.
	 */
	private List<Double> parseCSV()
	{
		List<Double> list = new ArrayList<Double>();
		try {
			BufferedReader reader  = new BufferedReader(new FileReader(file));
			String line = null;
			StringTokenizer st;
			while ((line = reader.readLine()) != null) {
				st = new StringTokenizer(line, ",");
				list.add(Double.parseDouble(st.nextToken()));
			}
			reader.close();
			return list;
		} catch (Exception e) {
			
		}
		
		return null;
	}
	
	/**
	 * Parses the file.
	 * 
	 * @return See above.
	 */
	private List<Double> parseFile()
	{
		CustomizedFileFilter filter = new CSVFilter();
		if (filter.accept(file))
			return parseCSV();
		return null;
	}
	
	/**
	 * Builds and lays out the controls.
	 * 
	 * @return See above.
	 */
	private JComponent buildControlsBar()
	{
		JPanel p = new JPanel();
		p.add(saveButton);
		p.add(Box.createHorizontalStrut(10));
		p.add(closeButton);
		return UIUtilities.buildComponentPanelRight(p);
	}
	
	/** Builds and lays out the UI. 
	 * 
	 * @param icon The icon to display.
	 */
	private void buildGUI(Icon icon)
	{
		TitlePanel tp = new TitlePanel("Results", 
				"Follow a view of the results.", icon);
		
		Container container = getContentPane();
		container.setLayout(new BorderLayout());
		container.add(tp, BorderLayout.NORTH);
		if (body != null)
			container.add(body, BorderLayout.CENTER);
		container.add(buildControlsBar(), BorderLayout.SOUTH);
		pack();
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param owner The owner the dialog.
	 * @param icon  The icon to display in the header.
	 * @param file  The file to handle.
	 * @param name  The name to display in the legend
	 * @param parameters The parameters describing.
	 */
	AnalysisResultsDialog(JFrame owner, Icon icon, File file, String name,
			AnalysisResultsHandlingParam parameters)
	{
		super(owner);
		if (file == null)
			throw new IllegalArgumentException("No results to display.");
		if (parameters == null)
			throw new IllegalArgumentException("No parameters set.");
		this.file = file;
		this.parameters = parameters;
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		initComponents(name);
		buildGUI(icon);
	}

	/**
	 * Reacts to close or save.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case CLOSE:
				close();
				break;
			case SAVE:
				save();
		}
	}

	/**
	 * Reacts to property fired by the file chooser.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (FileChooser.APPROVE_SELECTION_PROPERTY.equals(name)) {
			File f = (File) evt.getNewValue();
			if (f == null) return;
			FileFilter filter = chooser.getSelectedFilter();
			int type = ChartObject.SAVE_AS_PNG;
			String extension = PNGFilter.PNG;
			if (filter instanceof JPEGFilter) {
				type = ChartObject.SAVE_AS_JPEG;
				extension = JPEGFilter.JPEG;
			} 
			Iterator<FileFilter> i = FILTERS.iterator();
			boolean accept = false;
			while (i.hasNext()) {
				filter = i.next();
				if (filter.accept(f)) {
					accept = true;
					break;
				}
			}
			if (!accept)
				f = new File(f.getAbsolutePath()+"."+extension);
			boolean b = true;
			try {
				chartObject.saveAs(f, type);
			} catch (Exception e) {
				b = false;
			}
			firePropertyChange(SAVED_CHART_PROPERTY, Boolean.valueOf(!b), 
					Boolean.valueOf(b));
		}
		
	}
}
