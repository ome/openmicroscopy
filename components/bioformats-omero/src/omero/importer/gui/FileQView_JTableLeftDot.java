package omero.importer.gui;

import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Insets;

import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

import omero.importer.thirdparty.ETable;


public class FileQView_JTableLeftDot
    extends DefaultTableCellRenderer
{
    private static final long serialVersionUID = 1L;
    private ETable view;
    
    FileQView_JTableLeftDot(ETable view)
    {
        this.view = view;
    }
    public Component getTableCellRendererComponent(
        JTable table, Object value, boolean isSelected,
        boolean hasFocus, int row, int column)
    {
        super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);

        int availableWidth = table.getColumnModel().getColumn(column).getWidth();
        availableWidth -= table.getIntercellSpacing().getWidth();
        Insets borderInsets = getBorder().getBorderInsets((Component)this);
        availableWidth -= (borderInsets.left + borderInsets.right);
        String cellText = getText();
        FontMetrics fm = getFontMetrics( getFont() );
        // Set tool tip if desired

        if (fm.stringWidth(cellText) > availableWidth)
        {
            String dots = "...";
            int textWidth = fm.stringWidth( dots );
            int nChars = cellText.length() - 1;
            for (; nChars > 0; nChars--)
            {
                textWidth += fm.charWidth(cellText.charAt(nChars));

                if (textWidth > availableWidth)
                {
                    break;
                }
            }

            setText( dots + cellText.substring(nChars + 1) );
        }

        setFont(UIManager.getFont("TableCell.font"));
        if (view.getValueAt(row, 2) != null)
        {
            if (view.getValueAt(row, 2).equals("done"))
            { this.setEnabled(false);} 
            else
            { this.setEnabled(true); }
        }
       
        return this;
    }
}
