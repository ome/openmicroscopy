/*
 * org.openmicroscopy.shoola.agents.viewer.util.ProgressNotifier
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

package org.openmicroscopy.shoola.agents.viewer.util;


//Java imports
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.IconManager;
import org.openmicroscopy.shoola.agents.viewer.ViewerCtrl;
import org.openmicroscopy.shoola.util.ui.TitlePanel;

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
public class ProgressNotifier
	extends JDialog
{

	/** 
	 * The preferred size of the widget that displays the notification message.
	 * Only the part of text that fits into this display area will be displayed.
	 */
	private static final Dimension	BAR_SIZE = new Dimension(300, 20);
	
	/** Reference to the {@link ViewerCtrl controller}. */
	private ViewerCtrl		controller;
	
	public ProgressNotifier(ViewerCtrl controller, String imageName)
	{
		super(controller.getReferenceFrame(), "Loading image...");
		this.controller = controller;
		buildGUI(imageName);
		pack();
	}

	/** Build and lay out the GUI. */
	private void buildGUI(String imageName)
	{
		IconManager im = IconManager.getInstance(controller.getRegistry());
		getContentPane().setLayout(new BorderLayout(0, 0));
		String s = "Please wait while the image "+imageName+" is loaded " +
					"from the network...";
		TitlePanel tp = new TitlePanel(null, s, im.getIcon(IconManager.LOAD));			
		getContentPane().add(tp, BorderLayout.NORTH);
		getContentPane().add(buildProgressPanel(), BorderLayout.EAST);
	}
	
	/** Build panel with progress bar. */
	private JPanel buildProgressPanel()
	{
		JPanel p = new JPanel();
		JProgressBar bar = new JProgressBar();
		bar.setIndeterminate(true);
		bar.setPreferredSize(BAR_SIZE);
		bar.setSize(BAR_SIZE);
		p.add(bar);
		return p;
	}
	
}
