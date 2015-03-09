/*
 * org.openmicroscopy.shoola.util.ui.search.SearchPanel 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.dataBrowser.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.collections.CollectionUtils;
import org.openmicroscopy.shoola.util.CommonsLangUtils;
import org.jdesktop.swingx.JXDatePicker;
import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserAgent;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.finder.FinderFactory;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.IconManager;
import org.openmicroscopy.shoola.util.ui.SeparatorPane;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.search.ExperimenterContext;
import org.openmicroscopy.shoola.util.ui.search.GroupContext;
import org.openmicroscopy.shoola.util.ui.search.SearchContext;
import org.openmicroscopy.shoola.util.ui.search.SearchObject;
import org.openmicroscopy.shoola.util.ui.search.SearchUtil;

import pojos.ExperimenterData;

/**
 * The Component hosting the various fields used to collect the context of the
 * search.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk"
 *         >donald@lifesci.dundee.ac.uk</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $Date: $) </small>
 * @since OME3.0
 */
public class SearchPanel extends JPanel {

    /** Indicates that the date is to be interpreted as acquisition date */
    public static final String ITEM_ACQUISITIONDATE = "Acquisition date";

    /** Indicates that the date is to be interpreted as import date */
    public static final String ITEM_IMPORTDATE = "Import date";

    /** The title of the type UI component. */
    private static final String TYPE_TITLE = "Type";

    /** The number of columns of the search areas. */
    private static final int AREA_COLUMNS = 12;

    private static final String DATE_TOOLTIP = "<html>Please select a date from the drop-down menu or enter<br> a date in the format YYYY-MM-DD (e. g. 2014-07-10)</html>";
 
    public static final String DATE_TYPE_TOOLTIP = "Select the type of date (Acquisition date applies to images only)";

    /** The terms to search for. */
    private JTextField fullTextArea;

    /** Date used to specify the beginning of the time interval. */
    private JXDatePicker fromDate;

    /** Date used to specify the ending of the time interval. */
    private JXDatePicker toDate;

    /** Button to clear both date fields */
    private JButton clearDate;

    /** Reference to the model . */
    private SearchComponent model;

    /** Items used to defined the scope of the search (Name, Description, ...). */
    private Map<Integer, JCheckBox> scopes;

    /**
     * Items used to defined the scope of the search. (Images, Projects,
     * Datasets, ...).
     */
    private Map<Integer, JCheckBox> types;

    /** Button to bring up the tooltips for help. */
    private JButton helpBasicButton;

    /** The component used to perform a basic search. */
    private JPanel basicSearchComp;

    /** The component hosting either the advanced or basic search component. */
    private JPanel searchFor;

    /** The box displaying the groups. */
    private JComboBox groupsBox;

    /** The box displaying the users. */
    private JComboBox usersBox;

    /** Box for selecting the type of the dates */
    private JComboBox dateBox;

    /**
     * Creates a <code>JComboBox</code> with the available groups.
     * 
     * @return See above.
     */
    private JComboBox createGroupBox() {
        List<GroupContext> groups = model.getGroups();
        Object[] values = new Object[groups.size() + 1];
        values[0] = new GroupContext("All groups", GroupContext.ALL_GROUPS_ID);
        int j = 1;
        Iterator<GroupContext> i = groups.iterator();
        while (i.hasNext()) {
            values[j] = i.next();
            j++;
        }
        return new JComboBox(values);
    }

