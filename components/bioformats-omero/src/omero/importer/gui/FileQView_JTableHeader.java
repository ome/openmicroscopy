package omero.importer.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;


public class FileQView_JTableHeader
    extends DefaultTableCellRenderer 
{
    // This method is called each time a column header
    // using this renderer needs to be rendered.

    private static final long serialVersionUID = 1L;
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

       // setBorder(UIManager.getBorder("TableHeader.cellBorder"));
        setBorder(BorderFactory.createLineBorder(new Color(0xe0e0e0)));
        setForeground(UIManager.getColor("TableHeader.foreground"));
        setBackground(UIManager.getColor("TableHeader.background"));
        setFont(UIManager.getFont("TableHeader.font"));

        // Configure the component with the specified value
        setFont(getFont().deriveFont(Font.BOLD));
        setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
        setText(value.toString());
        setOpaque(true);
            
        // Set tool tip if desired
        setToolTipText((String)value);
        
        setEnabled(table == null || table.isEnabled());
                    
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        // Since the renderer is a component, return itself
        return this;
    }
    
    // The following methods override the defaults for performance reasons
    public void validate() {}
    public void revalidate() {}
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {}
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}
}
