package omero.importer.gui;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

import omero.importer.thirdparty.ETable;


public class FileQView_JTableCellCenter
extends DefaultTableCellRenderer 
{
//  This method is called each time a column header
//  using this renderer needs to be rendered.

    ETable view;

    FileQView_JTableCellCenter(ETable view) {
        this.view = view;
    }

    private static final long serialVersionUID = 1L;
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);

        setFont(UIManager.getFont("TableCell.font"));
        setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
        // Set tool tip if desired
        setToolTipText((String)value);

        if (view.getValueAt(row, 2) != null)
        {
            if (view.getValueAt(row, 2).equals("done") || 
                    view.getValueAt(row, 2).equals("failed"))
            { this.setEnabled(false); } 
            else
            { this.setEnabled(true); }
        }

        // Since the renderer is a component, return itself
        return this;
    }
}