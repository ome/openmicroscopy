/*
 * org.openmicroscopy.shoola.agents.roi.results.controls.ControlsBarMng
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

package org.openmicroscopy.shoola.agents.roi.results.controls;

//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.results.ROIResultsMng;
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
class ControlsBarMng
    implements ActionListener
{
    
    /** Action ID to close the window. */
    private static final int    CLOSE = 0;

    private ControlsBar         view;
    
    private ROIResultsMng       mng;
    
    ControlsBarMng(ControlsBar view, ROIResultsMng mng)
    {
        this.view = view;
        this.mng = mng;
        attachListeners();
    }
    
    /** Attach listeners. */
    private void attachListeners()
    {
        attachButtonListener(view.cancel, CLOSE);
    }
 
    /** Attach a listener to the specified button. */
    private void attachButtonListener(JButton button, int id)
    {
        button.setActionCommand(""+id);
        button.addActionListener(this);
    }
    
    /** Handle events fired by JButtons and JComboBox. */
    public void actionPerformed(ActionEvent e)
    {
        int index = Integer.parseInt(e.getActionCommand());
        try {
            switch (index) {
                case CLOSE:
                    mng.handleClose(); break;
            }
        } catch(NumberFormatException nfe) { 
            throw new Error("Invalid Action ID "+index, nfe); 
        }   
    }

}
