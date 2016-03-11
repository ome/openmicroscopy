/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2016 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.measurement.util.roitable;

import java.awt.Cursor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragSource;

import javax.activation.DataHandler;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;

import omero.log.LogMessage;

import org.openmicroscopy.shoola.agents.measurement.view.ROITable;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;

/**
 * TransferHandler for reordering rows in a JTable
 * 
 * Based on a stackoverflow post by Aaron Davidson:
 * http://stackoverflow.com/questions
 * /638807/how-do-i-drag-and-drop-a-row-in-a-jtable
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class TableRowTransferHandler extends TransferHandler {

    /** Reference to the table */
    private JTable table = null;

    /**
     * Creates a new instance
     * 
     * @param table
     *            Reference to the table
     */
    public TableRowTransferHandler(JTable table) {
        this.table = table;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        // Just use String as transfer object to keep it simple
        return new DataHandler(intArrayToString(table.getSelectedRows()),
                DataFlavor.stringFlavor.getMimeType());
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport info) {
        boolean b = info.getComponent() == table && info.isDrop()
                && info.isDataFlavorSupported(DataFlavor.stringFlavor)
                && checkDropTarget(info);

        table.setCursor(b ? DragSource.DefaultMoveDrop
                : DragSource.DefaultMoveNoDrop);

        return b;
    }

    /**
     * Check if the drop target is valid
     */
    private boolean checkDropTarget(TransferHandler.TransferSupport info) {
        JTable.DropLocation dl = (JTable.DropLocation) info.getDropLocation();
        int index = dl.getRow();
        ROINode n = (ROINode) ((ROITable) table).getNodeAtRow(index);
        return n.canEdit() && n.isFolderNode();
    }

    /**
     * Check if the current selection is draggable
     */
    private boolean checkDragSource() {
        int[] selection = table.getSelectedRows();
        for (int i = 0; i < selection.length; i++) {
            ROINode n = (ROINode) ((ROITable) table).getNodeAtRow(selection[i]);
            if (!n.canEdit())
                return false;
        }
        return true;
    }

    @Override
    public int getSourceActions(JComponent c) {
        if (checkDragSource())
            return TransferHandler.MOVE;
        else
            return TransferHandler.NONE;
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport info) {
        if (!info.isDrop())
            return false;
        ROITable target = (ROITable) info.getComponent();
        JTable.DropLocation dl = (JTable.DropLocation) info.getDropLocation();
        int index = dl.getRow();
        int max = table.getModel().getRowCount();
        if (index < 0 || index > max)
            index = max;
        target.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        try {
            String value = (String) info.getTransferable().getTransferData(
                    DataFlavor.stringFlavor);
            int[] rowsToMove = stringToIntArray(value);
            target.handleDragAndDrop(rowsToMove, index);
            return true;
        } catch (Exception e) {
            LogMessage msg = new LogMessage("DnD action failed", e);
            MetadataViewerAgent.getRegistry().getLogger().warn(this, msg);
        }
        return false;
    }

    @Override
    protected void exportDone(JComponent c, Transferable t, int act) {
        if (act == TransferHandler.MOVE || act == TransferHandler.NONE) {
            table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    private String intArrayToString(int[] array) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            sb.append(array[i]);
            if (i < array.length - 1)
                sb.append(',');
        }
        return sb.toString();
    }

    private int[] stringToIntArray(String s) {
        String[] tmp = s.split(",");
        int[] result = new int[tmp.length];
        for (int i = 0; i < result.length; i++)
            result[i] = Integer.parseInt(tmp[i]);
        return result;
    }
}
