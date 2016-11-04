/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2016 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.util.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.statistics.HistogramDataset;


/**
 * Creates an histogram from a buffered image.
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.3
 */
public class HistogramPane extends JPanel
{

    /** The default color for the marker.*/
    private static final Color MARKER_COLOR = Color.BLACK;

    /** The size of the color array.*/
    private static int SIZE = 256;

    /** The marker indicating the input start.*/
    private ValueMarker markerStart;

    /** The marker indicating the input end.*/
    private ValueMarker markerEnd;

    /** The histogram.*/
    private HistogramDataset dataset;

    /** Initializes the component.*/
    private void initialize()
    {
        dataset = new HistogramDataset();
        markerStart = new ValueMarker(0);
        markerStart.setPaint(MARKER_COLOR);
        markerEnd = new ValueMarker(SIZE);
        markerEnd.setPaint(MARKER_COLOR);
    }

    /** Lays out the components.*/
    private void buildLayout()
    {
        setBackground(UIUtilities.BACKGROUND);
        
        JFreeChart jfreechart = ChartFactory.createHistogram("", null, null,
                dataset, PlotOrientation.VERTICAL, false, false, false);
        XYPlot xyplot = (XYPlot) jfreechart.getPlot();
        ValueAxis range = xyplot.getRangeAxis();
        range.setVisible(false);
        range = xyplot.getDomainAxis();
        range.setVisible(false);
        xyplot.addDomainMarker(markerStart);
        xyplot.addDomainMarker(markerEnd);
        xyplot.setForegroundAlpha(0.85F);
        xyplot.setBackgroundPaint(UIUtilities.BACKGROUND);
        XYBarRenderer xybarrenderer = (XYBarRenderer) xyplot.getRenderer();
        xybarrenderer.setBarPainter(new StandardXYBarPainter());
        xybarrenderer.setDrawBarOutline(false);
        ChartPanel jpanel = new ChartPanel(jfreechart);
        jpanel.setPreferredSize(new Dimension(300, 150));
        add(jpanel);
    }

    /** Creates a new instance.*/
    public HistogramPane()
    {
        initialize();
    }

    /**
     * Sets the image used to build the histogram.
     *
     * @param image The image to use.
     */
    public void setImage(BufferedImage image)
    {
        //create histogram
        removeAll();
        dataset = new HistogramDataset();
        if (image != null) {
            Raster raster = image.getRaster();
            final int w = image.getWidth();
            final int h = image.getHeight();
            double[] r = new double[w * h];
            r = raster.getSamples(0, 0, w, h, 0, r);
            dataset.addSeries("", r, SIZE);
        }
        buildLayout();
        validate();
    }

    /**
     * Sets the input window.
     *
     * @param start The input start.
     * @param end The input end.
     */
    public void setInputWindow(double start, double end)
    {
        markerEnd.setValue(end*SIZE);
        markerStart.setValue(start*SIZE);
    }

}
