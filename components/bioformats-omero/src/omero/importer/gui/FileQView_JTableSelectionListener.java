/*
 * omero.importer.engine.FileQView_JTableSelectionListener
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2007 Open Microscopy Environment
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
 */

package omero.importer.gui;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import omero.importer.thirdparty.ETable;


public class FileQView_JTableSelectionListener
implements ListSelectionListener {

    ETable view;

//  It is necessary to keep the table since it is not possible
//  to determine the table from the event's source
    FileQView_JTableSelectionListener(ETable view) 
    {
        this.view = view;
    }

    public void valueChanged(ListSelectionEvent e) {
        // If cell selection is enabled, both row and column change events are fired
        if (e.getSource() == view.getSelectionModel()
                && view.getRowSelectionAllowed()) 
        {
            dselectRows();
        } 
    }

    private void dselectRows()
    {
        // Column selection changed
        int rows = view.getRowCount();

        for (int i = 0; i < rows; i++ )
        {
            try
            {
                if (view.getValueAt(i, 2) != null)
                {
                    if (!(view.getValueAt(i, 2).equals("Added") ||
                            view.getValueAt(i, 2).equals("Pending")) 
                            && view.getSelectionModel().isSelectedIndex(i))
                    {
                        view.getSelectionModel().removeSelectionInterval(i, i);
                    }
                } 
            } catch (ArrayIndexOutOfBoundsException e)
            {
                e.printStackTrace();
            }
        }
    }
}
