/*
 * org.openmicroscopy.shoola.util.ui.graphutils.LinePlot 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.ui.graphutils;


//Java imports
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
//Third-party libraries
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;

//Application-internal dependencies

/** 
 * Displays a bars using <code>JfreeChart</code>.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class LinePlot
    extends ChartObject
{

    /** The dataset for the line plot. */
    private DefaultXYDataset dataset;

    /** The x, y data of the plot. */
    private List<double[][]> data;

    /** The renderer for that plot.*/
    private StandardXYItemRenderer renderer;

    /** Initializes. */
    private void initialize()
    {
        data = new ArrayList<double[][]>();
        dataset = new DefaultXYDataset();
        renderer = new StandardXYItemRenderer();
    }

    /** Creates a new instance. */
    public LinePlot()
    {
        super("");
        initialize();
    }

    /**
     * Creates a new instance.
     *
     * @param title The title of the graph.
     * @param newLegends The legends of each series.
     * @param newData The data for each series.
     * @param newColours The colors for each series.
     */
    public LinePlot(String title, List<String> newLegends,
            List<double[][]> newData, List<Color> newColours)
    {
        super(title);
        if (newLegends == null || newData == null || newColours == null ||
                newLegends.size() != newData.size() && 
                newLegends.size() != newColours.size())
            throw new IllegalArgumentException("Mismatch between argument " +
                    "length");
        initialize();
        for (int i = 0 ; i < newLegends.size(); i++)
            addSeries(newLegends.get(i), newData.get(i), newColours.get(i));
        setDefaultAxis();
    }

    /**
     * Creates a new instance.
     *
     * @param title The title of the graph.
     * @param newLegends The legends of each series.
     * @param newData The data for each series.
     * @param newColours The colors for each series.
     * @param minValue The min value of the axis.
     * @param maxValue The max value of the axis.
     */
    public LinePlot(String title, List<String> newLegends,
            List<double[][]> newData, List<Color> newColours,
            double minValue, double maxValue)
    {
        super(title);
        if (newLegends == null || newData == null || newColours == null ||
                newLegends.size() != newData.size() && 
                newLegends.size() != newColours.size())
            throw new IllegalArgumentException("Mismatch between argument " +
                    "length");
        initialize();
        for (int i = 0 ; i < newLegends.size(); i++)
            addSeries(newLegends.get(i), newData.get(i), newColours.get(i));
        setDefaultAxis();
        rangeAxis.setRange(minValue, maxValue);
    }

    /**
     * Adds a new Series to the plot.
     *
     * @param legend The name of the new series.
     * @param newData The data.
     * @param color The color of the series.
     * @return The total number of series in the plot, this also gives the id
     * of the just added series.
     */
    public int addSeries(String legend, double[][] newData, Color color)
    {
        legends.add(legend);
        data.add(newData);
        colours.add(color);
        dataset.addSeries(legend, newData);
        return dataset.getSeriesCount();
    }

    /**
     * Adds the text to display in the tooltip.
     *
     * @param locations The values to display.
     */
    public void addLocations(Map<Integer, List<String>> locations)
    {
        renderer.setBaseToolTipGenerator(new ToolTipGenerator(locations));
    }

    /** 
     * Creates the chart.
     * @see ChartObject#createChar()
     */
    void createChart()
    {
        for (int i = 0 ; i < colours.size(); i++)
            renderer.setSeriesPaint(i, colours.get(i));
        XYPlot plot = new XYPlot(dataset, domainAxis, rangeAxis, renderer);
        if (backgroundImage != null) {
            plot.setRangeGridlinesVisible(false);
            plot.setDomainGridlinesVisible(false);
            plot.setBackgroundImage(backgroundImage);
        }
        
        chart = new JFreeChart(title, plot);
    }

    /**
     * Inner class used to display of the point on the image.
     */
    class ToolTipGenerator implements XYToolTipGenerator {

        /** Values to display when mousing over a series.*/
        private Map<Integer, List<String>> locations;

        /**
         * Creates a new instance.
         *
         * @param locations The values to display in tooltip.
         */
        public ToolTipGenerator(Map<Integer, List<String>> locations)
        {
            this.locations = locations;
        }

        @Override
        public String generateToolTip(XYDataset dataset, int series, int c) {
            if (locations == null) return "";
            int x = (int) dataset.getXValue(series, c);
            List<String> values = locations.get(series);
            if (CollectionUtils.isEmpty(values)) return "";
            if (x >=0 && x < values.size()) return values.get(x);
            return "";
        }
    }
}