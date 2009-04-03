/*
 * org.openmicroscopy.shoola.agents.viewer.transform.ImageInspector
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

package org.openmicroscopy.shoola.agents.viewer.transform;

//Java imports
import java.awt.BorderLayout;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.ViewerCtrl;
import org.openmicroscopy.shoola.agents.viewer.canvas.ImageCanvas;
import org.openmicroscopy.shoola.env.config.Registry;

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
public class ImageInspector
    extends JDialog
{
    
    /** Default zoom level. */
    public static final double      MIN_ZOOM_LEVEL = 0.25 , 
                                    MAX_ZOOM_LEVEL = 3.0,
                                    ZOOM_DEFAULT = 1.0,
                                    ZOOM_INCREMENT = 0.25;
    
    /** Reference to the {@link ToolBar}. */
    ToolBar                         toolBar;
    
    /** Reference to the {@link MenuBar}. */
    MenuBar                         menuBar;

    private ImageInspectorManager   manager;
    
    JScrollPane                     scroll;
    
    public ImageInspector(ViewerCtrl control, ImageCanvas canvas, 
                        double magFactor, int w, int h)
    {
        super(control.getReferenceFrame(), "Image inspector");
        init(control, canvas, magFactor, w, h);
        setJMenuBar(menuBar);
        buildGUI();
        pack();
    }

    public ImageInspectorManager getManager() { return manager; }
    
    /** Initializes the components. */
    private void init(ViewerCtrl control, ImageCanvas canvas, double magFactor, 
                        int w, int h)
    {
        Registry reg = control.getRegistry();
        manager = new ImageInspectorManager(this, control, magFactor);
        BufferedImage img = control.getBufferedImage();
        manager.setImageDimension(img.getWidth(), img.getHeight());
        manager.setCanvas(canvas);
        menuBar = new MenuBar(manager, magFactor);
        toolBar = new ToolBar(reg, manager, magFactor, w, h);
    }
    
    /** Build and lay out the GUI. */
    private void buildGUI()
    {
        setResizable(false);
        getContentPane().add(buildMain(), BorderLayout.NORTH);
    }
    
    private JPanel buildMain()
    {
        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        p.add(toolBar);
        return p;
    }

}
