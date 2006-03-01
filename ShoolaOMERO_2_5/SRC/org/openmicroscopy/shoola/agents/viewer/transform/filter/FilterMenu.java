/*
 * org.openmicroscopy.shoola.agents.viewer.transform.filter.FilterMenu
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

package org.openmicroscopy.shoola.agents.viewer.transform.filter;


//Java imports
import javax.swing.JMenu;
import javax.swing.JMenuItem;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.transform.ImageInspectorManager;

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
public class FilterMenu
    extends JMenu
{

    private FilterMenuMng manager;
    
    public FilterMenu(ImageInspectorManager mng)
    {
        setText("Effects");
        manager = new FilterMenuMng(mng);
        buildGUI();
    }
    
    /** Build and lay out the GUI. */
    private void buildGUI()
    {
        JMenuItem item = new JMenuItem("Sharpen");
        manager.attachItemListener(item, FilterMenuMng.SHARPEN);
        add(item);
        item = new JMenuItem("Low pass");
        manager.attachItemListener(item, FilterMenuMng.LOW_PASS);
        add(item);
        item = new JMenuItem("Reset");
        manager.attachItemListener(item, FilterMenuMng.RESET);
        add(item);
    }
    
}
