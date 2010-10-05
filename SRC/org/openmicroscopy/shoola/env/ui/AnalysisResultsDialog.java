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
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
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
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.AnalysisResultsHandlingParam;
import org.openmicroscopy.shoola.util.filter.file.CSVFilter;
import org.openmicroscopy.shoola.util.filter.file.CustomizedFileFilter;
import org.openmicroscopy.shoola.util.filter.file.JPEGFilter;
import org.openmicroscopy.shoola.util.filter.file.PNGFilter;
import org.openmicroscopy.shoola.util.ui.NumericalTextField;
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
	
	/** Action ID indicating to plot the result again. */
	private static final int PLOT = 2;
	
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
	
	/** Button to save the graphics as <code>PNG</code> or <code>JPEG</code>. */
	private JButton saveButton;
	
	/** Button to plot the result */
	private JButton plotButton;
	
	/** The threshold for the maximum value. */
	private NumericalTextField maxThreshold;
	
	/** The threshold for the minimum value. */
	private NumericalTextField minThreshold;
	
	/** The main component displaying the results. */
	private JComponent body;
	
	/** The object hosting the chart. */
	private ChartObject chartObject;
	
	/** The chooser. */
	private FileChooser chooser;
	
	/** The values extracted from the file. */
	private List<Double> parseValues;
	
	/** The name of the graph. */
	private String 		name;
	
	/** The table displaying the values plotted. */
	private Double[][] data;
	
	/**
	 * Creates the table displaying the plotted values.
	 * 
	 * @param values The values to display.
	 */
	private void createTable(Map<Double, Double> values)
	{
		data = new Double[values.size()+1][2];
		Iterator v = values.entrySet().iterator();
		Entry entry;
		int index = 0;
		double totalY = 0;
		Double[] numbers;
		while (v.hasNext()) {
			entry = (Entry) v.next();
			numbers = new Double[2];
			numbers[0] = (Double) entry.getKey();
			numbers[1] = (Double) entry.getValue();
			data[index] = numbers;
			totalY += ((Double) entry.getValue()).doubleValue();
			index++;
		}
		numbers = new Double[2];
		numbers[0] = null;
		numbers[1] = totalY;
		data[index] = numbers;
	}
	
	/** 
	 * Creates the chart. 
	 * 
	 * @param values The values to plot.
	 * @param bins The number of bins.
	 */
	private void createChart(double[] values, int bins)
	{
		switch (parameters.getIndex()) {
			case AnalysisResultsHandlingParam.HISTOGRAM:
				HistogramPlot hp = new HistogramPlot();
				hp.setXAxisName(parameters.getNameXaxis());
				hp.setYAxisName(parameters.getNameYaxis());
				
				if (values == null) {
					double min = Double.MAX_VALUE;
					double max = Double.MIN_VALUE;
					double v;
					
					values = new double[parseValues.size()];
					Iterator<Double> i = parseValues.iterator();
					int index = 0;
					while (i.hasNext()) {
						v = i.next();
						
						if (v < min) min = v;
						if (v > max) max = v;
						values[index] = v;
						index++;
					}
				}
				if (values.length > 0)
					hp.addSeries(name, values, DEFAULT_COLOR, BINS);
				
				createTable(hp.getYValues(0));
				body = hp.getChart();
				chartObject = hp;
				break;
				default:
					break;
			}
	}
	
	/** 
	 * Lays out the graph and the table if any.
	 * 
	 * @param container The container hosting the various components. 
	 */
	private void layoutBody(Container container)
	{
		if (body == null) return;
		if (data != null) {
			String[] columns = {parameters.getNameXaxis(), 
					parameters.getNameYaxis()};
			JSplitPane pane = new JSplitPane();
			pane.setLeftComponent(body);
			pane.setRightComponent(new JScrollPane(new JTable(data, columns)));
			container.add(pane, BorderLayout.CENTER);
		} else {
			container.add(body, BorderLayout.CENTER);
		}
	}
	
	/**
	 * Returns the legend to link to the display.
	 * 
	 * @return See above.
	 */
	private String getLegend() { return file.getName(); }
	
	/** 
	 * Initializes the components composing the display.
	 * 
	 *  @param name The name to display in the legend.
	 */
	private void initComponents(String name)
	{
		minThreshold = new NumericalTextField();
		minThreshold.setToolTipText("Plot only values greater than the " +
				"value entered.");
		minThreshold.setNumberType(Double.class);
		minThreshold.setColumns(3);
		maxThreshold = new NumericalTextField();
		maxThreshold.setColumns(3);
		maxThreshold.setNumberType(Double.class);
		maxThreshold.setToolTipText("Plot only values lower than the " +
		"value entered.");
		plotButton = new JButton("Plot");
		plotButton.setActionCommand(""+PLOT);
		plotButton.addActionListener(this);
		
		closeButton = new JButton("Close");
		closeButton.setActionCommand(""+CLOSE);
		closeButton.addActionListener(this);
		saveButton = new JButton("Save");
		saveButton.setActionCommand(""+SAVE);
		saveButton.addActionListener(this);
		parseValues = parseFile();
		if (parseValues == null || parseValues.size() == 0) {
			 JLabel l = new JLabel();
			 l.setText("Cannot display the results");
			 body = l;
		} else {
			if (name == null || name.trim().length() == 0)
				name = getLegend();
			this.name = name;
			createChart(null, BINS);
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
		int index = name.lastIndexOf(".");
		String value = name;
		if (index > 0) value = name.substring(0, index);
		chooser.setSelectedFile(value);
		chooser.addPropertyChangeListener(this);
		chooser.centerDialog();
	}
	
	/** Plots the results again. */
	private void plot()
	{
		if (parseValues == null || parseValues.size() == 0) return;
		Number nMin = minThreshold.getValueAsNumber();
		Number nMax = maxThreshold.getValueAsNumber();
		Iterator<Double> i = parseValues.iterator();
		double[] results;
		int index = 0;
		double min;
		double max;
		double v;
		List<Double> values;
		double binMin = Double.MAX_VALUE;
		double binMax = Double.MIN_VALUE;
		if (nMin == null && nMax == null) {
			results = new double[parseValues.size()];
			i = parseValues.iterator();
			while (i.hasNext()) {
				v = i.next();
				if (v < binMin) binMin = v;
				if (v > binMax) binMax = v;
				results[index] = v;
				index++;
			}
		} else if (nMin == null && nMax != null) {
			max = nMax.doubleValue();
			values = new ArrayList<Double>();
			while (i.hasNext()) {
				v = i.next();
				if (v < max) {
					if (v < binMin) binMin = v;
					if (v > binMax) binMax = v;
					values.add(v);
				}
			}
			results = new double[values.size()];
			i = values.iterator();
			while (i.hasNext()) {
				results[index] = i.next();
				index++;
			}	
		} else if (nMax == null && nMin != null) {
			min = nMin.doubleValue();
			values = new ArrayList<Double>();
			while (i.hasNext()) {
				v = i.next();
				if (v > min) {
					if (v < binMin) binMin = v;
					if (v > binMax) binMax = v;
					values.add(v);
				}
			}
			results = new double[values.size()];
			i = values.iterator();
			while (i.hasNext()) {
				results[index] = i.next();
				index++;
			}
		} else {
			min = nMin.doubleValue();
			max = nMax.doubleValue();
			values = new ArrayList<Double>();
			while (i.hasNext()) {
				v = i.next();
				if (v > min && v < max) {
					if (v < binMin) binMin = v;
					if (v > binMax) binMax = v;
					values.add(v);
				}
			}
			results = new double[values.size()];
			i = values.iterator();
			while (i.hasNext()) {
				results[index] = i.next();
				index++;
			}
		}
		int bins = (int) (binMax-binMin);
		//repaint
		createChart(results, bins);
		Container container = getContentPane();
		Component c = container.getComponent(0);
		container.removeAll();
		container.add(c, BorderLayout.NORTH);
		layoutBody(container);
		container.add(buildControlsBar(), BorderLayout.SOUTH);
		container.validate();
		container.repaint();
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
				while (st.hasMoreTokens()) {
					list.add(Double.parseDouble(st.nextToken()));
				}
			}
			reader.close();
			return list;
		} catch (Exception e) {
			//ignore
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
		JPanel content = new JPanel();
		content.setLayout(new FlowLayout());
		JLabel l = new JLabel();
		l.setText("Min Threshold:");
		l.setToolTipText(minThreshold.getToolTipText());
		content.add(l);
		content.add(minThreshold);
		l = new JLabel();
		l.setText("Max Threshold:");
		l.setToolTipText(maxThreshold.getToolTipText());
		content.add(l);
		content.add(maxThreshold);
		
		JPanel p = new JPanel();
		p.add(plotButton);
		p.add(Box.createHorizontalStrut(10));
		p.add(saveButton);
		p.add(Box.createHorizontalStrut(10));
		p.add(closeButton);
		JPanel bar = new JPanel();
		bar.setLayout(new BoxLayout(bar, BoxLayout.X_AXIS));
		bar.add(UIUtilities.buildComponentPanel(content));
		bar.add(Box.createHorizontalGlue());
		bar.add(UIUtilities.buildComponentPanelRight(p));
		return UIUtilities.buildComponentPanel(bar);
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
		layoutBody(container);
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
				break;
			case PLOT:
				plot();
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
