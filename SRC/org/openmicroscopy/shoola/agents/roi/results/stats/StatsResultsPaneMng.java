/*
 * org.openmicroscopy.shoola.agents.roi.results.stats.StatsResultsPaneMng
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.roi.results.stats;


//Java imports
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.table.AbstractTableModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.ROIAgtUIF;
import org.openmicroscopy.shoola.agents.roi.results.ROIResultsMng;
import org.openmicroscopy.shoola.agents.roi.results.stats.graphic.GraphicCanvas;
import org.openmicroscopy.shoola.agents.roi.results.stats.saver.ROISaver;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.rnd.roi.ROIPlaneStats;
import org.openmicroscopy.shoola.env.rnd.roi.ROIStats;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.math.geom2D.PlanePoint;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class StatsResultsPaneMng
{
 
    public static final int         GRAPHIC_RESULT = 0, TABLE_RESULT = 1;
    
    public static final int         Z_AXIS = 0, T_AXIS = 1;
    
    public static final int         TABLE_INITIAL = 0, TABLE_RATIO = 1,
                                    TABLE_BACKGROUND = 2;
    
    private StatsResultsPane        view;
    
    private ROIResultsMng           mng;
    
    private ROIStats                stats;
    
    private GraphicCanvas           graphicCanvas;
    
    private int                     sizeT, sizeZ, lengthStats;
    
    private int                     selectedChannel, statsIndex, resultIndex,
                                    tableIndex;
    
    private List                    zSelected, tSelected;
    
    private Number[][][]            graphicData;
    
    public StatsResultsPaneMng(StatsResultsPane view, ROIResultsMng mng, 
                                int sizeT, int sizeZ, ROIStats stats, 
                                int numChannel)
    {
        this.view = view;
        this.sizeZ = sizeZ;
        this.sizeT = sizeT;
        this.mng = mng;
        this.stats = stats;
        tableIndex = TABLE_INITIAL;
        lengthStats = stats.getSize()/numChannel;
        graphicData = new Number[sizeT][sizeZ][StatsResultsPane.MAX_ID-1];
        zSelected = new ArrayList();
        tSelected =  new ArrayList();
        resultIndex = TABLE_RESULT;
    }

    public AbstractTableModel getTableModel() { return view.getTableModel(); }
    
    public Registry getRegistry() { return mng.getRegistry(); }
    
    public ROIAgtUIF getReferenceFrame() { return mng.getReferenceFrame(); }
    
    public BufferedImage getGraphicImage()
    { 
        return graphicCanvas.getGraphicImage();
    }
    
    public Rectangle getViewportBounds()
    {
        return view.scrollPane.getViewportBorderBounds();
    }
    
    public void showGraphic(int statsIndex, int axis, Map zSelected, 
                            Map tSelected)
    {
        this.statsIndex = statsIndex;
        resultIndex = GRAPHIC_RESULT;
        graphicCanvas = new GraphicCanvas(this, 
                        StatsResultsPane.zAndtFieldNames[statsIndex]);
        Object[] result;
        if (axis == T_AXIS) {
            result = getGraphicDataT(tSelected, zSelected);
            graphicCanvas.acrossT(zSelected, ((Integer) result[0]).intValue(), 
                    ((Integer) result[1]).intValue(), 
                    ((Double) result[2]).doubleValue(), 
                    ((Double) result[3]).doubleValue(), (Map) result[4]);
        } else if (axis == Z_AXIS) {
            result = getGraphicDataZ(zSelected, tSelected);
            graphicCanvas.acrossT(tSelected, ((Integer) result[0]).intValue(), 
                    ((Integer) result[1]).intValue(), 
                    ((Double) result[2]).doubleValue(), 
                    ((Double) result[3]).doubleValue(), (Map) result[4]);
        }
        view.bar.setEnabledButtons(false);
        view.addGraphic(graphicCanvas);
    }
    
    public Number[][] getDataToDisplay(int channelIndex)
    {
        return getData(channelIndex, true);
    }
    
    public void displayResult(int channelIndex)
    {
        setTableIndex(StatsResultsPaneMng.TABLE_INITIAL);
        view.table.setTableData(getData(channelIndex, true));
    }
    
    void setTableIndex(int index) { tableIndex = index; }
    
    void backToGraphic()
    {
        resultIndex = GRAPHIC_RESULT;
        view.bar.setEnabledButtons(false);
        if (graphicCanvas != null) view.addGraphic(graphicCanvas);
    }
    
    void backToTable()
    {
        resultIndex = TABLE_RESULT;
        view.bar.setEnabledButtons(true);
        view.addTable();
    }
    
    /** Save the result displayed i.e. table or graphic. */
    void saveResult() 
    {
        UIUtilities.centerAndShow(new ROISaver(this, resultIndex));
    }
    
    String[] getListROIs() { return mng.getListROIs(); }
    
    List getZSelected() { return zSelected; }
    
    List getTSelected() { return tSelected; }
    
    void displayInitialData()
    {
        setTableIndex(StatsResultsPaneMng.TABLE_INITIAL);
        view.table.setTableData(getData(selectedChannel, true));
    }
    
    void compute(int viewIndex)
    {
        Number[][] data = null;
        Number[][] initialData = getData(selectedChannel, false),
                   bData = mng.getDataForROI(viewIndex, selectedChannel);
        if (initialData.length != bData.length) {
            UserNotifier un = mng.getRegistry().getUserNotifier();
            un.notifyInfo("Computation", "The 2D-selection must be defined " +
                    "on the same 2D-planes.");
            return; 
        }
        switch (tableIndex) {
            case TABLE_RATIO:
                data = computeRatio(initialData, bData);
                break;
            case TABLE_BACKGROUND:
                data = computeBackground(initialData, bData);
                break;
            default:
                data = getData(selectedChannel, true);
        }
        view.table.setTableData(data);
    }
    
    private Number[][] computeRatio(Number[][] initialData, Number[][] data)
    {
        Number[][] resultData = 
            new Number[initialData.length][StatsResultsPane.MAX_ID+1];
        int z, t;
        for (int i = 0; i < initialData.length; i++) {
            resultData[i][StatsResultsPane.Z] = 
                initialData[i][StatsResultsPane.Z];
            resultData[i][StatsResultsPane.T] = 
                initialData[i][StatsResultsPane.T];
            resultData[i][StatsResultsPane.MIN] = 
                getRatioValue(initialData[i][StatsResultsPane.MIN], 
                                data[i][StatsResultsPane.MIN]);
            resultData[i][StatsResultsPane.MAX] = 
                getRatioValue(initialData[i][StatsResultsPane.MAX], 
                                data[i][StatsResultsPane.MAX]);
            resultData[i][StatsResultsPane.MEAN] = 
                getRatioValue(initialData[i][StatsResultsPane.MEAN], 
                                data[i][StatsResultsPane.MEAN]);
            resultData[i][StatsResultsPane.STD] = 
                getRatioValue(initialData[i][StatsResultsPane.STD], 
                                data[i][StatsResultsPane.STD]);
            resultData[i][StatsResultsPane.AREA] = 
                getRatioValue(initialData[i][StatsResultsPane.AREA], 
                                data[i][StatsResultsPane.AREA]);
            resultData[i][StatsResultsPane.SUM] = 
                getRatioValue(initialData[i][StatsResultsPane.SUM], 
                                data[i][StatsResultsPane.SUM]);
            //graphical values
            z = ((Integer) resultData[i][StatsResultsPane.Z]).intValue();
            t = ((Integer) resultData[i][StatsResultsPane.T]).intValue();
            graphicData[t][z][StatsResultsPane.MIN
                              -StatsResultsPane.MINUS] 
                              = resultData[i][StatsResultsPane.MIN];
            graphicData[t][z][StatsResultsPane.MAX
                              -StatsResultsPane.MINUS] 
                              = resultData[i][StatsResultsPane.MAX];
            graphicData[t][z][StatsResultsPane.MEAN
                              -StatsResultsPane.MINUS] 
                              = resultData[i][StatsResultsPane.MEAN];
            graphicData[t][z][StatsResultsPane.STD
                              -StatsResultsPane.MINUS] 
                              = resultData[i][StatsResultsPane.STD];
            graphicData[t][z][StatsResultsPane.AREA
                              -StatsResultsPane.MINUS] 
                              = resultData[i][StatsResultsPane.AREA];
            graphicData[t][z][StatsResultsPane.SUM
                              -StatsResultsPane.MINUS] 
                              = resultData[i][StatsResultsPane.SUM];
        }
        
        return resultData;
    }
    
    private Number[][] computeBackground(Number[][] initialData, 
                                        Number[][] data)
    {
        Number[][] resultData = 
            new Number[initialData.length][StatsResultsPane.MAX_ID+1];
        int z, t;
        for (int i = 0; i < initialData.length; i++) {
            resultData[i][StatsResultsPane.Z] = 
                initialData[i][StatsResultsPane.Z];
            resultData[i][StatsResultsPane.T] = 
                initialData[i][StatsResultsPane.T];
            resultData[i][StatsResultsPane.MIN] = 
                getBackgroundValue(initialData[i][StatsResultsPane.MIN], 
                                data[i][StatsResultsPane.MIN]);
            resultData[i][StatsResultsPane.MAX] = 
                getBackgroundValue(initialData[i][StatsResultsPane.MAX], 
                                data[i][StatsResultsPane.MAX]);
            resultData[i][StatsResultsPane.MEAN] = 
                getBackgroundValue(initialData[i][StatsResultsPane.MEAN], 
                                data[i][StatsResultsPane.MEAN]);
            resultData[i][StatsResultsPane.STD] = 
                getBackgroundValue(initialData[i][StatsResultsPane.STD], 
                                data[i][StatsResultsPane.STD]);
            resultData[i][StatsResultsPane.AREA] = 
                getBackgroundValue(initialData[i][StatsResultsPane.AREA], 
                                data[i][StatsResultsPane.AREA]);
            resultData[i][StatsResultsPane.SUM] = 
                getBackgroundValue(initialData[i][StatsResultsPane.SUM], 
                                data[i][StatsResultsPane.SUM]);
            //graphical values
            z = ((Integer) resultData[i][StatsResultsPane.Z]).intValue();
            t = ((Integer) resultData[i][StatsResultsPane.T]).intValue();
            graphicData[t][z][StatsResultsPane.MIN
                              -StatsResultsPane.MINUS] 
                              = resultData[i][StatsResultsPane.MIN];
            graphicData[t][z][StatsResultsPane.MAX
                              -StatsResultsPane.MINUS] 
                              = resultData[i][StatsResultsPane.MAX];
            graphicData[t][z][StatsResultsPane.MEAN
                              -StatsResultsPane.MINUS] 
                              = resultData[i][StatsResultsPane.MEAN];
            graphicData[t][z][StatsResultsPane.STD
                              -StatsResultsPane.MINUS] 
                              = resultData[i][StatsResultsPane.STD];
            graphicData[t][z][StatsResultsPane.AREA
                              -StatsResultsPane.MINUS] 
                              = resultData[i][StatsResultsPane.AREA];
            graphicData[t][z][StatsResultsPane.SUM
                              -StatsResultsPane.MINUS] 
                              = resultData[i][StatsResultsPane.SUM];
        }
        
        return resultData;
    }
    
    private Number[][] getData(int channel, boolean flag)
    {
        selectedChannel = channel;
        Number[][] data = 
           new Number[lengthStats][StatsResultsPane.MAX_ID+1];
        ROIPlaneStats p;
        int c = 0;
        for (int t = 0; t < sizeT; t++) {
            for (int z = 0; z < sizeZ; z++) {
                p = stats.getPlaneStats(z, channel, t);
                if (p != null) {
                    data[c][StatsResultsPane.Z] = new Integer(z);
                    data[c][StatsResultsPane.T] = new Integer(t);
                    data[c][StatsResultsPane.MIN] = new Double(p.getMin());
                    data[c][StatsResultsPane.MAX] = new Double(p.getMax());
                    data[c][StatsResultsPane.MEAN] = new Double(p.getMean());
                    data[c][StatsResultsPane.STD] = 
                        new Double(p.getStandardDeviation());
                    data[c][StatsResultsPane.AREA] = 
                        new Integer(p.getPointsCount());
                    data[c][StatsResultsPane.SUM] = new Double(p.getSum());
                    if (!zSelected.contains(data[c][StatsResultsPane.Z]))
                        zSelected.add(data[c][StatsResultsPane.Z]);
                    if (!tSelected.contains(data[c][StatsResultsPane.T]))
                        tSelected.add(data[c][StatsResultsPane.T]);
                    
                    if (flag) {
                        graphicData[t][z][StatsResultsPane.MIN
                                          -StatsResultsPane.MINUS] 
                                          = data[c][StatsResultsPane.MIN];
                        graphicData[t][z][StatsResultsPane.MAX
                                          -StatsResultsPane.MINUS] 
                                          = data[c][StatsResultsPane.MAX];
                        graphicData[t][z][StatsResultsPane.MEAN
                                          -StatsResultsPane.MINUS] 
                                          = data[c][StatsResultsPane.MEAN];
                        graphicData[t][z][StatsResultsPane.STD
                                          -StatsResultsPane.MINUS] 
                                          = data[c][StatsResultsPane.STD];
                        graphicData[t][z][StatsResultsPane.AREA
                                          -StatsResultsPane.MINUS] 
                                          = data[c][StatsResultsPane.AREA];
                        graphicData[t][z][StatsResultsPane.SUM
                                          -StatsResultsPane.MINUS] 
                                          = data[c][StatsResultsPane.SUM];
                    }
                    c++;
                }
            }
        }
        return data;
    }
    
    private Number getBackgroundValue(Number n, Number d)
    {
        Number v = null;
        if (n instanceof Integer)
            v = new Integer(n.intValue()-d.intValue());
        else if (n instanceof Double)
            v = new Double(n.doubleValue()-d.doubleValue());
        return v;
    }
    
    private Number getRatioValue(Number n, Number d)
    {
        double v = 0, denom = d.doubleValue();
        if (denom != 0) v = n.doubleValue()/denom;
        return new Double(v);
    }
    
    //TODO:Refator this code, not really clean but running out of time!
    /** Prepare data displayed in the table for the graphical form. */
    private Object[] getGraphicDataT(Map xAxis, Map yAxis)
    {
        Object[] result = new Object[5];
        Iterator i = (yAxis.keySet()).iterator();
        Iterator j;
        Integer key;
        PlanePoint point;//Coordinate
        ArrayList list; 
        TreeMap values = new TreeMap();
        double v, maxY = 0, minY = 0;
        int x, c, minX = 0, maxX = 0;
        c = 0; 
        while (i.hasNext()) {
            key = (Integer) i.next();
            j = (xAxis.keySet()).iterator();
            list = new ArrayList();
            while (j.hasNext()) {
                x = ((Integer) j.next()).intValue();
                v = getValue(key.intValue(), x);
                if (c == 0) {
                    minY = v;
                    maxY = v;
                    minX = x;
                    maxX = x;
                } else {
                    minY = Math.min(v, minY);
                    maxY = Math.max(v, maxY);
                    minX = Math.min(minX, x);
                    maxX = Math.max(maxX, x);
                }
                c++;
                point = new PlanePoint(x, v);
                list.add(point);
            }
            values.put(key, list);
        }
        result[0] = new Integer(minX);
        result[1] = new Integer(maxX);
        result[2] = new Double(minY);
        result[3] = new Double(maxY);
        result[4] = values;
        return result;
    }
    
    private Object[] getGraphicDataZ(Map xAxis, Map yAxis)
    {
        Object[] result = new Object[5];
        Iterator i = (yAxis.keySet()).iterator();
        Iterator j;
        Integer key;
        PlanePoint point;//Coordinate
        ArrayList list; 
        TreeMap values = new TreeMap();
        double v, maxY = 0, minY = 0;
        int x, c, minX = 0, maxX = 0;
        c = 0; 
        while (i.hasNext()) {
            key = (Integer) i.next();
            j = (xAxis.keySet()).iterator();
            list = new ArrayList();
            while (j.hasNext()) {
                x = ((Integer) j.next()).intValue();
                v = getValue(x, key.intValue());
                if (c == 0) {
                    minY = v;
                    maxY = v;
                    minX = x;
                    maxX = x;
                } else {
                    minY = Math.min(v, minY);
                    maxY = Math.max(v, maxY);
                    minX = Math.min(minX, x);
                    maxX = Math.max(maxX, x);
                }
                c++;
                point = new PlanePoint(x, v);
                list.add(point);
            }
            values.put(key, list);
        }
        result[0] = new Integer(minX);
        result[1] = new Integer(maxX);
        result[2] = new Double(minY);
        result[3] = new Double(maxY);
        result[4] = values;
        return result;
    }
    
    private double getValue(int z, int t)
    {
        Number value;
        Number[] n = graphicData[t][z];
        switch (statsIndex) {
            case StatsResultsPane.MIN:
                value = n[StatsResultsPane.MIN-StatsResultsPane.MINUS]; 
                break;
            case StatsResultsPane.MAX:
                value = n[StatsResultsPane.MAX-StatsResultsPane.MINUS]; 
                break;
            case StatsResultsPane.MEAN:
                value = n[StatsResultsPane.MEAN-StatsResultsPane.MINUS]; 
                break;
            case StatsResultsPane.STD:
                value = n[StatsResultsPane.STD-StatsResultsPane.MINUS]; 
                break;
            case StatsResultsPane.SUM:
                value = n[StatsResultsPane.SUM-StatsResultsPane.MINUS]; 
                break;
            case StatsResultsPane.AREA:
                value = n[StatsResultsPane.AREA-StatsResultsPane.MINUS]; 
                break;
            default:
                value = new Integer(0);
                
        }
        return value.doubleValue();
    }
    

}
