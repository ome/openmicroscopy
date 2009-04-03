/*
 * org.openmicroscopy.shoola.agents.annotator.editors.AnnotateDatasetEditor
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

package org.openmicroscopy.shoola.agents.annotator.pane;


//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.annotator.Annotator;
import org.openmicroscopy.shoola.agents.annotator.AnnotatorCtrl;

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
class AnnotationBarMng
    implements ActionListener
{
    
    /** Action command ID. */
    private static final int    SAVE = 0, CANCEL = 1, DELETE = 2, 
                                SAVEWITHRS = 4;
    
    private AnnotationBar       view;
    
    private AnnotatorCtrl       control;
    
    public AnnotationBarMng(AnnotationBar view, AnnotatorCtrl control)
    {
        this.view = view;
        this.control = control;
        attachListeners();
    }

    /** Handle events fired by JButton. */
    public void actionPerformed(ActionEvent e)
    {
        try {
            int index = Integer.parseInt(e.getActionCommand());
            switch (index) {
                case SAVE:
                     control.save(Annotator.SAVE); break;
                case CANCEL:
                    control.close(); break;
                case DELETE:
                    control.delete(); break;
                case SAVEWITHRS:
                    control.save(Annotator.SAVEWITHRS); break;   
            }
        } catch(NumberFormatException nfe) {
            throw new Error("Invalid Action ID "+e.getActionCommand(), nfe);
        }
    }
    
    /** Attach listeners. */
    private void attachListeners()
    {
        attachButtonsListeners(view.save, SAVE);
        attachButtonsListeners(view.cancel, CANCEL);
        attachButtonsListeners(view.delete, DELETE);
        attachButtonsListeners(view.saveWithRS, SAVEWITHRS);
    }
    
    /** Attach listeners to a JButton. */
    private void attachButtonsListeners(JButton button, int id)
    {
        button.addActionListener(this);
        button.setActionCommand(""+id);
    }

}
