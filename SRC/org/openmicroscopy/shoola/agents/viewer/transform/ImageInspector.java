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
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.IconManager;
import org.openmicroscopy.shoola.agents.viewer.Viewer;
import org.openmicroscopy.shoola.agents.viewer.ViewerCtrl;
import org.openmicroscopy.shoola.agents.viewer.transform.zooming.ZoomPanel;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.env.rnd.events.ImageRendered;

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
	extends JFrame implements AgentEventListener
{
		
	ToolBar 						toolBar;
	MenuBar							menuBar;

	private ImageInspectorManager	manager;
	private ZoomPanel				zoomPanel;
	
	JScrollPane 					scroll;
	public ImageInspector(ViewerCtrl control)
	{
		//super(control.getReferenceFrame(), "Image Inspector");
		initFrame(IconManager.getInstance(control.getRegistry()));
		init(control);
		setJMenuBar(menuBar);
		buildGUI();
	}
	
	/** Initializes the frame. */
	private void initFrame(IconManager im)
	{
		setTitle("Image Inspector");
		setIconImage(
		((ImageIcon) im.getIcon(IconManager.INSPECTOR)).getImage());
	}
	
	/** Initializes the components. */
	private void init(ViewerCtrl control)
	{
		//register for the ImageRendered event.
		Registry reg = control.getRegistry();
		reg.getEventBus().register(this, ImageRendered.class);
		manager = new ImageInspectorManager(this);
		BufferedImage img = control.getBufferedImage();
		manager.setBufferedImage(img);
		zoomPanel = new ZoomPanel(manager);
		manager.setZoomPanel(zoomPanel);
		menuBar = new MenuBar(manager);
		toolBar = new ToolBar(reg, manager);
		setWindowSize(img.getWidth(), img.getHeight());
	}
	
	/** Build and lay out the GUI. */
	private void buildGUI()
	{
		scroll = new JScrollPane(zoomPanel);
		scroll.setBackground(Viewer.BACKGROUND_COLOR);
		getContentPane().add(toolBar, BorderLayout.NORTH);
		getContentPane().add(scroll, BorderLayout.CENTER);
		
	}

	/** Implement as specified by {@link AgentEventListener}. */
	public void eventFired(AgentEvent e)
	{
		if (e instanceof ImageRendered)	handleImageRendered((ImageRendered) e);
	}
	
	/** Handle event @see ImageRendered. */
	private void handleImageRendered(ImageRendered response)
	{
		manager.setBufferedImage(response.getRenderedImage());
		manager.zoom();
	} 
	
	
	/** Set the size of the window w.r.t the size of the screen. */
	private void setWindowSize(int w, int h)
	{
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int width = 8*(screenSize.width/10);
		int height = 8*(screenSize.height/10);
		if (w > width) w = width;
		if (h > height) h = height;
		setTBSize(w);
		setSize(w, h);		
	}
	
	/** Add a rigid area to the toolBar. */
	private void setTBSize(int w)
	{
		Dimension d = toolBar.getSize();
		int dw = d.width;
		if (w-dw > 0)
			toolBar.add(Box.createRigidArea(new Dimension(w-d.width, 1)));		
	}
	
}
