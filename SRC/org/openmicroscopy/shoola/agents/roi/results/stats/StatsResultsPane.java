/*
 * org.openmicroscopy.shoola.agents.roi.results.stats.StatsResultsPane
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
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.table.AbstractTableModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.ROIAgtUIF;
import org.openmicroscopy.shoola.agents.roi.results.ROIResultsMng;
import org.openmicroscopy.shoola.agents.roi.results.stats.graphic.GraphicCanvas;
import org.openmicroscopy.shoola.env.rnd.roi.ROIStats;

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
public class StatsResultsPane
    extends JPanel
{
    
    public static final int Z = 0;
    public static final int T = 1;
    public static final int MIN = 2;
    public static final int MAX = 3;
    public static final int MEAN = 4;
    public static final int STD = 5;
    public static final int AREA = 6;
    public static final int SUM = 7;
    public static final int MAX_ID = 7;
    
    /** Will remove the first two columns. */
    public static final int MINUS = 2;

    public static final String[]   zAndtFieldNames ;
    
    static {
        zAndtFieldNames = new String[MAX_ID+1];
        zAndtFieldNames[Z] = "Z";
        zAndtFieldNames[T] = "T";
        zAndtFieldNames[MIN] = "Min";
        zAndtFieldNames[MAX] = "Max";
        zAndtFieldNames[MEAN] = "Mean";
        zAndtFieldNames[STD] = "Std";
        zAndtFieldNames[AREA] = "Area";
        zAndtFieldNames[SUM] = "Sum";
    }
    
    /** Table to display the results of the ROI. */
    StatsTable                      table;
    
    /** Controls tool bar. */
    BottomBar                       bar;
    
    JScrollPane                     scrollPane;
    
    int                             roiIndex;
    
    private StatsResultsPaneMng     manager;

    public StatsResultsPane(ROIResultsMng mng, ROIStats stats, int sizeT, 
                            int sizeZ, int channel, int l, Icon up, Icon down,
                            int roiIndex, int length)
    {
        this.roiIndex = roiIndex;
        manager = new StatsResultsPaneMng(this, mng, sizeT, sizeZ, stats, l);
        table = new StatsTable(up, down);
        table.initTable(manager.getDataToDisplay(channel));
        bar = new BottomBar(manager, mng.getRegistry(), length);
        buildGUI();
    }
    
    public StatsResultsPaneMng getManager() { return manager; }

    AbstractTableModel getTableModel()
    { 
        return (AbstractTableModel) table.getModel();
    }
    
    void addGraphic(JComponent c)
    {
        JViewport viewPort = scrollPane.getViewport();
        //viewPort.removeAll();
        viewPort.add(c);
    }
    
    void addTable()
    {
        JViewport viewPort = scrollPane.getViewport();
        viewPort.removeAll();
        viewPort.add(table);
    }
    
    /** Build and lay out the GUI. */
    private void buildGUI()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scrollPane = new JScrollPane(table);
        scrollPane.setBackground(GraphicCanvas.BACKGROUND);
        setScrollPaneSize();
        addComponents(scrollPane);
    }
    
    private void addComponents(JComponent c)
    {
        add(c, BorderLayout.CENTER);
        add(bar, BorderLayout.SOUTH);
    }
    
    /** Set the size of the scrollPane. */
    private void setScrollPaneSize()
    {
        Dimension d = table.getPreferredScrollableViewportSize();
        int h = table.height;
        if (h > ROIAgtUIF.MAX_SCROLLPANE_HEIGHT) 
            h = ROIAgtUIF.MAX_SCROLLPANE_HEIGHT;
        scrollPane.setPreferredSize(new Dimension(d.width, h));
    }
    
}
