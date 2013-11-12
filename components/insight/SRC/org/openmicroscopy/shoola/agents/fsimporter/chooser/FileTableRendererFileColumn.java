/*
 * org.openmicroscopy.shoola.agents.fsimporter.chooser.FileTableRendererFileColumn
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
public class FileTableRendererFileColumn extends DefaultTableCellRenderer {

    /** Reference to the <code>Directory</code> icon. */
    private static final Icon DIRECTORY_ICON;

    /** Reference to the <code>File</code> icon. */
    private static final Icon FILE_ICON;

    static {
        final IconManager icons = IconManager.getInstance();
        DIRECTORY_ICON = icons.getIcon(IconManager.DIRECTORY);
        FILE_ICON = icons.getIcon(IconManager.IMAGE);
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
        final FileElement element = (FileElement) dtm.getValueAt(row, column);
        if (element.isDirectory()) setIcon(DIRECTORY_ICON);
        else setIcon(FILE_ICON);
        setText(element.toString());
        return this;
    }
}
