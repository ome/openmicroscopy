/*
 * org.openmicroscopy.shoola.env.ui.FLIMResultsDialog 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.agents.util.flim;


//Java imports
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.jdesktop.swingx.VerticalLayout;

//Third-party libraries
import info.clearthought.layout.TableLayout;
import processing.core.PVector;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import org.openmicroscopy.shoola.util.file.ExcelWriter;
import org.openmicroscopy.shoola.util.filter.file.ExcelFilter;
import org.openmicroscopy.shoola.util.filter.file.PNGFilter;
import org.openmicroscopy.shoola.util.image.geom.Factory;
import org.openmicroscopy.shoola.util.image.io.WriterImage;
import org.openmicroscopy.shoola.util.processing.chart.Histogram;
import org.openmicroscopy.shoola.util.processing.chart.ImageData;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.filechooser.FileChooser;
import org.openmicroscopy.shoola.util.ui.graphutils.ChartObject;
import org.openmicroscopy.shoola.util.ui.slider.TwoKnobsSlider;
import pojos.FileAnnotationData;

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
public class FLIMResultsDialog 
	extends JDialog
	implements ActionListener, PropertyChangeListener, ItemListener, ChangeListener
{

	/** Bound property indicating that the chart as been saved. */
	public static final String SAVED_FLIM_RESULTS_PROPERTY = "savedFlimResults";
	
	/** Action ID indicating to close the dialog. */
	private static final int CLOSE = 0;
	
	/** Action ID indicating to save the result. */
	private static final int SAVE = 1;
	
	/** Action ID indicating that a new file is selected. */
	private static final int SELECTION = 2;

	/** The number of bins. */
	private static final int BINS = 500;
	
	/** The multiplication factor. */
	private static final int FACTOR = 1;//1000;
	
	/** The name of the X-axis. */
	private static final String NAME_X_AXIS = ChartObject.X_AXIS;
	
	/** The name of the Y-axis. */
	private static final String NAME_Y_AXIS = "Number of Pixels";
	
	/** The columns of the table. */
	private static final String[] COLUMNS = {NAME_X_AXIS, NAME_Y_AXIS};
	
	/** Filters used for the save options. */
	private static List<FileFilter> FILTERS;

	/** The annotation files to exclude. */
	private static List<String> NAMES_TO_EXCLUDE;
	
	static {
		FILTERS = new ArrayList<FileFilter>();
		FILTERS.add(new ExcelFilter());
		NAMES_TO_EXCLUDE = new ArrayList<String>();
		NAMES_TO_EXCLUDE.add("(?i)gb.*");
	}
	
	/** The default columns in the historgram table. */
	static List<String> HISTOGRAMSTATS;
	
	static{
		HISTOGRAMSTATS = new ArrayList<String>();
		HISTOGRAMSTATS.add("Color");
		HISTOGRAMSTATS.add("Bin Start");
		HISTOGRAMSTATS.add("Bin End");
		HISTOGRAMSTATS.add("Min");
		HISTOGRAMSTATS.add("Max");
		HISTOGRAMSTATS.add("Mean");
		HISTOGRAMSTATS.add("Stddev");
		HISTOGRAMSTATS.add("Freq");
		HISTOGRAMSTATS.add("Percent");
	}
	
	/** The default columns in the cursor results table. */
	static List<String> CURSORSTATS;
	
	static{
		CURSORSTATS = new ArrayList<String>();
		CURSORSTATS.add("mean");
		CURSORSTATS.add("median");
		CURSORSTATS.add("binNo");
		CURSORSTATS.add("meanBin");
		CURSORSTATS.add("minBin");
		CURSORSTATS.add("maxBin");
		CURSORSTATS.add("stddevBin");
		CURSORSTATS.add("frequencyBin");
		CURSORSTATS.add("percentBin");
	}
	
	static Map<String, Boolean> statsButtons;
	
	static {
		statsButtons = new HashMap<String, Boolean>();
		statsButtons.put(ResultsDialog.LOAD,true);
		statsButtons.put(ResultsDialog.SAVE,true);
		statsButtons.put(ResultsDialog.CLEAR,true);
		statsButtons.put(ResultsDialog.WIZARD,true);
	}
	
	static Map<String, Boolean> cursorButtons;
	
	static {
		cursorButtons = new HashMap<String, Boolean>();
		cursorButtons.put(ResultsDialog.CLEAR,true);
		cursorButtons.put(ResultsDialog.WIZARD,true);
	}
	
	/** Button to close the dialog. */
	private JButton closeButton;
	
	/** Button to save the graphics as <code>PNG</code> or <code>JPEG</code>. */
	private JButton saveButton;
	
	/** The object hosting the chart. */
	private HistogramCanvas chartObject;

	/** The chooser. */
	private FileChooser chooser;
	
	/** The values extracted from the file. */
	private List<List<Double>> parseValues;
	
	/** The table displaying the values plotted. */
	private Double[][] data;

	/** The stats table.  */
	private ResultsDialog statsTable;

	/** The cursorResults table.  */
	private ResultsDialog cursorResults;
	
	/** The component hosting the various JXTaskPane. */
	private JXTaskPaneContainer paneContainer;
		
	/** The results to display. */
	private Map<FileAnnotationData, File> results;
	
	/** The component displaying the results. */
	private JComboBox resultsBox;
	
	/** The starting value of the interval. */
	private double start;
	
	/** The ending value of the interval. */
	private double end;
	
	/** Component displaying the image. */
	private ImageCanvas canvas;

	/** Component hosting the settings. */
	private JPanel settings;
	
	/** Component hosting the values of the selected data. */
	private JPanel cursorResultsPanel;
	
	/** Component hosting the settings. */
	private JComponent mainPane;
	
	/** The name of the image. */
	private String	imageName;
	
	/** The graph of the photons for the individual pixels selected. */
	private XYChartCanvas photonChart;
	
	/** Used to sort the results. */
	private ViewerSorter sorter;

	/** The label displaying the maximum value of the slider. */
	private JLabel thresholdSliderMaxValue;

	/** The label displaying the minimum value of the slider. */
	private JLabel thresholdSliderMinValue;

	/** The value of the left hand knob of the colour map slider. */
	private JLabel colourMapMinValue;

	/** The value of the right hand knob of the colour map slider. */
	private JLabel colourMapMaxValue;

	/** Slider used to enter the red and blue components of the colourmap. */
	private TwoKnobsSlider colourMapSlider;
	
	/** Button to toggle between RGB->BGR values. */
	private JToggleButton RGBButton; 
	
	/** The slider to determine the thresholding on the data.*/
	private TwoKnobsSlider thresholdSlider;
	
	/** The sorted data.*/
	private List<Double> sortedData;
	
	/** The value of the left knob of the slider.*/
	private double sliderLeftValue;
	
	/** The value of the right knob of the slider.*/
	private double sliderRightValue;
	/**
	 * Saves the data to the specified file.
	 * 
	 * @param f The file to handle.
	 */
	private void saveAs(File f)
	{
		if (f == null) return;
		FileFilter filter = chooser.getSelectedFilter();
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
			f = new File(f.getAbsolutePath()+"."+ExcelFilter.EXCEL);
		boolean b = true;
		File tmpFile = null;
		try {
			String v = f.getAbsolutePath();
			int index = v.lastIndexOf(".");
			//tmpFile = new File(v.substring(0, index)+"."+PNGFilter.PNG);
			//chartObject.saveAs(tmpFile, ChartObject.SAVE_AS_PNG);
		} catch (Exception e) {
			b = false;
		}
		
		//save to excel
		boolean result = true;
		try {
			int col;
			int index = 0;
			ExcelWriter writer = new ExcelWriter(f.getAbsolutePath());
			writer.openFile();
			writer.createSheet("FLIM Results for ");
			int row = 0;
			BufferedImage image;
			int w, h;
			Point p = null;
			if (b) {
				image = Factory.createImage(tmpFile);
				if (image != null) {
					w = image.getWidth();
					h = image.getHeight();
					String name = (String) resultsBox.getSelectedItem();
					writer.addImageToWorkbook(name, image); 
					col = writer.getMaxColumn(0);
					index += (col+2);
					p = writer.writeImage(0, index, w, h, name);
				}
			}
			image = canvas.getImage();
			if (image != null) {
				w = image.getWidth();
				h = image.getHeight();
				if (imageName == null) imageName = "image";
				writer.addImageToWorkbook(imageName, image); 
				int y = 0;
				if (p == null) row = 0;
				if (p != null) y = p.y;
				writer.writeImage(y+2, index, w, h, imageName);
				String v = f.getAbsolutePath();
				WriterImage.saveImage(new File(v+"."+PNGFilter.PNG), 
						image, PNGFilter.PNG);
			}
			writer.close();
		} catch (Exception e) {
			result = false;
		}
		if (tmpFile != null) tmpFile.delete();
		firePropertyChange(SAVED_FLIM_RESULTS_PROPERTY, 
				Boolean.valueOf(!result), Boolean.valueOf(result));
	}
	
	/**
	 * Creates the table displaying the plotted values.
	 * 
	 * @param values The values to display.
	 */
	private void createTable(Map<Double, Double> values)
	{
		
		//reformat table.
		Map<Double, Double> newValues = new HashMap<Double, Double>();
		Iterator v = values.entrySet().iterator();
		double key;
		Double value;
		Entry entry;
		while (v.hasNext()) {
			entry = (Entry) v.next();
			key = UIUtilities.roundTwoDecimals((Double) entry.getKey());
			value = newValues.get(key);
			if (value != null) value += (Double) entry.getValue();
			else value = (Double) entry.getValue();
			newValues.put(key, value);
		}
		//sort the map.
		
		List l = sorter.sort(newValues.keySet());
		data = new Double[newValues.size()+1][2];
		v = l.iterator();
		int index = 0;
		double totalY = 0;
		Double[] numbers;
		Double number;
		while (v.hasNext()) {
			numbers = new Double[2];
			numbers[0] = (Double) v.next();
			number = newValues.get(numbers[0]);
			numbers[1] = number;
			data[index] = numbers;
			totalY += number.doubleValue();
			index++;
		}
		numbers = new Double[2];
		numbers[0] = null;
		numbers[1] = totalY;
		data[index] = numbers;
	}
	
	/** Creates the chart. */
	private void createChart()
	{
		if (parseValues == null || parseValues.size() == 0) return;
		int rows = parseValues.size();
		List<Double> l = parseValues.get(0);
		int columns = l.size();
		Iterator<List<Double>> i = parseValues.iterator();
		Iterator<Double> j;
		int index = 0;
		List<Double> values = new ArrayList<Double>();
        double v;
        int row = 0; 
        int column = 0;
        double totalValue = 0;
        Map<Double, Double> map = new HashMap<Double, Double>();
        double value;
        while (i.hasNext()) {
			l = i.next();
			j = l.iterator();
			while (j.hasNext()) {
				value = 0;
				index = index+1;
				v = j.next();
				//if (v > min && v < max) {
					values.add(v);
					totalValue += v;
				//}
				if (map.containsKey(v)) {
					value = map.get(v);
					
				} 
				value++;
				map.put(v, value);
			}
		}
        createTable(map);
		List list = sorter.sort(values);
		int n = list.size();
		double median = 0;
		if (n > 2) median = (Double) list.get(n/2+1);
		else if (n == 1 || n == 2) median = (Double) list.get(0);
		//TODO:fix for results table
		//meanTextField.setText(""+UIUtilities.roundTwoDecimals(totalValue/index));
		//medianTextField.setText(""+UIUtilities.roundTwoDecimals(median));
		
		ImageData data = new ImageData(values, columns, rows, 1);
		sortedData = (List<Double>) sorter.sort(values);
		chartObject = new HistogramCanvas(sortedData, 
				data, BINS, true, getBestGuess(sortedData));
		chartObject.addPropertyChangeListener(this);
		colourMapSlider.setValues(BINS, 0, BINS, 0, BINS/4,BINS/2+BINS/4);
		thresholdSlider.setValues(BINS, 0, BINS, 0, 0,  BINS);
		thresholdSlider.addPropertyChangeListener(this);
		sliderLeftValue = BINS/4;
		sliderRightValue = BINS/4+BINS/2;
		
		chartObject.setRGB(true, BINS/4, BINS/2+BINS/4);
		colourMapSlider.addPropertyChangeListener(this);
		thresholdSliderMinValue.setText(UIUtilities.formatToDecimal(calculateValue(0,true)));
		thresholdSliderMaxValue.setText(UIUtilities.formatToDecimal(calculateValue(BINS,false)));
	}
	
	/** 
	 * Lays out the graph and the table if any.
	 * 
	 * @param container The container hosting the various components. 
	 */
	private JComponent layoutBody()
	{
		if (mainPane != null)
			getContentPane().remove(mainPane);
		JPanel p = new JPanel();
		Dimension dd = chartObject.getPreferredSize();
		int h = dd.height;
		double[][] topsize = 
		{	{0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05},
			{0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05}};
		p.setLayout(new TableLayout(topsize));
		p.add(settings,"0,0,3,2");
		p.add(cursorResultsPanel,"6,0,19,2");
		p.add(chartObject, "0,3,19,12");
		JPanel sliderPanel = new JPanel();
		double[][] sliderPanelSize = {{0.2,0.6,0.2},{TableLayout.PREFERRED,TableLayout.PREFERRED}};
		sliderPanel.setLayout(new TableLayout(sliderPanelSize));
		JPanel spacer = new JPanel();
		spacer.setLayout(new BoxLayout(spacer, BoxLayout.Y_AXIS));
		spacer.add(Box.createVerticalStrut(5));
		spacer.add(thresholdSlider);
		sliderPanel.add(thresholdSliderMinValue,"0,0,0,1");
		sliderPanel.add(spacer,"1,0,1,1");
		sliderPanel.add(thresholdSliderMaxValue,"2,0,2,1");
		
		p.add(sliderPanel,"0,13,4,13");
		sliderPanel = new JPanel();
		sliderPanelSize = new double[][] {{0.1,0.7,0.1,0.1},{TableLayout.PREFERRED,TableLayout.PREFERRED}};
		spacer = new JPanel();
		spacer.setLayout(new BoxLayout(spacer, BoxLayout.Y_AXIS));
		spacer.add(Box.createVerticalStrut(5));
		spacer.add(colourMapSlider);
		sliderPanel.setLayout(new TableLayout(sliderPanelSize));
		sliderPanel.add(colourMapMinValue,"0,0,0,1");
		sliderPanel.add(spacer,"1,0,1,1");
		sliderPanel.add(colourMapMaxValue,"2,0,2,1");
		sliderPanel.add(RGBButton,"3,0,3,1");
		p.add(sliderPanel,"6,13,19,13");
		p.add(statsTable,"0,15,19,19");
		p.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		return p;
	}
	
	/**
	 * Returns <code>true</code> if the name has to be excluded,
	 * <code>false</code> otherwise.
	 * 
	 * @param name The name to handle.
	 * @return See above.
	 */
	private boolean excludeName(String name)
	{
		Iterator<String> i = NAMES_TO_EXCLUDE.iterator();
		while (i.hasNext()) {
			if (i.next().matches(name))
				return true;	
		}
		return false;
	}
	
	/** Initializes the components composing the display. */
	private void initComponents()
	{
		colourMapMinValue = new JLabel("-");
		colourMapMinValue.setBorder(BorderFactory.createLoweredBevelBorder());
		colourMapMaxValue = new JLabel("-");
		colourMapMaxValue.setBorder(BorderFactory.createLoweredBevelBorder());
		
		colourMapSlider = new TwoKnobsSlider();
		colourMapSlider.setPaintMinorTicks(false);
		colourMapSlider.setPaintTicks(false);
		colourMapSlider.setPaintEndLabels(false);
		colourMapSlider.setPaintCurrentValues(false);

		
		statsTable = new ResultsDialog(HISTOGRAMSTATS, ResultsDialog.VERTICAL, true, statsButtons);
		statsTable.setRowHighlightMod(3);
		cursorResults = new ResultsDialog(CURSORSTATS, ResultsDialog.VERTICAL, true, cursorButtons);
		cursorResults.setRowHighlightMod(3);
		canvas = new ImageCanvas();
		RGBButton = new JToggleButton("RGB");
		RGBButton.setSelected(true);
		RGBButton.addItemListener(this);
		thresholdSlider = new TwoKnobsSlider(0,BINS,0,BINS);
		thresholdSlider.setPaintTicks(false);
		thresholdSlider.setPaintMinorTicks(false);
		thresholdSlider.setPaintLabels(false);
		thresholdSlider.setPaintEndLabels(false);
		thresholdSlider.setPaintCurrentValues(false);
		thresholdSliderMinValue = new JLabel("-");
		thresholdSliderMinValue.setBorder(BorderFactory.createLoweredBevelBorder());
		thresholdSliderMaxValue = new JLabel("-");
		thresholdSliderMaxValue.setBorder(BorderFactory.createLoweredBevelBorder());
		
		Iterator<FileAnnotationData> iterator = results.keySet().iterator();
		List<String> names = new ArrayList<String>();
		File file = null;
		String selectedFileName=null;
		while(iterator.hasNext())
		{
			FileAnnotationData annotation = iterator.next();
			if(!excludeName(annotation.getFileName()))
			{
				names.add(annotation.getFileName());
				if(results.get(annotation)!=null && file==null)
				{
					selectedFileName = annotation.getFileName();
					file = results.get(annotation);
				}
			}
		}
		
		if (file == null)
			return;
		final List<String> sortedNames = (List<String>) sorter.sort(names);

		int selectedFileIndex = sortedNames.indexOf(selectedFileName);
		resultsBox = new JComboBox(sortedNames.toArray());
		resultsBox.setSelectedIndex(selectedFileIndex);
		resultsBox.setActionCommand(""+SELECTION);
		resultsBox.addActionListener(this);
		paneContainer = new JXTaskPaneContainer();
		VerticalLayout layout = (VerticalLayout) paneContainer.getLayout();
		layout.setGap(0);
		
		JXTaskPane p = UIUtilities.createTaskPane("Intervals Data", null);
		//p.add(new JScrollPane(tableIntervals));
		paneContainer.add(p);
		paneContainer.add(UIUtilities.createTaskPane("Graph Data", null));
		
		closeButton = new JButton("Close");
		closeButton.setActionCommand(""+CLOSE);
		closeButton.addActionListener(this);
		saveButton = new JButton("Save");
		saveButton.setActionCommand(""+SAVE);
		saveButton.addActionListener(this);
		
		//parse the file
		parseValues = parseFile(file);
		if (parseValues == null || parseValues.size() == 0) {
			 return;
		} else {
			createChart();//(extractValues(), BINS);
		}
		saveButton.setEnabled(chartObject != null);

		photonChart = new XYChartCanvas();
		List<PVector> data = new ArrayList<PVector>();
		for(int h = 0 ; h < 100 ; h++)
			data.add(new PVector(h,0));
		photonChart.setData(data);
	}
	
	/** Closes the dialog. */
	private void close()
	{
		setVisible(false);
		dispose();
	}
	
	/** Saves the display. */
	private void save()
	{
		chooser = new FileChooser(null, FileChooser.SAVE, 
				"Save Results", "Saves the results", FILTERS);
		chooser.setCurrentDirectory(UIUtilities.getDefaultFolder());
		String name =  (String) resultsBox.getSelectedItem();
		int index = name.lastIndexOf(".");
		String value = name;
		if (index > 0) value = name.substring(0, index);
		chooser.setSelectedFile(value);
		chooser.addPropertyChangeListener(this);
		chooser.centerDialog();
	}

	/** 
	 * Plots the results again. */
	private void plot()
	{
		if (parseValues == null || parseValues.size() == 0) return;
		createChart();
		mainPane = layoutBody();
		getContentPane().add(mainPane, BorderLayout.CENTER);
		getContentPane().validate();
		getContentPane().repaint();
	}
	
	/** Selects the file. */
	private void selectFile()
	{
		int index = resultsBox.getSelectedIndex();
		String nameSelected = (String)resultsBox.getSelectedItem();
		/*int n = 0;
		Entry entry;
		File f = null;
		Iterator i = results.entrySet().iterator();
		while (i.hasNext()) {
			entry = (Entry) i.next();
			if (n == index) {
				f = (File) entry.getValue();
				break;
			}
			n++;
		}
		*/
		
		Iterator<FileAnnotationData> iterator = results.keySet().iterator();
		FileAnnotationData found = null;
		while(iterator.hasNext())
		{
			FileAnnotationData fileAnnotation = iterator.next();
			if(fileAnnotation.getFileName().equals(nameSelected))
				{
				found = fileAnnotation;
				break;
				}
		}
		if (found == null) return;
		parseValues = parseFile(results.get(found));
		plot();
	}
	
	
	/**
	 * Parses a CSV file.
	 * 
	 * @param file The file to parse.
	 * @return See above.
	 */
	private List<List<Double>> parseCSV(File file)
	{
		List<List<Double>> list = new ArrayList<List<Double>>();
		try {
			BufferedReader reader  = new BufferedReader(new FileReader(file));
			String line = null;
			StringTokenizer st;
			double v;
			List<Double> row;
			//set start and end
			start = Double.MAX_VALUE;
			end = Double.MIN_VALUE;
			while ((line = reader.readLine()) != null) {
				row = new ArrayList<Double>();
				st = new StringTokenizer(line, ",");
				while (st.hasMoreTokens()) {
					v = Double.parseDouble(st.nextToken());
					if (v < start) start = v;
					if (v > end) end = v;
					v = UIUtilities.roundTwoDecimals(v*FACTOR);
				
					row.add(v);
				}
				list.add(row);
			}
			reader.close();
			return list;
		} catch (Exception e) {e.printStackTrace();}
		return null;
	}
	
	/**
	 * Parses the file.
	 * 
	 * @param file The file to parse.
	 * @return See above.
	 */
	private List<List<Double>> parseFile(File file)
	{
		return parseCSV(file);
	}
	
	/**
	 * Builds the components used to modify the 
	 * @return
	 */
	private JPanel buildSettingsComponent()
	{
		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		JPanel row = new JPanel();
		row.setLayout(new FlowLayout());
		
		JLabel l = new JLabel();
		row = new JPanel();
		row.setLayout(new FlowLayout());
		l = new JLabel();
		l.setText("Results:");
		row.add(l);
		row.add(resultsBox);
		content.add(UIUtilities.buildComponentPanel(row));
		return content;
	}
	
	/**
	 * Build the component displaying the cursor results.
	 * @return See above.
	 */
	private JPanel buildCursorResultsComponent()
	{
		JPanel content = new JPanel();
		content.setLayout(new BorderLayout());
		content.add(cursorResults, BorderLayout.CENTER);
		return content;
	}
	
	/**
	 * Builds and lays out the controls.
	 * 
	 * @return See above.
	 */
	private JComponent buildControlsBar()
	{
		JPanel p = new JPanel();
		//p.add(plotButton);
		//p.add(Box.createHorizontalStrut(10));
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
		String text = "Follow a view of the results";
		if (imageName != null && imageName.length() > 0)
			text += " for "+imageName;
		TitlePanel tp = new TitlePanel("Results", text, icon);
		settings = buildSettingsComponent();
		cursorResultsPanel = buildCursorResultsComponent();
		Container container = getContentPane();
		container.setLayout(new BorderLayout());
		container.add(tp, BorderLayout.NORTH);
		mainPane = layoutBody();
		container.add(mainPane, BorderLayout.CENTER);
		container.add(buildControlsBar(), BorderLayout.SOUTH);
		plot();
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param owner The owner the dialog.
	 * @param icon  The icon to display in the header.
	 * @param imageName The name of the image.
	 * @param values The results to display.
	 */
	public FLIMResultsDialog(JFrame owner, String imageName, Icon icon,
			Map<FileAnnotationData, File> values)
	{
		super(owner);
		if (values == null)
			throw new IllegalArgumentException("No parameters set.");
		this.imageName = imageName;
		sorter = new ViewerSorter();
		final List<FileAnnotationData> list = sorter.sort(values.keySet());
		results = new LinkedHashMap<FileAnnotationData, File>();
		final Iterator<FileAnnotationData> i = list.iterator();
		FileAnnotationData fa;
		while (i.hasNext()) {
			fa = (FileAnnotationData) i.next();
			results.put(fa, values.get(fa));
		}
		initComponents();
		buildGUI(icon);
		setSize(1100, 800);
	}
	
	/**
	 * Returns the number of pixels contained in the specified interval or 
	 * <code>null</code>.
	 * 
	 * @param lowerBound The lower bound of the interval.
	 * @param upperBound The upper bound of the interval.
	 * @return See above.
	 */
	Double getValueInInterval(Double lowerBound, Double upperBound)
	{
		if (data == null) return null;
		if (lowerBound == null && upperBound == null) return null;
		int n = data.length-1;
		Double value;
		double count = 0;
		double min, max;
		if (lowerBound != null && upperBound != null) {
			min = lowerBound.doubleValue();
			max = upperBound.doubleValue();
			for (int i = 0; i < n; i++) {
				 value = data[i][0];
				 if (value >= min && value < max) 
					 count += data[i][1];
			}
			return count;
		} else if (lowerBound != null && upperBound == null) {
			min = lowerBound.doubleValue();
			for (int i = 0; i < n; i++) {
				value = data[i][0];
				 if (value >= min) 
					 count += data[i][1];
			}
			return count;
		}
		return null;
	}
	
	/**
	 * Returns the total number of pixels plotted.
	 * 
	 * @return See above.
	 */
	Double getTotalValue() { return data[data.length-1][1]; }
	
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
			case SELECTION:
				selectFile();
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
			File[] files = (File[]) evt.getNewValue();
			File f = files[0];
			saveAs(f);
		} else if (TwoKnobsSlider.KNOB_RELEASED_PROPERTY.equals(name) && evt.getSource().equals(colourMapSlider)) {
			if(sliderLeftValue==colourMapSlider.getStartValue() &&sliderRightValue==colourMapSlider.getEndValue())
				return;
			else
			{
				sliderLeftValue=colourMapSlider.getStartValue();
				sliderRightValue=colourMapSlider.getEndValue();
				newRangeSelected((int)colourMapSlider.getStartValue(), (int)colourMapSlider.getEndValue());
			}
		}else if(TwoKnobsSlider.KNOB_RELEASED_PROPERTY.equals(name) && evt.getSource().equals(thresholdSlider))
		{

			double startValue = calculateValue(thresholdSlider.getStartValueAsInt(),true);
			double endValue = calculateValue(thresholdSlider.getEndValueAsInt(),false);
	    	chartObject.setThreshold(startValue, endValue);
	    	thresholdSliderMinValue.setText(UIUtilities.formatToDecimal(startValue));
	    	thresholdSliderMaxValue.setText(UIUtilities.formatToDecimal(endValue));
		}else if (HistogramCanvas.CHARTSELECTED_PROPERTY.equals(name)){
			
			displayMapResult((Integer)evt.getNewValue());
		}else if(name.equals(JToggleButton.MODEL_CHANGED_PROPERTY)){
			chartObject.setRGB(RGBButton.isSelected(), (int)colourMapSlider.getStartValue(), (int)colourMapSlider.getEndValue());		
		}
	}
	
	/**
	 * Update the table, now that a new range has been selected. 
	 * @param start See above.
	 * @param end See above.
	 */
	private void newRangeSelected(int start, int end)
	{
		chartObject.setRGB(RGBButton.isSelected(), start, end);
		
		Map<String, Double> binStats = chartObject.getBinStats(start);
		if(binStats==null)
			return;
	
		colourMapMinValue.setText(UIUtilities.formatToDecimal(binStats.get(Histogram.MEAN)));
		binStats = chartObject.getBinStats(end);
		if(binStats==null)
			return;
	
		colourMapMaxValue.setText(UIUtilities.formatToDecimal(binStats.get(Histogram.MEAN)));;
		Map<String, Double> redStats = chartObject.getRangeStats(0,start);
		Map<String, Double> greenStats = chartObject.getRangeStats(start, end);
		Map<String, Double> blueStats = chartObject.getRangeStats(end, BINS);
		Map<String, Object> rowData = new HashMap<String, Object>();
		
		rowData.put("Color","Red");
		rowData.put("Bin Start",0);
		rowData.put("Bin End", start-1);
		rowData.put("Min", UIUtilities.formatToDecimal(redStats.get(Histogram.MIN)));
		rowData.put("Max", UIUtilities.formatToDecimal(redStats.get(Histogram.MAX)));
		rowData.put("Mean", UIUtilities.formatToDecimal(redStats.get(Histogram.MEAN)));
		rowData.put("Stddev", UIUtilities.formatToDecimal(redStats.get(Histogram.STDDEV)));
		rowData.put("Freq", UIUtilities.formatToDecimal(redStats.get(Histogram.FREQ)));
		rowData.put("Percent", UIUtilities.formatToDecimal(redStats.get(Histogram.PERCENT)));
		statsTable.insertData(rowData);
		rowData =  new HashMap<String, Object>();
		
		rowData.put("Color","Green");
		rowData.put("Bin Start",start);
		rowData.put("Bin End", end-1);
		rowData.put("Min", UIUtilities.formatToDecimal(greenStats.get(Histogram.MIN)));
		rowData.put("Max", UIUtilities.formatToDecimal(greenStats.get(Histogram.MAX)));
		rowData.put("Mean", UIUtilities.formatToDecimal(greenStats.get(Histogram.MEAN)));
		rowData.put("Stddev", UIUtilities.formatToDecimal(greenStats.get(Histogram.STDDEV)));
		rowData.put("Freq", UIUtilities.formatToDecimal(greenStats.get(Histogram.FREQ)));
		rowData.put("Percent", UIUtilities.formatToDecimal(greenStats.get(Histogram.PERCENT)));
		statsTable.insertData(rowData);
		rowData =  new HashMap<String, Object>();
		
		rowData.put("Color","Blue");
		rowData.put("Bin Start",end);
		rowData.put("Bin End", BINS);
		rowData.put("Min", UIUtilities.formatToDecimal(blueStats.get(Histogram.MIN)));
		rowData.put("Max", UIUtilities.formatToDecimal(blueStats.get(Histogram.MAX)));
		rowData.put("Mean", UIUtilities.formatToDecimal(blueStats.get(Histogram.MEAN)));
		rowData.put("Stddev", UIUtilities.formatToDecimal(blueStats.get(Histogram.STDDEV)));
		rowData.put("Freq", UIUtilities.formatToDecimal(blueStats.get(Histogram.FREQ)));
		rowData.put("Percent", UIUtilities.formatToDecimal(blueStats.get(Histogram.PERCENT)));
		statsTable.insertData(rowData);
	}

	/**
	 * Display the stats on the point clicked in the chart.
	 * @param bin The bin picked.
	 */
	private void displayMapResult(int bin)
	{
		
		Map<String, Double> binStats = chartObject.getBinStats(bin);
		if(binStats==null)
			return;
		Map<String, Object> rowData = new HashMap<String, Object>();
		
		rowData.put("mean",UIUtilities.formatToDecimal(chartObject.getMean()));
		rowData.put("median",UIUtilities.formatToDecimal(chartObject.getMedian()));
		rowData.put("meanBin",UIUtilities.formatToDecimal(binStats.get(Histogram.MEAN)));
		rowData.put("maxBin",UIUtilities.formatToDecimal(binStats.get(Histogram.MAX)));
		rowData.put("binNo",UIUtilities.formatToDecimal(bin));
		rowData.put("minBin",UIUtilities.formatToDecimal(binStats.get(Histogram.MIN)));
		rowData.put("percentBin",UIUtilities.formatToDecimal(binStats.get(Histogram.PERCENT)));
		rowData.put("stddevBin",UIUtilities.formatToDecimal(binStats.get(Histogram.STDDEV)));
		rowData.put("frequencyBin",UIUtilities.formatToDecimal(binStats.get(Histogram.FREQ)));
		cursorResults.insertData(rowData);
		
	}

	/**
	 * Overridden from {@see ItemListener#itemStateChanged(ItemEvent)
	 */
	public void itemStateChanged(ItemEvent e) 
	{
		if(e.getItem()==RGBButton)
		{ 
			if(e.getStateChange() == ItemEvent.SELECTED)
		    {
				RGBButton.setText("RGB");
				chartObject.setRGB(RGBButton.isSelected(), (int)colourMapSlider.getStartValue(), (int)colourMapSlider.getEndValue());	
		    }
            else
            {
            	RGBButton.setText("BGR");
            	chartObject.setRGB(RGBButton.isSelected(), (int)colourMapSlider.getStartValue(), (int)colourMapSlider.getEndValue());	
            } 
		}
	}
	
	/**
	 * Get the best guess for thresholding of the data.
	 * @param sortedData The sorted data to threshold.
	 * @return See above.
	 */
	private double getBestGuess(List<Double> sortedData)
	{
		double thresholdValue;
		if(sortedData.size()==0)
			thresholdValue= 0;
		else if(sortedData.get(0).equals(sortedData.get(sortedData.size()-1)))
			thresholdValue= sortedData.get(0)-1;
		else if(sortedData.get(sortedData.size()-1)==null)
			thresholdValue= sortedData.get(0)-1;
		else
			thresholdValue= sortedData.get(0);
		return thresholdValue;
	}

	/**
	 * Calculate the value that the slider would be in the data, if it was selecting the bin.
	 * @param bin See above.
	 * @return See above.
	 */
	private double calculateValue(int bin, boolean lower)
	{
		double min = sortedData.get(0);
		double max = sortedData.get(sortedData.size()-1);
		double range = max-min;
		double binWidth = range/(double)BINS;
		if(lower)
			return min+bin*binWidth;
		else
			return min+(bin+1)*binWidth;
	}
	
	/**
	 * Overridden from {@link ChangeListener#stateChanged(ChangeEvent)}
	 */
	public void stateChanged(ChangeEvent e) 
	{
		if (e.getSource() instanceof JSlider) 
		{
		      JSlider thresholdSlider = (JSlider) e.getSource();
		      if (!thresholdSlider.getValueIsAdjusting()) 
		      {
		      }
		}
	}
}