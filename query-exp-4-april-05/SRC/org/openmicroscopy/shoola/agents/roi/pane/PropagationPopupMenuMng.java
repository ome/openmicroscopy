/*
 * org.openmicroscopy.shoola.agents.roi.pane.PropagationPopupMenuMng
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

//Third-party libraries

//Application-internal dependencies

/** 
 * The UI manager of the {@link PropagationPopupMenu}.
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
class PropagationPopupMenuMng
    implements ActionListener
{
    
    /** This UI component's view. */
    private PropagationPopupMenu    view;
    
    private AssistantDialogMng      adm;
    
    PropagationPopupMenuMng(PropagationPopupMenu view, AssistantDialogMng adm)
    {
        this.view = view;
        this.adm = adm;
        attachListeners();
    }
    
    /** Attach listeners. */
    private void attachListeners()
    {
        view.copyOne.addActionListener(this);
        view.copyAll.addActionListener(this);
        view.cancel.addActionListener(this);
        view.erase.addActionListener(this);
    }

    /** 
     * Reacts to activation of the menu buttons.
     *
     * @param e   Represents an activation of a menu button, as a click.
     */
    public void actionPerformed(ActionEvent e)
    {
        Object src = e.getSource();
        if (src == view.erase) adm.removeSelected();
        else if (src == view.copyOne) adm.copy();
        else if (src == view.copyAll) adm.copySegments();
        else if (src == view.cancel) adm.cancel();
        view.setVisible(false);
    }
    
}
