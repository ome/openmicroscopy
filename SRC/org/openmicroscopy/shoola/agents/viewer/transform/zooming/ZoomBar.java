/*
 * org.openmicroscopy.shoola.agents.viewer.transform.zooming.ZoomBar
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

package org.openmicroscopy.shoola.agents.viewer.transform.zooming;

//Java imports
import java.awt.Color;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.IconManager;
import org.openmicroscopy.shoola.agents.viewer.transform.ImageInspectorManager;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.ui.UIFactory;

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
public class ZoomBar
	extends JToolBar
{
	
	private static final Color		STEELBLUE = new Color(0x4682B4);
																																					
	private JButton					zoomIn, zoomOut, zoomFit;
	
	/** Field displaying the zooming factor. */
	private JTextField				zoomField;
	
	private ZoomBarManager			manager;
	
	public ZoomBar(Registry registry, ImageInspectorManager mng)
	{
		initZoomComponents(registry);
		manager = new ZoomBarManager(this, mng);
		manager.attachListeners();
		buildToolBar();
	}
		
	JButton getZoomIn() { return zoomIn; }
	
	JButton getZoomOut() { return zoomOut; }
	
	JButton getZoomFit() { return zoomFit; }
	
	JTextField getZoomField() { return zoomField; }
	
	public ZoomBarManager getManager() { return manager; }
	
	/** Initialize the zoom components. */
	private void initZoomComponents(Registry registry)
	{
		zoomField = new JTextField("100%", "200%".length());
		zoomField.setForeground(STEELBLUE);
		zoomField.setToolTipText(
		UIFactory.formatToolTipText("zooming percentage."));
		//buttons
		IconManager im = IconManager.getInstance(registry);
		zoomIn =  new JButton(im.getIcon(IconManager.ZOOMIN));
		zoomIn.setToolTipText(
			UIFactory.formatToolTipText("Zoom in."));	
		zoomOut =  new JButton(im.getIcon(IconManager.ZOOMOUT));
		zoomOut.setToolTipText(
			UIFactory.formatToolTipText("Zoom out."));
		zoomFit =  new JButton(im.getIcon(IconManager.ZOOMFIT));
				zoomOut.setToolTipText(
					UIFactory.formatToolTipText("Reset."));
	}	
	
	/** Build the toolBar. */
	private void buildToolBar() 
	{
		setFloatable(false);
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		//add(zoomOut);
		add(buildTextPanel());
		//add(zoomIn);
		//add(zoomFit);
	}

	/** Panel containing textField. */
	private JPanel buildTextPanel()
	{
		JPanel p = new JPanel();
		p.add(zoomOut);
		p.add(zoomField);
		p.add(zoomIn);
		p.add(zoomFit);
		return p;
	}
	
}
