/*
 * org.openmicroscopy.shoola.agents.roi.pane.ControlsManager
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

package org.openmicroscopy.shoola.agents.roi.pane;

//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JCheckBox;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.ROIAgtCtrl;
import org.openmicroscopy.shoola.agents.roi.ROIFactory;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *                  a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class PaintingControlsMng
    implements ActionListener
{
    
    /** Action command ID for the erase button. */
    static final int            ERASE = 0;
    
    /** Action command ID for the eraseALL button. */
    static final int            ERASE_ALL = 1;
    
    /** Action command ID for the rectangle button. */
    private static final int    RECTANGLE = 2;
    
    /** Action command ID for the ellipse button. */
    private static final int    ELLIPSE = 3;
    
    /** Action command ID for the checkBox. */
    private static final int    TEXT_ON_OFF = 4;
   
    private ROIAgtCtrl          control;
    
    private PaintingControls    view;
    
    PaintingControlsMng(PaintingControls view, ROIAgtCtrl control)
    {
        this.view = view;
        this.control = control;
    }

    /** Attach listener to a menu Item. */
    void attachItemListener(AbstractButton item, int id)
    {
        item.setActionCommand(""+id);
        item.addActionListener(this);
    }
    
    /** Attach listeners to buttons, comboBox and checkbox. */
    void attachListeners()
    {
        attachButtonListener(view.rectangle, RECTANGLE);
        attachButtonListener(view.ellipse, ELLIPSE);
        attachCheckBoxListener(view.textOnOff, TEXT_ON_OFF);
    }
    
    private void attachButtonListener(JButton button, int id)
    {
        button.setActionCommand(""+id);
        button.addActionListener(this);
    }
    
    private void attachCheckBoxListener(JCheckBox box, int id)
    {
        box.addActionListener(this);
        box.setActionCommand(""+id);
    }
    
    /** Handle events fired by buttons. */
    public void actionPerformed(ActionEvent e)
    {
        int index = Integer.parseInt(e.getActionCommand());
        try {
            switch (index) {
                case RECTANGLE:
                    setType(ROIFactory.RECTANGLE); break;
                case ELLIPSE:
                    setType(ROIFactory.ELLIPSE); break;
                case ERASE:
                    control.removePlaneArea(); break;
                case ERASE_ALL:
                    control.removeAllPlaneAreas(); break;
                case TEXT_ON_OFF:
                    handleOnOffText(e); break;   
            }
        } catch(NumberFormatException nfe) { 
            throw new Error("Invalid Action ID "+index, nfe); 
        }
    }
    
    private void handleOnOffText(ActionEvent e)
    {
        JCheckBox box = (JCheckBox) e.getSource();
        control.onOffText(box.isSelected());
    }
    
    private void setType(int type)
    {
        view.paintButton(type);
        control.setType(type);
    }

}