    /** Initializes the components composing the display. */
    private void initComponents() {
        usersBox = new JComboBox();
        groupsBox = createGroupBox();
        scopes = new HashMap<Integer, JCheckBox>(model.getNodes().size());
        types = new HashMap<Integer, JCheckBox>(model.getTypes().size());
        IconManager icons = IconManager.getInstance();
        fromDate = UIUtilities.createDatePicker(true, EditorUtil.DATE_PICKER_FORMAT);
        fromDate.setBackground(UIUtilities.BACKGROUND_COLOR);
        fromDate.addPropertyChangeListener("date",
                new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        Date d = (Date) evt.getNewValue();
                        if (d != null) {
                            if (d.after(new Date())) {
                                UserNotifier un = DataBrowserAgent
                                        .getRegistry().getUserNotifier();
                                un.notifyWarning("Invalid Date",
                                        "Selecting a future 'From' date doesn't make any sense.");
                                fromDate.setDate(null);
                            }
                            if (toDate.getDate() != null
                                    && d.after(toDate.getDate())) {
                                UserNotifier un = DataBrowserAgent
                                        .getRegistry().getUserNotifier();
                                un.notifyWarning("Invalid Date",
                                        "Cannot set a 'From' date which is more recent than the 'To' date.");
                                fromDate.setDate(null);
                            }
                        }
                    }
                });
        fromDate.setToolTipText(DATE_TOOLTIP);
        
        toDate = UIUtilities.createDatePicker(true, EditorUtil.DATE_PICKER_FORMAT);
        toDate.setBackground(UIUtilities.BACKGROUND_COLOR);
        toDate.setToolTipText(DATE_TOOLTIP);
        toDate.addPropertyChangeListener("date", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                Date d = (Date) evt.getNewValue();
                if (d != null) {
                    if (fromDate.getDate() != null && d.before(fromDate.getDate())) {
                        UserNotifier un = DataBrowserAgent.getRegistry()
                                .getUserNotifier();
                        un.notifyWarning("Invalid Date",
                                "Cannot set a 'To' date which is prior to the 'From' date.");
                        toDate.setDate(null);
                    }
                }
            }
        });
        
        clearDate = new JButton(icons.getIcon(IconManager.CLOSE));
        clearDate.setToolTipText("Reset the dates");
        UIUtilities.unifiedButtonLookAndFeel(clearDate);
        clearDate.setBackground(UIUtilities.BACKGROUND_COLOR);
        clearDate.setActionCommand("" + SearchComponent.RESET_DATE);
        clearDate.addActionListener(model);

        fullTextArea = new JTextField(AREA_COLUMNS);
        fullTextArea.addKeyListener(new KeyAdapter() {

            /** Finds the phrase. */
            public void keyPressed(KeyEvent e) {
                Object source = e.getSource();
                if (source != fullTextArea)
                    return;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_ENTER:
                        model.search();
                }
            }
        });


        helpBasicButton = new JButton(icons.getIcon(IconManager.HELP));
        helpBasicButton.setToolTipText("Search Tips.");
        helpBasicButton.setBackground(UIUtilities.BACKGROUND_COLOR);
        UIUtilities.unifiedButtonLookAndFeel(helpBasicButton);
        helpBasicButton.addActionListener(model);
        helpBasicButton.setActionCommand("" + SearchComponent.HELP);

        SearchContext ctx = model.getSearchContext();
        if (ctx == null)
            return;
    }

    /**
     * Resets the date fields
     */
    public void resetDate() {
        toDate.setDate(null);
        fromDate.setDate(null);
    }

    /**
     * Builds the panel hosting the time fields.
     * 
     * @return See above;
     */
    private JPanel buildTimeRange() {
        JPanel p = new JPanel();
        p.setBackground(UIUtilities.BACKGROUND_COLOR);
        p.add(UIUtilities.setTextFont("From: "));
        p.add(fromDate);
        p.add(UIUtilities.setTextFont("To: "));
        p.add(toDate);
        p.add(clearDate);
        return p;
    }

    /**
     * Builds and lays out the component displaying the various options.
     * 
     * @return See above.
     */
    private JPanel buildFields() {
        List<SearchObject> nodes = model.getNodes();
        SearchObject n;
        int m = nodes.size();
        JCheckBox box;
        JPanel p = new JPanel();
        p.setBackground(UIUtilities.BACKGROUND_COLOR);
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1.0;
        c.gridy = 1;
        List<Integer> ctxNodes = null;
        SearchContext ctx = model.getSearchContext();
        if (ctx != null)
            ctxNodes = ctx.getContext();
        if (ctxNodes == null) {
            for (int i = 0; i < m; i++) {
                n = nodes.get(i);
                box = new JCheckBox(n.getDescription());
                box.setBackground(UIUtilities.BACKGROUND_COLOR);

                if (i % 2 == 0) {
                    c.gridy++;
                }

                p.add(box, c);
                scopes.put(n.getIndex(), box);
            }
        } else {
            for (int i = 0; i < m; i++) {
                n = nodes.get(i);
                box = new JCheckBox(n.getDescription());
                box.setBackground(UIUtilities.BACKGROUND_COLOR);
                box.setSelected(ctxNodes.contains(n.getIndex()));

                if (i % 2 == 0)
                    c.gridy++;

                p.add(box, c);
                scopes.put(n.getIndex(), box);
            }
        }
        c.gridy++;
        UIUtilities.setBoldTitledBorder("Restrict by Field", p);
        return p;
    }

    /**
     * Builds and lays out the component displaying the various options.
     * 
     * @return See above.
     */
    private JPanel buildScope() {
        JPanel p = new JPanel();
        p.setBackground(UIUtilities.BACKGROUND_COLOR);
        p.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;

        p.add(new JLabel("Groups:"), c);
        c.gridx = 1;
        p.add(groupsBox, c);

        groupsBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                updateUsersBox();
            }
        });

        c.gridx = 0;
        c.gridy++;

        p.add(new JLabel("Data owned by:"), c);
        c.gridx = 1;
        p.add(usersBox, c);

        UIUtilities.setBoldTitledBorder("Scope", p);

        return p;
    }

    /**
     * Updates the content of the usersBox depending on the current groupsBox
     * selection
     */
    private void updateUsersBox() {
        updateUsersBox(false);
    }

    /**
     * Updates the content of the usersBox depending on the current groupsBox
     * selection
     * 
     * @param reset
     *            If <code>true</code> reset the selection to 'all'
     */
    private void updateUsersBox(boolean reset) {
        ExperimenterContext me = new ExperimenterContext(getUserDetails());
        ExperimenterContext all = new ExperimenterContext("All",
                ExperimenterContext.ALL_EXPERIMENTERS_ID);
        ExperimenterContext selected = (usersBox.getSelectedIndex() != -1 && !reset) ? (ExperimenterContext) usersBox
                .getSelectedItem() : null;

        usersBox.removeAllItems();

        // the users to present in the combobox
        List<ExperimenterContext> items = new ArrayList<ExperimenterContext>();

        // always add 'me' and 'all'
        items.add(all);
        items.add(me);

        // gather the users from the GroupContexts
        if (groupsBox.getSelectedIndex() > -1) {
            GroupContext groupContext = (GroupContext) groupsBox
                    .getSelectedItem();

            List<GroupContext> groupContexts = new ArrayList<GroupContext>();
            if (groupContext.getId() == GroupContext.ALL_GROUPS_ID) {
                // users from all GroupContexts must be added
                for (int i = 0; i < groupsBox.getItemCount(); i++) {
                    groupContext = (GroupContext) groupsBox.getItemAt(i);
                    if (groupContext.getId() == GroupContext.ALL_GROUPS_ID)
                        continue;
                    groupContexts.add(groupContext);
                }
            } else {
                // just users from the selected GroupContext must be added
                groupContexts.add(groupContext);
            }

            for (GroupContext gc : groupContexts) {
                for (ExperimenterContext exp : gc.getExperimenters()) {
                    if (!items.contains(exp)) {
                        items.add(exp);
                    }
                }
            }
        }

        for (ExperimenterContext item : items) {
            usersBox.addItem(item);
        }

        // restore the previous selection if there was any
        if (selected != null && items.contains(selected)) {
            usersBox.setSelectedItem(selected);
        } else {
            usersBox.setSelectedItem(me);
        }
    }

    /**
     * Builds and lays out the component displaying the various types.
     * 
     * @return See above.
     */
    private JPanel buildType() {
        JPanel p = new JPanel();
        p.setBackground(UIUtilities.BACKGROUND_COLOR);
        p.setLayout(new GridBagLayout());
        // p.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(3, 3, 3, 3);
        List<SearchObject> nodes = model.getTypes();
        List<Integer> ctxNodes = null;
        SearchContext ctx = model.getSearchContext();
        if (ctx != null)
            ctxNodes = ctx.getType();

        SearchObject n;
        int m = nodes.size();
        JCheckBox box;
        c.weightx = 1.0;

        if (ctxNodes == null) {
            for (int i = 0; i < m; i++) {
                n = nodes.get(i);
                box = new JCheckBox(n.getDescription());
                box.setBackground(UIUtilities.BACKGROUND_COLOR);
                box.setSelected(true);
                p.add(box, c);
                if (i % 2 == 0)
                    c.gridy++;
                types.put(n.getIndex(), box);
            }
        } else {
            for (int i = 0; i < m; i++) {
                n = nodes.get(i);
                box = new JCheckBox(n.getDescription());
                box.setBackground(UIUtilities.BACKGROUND_COLOR);
                box.setSelected(ctxNodes.contains(n.getIndex()));
                p.add(box, c);
                if (i % 2 == 0)
                    c.gridy++;
                types.put(n.getIndex(), box);
            }
        }

        UIUtilities.setBoldTitledBorder(TYPE_TITLE, p);
        return p;
    }

    /**
     * Builds and lays out the Basic search component.
     * 
     * @return See above.
     */
    private JPanel buildBasicSearchComp() {
        basicSearchComp = new JPanel();
        basicSearchComp.setBackground(UIUtilities.BACKGROUND_COLOR);
        UIUtilities.setBoldTitledBorder("Search", basicSearchComp);
        basicSearchComp.setLayout(new BoxLayout(basicSearchComp,
                BoxLayout.X_AXIS));
        basicSearchComp.add(fullTextArea);
        basicSearchComp.add(helpBasicButton);
        return basicSearchComp;
    }

    /**
     * Builds the UI component hosting the terms to search for.
     * 
     * @return See above.
     */
    private JPanel buildSearchFor() {
        if (searchFor == null) {
            searchFor = new JPanel();
            searchFor.setBackground(UIUtilities.BACKGROUND_COLOR);
            searchFor.setLayout(new BoxLayout(searchFor, BoxLayout.Y_AXIS));
        }
        searchFor.removeAll();
        searchFor.add(buildBasicSearchComp());
        return searchFor;
    }

    /**
     * Builds the UI component hosting the time interval.
     * 
     * @return See above.
     */
    private JPanel buildDate() {

        JPanel p = new JPanel();

        p.setBackground(UIUtilities.BACKGROUND_COLOR);

        p.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        dateBox = new JComboBox();
        dateBox.setToolTipText(DATE_TYPE_TOOLTIP);
        dateBox.addItem(ITEM_IMPORTDATE);
        dateBox.addItem(ITEM_ACQUISITIONDATE);
        p.add(dateBox);

        c.gridy++;

        p.add(buildTimeRange(), c);

        UIUtilities.setBoldTitledBorder("Date", p);
        return p;
    }

    /** Builds and lays out the UI. */
    private void buildGUI() {
        setBorder(null);
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 2, 2, 0);
        c.gridy = 0;
        c.gridx = 0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1.0;
        setBackground(UIUtilities.BACKGROUND_COLOR);

        add(buildSearchFor(), c);// , "0, 0");

        SeparatorPane sep = new SeparatorPane();
        sep.setBackground(UIUtilities.BACKGROUND_COLOR);
        c.gridy++;
        add(sep, c);// , "0, 1");

        JPanel typePanel = buildType();
        c.gridy++;
        add(typePanel, c);// , "0, 2")

        JPanel fieldsPanel = buildFields();
        c.gridy++;
        add(fieldsPanel, c);// , "0, 2");

        JPanel scopePanel = buildScope();
        c.gridy++;
        add(scopePanel, c);// , "0, 2");

        JPanel datePanel = buildDate();
        c.gridy++;
        add(datePanel, c);// , "0, 4");

        updateUsersBox();
    }

    /**
     * Creates a new instance.
     * 
     * @param model
     *            Reference to the model. Mustn't be <code>null</code>.
     */
    SearchPanel(SearchComponent model) {
        if (model == null)
            throw new IllegalArgumentException("No model.");
        this.model = model;
        initComponents();
        buildGUI();
    }

    /** Resets. */
    public void reset() {
        updateUsersBox(true);

        resetDate();

        setTerms(Collections.<String> emptyList());

        for (Entry<Integer, JCheckBox> scope : scopes.entrySet()) {
            scope.getValue().setSelected(false);
        }

        for (Entry<Integer, JCheckBox> type : types.entrySet()) {
            type.getValue().setSelected(true);
        }
    }

    /**
     * Returns the 'from' time.
     * 
     * @return See above.
     */
    Timestamp getFromDate() {
        Date d = fromDate.getDate();
        if (d == null)
            return null;
        return new Timestamp(d.getTime());
    }

    /**
     * Returns the 'to' time.
     * 
     * @return See above.
     */
    Timestamp getToDate() {
        Date d = toDate.getDate();
        if (d == null)
            return null;
        return new Timestamp(d.getTime());
    }

    /**
     * Returns the type of the date (acquisition/import)
     */
    String getDateType() {
        return (String) dateBox.getSelectedItem();
    }

    /**
     * Returns the scope of the search.
     * 
     * @return See above.
     */
    List<Integer> getScope() {
        List<Integer> list = new ArrayList<Integer>();
        Iterator i = scopes.keySet().iterator();
        JCheckBox box;
        Integer key;
        while (i.hasNext()) {
            key = (Integer) i.next();
            box = scopes.get(key);
            if (box.isSelected())
                list.add(key);
        }
        return list;
    }

    /**
     * Returns the scope of the search.
     * 
     * @return See above.
     */
    List<Integer> getType() {
        List<Integer> list = new ArrayList<Integer>();
        Iterator i = types.keySet().iterator();
        JCheckBox box;
        Integer key;
        while (i.hasNext()) {
            key = (Integer) i.next();
            box = types.get(key);
            if (box.isSelected())
                list.add(key);
        }
        return list;
    }

    /**
     * Sets the values to add.
     * 
     * @param values
     *            The values to add.
     */
    void setTerms(List<String> terms) {
        if (CollectionUtils.isEmpty(terms))
            return;

        StringBuffer text = new StringBuffer();
        Iterator<String> i = terms.iterator();
        while (i.hasNext()) {
            text.append(i.next());
            text.append(SearchUtil.SPACE_SEPARATOR);
        }

        fullTextArea.setText(text.toString());
    }

    String getQuery() {
        return fullTextArea.getText();
    }

    /**
     * Returns the terms that may be in the document.
     * 
     * @return See above.
     */
    String[] getQueryTerms() {
        String text;
        text = fullTextArea.getText();
        if (CommonsLangUtils.isNotBlank(text)) {
            List<String> l = SearchUtil.splitTerms(text);
            if (l.size() > 0)
                return l.toArray(new String[0]);
        }

        List<String> l = SearchUtil.splitTerms(text);

        if (l.size() > 0)
            return l.toArray(new String[] {});
        return null;
    }

    /**
     * Returns the current user's details.
     * 
     * @return See above.
     */
    private ExperimenterData getUserDetails() {
        return (ExperimenterData) FinderFactory.getRegistry().lookup(
                LookupNames.CURRENT_USER_DETAILS);
    }

    /**
     * Get the selected groupId
     */
    long getGroupId() {
        long result = GroupContext.ALL_GROUPS_ID;
        if (groupsBox.getSelectedIndex() >= 0) {
            GroupContext g = (GroupContext) groupsBox.getSelectedItem();
            result = g.getId();
        }
        return result;
    }

    /**
     * Get the selected userId
     */
    long getUserId() {
        long result = -1;
        if (usersBox.getSelectedIndex() >= 0) {
            ExperimenterContext u = (ExperimenterContext) usersBox
                    .getSelectedItem();
            result = u.getId() == ExperimenterContext.ALL_EXPERIMENTERS_ID ? -1
                    : u.getId();
        }
        return result;
    }
}
