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

    private DataFlavor flavor = new DataFlavor(int[].class, "Integer Array");
    
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
        return new DataHandler(table.getSelectedRows(), flavor.getMimeType());
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport info) {
        boolean b = info.getComponent() == table && info.isDrop()
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
        ROINode target = (ROINode) ((ROITable) table).getNodeAtRow(index);
        if (target == null)
            return true;

        if (!target.canEdit() || !target.isFolderNode())
            return false;

        int[] selection = table.getSelectedRows();
        for (int i = 0; i < selection.length; i++) {
            ROINode n = (ROINode) ((ROITable) table).getNodeAtRow(selection[i]);
            if (n != null && n.isAncestorOf(target))
                return false;
        }

        return true;
    }
    
    /**
     * Check if the current selection is draggable
     */
    private boolean checkDragSource() {
        if (table.getSelectedColumn() > 0)
            return false;
        int[] selection = table.getSelectedRows();
        for (int i = 0; i < selection.length; i++) {
            ROINode n = (ROINode) ((ROITable) table).getNodeAtRow(selection[i]);
            if (n == null || !n.canEdit())
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
            int[] rowsToMove = (int[]) info.getTransferable().getTransferData(
                   flavor);
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
}
