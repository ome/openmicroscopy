/*
 * org.openmicroscopy.shoola.agents.roi.editor.ROIEditorMng
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

package org.openmicroscopy.shoola.agents.roi.editor;


//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.ROIAgtCtrl;

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
class ROIEditorMng
    implements ActionListener
{
    
    /** Action commands ID. */
    private static final int SAVE = 0, CANCEL = 1;
    
    private ROIEditor       view;
    
    private ROIAgtCtrl      control;
    
    private int             index;
    
    ROIEditorMng(ROIEditor view, ROIAgtCtrl control, int index)
    {
        this.view = view;
        this.control = control;
        this.index = index;
        attachListeners();
    }
    
    /** Attach the listeners. */
    void attachListeners()
    {
        attachButtonsListener(view.save, SAVE);
        attachButtonsListener(view.cancel, CANCEL);
    }
    
    private void attachButtonsListener(JButton button, int id)
    {
        button.addActionListener(this);
        button.setActionCommand(""+id);
    }
    
    /** Handle events fired by buttons. */
    public void actionPerformed(ActionEvent e)
    {
        int index = -1;
        try {
            index = Integer.parseInt(e.getActionCommand());
            switch (index) { 
                case SAVE:
                    save(); break;
                case CANCEL:
                    cancel(); break;
            }
        } catch(NumberFormatException nfe) {
            throw new Error("Invalid Action ID "+index, nfe);
        } 
    }

    /** Save the annotation. */
    private void save()
    {
        int i = view.colors.getSelectedIndex();
        if (index == -1)
            control.setROI5DDescription(view.nameArea.getText(), 
                    view.annotationArea.getText(), view.getColorSelected(i));
        else
            control.saveROI5DDescription(view.nameArea.getText(), 
                    view.annotationArea.getText(), view.getColorSelected(i));
        cancel();
    }
    
    /** Close the window. */
    private void cancel()
    {
        view.setVisible(false);
        view.dispose();
    }

}
