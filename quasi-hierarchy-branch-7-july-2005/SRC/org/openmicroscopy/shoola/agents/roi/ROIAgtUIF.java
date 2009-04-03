/*
 * org.openmicroscopy.shoola.agents.roi.ROIAgtUIF
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

package org.openmicroscopy.shoola.agents.roi;


//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.pane.PaintingControls;
import org.openmicroscopy.shoola.agents.roi.pane.AnalysisControls;
import org.openmicroscopy.shoola.agents.roi.pane.ToolBar;
import org.openmicroscopy.shoola.env.ui.TopWindow;

/** 
 * The UI facade of the {@link ROIAgt} agent.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *                  a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class ROIAgtUIF
    extends TopWindow
{

    public static final int             MAX_SCROLLPANE_HEIGHT = 400;
    
    public static final int             SCROLLPANE_HEADER = 30;
    
    /** Drawing command ID. */
    public static final int             NOT_ACTIVE_STATE = 0, MOVING = 1,
                                        CONSTRUCTING = 2, RESIZING = 3;
    
    public static final Color           STEELBLUE = new Color(0x4682B4);
   
    /** Background color. */
    public static final Color           BACKGROUND_COLOR = 
                                        new Color(204, 204, 255);
    
    public static final int             START = 25;
    
    public static final Dimension       BOX = new Dimension(10, 16), 
                                        HBOX = new Dimension(5, 0),
                                        VBOX = new Dimension(0, 5),
                                        DIM_SCROLL = new Dimension(40, 60);

    public static final int             ROW_TABLE_HEIGHT = 40, 
                                        ROW_NAME_FIELD = 25,
                                        EDITOR_WIDTH = 300,
                                        EDITOR_HEIGHT = 250,
                                        COLUMN_WIDTH = 200;

    public static final int             STATS = 0;
    public static final int             TEST = 1;
    public static final int             MAX = 1;
    
    public static final String[]        listAlgorithms;
    
    static {
        listAlgorithms = new String[MAX+1];
        listAlgorithms[STATS] = "Stats";
        listAlgorithms[TEST] = "TEST";
    }
    
    /** Reference to the painting panel. */
    private PaintingControls            paintingControls;
    
    /** Reference to the analysisControls panel. */
    private AnalysisControls            analysisControls;
    
    private ToolBar                     toolBar;
    
    ROIAgtUIF(ROIAgtCtrl control, String imageName, int maxT, int maxZ)
    {
        super("ROI "+imageName);
        String[] data = {}; //TODO RETRIEVE FROM abstraction
        toolBar = new ToolBar(control, maxT, maxZ, data);
        paintingControls = new PaintingControls(control);
        analysisControls = new AnalysisControls(control, data);
        buildGUI();
        pack();
    }
    
    ToolBar getToolBar() { return toolBar; }
    
    PaintingControls getPaintingControls() { return paintingControls; }
    
    AnalysisControls getAnalysisControls() { return analysisControls; }
    
    /** Build and lay out the GUI. */
    private void buildGUI()
    {
        getContentPane().setLayout(new BorderLayout(0, 0));
        getContentPane().add(toolBar, BorderLayout.NORTH); 
        getContentPane().add(paintingControls, BorderLayout.CENTER); 
        getContentPane().add(analysisControls, BorderLayout.SOUTH); 
    }

}
