/*
 * org.openmicroscopy.shoola.agents.viewer.transform.filter.FilterMenuMng
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;

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
class FilterMenuMng
    implements ActionListener
{

    static final int                SHARPEN = 0;
    static final int                LOW_PASS = 1;
    static final int                RESET = 2;
    private ImageInspectorManager   control;
    
    FilterMenuMng(ImageInspectorManager control)
    {
       this.control = control; 
    }

    /** Attach listener to a menu Item. */
    void attachItemListener(AbstractButton item, int id)
    {
        item.setActionCommand(""+id);
        item.addActionListener(this);
    }
    
    /** Handle events fired by menuItem. */
    public void actionPerformed(ActionEvent e)
    {
        int index = Integer.parseInt(e.getActionCommand());
        try {
            switch (index) {
                case SHARPEN:
                    control.sharpenImage(); break;
                case LOW_PASS:
                    control.lowPassImage(); break;
                case RESET:
                    control.resetImage();
            }
        } catch(NumberFormatException nfe) { 
            throw new Error("Invalid Action ID "+index, nfe);
        }  
    }

}
