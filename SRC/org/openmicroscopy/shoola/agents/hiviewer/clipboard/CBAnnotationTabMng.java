/*
 * org.openmicroscopy.shoola.agents.hiviewer.clipboard.CBAnnotationTabControl
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

package org.openmicroscopy.shoola.agents.hiviewer.clipboard;




//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;

//Third-party libraries

//Application-internal dependencies

/** 
 * The {@link CBAnnotationTabView}'s controller.
 *
 * @author  Barry Anderson &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:banderson@computing.dundee.ac.uk">
 *              banderson@comnputing.dundee.ac.uk
 *              </a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class CBAnnotationTabMng
    implements ActionListener
{

    /** Indicates to create a new annotation. */
    private static final int      CREATE = 0;
    
    /** Indicates to update the currently selected annotation. */
    private static final int      SAVE = 1;
    
    /** Indicates to update the currently selected annotation. */
    private static final int      DELETE = 2;
    
    /** The view this control is for. */
    private CBAnnotationTabView view;
    
    /**
     * Adds an {@link ActionListener} to the specified component.
     * 
     * @param button The component.
     * @param id The action command ID.
     */
    private void attachButtonListener(JButton button, int id)
    {
        button.addActionListener(this);
        button.setActionCommand(""+id); 
    }
    
    /** Adds listener to the UI components. */
    private void attachListeners()
    {
        attachButtonListener(view.createAnnotation, CREATE);
        attachButtonListener(view.saveAnnotation, SAVE);
        attachButtonListener(view.removeAnnotation, DELETE);
        view.annotatedByList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    view.allowUserUpdate();
                    view.showSingleAnnotation();
                 }
            }
        });
    }
    
    /**
     * Creates a new instance.
     * 
     * @param view The View this control is for. Mustn't be <code>null</code>.
     */
    CBAnnotationTabMng(CBAnnotationTabView view)
    {
        if (view == null) throw new NullPointerException("No view.");
        this.view = view;
        attachListeners();
    }

    /**
     * Handles actions.
     */
    public void actionPerformed(ActionEvent e)
    {
        int index = -1;
        try {
            index = Integer.parseInt(e.getActionCommand());
            switch (index) { 
                case CREATE:
                    view.focusOnOwnerAnnotation();
                    view.showSingleAnnotation();
                    view.allowCreation(true);
                    break;
                case SAVE:
                    if (view.isAnnotated()) view.update();
                    else view.create();     
                    break;
                case DELETE:
                    view.delete();
                    break;
            } 
        } catch(NumberFormatException nfe) {
            throw new Error("Invalid Action ID "+index, nfe);
        } 
    }
    
}
