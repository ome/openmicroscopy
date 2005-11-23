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
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
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
	extends JPanel
{

	/** Reference to the registry. */
	private Registry       registry;
	
	/** Load all tree. */
	JButton                load, filter;
	
    JComboBox              selections;
    
    static final String[]  listOfItems;
    
    static final int            IMAGES_IMPORTED = 0;
    static final int            IMAGES_USED = 1;
    static final int            IMAGES_GROUP = 2;
    static final int            IMAGES_SYSTEM = 3;
    private static final int    MAX_ID = 3;
    
    static {
        listOfItems = new String[MAX_ID+1];
        listOfItems[IMAGES_IMPORTED] = "All images I own";
        listOfItems[IMAGES_USED] = "All images in my datasets";
        listOfItems[IMAGES_GROUP] = "All images in my group";
        listOfItems[IMAGES_SYSTEM] = "All images";
    }
    
	ImagesPaneBar(Registry registry)
	{
		this.registry = registry;
		initComponents();
		buildGUI();
	}
	
	/** Initialize the components. */
	private void initComponents()
	{
		//buttons
		IconManager im = IconManager.getInstance(registry);
		load = new JButton(im.getIcon(IconManager.IMAGE));
		load.setToolTipText(
			UIUtilities.formatToolTipText("Retrieve images."));
        filter = new JButton(im.getIcon(IconManager.FILTER));
        filter.setToolTipText(
            UIUtilities.formatToolTipText("Filters..."));
        selections = new JComboBox(listOfItems);
	}	
	
	/** Build and lay out the GUI. */
	private void buildGUI()
	{
        setLayout(new FlowLayout(FlowLayout.LEFT));
        add(UIUtilities.buildComponentPanel(selections));
        add(buildBar());
	}
	
    /** Display the buttons in a JToolBar. */
    private JToolBar buildBar()
    {
        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        bar.putClientProperty("JToolBar.isRollover", new Boolean(true));
        //bar.add(filter);
        bar.add(load);
        return bar;
    }
    
}
