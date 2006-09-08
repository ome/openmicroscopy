/*
 * org.openmicroscopy.shoola.agents.roi.results.stats.graphic.SelectionPaneMng
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

package org.openmicroscopy.shoola.agents.roi.results.stats.graphic;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JButton;

//Java imports

//Third-party libraries

//Application-internal dependencies
//import org.openmicroscopy.shoola.util.ui.ColorChooser;
import org.openmicroscopy.shoola.util.ui.ColoredButton;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.table.TableComponent;
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
class SelectionPaneMng
    implements ActionListener
{
 
    static final int            SELECT_ALL = 0, DESELECT_ALL = 1;
    
    private SelectionPane       view;
    
    private HashMap             coloredButtons;
    
    SelectionPaneMng(SelectionPane view)
    {
        this.view = view;
        coloredButtons = new HashMap();
    }

    void attachButtonListener(JButton button, int id)
    {
        button.addActionListener(this);
        button.setActionCommand(""+id);
    }

    /** Handle event fired by coloredButton. */
    public void actionPerformed(ActionEvent e)
    {
        Object component = e.getSource();
        int index = -1;
        try {
            index = Integer.parseInt(e.getActionCommand());
            if (component instanceof ColoredButton) {
                ColoredButton b = (ColoredButton) component;
                coloredButtons.put(new Integer(index), b);
                //UIUtilities.centerAndShow(new ColorChooser(view, 
                   //                 b.getBackground(), index)); 
            } else {
                switch (index) {
                    case SELECT_ALL:
                        selectAll(true); break;
                    case DESELECT_ALL:
                        selectAll(false);
                }
            }
        } catch(NumberFormatException nfe) {
            throw new Error("Invalid Action ID "+index, nfe);
        }    
    }
    
    /** Set the background color of the pressed button. */
    void setColor(int i, Color c)
    {
        ColoredButton b = (ColoredButton) coloredButtons.get(new Integer(i));
        b.setBackground(c);
    }
    
    private void selectAll(boolean b)
    {
        TableComponent table = view.table;
        Boolean obj = Boolean.FALSE;
        if (b) obj = Boolean.TRUE;
        for (int i = 0; i < table.getRowCount(); i++)
            table.setValueAt(obj, i, ContextDialog.BOOLEAN);
    }
    
}

