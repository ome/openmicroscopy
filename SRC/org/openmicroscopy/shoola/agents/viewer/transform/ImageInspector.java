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
import javax.swing.JDialog;
import javax.swing.JScrollPane;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.Viewer;
import org.openmicroscopy.shoola.agents.viewer.ViewerCtrl;
import org.openmicroscopy.shoola.agents.viewer.transform.zooming.ZoomPanel;

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
	
	/** Maximum width of the window. */
	private static final int		WIN_W = 500;
	
	/** Maximum height of the window. */
	private static final int		WIN_H = 500;
	
	ToolBar 						toolBar;
	MenuBar							menuBar;

	private ImageInspectorManager	manager;
	private ZoomPanel				zoomPanel;
	
	public ImageInspector(ViewerCtrl control)
	{
		super(control.getReferenceFrame(), "Image Inspector", true);
		manager = new ImageInspectorManager(this, control.getBufferedImage());
		init(control);
		setJMenuBar(menuBar);
		buildGUI();
		setSize(WIN_W, WIN_H);
	}
	
	/** Initializes the components. */
	private void init(ViewerCtrl control)
	{
		zoomPanel = new ZoomPanel(control.getBufferedImage());
		manager.setZoomPanel(zoomPanel);
		menuBar = new MenuBar(manager);
		toolBar = new ToolBar(control.getRegistry(), manager);
	}
	
	/** Build and lay out the GUI. */
	private void buildGUI()
	{
		JScrollPane scroll = new JScrollPane(zoomPanel);
		scroll.setBackground(Viewer.BACKGROUND_COLOR);
		getContentPane().add(toolBar, BorderLayout.NORTH);
		getContentPane().add(scroll, BorderLayout.CENTER);
	}
	
}
