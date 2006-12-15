/*
 * adminTool.ListRenderer 
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package src.adminTool.ui;

// Java imports
import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

// Third-party libraries

// Application-internal dependencies

/**
 * 
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $Date: $)
 *          </small>
 * @since OME3.0
 */
public class ListRenderer extends JLabel implements ListCellRenderer {

    /**
     * Border of the cell.
     */
    private Border emptyBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);

    /**
     * Constructor for the ColourListRenderer(sets the backgroud ot opaque.
     */
    ListRenderer() {
        setOpaque(true);
    }

    /**
     * Overridden method
     * 
     * @see javax.swing.ListCellRenderer#getListCellRendererComponent
     *      (javax.swing.JList, java.lang.Object, int, boolean, boolean)
     */
    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean hasFocus) {

        this.setVerticalAlignment(SwingConstants.CENTER);
        setText((String) value);
        this.setHorizontalAlignment(SwingConstants.LEFT);
        if (isSelected) {
            setForeground(list.getSelectionForeground());
            setBackground(list.getSelectionBackground());
        } else {
            if (index % 2 == 0)
                setBackground(Color.white);
            else
                setBackground(new Color(236, 243, 254));
            setForeground(list.getForeground());
        }

        setBorder(emptyBorder);

        return this;
    }
}
