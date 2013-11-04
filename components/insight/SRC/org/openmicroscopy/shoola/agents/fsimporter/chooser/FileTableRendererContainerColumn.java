/*
 * org.openmicroscopy.shoola.agents.fsimporter.chooser.FileTableRendererContainerColumn
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.fsimporter.chooser;

//Java imports
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.fsimporter.IconManager;

/** 
 * Display the name and the icon. Adapted from previous FileTableRenderer.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class FileTableRendererContainerColumn extends DefaultTableCellRenderer {

    /** Reference to the <code>Screen</code> icon. */
    private static final Icon SCREEN_ICON;

    /** Reference to the <code>Dataset</code> icon. */
    private static final Icon DATASET_ICON;

    static {
        final IconManager icons = IconManager.getInstance();
        SCREEN_ICON = icons.getIcon(IconManager.SCREEN);
        DATASET_ICON = icons.getIcon(IconManager.DATASET);
    }

    /**
     * Overridden to set the correct renderer.
     * @see DefaultTableCellRenderer#getTableCellRendererComponent(JTable,
     * Object, boolean, boolean, int, int)
     */
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected,
                hasFocus, row, column);
        final DefaultTableModel dtm = (DefaultTableModel) table.getModel();
        final DataNodeElement n = (DataNodeElement) dtm.getValueAt(row, column);
        setText(n.toString());
        final Boolean isHCS = n.isHCSContainer();
        if (isHCS == null) setIcon(null);
        else setIcon(isHCS ? SCREEN_ICON : DATASET_ICON);
        return this;
    }
}
