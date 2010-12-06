/*
 * org.openmicroscopy.shoola.env.ui.FLIMResultsDialog 
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
package org.openmicroscopy.shoola.agents.util.flim;


//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileFilter;


//Third-party libraries
import info.clearthought.layout.TableLayout;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.jdesktop.swingx.VerticalLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import org.openmicroscopy.shoola.util.file.ExcelWriter;
import org.openmicroscopy.shoola.util.filter.file.ExcelFilter;
import org.openmicroscopy.shoola.util.filter.file.PNGFilter;
import org.openmicroscopy.shoola.util.image.geom.Factory;
import org.openmicroscopy.shoola.util.ui.NumericalTextField;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.colour.GradientUtil;
import org.openmicroscopy.shoola.util.ui.filechooser.FileChooser;
import org.openmicroscopy.shoola.util.ui.graphutils.ChartObject;
import org.openmicroscopy.shoola.util.ui.graphutils.HistogramPlot;
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
	implements ActionListener, PropertyChangeListener
{

	/** Bound property indicating that the chart as been saved. */
	public static final String SAVED_FLIM_RESULTS_PROPERTY = "savedFlimResults";
	
	/** Action ID indicating to close the dialog. */
	private static final int CLOSE = 0;
	
	/** Action ID indicating to save the result. */
	private static final int SAVE = 1;
	
	/** Action ID indicating to plot the result again. */
	private static final int PLOT = 2;
	
	/** Action ID indicating that a new file is selected. */
	private static final int SELECTION = 3;
	
	/** The default color. */
	private static final Color DEFAULT_COLOR = Color.RED;
	
	/** The number of bins. */
	private static final int BINS = 1001;
	
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

	static {
		FILTERS = new ArrayList<FileFilter>();
		FILTERS.add(new ExcelFilter());
	}
	
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
	private List<List<Double>> parseValues;
	
	/** The table displaying the values plotted. */
	private Double[][] data;
	
	/** The component hosting the various JXTaskPane. */
	private JXTaskPaneContainer paneContainer;
	
	/** The table displaying the intervals. */
	private TableIntervals tableIntervals;

	/** The table displaying the plotted values. */
	private JTable tableValues;
	
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
	
	/** Component hosting the settings. */
	private JComponent mainPane;
	
	/** The name of the image. */
	private String	imageName;
	
	private JComponent intervalsPane;
	
	private JComponent graphicsPane;
	
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
			tmpFile = new File(v.substring(0, index)+"."+PNGFilter.PNG);
			chartObject.saveAs(tmpFile, ChartObject.SAVE_AS_PNG);
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
			if (tableIntervals != null) {
				row = tableIntervals.getModel().getRowCount()+1;
				writer.writeTableToSheet(0, 0, tableIntervals.getModel());
			}
			if (tableValues != null)
				writer.writeTableToSheet(row+3, 0, tableValues.getModel());
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
				writer.addImageToWorkbook(imageName, image); 
				if (p == null) row = 0;
				writer.writeImage(p.y+2, index, w, h, imageName);
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
		Entry entry;
		Map<Double, Double> newValues = new HashMap<Double, Double>();
		Iterator v = values.entrySet().iterator();
		double key;
		Double value;
		while (v.hasNext()) {
			entry = (Entry) v.next();
			key = UIUtilities.roundTwoDecimals((Double) entry.getKey());
			value = newValues.get(key);
			if (value != null) value += (Double) entry.getValue();
			else value = (Double) entry.getValue();
			newValues.put(key, value);
		}
		data = new Double[newValues.size()+1][2];
		v = newValues.entrySet().iterator();
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
		HistogramPlot hp = new HistogramPlot();
		hp.setXAxisName(NAME_X_AXIS);
		hp.setYAxisName(NAME_Y_AXIS);
		
		if (values == null) {
			double v;
			List<Double> list = new ArrayList<Double>();
			Iterator<List<Double>> i = parseValues.iterator();
			int index = 0;
			List<Double> l;
			Iterator<Double> j;
			while (i.hasNext()) {
				l = i.next();
				j = l.iterator();
				while (j.hasNext()) {
					v = j.next();
					if (v < start) start = v;
					if (v > end) end = v;
					list.add(v);
				}
			}
			values = new double[list.size()];
			j = list.iterator();
			while (j.hasNext()) {
				values[index] = j.next();
				index++;
			}
		}
		if (values.length > 0) {
			hp.addSeries((String) resultsBox.getSelectedItem(), 
					values, DEFAULT_COLOR, BINS);
		}
		createTable(hp.getYValues(0));
		body = hp.getChart();
		chartObject = hp;
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
		if (body == null) return new JPanel();
		if (data == null) return body;
		JPanel p = new JPanel();
		int h = canvas.getPreferredSize().height;
		double[][] size = {{TableLayout.PREFERRED, TableLayout.PREFERRED}, 
				{h, 200, 200}};
		p.setLayout(new TableLayout(size));
		p.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
		p.add(canvas, "0, 0, LEFT, TOP");
		p.add(settings, "0, 1, LEFT, TOP");
		p.add(body, "1, 0");
		p.add(graphicsPane, "1, 1");
		p.add(intervalsPane, "1, 2");
		return p;
	}
	
	/** Initializes the components composing the display. */
	private void initComponents()
	{
		canvas = new ImageCanvas();
		Entry entry;
		Iterator i = results.entrySet().iterator();
		FileAnnotationData fa;
		String[] names = new String[results.size()];
		int index = 0;
		File file = null;
		int selectedIndex = 0;
		while (i.hasNext()) {
			entry = (Entry) i.next();
			fa = (FileAnnotationData) entry.getKey();
			names[index] = fa.getFileName();
			if (file == null) {
				file = (File) entry.getValue();
				selectedIndex = index;
			}
			index++;
		}
		resultsBox = new JComboBox(names);
		resultsBox.setSelectedIndex(selectedIndex);
		resultsBox.setActionCommand(""+SELECTION);
		resultsBox.addActionListener(this);
		tableIntervals = new TableIntervals(this, NAME_Y_AXIS);
		paneContainer = new JXTaskPaneContainer();
		VerticalLayout layout = (VerticalLayout) paneContainer.getLayout();
		layout.setGap(0);
		
		JXTaskPane p = UIUtilities.createTaskPane("Intervals Data", null);
		//p.add(new JScrollPane(tableIntervals));
		intervalsPane = new JScrollPane(tableIntervals);
		paneContainer.add(p);
		paneContainer.add(UIUtilities.createTaskPane("Graph Data", null));
		
		minThreshold = new NumericalTextField();
		minThreshold.setToolTipText("Plot only values greater than the " +
				"value entered.");
		minThreshold.setNumberType(Double.class);
		minThreshold.setColumns(3);
		minThreshold.setText(""+0.0);
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
		
		//parse the file
		parseValues = parseFile(file);
		start = 0.0;//Double.MAX_VALUE;
		end = Double.MIN_VALUE;
		
		if (parseValues == null || parseValues.size() == 0) {
			 JLabel l = new JLabel();
			 l.setText("Cannot display the results");
			 body = l;
		} else {
			createChart(extractValues(), BINS);
		}
		createImage();
		tableIntervals.populateTable();
		tableValues = new JTable(data, COLUMNS);
		tableValues.getTableHeader().setReorderingAllowed(false);
		graphicsPane = new JScrollPane(tableValues);
		saveButton.setEnabled(chartObject != null);
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
	
	/** Creates the image representing the values read from the file.*/
	private void createImage()
	{
		if (parseValues == null || parseValues.size() == 0) return;
		int rows = parseValues.size();
		List<Double> l = parseValues.get(0);
		int columns = l.size();
		Iterator<List<Double>> i = parseValues.iterator();
		Iterator<Double> j;
		int index = 0;
		double[][] values = new double[columns][rows];
		double largest = Double.MIN_VALUE;
        double smallest = Double.MAX_VALUE;
        double value;
        int row = 0; 
        int column = 0;
        while (i.hasNext()) {
			l = i.next();
			j = l.iterator();
			column = 0;
			while (j.hasNext()) {
				value = convertValue(j.next().doubleValue());
				values[column][row] = value;
				largest = Math.max(value, largest);
				smallest = Math.min(value, smallest);
				//index++;
				column++;
			}
			row++;
		}
        Color[] colors = GradientUtil.GRADIENT_HOT;
        
        double range = largest-smallest;
        BufferedImage image = new BufferedImage(columns, rows, 
        		BufferedImage.TYPE_INT_RGB);
        Color c;
        int v;
        double norm;
        for (int x = 0; x < columns; x++) {
        	for (int y = 0; y < rows; y++) {
        		v = (int) values[x][y];
        		norm = (v-smallest)/range; // 0 < norm < 1
                c = colors[(int) Math.floor(norm*(colors.length-1))];
        		image.setRGB(x, y, Factory.makeRGB(c.getAlpha(), c.getRed(), 
        				c.getGreen(), c.getBlue()));
        		index++;
        	}
		}
        
        Dimension d = new Dimension(columns, rows);
		canvas.setPreferredSize(d);
		canvas.setSize(d);
		canvas.setImage(image);
	}
	
	/**
	 * Maps the value to [0, 255] for display.
	 * 
	 * @param value The value to convert.
	 * @return See above.
	 */
	private double convertValue(double value)
	{
		if (value < start) return 0;
		if (value > end) return 255;
		if (end == 0) return 0;
		return 255*(value-start)/(end-start);
	}

	/** 
	 * Extracts the values.
	 * 
	 * @return See above.
	 */
	private double[] extractValues()
	{
		Number nMin = minThreshold.getValueAsNumber();
		Number nMax = maxThreshold.getValueAsNumber();
		Iterator<List<Double>> i = parseValues.iterator();
		double[] results;
		int index = 0;
		double min;
		double max;
		double v;
		List<Double> l;
		Iterator<Double> j;
		List<Double> values;
		double binMin = Double.MAX_VALUE;
		double binMax = Double.MIN_VALUE;
		values = new ArrayList<Double>();
		if (nMin == null && nMax == null) {
			while (i.hasNext()) {
				l = i.next();
				j = l.iterator();
				while (j.hasNext()) {
					v = j.next();
					if (v < binMin) binMin = v;
					if (v > binMax) binMax = v;
					values.add(v);
				}
			}
		} else if (nMin == null && nMax != null) {
			max = nMax.doubleValue();
			while (i.hasNext()) {
				l = i.next();
				j = l.iterator();
				while (j.hasNext()) {
					v = j.next();
					if (v < max) {
						if (v < binMin) binMin = v;
						if (v > binMax) binMax = v;
						values.add(v);
					}
				}	
			}	
		} else if (nMax == null && nMin != null) {
			min = nMin.doubleValue();
			while (i.hasNext()) {
				l = i.next();
				j = l.iterator();
				while (j.hasNext()) {
					v = j.next();
					if (v > min) {
						if (v < binMin) binMin = v;
						if (v > binMax) binMax = v;
						values.add(v);
					}
				}
			}
		} else {
			min = nMin.doubleValue();
			max = nMax.doubleValue();
			while (i.hasNext()) {
				l = i.next();
				j = l.iterator();
				while (j.hasNext()) {
					v = j.next();
					if (v > min && v < max) {
						if (v < binMin) binMin = v;
						if (v > binMax) binMax = v;
						values.add(v);
					}
				}
			}
		}
		results = new double[values.size()];
		j = values.iterator();
		while (j.hasNext()) {
			results[index] = j.next();
			index++;
		}
		start = binMin;
		end = binMax;
		return results;
	}
	
	/** 
	 * Plots the results again. */
	private void plot()
	{
		if (parseValues == null || parseValues.size() == 0) return;
		double[] values = extractValues();
		int bins = (int) (end-start);
		//repaint
		createChart(values, bins);
		createImage();
		tableIntervals.populateTable();
		
		JXTaskPane pane = (JXTaskPane) paneContainer.getComponent(1);
		pane.removeAll();
		tableValues = new JTable(data, COLUMNS);
		tableValues.getTableHeader().setReorderingAllowed(false);
		//pane.add(new JScrollPane(tableValues));
		graphicsPane = new JScrollPane(tableValues);
		mainPane = layoutBody();
		getContentPane().add(mainPane, BorderLayout.CENTER);
		getContentPane().validate();
		getContentPane().repaint();
	}
	
	/** Selects the file. */
	private void selectFile()
	{
		int index = resultsBox.getSelectedIndex();
		int n = 0;
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
		if (f == null) return;
		parseValues = parseFile(f);
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
			while ((line = reader.readLine()) != null) {
				row = new ArrayList<Double>();
				st = new StringTokenizer(line, ",");
				while (st.hasMoreTokens()) {
					v = Double.parseDouble(st.nextToken());
					row.add(UIUtilities.roundTwoDecimals(v*FACTOR));
				}
				list.add(row);
			}
			reader.close();
			return list;
		} catch (Exception e) {}
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
		//CustomizedFileFilter filter = new CSVFilter();
		//if (filter.accept(file))
			return parseCSV(file);
		//return null;
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
		l.setText("Min Threshold:");
		l.setToolTipText(minThreshold.getToolTipText());
		row.add(l);
		row.add(minThreshold);
		content.add(UIUtilities.buildComponentPanel(row));
		row = new JPanel();
		row.setLayout(new FlowLayout());
		l = new JLabel();
		l.setText("Max Threshold:");
		l.setToolTipText(maxThreshold.getToolTipText());
		row.add(l);
		row.add(maxThreshold);
		content.add(UIUtilities.buildComponentPanel(row));
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
	 * Builds and lays out the controls.
	 * 
	 * @return See above.
	 */
	private JComponent buildControlsBar()
	{
		JPanel p = new JPanel();
		p.add(plotButton);
		p.add(Box.createHorizontalStrut(10));
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
		Container container = getContentPane();
		container.setLayout(new BorderLayout());
		container.add(tp, BorderLayout.NORTH);
		mainPane = layoutBody();
		container.add(mainPane, BorderLayout.CENTER);
		container.add(buildControlsBar(), BorderLayout.SOUTH);
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
		ViewerSorter sorter = new ViewerSorter();
		List list = sorter.sort(values.keySet());
		results = new LinkedHashMap<FileAnnotationData, File>();
		Iterator i = list.iterator();
		FileAnnotationData fa;
		while (i.hasNext()) {
			fa = (FileAnnotationData) i.next();
			results.put(fa, values.get(fa));
		}
		initComponents();
		buildGUI(icon);
		pack();
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
			case PLOT:
				plot();
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
		}
	}
	
}
