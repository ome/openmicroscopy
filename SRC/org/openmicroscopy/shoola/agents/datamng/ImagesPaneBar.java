/*
 * org.openmicroscopy.shoola.agents.datamng.ImagePaneBar
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

package org.openmicroscopy.shoola.agents.datamng;


//Java imports
import javax.swing.JButton;
import javax.swing.JToolBar;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;
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
class ImagesPaneBar
	extends JToolBar
{

	private Registry	registry;
	
	/** Order by button. */
	private JButton		filter;
	
	/** Load all tree. */
	private JButton		load;
	
	ImagesPaneBar(Registry registry)
	{
		this.registry = registry;
		initZoomComponents();
		buildGUI();
	}

	public JButton getFilter() { return filter; }
	
	public JButton getLoad() { return load; }
	
	/** Initialize the components. */
	private void initZoomComponents()
	{
		//buttons
		IconManager im = IconManager.getInstance(registry);
		load = new JButton(im.getIcon(IconManager.IMAGE));
		load.setToolTipText(
			UIUtilities.formatToolTipText("Retrieve user's images."));
		filter = new JButton(im.getIcon(IconManager.FILTER));
		filter.setToolTipText(
			UIUtilities.formatToolTipText("Filter."));	
		filter.setEnabled(false);
	}	
	
	/** Build and lay out the GUI. */
	private void buildGUI()
	{
		setFloatable(false);
		putClientProperty("JToolBar.isRollover", new Boolean(true));
		add(load);
		add(filter);
	}
	
}
