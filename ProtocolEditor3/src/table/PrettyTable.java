package table;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class PrettyTable extends JTable {
	
	public void paint(Graphics g) {
        super.paint(g);
        paintEmptyRows(g);
    }
	
	 /**
     * Paints the backgrounds of the implied empty rows when the
     * tableModel model is insufficient to fill all the visible area
     * available to us. We don't involve cell renderers, because
     * we have no data.
     */
    protected void paintEmptyRows(Graphics g) {
        final int rowCount = getRowCount();
        final Rectangle clip = g.getClipBounds();
        final int height = clip.y + clip.height;
        
            
            // Mac OS' Aqua LAF never draws vertical grid lines, so we have to draw them ourselves.
            if (isMacOs()) {
                g.setColor(MAC_UNFOCUSED_UNSELECTED_VERTICAL_LINE_COLOR);
                TableColumnModel columnModel = getColumnModel();
                int x = 0;
                for (int i = 0; i < columnModel.getColumnCount(); ++i) {
                    TableColumn column = columnModel.getColumn(i);
                    x += column.getWidth();
                    g.drawLine(x - 1, rowCount * rowHeight, x - 1, height);
                }
            }
        
    }
    
    /**
     * Returns the appropriate background color for the given row.
     */
    protected Color colorForRow(int row) {
        return (row % 2 == 0) ? alternateRowColor() : getBackground();
    }
    
    private Color alternateRowColor() {
        return isGtk() ? Color.WHITE : ALTERNATE_ROW_COLOR;
    }
    
    /**
     * Tests whether we're running on Mac OS. The Mac is quite
     * different from Linux and Windows, and it's sometimes
     * necessary to put in special-case behavior if you're running
     * on the Mac.
     */
    public static boolean isMacOs() {
        return System.getProperty("os.name").contains("Mac");
    }
    
    /**
     * Tests whether we're using the GTK+ LAF (and so are probably on Linux or Solaris).
     */
    public static boolean isGtk() {
        return UIManager.getLookAndFeel().getClass().getName().contains("GTK");
    }
    /**
     * The background color for alternate rows in lists and tables.
     */
    public static final Color ALTERNATE_ROW_COLOR = new Color(0.92f, 0.95f, 0.99f);
    private static final Color MAC_UNFOCUSED_UNSELECTED_VERTICAL_LINE_COLOR = new Color(0xd9d9d9);
    

}
