/*
 * org.openmicroscopy.shoola.agents.metadata.util.ScriptComponent 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.util.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import info.clearthought.layout.TableLayout;

import org.apache.commons.collections.CollectionUtils;
import org.openmicroscopy.shoola.util.CommonsLangUtils;

import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.env.data.model.ScriptObject;
import org.openmicroscopy.shoola.util.ui.NumericalTextField;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Hosts information related to a parameter for the script.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
class ScriptComponent
    extends JPanel
    implements ChangeListener
{

    /** The number of columns. */
    static int COLUMNS = 10;

    /** Indicates the tabulation value. */
    private static final int TAB = 15;

    /** The default text for the component.*/
    private static final String DEFAULT_TEXT = "";

    /** The component to host. */
    private JComponent component;

    /** The text associated to the component. */
    private JLabel label;

    /** The text associated to the component. */
    private JLabel requireLabel;

    /** Component indicating the units or other text. */
    private JLabel unitLabel;

    /** 
     * The text explaining the component. It should only be set for
     * collections and maps.
     */
    private JLabel info;

    /** Indicates if a value is required. */
    private boolean required;

    /** The index of the parent for grouping. */
    private String parentIndex;

    /** The name associated to the component. */
    private String nameLabel;

    /** The name of the parameter. */
    private String name;

    /** The grouping value or empty string. */
    private String grouping;

    /** The components related to the current component. */
    private List<ScriptComponent> children;

    /** The parent of the component. */
    private ScriptComponent parent;

    /** 
     * Iterates through the components and sets the background color.
     * 
     * @param c The component to handle
     * @param background The color to set.
     */
    private void setComponentColor(JComponent c, Color background)
    {
        c.setBackground(background);
        if (c.getComponentCount() > 0) {
            Component[] components = c.getComponents();
            for (int i = 0; i < components.length; i++) {
                if (components[i] instanceof JComponent) {
                    setComponentColor((JComponent) components[i], background);
                }
            }
        }
    }

    /**
     * Creates a row.
     * 
     * @return See above.
     */
    private JPanel createRow()
    {
        JPanel row = new JPanel();
        row.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.add(getLabel());
        row.add(getComponent());
        setComponentColor(row, ScriptingDialog.BG_COLOR);
        return row;
    }

    /**
     * Returns the tabulation index. Returns <code>0</code> if no parent index
     * is set, otherwise the number of level in the parent e.g.
     * if parent is <code>3</code>, the method returns <code>1</code>, 
     * if parent is <code>3.1</code>, the method returns <code>2</code>.
     * 
     * @return See above.
     */
    private int getTabulationLevel()
    {
        if (parentIndex == null || parentIndex.length() == 0)
            return 0;
        String[] values = parentIndex.split("\\.");
        return values.length;
    }

    /**
     * Returns the label associated to the component.
     * 
     * @return See above.
     */
    private JComponent getLabel()
    { 
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        if (requireLabel != null) {
            JPanel lp = new JPanel();
            lp.setLayout(new BoxLayout(lp, BoxLayout.X_AXIS));
            lp.add(label);
            lp.add(requireLabel);
            p.add(lp);
        } else p.add(label);

        if (info != null) p.add(info);
        return UIUtilities.buildComponentPanel(p, 0, 0); 
    }

    /**
     * Returns the component hosted.
     * 
     * @return See above.
     */
    private JComponent getComponent()
    { 
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.add(component);
        if (unitLabel != null) p.add(unitLabel);
        return p; 
    }

    /** Handles the selection. */
    private void handleSelection()
    {
        if (children == null) return;
        if (component instanceof JCheckBox) {
            Iterator<ScriptComponent> i = children.iterator();
            ScriptComponent c;
            JCheckBox box = (JCheckBox) component;
            boolean selected = box.isSelected();
            while (i.hasNext()) {
                c = i.next();
                c.setEnabled(selected);
            }
        }
    }

    /** Creates a new instance.*/
    ScriptComponent()
    {
        this(new JLabel(), DEFAULT_TEXT);
    }

    /**
     * Creates a new instance.
     * 
     * @param component The component to host.
     * @param name The name of the parameter
     */
    ScriptComponent(JComponent component, String name)
    {
        if (component == null)
            throw new IllegalArgumentException("No component specified.");
        if (name == null) name = DEFAULT_TEXT;
        if (component instanceof NumericalTextField) 
            ((NumericalTextField) component).setHorizontalAlignment(
                    JTextField.LEFT);
        this.component = component;
        this.name = name;
        parentIndex = null;
        //format
        if (!name.equals(DEFAULT_TEXT))
            name = " "+name+":";

        if (name.contains(ScriptObject.PARAMETER_SEPARATOR)) {
            label = UIUtilities.setTextFont(name.replace(
                    ScriptObject.PARAMETER_SEPARATOR, 
                    ScriptObject.PARAMETER_UI_SEPARATOR));
        } else {
            label = UIUtilities.setTextFont(name);
        }
        label.setToolTipText(component.getToolTipText());
        required = false;
    }

    /**
     * Sets the parameter name.
     *
     * @param name The value to set.
     */
    void setParameterName(String name) { this.name = name;}

    /**
     * Returns the name of the parameter.
     * 
     * @return See above.
     */
    String getParameterName() { return name; }

    /**
     * Sets the text explaining the component when the component is a list
     * or a map.
     * 
     * @param text The value to set.
     */
    void setInfo(String text)
    {
        if (CommonsLangUtils.isBlank(text)) return;
        info = new JLabel();
        Font f = info.getFont();
        info.setFont(f.deriveFont(Font.ITALIC, f.getSize()-2));
    }

    /**
     * Sets to <code>true</code> if a value is required for the field,
     * <code>false</code> otherwise.
     * 
     * @param required The value to set.
     */
    void setRequired(boolean required)
    { 
        this.required = required;
        if (required) {
            requireLabel = UIUtilities.setTextFont(EditorUtil.MANDATORY_SYMBOL);
            requireLabel.setForeground(UIUtilities.REQUIRED_FIELDS_COLOR);
            requireLabel.setToolTipText(label.getToolTipText());
        }
    }

    /**
     * Sets the text of the unit label.
     * 
     * @param text The value to set.
     */
    void setUnit(String text)
    {
        if (CommonsLangUtils.isBlank(text)) return;
        if (unitLabel == null) unitLabel = new JLabel();
        unitLabel.setText(text);
    }

    /**
     * Returns <code>true</code> if a value is required for that component.
     * 
     * @return See above.
     */
    boolean isRequired() { return required; }

    /** Builds and lays out the UI. */
    void buildUI()
    {
        int width = TAB*getTabulationLevel();
        if (DEFAULT_TEXT.equals(name) || CommonsLangUtils.isNumber(name)){
            width = 0;
        }
        if (CollectionUtils.isEmpty(children)) {
            double[][] size = {{width, TableLayout.PREFERRED, 5,
                TableLayout.FILL}, {TableLayout.PREFERRED}};
            setLayout(new TableLayout(size));
            add(Box.createHorizontalStrut(width), "0, 0");
            add(getLabel(), "1, 0");
            add(getComponent(), "3, 0");
        } else {
            //create a row.
            double[][] size = {{TableLayout.FILL}, {TableLayout.PREFERRED}};
            TableLayout layout = new TableLayout(size);
            setLayout(layout);
            Iterator<ScriptComponent> i = children.iterator();
            ScriptComponent child;
            setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            add(createRow(), "0, 0");
            int index = 1;
            while (i.hasNext()) {
                child = i.next();
                String v = child.parentIndex;
                if (name != null && name.equals(v)) {
                    child.parentIndex = null;
                }
                child.buildUI();
                child.parentIndex = v;
                layout.insertRow(index, TableLayout.PREFERRED);
                add(child, "0, "+index);
                index++;
            }
        }
    }

    /** 
     * Returns the value associated to a script.
     * 
     * @return See above.
     */
    Object getValue()
    {
        if (parent != null) {
            Object v = parent.getValue();
            if (v instanceof Boolean) {
                boolean b = ((Boolean) v).booleanValue();
                if (!b) return null;
            }
        }
        return ScriptComponent.getComponentValue(component);
    }

    /** 
     * Helper method. Returns the value associated to a script.
     * 
     * @param c The component to handle.
     * @return See above.
     */
    static Object getComponentValue(JComponent c)
    {
        if (c == null) return null;
        if (c instanceof JCheckBox) {
            JCheckBox box = (JCheckBox) c;
            return box.isSelected();
        } else if (c instanceof NumericalTextField) {
            return ((NumericalTextField) c).getValueAsNumber();
        } else if (c instanceof JTextField) {
            JTextField field = (JTextField) c;
            String value = field.getText();
            if (CommonsLangUtils.isBlank(value))
                return null;
            return value;
        } else if (c instanceof JComboBox) {
            JComboBox box = (JComboBox) c;
            return box.getSelectedItem();
        } else if (c instanceof ComplexParamPane) {
            return ((ComplexParamPane) c).getValue(); 
        } else if (c instanceof IdentifierParamPane) {
            return ((IdentifierParamPane) c).isReady();
        }
        return null;
    }

    /**
     * Sets the parent grouping values.
     * 
     * @param parentIndex The value to set.
     */
    void setParentIndex(String parentIndex)
    {
        if (parentIndex != null) parentIndex = parentIndex.trim();
        this.parentIndex = parentIndex;
    }

    /**
     * Returns the parent grouping values.
     * 
     * @return See above.
     */
    String getParentIndex() { return parentIndex; }

    /**
     * Returns the grouping.
     * 
     * @param grouping The value to set.
     */
    void setGrouping(String grouping) { this.grouping = grouping; }

    /**
     * Returns the grouping.
     * 
     * @return See above.
     */
    String getGrouping() { return grouping; }

    /**
     * Returns the children associated to that component.
     * 
     * @param children The value to set.
     */
    void setChildren(List<ScriptComponent> children)
    {
        this.children = children;
        if (component instanceof JCheckBox) {
            JCheckBox box = (JCheckBox) component;
            box.addChangeListener(this);
            Iterator<ScriptComponent> i = children.iterator();
            while (i.hasNext())
                i.next().setParent(this);
        }
        handleSelection();
    }

    /**
     * Sets the parent of the component.
     * 
     * @param parent The value to set.
     */
    void setParent(ScriptComponent parent)
    {
        if (children != null) return;
        this.parent = parent;
    }

    /**
     * Returns <code>true</code> if the component has children,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean hasChildren() { return CollectionUtils.isNotEmpty(children);}

    /**
     * Sets the name used to sort the object.
     * 
     * @param nameLabel The value to set.
     */
    void setNameLabel(String nameLabel) { this.nameLabel = nameLabel; }

    /** 
     * Controls the children is any set.
     * @see ChangeListener#stateChanged(ChangeEvent)
     */
    public void stateChanged(ChangeEvent e) { handleSelection(); }

    /**
     * Overridden to handle grouping.
     * @see JPanel#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);
        if (parentIndex == null) return;
        component.setEnabled(enabled);
    }

    /**
     * Overridden to sort the object using its label.
     * @see JPanel#toString()
     */
    public String toString() { return nameLabel; }

}
