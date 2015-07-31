/*
 * org.openmicroscopy.shoola.agents.metadata.util.ScriptingDialog 
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import info.clearthought.layout.TableLayout;

import org.apache.commons.collections.CollectionUtils;
import org.openmicroscopy.shoola.util.CommonsLangUtils;

import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import org.openmicroscopy.shoola.env.data.model.ParamData;
import org.openmicroscopy.shoola.env.data.model.ScriptObject;
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.NumericalTextField;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.omeeditpane.OMEWikiComponent;
import org.openmicroscopy.shoola.util.ui.tdialog.TinyDialog;

import pojos.DataObject;

/** 
 * Dialog to run the selected script. The UI is created on the fly.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class ScriptingDialog
    extends JDialog
    implements ActionListener, DocumentListener, PropertyChangeListener
{

    /** Message indicating that script can only run on objects.*/
    public static final String WARNING = 
       "This script can only run on data from the same group.\n" +
       "Please perform a separate run of the script for each group,\n" +
       "selecting only data from a single group to run the script on each time.";

    /** Bound property indicating to run the script. */
    public static final String RUN_SELECTED_SCRIPT_PROPERTY =
            "runSelectedScript";

    /** Bound property indicating to download the script. */
    public static final String DOWNLOAD_SELECTED_SCRIPT_PROPERTY =
            "downloadSelectedScript";

    /** Bound property indicating to view the script. */
    public static final String VIEW_SELECTED_SCRIPT_PROPERTY =
            "viewSelectedScript";

    /** Bound property indicating to close the dialog. */
    public static final String CLOSE_SCRIPT_PROPERTY = "closeScript";

    /** The background of the description. */
    static final Color BG_COLOR = Color.LIGHT_GRAY;

    /**
     * The size of the invisible components used to separate buttons
     * horizontally.
     */
    private static final Dimension H_SPACER_SIZE = new Dimension(5, 10);

    /** Title of the dialog. */
    private static final String TITLE = "Run Script";

    /** The text displayed in the header. */
    private static final String TEXT = "Set the parameters of the " +
            "selected script.";

    /** The text displayed in the header. */
    private static final String TEXT_END = EditorUtil.MANDATORY_SYMBOL +
            " indicates the required parameter.";

    /** Indicates to close the dialog. */
    private static final int CANCEL = 0;

    /** Indicates to run the script. */
    private static final int APPLY = 1;

    /** Indicates to download the script. */
    private static final int DOWNLOAD = 2;

    /** Indicates to view the script. */
    private static final int VIEW = 3;

    /** Close the dialog. */
    private JButton cancelButton;

    /** Run the script. */
    private JButton applyButton;

    /** Menu offering the ability to download or view the script. */
    private JButton menuButton;

    /** The object to handle. */
    private ScriptObject script;

    /** The components to display. */
    private Map<String, ScriptComponent> components;

    /** The components to display. */
    private Map<String, ScriptComponent> componentsAll;

    /** Used to sort collections. */
    private ViewerSorter sorter;

    /** The menu offering various options to manipulate the script. */
    private JPopupMenu optionMenu;

    /** The objects of reference.*/
    private List<DataObject> refObjects;

    /** The component displaying the data types.*/
    private JComboBox dataTypes;

    /** The component related to the identifier.*/
    private IdentifierParamPane identifier;

    /** Flag indicating if the binary data are available.*/
    private boolean binaryAvailable;

    /** Populates the changes of data types.*/
    private void handleDataTypeChanges()
    {
        if (identifier == null || dataTypes == null) return;
        Object o = dataTypes.getSelectedItem();
        Class<?> c = script.convertDataType(o.toString());
        if (c != null && CollectionUtils.isNotEmpty(refObjects)) {
            DataObject object = refObjects.get(0);
            if (object.getClass().equals(c))
                identifier.setValues(refObjects);
            else identifier.setValues(null);
        }
    }

    /** Sets the type according to the selected objects.*/
    private void setSelectedDataType()
    {
        if (identifier == null || dataTypes == null ||
                CollectionUtils.isEmpty(refObjects))
            return;
        DataObject object = refObjects.get(0);
        Class<?> klass = object.getClass();
        Class<?> c;
        int selected = dataTypes.getSelectedIndex();
        for (int i = 0; i < dataTypes.getItemCount(); i++) {
            c = script.convertDataType(dataTypes.getItemAt(i).toString());
            if (klass.equals(c)) {
                selected = i;
                break;
            }
        }
        dataTypes.setSelectedIndex(selected);
    }

    /** 
     * Creates the option menu.
     * 
     * @return See above.
     */
    private JPopupMenu createOptionMenu()
    {
        if (optionMenu != null) return optionMenu;
        optionMenu = new JPopupMenu();
        optionMenu.add(createButton("Download", DOWNLOAD));
        optionMenu.add(createButton("View", VIEW));
        return optionMenu;
    }

    /**
     * Creates a button.
     *
     * @param text The text of the button.
     * @param actionID The action command id.
     * @return See above.
     */
    private JButton createButton(String text, int actionID)
    {
        JButton b = new JButton(text);
        b.setActionCommand(""+actionID);
        b.addActionListener(this);
        b.setOpaque(false);
        UIUtilities.unifiedButtonLookAndFeel(b);
        return b;
    }

    /** Closes the dialog. */
    private void close()
    {
        setVisible(false);
        firePropertyChange(CLOSE_SCRIPT_PROPERTY, Boolean.valueOf(false),
                Boolean.valueOf(true));
    }

    /**
     * Displays information of the identifier.
     *
     * @param location Indicates where to display the component.
     */
    private void displayIdentifierInformation(Point location)
    {
        JLabel l = new JLabel(IdentifierParamPane.INFO_TEXT);
        TinyDialog d = new TinyDialog((Frame) getOwner(), l,
                TinyDialog.CLOSE_ONLY);
        d.pack();
        d.setLocation(location);
        d.setVisible(true);
    }

    /**
     * Sets the enabled flag of the {@link #applyButton} to <code>true</code>
     * if the script can run i.e. all required fields are filled, to 
     * <code>false</code> otherwise.
     */
    private void canRunScript()
    {
        Iterator<Entry<String, ScriptComponent>>
        i = components.entrySet().iterator();
        Entry<String, ScriptComponent> entry;
        ScriptComponent c;
        int required = 0;
        int valueSet = 0;
        Object value;
        while (i.hasNext()) {
            entry = i.next();
            c = entry.getValue();
            if (c.isRequired()) {
                required++;
                value = c.getValue();
                if (value != null) {
                    if (value instanceof String) {
                        if (CommonsLangUtils.isNotBlank((String) value)) valueSet++;
                    } else if (value instanceof List) {
                        List<Object> l = (List<Object>) value;
                        if (l.size() > 0) valueSet++;
                    } else if (value instanceof Map) {
                        Map<Object, Object> m = (Map<Object, Object>) value;
                        if (m.size() > 0) valueSet++;
                    } else valueSet++;
                }
            }
        }
        applyButton.setEnabled(required == valueSet);
        if (!binaryAvailable) applyButton.setEnabled(false);
    }

    /** Collects the data and fires a property.*/
    private void runScript()
    {
        Entry<String, ScriptComponent> entry;
        ScriptComponent c;
        Iterator<Entry<String, ScriptComponent>> i =
                componentsAll.entrySet().iterator();
        Map<String, ParamData> inputs = script.getInputs();
        ParamData param;
        while (i.hasNext()) {
            entry = i.next();
            c = entry.getValue();
            param = inputs.get(entry.getKey());
            param.setValueToPass(c.getValue());
        }
        if (CollectionUtils.isNotEmpty(refObjects)) {
            DataObject node = refObjects.get(0);
            script.setGroupID(node.getGroupId());
        }
        firePropertyChange(RUN_SELECTED_SCRIPT_PROPERTY, null, script);
        close();
    }

    /**
     * Creates a component displaying the various options.
     *
     * @param values The values to display.
     * @param defValue The default value.
     * @return See above.
     */
    private JComboBox createValuesBox(List<Object> values, Object defValue)
    {
        if (values == null) return null;
        Object[] v = new Object[values.size()];
        Iterator<Object> i = values.iterator();
        int j = 0;
        int index = 0;
        while (i.hasNext()) {
            v[j] = i.next();
            if (v[j].equals(defValue)) {
                index = j;
            }
            j++;
        }
        JComboBox box = new JComboBox(v);
        box.setSelectedIndex(index);
        return box;
    }

    /** Initializes the components. */
    private void initComponents()
    {
        sorter = new ViewerSorter();
        cancelButton = new JButton("Cancel");
        cancelButton.setToolTipText("Close the dialog.");
        cancelButton.setActionCommand(""+CANCEL);
        cancelButton.addActionListener(this);
        applyButton = new JButton("Run Script");
        applyButton.setToolTipText("Run the selected script.");
        applyButton.setActionCommand(""+APPLY);
        applyButton.addActionListener(this);
        IconManager icons = IconManager.getInstance();
        menuButton = new JButton(icons.getIcon(IconManager.FILTER_MENU));
        menuButton.setText("Script");
        menuButton.setHorizontalTextPosition(JButton.LEFT);
        menuButton.addMouseListener(new MouseAdapter() {

            public void mouseReleased(MouseEvent e) {
                Object src = e.getSource();
                if (src instanceof Component) {
                    Point p = e.getPoint();
                    createOptionMenu().show((Component) src, p.x, p.y);
                }
            }
        });
        components = new LinkedHashMap<String, ScriptComponent>();
        componentsAll = new LinkedHashMap<String, ScriptComponent>();
        Map<String, ParamData> types = script.getInputs();
        if (types == null) return;
        List <ScriptComponent> results = new ArrayList<ScriptComponent>();
        Entry<String, ParamData> entry;
        ParamData param;
        JComponent comp;
        ScriptComponent c;
        String name;
        Class<?> type;
        Object defValue ;
        Iterator<Entry<String, ParamData>> i = types.entrySet().iterator();
        List<Object> values;
        Number n;
        String details = "";
        String text = "";
        String grouping;
        String parent;
        Map<String, List<ScriptComponent>> childrenMap =
                new HashMap<String, List<ScriptComponent>>();
        List<ScriptComponent> l;
        int length;
        boolean columnsSet;
        while (i.hasNext()) {
            text = "";
            columnsSet = false;
            comp = null;
            entry = i.next();
            param = entry.getValue();
            name = entry.getKey();
            type = param.getPrototype();
            values = param.getValues();
            defValue = param.getDefaultValue();
            if (CollectionUtils.isNotEmpty(values)) {
                comp = createValuesBox(values, defValue);
            }
            if (Long.class.equals(type) || Integer.class.equals(type) ||
                    Float.class.equals(type) || Double.class.equals(type)) {
                if (comp == null) {
                    if (script.isIdentifier(name)) {
                        comp = new NumericalTextField();
                        ((NumericalTextField) comp).setNumberType(type);
                        ((NumericalTextField) comp).setNegativeAccepted(false);
                        if (CollectionUtils.isNotEmpty(refObjects)) {
                            //support of image type
                            DataObject object = refObjects.get(0);
                            if (script.isSupportedType(object, name)){
                                defValue = object.getId();
                            }
                        }
                    } else {
                        comp = new NumericalTextField();
                        ((NumericalTextField) comp).setNumberType(type);
                        n = param.getMinValue();
                        if (n != null) {
                            if (Long.class.equals(type)) {
                                text += "Min: "+n.longValue()+" ";
                                ((NumericalTextField) comp).setMinimum(
                                        n.longValue());
                            } else if (Integer.class.equals(type)) {
                                text += "Min: "+n.intValue()+" ";
                                ((NumericalTextField) comp).setMinimum(
                                        n.intValue());
                            } else if (Double.class.equals(type)) {
                                text += "Min: "+n.doubleValue()+" ";
                                ((NumericalTextField) comp).setMinimum(
                                        n.doubleValue());
                            } else if (Float.class.equals(type)) {
                                text += "Min: "+n.floatValue()+" ";
                                ((NumericalTextField) comp).setMinimum(
                                        n.floatValue());
                            }
                        } else {
                            ((NumericalTextField) comp).setNegativeAccepted(true);
                        }
                        n = param.getMaxValue();
                        if (n != null) {
                            if (Long.class.equals(type)) {
                                text += "Max: "+n.longValue()+" ";
                                ((NumericalTextField) comp).setMaximum(
                                        n.longValue());
                            } else if (Integer.class.equals(type)) {
                                text += "Max: "+n.intValue()+" ";
                                ((NumericalTextField) comp).setMaximum(
                                        n.intValue());
                            } else if (Double.class.equals(type)) {
                                text += "Max: "+n.doubleValue()+" ";
                                ((NumericalTextField) comp).setMaximum(
                                        n.doubleValue());
                            } else if (Float.class.equals(type)) {
                                text += "Max: "+n.floatValue()+" ";
                                ((NumericalTextField) comp).setMaximum(
                                        n.floatValue());
                            } 
                        }
                    }
                    if (defValue != null)
                        ((NumericalTextField) comp).setText(""+defValue);
                }
            } else if (String.class.equals(type)) {
                if (comp == null) {
                    comp = new JTextField();
                    if (defValue != null) {
                        length = defValue.toString().length();
                        String s = defValue.toString().trim();
                        ((JTextField) comp).setColumns(length);
                        ((JTextField) comp).setText(s);
                        columnsSet = s.length() > 0;
                    }
                }
            } else if (Boolean.class.equals(type)) {
                if (comp == null) {
                    comp = new JCheckBox();
                    if (defValue != null)
                        ((JCheckBox) comp).setSelected((Boolean) defValue);
                }
            } else if (Map.class.equals(type)) {
                if (comp == null)
                    comp = new ComplexParamPane(param.getKeyType(),
                            param.getValueType());
                else 
                    comp = new ComplexParamPane(param.getKeyType(),
                            (JComboBox) comp);
            } else if (List.class.equals(type)) {
                if (script.isIdentifier(name)) {
                    identifier = new IdentifierParamPane(Long.class);
                    identifier.setValues(refObjects);
                    identifier.addDocumentListener(this);
                    comp = identifier;
                } else {
                    if (comp == null)
                        comp = new ComplexParamPane(param.getKeyType());
                    else 
                        comp = new ComplexParamPane((JComboBox) comp);
                }
            }
            if (comp != null) {
                if (comp instanceof JTextField) {
                    if (!columnsSet)
                        ((JTextField) comp).setColumns(ScriptComponent.COLUMNS);
                    ((JTextField) comp).getDocument().addDocumentListener(this);
                }
                if (comp instanceof ComplexParamPane)
                    comp.addPropertyChangeListener(this);
                comp.setToolTipText(param.getDescription());
                c = new ScriptComponent(comp, name);
                if (text.trim().length() > 0) c.setUnit(text);
                if (!(comp instanceof JComboBox || comp instanceof JCheckBox))
                    c.setRequired(!param.isOptional());
                if (details != null && details.trim().length() > 0)
                    c.setInfo(details);
                if (comp instanceof JComboBox && script.isDataType(name)) {
                    dataTypes = (JComboBox) comp;
                    dataTypes.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent e) {
                            handleDataTypeChanges();
                        }
                    });
                }
                grouping = param.getGrouping();
                parent = param.getParent();
                c.setParentIndex(parent);
                if (parent.length() > 0) {
                    l = childrenMap.get(parent);
                    if (l == null) {
                        l = new ArrayList<ScriptComponent>();
                        childrenMap.put(parent, l);
                    }
                    l.add(c);
                }

                if (grouping.length() > 0) {
                    c.setGrouping(grouping);
                    c.setNameLabel(grouping);
                } else {
                    c.setNameLabel(name);
                }
                if (c.hasChildren() || parent.length() == 0) {
                    results.add(c);
                } 
                componentsAll.put(c.getParameterName(), c);
            }
        }
        ScriptComponent key;
        Iterator<ScriptComponent> k = results.iterator();
        while (k.hasNext()) {
            key = k.next();
            grouping = key.getGrouping();
            l = childrenMap.get(grouping);
            childrenMap.remove(grouping);
            if (l != null)
               key.setChildren(sorter.sort(l));
        }
        if (childrenMap != null && childrenMap.size() > 0) {
            Iterator<String> j = childrenMap.keySet().iterator();
            ScriptComponent sc;
            while (j.hasNext()) {
                parent = j.next();
                sc = new ScriptComponent();
                sc.setGrouping(parent);
                sc.setNameLabel(parent);
                if (CommonsLangUtils.isBlank(sc.getName())) {
                    sc.setParameterName(parent);
                }
                sc.setChildren(sorter.sort(childrenMap.get(parent)));
                results.add(sc);
            }
        }
        List<ScriptComponent> sortedKeys = sorter.sort(results);
        k = sortedKeys.iterator();

        while (k.hasNext()) {
            key = k.next();
            components.put(key.getParameterName(), key);
        }
        setSelectedDataType();
        if (identifier != null) 
            identifier.addPropertyChangeListener(this);
        canRunScript();
    }

    /**
     * Builds the panel hosting the components.
     *
     * @return See above.
     */
    private JPanel buildControlPanel()
    {
        JPanel controlPanel = new JPanel();
        controlPanel.setBorder(null);
        controlPanel.add(cancelButton);
        controlPanel.add(Box.createRigidArea(H_SPACER_SIZE));
        controlPanel.add(applyButton);
        controlPanel.add(Box.createRigidArea(H_SPACER_SIZE));
        JPanel bar = new JPanel();
        bar.setLayout(new BoxLayout(bar, BoxLayout.Y_AXIS));
        bar.add(controlPanel);
        bar.add(Box.createVerticalStrut(10));

        JPanel all = new JPanel();
        all.setLayout(new BoxLayout(all, BoxLayout.X_AXIS));
        all.add(UIUtilities.buildComponentPanel(menuButton));
        all.add(UIUtilities.buildComponentPanelRight(bar));
        return all;
    }

    /**
     * Returns the component displaying the description of the script.
     *
     * @return See above.
     */
    private JComponent buildDescriptionPane()
    {
        String description = script.getDescription();
        if (CommonsLangUtils.isBlank(description))
            return  null;
        OMEWikiComponent area = new OMEWikiComponent(false);
        area.setEnabled(false);
        area.setText(description);
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        JLabel label = UIUtilities.setTextFont(script.getName());
        Font f = label.getFont();
        label.setFont(f.deriveFont(f.getStyle(), f.getSize()+2));
        content.add(UIUtilities.buildComponentPanel(label));
        content.add(Box.createVerticalStrut(5));
        JPanel p = UIUtilities.buildComponentPanel(area);
        p.setBackground(BG_COLOR);
        area.setBackground(BG_COLOR);
        content.add(p);
        return content;
    }

    /** 
     * Builds and lays out the details of the script e.g.
     * authors, contact, version.
     * 
     * @return See above.
     */
    private JPanel buildScriptDetails()
    {
        String[] authors = script.getAuthors();
        String contact = script.getContact();
        String version = script.getVersion();
        if (authors == null && contact == null && version == null)
            return null;
        JPanel p = new JPanel();
        p.setBackground(BG_COLOR);
        double[] columns = {TableLayout.PREFERRED, 5, TableLayout.FILL};
        TableLayout layout = new TableLayout();
        layout.setColumn(columns);
        p.setLayout(layout);
        int row = 0;
        JLabel l;
        if (authors != null && authors.length > 0) {
            l = UIUtilities.setTextFont("Authors:");
            StringBuffer buffer = new StringBuffer();
            int n = authors.length-1;
            for (int i = 0; i < authors.length; i++) {
                buffer.append(authors[i]);
                if (i < n) buffer.append(", ");
            }
            layout.insertRow(row, TableLayout.PREFERRED);
            p.add(l, "0,"+row);
            l = new JLabel();
            l.setText(buffer.toString());
            p.add(l, "2,"+row);
            row++;
        }
        if (CommonsLangUtils.isNotBlank(contact)) {
            l = UIUtilities.setTextFont("Contact:");
            layout.insertRow(row, TableLayout.PREFERRED);
            p.add(l, "0,"+row);
            l = new JLabel();
            l.setText(contact);
            p.add(l, "2,"+row);
            row++;
        }
        if (CommonsLangUtils.isNotBlank(version)) {
            l = UIUtilities.setTextFont("Version:");
            layout.insertRow(row, TableLayout.PREFERRED);
            p.add(l, "0,"+row);
            l = new JLabel();
            l.setText(version);
            p.add(l, "2,"+row);
        }
        if (p.getComponentCount() == 0) return null;
        return p;
    }

    /**
     * Builds the component displaying the parameters.
     *
     * @return See above.
     */
    private JPanel buildBody()
    {
        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        double[] columns = {TableLayout.PREFERRED, 5, TableLayout.FILL};
        TableLayout layout = new TableLayout();
        layout.setColumn(columns);
        p.setLayout(layout);
        int row = 0;
        JComponent area = buildDescriptionPane();
        JComponent authorsPane = buildScriptDetails();
        if (area != null) {
            layout.insertRow(row, TableLayout.PREFERRED);
            p.add(area, "0,"+row+", 2, "+row);
            row++;
        }

        if (authorsPane != null) {
            layout.insertRow(row, TableLayout.PREFERRED);
            p.add(authorsPane, "0,"+row+", 2, "+row);
            row++;
        }
        layout.insertRow(row, 5);

        row++;
        Entry<String, ScriptComponent> entry;
        Iterator<Entry<String, ScriptComponent>>
        i = components.entrySet().iterator();
        ScriptComponent comp;
        int required = 0;
        while (i.hasNext()) {
            entry = i.next();
            comp = entry.getValue();
            layout.insertRow(row, TableLayout.PREFERRED);
            comp.buildUI();
            if (comp.isRequired()) required++;
            p.add(comp, "0,"+row+", 2, "+row);
            row++;
            layout.insertRow(row, 2);
            row++;
        }
        if (required > 0) {
            JLabel label = new JLabel(TEXT_END);
            Font font = label.getFont();
            label.setFont(font.deriveFont(font.getStyle(), font.getSize()-2));
            label.setForeground(UIUtilities.REQUIRED_FIELDS_COLOR);
            layout.insertRow(row, TableLayout.PREFERRED);
            p.add(UIUtilities.buildComponentPanel(label), "0,"+row+",2, "+row);
        }

        JPanel controls = new JPanel();
        controls.setLayout(new BorderLayout(0, 0));
        controls.add(new JScrollPane(p), BorderLayout.CENTER);
        controls.add(buildControlPanel(), BorderLayout.SOUTH);
        return controls;
    }

    /** Builds and lays out the UI. */
    private void buildGUI()
    {
        String text = TEXT;
        TitlePanel tp = new TitlePanel(TITLE, text, script.getIconLarge());
        Container c = getContentPane();
        c.removeAll();
        c.setLayout(new BorderLayout(0, 0));
        c.add(tp, BorderLayout.NORTH);
        c.add(buildBody(), BorderLayout.CENTER);
    }

    /**
     * Creates a new instance.
     *
     * @param parent The parent of the frame.
     * @param script The script to run. Mustn't be <code>null</code>.
     * @param refObjects The objects of reference.
     * @param binaryAvailable Flag indicating if binary data are available.
     */
    public ScriptingDialog(JFrame parent, ScriptObject script,
            List<DataObject> refObjects, boolean binaryAvailable)
    {
        super(parent);
        this.binaryAvailable = binaryAvailable;
        reset(script, refObjects);
    }

    /**
     * Resets the value.
     *
     * @param script The script to run. Mustn't be <code>null</code>.
     * @param refObjects The objects of reference.
     */
    public void reset(ScriptObject script, List<DataObject> refObjects)
    {
        if (script == null)
            throw new IllegalArgumentException("No script specified");
        this.script = script;
        this.refObjects = refObjects;
        initComponents();
        buildGUI();
        pack();
    }

    /**
     * Closes or runs the scripts.
     * @see ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        int index = Integer.parseInt(e.getActionCommand());
        switch (index) {
        case CANCEL:
            close();
            break;
        case APPLY:
            runScript();
            break;
        case DOWNLOAD:
            firePropertyChange(DOWNLOAD_SELECTED_SCRIPT_PROPERTY, null, script);
            break;
        case VIEW:
            firePropertyChange(VIEW_SELECTED_SCRIPT_PROPERTY, null, script);
        }
    }

    /**
     * Checks if the user can run the script.
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        String name = evt.getPropertyName();
        if (RowPane.MODIFIED_CONTENT_PROPERTY.equals(name))
            canRunScript();
        else if (IdentifierParamPane.DISPLAY_INFO_PROPERTY.equals(name)) {
            displayIdentifierInformation((Point) evt.getNewValue());
        }
    }

    /**
     * Allows the user to run or not the script.
     * @see DocumentListener#insertUpdate(DocumentEvent)
     */
    public void insertUpdate(DocumentEvent e) { canRunScript(); }

    /**
     * Allows the user to run or not the script.
     * @see DocumentListener#removeUpdate(DocumentEvent)
     */
    public void removeUpdate(DocumentEvent e) { canRunScript(); }

    /**
     * Required by the {@link DocumentListener} I/F but no-operation 
     * implementation in our case.
     * @see DocumentListener#changedUpdate(DocumentEvent)
     */
    public void changedUpdate(DocumentEvent e) {}

}
