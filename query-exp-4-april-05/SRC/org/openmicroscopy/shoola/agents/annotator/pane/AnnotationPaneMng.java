/*
 * org.openmicroscopy.shoola.agents.annotator.editors.AnnotateDatasetEditorMng
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

//Java imports;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;

//Third-party libraries

//Application-internal dependencies
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
public class AnnotationPaneMng
    implements ActionListener
{
    
    private static final int    OWNER = 0;
    
    private AnnotationPane      view;
    
    private AnnotatorCtrl       control;
    
    public AnnotationPaneMng(AnnotationPane view, AnnotatorCtrl control)
    {
        this.view = view;
        this.control = control;
        attachListeners();
    }
    
    /** Handle events fired by JCombobox. */
    public void actionPerformed(ActionEvent e)
    {
        String s = e.getActionCommand();
        try {
            int index = Integer.parseInt(s);
            switch (index) { 
                case OWNER:
                    JComboBox box = (JComboBox) e.getSource();
                    setOwner(box.getSelectedIndex());
            }
        } catch(NumberFormatException nfe) {  
            throw new Error("Invalid Action ID "+s, nfe);
        } 
    }
    
    /** Select a new person. */
    private void setOwner(int index)
    {
        //Need to synchronize the buttons in the toolBar
        boolean b = synchBar(index);
        view.setTableAnnotation(control.getOwnerAnnotation(index), b); 
    }
    
    /** Synchronize the buttonBar. */
    public boolean synchBar(int index)
    {
        int userIndex = control.getUserIndex();
        boolean b = false;
        if (userIndex == index) {
            b = true;
            if (view.table.creation) control.saveEnabled(true);
            else control.buttonsEnabled(true);
        } else control.buttonsEnabled(false);
        return b;
    }
    
    /** Attach listeners. */
    private void attachListeners()
    {
        attachComboBoxListeners(view.owners, OWNER);
    }
    
    /** Attach listeners to a {@link JComboBox}. */
    private void attachComboBoxListeners(JComboBox box, int id)
    {
        box.addActionListener(this);
        box.setActionCommand(""+id);
    }

}
