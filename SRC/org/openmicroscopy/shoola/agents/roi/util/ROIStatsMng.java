/*
 * org.openmicroscopy.shoola.agents.roi.pane.ROIStatsMng
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

package org.openmicroscopy.shoola.agents.roi.util;

//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.table.AbstractTableModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.ROIAgtCtrl;
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
class ROIStatsMng
    implements ActionListener
{
    /** Action ID to close the window. */
    private static final int    CLOSE = 0;
    
    /** Action ID to save the data. */
    private static final int    SAVE = 1;
    
    private ROIAgtCtrl          control;
    
    private ROIStats            view;
    
    ROIStatsMng(ROIStats view, ROIAgtCtrl control)
    {
        this.control = control;
        this.view = view;
    }

    /** Attach listeners. */
    void attachListeners()
    {
        JButton close = view.getClose(), save = view.getSave();
        close.setActionCommand(""+CLOSE);
        close.addActionListener(this);
        save.setActionCommand(""+SAVE);
        save.addActionListener(this);
        //add window listener.
        view.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) { handleClose(); }
        });
    }
    
    AbstractTableModel getModel() { return view.getModel(); }
    
    String[][] getROIStats()
    {
        return control.getROIStats();
    }

    /** Handle events fired by JButtons. */
    public void actionPerformed(ActionEvent e)
    {
        int index = Integer.parseInt(e.getActionCommand());
        try {
            switch (index) {
                case CLOSE:
                    handleClose(); break;
                case SAVE:
                    handleSave();
            }
        } catch(NumberFormatException nfe) { 
            throw new Error("Invalid Action ID "+index, nfe); 
        }   
    }
    
    /** Handle the save event. */
    private void handleSave()
    {
        UIUtilities.centerAndShow(new ROIStatsSaver(this, control));
    }
    
    /** Handle the close event. */
    private void handleClose()
    {
        view.dispose();
    }
    
}
