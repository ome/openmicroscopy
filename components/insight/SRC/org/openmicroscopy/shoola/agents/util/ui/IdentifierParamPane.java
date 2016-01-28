/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.util.ui;


import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentListener;

import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import omero.gateway.model.DataObject;

/**
 * Component used to enter the identifiers.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
class IdentifierParamPane
    extends JPanel
{

    /** Displays the information.*/
    static final String DISPLAY_INFO_PROPERTY = "displayInfo";

    /** The text indicating how to enter the values.*/
    static final String INFO_TEXT = "Use ',' to separate values.";

    /** The type of values to return after parsing e.g. number.*/
    private Class<?> type;

    /** The field displayed the identifier.*/
    private JTextField field;

    /** The button displaying how to fill the text field.*/
    private JButton		infoButton;

    /** Flag indicating that a value is required. */
    private boolean required;

    /** Initializes the components.*/
    private void initializeComponents()
    {
        field = new JTextField(ScriptComponent.COLUMNS);
        IconManager icons = IconManager.getInstance();
        infoButton = new JButton(icons.getIcon(IconManager.INFO));
        infoButton.setToolTipText(INFO_TEXT);
        UIUtilities.unifiedButtonLookAndFeel(infoButton);
        infoButton.addMouseListener(new MouseAdapter() {

            /** Fires a property indicating to display the information.
             * @see MouseListener#mousePressed(MouseEvent)
             */
            public void mousePressed(MouseEvent e){
                Point p = e.getPoint();
                SwingUtilities.convertPointToScreen(p, infoButton);
                firePropertyChange(DISPLAY_INFO_PROPERTY, null, p);
            }
        });
    }

    /**
     * Builds and lays out the UI.
     *
     * @param showInfo Pass <code>true</code> to show the info button.
     */
    private void buildGUI(boolean showInfo)
    {
        setLayout(new FlowLayout(FlowLayout.LEFT));
        if (showInfo) add(infoButton);
        add(field);
    }

    /**
     * Creates a new instance with a requested field.
     *
     * @param type The type of values to return after parsing e.g. number.
     */
    IdentifierParamPane(Class<?> type)
    {
        this(type, true, true);
    }

    /**
     * Creates a new instance with a requested field.
     *
     * @param type The type of values to return after parsing e.g. number.
     * @param showInfo Pass <code>true</code> to show the info button.
     */
    IdentifierParamPane(Class<?> type, boolean showInfo)
    {
        this(type, true, showInfo);
    }

    /**
     * Creates a new instance with a requested field.
     *
     * @param type The type of values to return after parsing e.g. number.
     * @param required Pass <code>true</code> to indicate that a value is
     *                 required, <code>false</code> otherwise.
     * @param showInfo Pass <code>true</code> to show the info button.
     */
    IdentifierParamPane(Class<?> type, boolean required, boolean showInfo)
    {
        this.type = type;
        this.required = required;
        initializeComponents();
        buildGUI(showInfo);
    }

    /**
     * Sets the values.
     * 
     * @param values The values to set.
     */
    void setValues(List<DataObject> values)
    {
        if (values == null) {
            field.setText("");
            return;
        }
        Iterator<DataObject> i = values.iterator();
        StringBuffer buffer = new StringBuffer();
        int index = 0;
        int n = values.size()-1;
        while (i.hasNext()) {
            buffer.append(i.next().getId());
            if (index < n)
                buffer.append(", ");
            index++;
        }
        field.setText(buffer.toString());
    }

    /**
     * Returns the values entered as String.
     *
     * @return See above.
     */
    List<String> getValuesAsString()
    {
        String text = field.getText();
        if (text == null) return null;
        String[] values = text.split(",");
        if (values == null || values.length == 0)
            return null;
        List<String> l = new ArrayList<String>();
        String v;
        for (int i = 0; i < values.length; i++) {
            v = values[i];
            v = v.trim();
            l.add(v);
        }
        return l;
    }

    /**
     * Returns the values entered as Long.
     *
     * @return See above.
     */
    List<Long> getValuesAsLong()
    {
        String text = field.getText();
        if (text == null) return null;
        if (!Long.class.equals(type)) return null;
        String[] values = text.split(",");
        if (values == null || values.length == 0)
            return null;
        List<Long> l = new ArrayList<Long>();
        String v;
        for (int i = 0; i < values.length; i++) {
            v = values[i];
            v = v.trim();
            try {
                l.add(Long.parseLong(v));
            } catch (Exception e) {
                //ignore
            }
        }
        return l;
    }

    /**
     * Returns the values entered in the text field.
     *
     * @return See above
     */
    List<Object> getValues()
    {
        String text = field.getText();
        if (text == null) return null;
        String[] values = text.split(",");
        if (values == null || values.length == 0)
            return null;
        List<Object> l = new ArrayList<Object>();
        String v;
        for (int i = 0; i < values.length; i++) {
            v = values[i];
            v = v.trim();
            if (Long.class.equals(type)) {
                try {
                    l.add(Long.parseLong(v));
                } catch (Exception e) {}
            } else {
                l.add(v);
            }
        }
        return l;
    }

    /**
     * Returns <code>true</code> if the value entered is valid,
     * <code>false</code> otherwise.
     *
     * @return See above.
     */
    List<Object> isReady()
    {
        if (!required) return null;
        return getValues();
    }

    /**
     * Adds the listener to the text field.
     *
     * @param listener The listener to handle.
     */
    void addDocumentListener(DocumentListener listener)
    {
        if (listener == null) return;
        field.getDocument().addDocumentListener(listener);
    }

}
